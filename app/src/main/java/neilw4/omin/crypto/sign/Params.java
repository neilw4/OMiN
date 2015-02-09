package neilw4.omin.crypto.sign;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;

import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06MasterSecretKeyParameters;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06Parameters;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06PublicKeyParameters;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.parameters.PropertiesParameters;

public abstract class Params {

    protected final static int NU = 256;
    protected final static int NM = 256;

    private volatile PropertiesParameters curveParams = null;
    private volatile PS06Parameters cipherParams = null;
    private volatile AsymmetricCipherKeyPair keyPair = null;
    private volatile Pairing pairing = null;

    private final Object curveParamSync = new Object();
    private final Object cipherParamSync = new Object();
    private final Object keyPairSync = new Object();
    private final Object pairingSync = new Object();

    public PropertiesParameters getCurveParams() {
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
                    cipherParams = generateCipherParams();
                }
            }
        }
        return cipherParams;
    }

    public AsymmetricCipherKeyPair getKeyPair() {
        if (keyPair == null) {
            synchronized (keyPairSync) {
                if (keyPair == null) {
                    keyPair = generateKeyPair();
                }
            }
        }
        return keyPair;
    }

    public PS06MasterSecretKeyParameters getMasterSecret() {
        return (PS06MasterSecretKeyParameters)getKeyPair().getPrivate();
    }

    public PS06PublicKeyParameters getMasterPublic() {
        return (PS06PublicKeyParameters)getKeyPair().getPublic();
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

    protected PropertiesParameters generateCurveParams() {
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

    protected abstract PS06Parameters generateCipherParams();
    protected abstract AsymmetricCipherKeyPair generateKeyPair();

}
