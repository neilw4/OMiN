package neilw4.omin;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.*;

import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06MasterSecretKeyParameters;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06SecretKeyParameters;
import neilw4.omin.crypto.Base64;
import neilw4.omin.crypto.sign.FileParams;
import neilw4.omin.crypto.sign.PS06;
import neilw4.omin.crypto.sign.Params;
import neilw4.omin.crypto.sign.ReadParams;
import neilw4.omin.crypto.sign.Serialiser;

public class PKG {
    public static final File USERS_FILE = new File("users.txt");
    public static final File MSK_FILE = new File("msk.params");
    public static final File MPK_FILE = new File("mpk.params");
    public static final File PARAMS_FILE = new File("cipher_params.params");
    public static final File ERROR_LOG = new File("errors.log");

    public static void main(String args[]) {
        FileChannel usersChannel = null;
        FileLock usersLock = null;
        try {
            // Redirect stderr to a file.
            System.setErr(new PrintStream(new FileOutputStream(ERROR_LOG, true)));

            System.out.println("Content-type: text/plain");

            Hashtable form_data = cgi_lib.ReadParse(System.in);

            if (!form_data.containsKey("id")) {
                // Request must contain the ID.
                System.out.println("Status: 400 Bad Request\n");
                return;
            }

            String id = ((String)form_data.get("id")).trim();
            if (!id.matches("[a-z]+")) {
                System.out.println("Status: 400 Bad Request\n");
                return;
            }

            if (!USERS_FILE.exists()) {
                USERS_FILE.createNewFile();
            }

            usersChannel = new RandomAccessFile(USERS_FILE, "rw").getChannel();
            usersLock = usersChannel.lock();

            InputStream usersIn = Channels.newInputStream(usersChannel);

            BufferedReader usersReader = new BufferedReader(new InputStreamReader(usersIn));
            String ln;
            while((ln = usersReader.readLine()) != null) {
               if (id.equals(ln.trim())) {
                   // ID already exists.
                   System.out.println("Status: 401 Unauthorized\n");
                   return;
               }
            }

            // Finished with CGI headers.
            System.out.println();

            OutputStream usersOut = Channels.newOutputStream(usersChannel);
            BufferedWriter usersWriter = new BufferedWriter(new OutputStreamWriter(usersOut));
            usersWriter.write(id + "\n");
            usersWriter.flush();

            Params params = new ReadParams(new FileParams.Reader(PARAMS_FILE, MPK_FILE, MSK_FILE));

            PS06MasterSecretKeyParameters msk = params.getMasterSecret();
            PS06SecretKeyParameters sk = (PS06SecretKeyParameters)
                    new PS06().extract(
                            new AsymmetricCipherKeyPair(
                                    params.getMasterPublic(),
                                    params.getMasterSecret()),
                            id);

            byte[] skBytes = Serialiser.serialiseSecret(sk);
            String skString = Base64.encodeToString(skBytes, Base64.NO_WRAP);
            System.out.println(skString);

        } catch (Exception e) {
            System.out.println("Status: 500 Internal Server Error\n");
            e.printStackTrace();
        } finally {
            if (usersChannel != null) {
                try {
                    usersChannel.close();
                } catch (IOException e) {}
            }
            if (usersLock != null) {
                try {
                    usersLock.release();
                } catch (IOException e) {}
            }
        }

    }
}
