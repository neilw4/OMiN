package neilw4.omin;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.util.JsonReader;
import android.util.JsonWriter;

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

import static junit.framework.Assert.assertEquals;
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
        onConnected(device, in, out);
    }

    @Override
    public void onConnectedToClient(BluetoothDevice device, InputStream in, OutputStream out) throws IOException {
        onConnected(device, in, out);
    }

    private void onConnected(BluetoothDevice device, InputStream in, OutputStream out) throws IOException {
        String myAddress = BluetoothAdapter.getDefaultAdapter().getAddress();
        String myName = BluetoothAdapter.getDefaultAdapter().getName();
        info(TAG, myAddress + " (" + myName + ") connected to " + device.getAddress());
        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        final OutputStreamWriter writer = new OutputStreamWriter(out);
        final JsonWriter jsonWriter = new JsonWriter(writer);
        final JsonReader jsonReader = new JsonReader(reader);
        jsonWriter.setLenient(false);
        jsonReader.setLenient(false);

        jsonWriter.beginObject();
        jsonWriter.name("following");

        MySugarTransactionHelper.doInTransaction(new MySugarTransactionHelper.Callback<Void>() {
            @Override
            public Void manipulateInTransaction() throws IOException {
                BloomFilter<UserId> following = UserId.followingUserIds();
                following.write(jsonWriter);
                return null;
            }
        });
        writer.flush();

        jsonReader.beginObject();
        assertEquals("following", jsonReader.nextName());
        BloomFilter<UserId> partnerFollowing = BloomFilter.read(jsonReader);

        jsonWriter.name("messages");

        Messages.write(jsonWriter, Select.from(Message.class).list());
        writer.flush();

        assertEquals("messages", jsonReader.nextName());
        Messages.read(jsonReader);

        jsonWriter.endObject();
        writer.flush();
        jsonReader.endObject();
        info(TAG, myAddress + " (" + myName + ") disconnected from " + device.getAddress());
    }

    public void onFailure(String msg) {
        error(TAG, "failure: " + msg);
    }

}
