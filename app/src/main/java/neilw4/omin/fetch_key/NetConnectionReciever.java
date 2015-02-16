package neilw4.omin.fetch_key;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

public class NetConnectionReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            if (!intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
                FetchKey.asyncFetch();
            }
        }
    }
}
