package neilw4.omin.ui;

import android.app.Activity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.orm.query.Condition;
import com.orm.query.Select;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import neilw4.omin.R;
import neilw4.omin.crypto.sign.Signer;
import neilw4.omin.db.Message;
import neilw4.omin.db.MessageUid;
import neilw4.omin.db.PrivateKey;

import static neilw4.omin.Logger.*;

public class SendMessageManager {
    public static final String TAG = SendMessageManager.class.getSimpleName();

    private final Activity context;

    private EditText msg_text;
    private Button msg_button;


    public SendMessageManager(Activity context) {
        this.context = context;
    }

    public void setup() {
        msg_text = (EditText)context.findViewById(R.id.send_message_text);
        msg_button = (Button)context.findViewById(R.id.send_message_button);

        msg_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        String body = msg_text.getText().toString();

        if (body.length() == 0) {
            warn(TAG, "Couldn't send message: no content");
            return;
        }

        List<PrivateKey> keys = Select.from(PrivateKey.class).where(Condition.prop("PS06_KEY").like("%")).list();
        if (keys.isEmpty()) {
            warn(TAG, "Couldn't send message: no user id with secret key");
            return;
        }

        Message msg = new Message(body, new Timestamp(new Date().getTime()));
        msg.save();

        for (PrivateKey key: keys) {
            MessageUid uid = new MessageUid(key.uid, msg);
            uid.save();
        }

        Signer.asyncSign(msg);

        msg_text.getText().clear();
        info(TAG, "new message: " + msg.sent);
    }
}
