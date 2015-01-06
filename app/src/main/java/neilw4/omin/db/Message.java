package neilw4.omin.db;

import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import com.orm.MySugarTransactionHelper;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import static junit.framework.Assert.*;

public class Message extends SugarRecord<Message> {

    public static final int MESSAGE_BUFFER_SIZE = 10;
    public static final String TAG = Message.class.getSimpleName();

    public String signature;
    public String body;
    public Timestamp sent;
    public Timestamp lastSent = null;

    public Message(String signature, String body, Timestamp sent) {
        this.signature = signature;
        this.body = body;
        this.sent = sent;
        this.lastSent = new Timestamp(new Date().getTime());
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
            msg = new Message(signature, body, sent);
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

        // Record that the message is being sent.
        lastSent = new Timestamp(new Date().getTime());
        save();
    }

    private void evict() {
        try {
            MySugarTransactionHelper.doInTransaction(new MySugarTransactionHelper.Callback<Void>() {
                @Override
                public Void manipulateInTransaction() {
                    Select<Message> buffer = Select.from(Message.class).orderBy("last_sent");
                        while (buffer.count() > MESSAGE_BUFFER_SIZE) {
                            Message evict = buffer.first();
                            Log.d(TAG, "Evicted message " + evict);
                            evict.delete();
                        }
                    return null;
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "Failure running eviction strategy: " + e.getMessage());
        }
    }

    @Override
    public void save() {
        assertNotNull(signature);
        assertNotNull(body);
        assertNotNull(sent);
        super.save();

        evict();
    }

    @Override
    public String toString() {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        try {
            write(jsonWriter);
            jsonWriter.close();
        } catch (IOException e) {
            return body;
        }
        return stringWriter.toString();
    }

}
