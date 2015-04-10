package neilw4.omin.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static neilw4.omin.Logger.*;

public class BtManager implements BtConnection.StateCallback {

    public static enum State {STOPPED, LISTENING, SCANNING, CONNECTED}

    private static BtManager instance = new BtManager();

    private final BtConnection connection = new BtConnection(this);
    private final BtServer server = new BtServer(connection);

    private BtScan scan = new BtScan();

    State state = State.STOPPED;

    public static BtManager getInstance() {
        return instance;
    }

    private BtManager() {}

    public boolean isStopped() {
        return state == State.STOPPED;
    }

    public void scan(Context context) {
        String TAG = BtManager.class.getSimpleName() + ".scan(" + state + ")";

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && adapter.isEnabled()) {
            server.start(adapter);
            if (adapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                debug(TAG, "making discoverable");
                // Make bluetooth discoverable.
                Intent discoverableIntent = new
                        Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
                discoverableIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(discoverableIntent);
            }

            if (state == State.STOPPED || state == State.LISTENING) {
                debug(TAG, "scanning");
                state = State.LISTENING;
                if (adapter.isDiscovering()) {
                    debug(TAG, "unexpected state - already discovering");
                    state = State.SCANNING;
                } else {
                    info(TAG, "started discovery");
                    onStartScan();
                    adapter.startDiscovery();
                }
            }
        } else {
            debug(TAG, "bluetooth unavailable or disabled");
        }

    }

    void onStartScan() {
        scan = new BtScan();
        state = State.SCANNING;
    }

    void onDeviceFound(BluetoothDevice device) {
        scan.onDeviceFound(device);
    }

    void onEndScan() {
        String TAG = BtManager.class.getSimpleName() + ".onEndScan(" + state + ")";
        debug(TAG, "scan ended");
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && adapter.isEnabled()) {
            if (state == State.SCANNING) {
                debug(TAG, "ending scan");
                scan.onEndScan();
            } else {
                debug(TAG, "unexpected state - not scanning");
            }
            server.start(adapter);
            state = State.LISTENING;
        } else {
            debug(TAG, "bluetooth unavailable or disabled");
            stop();
        }
    }

    void onUuidFound(BluetoothDevice device, UUID uuid) {
        scan.onUuidFound(device, uuid);
    }

    @Override
    public void onStartConnection() {
        String TAG = BtManager.class.getSimpleName() + ".onStartConnection(" + state + ")";
        if (state != State.STOPPED) {
            debug(TAG, "starting connection");
            state = State.CONNECTED;
        }
    }

    @Override
    public void onEndConnection() {
        String TAG = BtManager.class.getSimpleName() + ".onEndConnection(" + state + ")";
        if (state == State.CONNECTED) {
            debug(TAG, "ending connection");
            state = State.LISTENING;
        }
    }

    public void stop() {
        String TAG = BtManager.class.getSimpleName() + ".stop(" + state + ")";
        if (state != State.STOPPED) {
            debug(TAG, "stopping");
            scan = new BtScan();

            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter != null && adapter.isDiscovering()) {
                adapter.cancelDiscovery();
            }

            connection.cancel();
            server.cancel();
            state = State.STOPPED;
        } else {
            debug(TAG, "already stopped");
        }
    }


    private class BtScan {
        private Set<BluetoothDevice> visibleDevices = new HashSet<>();

        public void onDeviceFound(BluetoothDevice device) {
            if (device != null) {
                String TAG = BtScan.class.getSimpleName() + ".onDeviceFound(" + state + ")";
                debug(TAG, "found device " + device.getName());

                visibleDevices.add(device);
            }
        }

        public void onEndScan() {
            String TAG = BtScan.class.getSimpleName() + ".onEndScanFound(" + state + ")";
            debug(TAG, "scan ended");
            for (BluetoothDevice device: visibleDevices) {
                boolean hasKnownOminUuid = false;
                if (device.getUuids() != null) {
                    for (ParcelUuid pUuid : device.getUuids()) {
                        if (BtConnection.MY_UUID.equals(pUuid.getUuid())) {
                            hasKnownOminUuid = true;
                        }
                    }
                }
                if (hasKnownOminUuid) {
                    debug(TAG, "found OMiN device " + device.getName() + ". Connecting.");
                    connection.connnect(device);
                } else {
                    debug(TAG, "fetching uuid for " + device.getName());
                    device.fetchUuidsWithSdp();
                }
            }
        }

        public void onUuidFound(BluetoothDevice device, UUID uuid) {
            String TAG = BtScan.class.getSimpleName() + ".onUuidFound(" + state + ")";
            debug(TAG, device.getName() + ": " + uuid);

            visibleDevices.remove(device);
            if (uuid != null && BtConnection.MY_UUID.equals(uuid)) {
                debug(TAG, "found OMiN device " + device.getName() + ". Connecting.");
                connection.connnect(device);
            }
        }

    }

}