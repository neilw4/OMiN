package neilw4.omin.ui.message;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.melnykov.fab.FloatingActionButton;

import neilw4.omin.R;
import neilw4.omin.connection.ConnectionServiceStarter;
import neilw4.omin.controller.MessageController;
import neilw4.omin.ui.Refreshable;

import static neilw4.omin.Logger.warn;


public class MessageFragment extends ListFragment implements Refreshable, MessageController.OnMessagesChangedListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = MessageFragment.class.getSimpleName();
    private MessageAdapter adapter;
    private LayoutInflater inflater;
    private SwipeRefreshLayout swipeRefreshLayout;

    public static MessageFragment newInstance() {
        MessageFragment fragment = new MessageFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public MessageFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_messages, null);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        inflater = getLayoutInflater(savedInstanceState);
        adapter = new MessageAdapter(inflater);
        setListAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton)getView().findViewById(R.id.new_message_fab);
        fab.attachToListView(getListView());
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SendMessagePopup().show();
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.refresh_messages);
        swipeRefreshLayout.setOnRefreshListener(this);

    }

    @Override
    public void onStart() {
        super.onStart();
        MessageController.addChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        MessageController.removeChangeListener();
    }

    @Override
    public void onRefresh() {
        refresh();
        ConnectionServiceStarter.start(getActivity().getApplicationContext());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 700);
    }

    @Override
    public void refresh() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onMessagesChanged() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    ((Refreshable) getActivity()).refresh();
                } catch (NullPointerException | ClassCastException e) {
                    warn(TAG, "Error while refreshing messages", e);
                }
            }
        });
    }

    class SendMessagePopup implements DialogInterface.OnClickListener {

        private final String TAG = SendMessagePopup.class.getSimpleName();

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

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                String body = mMessageContent.getText().toString();
                if (MessageController.sendMessage(body, getActivity())) {
                    try {
                        ((Refreshable) getActivity()).refresh();
                    } catch (NullPointerException | ClassCastException e) {
                        warn(TAG, "Error while refreshing messages", e);
                    }
                }
            }
        }

    }

}
