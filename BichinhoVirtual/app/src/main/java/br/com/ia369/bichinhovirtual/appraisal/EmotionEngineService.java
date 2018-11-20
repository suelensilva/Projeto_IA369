package br.com.ia369.bichinhovirtual.appraisal;

import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.occ.entities.Emotion;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import br.com.ia369.bichinhovirtual.MainActivity;
import br.com.ia369.bichinhovirtual.R;
import br.com.ia369.bichinhovirtual.model.Creature;
import br.com.ia369.bichinhovirtual.model.EmotionVariables;
import br.com.ia369.bichinhovirtual.room.EmotionRepository;

public class EmotionEngineService extends Service {

    private static final String TAG = EmotionEngineService.class.getSimpleName();

    private static final String INTERVAL_REQUEST = "interval_request";

    public static final int REQUEST_CODE = 42;
    public static final int NOTIFICATION_ID = 3;
    public static final String CHANNEL_ID = "bichinho_virtual";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        if(intent != null) {
            if(intent.hasExtra(INTERVAL_REQUEST)) {
                scheduleEmotionEngineJob(this);
                new DecayEmotionAsyncTask(this).execute();
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

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        String decayIntervalPrefKey = context.getString(R.string.decay_interval_pref);
        String decayIntervalString = preferences.getString(decayIntervalPrefKey, "10");
        int decayInterval = Integer.valueOf(decayIntervalString);

        long now = System.currentTimeMillis();
        long triggerAt = now + (decayInterval*1000);

        AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(triggerAt, null);

        if(alarmManager != null) {
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
        }
    }

    private void updateCreatureWithNewEmotion(Creature creature, Emotion newEmotion) {
        int emotionId = Appraisal.getEmotionIdByName(newEmotion.getName());

        if(emotionId != creature.getEmotion()) {
            if(newEmotion.getIntensity() >= creature.getIntensity() ||
                    creature.getEmotion() == AppraisalConstants.EMOTION_NEUTRAL) {
                // A nova emocao eh mais intensa que a atual, ou a atual eh neutra
                // que eh suscetivel a qualquer nova emocao
                creature.setEmotion(emotionId);
                creature.setIntensity(newEmotion.getIntensity());
            } else {
                double newIntensity = creature.getIntensity() - newEmotion.getIntensity();
                creature.setIntensity(newIntensity);
            }
        } else {
            creature.setIntensity(newEmotion.getIntensity());
        }
    }

    static class DecayEmotionAsyncTask extends AsyncTask<Void, Void, Void> {
        WeakReference<EmotionEngineService> emotionEngineServiceWeakReference;

        DecayEmotionAsyncTask(EmotionEngineService instance) {
            this.emotionEngineServiceWeakReference = new WeakReference<>(instance);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG, "Decaying emotion...");

            EmotionEngineService emotionEngineService = emotionEngineServiceWeakReference.get();
            Application application = emotionEngineService.getApplication();
            EmotionRepository repository = new EmotionRepository(application);

            Creature creature = repository.getCreature();
            emotionEngineService.decayEmotionIntensity(creature);

            Log.d(TAG, "new decayed emotion = "+creature.getEmotion());
            repository.updateCreature(creature);

            return null;
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

            Creature creature = repository.getCreature();

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(emotionEngineService);

            List<Emotion> passiveEmotionList = new ArrayList<>();

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            int hour = calendar.get(Calendar.HOUR_OF_DAY);

            String dispositionTimeStartPrefKey = emotionEngineService.getString(R.string.disposition_time_start_pref);
            String dispositionTimeEndPrefKey = emotionEngineService.getString(R.string.disposition_time_end_pref);
            String lastDispositionResultPrefKey = emotionEngineService.getString(R.string.last_disposition_result_pref);

            String dispositionTimeStartString = sharedPreferences.getString(dispositionTimeStartPrefKey, "8");
            String dispositionTimeEndString = sharedPreferences.getString(dispositionTimeEndPrefKey, "20");

            int dispositionTimeStart = Integer.parseInt(dispositionTimeStartString);
            int dispositionTimeEnd = Integer.parseInt(dispositionTimeEndString);
            int lastDispositionResult = sharedPreferences.getInt(lastDispositionResultPrefKey, -1);

            int inputAction;
            if(hour >= dispositionTimeStart && hour < dispositionTimeEnd) {
                inputAction = AppraisalConstants.INPUT_TIME_DISPOSED;
            } else {
                inputAction = AppraisalConstants.INPUT_TIME_INDISPOSED;
            }

            if(lastDispositionResult != inputAction) {
                EmotionVariables emotionVariables = repository.getEmotionVariable(creature.getPersonality(), inputAction);
                if (emotionVariables != null) {
                    Emotion emotionFromTime = emotionEngineService.appraiseNewEmotion(emotionVariables);
                    if (emotionFromTime != null) {
                        Log.d(TAG, "New emotion from time = " + emotionFromTime.getName());
                        passiveEmotionList.add(emotionFromTime);
                    }
                }

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(lastDispositionResultPrefKey, inputAction);
                editor.apply();
            }

            String weatherConditionPrefKey = emotionEngineService.getString(R.string.weather_condition_pref);
            String lastWeatherConditionPrefKey = emotionEngineService.getString(R.string.last_weather_condition_pref);

            String weatherConditionsString = sharedPreferences.getString(weatherConditionPrefKey, String.valueOf(AppraisalConstants.INPUT_FORECAST_GOOD));
            int weatherConditions = Integer.valueOf(weatherConditionsString);
            int lastWeatherResult = sharedPreferences.getInt(lastWeatherConditionPrefKey, -1);

            if(lastWeatherResult != weatherConditions) {
                EmotionVariables emotionVariables = repository.getEmotionVariable(creature.getPersonality(), weatherConditions);
                if (emotionVariables != null) {
                    Emotion emotionFromWeather = emotionEngineService.appraiseNewEmotion(emotionVariables);
                    if (emotionFromWeather != null) {
                        Log.d(TAG, "New emotion from weather = " + emotionFromWeather.getName());
                        passiveEmotionList.add(emotionFromWeather);
                    }
                }

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(lastWeatherConditionPrefKey, weatherConditions);
                editor.apply();
            }

            String movingStatusPrefKey = emotionEngineService.getString(R.string.moving_status_pref);
            String lastMovingStatusPrefKey = emotionEngineService.getString(R.string.last_moving_status_pref);

            boolean isMoving = sharedPreferences.getBoolean(movingStatusPrefKey, false);
            boolean lastMovingStatus = sharedPreferences.getBoolean(lastMovingStatusPrefKey, false);

            if(lastMovingStatus != isMoving) {
                int isMovingInput = isMoving ? AppraisalConstants.INPUT_LOCATION_MOVING : AppraisalConstants.INPUT_LOCATION_IDLE;
                EmotionVariables emotionVariables = repository.getEmotionVariable(creature.getPersonality(), isMovingInput);
                if (emotionVariables != null) {
                    Emotion emotionFromLocation = emotionEngineService.appraiseNewEmotion(emotionVariables);
                    if (emotionFromLocation != null) {
                        Log.d(TAG, "New emotion from location = " + emotionFromLocation.getName());
                        passiveEmotionList.add(emotionFromLocation);
                    }
                }

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(lastMovingStatusPrefKey, isMoving);
                editor.apply();
            }

            Emotion moreIntensePassiveEmotion = emotionEngineService.getMoreIntenseEmotionFromList(passiveEmotionList);
            if(moreIntensePassiveEmotion != null) {

                Log.d(TAG, "New passive emotion = "+moreIntensePassiveEmotion.getName());
                emotionEngineService.updateCreatureWithNewEmotion(creature, moreIntensePassiveEmotion);

                Log.d(TAG, "[passive] emotion = "+creature.getEmotion());
                repository.updateCreature(creature);
            }
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
                    emotionEngineService.updateCreatureWithNewEmotion(creature, newEmotion);

                    Log.d(TAG, "[active] emotion = "+creature.getEmotion());
                    repository.updateCreature(creature);
                }
            }

            return null;
        }
    }

    private void decayEmotionIntensity(Creature creature) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String decayFactorString = preferences.getString(getString(R.string.decay_factor_pref), "0.5");
        double decayFactor = Double.valueOf(decayFactorString);

        double emotionIntensity = creature.getIntensity();
        int currEmotion = creature.getEmotion();
        int newEmotion = currEmotion;

        double newEmotionIntensity = emotionIntensity-decayFactor > 0 ? emotionIntensity-decayFactor : 0.0;
        if(newEmotionIntensity == 0.0) {
            switch (currEmotion) {
                case AppraisalConstants.EMOTION_NEUTRAL:
                    newEmotion = AppraisalConstants.EMOTION_BORED;
                    break;
                case AppraisalConstants.EMOTION_BORED:
                    // continua entediado
                    break;
                default:
                    newEmotion = AppraisalConstants.EMOTION_NEUTRAL;
                    newEmotionIntensity = 5.0;
                    break;
            }
        }

        Log.d(TAG, "Emotion = "+newEmotion);
        Log.d(TAG, "Emotion intensity = "+newEmotionIntensity);

        if(newEmotion != currEmotion && newEmotion == AppraisalConstants.EMOTION_BORED) {
            String message = getRandomBoredMessage(creature.getPersonality());

            createNotification(message);
        }

        creature.setEmotion(newEmotion);
        creature.setIntensity(newEmotionIntensity);
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

        return getMoreIntenseEmotionFromList(emotionList);
    }

    private Emotion getMoreIntenseEmotionFromList(List<Emotion> emotionList) {

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

    private String getRandomBoredMessage(int personality) {
        String[] optionsArray;
        if(personality == AppraisalConstants.PERSONALITY_EXTROVERT) {
            optionsArray = getResources().getStringArray(R.array.extrovert_bored_messages);
        } else {
            optionsArray = getResources().getStringArray(R.array.neurotic_bored_messages);
        }

        Random random = new Random();
        int index = random.nextInt(optionsArray.length);
        return optionsArray[index];
    }

    private void createNotification(String message) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 2, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(message)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        Notification notification = mBuilder.build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
