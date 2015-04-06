package neilw4.omin.ui.follow;

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
import android.widget.EditText;

import com.melnykov.fab.FloatingActionButton;
import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.SimpleSwipeUndoAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.TimedUndoAdapter;

import neilw4.omin.R;
import neilw4.omin.controller.FollowController;


public class FollowFragment extends ListFragment {

    private FollowAdapter adapter;
    private LayoutInflater inflater;

    public static FollowFragment newInstance() {
        FollowFragment fragment = new FollowFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public FollowFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_following, null);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        inflater = getLayoutInflater(savedInstanceState);
        adapter = new FollowAdapter(inflater);
        SimpleSwipeUndoAdapter undoAdapter = new TimedUndoAdapter(adapter, getActivity(), adapter);
        undoAdapter.setAbsListView(getListView());
        setListAdapter(undoAdapter);

        DynamicListView list = (DynamicListView) getListView();
        list.enableSwipeUndo(undoAdapter);

        FloatingActionButton fab = (FloatingActionButton)getView().findViewById(R.id.new_follow_fab);
        fab.attachToListView(list);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SendMessagePopup().show();
            }
        });
    }

    class SendMessagePopup implements DialogInterface.OnClickListener {

        private final View mRoot;
        private final EditText mName;
        private final EditText mId;

        public SendMessagePopup() {
            mRoot = inflater.inflate(R.layout.dialog_new_follow, null);
            mName = (EditText) mRoot.findViewById(R.id.new_follow_name);
            mId = (EditText) mRoot.findViewById(R.id.new_follow_id);
        }

        public void show() {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.new_message)
                    .setView(mRoot)
                    .setPositiveButton(R.string.follow, this)
                    .setNegativeButton(android.R.string.cancel, this)
                    .setCancelable(true)
                    .create().show();

            // hack to force the keyboard to show
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    mName.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                    mName.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
                }
            }, 50);
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                String name = mName.getText().toString();
                String id = mId.getText().toString();

                if (FollowController.followUser(name, id)) {
                    adapter.notifyDataSetChanged();
                }
            }
        }

    }

}
