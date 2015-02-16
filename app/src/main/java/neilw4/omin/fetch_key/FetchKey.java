package neilw4.omin.fetch_key;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;


import com.orm.query.Select;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import neilw4.omin.db.SecretKey;

import static neilw4.omin.Logger.*;

public class FetchKey {

    private static final String PKG_URL = "http://ndw.host.cs.st-andrews.ac.uk/omin.cgi";

    private static volatile AsyncFetchTask asyncFetchTask = null;

    public static void asyncFetch() {
        List<SecretKey> needsKey = new ArrayList<>();
        for (SecretKey pk: Select.from(SecretKey.class).list()) {
            if (pk.ps06Key == null) {
                needsKey.add(pk);
            }
        }

        if (asyncFetchTask == null && !needsKey.isEmpty()) {
            asyncFetchTask = new AsyncFetchTask(needsKey);
            asyncFetchTask.execute();
        }
    }

    private static class AsyncFetchTask extends AsyncTask<Void, Void, Void> {

        private static final String TAG = AsyncFetchTask.class.getSimpleName();
        private final List<SecretKey> needsKey;

        protected AsyncFetchTask(List<SecretKey> needsKey) {
            this.needsKey = needsKey;
        }

        @Override
        protected Void doInBackground(Void... params) {
            AndroidHttpClient client = AndroidHttpClient.newInstance("OMiN");
            info(TAG, "Fetching secret keys from PKG");
            try {
                for (SecretKey pk: needsKey) {
                    HttpGet get = new HttpGet(PKG_URL + "?id=" + pk.uid.uname);
                    HttpResponse response = client.execute(get);
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == 200) {
                        pk.ps06Key = EntityUtils.toString(response.getEntity());
                        info(TAG, "successfully got secret key for " + pk.uid.uname);
                    } else if (statusCode == 401) {
                        // id already taken.
                        warn(TAG, "ID " + pk.uid.uname + " already taken");
                        pk.delete();
                    } else {
                        error(TAG, "PKG returned " + response.getStatusLine());
                    }
                }
            } catch (IOException e) {
                debug(TAG, "Failed to communicate with PKG", e);
            } finally {
                client.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            for (SecretKey pk: needsKey) {
                if (pk.ps06Key != null) {
                    pk.save();
                }
            }
            asyncFetchTask = null;
        }
    }
}
