package neilw4.omin.ui.message;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.List;
import java.util.Locale;

import neilw4.omin.R;
import neilw4.omin.controller.MessageController;
import neilw4.omin.controller.UnameController;
import neilw4.omin.db.Message;
import neilw4.omin.db.UserId;

public class MessageAdapter extends BaseAdapter {

    private final LayoutInflater inflater;
    private List<Message> messages;

    public MessageAdapter(LayoutInflater inflater) {
        super();
        updateMessages();
        this.inflater = inflater;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Message getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message msg = getItem(position);
        if (convertView != null) {
            try {
                return setText(convertView, msg);
            } catch (NullPointerException | ClassCastException e) {
                // Unexpected view type - move on and generate a new view instead.
            }
        }
        View v = inflater.inflate(R.layout.listitem_message, parent, false);
        return setText(v, msg);
    }

    private View setText(final View v, final Message msg) {
        UserId uid = UnameController.getUidFor(msg);
        String user_name;
        if (uid == null) {
            user_name = "(anonymous)";
        } else if (uid.user != null && uid.user.name != null) {
            user_name = uid.user.name;
        } else {
            user_name = uid.uname;
        }
        TextView user = (TextView) v.findViewById(R.id.message_user);
        user.setText(user_name);


        PrettyTime t = new PrettyTime(Locale.getDefault());
        TextView time = (TextView) v.findViewById(R.id.message_time);
        time.setText(t.format(msg.sent));

        TextView body = (TextView) v.findViewById(R.id.message_body);
        body.setText(msg.body);
        return v;
    }

    @Override
    public void notifyDataSetChanged() {
        updateMessages();
        super.notifyDataSetChanged();
    }

    private void updateMessages() {
        messages = MessageController.getMessages();
    }

}
