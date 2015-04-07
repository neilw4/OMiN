package neilw4.omin.controller;

import android.content.Context;

import com.google.common.collect.Lists;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.ArrayList;
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

    private static OnMessagesChangedListener listener;

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
        } else {
            ConnectionServiceStarter.start(context.getApplicationContext());
        }
        info(TAG, "new message " + msg);
        return true;
    }

    public static List<Message> getMessages() {
        List<Message> allMessages = Lists.reverse(Select.from(Message.class).orderBy("sent").list());
        List<Message> showMessages = new ArrayList<>();
        for (Message message: allMessages) {
            List<MessageUid> msgUids = Select.from(MessageUid.class).where(Condition.prop("msg").eq(message.getId())).list();
            boolean showMessage = msgUids.isEmpty(); // Show anonymous messages.
            for (MessageUid msgUid: msgUids) {
                if (msgUid.uid.user != null && msgUid.uid.user.following) {
                    showMessage = true;
                    break;
                } else if (Select.from(SecretKey.class).where(Condition.prop("uid").eq(msgUid.uid.getId())).first() != null) {
                    showMessage = true;
                    break;
                }
            }
            if (showMessage) {
                showMessages.add(message);
            }
        }
        return showMessages;
    }

    public static void onMessagesChanged() {
        if (listener != null) {
            listener.onMessagesChanged();
        }
    }

    public static void addChangeListener(OnMessagesChangedListener listener) {
        MessageController.listener = listener;
    }

    public static void removeChangeListener() {
        listener = null;
    }

    public interface OnMessagesChangedListener {
        public void onMessagesChanged();
    }

}
