package br.com.ia369.bichinhovirtual.room;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import br.com.ia369.bichinhovirtual.model.Creature;
import br.com.ia369.bichinhovirtual.model.EmotionVariables;

@Dao
public interface EmotionDao {

    @Insert
    void insert(EmotionVariables emotionVariables);

    @Insert
    void insertCreature(Creature creature);

    @Query("SELECT * from emotion_variables WHERE personality = :personality AND input = :input ")
    EmotionVariables getEmotionVariables(int personality, int input);

    @Query("SELECT * from creature")
    LiveData<Creature> getLiveDataCreature();

    @Query("SELECT * from creature")
    Creature getCreature();

    @Update
    void updateCreature(Creature creature);
}
