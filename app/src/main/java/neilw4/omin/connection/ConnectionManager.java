package neilw4.omin.connection;

/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static neilw4.omin.Logger.*;


/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class ConnectionManager {
    // Debugging
    private static final String TAG = ConnectionManager.class.getSimpleName();

    // Name for the SDP record when creating server socket
    private static final String NAME = "OMiN";

    // Unique UUID for this application
    public static final UUID MY_UUID = new UUID(-1182240414495433873l, 7307222179317493476l);

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final ConnectionCallback mCallback;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private int mState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device


    /**
     * Constructor. Prepares a new BluetoothChat session.
     */
    public ConnectionManager(ConnectionCallback callback) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mCallback = callback;
    }

    /**
     * Set the current state of the connection connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (mState != state) {
            verbose(TAG, "setState() " + mState + " -> " + state);
            mState = state;
        }
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the connection service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        verbose(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        setState(STATE_LISTEN);

        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            try {
                mAcceptThread = new AcceptThread();
                mAcceptThread.start();
            } catch (IOException e) {
                error(TAG, "cannot start AcceptThread", e);
            }
        }
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) throws IOException {
        verbose(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING && mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        setState(STATE_CONNECTING);
        mConnectThread.start();
    }

    /**
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(final BluetoothSocket socket, final BluetoothDevice
            device, final boolean connectedToServer) {
        verbose(TAG, "connected");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        setState(STATE_CONNECTED);
        try {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            if(connectedToServer) {
                mCallback.onConnectedToServer(device, in, out);
                socket.close();
            } else {
                mCallback.onConnectedToClient(device, in, out);
            }
            socket.close();
        } catch (IOException e) {
            error(TAG, "IOException during connection", e);
        } finally {
            connectionLost();
        }
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        verbose(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        setState(STATE_NONE);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        mCallback.onFailure("Unable to connect to device");
        // Start the service over to restart listening mode
        connectionLost();
    }

    /**
     * Indicate that the connection was lost.
     */
    private void connectionLost() {
        // Start the service over to restart listening mode
        ConnectionManager.this.start();
    }

    public static boolean uuidMatches(UUID uuid) {
        return MY_UUID.equals(uuid);
    }

    public boolean isListening() {
        return mState == STATE_LISTEN;
    }

    public boolean isConnected() {
        return mState == STATE_CONNECTED || mState == STATE_CONNECTING;
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() throws IOException {
            BluetoothServerSocket tmp = null;
            // Create a new listening server socket
            tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(
                    NAME, MY_UUID);
            mmServerSocket = tmp;
        }

        public void run() {
            verbose(TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                BluetoothSocket socket;
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    verbose(TAG, "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (ConnectionManager.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice(), false);
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    error(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            verbose(TAG, "END mAcceptThread");

        }

        public void cancel() {
            verbose(TAG, "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                verbose(TAG, "close() of server failed", e);
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) throws IOException {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            tmp = device.createInsecureRfcommSocketToServiceRecord(
                    MY_UUID);
            mmSocket = tmp;
        }

        public void run() {
            verbose(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    error(TAG, "unable to close() socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (ConnectionManager.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice, true);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                error(TAG, "close() of connect socket failed", e);
            }
        }
    }

    public interface ConnectionCallback {
        void onConnectedToServer(BluetoothDevice device, InputStream in, OutputStream out) throws IOException;
        void onConnectedToClient(BluetoothDevice device, InputStream in, OutputStream out) throws IOException;
        void onFailure(String msg);
    }
}
