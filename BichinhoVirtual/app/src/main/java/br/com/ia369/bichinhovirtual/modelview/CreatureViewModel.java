package br.com.ia369.bichinhovirtual.modelview;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import br.com.ia369.bichinhovirtual.model.Creature;
import br.com.ia369.bichinhovirtual.room.EmotionRepository;

public class CreatureViewModel extends AndroidViewModel {

    private EmotionRepository mEmotionRepository;
    private LiveData<Creature> mCreature;

    public CreatureViewModel(@NonNull Application application) {
        super(application);

        mEmotionRepository = new EmotionRepository(application);
        mCreature = mEmotionRepository.getLiveDataCreature();
    }

    public LiveData<Creature> getCreature() {
        return mCreature;
    }
}
