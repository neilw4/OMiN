package neilw4.omin.db;

import android.util.JsonReader;
import android.util.JsonWriter;

import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import neilw4.omin.crypto.sign.Signer;
import neilw4.omin.datastructure.BloomFilter;

import static junit.framework.Assert.*;
import static neilw4.omin.Logger.*;

public class Message extends SugarRecord<Message> {

    public static final String TAG = Message.class.getSimpleName();

    public static enum Security {UNVERIFIED, SECURE, INSECURE}

    public String body;
    public Timestamp sent;
    public Timestamp lastSent = null;
    public Security security = Security.UNVERIFIED;


    public Message(String body, Timestamp sent) {
        this.body = body;
        this.sent = sent;
        updateSent();
    }

    @SuppressWarnings("unused")
    public Message() {
        // Sugar ORM requires an empty constructor.
    }

    private void updateSent() {
        // Record that the message is being sent.
        this.lastSent = new Timestamp(new Date().getTime());
    }

    private static Message findInDb(String body, Timestamp sent, List<MessageUid> msgUids) {
        for (MessageUid msgUid: msgUids) {
            MessageUid matchMsgUid = Select.from(MessageUid.class)
                    .where(
                            Condition.prop("uid").eq(msgUid.uid),
                            Condition.prop("signature").eq(msgUid.signature)
                    ).first();
            if (matchMsgUid == null) {
                return null;
            }
            Message msg = matchMsgUid.msg;
            if (msg.body.equals(body) && msg.sent.equals(sent)) {
                return msg;
            }
        }
        return null;
    }

    public static Message read(JsonReader reader) throws IOException {
        reader.beginObject();

        assertEquals("body", reader.nextName());
        String body = reader.nextString();
        assertEquals("sent", reader.nextName());
        Timestamp sent = new Timestamp(reader.nextLong());

        // Get the user ids and infer the user.
        assertEquals("user", reader.nextName());
        List<MessageUid> msgUids = MessageUid.readUnsavedMessageUids(reader);
        assertFalse(msgUids.isEmpty());

        reader.endObject();

        // Pull message from database if it exists.
        Message msg = findInDb(body, sent, msgUids);

        if (msg == null) {
            // Message doesn't exist in database - create it.
            msg = new Message(body, sent);
            msg.save();

            List<UserId> uids = new ArrayList<>(msgUids.size());
            for (MessageUid msgUid: msgUids) {
                msgUid.msg = msg;
                msgUid.save();
                uids.add(msgUid.uid);
            }
            User.consolidateUserIds(uids);
            Signer.asyncVerify(msg);
            info(TAG, "received message " + sent + " from " + Arrays.toString(uids.toArray()));
        } else {
            debug(TAG, "message " + sent + "already exists");
        }
        return msg;
    }

    public void write(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("body").value(body);
        writer.name("sent").value(sent.getTime());
        writer.name("user");
        List<MessageUid> msgUids = Select.from(MessageUid.class).where(Condition.prop("msg").eq(getId())).list();
        MessageUid.writeMsgUids(writer, msgUids);
        writer.endObject();

        updateSent();

        debug(TAG, "sent message " + body);
        save();
    }

    @Override
    public void save() {
        assertNotNull(body);
        assertNotNull(sent);
        super.save();

        BloomFilter<Message> filter = Messages.getFilter();
        filter.put(this);
        Messages.setFilter(filter);

        Messages.evict();
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

    @Override
    public int hashCode() {
        return body.hashCode() ^ (int)sent.getTime();
    }

}
