package br.com.ia369.bichinhovirtual.appraisal;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.app.Service;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.occ.entities.Emotion;

import java.lang.ref.WeakReference;

import br.com.ia369.bichinhovirtual.model.Creature;
import br.com.ia369.bichinhovirtual.model.EmotionVariables;
import br.com.ia369.bichinhovirtual.room.EmotionRepository;

public class EmotionEngineService extends Service {

    private static final String TAG = EmotionEngineService.class.getSimpleName();

    private static final String INTERVAL_REQUEST = "interval_request";

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

        if(intent != null) {
            if(intent.hasExtra(INTERVAL_REQUEST)) {
                scheduleEmotionEngineJob(this);
                new AppraisePassiveInputsAsyncTask(this).execute();
            } else {
                String action = intent.getAction();
                if(action != null) {
                    switch (action) {
                        case AppraisalConstants.INPUT_FACE_POSITIVE_ACTION:
                            break;
                        case AppraisalConstants.INPUT_FACE_NEGATIVE_ACTION:
                            break;
                        case AppraisalConstants.INPUT_TEXT_ANGRY_ACTION:
                            break;
                        case AppraisalConstants.INPUT_TEXT_DISGUST_ACTION:
                            break;
                        case AppraisalConstants.INPUT_TEXT_FEAR_ACTION:
                            break;
                        case AppraisalConstants.INPUT_TEXT_JOY_ACTION:
                            break;
                        case AppraisalConstants.INPUT_TEXT_SADNESS_ACTION:
                            break;
                        case AppraisalConstants.INPUT_PROXIMITY_SENSOR_ACTION:
                            break;
                        case AppraisalConstants.INPUT_TIME_DISPOSED_ACTION:
                            break;
                        case AppraisalConstants.INPUT_TIME_INDISPOSED_ACTION:
                            break;
                        case AppraisalConstants.INPUT_LOCATION_IDLE_ACTION:
                            break;
                        case AppraisalConstants.INPUT_LOCATION_MOVING_ACTION:
                            break;
                        case AppraisalConstants.INPUT_FORECAST_COLD_ACTION:
                            break;
                        case AppraisalConstants.INPUT_FORECAST_GOOD_ACTION:
                            break;
                        case AppraisalConstants.INPUT_FORECAST_HOT_ACTION:
                            break;
                        case AppraisalConstants.INPUT_FORECAST_RAIN_ACTION:
                            break;

                    }
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }


    // FIXME alarmManager nao funciona em doze mode
    public static void scheduleEmotionEngineJob(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent emotionEngineServiceIntent = new Intent(context, EmotionEngineService.class);
        emotionEngineServiceIntent.putExtra(INTERVAL_REQUEST, true);
        PendingIntent pendingIntent = PendingIntent.getService(context, REQUEST_CODE, emotionEngineServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long now = System.currentTimeMillis();
        long triggerAt = now + INTERVAL_TIME;

        AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(triggerAt, null);

        if(alarmManager != null) {
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
        }
    }

    static class AppraisePassiveInputsAsyncTask extends AsyncTask<Void, Void, Void> {
        WeakReference<EmotionEngineService> emotionEngineServiceWeakReference;

        AppraisePassiveInputsAsyncTask(EmotionEngineService instance) {
            this.emotionEngineServiceWeakReference = new WeakReference<>(instance);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            EmotionEngineService emotionEngineService = emotionEngineServiceWeakReference.get();
            Application application = emotionEngineService.getApplication();
            EmotionRepository repository = new EmotionRepository(application);

            // TODO pegar os valores dos inputs passivos

            Creature creature = repository.getCreature();
            emotionEngineService.decayEmotionIntensity(creature);
            repository.updateCreature(creature);

//            EmotionVariables emotionVariables = repository.getEmotionVariable(AppraisalConstants.PERSONALITY_EXTROVERT, AppraisalConstants.INPUT_TEXT_JOY);
//
//            if(emotionVariables != null) {
//                emotionEngineService.appraiseEmotions(emotionVariables);
//            }

            return null;
        }
    }

    private void decayEmotionIntensity(Creature creature) {
        double decayFactor = creature.getDecayFactor();
        double emotionIntensity = creature.getIntensity();
        int emotion = creature.getEmotion();

        double emotionNewIntensity = emotionIntensity-decayFactor > 0 ? emotionIntensity-decayFactor : 0.0;
        if(emotionNewIntensity == 0.0) {
            switch (emotion) {
                case AppraisalConstants.EMOTION_NEUTRAL:
                    emotion = AppraisalConstants.EMOTION_BORED;
                    break;
                case AppraisalConstants.EMOTION_BORED:
                    // continua entendiado
                    break;
                default:
                    emotion = AppraisalConstants.EMOTION_NEUTRAL;
                    emotionNewIntensity = 5.0;
                    break;
            }
        }

        Log.d(TAG, "Emotion = "+emotion);
        Log.d(TAG, "Emotion intensity = "+emotionNewIntensity);

        creature.setEmotion(emotion);
        creature.setIntensity(emotionNewIntensity);
    }

    private void appraiseEmotions(EmotionVariables emotionVariables) {
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

        Emotion sadness = Appraisal.evaluateSadness(emotionVariables);
        if(sadness != null) {
            Log.d(TAG, "Sadness intensity = "+sadness.getIntensity());
        } else {
            Log.d(TAG, "Not sadness");
        }

        Emotion disgust = Appraisal.evaluateReproach(emotionVariables);
        if(disgust != null) {
            Log.d(TAG, "Disgust intensity = "+disgust.getIntensity());
        } else {
            Log.d(TAG, "Not disgust");
        }

        Emotion anger = Appraisal.evaluateAnger(emotionVariables);
        if(anger != null) {
            Log.d(TAG, "Anger intensity = "+anger.getIntensity());
        } else {
            Log.d(TAG, "Not anger");
        }

        Emotion satisfaction = Appraisal.evaluateSatisfaction(emotionVariables);
        if(satisfaction != null) {
            Log.d(TAG, "Satisfaction intensity = "+satisfaction.getIntensity());
        } else {
            Log.d(TAG, "Not satisfaction");
        }

        Emotion distress = Appraisal.evaluateDistress(emotionVariables);
        if(distress != null) {
            Log.d(TAG, "Distress intensity = "+distress.getIntensity());
        } else {
            Log.d(TAG, "Not distress");
        }

        Emotion gratitude = Appraisal.evaluateGratitude(emotionVariables);
        if(gratitude != null) {
            Log.d(TAG, "Gratitude intensity = "+gratitude.getIntensity());
        } else {
            Log.d(TAG, "Not gratitude");
        }
    }
}
