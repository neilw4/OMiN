package neilw4.omin.db;

import android.util.JsonReader;
import android.util.JsonWriter;

import com.orm.MySugarTransactionHelper;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.List;
import java.util.regex.Pattern;

import static junit.framework.Assert.*;

public class Message extends SugarRecord<Message> {

    public int distributionCount = 0;
    public boolean read = false;
    public User fromUser;
    public String signature;
    public String body;
    public Timestamp sent;

    public Message(User fromUser, String signature, String body, Timestamp sent) {
        this.fromUser = fromUser;
        this.signature = signature;
        this.body = body;
        this.sent = sent;
    }

    @SuppressWarnings("unused")
    public Message() {
        // Sugar ORM requires an empty constructor.
    }

    public static Message read(InputStream in) throws IOException {
        final JsonReader reader = new JsonReader(new InputStreamReader(in));
        try {
            reader.setLenient(false);
            return MySugarTransactionHelper.doInTransaction(new MySugarTransactionHelper.Callback<Message>() {
                @Override
                public Message manipulateInTransaction() throws IOException {
                    return read(reader);
                }
            });
        } finally {
            reader.close();
        }
    }

    public static Message read(JsonReader reader) throws IOException {
        reader.beginObject();

        // Get the user ids and infer the user.
        assertEquals("fromUser", reader.nextName());
        List<UserId> uids = UserId.readUids(reader);
        assertFalse(uids.isEmpty());
        // All uids point to the same user.
        User fromUser = uids.get(0).user;

        assertEquals("signature", reader.nextName());
        String signature = reader.nextString();
        assertEquals("body", reader.nextName());
        String body = reader.nextString();
        assertEquals("sent", reader.nextName());
        Timestamp sent = new Timestamp(reader.nextLong());

        reader.endObject();

        // Pull message from database if it exists.
        Message msg = Select.from(Message.class).where(Condition.prop("signature").eq(signature))
                .first();

        if (msg == null) {
            // Message doesn't exist in database - create it.
            msg = new Message(fromUser, signature, body, sent);
            msg.save();
            MessageUid.makeMsgUids(uids, msg);
        }
        return msg;
    }

    public void write(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("fromUser");
        MessageUid.writeMsgUids(writer, this);
        writer.name("signature").value(signature);
        writer.name("body").value(body);
        writer.name("sent").value(sent.getTime());
        writer.endObject();

        // Message is being sent, increment the distribution count.
        distributionCount++;
        save();
    }

}
