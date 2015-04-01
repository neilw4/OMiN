package neilw4.omin.db;

import android.content.Context;

import com.orm.query.Select;

import java.sql.Timestamp;
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
        if (keys.isEmpty()) {
            warn(TAG, "Couldn't send message: no user id");
            return false;
        }

        Message msg = new Message(body, new Timestamp(new Date().getTime()));
        msg.save();

        for (SecretKey key: keys) {
            MessageUid uid = new MessageUid(key.uid, msg);
            uid.save();
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
}
