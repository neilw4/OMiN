package neilw4.omin.db;

import android.util.JsonWriter;

import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.*;

public class MessageUid extends SugarRecord<MessageUid> {

    public UserId uid;
    public Message msg;

    public MessageUid(UserId uid, Message msg) {
        this.uid = uid;
        this.msg = msg;
    }

    @SuppressWarnings("unused")
    public MessageUid() {
        // Sugar ORM requires an empty constructor.
    }

    protected static void makeMsgUids(List<UserId> uids, Message msg) {
        for (UserId uid: uids) {
            // Pull message-uid from database if it exists.
            MessageUid msgUid = Select.from(MessageUid.class).where(
                    Condition.prop("uid").eq(uid.getId()),
                    Condition.prop("msg").eq(msg.getId())
                ).first();

            if (msgUid == null) {
                // Doesn't exist in database - create it.
                msgUid = new MessageUid(uid, msg);
                msgUid.save();
            }
        }
    }

    protected static void writeMsgUids(JsonWriter writer, Message msg) throws IOException {
        writer.beginArray();
        for (MessageUid msgUid:
                Select.from(MessageUid.class).where(Condition.prop("msg").eq(msg.getId())).list()) {
            msgUid.uid.write(writer);
        }
        writer.endArray();
    }

    @Override
    public void save() {
        assertNotNull(uid);
        assertNotNull(msg);
        super.save();
    }

}
