package neilw4.omin;

import android.content.Context;

import com.logentries.android.AndroidLogger;
import com.logentries.android.TimedLogger;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class Logger {
    public static AndroidLogger logger;
    private static final String TAG = Logger.class.getSimpleName();

    private Logger() {}


    public static synchronized void setupLog(Context context) {
        if (logger == null) {
            logger = AndroidLogger.getLogger(context.getApplicationContext(), "33d79264-9dc8-4102-9e73-2ded60227ecb", false);
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
    }

    public static void error(String tag, String msg, Exception e) {
        final StringWriter string = new StringWriter();
        e.printStackTrace(new PrintWriter(string, true));
        error(tag, msg + ": " + string.toString());
    }

}
