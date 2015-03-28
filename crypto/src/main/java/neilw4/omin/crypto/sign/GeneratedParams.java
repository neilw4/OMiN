package neilw4.omin.crypto.sign;


import org.bouncycastle.crypto.AsymmetricCipherKeyPair;

import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06MasterSecretKeyParameters;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06PublicKeyParameters;

public class GeneratedParams extends Params {

    @Override
    protected void generateCipherParams() {
        generateParams();
    }

    @Override
    protected void generateMasterPublic() {
        generateParams();
    }

    @Override
    protected void generateMasterSecret() {
        generateParams();
    }

    private void generateParams() {
        long start = System.nanoTime();

        PS06 ps06 = new PS06();
        cipherParams = ps06.createParameters(NU, NM, getCurveParams());
        AsymmetricCipherKeyPair keyPair = ps06.setup(cipherParams);
        masterPublic = (PS06PublicKeyParameters) keyPair.getPublic();
        masterSecret = (PS06MasterSecretKeyParameters) keyPair.getPrivate();

        long end = System.nanoTime();
        System.err.println("generated master parameters in " + ((end - start) / 1000000) + "ms");
    }
}
