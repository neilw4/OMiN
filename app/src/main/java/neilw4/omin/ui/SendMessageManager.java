package neilw4.omin.ui;

import android.app.Activity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.orm.query.Select;

import java.sql.Timestamp;
import java.util.Date;

import neilw4.omin.R;
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

        PrivateKey key = Select.from(PrivateKey.class).first();
        if (key == null) {
            warn(TAG, "Couldn't send message: no user id");
            return;
        }

        Message msg = new Message("sig", body, new Timestamp(new Date().getTime()));
        msg.save();
        new MessageUid(Select.from(PrivateKey.class).first().uid, msg).save();

        msg_text.getText().clear();
        info(TAG, "send message: " + body);
    }
}
