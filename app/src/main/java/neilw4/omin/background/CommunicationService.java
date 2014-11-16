package neilw4.omin.background;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;


public class CommunicationService extends IntentService {

    public static String TAG = "CommunicationService";
    public static int REPEAT_SECONDS = 5;//30;

    private static PendingIntent repeatingIntent;

    public static void start(Context context) {
        stop(context);
        Intent intent = new Intent(context, CommunicationService.class);
        android.util.Log.d(TAG, "Starting CommunicationService");
        repeatingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, 0, REPEAT_SECONDS * 1000, repeatingIntent);
    }

    public static void stop(Context context) {
        if (repeatingIntent != null) {
            AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            alarm.cancel(repeatingIntent);
        }
    }

    public CommunicationService() {
        super("CommunicationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        android.util.Log.d(TAG, "Got intent " + intent);
        //TODO
    }
}
