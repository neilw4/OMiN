package neilw4.omin.crypto.sign;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;

import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06Parameters;

public class GeneratedParams extends Params {

    @Override
    protected PS06Parameters generateCipherParams() {
        return new PS06().createParameters(NU, NM, getCurveParams());
    }

    @Override
    protected AsymmetricCipherKeyPair generateKeyPair() {
        return new PS06().setup(getCipherParams());
    }

}
