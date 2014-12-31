package neilw4.omin.background;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.util.Log;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.UUID;

public class OminService extends IntentService {

    public static final String TAG = OminService.class.getSimpleName();

    public static final String ACTION_START = "neilw4.omin.BluetoothService.START";
    public static final String ACTION_STOP = "neilw4.omin.BluetoothService.STOP";
    public static final String ACTION_SCAN = "neilw4.omin.BluetoothService.SCAN";

    // Start bluetooth discovery after REPEAT_SECONDS seconds.
    public static final int REPEAT_SECONDS = 300;

    public OminService() {
        super(TAG);
    }

    private static MessageCallback messageCallback = new MessageCallback();
    private static Handler messageHandler = new Handler(messageCallback);
    private static PendingIntent repeatingIntent = null;

    public static void start(Context context) {
        if (repeatingIntent == null) {
            Intent startIntent = new Intent(context, OminService.class);
            startIntent.setAction(ACTION_START);
            context.startService(startIntent);
            Intent scanIntent = new Intent(context, OminService.class);
            scanIntent.setAction(ACTION_SCAN);
            repeatingIntent = PendingIntent.getService(context, 0, scanIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, 0, REPEAT_SECONDS * 1000, repeatingIntent);
        }
    }

    public static void stop(Context context) {
        if (repeatingIntent != null) {
            AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            alarm.cancel(repeatingIntent);
            repeatingIntent = null;

            Intent stopIntent = new Intent(context, OminService.class);
            stopIntent.setAction(ACTION_STOP);
            context.stopService(stopIntent);
        }
    }

    public static void pass(Context context, Intent broadcast) {
        Intent i = new Intent(context, OminService.class);
        i.setAction(broadcast.getAction());
        Bundle extras = broadcast.getExtras();
        if (extras != null) {
            i.putExtras(extras);
        }
        context.startService(i);
    }


    public static ConnectionManager connection = null;

    private static LinkedHashSet<String> recentDevices = new LinkedHashSet<String>();
    private static Map<String, BluetoothDevice> visibleDevices = new HashMap<String, BluetoothDevice>();
    private static Map<String, BluetoothDevice> ominDevices = new HashMap<String, BluetoothDevice>();

    protected synchronized void startServer() {
        if (connection == null) {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                Log.i(TAG, "making discoverable");
                // Make bluetooth discoverable.
                Intent discoverableIntent = new
                        Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
                discoverableIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(discoverableIntent);
            }

            Log.i(TAG, "starting server");
            connection = new ConnectionManager(this, messageHandler);
            messageCallback.setConnection(connection);
            messageCallback.setContext(getBaseContext());
            connection.start();
        }
    }

    protected void stopServer() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter.isDiscovering()) {
            Log.i(TAG, "cancelling discovery");
            adapter.cancelDiscovery();
        }

        if (connection != null) {
            connection.stop();
            connection = null;
        }
        messageCallback.setConnection(null);
        messageCallback.setContext(null);
    }

    protected void connectToDevice(BluetoothDevice device) {
        if (connection == null || !connection.isListening()) {
            Log.i(TAG, "didn't connect to " + device.getAddress() + ": invalid state " + (connection != null ? connection.getState() : null));
            return;
        }
        Log.i(TAG, "connecting to " + device.getAddress());

        visibleDevices.clear();
        ominDevices.clear();

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        adapter.cancelDiscovery();

        String address = device.getAddress();
        if (recentDevices.contains(address)) {
            recentDevices.remove(address);
        }
        recentDevices.add(address);

        connection.connect(device);
    }

    protected void connect() {
        if (connection == null || !connection.isListening()) {
            Log.i(TAG, "didn't connect: invalid state " + (connection != null ? connection.getState() : null));
            return;
        }
        visibleDevices.clear();
        if (ominDevices.isEmpty()) {
            Log.i(TAG, "no OMiN devices detected");
            return;
        }
        for (String address: ominDevices.keySet()) {
            if (!recentDevices.contains(address)) {
                Log.i(TAG, "connecting to new device");
                connectToDevice(ominDevices.get(address));
                return;
            }
        }
        for (String address: recentDevices) {
            if (ominDevices.containsKey(address)) {
                Log.i(TAG, "connecting to previously seen device");
                connectToDevice(ominDevices.get(address));
                return;
            }
        }
        Log.e(TAG, "no valid connection to be made out of ");
        for (BluetoothDevice d: ominDevices.values()) {
            Log.e(TAG, d.getAddress());
        }
    }

    protected void startDiscovery() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (!adapter.isDiscovering() && connection != null && connection.isListening()) {
            Log.i(TAG, "started discovery");
            adapter.startDiscovery();
        } else {
            if (!adapter.isDiscovering()) {
                Log.i(TAG, "didn't start discovery: already discovering");
            }
            if (connection == null) {
                Log.i(TAG, "didn't start discovery: CM was null");
            } else if (connection.isConnected()) {
                Log.i(TAG, "didn't start discovery: already connected");
            }
        }
    }

    protected void onStartDiscovery() {
        visibleDevices.clear();
        ominDevices.clear();
    }

    protected void endDiscovery() {
        if (connection != null && connection.isListening()) {
            Log.i(TAG, "finished discovery");
            for (BluetoothDevice device: visibleDevices.values()) {
                device.fetchUuidsWithSdp();
            }
        }
    }

    protected void foundDevice(BluetoothDevice device) {
        if (!visibleDevices.containsKey(device.getAddress())) {
            Log.i(TAG, "found device " + device.getName() + " (" + device.getAddress() + ")");
            visibleDevices.put(device.getAddress(), device);
        }
    }

    protected void foundUuid(BluetoothDevice device, UUID uuid) {
        if (uuid == null) {
            Log.e(TAG, "UUID for " + device.getName() + " (" + device.getAddress() + ") was null");
            return;
        }
        String address = device.getAddress();
        if (visibleDevices.containsKey(address)) {
            visibleDevices.remove(address);
        }

        if (ConnectionManager.uuidMatches(uuid) && !ominDevices.containsKey(address)) {
            Log.i(TAG, device.getName() + " is an OMiN device");
            ominDevices.put(address, device);
            if (!recentDevices.contains(address) && connection.isListening()) {
                Log.i(TAG, device.getName() + " not contacted recently. Connecting");
                connect();
                return;
            }
        }

        if (visibleDevices.isEmpty() && connection.isListening() && !ominDevices.isEmpty()) {
            Log.i("TAG", "Found all UUIDs. Connecting");
            connect();
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        startServer();
        if (ACTION_START.equals(action)) {
            // Server already started - do nothing.
        } else if (ACTION_STOP.equals(action)) {
            stopServer();
        } else if (ACTION_SCAN.equals(action)) {
            startDiscovery();
        } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
            onStartDiscovery();
        } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice singleDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (singleDevice != null) {
                foundDevice(singleDevice);
            } else {
                Parcelable[] devices = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_DEVICE);
                if (devices != null) {
                    for (Parcelable device : devices) {
                        foundDevice((BluetoothDevice) device);
                    }
                }
            }
        } else if (BluetoothDevice.ACTION_UUID.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            ParcelUuid singleParcel = intent.getParcelableExtra(BluetoothDevice.EXTRA_UUID);
            if (singleParcel != null) {
                foundUuid(device, singleParcel.getUuid());
            } else {
                Parcelable[] uuidParcels = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
                if (uuidParcels != null) {
                    for (Parcelable parcel : uuidParcels) {
                        foundUuid(device, ((ParcelUuid) parcel).getUuid());
                    }
                }
            }
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            endDiscovery();
        } else {
            Log.e(TAG, "Unknown action: " + action);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
