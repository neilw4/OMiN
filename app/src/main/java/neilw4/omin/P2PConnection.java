package neilw4.omin;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.util.JsonReader;
import android.util.JsonWriter;

import android.widget.Toast;

import com.orm.MySugarTransactionHelper;
import com.orm.query.Select;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import neilw4.omin.connection.ConnectionManager;
import neilw4.omin.datastructure.BloomFilter;
import neilw4.omin.db.Message;
import neilw4.omin.db.Messages;
import neilw4.omin.db.UserId;

import static neilw4.omin.Logger.*;

public class P2PConnection implements ConnectionManager.ConnectionCallback {
    public static final String TAG = P2PConnection.class.getSimpleName();

    private Handler handler = new Handler();
    private Context context;

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void onConnectedToServer(BluetoothDevice device, InputStream in, OutputStream out) throws IOException {
        info(TAG, "connected to server " + device.getName());
        onConnected(device, in, out);
    }

    @Override
    public void onConnectedToClient(BluetoothDevice device, InputStream in, OutputStream out) throws IOException {
        info(TAG, "connected to client " + device.getName());
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

        Messages.write(jsonWriter, Select.from(Message.class).list());
        Messages.read(jsonReader);
    }

    public void onFailure(String msg) {
        error(TAG, "failure: " + msg);
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
