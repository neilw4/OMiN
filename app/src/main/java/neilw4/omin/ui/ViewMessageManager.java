package neilw4.omin.ui;

import android.app.Activity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import neilw4.omin.R;

import static neilw4.omin.Logger.*;

public class ViewMessageManager {
    private final Activity context;

    private ListView messageList;
    private ImageButton refresh;
    private ViewMessageAdapter messageListAdapter;

    public ViewMessageManager(Activity context) {
        this.context = context;
    }

    public void setup() {
        messageList = (ListView)context.findViewById(R.id.message_list);
        refresh = (ImageButton)context.findViewById(R.id.refresh_messages_button);
        messageListAdapter = new ViewMessageAdapter(context);

        messageList.setAdapter(messageListAdapter);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageList.invalidateViews();
            }
        });
    }

}
