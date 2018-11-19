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

import br.com.ia369.bichinhovirtual.MainActivity;
import br.com.ia369.bichinhovirtual.R;
import br.com.ia369.bichinhovirtual.model.Creature;
import br.com.ia369.bichinhovirtual.model.EmotionVariables;
import br.com.ia369.bichinhovirtual.room.EmotionRepository;

public class EmotionEngineService extends Service {

    private static final String TAG = EmotionEngineService.class.getSimpleName();

    private static final String INTERVAL_REQUEST = "interval_request";

    public static final int REQUEST_CODE = 42;
    public static final int INTERVAL_TIME = 10 * 1000; // TODO definir intervalo de tempo adequado
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

        long now = System.currentTimeMillis();
        long triggerAt = now + INTERVAL_TIME;

        AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(triggerAt, null);

        if(alarmManager != null) {
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
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

            Emotion emotionFromTime;
            Emotion emotionFromWeather;

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            int hour = calendar.get(Calendar.HOUR_OF_DAY);

            if(hour >= creature.getDispositionTimeStart() && hour < creature.getDispositionTimeEnd()) {
                int inputAction = AppraisalConstants.INPUT_TIME_DISPOSED;

                int lastDispositionResult = sharedPreferences.getInt("last_disposition_result", -1);

                if(lastDispositionResult != inputAction) {
                    EmotionVariables emotionVariables = repository.getEmotionVariable(creature.getPersonality(), inputAction);
                    if (emotionVariables != null) {
                        emotionFromTime = emotionEngineService.appraiseNewEmotion(emotionVariables);
                        if (emotionFromTime != null) {
                            Log.d(TAG, "New emotion from time = " + emotionFromTime.getName());
                        }
                    }

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("last_disposition_result", inputAction);
                    editor.apply();
                }
            }


            int weatherConditions = sharedPreferences.getInt("weather_condition_prefs", AppraisalConstants.INPUT_FORECAST_GOOD);

            if(weatherConditions > 0) {
                int lastWeatherResult = sharedPreferences.getInt("last_weather_result", -1);

                if(lastWeatherResult != weatherConditions) {
                    EmotionVariables emotionVariables = repository.getEmotionVariable(creature.getPersonality(), weatherConditions);
                    if (emotionVariables != null) {
                        emotionFromWeather = emotionEngineService.appraiseNewEmotion(emotionVariables);
                        if (emotionFromWeather != null) {
                            Log.d(TAG, "New emotion from weather = " + emotionFromWeather.getName());
                        }
                    }

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("last_weather_result", weatherConditions);
                    editor.apply();
                }
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
        int currEmotion = creature.getEmotion();
        int newEmotion = currEmotion;

        double newEmotionIntensity = emotionIntensity-decayFactor > 0 ? emotionIntensity-decayFactor : 0.0;
        if(newEmotionIntensity == 0.0) {
            switch (currEmotion) {
                case AppraisalConstants.EMOTION_NEUTRAL:
                    newEmotion = AppraisalConstants.EMOTION_BORED;
                    break;
                case AppraisalConstants.EMOTION_BORED:
                    // continua entendiado
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
            String message;
            if(creature.getPersonality() == AppraisalConstants.PERSONALITY_EXTROVERT) {
                message = "Como está meu humano favorito?";
            } else {
                message = "Oi??? Esqueceu que eu existo?";
            }
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
