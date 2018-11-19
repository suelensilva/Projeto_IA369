package br.com.ia369.bichinhovirtual.room;

import android.app.Application;
import android.arch.lifecycle.LiveData;

import br.com.ia369.bichinhovirtual.model.Creature;
import br.com.ia369.bichinhovirtual.model.EmotionVariables;

public class EmotionRepository {

    private EmotionDao mEmotionDao;

    public EmotionRepository(Application application) {
        EmotionRoomDatabase db = EmotionRoomDatabase.getDatabase(application);
        mEmotionDao = db.emotionDao();
    }

    public EmotionVariables getEmotionVariable(int personality, int input) {
        return mEmotionDao.getEmotionVariables(personality, input);
    }

    public LiveData<Creature> getLiveDataCreature() {
        return mEmotionDao.getLiveDataCreature();
    }

    public Creature getCreature() {
        return mEmotionDao.getCreature();
    }

    public void updateCreature(Creature creature) {
        mEmotionDao.updateCreature(creature);
    }
}
