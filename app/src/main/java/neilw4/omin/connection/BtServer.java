package neilw4.omin.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;

import static neilw4.omin.Logger.*;

public class BtServer {


    private final BtConnection connection;

    private volatile BluetoothServerThread runningThread = null;

    private volatile boolean serverRunning = false;

    public BtServer(BtConnection connection) {
        this.connection = connection;
    }

    public synchronized void start(BluetoothAdapter adapter) {
        String TAG = BtServer.class.getSimpleName() + ".start()";
        serverRunning = true;
        if (runningThread == null || !runningThread.isAlive()) {
            debug(TAG, "");
            try {
                runningThread = new BluetoothServerThread(adapter);
                runningThread.start();
            } catch (IOException e) {
                warn(TAG, "error starting server thread", e);
                runningThread = null;
                serverRunning = false;
            }
        }
    }

    public void cancel() {
        serverRunning = false;
        if (runningThread != null) {
            runningThread.cancel();
        }
    }

    class BluetoothServerThread extends Thread {
        BluetoothServerSocket serverSocket;

        public BluetoothServerThread(BluetoothAdapter adapter) throws IOException {
            serverSocket = adapter.listenUsingInsecureRfcommWithServiceRecord(BtConnection.MY_NAME, BtConnection.MY_UUID);
        }

        @Override
        public void run() {
            String TAG = BluetoothServerThread.class.getSimpleName() + ".run()";
            while (serverRunning) {
                BluetoothSocket socket;
                try {
                    socket = serverSocket.accept();
                    connection.connect(socket);
                } catch (IOException e) {
                    if (serverRunning) {
                        warn(TAG, "error accepting connection", e);
                    }
                }
            }
        }

        public void cancel() {
            String TAG = BluetoothServerThread.class.getSimpleName() + ".cancel()";
            debug(TAG, "");
            try {
                runningThread = null;
                serverRunning = false;
                serverSocket.close();
            } catch (IOException e) {
                warn(TAG, "error stopping server", e);
            }
        }
    }

}
