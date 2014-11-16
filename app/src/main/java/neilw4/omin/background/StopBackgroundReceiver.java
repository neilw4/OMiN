package neilw4.omin.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StopBackgroundReceiver extends BroadcastReceiver {
    public StopBackgroundReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        CommunicationService.stop(context);
    }
}
