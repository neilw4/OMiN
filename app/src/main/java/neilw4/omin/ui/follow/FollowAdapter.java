package neilw4.omin.ui.follow;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.common.base.Joiner;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter;

import java.util.List;

import neilw4.omin.R;
import neilw4.omin.controller.FollowController;
import neilw4.omin.db.User;
import neilw4.omin.db.UserId;

public class FollowAdapter extends BaseAdapter implements UndoAdapter, OnDismissCallback {

    private final LayoutInflater inflater;
    private List<User> following;

    public FollowAdapter(LayoutInflater inflater) {
        super();
        updateFollowing();
        this.inflater = inflater;
    }

    @Override
    public int getCount() {
        return following.size();
    }

    @Override
    public User getItem(int position) {
        return following.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        User usr = getItem(position);
        if (convertView != null) {
            try {
                return setText(convertView, usr);
            } catch (NullPointerException | ClassCastException e) {
                // Unexpected view type - move on and generate a new view instead.
            }
        }
        View v = inflater.inflate(R.layout.listitem_follow, parent, false);
        return setText(v, usr);
    }

    private View setText(final View v, final User user) {
        List<UserId> uids = FollowController.getUids(user);

        TextView uname = (TextView) v.findViewById(R.id.follow_uname);
        uname.setText(user.name);

        TextView uid = (TextView) v.findViewById(R.id.follow_uids);
        String uidsText = Joiner.on(", ").join(uids);
        uid.setText(uidsText);

        TextView msgCount = (TextView) v.findViewById(R.id.follow_count_message);
        int messages = FollowController.countMessages(user);
        if (messages > 0) {
            Resources r = v.getContext().getApplicationContext().getResources();
            int suffix = messages == 1 ? R.string.message_singular : R.string.message_plural;
            String messagesText = messages + " " + r.getString(suffix);
            msgCount.setText(messagesText);
        } else {
            msgCount.setText("");
        }

        return v;
    }

    @Override
    public void notifyDataSetChanged() {
        updateFollowing();
        super.notifyDataSetChanged();
    }

    public void updateFollowing() {
        following = FollowController.getFollowing();
    }

    @NonNull
    @Override
    public View getUndoView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.undo_row, parent, false);
        }
        TextView text = (TextView) view.findViewById(R.id.undo_row_texttv);

        Resources r = text.getResources();
        text.setText(r.getString(R.string.user_unfollowed) + " " + following.get(position).name);
        return view;
    }

    @NonNull
    @Override
    public View getUndoClickView(@NonNull View undoView) {
        return undoView.findViewById(R.id.undo_row_undobutton);
    }

    @Override
    public void onDismiss(@NonNull ViewGroup listView, @NonNull int[] positions) {
        for (int position: positions) {
            FollowController.unfollow(following.get(position));
        }
        updateFollowing();
    }
}
