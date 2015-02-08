package neilw4.omin.crypto.sign;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.digests.SHA256Digest;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import javax.crypto.Cipher;

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
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingPreProcessing;
import it.unisa.dia.gas.plaf.jpbc.field.curve.CurveElement;
import it.unisa.dia.gas.plaf.jpbc.field.curve.CurveField;
import it.unisa.dia.gas.plaf.jpbc.pairing.AbstractPairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeAPairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.immutable.ImmutablePairingPreProcessing;
import it.unisa.dia.gas.plaf.jpbc.pairing.parameters.PropertiesParameters;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static neilw4.omin.Logger.error;

import it.unisa.dia.gas.plaf.jpbc.pairing.immutable.ImmutableParing;
import neilw4.omin.db.Message;

public class Signer {

    public static void sign(Message msg) {
        //TODO
    }

    public static boolean verify(Message msg) {
        //TODO
        return true;
    }

//    private static PropertiesParameters curveParams = curveParams();
//    private static CurveElement curveElement = curveElement();

//    public static PS06Parameters master = new PS06Parameters(
//        curveParams,
//            curveElement,
//        256,
//        256
//    );

//    public static byte[] privateKey(String id) {
//        AsymmetricCipherKeyPair
//    }
//
//    public static String sign(String... msg) {
//
//    }
//
//    public static boolean verify(String signature, String... msg) {
//
//    }

//    public static AsymmetricCipherKeyPair genMasterKeys(int nU, int nM) throws UnsupportedEncodingException {
//        PS06Parameters params = new PS06ParametersGenerator().init(
//                curveParams(),
//                nU, nM).generateParameters();
//        PS06SetupGenerator setup = new PS06SetupGenerator();
//                   setup.init(new PS06SetupGenerationParameters(null, params));
//        AsymmetricCipherKeyPair masters = setup.generateKeyPair();
//
//        Pairing pairing = PairingFactory.getPairing(params.getCurveParams());
//
//        Element mskElement = ((PS06MasterSecretKeyParameters)masters.getPrivate()).getMsk();
//        byte[] mskBytes = mskElement.toBytes();
//        String mskString = Arrays.toString(mskBytes);
//
//        PS06Parameters params2 = new PS06ParametersGenerator().init(
//                curveParams(),
//                nU, nM).generateParameters();
//        Pairing pairing2 = PairingFactory.getPairing(params.getCurveParams());
//
//        Element restoredMskElement = new CurveElement((CurveField)pairing2.getG1());
//        restoredMskElement.setFromBytes(mskBytes);
//
//
//        PS06PublicKeyParameters
//
//
//        CipherParameters secretKey = extract(new AsymmetricCipherKeyPair(masters.getPublic(), new PS06MasterSecretKeyParameters(params, restoredMskElement)), "01001101");
//        String message = "Hello World!!!";
//        byte[] signature = sign(message, secretKey);
//        assertTrue(verify(masters.getPublic(), message, "01001101", signature));
//
//
//        return masters;
//    }

}
