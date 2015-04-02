package neilw4.omin.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.SystemClock;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.orm.SugarTransactionHelper;
import com.orm.query.Condition;
import com.orm.query.Select;

import neilw4.omin.R;
import neilw4.omin.db.SecretKey;
import neilw4.omin.db.UserId;
import neilw4.omin.fetch_key.FetchKey;

import static neilw4.omin.Logger.*;

public class UnameManager {

    final static String TAG = UnameManager.class.getSimpleName();

    final Context mContext;

    final EditText mEditUname;
    final ImageButton mToggleEdit;

    final Drawable editTextBg;
    private final InputMethodManager mInputManager;

    boolean editable = false;
    String uname = null;

    public UnameManager(Context context, View root) {
        mContext = context;
        mEditUname = (EditText) root.findViewById(R.id.edit_uname_text);
        mToggleEdit = (ImageButton) root.findViewById(R.id.edit_uname_toggle);

        mInputManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);

        editTextBg = mEditUname.getBackground();

        SecretKey myKey = getKey();
        if (myKey != null) {
            uname = myKey.uid.uname;
        }

        mEditUname.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    endEdit();
                    return true;
                }
                return false;
            }
        });

        mEditUname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeEditable();
            }
        });

        mToggleEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editable) {
                    endEdit();
                } else {
                    makeEditable();
                }
            }
        });

        configureView();
    }

    public void configureView() {
        if (editable) {
            mEditUname.setBackgroundDrawable(editTextBg);
            mEditUname.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            mToggleEdit.setImageResource(R.drawable.tick);
        } else {
            mEditUname.setBackgroundResource(android.R.color.transparent);
            mEditUname.setInputType(InputType.TYPE_NULL);
            mToggleEdit.setImageResource(R.drawable.edit);
        }

        if (uname == null) {
            if (editable) {
                mEditUname.setText("");
            } else {
                mEditUname.setText(R.string.anonymous_uname);
            }
        } else {
            mEditUname.setText(uname);
        }
    }

    public void makeEditable() {
        if (!editable) {
            editable = true;
            configureView();

            // hack to force the keyboard to show
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    mEditUname.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                    mEditUname.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));

                }
            }, 50);
        }
    }



    public void endEdit() {
        if (editable) {
            editable = false;

            uname = mEditUname.getText().toString();
            if (uname.isEmpty()) {
                uname = null;
            }
            mEditUname.clearFocus();
            mInputManager.hideSoftInputFromWindow(mEditUname.getWindowToken(), 0);
            saveUname();
            configureView();
        }
    }

    private SecretKey getKey() {
        return Select.from(SecretKey.class).first();
    }

    public boolean saveUname() {
        if (uname != null && !UserId.valid(uname)) {
            uname = null;
            warn(TAG, "Invalid username format");
            return false;
        }

        new Thread() {
            @Override
            public void run() {
                SugarTransactionHelper.doInTansaction(new SugarTransactionHelper.Callback() {
                    @Override
                    public void manipulateInTransaction() {
                        SecretKey myKey = getKey();
                        if (myKey == null || !myKey.uid.uname.equals(uname)) {
                            if (myKey != null) {
                                deleteKey(myKey);
                            }
                            if (uname != null) {
                                UserId uid = makeUid();
                                SecretKey key = new SecretKey(uid);
                                key.save();
                                info(TAG, "new uname");
                                FetchKey.asyncFetch();
                            }
                        }
                    }
                });
            }
        }.start();
        return true;
    }

    private UserId makeUid() {
        UserId uid = Select.from(UserId.class).where(
                Condition.prop("uname").eq(uname),
                Condition.prop("parent").notLike("%")
        ).first();

        if (uid == null) {
            uid = new UserId(uname, null);
            debug(TAG, "Created new uid " + uname);
            uid.save();
        } else {
            debug(TAG, "Found existing uid " + uname);
        }
        return uid;
    }

    private void deleteKey(SecretKey key) {
        if (key != null) {
            debug(TAG, "deleting key " + key.uid.uname);
            for (UserId toDelete = key.uid; toDelete != null; toDelete = toDelete.parent) {
                debug(TAG, "checking parent id " + toDelete.uname);
                Select<UserId> children = Select.from(UserId.class).where(Condition.prop("parent").eq(toDelete.getId()));
                if (children.count() == 0) {
                    debug(TAG, "deleting parent id " + toDelete.uname);
                    toDelete.delete();
                }
            }
            key.delete();
        }
    }
}
