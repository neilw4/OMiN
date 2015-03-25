package neilw4.omin.db;

import android.util.JsonReader;
import android.util.JsonWriter;

import com.orm.SugarRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.*;

public class MessageUid extends SugarRecord<MessageUid> {

    public UserId uid;
    public String signature;
    public Message msg;

    public MessageUid(UserId uid, Message msg) {
        this(uid, null, msg);
    }

    public MessageUid(UserId uid, String signature) {
        this(uid, signature, null);
    }

    public MessageUid(UserId uid, String signature, Message msg) {
        this.uid = uid;
        this.msg = msg;
        this.signature = signature;
    }

    @SuppressWarnings("unused")
    public MessageUid() {
        // Sugar ORM requires an empty constructor.
    }

    protected static List<MessageUid> readUnsavedMessageUids(JsonReader reader) throws IOException {
        List<MessageUid> msgUids = new ArrayList<>();
        reader.beginArray();
        while(reader.hasNext()) {
            msgUids.add(readUnsaved(reader));
        }

        reader.endArray();
        return msgUids;
    }

    protected static MessageUid readUnsaved(JsonReader reader) throws IOException {
        reader.beginObject();
        assertEquals("uid", reader.nextName());
        UserId uid = UserId.read(reader);
        assertEquals("signature", reader.nextName());
        String signature = reader.nextString();
        reader.endObject();
        return new MessageUid(uid, signature);
    }

    protected static void writeMsgUids(JsonWriter writer, List<MessageUid> msgUids) throws IOException {
        writer.beginArray();
        for (MessageUid msgUid: msgUids) {
            msgUid.write(writer);
        }
        writer.endArray();
    }

    protected void write(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("uid");
        uid.write(writer);
        writer.name("signature");
        writer.value(signature);
        writer.endObject();
    }

    @Override
    public void save() {
        assertNotNull(uid);
        assertNotNull(msg);
        super.save();
    }

}
