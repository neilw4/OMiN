package neilw4.omin.crypto.sign;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

public class FileParams {

    public static class Reader implements ReadParams.ParamsReader {
        private final File paramsFile;
        private final File mpkFile;
        private final File mskFile;

        public Reader(File paramsFile, File mpkFile, File mskFile) {
            this.paramsFile = paramsFile;
            this.mpkFile = mpkFile;
            this.mskFile = mskFile;
        }

        @Override
        public byte[] readCipherParams() {
            return read(paramsFile);
        }

        @Override
        public byte[] readMPK() {
            return read(mpkFile);
        }

        @Override
        public byte[] readMSK() {
            return read(mskFile);
        }

        private byte[] read(File f) {
            try {
                try {
                    return Files.readAllBytes(f.toPath());
                } catch (FileNotFoundException | NoSuchFileException e) {
                    new Writer(new GeneratedParams()).save(paramsFile, mpkFile, mskFile);
                    return Files.readAllBytes(f.toPath());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public static class Writer {

        private final Params params;

        public Writer(Params params) {
            this.params = params;
        }

        public void save(File paramsFile, File mpkFile, File mskFile) throws IOException {
            byte[] cipherParams = Serialiser.serialiseCipherParams(params.getCipherParams());
            Files.write(paramsFile.toPath(), cipherParams);

            byte[] mpk = Serialiser.serialiseMasterPublic(params.getMasterPublic());
            Files.write(mpkFile.toPath(), mpk);

            byte[] msk = Serialiser.serialiseMasterSecret(params.getMasterSecret());
            Files.write(mskFile.toPath(), msk);
        }
    }

}
