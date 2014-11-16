package neilw4.omin.background;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BluetoothServiceManager extends BroadcastReceiver {
    public static String TAG = BluetoothServiceManager.class.getSimpleName();
    public static String OMIN_SERVICE_NAME = "OMiN Service";
    public static UUID OMIN_SERVICE_UUID = new UUID(-9182340414495433873l, 3307222079317493476l);

    private static List<String> ENABLE_ACTIONS = Arrays.asList(
            "android.intent.action.BOOT_COMPLETED",
            "android.intent.action.MY_PACKAGE_REPLACED",
            "android.intent.action.ACTION_POWER_CONNECTED",
            "android.intent.action.BATTERY_OKAY"
    );

    private static List<String> DISABLE_ACTIONS = Arrays.asList(
            "android.intent.action.BATTERY_LOW",
            "android.intent.action.AIRPLANE_MODE"
    );

    private static String BLUETOOTH_STATE_CHANGED_ACTION = "android.bluetooth.adapter.action.STATE_CHANGED";

    public static void start(Context context) {
        Log.d(TAG, "Starting bluetooth services");
        BluetoothDiscoveryService.start(context);
        BluetoothServerService.start(context);
    }

    public static void stop(Context context) {
        Log.d(TAG, "Stopping bluetooth services");
        BluetoothDiscoveryService.stop(context);
        BluetoothServerService.stop(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "Received action " + action);
        if (ENABLE_ACTIONS.contains(action)) {
            start(context);
        } else if (DISABLE_ACTIONS.contains(action)) {
            stop(context);
        } else if (BLUETOOTH_STATE_CHANGED_ACTION.equals(action)) {
            switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
                case BluetoothAdapter.STATE_ON: {
                    start(context);
                    break;
                }
                case BluetoothAdapter.STATE_OFF:
                case BluetoothAdapter.STATE_TURNING_OFF: {
                    stop(context);
                    break;
                }
            }
        }
        else {
            android.util.Log.e(TAG, "Unrecognised intent action: " + action);
        }
    }
}
