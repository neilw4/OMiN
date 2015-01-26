package neilw4.omin.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


import java.util.Arrays;
import java.util.List;

import static neilw4.omin.Logger.*;

public class ConnectionServiceStarter extends BroadcastReceiver {
    public static final String TAG = ConnectionServiceStarter.class.getSimpleName();

    private static final List<String> ENABLE_ACTIONS = Arrays.asList(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_BATTERY_OKAY
    );

    private static final List<String> DISABLE_ACTIONS = Arrays.asList(
            Intent.ACTION_BATTERY_LOW
    );

    private static final List<String> PASS_ACTIONS = Arrays.asList(
            BluetoothAdapter.ACTION_DISCOVERY_STARTED,
            BluetoothAdapter.ACTION_DISCOVERY_FINISHED,
            BluetoothDevice.ACTION_FOUND,
            BluetoothDevice.ACTION_UUID
    );

    private static final String BLUETOOTH_STATE_CHANGED_ACTION = BluetoothAdapter.ACTION_STATE_CHANGED;

    public static void start(Context context) {
        debug(TAG, "Starting bluetooth services");
        ConnectionService.start(context);
    }

    public static void stop(Context context) {
        debug(TAG, "Stopping bluetooth services");
        ConnectionService.stop(context);
    }

    public static void pass(Context context, Intent intent) {
        ConnectionService.pass(context, intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
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
        } else if (PASS_ACTIONS.contains(action)) {
            pass(context, intent);
        } else {
            error(TAG, "Unrecognised intent action: " + action);
        }
    }
}
