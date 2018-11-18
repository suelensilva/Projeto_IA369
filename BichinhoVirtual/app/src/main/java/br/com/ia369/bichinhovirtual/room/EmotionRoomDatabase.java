package br.com.ia369.bichinhovirtual.room;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import br.com.ia369.bichinhovirtual.appraisal.AppraisalConstants;
import br.com.ia369.bichinhovirtual.model.EmotionVariables;

@Database(entities = {EmotionVariables.class}, version = 1)
abstract class EmotionRoomDatabase extends RoomDatabase {

    private static final String TAG = EmotionRoomDatabase.class.getSimpleName();

    abstract EmotionDao emotionDao();

    private static EmotionRoomDatabase INSTANCE;

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

            EmotionVariables emotionVariablesExt1 = new EmotionVariables();
            emotionVariablesExt1.setPersonality(AppraisalConstants.PERSONALITY_EXTROVERT);
            emotionVariablesExt1.setInput(AppraisalConstants.INPUT_FACE_POSITIVE);
            emotionVariablesExt1.setUnexpectedness(0.3);
            emotionVariablesExt1.setSenseOfReality(0.3);
            emotionVariablesExt1.setProximity(0.3);
            emotionVariablesExt1.setArousal(2.0);
            emotionVariablesExt1.setDesirability(1.0);

            mDao.insert(emotionVariablesExt1);

            EmotionVariables emotionVariablesExt4 = new EmotionVariables();
            emotionVariablesExt4.setPersonality(AppraisalConstants.PERSONALITY_EXTROVERT);
            emotionVariablesExt4.setInput(AppraisalConstants.INPUT_TEXT_JOY);
            emotionVariablesExt4.setUnexpectedness(0.3);
            emotionVariablesExt4.setSenseOfReality(0.3);
            emotionVariablesExt4.setProximity(0.3);
            emotionVariablesExt4.setArousal(1.0);
            emotionVariablesExt4.setDesirability(1.0);

            mDao.insert(emotionVariablesExt4);

            EmotionVariables emotionVariablesNeur4 = new EmotionVariables();
            emotionVariablesNeur4.setPersonality(AppraisalConstants.PERSONALITY_NEUROTIC);
            emotionVariablesNeur4.setInput(AppraisalConstants.INPUT_TEXT_JOY);
            emotionVariablesNeur4.setUnexpectedness(1.0);
            emotionVariablesNeur4.setSenseOfReality(0.3);
            emotionVariablesNeur4.setProximity(0.3);
            emotionVariablesNeur4.setArousal(0.5);
            emotionVariablesNeur4.setDesirability(1.0);

            mDao.insert(emotionVariablesNeur4);

            return null;
        }
    }
}
