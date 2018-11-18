package br.com.ia369.bichinhovirtual.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "emotion_variables")
public class EmotionVariables {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private long id;

    @NonNull
    private int personality;

    @NonNull
    private int input;

    private Double unexpectedness;
    private Double senseOfReality;
    private Double proximity;
    private Double arousal;
    private Double desirability;
    private Double likelihood;
    private Double realization;
    private Double effort;
    private Double praiseworthiness;
    private Double expectationDeviation;

    @NonNull
    public long getId() {
        return id;
    }

    public void setId(@NonNull long id) {
        this.id = id;
    }

    @NonNull
    public int getPersonality() {
        return personality;
    }

    public void setPersonality(@NonNull int personality) {
        this.personality = personality;
    }

    @NonNull
    public int getInput() {
        return input;
    }

    public void setInput(@NonNull int input) {
        this.input = input;
    }

    public Double getUnexpectedness() {
        return unexpectedness;
    }

    public void setUnexpectedness(Double unexpectedness) {
        this.unexpectedness = unexpectedness;
    }

    public Double getSenseOfReality() {
        return senseOfReality;
    }

    public void setSenseOfReality(Double senseOfReality) {
        this.senseOfReality = senseOfReality;
    }

    public Double getProximity() {
        return proximity;
    }

    public void setProximity(Double proximity) {
        this.proximity = proximity;
    }

    public Double getArousal() {
        return arousal;
    }

    public void setArousal(Double arousal) {
        this.arousal = arousal;
    }

    public Double getDesirability() {
        return desirability;
    }

    public void setDesirability(Double desirability) {
        this.desirability = desirability;
    }

    public Double getLikelihood() {
        return likelihood;
    }

    public void setLikelihood(Double likelihood) {
        this.likelihood = likelihood;
    }

    public Double getRealization() {
        return realization;
    }

    public void setRealization(Double realization) {
        this.realization = realization;
    }

    public Double getEffort() {
        return effort;
    }

    public void setEffort(Double effort) {
        this.effort = effort;
    }

    public Double getPraiseworthiness() {
        return praiseworthiness;
    }

    public void setPraiseworthiness(Double praiseworthiness) {
        this.praiseworthiness = praiseworthiness;
    }

    public Double getExpectationDeviation() {
        return expectationDeviation;
    }

    public void setExpectationDeviation(Double expectationDeviation) {
        this.expectationDeviation = expectationDeviation;
    }
}
