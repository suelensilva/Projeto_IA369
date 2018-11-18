package br.com.ia369.bichinhovirtual.appraisal;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class EmotionEngineService extends Service {

    private static final String TAG = EmotionEngineService.class.getSimpleName();
    public static final int REQUEST_CODE = 42;
    public static final int INTERVAL_TIME = 60 * 1000;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        scheduleEmotionEngineJob(this);
        return super.onStartCommand(intent, flags, startId);
    }

    public static void scheduleEmotionEngineJob(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent emotionEngineServiceIntent = new Intent(context, EmotionEngineService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, REQUEST_CODE, emotionEngineServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long now = System.currentTimeMillis();
        long triggerAt = now + INTERVAL_TIME;

        AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(triggerAt, null);

        if(alarmManager != null) {
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
        }
    }
}
