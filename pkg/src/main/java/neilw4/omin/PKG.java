package neilw4.omin;

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

public class PKG {
    public static final File USERS_FILE = new File("users.txt");
    public static final File SK_FILE = new File("sk.txt");
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
                System.out.println("Status: 400 Bad Request");
                return;
            }

            String id = ((String)form_data.get("id")).trim();
            if (!id.matches("[a-z]+")) {
                System.out.println("Status: 400 Bad Request");
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
                   System.out.println("Status: 401 Unauthorized");
                   return;
               }
            }

            OutputStream usersOut = Channels.newOutputStream(usersChannel);
            BufferedWriter usersWriter = new BufferedWriter(new OutputStreamWriter(usersOut));
            usersWriter.write(id + "\n");
            usersWriter.flush();



        } catch (Exception e) {
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
