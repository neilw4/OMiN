package neilw4.omin.crypto.sign;

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
    public static final int ELEMENT_SIZE_BYTES = 128;

    public static byte[] serialiseCurveElement(Element e) {
        byte[] bytes = e.toBytes();
        assertEquals(ELEMENT_SIZE_BYTES, bytes.length);
        return bytes;
    }

    public static void serialiseCurveElement(Element e, byte[] buffer, int offset) {
        byte[] bytes = serialiseCurveElement(e);
        System.arraycopy(bytes, 0, buffer, offset, bytes.length);
    }

    // Only works with G1 CurveElements.
    public static Element deserialiseCurveElement(byte[] bytes, int offset, Pairing pairing) {
        Element e = new CurveElement((CurveField)pairing.getG1());
        e.setFromBytes(bytes, offset);
        return e.getImmutable();
    }

    public static Element deserialiseCurveElement(byte[] bytes, Pairing pairing) {
        assertEquals(ELEMENT_SIZE_BYTES, bytes.length);
        return deserialiseCurveElement(bytes, 0, pairing);
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

    public static byte[] serialiseMasterPublic(PS06PublicKeyParameters mpk) {
        int nm = mpk.getParameters().getnM();
        int nu = mpk.getParameters().getnU();

        byte[] bytes = new byte[(4 + nm + nu) * ELEMENT_SIZE_BYTES];
        serialiseCurveElement(mpk.getG1(), bytes, 0);
        serialiseCurveElement(mpk.getG2(), bytes, ELEMENT_SIZE_BYTES);
        serialiseCurveElement(mpk.getmPrime(), bytes, ELEMENT_SIZE_BYTES * 2);
        serialiseCurveElement(mpk.getuPrime(), bytes, ELEMENT_SIZE_BYTES * 3);

        for (int i = 0; i < nm; i++) {
            serialiseCurveElement(mpk.getMAt(i), bytes, ELEMENT_SIZE_BYTES * (4 + i));
        }

        for (int i = 0; i < nu; i++) {
            serialiseCurveElement(mpk.getUAt(i), bytes, ELEMENT_SIZE_BYTES * (4 + nm + i));
        }
        return bytes;
    }

    public static PS06PublicKeyParameters deserialiseMasterPublic(byte[] bytes, PS06Parameters cipherParams, Pairing pairing) {
        int nm = cipherParams.getnM();
        int nu = cipherParams.getnU();
        assertEquals((4 + nm + nu) * ELEMENT_SIZE_BYTES, bytes.length);

        Element g1 = deserialiseCurveElement(bytes, 0, pairing);
        Element g2 = deserialiseCurveElement(bytes, ELEMENT_SIZE_BYTES, pairing);
        Element mPrime = deserialiseCurveElement(bytes, ELEMENT_SIZE_BYTES * 2, pairing);
        Element uPrime = deserialiseCurveElement(bytes, ELEMENT_SIZE_BYTES * 3, pairing);

        Element[] ms = new Element[nm];
        for (int i = 0; i < nm; i++) {
            ms[i] = deserialiseCurveElement(bytes, ELEMENT_SIZE_BYTES * (4 + i), pairing);
        }

        Element[] us = new Element[nu];
        for (int i = 0; i < nu; i++) {
            deserialiseCurveElement(bytes, ELEMENT_SIZE_BYTES * (4 + nm + i), pairing);
        }

        return new PS06PublicKeyParameters(cipherParams, g1, g2, uPrime, mPrime, us, ms);
    }

    public static byte[] serialiseSecret(PS06SecretKeyParameters secret) {
        byte[] bytes = new byte[ELEMENT_SIZE_BYTES * 2];
        serialiseCurveElement(secret.getD1(), bytes, 0);
        serialiseCurveElement(secret.getD2(), bytes, ELEMENT_SIZE_BYTES);
        return bytes;
    }

    public static PS06SecretKeyParameters deserialiseSecret(byte[] bytes, String id, PS06PublicKeyParameters masterPublic, Pairing pairing) {
        Element d1 = deserialiseCurveElement(bytes, 0, pairing);
        Element d2 = deserialiseCurveElement(bytes, ELEMENT_SIZE_BYTES, pairing);
        return new PS06SecretKeyParameters(masterPublic, id, d1, d2);
    }

    private static void assertEquals(int a, int b) {
        if (a != b) {
            throw new AssertionError();
        }
    }
}
