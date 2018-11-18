package br.com.ia369.bichinhovirtual.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import br.com.ia369.bichinhovirtual.model.EmotionVariables;

@Dao
public interface EmotionDao {

    @Insert
    void insert(EmotionVariables emotionVariables);

    @Query("SELECT * from emotion_variables WHERE personality = :personality AND input = :input ")
    EmotionVariables getEmotionVariables(int personality, int input);
}
