package neilw4.omin.crypto.sign;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Base64;

import com.google.common.io.ByteStreams;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06SecretKeyParameters;
import neilw4.omin.R;
import neilw4.omin.db.Message;
import neilw4.omin.db.MessageUid;
import neilw4.omin.db.SecretKey;

import static neilw4.omin.Logger.*;

public class Signer {
    private final static String TAG = Signer.class.getSimpleName();

    private static Resources resources;

    private static volatile PS06 ps06 = new PS06();
    private static volatile Params params = new ReadParams(new ReadParams.ParamsReader() {
        private byte[] read(int id) {
            try {
                InputStream stream = resources.openRawResource(id);
                return ByteStreams.toByteArray(stream);
            } catch (IOException e) {
                error(TAG, "Couldn't read raw resource", e);
                return null;
            }
        }

        @Override
        public byte[] readCipherParams() {
            return read(R.raw.cipher_params);
        }

        @Override
        public byte[] readMPK() {
            return read(R.raw.mpk);
        }

        @Override
        public byte[] readMSK() {
            throw new UnsupportedOperationException("Client has no access to master secret");
        }
    });

    // Slightly hacky but it's nearly impossible to pass the context around everywhere.
    public static void setResources(Resources resources) {
        if (Signer.resources == null) {
            Signer.resources = resources;
        }
    }

    public static void asyncSign(Message msg, Callback callback) {
        new AsyncSignTask(msg, callback).execute();
    }

    public static void asyncSign(Message msg) {
        asyncSign(msg, null);
    }

    public static void asyncVerify(Message msg, Callback callback) {
        new AsyncVerifyTask(msg, callback).execute();
    }

    public static void asyncVerify(Message msg) {
        asyncVerify(msg, null);
    }

    public static interface Callback {
        void onSuccess();
        void onFail();
    }


    private static class AsyncSignTask extends AsyncTask<Void, Void, Message.Security> {

        private final Message msg;
        private final List<MessageUid> msgUids;
        private final Callback callback;

        public AsyncSignTask(Message msg, Callback callback) {
            this.callback = callback;
            this.msg = msg;
            msgUids = Select.from(MessageUid.class).where(Condition.prop("msg").eq(msg.getId())).list();
        }

        @Override
        protected Message.Security doInBackground(Void... ps) {
            Message.Security secure = Message.Security.INSECURE;
            for (MessageUid msgUid: msgUids) {
                if (msgUid.uid.parent == null) {
                    SecretKey sk = Select.from(SecretKey.class).where(Condition.prop("uid").eq(msgUid.uid.getId())).first();
                    if (sk.ps06Key != null) {
                        long start = System.nanoTime();
                        byte[] skBytes = Base64.decode(sk.ps06Key, Base64.NO_WRAP);
                        PS06SecretKeyParameters skParams = Serialiser.deserialiseSecret(skBytes, msgUid.uid.uname, params.getMasterPublic(), params.getPairing());
                        byte[] sig = ps06.sign(msg.body, skParams);
                        msgUid.signature = Base64.encodeToString(sig, Base64.DEFAULT);
                        long end = System.nanoTime();
                        msgUid.save();
                        secure = Message.Security.SECURE;
                        info(TAG, "signed message " + msg.sent + " for " + msgUid.uid.uname + " in " + ((end - start) / 1000000) + "ms - " + sig.length + " bytes");
                    } else {
                        warn(TAG, "No secret key found for user " + msgUid.uid.uname);
                    }
                }
            }
            return secure;
        }

        @Override
        protected void onPostExecute(Message.Security security) {
            msg.security = security;
            msg.save();
            if (security == Message.Security.SECURE) {
                callback.onSuccess();
            } else {
                callback.onFail();
            }
        }
    }

    private static class AsyncVerifyTask extends AsyncTask<Void, Void, Message.Security> {
        private final Message msg;
        private final List<MessageUid> msgUids;
        private final Callback callback;

        public AsyncVerifyTask(Message msg, Callback callback) {
            this.callback = callback;
            this.msg = msg;
            msgUids = Select.from(MessageUid.class).where(Condition.prop("msg").eq(msg.getId())).list();
        }

        @Override
        protected Message.Security doInBackground(Void... ps) {
            Message.Security security = Message.Security.INSECURE;
            for (MessageUid msgUid: msgUids) {
                if (msgUid.uid.parent == null && msgUid.signature != null) {
                    long start = System.nanoTime();
                    byte[] sig = Base64.decode(msgUid.signature, Base64.DEFAULT);
                    if (!ps06.verify(params.getMasterPublic(), msg.body, msgUid.uid.uname, sig)) {
                        warn(TAG, "Message verification failed for message " + msg.sent + " from " + msgUid.uid.uname + " with signature " + msgUid.signature);
                        return Message.Security.UNVERIFIED;
                    }
                    long end = System.nanoTime();
                    info(TAG, "verified message " + msg.sent + " from " + msgUid.uid.uname + " in " + ((end - start) / 1000000) + "ms");
                    security = Message.Security.SECURE;
                }
            }
            return security;
        }

        @Override
        protected void onPostExecute(Message.Security security) {
            if (security == Message.Security.UNVERIFIED) {
                msg.delete();
                callback.onFail();
            } else {
                msg.security = security;
                msg.save();
                callback.onSuccess();
            }
        }
    }
}
