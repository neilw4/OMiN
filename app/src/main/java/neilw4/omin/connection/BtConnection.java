package neilw4.omin.connection;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import neilw4.omin.P2PConnection;

import static neilw4.omin.Logger.*;

public class BtConnection extends Thread {
    // Unique UUID for this application
    public static final UUID MY_UUID = new UUID(-1182240414495433873l, 7307222179317493476l);
    // Name for the SDP record when creating server socket
    public static final String MY_NAME = "OMiN";

    static final String TAG = BtConnection.class.getSimpleName();

    final ConnectionCallback connection = new P2PConnection();
    final StateCallback callback;

    final Queue<BluetoothDevice> devices = new ConcurrentLinkedQueue<>();
    final Queue<BluetoothSocket> serverConnections = new ConcurrentLinkedQueue<>();

    volatile Thread runningThread = null;

    public BtConnection(StateCallback callback) {
        this.callback = callback;
    }

    @Override
    public void run() {
        if (!devices.isEmpty() || !serverConnections.isEmpty()) {
            callback.onStartConnection();
        }
        while (!devices.isEmpty() || !serverConnections.isEmpty()) {
            BluetoothDevice device;
            BluetoothSocket socket;

            socket = serverConnections.poll();
            if (socket != null) {
                device = socket.getRemoteDevice();
            } else {
                device = devices.poll();
                try {
                    socket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                    socket.connect();
                } catch (IOException e) {
                    debug(TAG, "Couldn't connect to device " + device.getName(), e);
                    continue;
                }
            }

            info(TAG, "Connecting to " + device.getAddress());
            try {
                connection.onConnected(device, socket.getInputStream(), socket.getOutputStream());
                info(TAG, "Finished connecting to " + device.getAddress());
            } catch (IOException e) {
                debug(TAG, "Error while communicating with device " + device.getName(), e);
            }
        }

        runningThread = null;
        callback.onEndConnection();
    }

    public void connnect(BluetoothDevice device) {
        String TAG = BtConnection.class.getSimpleName() + ".connect(Device)";
        debug(TAG, device.getName());
        devices.add(device);
        runThread();
    }

    public void connect(BluetoothSocket socket) {
        String TAG = BtConnection.class.getSimpleName() + ".connect(Socket)";
        debug(TAG, socket.getRemoteDevice().getName());
        this.serverConnections.add(socket);
        runThread();
    }

    private synchronized void runThread() {
        if (runningThread == null || !runningThread.isAlive()) {
            runningThread = new Thread(this);
            runningThread.start();
        }
    }

    public void cancel() {
        devices.clear();
        for (BluetoothSocket socket: serverConnections) {
            try {
                socket.close();
            } catch (IOException e) {}
        }
        serverConnections.clear();
    }

    public interface ConnectionCallback {
        void onConnected(BluetoothDevice device, InputStream in, OutputStream out) throws IOException;
    }

    public interface StateCallback {
        void onStartConnection();
        void onEndConnection();
    }
}
