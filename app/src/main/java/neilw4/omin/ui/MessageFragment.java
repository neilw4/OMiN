package neilw4.omin.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import com.melnykov.fab.FloatingActionButton;
import com.orm.query.Select;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import neilw4.omin.R;
import neilw4.omin.connection.ConnectionServiceStarter;
import neilw4.omin.crypto.sign.Signer;
import neilw4.omin.db.Database;
import neilw4.omin.db.Message;
import neilw4.omin.db.MessageUid;
import neilw4.omin.db.SecretKey;
import neilw4.omin.ui.dummy.DummyContent;

import static neilw4.omin.Logger.*;

public class MessageFragment extends ListFragment {

    private LayoutInflater inflater;

    public static MessageFragment newInstance() {
        MessageFragment fragment = new MessageFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public MessageFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: Change Adapter to display your content
        setListAdapter(new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, DummyContent.ITEMS));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_messages, null);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        inflater = getLayoutInflater(savedInstanceState);
        FloatingActionButton fab = (FloatingActionButton)getView().findViewById(R.id.new_message_fab);
        fab.attachToListView(getListView());
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SendMessagePopup().show();
            }
        });
    }

    class SendMessagePopup implements DialogInterface.OnClickListener {

        String TAG = SendMessagePopup.class.getSimpleName();

        private final View mRoot;
        private final EditText mMessageContent;

        public SendMessagePopup() {
            mRoot = inflater.inflate(R.layout.dialog_new_message, null);
            mMessageContent = (EditText) mRoot.findViewById(R.id.new_message_content);
        }

        public void show() {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.new_message)
                    .setView(mRoot)
                    .setPositiveButton(R.string.send, this)
                    .setNegativeButton(android.R.string.cancel, this)
                    .setCancelable(true)
                    .create().show();

            // hack to force the keyboard to show
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    mMessageContent.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                    mMessageContent.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
                }
            }, 50);

        }

        public void sendMessage() {
            String body = mMessageContent.getText().toString();
            Database.sendMessage(body, getActivity());
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                sendMessage();
            }
        }

    }

}
