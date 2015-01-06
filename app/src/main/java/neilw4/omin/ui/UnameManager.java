package neilw4.omin.ui;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.orm.SugarTransactionHelper;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.regex.Pattern;

import neilw4.omin.R;

import neilw4.omin.db.PrivateKey;
import neilw4.omin.db.UserId;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class UnameManager {

    public static final String TAG = UnameManager.class.getSimpleName();

    private final Activity context;

    private EditText unameText;
    private Button unameButton;

    public UnameManager(Activity context) {
        this.context = context;
    }

    public void setup() {
        unameText = (EditText)context.findViewById(R.id.uname_text);
        unameButton = (Button)context.findViewById(R.id.uname_button);

        unameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUname(unameText.getText().toString());
            }
        });

        unameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                setButtonEnabled(s.toString());
            }
        });

        PrivateKey myKey = getKey();
        if (myKey != null) {
            unameText.setText(myKey.uid.uname);
        }
    }

    public void setUname(final String uname) {
        assertNotNull(uname);
        assertTrue(UserId.valid(uname));
        SugarTransactionHelper.doInTansaction(new SugarTransactionHelper.Callback() {
            @Override
            public void manipulateInTransaction() {
                PrivateKey myKey = getKey();
                if (myKey == null || !myKey.uid.uname.equals(uname)) {
                    if (myKey != null) {
                        deleteKey(myKey);
                    }
                    UserId uid = getUid(uname);
                    new PrivateKey(uid, null).save();
                    setButtonEnabled(unameText.getText().toString());
                }
            }
        });
    }

    private PrivateKey getKey() {
        return Select.from(PrivateKey.class).first();
    }

    private void deleteKey(PrivateKey key) {
        if (key != null) {
            Log.i(TAG, "deleting key " + key.uid.uname);
            for (UserId toDelete = key.uid; toDelete != null; toDelete = toDelete.parent) {
                Log.i(TAG, "checking parent id " + toDelete.uname);
                Select<UserId> children = Select.from(UserId.class).where(Condition.prop("parent").eq(toDelete.getId()));
                if (children.count() == 0) {
                    Log.i(TAG, "deleting parent id " + toDelete.uname);
                    toDelete.delete();
                }
            }
            key.delete();
        }
    }

    private UserId getUid(String uname) {
        UserId uid = Select.from(UserId.class).where(
                Condition.prop("uname").eq(uname),
                Condition.prop("parent").notLike("%")
            ).first();

        if (uid == null) {
            uid = new UserId(uname, null);
            Log.i(TAG, "Created new uid " + uname);
            uid.save();
        } else {
            Log.i(TAG, "Found existing uid " + uname);
        }
        return uid;
    }

    private void setButtonEnabled(String newUname) {
        PrivateKey myKey = getKey();
        if (myKey != null && newUname.equals(myKey.uid.uname)) {
            unameButton.setEnabled(false);
        } else if (!UserId.valid(newUname)) {
            unameButton.setEnabled(false);
        } else {
            unameButton.setEnabled(true);
        }

    }

}
