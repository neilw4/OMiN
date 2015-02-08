package neilw4.omin.crypto.sign;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.digests.SHA256Digest;

import it.unisa.dia.gas.crypto.jpbc.signature.ps06.engines.PS06Signer;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.generators.PS06ParametersGenerator;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.generators.PS06SecretKeyGenerator;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.generators.PS06SetupGenerator;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06MasterSecretKeyParameters;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06Parameters;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06PublicKeyParameters;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06SecretKeyGenerationParameters;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06SecretKeyParameters;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06SetupGenerationParameters;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06SignParameters;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06VerifyParameters;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.parameters.PropertiesParameters;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;


//http://gas.dia.unisa.it/projects/jpbc/schemes/ibs_ps06.html#.VNYWyzasXeQ
public class PS06 {

    protected final static int NU = 256;
    protected final static int NM = 256;

    protected static PropertiesParameters curveParams = null;
    protected static PS06Parameters cipherParams = null;
    protected static AsymmetricCipherKeyPair keyPair = null;
    protected static Pairing pairing = null;

    public static PropertiesParameters getCurveParams() {
        if (curveParams == null) {
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
        return curveParams;
    }

    public static PS06Parameters getCipherParams() {
        if (cipherParams == null) {
            cipherParams = Serialiser.deserialiseCipherParams(StoredParams.cipherParams());
        }
        return cipherParams;
    }

    public static AsymmetricCipherKeyPair getKeyPair() {
        if (keyPair == null) {
            PS06MasterSecretKeyParameters masterSecret = Serialiser.deserialiseMasterSecret(StoredParams.masterSecret());
            PS06PublicKeyParameters masterPublic = Serialiser.deserialiseMasterPublic(StoredParams.masterPublic());
            keyPair = new AsymmetricCipherKeyPair(masterPublic, masterSecret);
        }
        return keyPair;
    }

    public static PS06MasterSecretKeyParameters getMasterSecret() {
        return (PS06MasterSecretKeyParameters) getKeyPair().getPrivate();
    }

    public static PS06PublicKeyParameters getMasterPublic() {
        return (PS06PublicKeyParameters) getKeyPair().getPublic();
    }

    public static Pairing getPairing() {
        if (pairing == null) {
            pairing = PairingFactory.getPairing(PS06.getCurveParams());
        }
        return pairing;
    }

    // Generate new master keys based on new pairings and parameters.
    protected static void regenerate() {
        PS06ParametersGenerator generator = new PS06ParametersGenerator();
        generator.init(getCurveParams(), NU, NM);
        cipherParams = generator.generateParameters();

        PS06SetupGenerator setup = new PS06SetupGenerator();
        setup.init(new PS06SetupGenerationParameters(null, getCipherParams()));
        keyPair =  setup.generateKeyPair();

        pairing = PairingFactory.getPairing(getCurveParams());

        byte[] cipherParamsBytes = Serialiser.serialiseCipherParams(cipherParams);
        byte[] masterPrivateBytes = Serialiser.serialiseMasterSecret((PS06MasterSecretKeyParameters) keyPair.getPrivate());
        byte[][] masterPublicBytes = Serialiser.serialiseMasterPublic((PS06PublicKeyParameters) keyPair.getPublic());

//        String cipherParamsString = Base64.encodeToString(cipherParamsBytes, Base64.DEFAULT);
//        String masterPrivateString = Base64.encodeToString(masterPrivateBytes, Base64.DEFAULT);
//        String[] masterPublicString = new String[masterPublicBytes.length];
//        for (int i = 0; i < masterPublicBytes.length; i++) {
//            masterPublicString[i] = Base64.encodeToString(masterPublicBytes[i], Base64.DEFAULT);
//        }
    }

    public static PS06SecretKeyParameters extract(String identity) {
       PS06SecretKeyGenerator extract = new PS06SecretKeyGenerator();
       extract.init(new PS06SecretKeyGenerationParameters(getKeyPair(), identity));

       return (PS06SecretKeyParameters)extract.generateKey();
    }

    public static byte[] sign(String message, PS06SecretKeyParameters secretKey) {
       byte[] bytes = message.getBytes();

       PS06Signer signer = new PS06Signer(new SHA256Digest());
       signer.init(true, new PS06SignParameters(secretKey));
       signer.update(bytes, 0, bytes.length);

       byte[] signature = null;
       try {
           signature = signer.generateSignature();
       } catch (CryptoException e) {
           fail(e.getMessage());
       }

       return signature;
    }

    public static boolean verify(String message, String identity, byte[] signature) {
       byte[] bytes = message.getBytes();

       PS06Signer signer = new PS06Signer(new SHA256Digest());
       signer.init(false, new PS06VerifyParameters(getMasterPublic(), identity));
       signer.update(bytes, 0, bytes.length);

       return signer.verifySignature(signature);
    }

    public static void testPS06() {
        String message = "Hello World!!!";
        PS06SecretKeyParameters secretKey = PS06.extract("01001101");
        byte[] signature = PS06.sign(message, secretKey);
        assertTrue(PS06.verify(message, "01001101", signature));
    }

}
