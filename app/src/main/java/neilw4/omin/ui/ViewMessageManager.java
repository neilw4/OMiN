package neilw4.omin.ui;

import android.app.Activity;
import android.widget.ListView;

import neilw4.omin.R;

public class ViewMessageManager {
    private final Activity context;

    private ListView messageList;

    public ViewMessageManager(Activity context) {
        this.context = context;
    }

    public void setup() {
        messageList = (ListView)context.findViewById(R.id.message_list);
        messageList.setAdapter(new ViewMessageAdapter(context));
    }
}
