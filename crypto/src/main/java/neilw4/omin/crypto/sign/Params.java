package neilw4.omin.crypto.sign;

import java.io.IOException;

import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06MasterSecretKeyParameters;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06Parameters;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06PublicKeyParameters;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.parameters.PropertiesParameters;

public class Params {

    public interface ParamsFileReader {
        byte[] readFile(String fname) throws IOException;
    }

    public final static int NU = 256;
    public final static int NM = 256;

    private static volatile PropertiesParameters curveParams = null;
    private volatile PS06Parameters cipherParams = null;
    private volatile PS06PublicKeyParameters masterPublic = null;
    private volatile PS06MasterSecretKeyParameters masterSecret = null;
    private volatile Pairing pairing = null;

    private static final Object curveParamSync = new Object();
    private final Object cipherParamSync = new Object();
    private final Object masterPublicSync = new Object();
    private final Object masterSecretSync = new Object();
    private final Object pairingSync = new Object();

    private final ParamsFileReader reader;

    public Params(ParamsFileReader reader) {
        this.reader = reader;
    }

    public static PropertiesParameters getCurveParams() {
        if (curveParams == null) {
            synchronized (curveParamSync) {
                if (curveParams == null) {
                    curveParams = generateCurveParams();
                }
            }
        }
        return curveParams;
    }

    public PS06Parameters getCipherParams() {
        if (cipherParams == null) {
            synchronized(cipherParamSync) {
                if (cipherParams == null) {
                    cipherParams = readCipherParams();
                }
            }
        }
        return cipherParams;
    }

    public PS06PublicKeyParameters getMasterPublic() {
        if (masterPublic == null) {
            synchronized (masterPublicSync) {
                if (masterPublic == null) {
                    masterPublic = readMasterPublic();
                }
            }
        }
        return masterPublic;
    }

    public PS06PublicKeyParameters getMasterSecret() {
        if (masterSecret == null) {
            synchronized (masterSecretSync) {
                if (masterSecret == null) {
                    masterSecret = readMasterSecret();
                }
            }
        }
        return masterPublic;
    }

    public Pairing getPairing() {
        if (pairing == null) {
            synchronized (pairingSync) {
                if (pairing == null) {
                    pairing = PairingFactory.getPairing(getCurveParams());
                }
            }
        }
        return pairing;
    }

    private static PropertiesParameters generateCurveParams() {
        PropertiesParameters curveParams = new PropertiesParameters();
        curveParams.put("type", "a");
        curveParams.put("q", "8780710799663312522437781984754049815806883199414208211028653399266475630880222957078625179422662221423155858769582317459277713367317481324925129998224791");
        curveParams.put("h", "12016012264891146079388821366740534204802954401251311822919615131047207289359704531102844802183906537786776");
        curveParams.put("r", "730750818665451621361119245571504901405976559617");
        curveParams.put("exp2", "159");
        curveParams.put("exp1", "107");
        curveParams.put("sign1", "1");
        curveParams.put("sign0", "1");
        return curveParams;
    }

    private PS06Parameters readCipherParams() {
        byte[] bytes;
        try {
            bytes = reader.readFile("cipher_params.sign.param");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Serialiser.deserialiseCipherParams(bytes, getCurveParams(), getPairing(), NU, NM);
    }

    private PS06PublicKeyParameters readMasterPublic() {
        byte[] bytes;
        try {
            bytes = reader.readFile("mpk.sign.param");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Serialiser.deserialiseMasterPublic(bytes, getCipherParams(), getPairing());
    }

    private PS06MasterSecretKeyParameters readMasterSecret() {
        byte[] bytes;
        try {
            bytes = reader.readFile("msk.sign.param");
        } catch (IOException e) {
            return null;
        }
        return Serialiser.deserialiseMasterSecret(bytes, getCipherParams(), getPairing());
    }

}