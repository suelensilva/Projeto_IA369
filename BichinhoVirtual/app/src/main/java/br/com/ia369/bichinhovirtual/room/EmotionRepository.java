package br.com.ia369.bichinhovirtual.room;

import android.app.Application;

import br.com.ia369.bichinhovirtual.model.EmotionVariables;

public class EmotionRepository {

    private EmotionDao mEmotionDao;
    //private EmotionVariables mEmotionVariable;
    //private LiveData<List<Word>> mAllWords;

    public EmotionRepository(Application application) {
        EmotionRoomDatabase db = EmotionRoomDatabase.getDatabase(application);
        mEmotionDao = db.emotionDao();
        //mAllWords = mEmotionDao.getAllWords();
    }

    public EmotionVariables getEmotionVariable(int personality, int input) {
        return mEmotionDao.getEmotionVariables(personality, input);
    }
}
