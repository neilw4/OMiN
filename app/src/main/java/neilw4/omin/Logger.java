package neilw4.omin;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.widget.Toast;

import com.logentries.android.AndroidLogger;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class Logger {
    public static AndroidLogger logger;
    private static final String TAG = Logger.class.getSimpleName();
    private static Context context;
    private static Handler handler = new Handler();

    private Logger() {}


    public static synchronized void setupLog(Context context) {
        Logger.context = context.getApplicationContext();
        if (logger == null) {
            logger = AndroidLogger.getLogger(Logger.context, "33d79264-9dc8-4102-9e73-2ded60227ecb", false);
            debug(TAG, "starting logger");
        }
    }

    public static void verbose(String tag, String msg) {
        android.util.Log.v(tag, msg);
    }

    public static void verbose(String tag, String msg, Exception e) {
        final StringWriter string = new StringWriter();
        e.printStackTrace(new PrintWriter(string, true));
        verbose(tag, msg + ": " + string.toString());
    }

    public static void debug(String tag, String msg) {
        android.util.Log.d(tag, msg);
    }

    public static void debug(String tag, String msg, Exception e) {
        final StringWriter string = new StringWriter();
        e.printStackTrace(new PrintWriter(string, true));
        debug(tag, msg + ": " + string.toString());
    }

    public static void info(String tag, String msg) {
        if (logger != null) {
            logger.info(tag + ": " + msg);
        }
        android.util.Log.i(tag, msg);
        toast(msg);
    }

    public static void info(String tag, String msg, Exception e) {
        final StringWriter string = new StringWriter();
        e.printStackTrace(new PrintWriter(string, true));
        info(tag, msg + ": " + string.toString());
    }

    public static void warn(String tag, String msg) {
        if (logger != null) {
            logger.warn(tag + ": " + msg);
        }
        android.util.Log.w(tag, msg);
        toast(msg);
    }

    public static void warn(String tag, String msg, Exception e) {
        final StringWriter string = new StringWriter();
        e.printStackTrace(new PrintWriter(string, true));
        warn(tag, msg + ": " + string.toString());
    }

    public static void error(String tag, String msg) {
        if (logger != null) {
            logger.error(tag + ": " + msg);
        }
        android.util.Log.e(tag, msg);
        toast(msg);
    }

    public static void error(String tag, String msg, Exception e) {
        final StringWriter string = new StringWriter();
        e.printStackTrace(new PrintWriter(string, true));
        error(tag, msg + ": " + string.toString());
    }

    // http://stackoverflow.com/a/20072918
    public static synchronized void toast(final String msg) {
        try {
            new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
            }.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
