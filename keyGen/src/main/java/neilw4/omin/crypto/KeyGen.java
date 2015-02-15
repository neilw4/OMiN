package neilw4.omin.crypto.sign;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06MasterSecretKeyParameters;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06PublicKeyParameters;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06SecretKeyParameters;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06Parameters;
import neilw4.omin.crypto.sign.PS06;
import neilw4.omin.crypto.sign.Params;
import neilw4.omin.crypto.sign.Serialiser;

public class KeyGen {
    public static final File CIPHER_PARAMS_FILE = new File("cipher_params.sign.param");
    public static final File MSK_FILE = new File("msk.sign.param");
    public static final File MPK_FILE = new File("mpk.sign.param");

    private static final PS06 ps06 = new PS06();

    public static void main(String args[]) throws IOException {
        PS06Parameters cipherParams = ps06.createParameters(Params.NU, Params.NM, Params.getCurveParams());
        AsymmetricCipherKeyPair keyPair = ps06.setup(ps06.createParameters(Params.NU, Params.NM, Params.getCurveParams()));

        FileOutputStream cipherParamsStream = new FileOutputStream(CIPHER_PARAMS_FILE, false);
        cipherParamsStream.write(Serialiser.serialiseCipherParams(cipherParams));
        cipherParamsStream.close();

        FileOutputStream mskStream = new FileOutputStream(MSK_FILE, false);
        mskStream.write(Serialiser.serialiseMasterSecret((PS06MasterSecretKeyParameters)keyPair.getPrivate()));
        mskStream.close();

        byte[][] xs = Serialiser.serialiseMasterPublic((PS06PublicKeyParameters)keyPair.getPublic());
        for (byte[] x: xs) {
            System.out.println(x.length);
        }

//        FileOutputStream mpkStream = new FileOutputStream(MPK_FILE, false);
//        mpkStream.write(Serialiser.serialiseMasterPublic((PS06PublicKeyParameters)keyPair.getPublic()));
//        mpkStream.close();
    }
}
