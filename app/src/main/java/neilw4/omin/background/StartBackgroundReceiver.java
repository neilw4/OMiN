package neilw4.omin.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartBackgroundReceiver extends BroadcastReceiver {
    public StartBackgroundReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        CommunicationService.start(context);
    }
}
