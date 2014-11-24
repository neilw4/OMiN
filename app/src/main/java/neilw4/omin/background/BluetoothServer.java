package neilw4.omin.background;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Random;

public class BluetoothServer extends Service {

    public static String TAG = BluetoothServer.class.getSimpleName();

    public static void start(Context context) {
        context.startService(new Intent(context, BluetoothServer.class));
    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, BluetoothServer.class));
    }

    Thread serverThread;

    private class ServerThread extends Thread {
        public BluetoothServerSocket serverSocket;

        public ServerThread(BluetoothAdapter btAdapter) throws IOException {
                this.serverSocket = btAdapter.listenUsingInsecureRfcommWithServiceRecord(BluetoothServiceManager.OMIN_SERVICE_NAME, BluetoothServiceManager.OMIN_SERVICE_UUID);
        }

        @Override
        public void run() {
            Log.d(TAG, "Starting server " + hashCode());
            while (!isInterrupted()) {
                try {
                    Log.d(TAG, "server " + hashCode() + " waiting for connection");
                    BluetoothSocket socket = serverSocket.accept();
                    Log.d(TAG, "accepted connection from client");
                    int i = new Random().nextInt();
                    OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
                    out.write(i);
                    out.flush();
                    Log.d(TAG, "server sent " + i);
                    InputStreamReader in = new InputStreamReader(socket.getInputStream());
                    int j = in.read();
                    Log.d(TAG, "server received " + j);
                    assert i == j;
                }
                catch (IOException e) {} // socket was closed - ignore it.
            }
        }

        @Override
        public void interrupt() {
            Log.d(TAG, "Stopped bluetooth server thread");
            super.interrupt();
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Exception closing bluetooth server: " + e.getMessage());
            }
        }
    }

    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
        if (serverThread == null) {
            final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
            if (btAdapter == null || !btAdapter.isEnabled()) {
                Log.e(TAG, "Bluetooth not available");
                return START_NOT_STICKY;
            }
            if (btAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                // Make bluetooth discoverable.
                Intent discoverableIntent = new
                        Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
                discoverableIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(discoverableIntent);
            }

            try {
                serverThread = new ServerThread(btAdapter);
                serverThread.start();
            } catch (IOException e) {
                Log.e(TAG, "Exception while setting up server socket: " + e);
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (serverThread != null) {
            serverThread.interrupt();
            serverThread = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
