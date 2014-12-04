package neilw4.omin.background;

import android.content.Context;
import android.os.Message;
import android.util.Log;

import android.os.Handler;
import android.widget.Toast;

public class MessageCallback implements Handler.Callback {
    public static final String TAG = MessageCallback.class.getSimpleName();

    private ConnectionManager connection;

    String mConnectedDeviceName = null;
    boolean connectedToServer;
    private Context context = null;

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case ConnectionManager.MESSAGE_STATE_CHANGE:
                switch (msg.arg1) {
                    case ConnectionManager.STATE_CONNECTED:
                        Log.i(TAG, "connected to " + mConnectedDeviceName);
                        if (connection != null) {
                            connection.write("HELLO WORLD!".getBytes());
                        } else {
                            Log.e(TAG, "Couldn't write message: connection doesn't exist");
                        }
                        break;
                    case ConnectionManager.STATE_CONNECTING:
                        Log.i(TAG, "connecting");
                        break;
                    case ConnectionManager.STATE_LISTEN:
                    case ConnectionManager.STATE_NONE:
                        Log.i(TAG, "not connected");
                        break;
                }
                break;
            case ConnectionManager.MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                Log.i(TAG, "write " + writeMessage);
                break;
            case ConnectionManager.MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                Log.i(TAG, mConnectedDeviceName + ":  " + readMessage);
                if (context != null) {
                    Toast.makeText(context, mConnectedDeviceName + "(" + (connectedToServer ? "client" : "server") + "): " + readMessage, Toast.LENGTH_LONG).show();
                }
                try {
                    int i = Integer.parseInt(readMessage);
                    connection.write(Integer.toString(i + 1).getBytes());
                } catch (NumberFormatException e) {}
                break;
            case ConnectionManager.MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(ConnectionManager.DEVICE_NAME);
                connectedToServer = msg.getData().getBoolean(ConnectionManager.CONNECTED_TO_SERVER);
                Log.i(TAG, "connected to " + mConnectedDeviceName);
                break;
            case ConnectionManager.MESSAGE_FAILURE:
                Log.e(TAG, "failure: " + msg.obj);
                break;
        }
        return true;
    }

    public void setConnection(ConnectionManager connection) {
        this.connection = connection;
    }

    public void setContext(Context context) {
        this.context = context;
    }

}