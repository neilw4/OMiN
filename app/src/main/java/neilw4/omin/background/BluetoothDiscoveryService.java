package neilw4.omin.background;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.UUID;


public class BluetoothDiscoveryService extends IntentService {

    public static String TAG = BluetoothDiscoveryService.class.getSimpleName();
    public static int REPEAT_SECONDS = 20;

    private static PendingIntent repeatingIntent;

    public static void start(Context context) {
        if (repeatingIntent == null) {
            Intent intent = new Intent(context, BluetoothDiscoveryService.class);
            repeatingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, 0, REPEAT_SECONDS * 1000, repeatingIntent);
        }
    }

    public static void stop(Context context) {
        if (repeatingIntent != null) {
            AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            alarm.cancel(repeatingIntent);
            Intent intent = new Intent(context, BluetoothDiscoveryService.class);
            context.stopService(intent);
        }
    }

    public BluetoothDiscoveryService() {
        super("BluetoothClientService");
    }

    public BroadcastReceiver discoveredReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "received discoveredReceiver found action " + intent.getAction());
            if (BluetoothDevice.ACTION_UUID.equals(intent.getAction())) {
                Log.d(TAG, "received UUID action");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Parcelable[] uuids = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
                for (Parcelable p: uuids) {
                    if (p instanceof ParcelUuid) {
                        UUID uuid = ((ParcelUuid)p).getUuid();
                        if(uuid.equals(BluetoothServiceManager.OMIN_SERVICE_UUID)) {
                            onDeviceDiscovered(device);
                        } else {
                            Log.d(TAG, "encountered another UUID: " + uuid);
                        }
                    } else {
                        Log.e(TAG, "encountered unexpected parcelable type: " + p);
                    }
                }
            }
        }
    };

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Starting bluetooth discover");
        final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null || !btAdapter.isEnabled()) {
            Log.e(TAG, "Bluetooth not available");
            stop(this);
            return;
        }

        // Register bluetooth discovery receiver.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_UUID);
        registerReceiver(discoveredReceiver, filter);

        // Start discovery.
        if (!btAdapter.isDiscovering()) {
            boolean started = btAdapter.startDiscovery();
            if (!started) {
                Log.e(TAG, "Discovery not started");
            }
        }
    }

    private void onDeviceDiscovered(BluetoothDevice device) {
        Log.d(TAG, "discovered bluetooth device at " + device.getName() + ": " + device.getAddress());
        try {
            BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(BluetoothServiceManager.OMIN_SERVICE_UUID);
            InputStreamReader in = new InputStreamReader(socket.getInputStream());
            int i = in.read();
            Log.d(TAG, "client received " + i);
            OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
            out.write(i);
            out.flush();
            socket.close();

        } catch (IOException e) {
            Log.e(TAG, "Exception in bluetooth client: " + e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(discoveredReceiver);
    }
}
