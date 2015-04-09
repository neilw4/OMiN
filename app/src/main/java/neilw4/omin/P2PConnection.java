package neilw4.omin;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.util.JsonReader;
import android.util.JsonWriter;

import com.orm.query.Select;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import neilw4.omin.connection.ConnectionManager;
import neilw4.omin.controller.MessageController;
import neilw4.omin.db.Message;
import neilw4.omin.db.Messages;

import static neilw4.omin.Logger.*;

public class P2PConnection implements ConnectionManager.ConnectionCallback {
    public static final String TAG = P2PConnection.class.getSimpleName();

    @Override
    public void onConnectedToServer(BluetoothDevice device, InputStream in, OutputStream out) throws IOException {
        onConnected(device, in, out);
    }

    @Override
    public void onConnectedToClient(BluetoothDevice device, InputStream in, OutputStream out) throws IOException {
        onConnected(device, in, out);
    }

    private void onConnected(BluetoothDevice device, InputStream in, OutputStream out) throws IOException {
        String myAddress = BluetoothAdapter.getDefaultAdapter().getAddress();
        info(TAG, myAddress + " connected to " + device.getAddress());
        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        final OutputStreamWriter writer = new OutputStreamWriter(out);
        final JsonWriter jsonWriter = new JsonWriter(writer);
        final JsonReader jsonReader = new JsonReader(reader);
        jsonWriter.setLenient(false);
        jsonReader.setLenient(false);

        Messages.write(jsonWriter, Select.from(Message.class).list());
        jsonWriter.flush();
        writer.flush();

        int newMessages = Messages.read(jsonReader);
        if (newMessages > 0) {
            MessageController.onMessagesChanged();
        }

        reader.close();
        writer.close();
        info(TAG, myAddress + " disconnected from " + device.getAddress());
    }

    public void onFailure(String msg) {
        error(TAG, "failure: " + msg);
    }

}
