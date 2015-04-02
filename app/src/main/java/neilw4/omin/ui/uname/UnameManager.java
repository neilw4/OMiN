package neilw4.omin.ui.uname;

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

import neilw4.omin.R;
import neilw4.omin.controller.UnameController;
import neilw4.omin.db.SecretKey;

public class UnameManager {

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

        SecretKey myKey = UnameController.getSecretKey();
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
            if (!UnameController.setUname(uname)) {
                uname = null;
            }
            configureView();
        }
    }

}
