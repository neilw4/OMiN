package neilw4.omin;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import neilw4.omin.connection.ConnectionManager;

public class ConnectionCallback implements ConnectionManager.ConnectionCallback {
    public static final String TAG = ConnectionCallback.class.getSimpleName();

    private Handler handler = new Handler();
    private Context context;

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void onConnectedToServer(BluetoothDevice device, InputStream in, OutputStream out) throws IOException {
        Log.i(TAG, "connected to server " + device.getName());
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        OutputStreamWriter writer = new OutputStreamWriter(out);
        writer.write("Hi from client\n");
        writer.flush();
        String line = reader.readLine();
        toast("server " + device.getName() + " says " + line);
    }

    @Override
    public void onConnectedToClient(BluetoothDevice device, InputStream in, OutputStream out) throws IOException {
        Log.i(TAG, "connected to client " + device.getName());
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        OutputStreamWriter writer = new OutputStreamWriter(out);
        String line = reader.readLine();
        toast("client " + device.getName() + " says " + line);
        writer.write("Hi from server\n");
        writer.flush();
    }

    public void onFailure(String msg) {
        Log.e(TAG, "failure: " + msg);
    }

    private void toast(final String msg) {
        if (context != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
