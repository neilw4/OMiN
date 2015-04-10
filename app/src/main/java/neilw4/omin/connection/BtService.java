package neilw4.omin.connection;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;


import static neilw4.omin.Logger.*;

public class BtService extends Service {

    private static final String TAG = BtService.class.getSimpleName();

    private static final String ACTION_SCAN = BtService.class.getCanonicalName() + ".SCAN";

    // Start bluetooth discovery after REPEAT_SECONDS seconds.
    public static final int REPEAT_SECONDS = 450;

    private static PendingIntent repeatingIntent = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void startRepeatingAlarm(Context context) {
        if (repeatingIntent == null) {
            AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent scanIntent = new Intent(context, BtService.class);
            scanIntent.setAction(ACTION_SCAN);
            repeatingIntent = PendingIntent.getService(context, 0, scanIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, 0, REPEAT_SECONDS * 1000, repeatingIntent);
            debug(TAG, "started repeating alarm");
        }
    }

    public static void stopRepeatingAlarm(Context context) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(repeatingIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onHandleIntent(intent);
        return START_STICKY;
    }

    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        debug(TAG, action);

        if (ACTION_SCAN.equals(action)) {
            BtManager.getInstance().scan(this);
        } else {
            warn(TAG, "unexpected intent action " + action);
        }
    }


}
