package neilw4.omin.connection;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.os.Parcelable;
import static neilw4.omin.Logger.*;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.UUID;

import neilw4.omin.ConnectionCallback;
import neilw4.omin.Logger;

public class ConnectionService extends IntentService {

    public static final String TAG = ConnectionService.class.getSimpleName();

    public static final String ACTION_START = "neilw4.omin.BluetoothService.START";
    public static final String ACTION_STOP = "neilw4.omin.BluetoothService.STOP";
    public static final String ACTION_SCAN = "neilw4.omin.BluetoothService.SCAN";

    // Start bluetooth discovery after REPEAT_SECONDS seconds.
    public static final int REPEAT_SECONDS = 30;

    public ConnectionService() {
        super(TAG);
    }

    private static ConnectionCallback connectionCallback = new ConnectionCallback();
    private static PendingIntent repeatingIntent = null;

    public static void start(Context context) {
        if (BluetoothAdapter.getDefaultAdapter() == null) {
            warn(TAG, "Could not start service: bluetooth adapter not available");
            return;
        }

        if (repeatingIntent == null) {
            Intent startIntent = new Intent(context, ConnectionService.class);
            startIntent.setAction(ACTION_START);
            context.startService(startIntent);
            Intent scanIntent = new Intent(context, ConnectionService.class);
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

            Intent stopIntent = new Intent(context, ConnectionService.class);
            stopIntent.setAction(ACTION_STOP);
            context.stopService(stopIntent);
        }
    }

    public static void pass(Context context, Intent broadcast) {
        Intent i = new Intent(context, ConnectionService.class);
        i.setAction(broadcast.getAction());
        Bundle extras = broadcast.getExtras();
        if (extras != null) {
            i.putExtras(extras);
        }
        context.startService(i);
    }


    public static ConnectionManager connection = null;

    private static LinkedHashSet<String> recentDevices = new LinkedHashSet<>();
    private static Map<String, BluetoothDevice> visibleDevices = new HashMap<>();
    private static Map<String, BluetoothDevice> ominDevices = new HashMap<>();

    protected synchronized void startServer() {
        Logger.setupLog(this);
        if (connection == null) {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter != null) {
                if (adapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    info(TAG, "making discoverable");
                    // Make bluetooth discoverable.
                    Intent discoverableIntent = new
                            Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
                    discoverableIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(discoverableIntent);
                }

                info(TAG, "starting server");
                connection = new ConnectionManager(connectionCallback);
                connectionCallback.setContext(getBaseContext());
                connection.start();
            }
        }
    }

    protected void stopServer() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            if (adapter.isDiscovering()) {
                info(TAG, "cancelling discovery");
                adapter.cancelDiscovery();
            }

            if (connection != null) {
                connection.stop();
                connection = null;
            }
            connectionCallback.setContext(null);
        }
    }

    protected void connectToDevice(BluetoothDevice device) {
        if (connection == null || !connection.isListening()) {
            info(TAG, "didn't connect to " + device.getAddress() + ": invalid state " + (connection != null ? connection.getState() : null));
            return;
        }
        info(TAG, "connecting to " + device.getAddress());

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
            info(TAG, "didn't connect: invalid state " + (connection != null ? connection.getState() : null));
            return;
        }
        visibleDevices.clear();
        if (ominDevices.isEmpty()) {
            info(TAG, "no OMiN devices detected");
            return;
        }
        for (String address: ominDevices.keySet()) {
            if (!recentDevices.contains(address)) {
                info(TAG, "connecting to new device");
                connectToDevice(ominDevices.get(address));
                return;
            }
        }
        for (String address: recentDevices) {
            if (ominDevices.containsKey(address)) {
                info(TAG, "connecting to previously seen device");
                connectToDevice(ominDevices.get(address));
                return;
            }
        }
        error(TAG, "no valid connection to be made out of ");
        for (BluetoothDevice d: ominDevices.values()) {
            error(TAG, d.getAddress());
        }
    }

    protected void startDiscovery() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (!adapter.isDiscovering() && connection != null && connection.isListening()) {
            info(TAG, "started discovery");
            adapter.startDiscovery();
        } else {
            if (!adapter.isDiscovering()) {
                info(TAG, "didn't start discovery: already discovering");
            }
            if (connection == null) {
                info(TAG, "didn't start discovery: CM was null");
            } else if (connection.isConnected()) {
                info(TAG, "didn't start discovery: already connected");
            }
        }
    }

    protected void onStartDiscovery() {
        visibleDevices.clear();
        ominDevices.clear();
    }

    protected void endDiscovery() {
        if (connection != null && connection.isListening()) {
            info(TAG, "finished discovery");
            for (BluetoothDevice device: visibleDevices.values()) {
                device.fetchUuidsWithSdp();
            }
        }
    }

    protected void foundDevice(BluetoothDevice device) {
        if (!visibleDevices.containsKey(device.getAddress())) {
            info(TAG, "found device " + device.getName() + " (" + device.getAddress() + ")");
            visibleDevices.put(device.getAddress(), device);
        }
    }

    protected void foundUuid(BluetoothDevice device, UUID uuid) {
        if (uuid == null) {
            error(TAG, "UUID for " + device.getName() + " (" + device.getAddress() + ") was null");
            return;
        }
        String address = device.getAddress();
        if (visibleDevices.containsKey(address)) {
            visibleDevices.remove(address);
        }

        if (ConnectionManager.uuidMatches(uuid) && !ominDevices.containsKey(address)) {
            info(TAG, device.getName() + " is an OMiN device");
            ominDevices.put(address, device);
            if (!recentDevices.contains(address) && connection.isListening()) {
                info(TAG, device.getName() + " not contacted recently. Connecting");
                connect();
                return;
            }
        }

        if (visibleDevices.isEmpty() && connection.isListening() && !ominDevices.isEmpty()) {
            info("TAG", "Found all UUIDs. Connecting");
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
            for (Parcelable device : intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_DEVICE)) {
                foundDevice((BluetoothDevice) device);
            }
        } else if (BluetoothDevice.ACTION_UUID.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            for (Parcelable parcel : intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID)) {
                foundUuid(device, ((ParcelUuid) parcel).getUuid());
            }
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            endDiscovery();
        } else {
            error(TAG, "Unknown action: " + action);
        }
    }
}
