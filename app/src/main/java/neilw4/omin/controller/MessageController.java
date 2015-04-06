package neilw4.omin.controller;

import android.content.Context;

import com.google.common.collect.Lists;
import com.orm.query.Select;

import java.util.Date;
import java.util.List;

import neilw4.omin.connection.ConnectionServiceStarter;
import neilw4.omin.crypto.sign.Signer;
import neilw4.omin.db.Message;
import neilw4.omin.db.MessageUid;
import neilw4.omin.db.SecretKey;

import static neilw4.omin.Logger.*;

public class MessageController {

    private static final String TAG = MessageController.class.getSimpleName();

    public static boolean sendMessage(String body, final Context context) {

        if (body.length() == 0) {
            warn(TAG, "Couldn't send message: no content");
            return false;
        }


        SecretKey key = UnameController.getSecretKey();


        if (key != null && key.ps06Key == null) {
            warn(TAG, "No secret key for user. Try sending an anonymous message.");
            return false;
        }

        Message msg = new Message(body, new Date());
        msg.save();

        if (key != null) {
            MessageUid uid = new MessageUid(key.uid, msg);
            uid.save();

            Signer.setResources(context.getResources());
            Signer.asyncSign(msg, new Signer.Callback() {
                @Override
                public void onSuccess() {
                    // Initiate scan for nearby devices.
                    ConnectionServiceStarter.start(context.getApplicationContext());
                }

                @Override
                public void onFail() {
                }
            });
        }
        info(TAG, "new message " + msg);
        return true;
    }

    public static List<Message> getMessages() {
        //TODO: only following messages
        return Lists.reverse(Select.from(Message.class).orderBy("sent").list());
    }

}
