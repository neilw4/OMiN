package neilw4.omin.crypto.sign;

import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06MasterSecretKeyParameters;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06Parameters;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06PublicKeyParameters;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.parameters.PropertiesParameters;

public abstract class Params {

    public final static int NU = 256;
    public final static int NM = 256;

    private volatile PropertiesParameters curveParams = null;
    protected volatile PS06Parameters cipherParams = null;
    protected volatile PS06PublicKeyParameters masterPublic = null;
    protected volatile PS06MasterSecretKeyParameters masterSecret = null;
    private volatile Pairing pairing = null;

    private final Object curveParamSync = new Object();
    private final Object cipherParamSync = new Object();
    private final Object masterPublicSync = new Object();
    private final Object masterSecretSync = new Object();
    private final Object pairingSync = new Object();


    public PropertiesParameters getCurveParams() {
        if (curveParams == null) {
            synchronized (curveParamSync) {
                if (curveParams == null) {
                    generateCurveParams();
                    assert curveParams != null;
                }
            }
        }
        return curveParams;
    }

    public PS06Parameters getCipherParams() {
        if (cipherParams == null) {
            synchronized(cipherParamSync) {
                if (cipherParams == null) {
                    generateCipherParams();
                    assert cipherParams != null;
                }
            }
        }
        return cipherParams;
    }

    public PS06PublicKeyParameters getMasterPublic() {
        if (masterPublic == null) {
            synchronized (masterPublicSync) {
                if (masterPublic == null) {
                    generateMasterPublic();
                    assert masterPublic != null;
                }
            }
        }
        return masterPublic;
    }

    public PS06MasterSecretKeyParameters getMasterSecret() {
        if (masterSecret == null) {
            synchronized (masterSecretSync) {
                if (masterSecret == null) {
                    generateMasterSecret();
                    assert masterSecret != null;
                }
            }
        }
        return masterSecret;
    }

    public Pairing getPairing() {
        if (pairing == null) {
            synchronized (pairingSync) {
                if (pairing == null) {
                    generatePairing();
                    assert pairing != null;
                }
            }
        }
        return pairing;
    }

    protected void generateCurveParams() {
        curveParams = new PropertiesParameters();
        curveParams.put("type", "a");
        curveParams.put("q", "8780710799663312522437781984754049815806883199414208211028653399266475630880222957078625179422662221423155858769582317459277713367317481324925129998224791");
        curveParams.put("h", "12016012264891146079388821366740534204802954401251311822919615131047207289359704531102844802183906537786776");
        curveParams.put("r", "730750818665451621361119245571504901405976559617");
        curveParams.put("exp2", "159");
        curveParams.put("exp1", "107");
        curveParams.put("sign1", "1");
        curveParams.put("sign0", "1");
    }

    protected void generatePairing() {
        pairing = PairingFactory.getPairing(getCurveParams());
    }

    protected abstract void generateCipherParams();

    protected abstract void generateMasterPublic();

    protected abstract void generateMasterSecret();

}
