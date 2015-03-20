package neilw4.omin.crypto.sign;

public class ReadParams extends Params {

    public interface ParamsReader {
        byte[] readCipherParams();
        byte[] readMPK();
        byte[] readMSK();
    }

    private final ParamsReader reader;

    public ReadParams(ParamsReader reader) {
        this.reader = reader;
    }


    protected void generateCipherParams() {
        byte[] bytes = reader.readCipherParams();
        cipherParams = Serialiser.deserialiseCipherParams(bytes, getCurveParams(), getPairing(), NU, NM);
    }

    protected void generateMasterPublic() {
        byte[] bytes = reader.readMPK();
        masterPublic = Serialiser.deserialiseMasterPublic(bytes, getCipherParams(), getPairing());
    }

    protected void generateMasterSecret() {
        byte[] bytes = reader.readMSK();
        masterSecret = Serialiser.deserialiseMasterSecret(bytes, getCipherParams(), getPairing());
    }

}
