package br.com.ia369.bichinhovirtual.appraisal;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.occ.entities.Emotion;

import java.lang.ref.WeakReference;

import br.com.ia369.bichinhovirtual.model.EmotionVariables;
import br.com.ia369.bichinhovirtual.room.EmotionRepository;

public class EmotionEngineService extends Service {

    private static final String TAG = EmotionEngineService.class.getSimpleName();
    public static final int REQUEST_CODE = 42;
    public static final int INTERVAL_TIME = 10 * 1000; // TODO definir intervalo de tempo adequado

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        scheduleEmotionEngineJob(this);

        new GetEmotionVariablesAsyncTask(this).execute();

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

    static class GetEmotionVariablesAsyncTask extends AsyncTask<Void, Void, Void> {
        WeakReference<EmotionEngineService> emotionEngineServiceWeakReference;

        public GetEmotionVariablesAsyncTask(EmotionEngineService instance) {
            this.emotionEngineServiceWeakReference = new WeakReference<>(instance);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Application application = emotionEngineServiceWeakReference.get().getApplication();
            EmotionRepository repository = new EmotionRepository(application);
            EmotionVariables emotionVariables = repository.getEmotionVariable(AppraisalConstants.PERSONALITY_EXTROVERT, AppraisalConstants.INPUT_TEXT_JOY);
            if(emotionVariables != null) {
                Emotion fear = Appraisal.evaluateFear(emotionVariables);
                if(fear != null) {
                    Log.d(TAG, "Fear intensity = "+fear.getIntensity());
                } else {
                    Log.d(TAG, "Not fear");
                }

                Emotion joy = Appraisal.evaluateJoy(emotionVariables);
                if(joy != null) {
                    Log.d(TAG, "Joy intensity = "+joy.getIntensity());
                } else {
                    Log.d(TAG, "Not joy");
                }
            }
            return null;
        }
    }
}
