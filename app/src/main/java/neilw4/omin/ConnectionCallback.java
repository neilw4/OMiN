package neilw4.omin;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import neilw4.omin.connection.ConnectionManager;

public class ConnectionCallback implements ConnectionManager.ConnectionCallback {
    public static final String TAG = ConnectionCallback.class.getSimpleName();

    private Handler handler = new Handler();
    private BluetoothDevice device;
    private boolean connectedToServer;
    private ConnectionManager connection;
    private Context context;

    public void setConnection(ConnectionManager connection) {
        this.connection = connection;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void onRecieveMessage(final byte[] readBuf, int bytes) {
        // construct a string from the valid bytes in the buffer
        final String readMessage = new String(readBuf, 0, bytes);
        Log.i(TAG, device.getName() + "(" + (connectedToServer ? "client" : "server") + "):  " + readMessage);
        if (context != null) {
            handler.post(new Runnable() {
                 @Override
                 public void run() {
                     Toast.makeText(context, device.getName() + "(" + (connectedToServer ? "client" : "server") + "): " + readMessage, Toast.LENGTH_LONG).show();
                 }
             });
        }
        connection.disconnect();
    }

    public void onConnected(BluetoothDevice device, boolean connectedToServer) {
        Log.i(TAG, "connected to " + device.getName());
        this.device = device;
        this.connectedToServer = connectedToServer;
        handler.post(new Runnable() {
                         @Override
                         public void run() {
                             String myName = BluetoothAdapter.getDefaultAdapter().getName();
                             connection.write((myName + " says HELLO WORLD!").getBytes());
                         }
                     });
    }
    public void onFailure(String msg) {
        Log.e(TAG, "failure: " + msg);
    }
}
