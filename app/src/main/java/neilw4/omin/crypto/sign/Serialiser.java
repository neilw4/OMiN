package neilw4.omin.crypto.sign;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;

import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06MasterSecretKeyParameters;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06Parameters;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06PublicKeyParameters;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06SecretKeyParameters;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.plaf.jpbc.field.curve.CurveElement;
import it.unisa.dia.gas.plaf.jpbc.field.curve.CurveField;

import static junit.framework.Assert.assertTrue;

public class Serialiser {

    private static byte[] serialiseElement(Element e) {
        return e.toBytes();
    }

    // Only works with G1 CurveElements.
    private static Element deserialiseElement(byte[] bytes) {
        Element e = new CurveElement((CurveField)PS06.getPairing().getG1());
        e.setFromBytes(bytes);
        return e.getImmutable();
    }

    public static byte[] serialiseCipherParams(PS06Parameters cipherParams) {
        return serialiseElement(cipherParams.getG());
    }

    public static PS06Parameters deserialiseCipherParams(byte[] bytes) {
        Element g = deserialiseElement(bytes);
        return new PS06Parameters(PS06.getCurveParams(), g, PS06.NU, PS06.NM);
    }

    public static byte[] serialiseMasterSecret(PS06MasterSecretKeyParameters msk) {
        return  msk.getMsk().toBytes();
    }

    public static PS06MasterSecretKeyParameters deserialiseMasterSecret(byte[] bytes) {
        Element msk = deserialiseElement(bytes);
        return new PS06MasterSecretKeyParameters(PS06.getCipherParams(), msk);
    }

    public static byte[][] serialiseMasterPublic(PS06PublicKeyParameters mpk) {
        int nm = mpk.getParameters().getnM();
        int nu = mpk.getParameters().getnU();

        byte[][] bytes = new byte[4 + nm + nu][];
        bytes[0] = serialiseElement(mpk.getG1());
        bytes[1] = serialiseElement(mpk.getG2());
        bytes[2] = serialiseElement(mpk.getmPrime());
        bytes[3] = serialiseElement(mpk.getuPrime());

        for (int i = 0; i < nm; i++) {
            bytes[4 + i] = serialiseElement(mpk.getMAt(i));
        }

        for (int i = 0; i < nu; i++) {
            bytes[4 + nm + i] = serialiseElement(mpk.getUAt(i));
        }
        return bytes;
    }

    public static PS06PublicKeyParameters deserialiseMasterPublic(byte[][] bytes) {
        int nm = PS06.getCipherParams().getnM();
        int nu = PS06.getCipherParams().getnU();

        Element g1 = deserialiseElement(bytes[0]);
        Element g2 = deserialiseElement(bytes[1]);
        Element mPrime = deserialiseElement(bytes[2]);
        Element uPrime = deserialiseElement(bytes[3]);

        Element[] ms = new Element[nm];
        for (int i = 0; i < nm; i++) {
            ms[i] = deserialiseElement(bytes[4 + i]);
        }

        Element[] us = new Element[nu];
        for (int i = 0; i < nu; i++) {
            us[i] = deserialiseElement(bytes[4 + nm + i]);
        }

        return new PS06PublicKeyParameters(PS06.getCipherParams(), g1, g2, uPrime, mPrime, us, ms);
    }

    public static byte[][] serialiseSecret(PS06SecretKeyParameters secret) {
        return new byte[][] {
                serialiseElement(secret.getD1()),
                serialiseElement(secret.getD2())
        };
    }

    public static PS06SecretKeyParameters deserialiseSecret(byte[][] bytes, String id) {
        Element d1 = deserialiseElement(bytes[0]);
        Element d2 = deserialiseElement(bytes[1]);
        return new PS06SecretKeyParameters(PS06.getMasterPublic(), id, d1, d2);
    }

    public static void testSerialiseAll() {
        PS06.regenerate();

        byte[] paramBytes = serialiseCipherParams(PS06.getCipherParams());
        byte[] mskBytes = serialiseMasterSecret(PS06.getMasterSecret());
        byte[][] mpkBytes = serialiseMasterPublic(PS06.getMasterPublic());

        String message = "Hello World!!!";
        PS06SecretKeyParameters secretKey = PS06.extract("01001101");
        byte[] signature = PS06.sign(message, secretKey);

        byte[][] secretKey1Bytes = serialiseSecret(secretKey);

        PS06.regenerate();

        PS06.cipherParams = deserialiseCipherParams(paramBytes);
        PS06MasterSecretKeyParameters msk = deserialiseMasterSecret(mskBytes);
        PS06PublicKeyParameters mpk = deserialiseMasterPublic(mpkBytes);
        PS06.keyPair = new AsymmetricCipherKeyPair(mpk, msk);

        PS06SecretKeyParameters secretKey2 = PS06.extract("01001101");
        byte[] signature2 = PS06.sign(message, secretKey2);

        byte[][] secretKey2Bytes = serialiseSecret(secretKey2);

        PS06SecretKeyParameters secretKey3 = deserialiseSecret(secretKey1Bytes, "01001101");
        byte[] signature3 = PS06.sign(message, secretKey3);

        PS06SecretKeyParameters secretKey4 = deserialiseSecret(secretKey2Bytes, "01001101");
        byte[] signature4 = PS06.sign(message, secretKey4);

        assertTrue(PS06.verify(message, "01001101", signature));
        assertTrue(PS06.verify(message, "01001101", signature2));
        assertTrue(PS06.verify(message, "01001101", signature3));
        assertTrue(PS06.verify(message, "01001101", signature4));
    }

}
