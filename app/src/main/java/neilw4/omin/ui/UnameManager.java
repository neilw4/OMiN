package neilw4.omin.ui;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.orm.query.Select;

import neilw4.omin.R;
import neilw4.omin.db.MyUser;
import neilw4.omin.db.User;
import neilw4.omin.db.UserId;

public class UnameManager {

    private EditText unameText;
    private Button unameSet;
    private Activity context;

    public UnameManager(Activity context) {
        this.context = context;
    }

    public void setup() {
        unameText = (EditText)context.findViewById(R.id.uname_text);
        unameSet = (Button)context.findViewById(R.id.uname_set);

        MyUser myUser = Select.from(MyUser.class).first();

        if (myUser != null) {
            unameText.setText(myUser.myUser.name);
        } else {
            unameText.setText("");
        }

        setUnameSetText();
        unameSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUname(unameText.getText().toString());
            }
        });
    }

    private void setUnameSetText() {
        MyUser myUser = Select.from(MyUser.class).first();

        if (myUser != null) {
            unameSet.setText(R.string.add_username);
        } else {
            unameSet.setText(R.string.set_username);
        }
    }

    public void setUname(String uname) {
        MyUser myUser = Select.from(MyUser.class).first();
        if (myUser == null) {
            User user = new User(uname);
            user.save();
            myUser = new MyUser(user);
            myUser.save();
        }

        User user = myUser.myUser;

        user.name = uname;
        user.save();

        UserId id = new UserId(uname, null);
        id.user = user;
        id.save();

        setUnameSetText();
    }

}
