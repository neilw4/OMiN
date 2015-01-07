package neilw4.omin.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.orm.query.Select;

import java.util.List;

import neilw4.omin.db.Message;

public class ViewMessageAdapter extends BaseAdapter {

    private final List<Message> messages = Select.from(Message.class).orderBy("sent").list();
    private final Context context;

    public ViewMessageAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView v = new TextView(context);
        Message msg = (Message)getItem(position);
        v.setText(msg.body);
        return v;
    }
}
