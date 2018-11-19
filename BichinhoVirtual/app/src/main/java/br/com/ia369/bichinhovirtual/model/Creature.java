package br.com.ia369.bichinhovirtual.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "creature")
public class Creature {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private int emotion;
    private int personality;
    private Double intensity;
    private Double decayFactor;
    private int dispositionTimeStart;
    private int dispositionTimeEnd;
    private String weatherPreference;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getEmotion() {
        return emotion;
    }

    public void setEmotion(int emotion) {
        this.emotion = emotion;
    }

    public int getPersonality() {
        return personality;
    }

    public void setPersonality(int personality) {
        this.personality = personality;
    }

    public Double getIntensity() {
        return intensity;
    }

    public void setIntensity(Double intensity) {
        this.intensity = intensity;
    }

    public Double getDecayFactor() {
        return decayFactor;
    }

    public void setDecayFactor(Double decayFactor) {
        this.decayFactor = decayFactor;
    }

    public int getDispositionTimeStart() {
        return dispositionTimeStart;
    }

    public void setDispositionTimeStart(int dispositionTimeStart) {
        this.dispositionTimeStart = dispositionTimeStart;
    }

    public int getDispositionTimeEnd() {
        return dispositionTimeEnd;
    }

    public void setDispositionTimeEnd(int dispositionTimeEnd) {
        this.dispositionTimeEnd = dispositionTimeEnd;
    }

    public String getWeatherPreference() {
        return weatherPreference;
    }

    public void setWeatherPreference(String weatherPreference) {
        this.weatherPreference = weatherPreference;
    }
}
