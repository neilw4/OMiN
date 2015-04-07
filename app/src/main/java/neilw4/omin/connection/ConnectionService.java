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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import neilw4.omin.P2PConnection;
import neilw4.omin.Logger;
import neilw4.omin.crypto.sign.Signer;

public class ConnectionService extends IntentService {

    public static final String TAG = ConnectionService.class.getSimpleName();

    public static final String ACTION_START = "neilw4.omin.BluetoothService.START";
    public static final String ACTION_STOP = "neilw4.omin.BluetoothService.STOP";
    public static final String ACTION_SCAN = "neilw4.omin.BluetoothService.SCAN";

    // Start bluetooth discovery after REPEAT_SECONDS seconds.
    public static final int REPEAT_SECONDS = 450;

    public ConnectionService() {
        super(TAG);
    }

    private static P2PConnection p2PConnection = new P2PConnection();
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
                    debug(TAG, "making discoverable");
                    // Make bluetooth discoverable.
                    Intent discoverableIntent = new
                            Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
                    discoverableIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(discoverableIntent);
                }

                debug(TAG, "starting server");
                connection = new ConnectionManager(p2PConnection);
                p2PConnection.setContext(getBaseContext());
                connection.start();
            }
        }
    }

    protected void stopServer() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            if (adapter.isDiscovering()) {
                debug(TAG, "cancelling discovery");
                adapter.cancelDiscovery();
            }

            if (connection != null) {
                connection.stop();
                connection = null;
            }
            p2PConnection.setContext(null);
        }
    }

    protected void connectToDevice(BluetoothDevice device) {
        if (connection == null || !connection.isListening()) {
            info(TAG, "didn't connect to " + device.getAddress() + ": invalid state " + (connection != null ? connection.getState() : null));
            return;
        }
        debug(TAG, "connecting to " + device.getAddress());

        visibleDevices.clear();
        ominDevices.clear();

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        adapter.cancelDiscovery();

        String address = device.getAddress();
        if (recentDevices.contains(address)) {
            recentDevices.remove(address);
        }
        recentDevices.add(address);

        try {
            connection.connect(device);
        } catch (IOException e) {
            error(TAG, "Couldn't connect to device", e);
        }
    }

    protected void connect() {
        if (connection == null || !connection.isListening()) {
            debug(TAG, "didn't connect: invalid state " + (connection != null ? connection.getState() : null));
            return;
        }
        visibleDevices.clear();
        if (ominDevices.isEmpty()) {
            debug(TAG, "no OMiN devices detected");
            return;
        }
        for (Map.Entry<String, BluetoothDevice> entry: ominDevices.entrySet()) {
            if (!recentDevices.contains(entry.getKey())) {
                debug(TAG, "connecting to new device");
                connectToDevice(entry.getValue());
                return;
            }
        }
        for (String address: recentDevices) {
            if (ominDevices.containsKey(address)) {
                debug(TAG, "connecting to previously seen device");
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
                debug(TAG, "didn't start discovery: already discovering");
            }
            if (connection == null) {
                debug(TAG, "didn't start discovery: CM was null");
            } else if (connection.isConnected()) {
                debug(TAG, "didn't start discovery: already connected");
            }
        }
    }

    protected void onStartDiscovery() {
        visibleDevices.clear();
        ominDevices.clear();
    }

    protected void endDiscovery() {
        if (connection != null && connection.isListening()) {
            debug(TAG, "finished discovery");
            for (BluetoothDevice device: visibleDevices.values()) {
                debug(TAG, "fetching uuid for " + device.getAddress() + " (" + device.getName() + ")");
                device.fetchUuidsWithSdp();
            }
        }
    }

    protected void foundDevice(BluetoothDevice device) {
        if (device != null && !visibleDevices.containsKey(device.getAddress())) {
            debug(TAG, "found device " + device.getName() + " (" + device.getAddress() + ")");
            visibleDevices.put(device.getAddress(), device);
        }
    }

    protected void foundUuid(BluetoothDevice device, UUID uuid) {

        String address = device.getAddress();
        if (visibleDevices.containsKey(address)) {
            visibleDevices.remove(address);
        }

        if (uuid != null && ConnectionManager.uuidMatches(uuid) && !ominDevices.containsKey(address)) {
            debug(TAG, "found OMiN device " + device.getAddress());
            ominDevices.put(address, device);
            if (!recentDevices.contains(address) && connection.isListening()) {
                debug(TAG, device.getName() + " not contacted recently. Connecting");
                connect();
                return;
            }
        }

        if (visibleDevices.isEmpty() && connection.isListening() && !ominDevices.isEmpty()) {
            debug("TAG", "Found all UUIDs. Connecting");
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
            for (BluetoothDevice device : getExtras(intent, BluetoothDevice.EXTRA_DEVICE, BluetoothDevice.class)) {
                foundDevice(device);
            }
        } else if (BluetoothDevice.ACTION_UUID.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            for (ParcelUuid parcel : getExtras(intent, BluetoothDevice.EXTRA_UUID, ParcelUuid.class)) {
                UUID uuid = null;
                if (parcel != null) {
                    uuid = parcel.getUuid();
                }
                foundUuid(device, uuid);
            }
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            endDiscovery();
        } else {
            error(TAG, "Unknown action: " + action);
        }
    }

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
                return Collections.singleton((T)p);
            } else {
                debug(TAG, "No parcelable extra found for " + extra + " in intent " + i);
                return Collections.singleton(null);
            }
        }
    }
}
