package neilw4.omin.crypto.sign;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;

import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06MasterSecretKeyParameters;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06Parameters;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06PublicKeyParameters;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06SecretKeyParameters;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.field.curve.CurveElement;
import it.unisa.dia.gas.plaf.jpbc.field.curve.CurveField;
import it.unisa.dia.gas.plaf.jpbc.pairing.parameters.PropertiesParameters;

public class Serialiser {

    public static byte[] serialiseCurveElement(Element e) {
        return e.toBytes();
    }

    // Only works with G1 CurveElements.
    public static Element deserialiseCurveElement(byte[] bytes, Pairing pairing) {
        Element e = new CurveElement((CurveField)pairing.getG1());
        e.setFromBytes(bytes);
        return e.getImmutable();
    }

    public static byte[] serialiseCipherParams(PS06Parameters cipherParams) {
        return serialiseCurveElement(cipherParams.getG());
    }

    public static PS06Parameters deserialiseCipherParams(byte[] bytes, PropertiesParameters curveParams, Pairing pairing, int NU, int NM) {
        Element g = deserialiseCurveElement(bytes, pairing);
        return new PS06Parameters(curveParams, g, NU, NM);
    }

    public static byte[] serialiseMasterSecret(PS06MasterSecretKeyParameters msk) {
        return  msk.getMsk().toBytes();
    }

    public static PS06MasterSecretKeyParameters deserialiseMasterSecret(byte[] bytes, PS06Parameters cipherParams, Pairing pairing) {
        Element msk = deserialiseCurveElement(bytes, pairing);
        return new PS06MasterSecretKeyParameters(cipherParams, msk);
    }

    public static byte[][] serialiseMasterPublic(PS06PublicKeyParameters mpk) {
        int nm = mpk.getParameters().getnM();
        int nu = mpk.getParameters().getnU();

        byte[][] bytes = new byte[4 + nm + nu][];
        bytes[0] = serialiseCurveElement(mpk.getG1());
        bytes[1] = serialiseCurveElement(mpk.getG2());
        bytes[2] = serialiseCurveElement(mpk.getmPrime());
        bytes[3] = serialiseCurveElement(mpk.getuPrime());

        for (int i = 0; i < nm; i++) {
            bytes[4 + i] = serialiseCurveElement(mpk.getMAt(i));
        }

        for (int i = 0; i < nu; i++) {
            bytes[4 + nm + i] = serialiseCurveElement(mpk.getUAt(i));
        }
        return bytes;
    }

    public static PS06PublicKeyParameters deserialiseMasterPublic(byte[][] bytes, PS06Parameters cipherParams, Pairing pairing) {
        int nm = cipherParams.getnM();
        int nu = cipherParams.getnU();

        Element g1 = deserialiseCurveElement(bytes[0], pairing);
        Element g2 = deserialiseCurveElement(bytes[1], pairing);
        Element mPrime = deserialiseCurveElement(bytes[2], pairing);
        Element uPrime = deserialiseCurveElement(bytes[3], pairing);

        Element[] ms = new Element[nm];
        for (int i = 0; i < nm; i++) {
            ms[i] = deserialiseCurveElement(bytes[4 + i], pairing);
        }

        Element[] us = new Element[nu];
        for (int i = 0; i < nu; i++) {
            us[i] = deserialiseCurveElement(bytes[4 + nm + i], pairing);
        }

        return new PS06PublicKeyParameters(cipherParams, g1, g2, uPrime, mPrime, us, ms);
    }

    public static byte[][] serialiseSecret(PS06SecretKeyParameters secret) {
        return new byte[][] {
                serialiseCurveElement(secret.getD1()),
                serialiseCurveElement(secret.getD2())
        };
    }

    public static PS06SecretKeyParameters deserialiseSecret(byte[][] bytes, String id, PS06PublicKeyParameters masterPublic, Pairing pairing) {
        Element d1 = deserialiseCurveElement(bytes[0], pairing);
        Element d2 = deserialiseCurveElement(bytes[1], pairing);
        return new PS06SecretKeyParameters(masterPublic, id, d1, d2);
    }


    public static void test() {
        String id = "01001101";

        Params gen = new GeneratedParams();
        PS06 ps06 = new PS06();

        byte[] paramBytes = serialiseCipherParams(gen.getCipherParams());
        byte[] mskBytes = serialiseMasterSecret(gen.getMasterSecret());
        byte[][] mpkBytes = serialiseMasterPublic(gen.getMasterPublic());

        String message = "Hello World!!!";
        PS06SecretKeyParameters secretKey = (PS06SecretKeyParameters)ps06.extract(gen.getKeyPair(), id);
        byte[] signature = ps06.sign(message, secretKey);

        byte[][] secretKey1Bytes = serialiseSecret(secretKey);

        PS06Parameters cipherParams = deserialiseCipherParams(paramBytes, gen.getCurveParams(), gen.getPairing(), Params.NU, Params.NM);
        PS06MasterSecretKeyParameters msk = deserialiseMasterSecret(mskBytes, cipherParams, gen.getPairing());
        PS06PublicKeyParameters mpk = deserialiseMasterPublic(mpkBytes, cipherParams, gen.getPairing());
        AsymmetricCipherKeyPair keyPair = new AsymmetricCipherKeyPair(mpk, msk);

        PS06SecretKeyParameters secretKey2 = (PS06SecretKeyParameters)ps06.extract(keyPair, id);
        byte[] signature2 = ps06.sign(message, secretKey2);

        byte[][] secretKey2Bytes = serialiseSecret(secretKey2);

        PS06SecretKeyParameters secretKey3 = deserialiseSecret(secretKey1Bytes, id, mpk, gen.getPairing());
        byte[] signature3 = ps06.sign(message, secretKey3);

        PS06SecretKeyParameters secretKey4 = deserialiseSecret(secretKey2Bytes, id, mpk, gen.getPairing());
        byte[] signature4 = ps06.sign(message, secretKey4);

        assertTrue(ps06.verify(mpk, message, id, signature));
        assertTrue(ps06.verify(mpk, message, id, signature2));
        assertTrue(ps06.verify(gen.getMasterPublic(), message, id, signature3));
        assertTrue(ps06.verify(gen.getMasterPublic(), message, id, signature4));
    }

    static void assertTrue(boolean test) {
        if (!test) {
            throw new AssertionError();
        }
    }

}
