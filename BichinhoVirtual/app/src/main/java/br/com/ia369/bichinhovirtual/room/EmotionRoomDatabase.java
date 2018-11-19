package br.com.ia369.bichinhovirtual.room;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import br.com.ia369.bichinhovirtual.R;
import br.com.ia369.bichinhovirtual.appraisal.AppraisalConstants;
import br.com.ia369.bichinhovirtual.model.Creature;
import br.com.ia369.bichinhovirtual.model.EmotionVariables;

@Database(entities = {EmotionVariables.class, Creature.class}, version = 1)
public abstract class EmotionRoomDatabase extends RoomDatabase {

    private static final String TAG = EmotionRoomDatabase.class.getSimpleName();

    abstract EmotionDao emotionDao();

    private static EmotionRoomDatabase INSTANCE;
    private static Resources resources;

    private static RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback(){

                @Override
                public void onOpen (@NonNull SupportSQLiteDatabase db){
                    super.onOpen(db);
                    new PopulateDbAsync(INSTANCE).execute();
                }
            };

    static EmotionRoomDatabase getDatabase(final Context context) {

        if (INSTANCE == null) {
            synchronized (EmotionRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            EmotionRoomDatabase.class, "emotion_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }

                resources = context.getResources();
            }
        }
        return INSTANCE;
    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final EmotionDao mDao;

        PopulateDbAsync(EmotionRoomDatabase db) {
            mDao = db.emotionDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            Creature creature = new Creature();
            creature.setPersonality(AppraisalConstants.PERSONALITY_EXTROVERT);
            creature.setEmotion(AppraisalConstants.EMOTION_NEUTRAL);
            creature.setIntensity(5.0);
//            creature.setDecayFactor(0.5);
//            creature.setDispositionTimeStart(8);
//            creature.setDispositionTimeEnd(18);

            mDao.insertCreature(creature);

            try {
                InputStream inputStream = resources.openRawResource(R.raw.emotions_variables);
                BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder total = new StringBuilder();
                for (String line; (line = r.readLine()) != null; ) {
                    total.append(line).append('\n');
                }

                JSONArray emotionVariablesJsonArray = new JSONArray(total.toString());
                for(int i = 0; i < emotionVariablesJsonArray.length(); i++) {
                    JSONObject emotionVariableJsonObject = emotionVariablesJsonArray.getJSONObject(i);
                    EmotionVariables emotionVariables = new EmotionVariables();
                    emotionVariables.setPersonality(emotionVariableJsonObject.getInt("personality"));
                    emotionVariables.setInput(emotionVariableJsonObject.getInt("input"));
                    emotionVariables.setUnexpectedness(emotionVariableJsonObject.getDouble("unexpectedness"));
                    emotionVariables.setSenseOfReality(emotionVariableJsonObject.getDouble("senseOfReality"));
                    emotionVariables.setProximity(emotionVariableJsonObject.getDouble("proximity"));
                    emotionVariables.setArousal(emotionVariableJsonObject.getDouble("arousal"));

                    if(emotionVariableJsonObject.has("desirability")) {
                        emotionVariables.setDesirability(emotionVariableJsonObject.getDouble("desirability"));
                    }
                    if(emotionVariableJsonObject.has("effort")) {
                        emotionVariables.setEffort(emotionVariableJsonObject.getDouble("effort"));
                    }
                    if(emotionVariableJsonObject.has("expectationDeviation")) {
                        emotionVariables.setExpectationDeviation(emotionVariableJsonObject.getDouble("expectationDeviation"));
                    }
                    if(emotionVariableJsonObject.has("realization")) {
                        emotionVariables.setRealization(emotionVariableJsonObject.getDouble("realization"));
                    }
                    if(emotionVariableJsonObject.has("praiseworthiness")) {
                        emotionVariables.setPraiseworthiness(emotionVariableJsonObject.getDouble("praiseworthiness"));
                    }
                    if(emotionVariableJsonObject.has("likelihood")) {
                        emotionVariables.setLikelihood(emotionVariableJsonObject.getDouble("likelihood"));
                    }

                    mDao.insert(emotionVariables);
                }
            } catch (IOException e) {
                Log.e(TAG, "IOEXception while reading json file", e);
            } catch (JSONException e) {
                Log.e(TAG, "JsonException while parsing json", e);
            }
            return null;
        }
    }
}
