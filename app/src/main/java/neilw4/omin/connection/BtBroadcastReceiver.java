package neilw4.omin.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static neilw4.omin.Logger.*;

public class BtBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = BtBroadcastReceiver.class.getSimpleName();

    private static final List<String> ENABLE_ACTIONS = Arrays.asList(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_BATTERY_OKAY
    );

    private static final List<String> DISABLE_ACTIONS = Arrays.asList(
            Intent.ACTION_BATTERY_LOW
    );

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        debug(TAG, action);
        BtManager manager = BtManager.getInstance();

        if (ENABLE_ACTIONS.contains(action)) {
            if (manager.isStopped()) {
                BtService.startRepeatingAlarm(context);
            }
            manager.scan(context);
        } else if (DISABLE_ACTIONS.contains(action)) {
            if (!manager.isStopped()) {
                BtService.stopRepeatingAlarm(context);
            }
            manager.stop();
        } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            if (bluetoothState == BluetoothAdapter.STATE_ON) {
                manager.scan(context);
            } else if (bluetoothState == BluetoothAdapter.STATE_OFF) {
                manager.stop();
            }
        } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
            manager.onStartScan();
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            manager.onEndScan();
        } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            Parcelable[] ps = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_DEVICE);
            Parcelable p = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            for (BluetoothDevice device : getExtras(intent, BluetoothDevice.EXTRA_DEVICE, BluetoothDevice.class)) {
                manager.onDeviceFound(device);
            }
        } else if (BluetoothDevice.ACTION_UUID.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            for (ParcelUuid parcel : getExtras(intent, BluetoothDevice.EXTRA_UUID, ParcelUuid.class)) {
                if (parcel != null) {
                    manager.onUuidFound(device, parcel.getUuid());
                }
            }
        } else {
            warn(TAG, "unexpected intent action " + action);
        }
    }


    // There is no way to find out if an intent's extras is an array or a singular item. This is a hacky way round this that tries both.
    public static <T extends Parcelable> Iterable<T> getExtras(Intent i, String extra, Class<T> clazz) {
        Parcelable[] ps = i.getParcelableArrayExtra(extra);
        if (ps != null) {
            List<T> ts = new ArrayList<T>(ps.length);
            for (Parcelable p: ps) {
                ts.add((T)p);
            }
            return ts;
        } else {
            Parcelable p = i.getParcelableExtra(extra);
            if (p != null) {
                return Collections.singleton((T) p);
            } else {
                debug(TAG, "No parcelable extra found for " + extra + " in intent " + i);
                return Collections.EMPTY_LIST;
            }
        }
    }
}
