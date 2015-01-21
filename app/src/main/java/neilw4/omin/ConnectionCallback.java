package neilw4.omin;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;
import android.widget.Toast;

import com.orm.MySugarTransactionHelper;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import neilw4.omin.connection.ConnectionManager;
import neilw4.omin.datastructure.BloomFilter;
import neilw4.omin.db.User;
import neilw4.omin.db.UserId;

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
        onConnected(device, in, out);
    }

    @Override
    public void onConnectedToClient(BluetoothDevice device, InputStream in, OutputStream out) throws IOException {
        Log.i(TAG, "connected to client " + device.getName());
        onConnected(device, in, out);

    }

    private void onConnected(BluetoothDevice device, InputStream in, OutputStream out) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        final OutputStreamWriter writer = new OutputStreamWriter(out);
        final JsonWriter jsonWriter = new JsonWriter(writer);
        final JsonReader jsonReader = new JsonReader(reader);

        MySugarTransactionHelper.doInTransaction(new MySugarTransactionHelper.Callback<Void>() {
            @Override
            public Void manipulateInTransaction() throws IOException {
                BloomFilter<UserId> interested = UserId.interestedUserIds();
                interested.write(jsonWriter);
                return null;
            }
        });
        writer.flush();

        BloomFilter<UserId> partnerInterested = BloomFilter.read(jsonReader);
        // send array of possible message signatures
        // receive array possible message of signatures
        // send messages
        // receive messages
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
