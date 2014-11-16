package neilw4.omin.background;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Arrays;
import java.util.List;

public class EnableDisableBackgroundReceiver extends BroadcastReceiver {
    public static String TAG = "EnableDisableBackgroundReceiver";

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

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ENABLE_ACTIONS.contains(action)) {
            BluetoothOminService.start(context);
        } else if (DISABLE_ACTIONS.contains(action)) {
            BluetoothOminService.stop(context);
        } else if (BLUETOOTH_STATE_CHANGED_ACTION.equals(action)) {
            switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
                case BluetoothAdapter.STATE_ON: {
                    BluetoothOminService.start(context);
                    break;
                }
                case BluetoothAdapter.STATE_OFF:
                case BluetoothAdapter.STATE_TURNING_OFF: {
                    BluetoothOminService.stop(context);
                }
            }
        }
        else {
            android.util.Log.e(TAG, "Unrecognised intent action: " + action);
        }
    }
}
