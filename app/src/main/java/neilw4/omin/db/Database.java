package neilw4.omin.db;

import android.content.Context;

import com.google.common.collect.Lists;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.Date;
import java.util.List;

import neilw4.omin.connection.ConnectionServiceStarter;
import neilw4.omin.crypto.sign.Signer;

import static neilw4.omin.Logger.info;
import static neilw4.omin.Logger.warn;

public class Database {

    private static final String TAG = Database.class.getSimpleName();

    public static boolean sendMessage(String body, final Context context) {

        if (body.length() == 0) {
            warn(TAG, "Couldn't send message: no content");
            return false;
        }

        List<SecretKey> keys = Select.from(SecretKey.class).list();

        Message msg = new Message(body, new Date());
        msg.save();

        for (SecretKey key: keys) {
            if (key.ps06Key != null) {
                MessageUid uid = new MessageUid(key.uid, msg);
                uid.save();
            }
        }

        Signer.setResources(context.getResources());
        Signer.asyncSign(msg, new Signer.Callback() {
            @Override
            public void onSuccess() {
                // Initiate scan for nearby devices.
                ConnectionServiceStarter.start(context.getApplicationContext());
            }

            @Override
            public void onFail() {}
        });
        info(TAG, "new message " + msg);
        return true;
    }

    public static List<Message> getMessages() {
        //TODO: only interested messages
        return Lists.reverse(Select.from(Message.class).orderBy("sent").list());
    }

    public static UserId getUidFor(Message msg) {
        MessageUid msgUid = Select.from(MessageUid.class).where(Condition.prop("msg").eq(msg.getId())).first();
        if (msgUid != null) {
            return msgUid.uid;
        }
        return null;
    }
}
