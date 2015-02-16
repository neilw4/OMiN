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
import neilw4.omin.db.PrivateKey;

import static neilw4.omin.Logger.*;

public class Signer {
    private final static String TAG = Signer.class.getSimpleName();

    private static Resources resources;

    private static volatile PS06 ps06 = new PS06();
    private static volatile Params params = new Params(new Params.ParamsReader() {
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

    public static void asyncSign(Message msg) {
        new AsyncSignTask(msg).execute();
    }

    public static void asyncVerify(Message msg) {
        new AsyncVerifyTask(msg).execute();
    }

    private static class AsyncSignTask extends AsyncTask<Void, Void, Void> {

        private final Message msg;
        private final List<MessageUid> msgUids;

        public AsyncSignTask(Message msg) {
            this.msg = msg;
            msgUids = Select.from(MessageUid.class).where(Condition.prop("msg").eq(msg.getId())).list();
        }

        @Override
        protected Void doInBackground(Void... ps) {
            for (MessageUid msgUid: msgUids) {
                if (msgUid.uid.parent == null) {
                    long start = System.currentTimeMillis();
                    String skString = Select.from(PrivateKey.class).where(Condition.prop("uid").eq(msgUid.uid.getId())).first().ps06Key;
                    if (skString == null) {
                        warn(TAG, "No secret key found for user " + msgUid.uid.uname);
                        continue;
                    }
                    byte[] skBytes = Base64.decode(skString, Base64.NO_WRAP);
                    PS06SecretKeyParameters sk = Serialiser.deserialiseSecret(skBytes, msgUid.uid.uname, params.getMasterPublic(), params.getPairing());
                    byte[] sig = ps06.sign(msg.body, sk);
                    msgUid.signature = Base64.encodeToString(sig, Base64.DEFAULT);
                    msgUid.save();
                    long time = System.currentTimeMillis() - start;
                    info(TAG, "signed message " + msg.sent + " for " + msgUid.uid.uname + " in " + time + "ms - " + sig.length + " bytes");
                }
            }
            return null;
        }
    }

    private static class AsyncVerifyTask extends AsyncTask<Void, Void, Boolean> {
        private final Message msg;
        private final List<MessageUid> msgUids;

        public AsyncVerifyTask(Message msg) {
            this.msg = msg;
            msgUids = Select.from(MessageUid.class).where(Condition.prop("msg").eq(msg.getId())).list();
        }

        @Override
        protected Boolean doInBackground(Void... ps) {
            for (MessageUid msgUid: msgUids) {
                if (msgUid.uid.parent == null) {
                    long start = System.currentTimeMillis();
                    byte[] sig = Base64.decode(msgUid.signature, Base64.DEFAULT);
                    if (!ps06.verify(params.getMasterPublic(), msg.body, msgUid.uid.uname, sig)) {
                        warn(TAG, "Message verification failed for message " + msg.sent + " from " + msgUid.uid.uname + " with signature " + msgUid.signature);
                        return false;
                    }
                    long time = System.currentTimeMillis() - start;
                    info(TAG, "verified message " + msg.sent + " from " + msgUid.uid.uname + " in " + time + "ms");
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                msg.delete();
            }
        }
    }
}
