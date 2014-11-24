package neilw4.omin.background;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.Context;
import android.util.Log;


public class BluetoothDiscovery extends IntentService {

    public static String TAG = BluetoothDiscovery.class.getSimpleName();
    public static int REPEAT_SECONDS = 20;

    private static PendingIntent repeatingIntent;

    public static void start(Context context) {
        if (repeatingIntent == null) {
            Intent intent = new Intent(context, BluetoothDiscovery.class);
            repeatingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, 0, REPEAT_SECONDS * 1000, repeatingIntent);
        }
    }

    public static void stop(Context context) {
        if (repeatingIntent != null) {
            AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            alarm.cancel(repeatingIntent);
            Intent intent = new Intent(context, BluetoothDiscovery.class);
            context.stopService(intent);
        }
    }

    public BluetoothDiscovery() {
        super("BluetoothClientService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "trying bluetooth discover");
        final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null || !btAdapter.isEnabled()) {
            Log.e(TAG, "Bluetooth not available");
            stop(this);
            return;
        }

        // Start discovery.
        if (!btAdapter.isDiscovering()) {
            Log.d(TAG, "started bluetooth discovery");
            boolean started = btAdapter.startDiscovery();
            if (!started) {
                Log.e(TAG, "Discovery not started");
            }
        }
    }

}
