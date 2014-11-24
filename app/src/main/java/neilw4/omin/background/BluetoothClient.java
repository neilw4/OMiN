package neilw4.omin.background;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.UUID;

public class BluetoothClient extends BroadcastReceiver {
    public static String TAG = BluetoothClient.class.getSimpleName();

    private class ClientThread extends Thread {
        private final BluetoothDevice device;

        public ClientThread(BluetoothDevice device) {
            this.device = device;
        }

        @Override
        public void run() {
            try {
                BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(BluetoothServiceManager.OMIN_SERVICE_UUID);
                Log.d(TAG, "connected to server");
                InputStreamReader in = new InputStreamReader(socket.getInputStream());
                int i = in.read();
                Log.d(TAG, "client received " + i);
                OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
                out.write(i);
                out.flush();
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Exception in bluetooth client: " + e.getMessage());
            }
        }
    }

        @Override
    public void onReceive(Context context, Intent intent) {
        if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            boolean success = device.fetchUuidsWithSdp();
            if (!success) {
                Log.e(TAG, "Could not fetch service discovery on device " + device.getName());
            }
        } else if (BluetoothDevice.ACTION_UUID.equals(intent.getAction())) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Parcelable[] uuids = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
            for (Parcelable p: uuids) {
                if (p instanceof ParcelUuid) {
                    UUID uuid = ((ParcelUuid)p).getUuid();
                    if(uuid.equals(BluetoothServiceManager.OMIN_SERVICE_UUID)) {
                        Log.d(TAG, "discovered OMiN device: " + device.getName());
//                        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                        new ClientThread(device).start();
                    }
                } else {
                    Log.e(TAG, "encountered unexpected parcelable type: " + p);
                }
            }
        } else {
            Log.d(TAG, "unexpected intent action: " + intent.getAction());
        }
    }
}
