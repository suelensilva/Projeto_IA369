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
import java.util.ArrayList;
import java.util.List;

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
                        case AppraisalConstants.ACTIVE_INPUT_ACTION:
                            int inputType = intent.getIntExtra(AppraisalConstants.INPUT_TYPE_EXTRA, -1);
                            if(inputType > 0) {
                                new AppraiseActiveInputAsyncTask(this).execute(inputType);
                            }
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
            Log.d(TAG, "Appraising passive inputs...");

            EmotionEngineService emotionEngineService = emotionEngineServiceWeakReference.get();
            Application application = emotionEngineService.getApplication();
            EmotionRepository repository = new EmotionRepository(application);

            // TODO pegar os valores dos inputs passivos

            Creature creature = repository.getCreature();
            emotionEngineService.decayEmotionIntensity(creature);

            Log.d(TAG, "[passive] emotion = "+creature.getEmotion());
            repository.updateCreature(creature);

//            EmotionVariables emotionVariables = repository.getEmotionVariable(AppraisalConstants.PERSONALITY_EXTROVERT, AppraisalConstants.INPUT_TEXT_JOY);
//
//            if(emotionVariables != null) {
//                emotionEngineService.appraiseEmotions(emotionVariables);
//            }

            return null;
        }
    }

    static class AppraiseActiveInputAsyncTask extends AsyncTask<Integer, Void, Void> {
        WeakReference<EmotionEngineService> emotionEngineServiceWeakReference;

        AppraiseActiveInputAsyncTask(EmotionEngineService instance) {
            this.emotionEngineServiceWeakReference = new WeakReference<>(instance);
        }

        @Override
        protected Void doInBackground(Integer... params) {
            Log.d(TAG, "Appraising active inputs...");

            int inputAction = params[0];

            EmotionEngineService emotionEngineService = emotionEngineServiceWeakReference.get();
            Application application = emotionEngineService.getApplication();
            EmotionRepository repository = new EmotionRepository(application);

            Creature creature = repository.getCreature();

            EmotionVariables emotionVariables = repository.getEmotionVariable(creature.getPersonality(), inputAction);

            if(emotionVariables != null) {
                Emotion newEmotion = emotionEngineService.appraiseNewEmotion(emotionVariables);
                if(newEmotion != null) {
                    Log.d(TAG, "New emotion = "+newEmotion.getName());
                    creature.setEmotion(Appraisal.getEmotionIdByName(newEmotion.getName()));
                    creature.setIntensity(newEmotion.getIntensity());

                    Log.d(TAG, "[active] emotion = "+creature.getEmotion());
                    repository.updateCreature(creature);
                }
            }

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

    private Emotion appraiseNewEmotion(EmotionVariables emotionVariables) {

        List<Emotion> emotionList = new ArrayList<>();

        Emotion fear = Appraisal.evaluateFear(emotionVariables);
        if(fear != null) {
            Log.d(TAG, "Fear intensity = "+fear.getIntensity());
            emotionList.add(fear);
        } else {
            Log.d(TAG, "Not fear");
        }

        Emotion joy = Appraisal.evaluateJoy(emotionVariables);
        if(joy != null) {
            Log.d(TAG, "Joy intensity = "+joy.getIntensity());
            emotionList.add(joy);
        } else {
            Log.d(TAG, "Not joy");
        }

        Emotion sadness = Appraisal.evaluateSadness(emotionVariables);
        if(sadness != null) {
            Log.d(TAG, "Sadness intensity = "+sadness.getIntensity());
            emotionList.add(sadness);
        } else {
            Log.d(TAG, "Not sadness");
        }

        Emotion disgust = Appraisal.evaluateReproach(emotionVariables);
        if(disgust != null) {
            Log.d(TAG, "Disgust intensity = "+disgust.getIntensity());
            emotionList.add(disgust);
        } else {
            Log.d(TAG, "Not disgust");
        }

        Emotion anger = Appraisal.evaluateAnger(emotionVariables);
        if(anger != null) {
            Log.d(TAG, "Anger intensity = "+anger.getIntensity());
            emotionList.add(anger);
        } else {
            Log.d(TAG, "Not anger");
        }

        Emotion satisfaction = Appraisal.evaluateSatisfaction(emotionVariables);
        if(satisfaction != null) {
            Log.d(TAG, "Satisfaction intensity = "+satisfaction.getIntensity());
            emotionList.add(satisfaction);
        } else {
            Log.d(TAG, "Not satisfaction");
        }

        Emotion distress = Appraisal.evaluateDistress(emotionVariables);
        if(distress != null) {
            Log.d(TAG, "Distress intensity = "+distress.getIntensity());
            emotionList.add(distress);
        } else {
            Log.d(TAG, "Not distress");
        }

        Emotion gratitude = Appraisal.evaluateGratitude(emotionVariables);
        if(gratitude != null) {
            Log.d(TAG, "Gratitude intensity = "+gratitude.getIntensity());
            emotionList.add(gratitude);
        } else {
            Log.d(TAG, "Not gratitude");
        }

        if(emotionList.size() == 1) {
            return emotionList.get(0);
        } else if(emotionList.size() > 1) {
            // Seleciona a emocao com maior intensidade

            Emotion moreIntenseEmotion = null;
            double currIntensity = -1;
            for (int i = 0; i < emotionList.size(); i++) {
                Emotion emotion = emotionList.get(i);
                if(emotion.getIntensity() > currIntensity) {
                    moreIntenseEmotion = emotion;
                    currIntensity = emotion.getIntensity();
                }
            }

            return moreIntenseEmotion;
        }

        return null;
    }
}
