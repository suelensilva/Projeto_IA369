package br.com.ia369.bichinhovirtual.appraisal;

import android.support.annotation.NonNull;
import android.util.Log;

import com.occ.common.Evaluator;
import com.occ.common.VariableType;
import com.occ.entities.Emotion;
import com.occ.entities.Variable;
import com.occ.models.infra.Model;
import com.occ.rules.infra.Rule;
import com.occ.rules.infra.RulesBuilder;

import br.com.ia369.bichinhovirtual.model.EmotionVariables;

public class Appraisal {

    private static final Double THRESHOLD = 0.3;

    static Emotion evaluateFear(EmotionVariables emotionVariables) {

        Model model = new Model();
        model
                .add(new Variable(VariableType.SENSE_OF_REALITY, emotionVariables.getSenseOfReality().toString()))
                .add(new Variable(VariableType.PROXIMITY, emotionVariables.getProximity().toString()))
                .add(new Variable(VariableType.UNEXPECTEDNESS, emotionVariables.getUnexpectedness().toString()))
                .add(new Variable(VariableType.AROUSAL, emotionVariables.getArousal().toString()));

        Rule rule = RulesBuilder.buildFearRule();
        Emotion emotion = new Emotion("Fear", rule, THRESHOLD);

        if(emotionVariables.getDesirability() != null) {
            Variable desirability = new Variable(VariableType.DESIRABILITY, emotionVariables.getDesirability().toString());
            model.add(desirability);
        }

        if(emotionVariables.getLikelihood() != null) {
            Variable likelihood = new Variable(VariableType.LIKELIHOOD, emotionVariables.getLikelihood().toString());
            model.add(likelihood);
        }

        try {
            boolean isFear = Evaluator.evaluate(emotion, model);
            if(isFear) {
                return emotion;
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    static Emotion evaluateJoy(EmotionVariables emotionVariables) {

        Model model = new Model();
        model
                .add(new Variable(VariableType.SENSE_OF_REALITY, emotionVariables.getSenseOfReality().toString()))
                .add(new Variable(VariableType.PROXIMITY, emotionVariables.getProximity().toString()))
                .add(new Variable(VariableType.UNEXPECTEDNESS, emotionVariables.getUnexpectedness().toString()))
                .add(new Variable(VariableType.AROUSAL, emotionVariables.getArousal().toString()));

        Rule rule = RulesBuilder.buildJoyRule();
        Emotion emotion = new Emotion("Joy", rule, THRESHOLD);

        if(emotionVariables.getDesirability() != null) {
            Variable desirability = new Variable(VariableType.DESIRABILITY, emotionVariables.getDesirability().toString());
            model.add(desirability);
        }

        try {
            boolean isJoy = Evaluator.evaluate(emotion, model);
            if(isJoy) {
                return emotion;
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    static Emotion evaluateSadness(EmotionVariables emotionVariables) {

        Model model = new Model();
        model
                .add(new Variable(VariableType.SENSE_OF_REALITY, emotionVariables.getSenseOfReality().toString()))
                .add(new Variable(VariableType.PROXIMITY, emotionVariables.getProximity().toString()))
                .add(new Variable(VariableType.UNEXPECTEDNESS, emotionVariables.getUnexpectedness().toString()))
                .add(new Variable(VariableType.AROUSAL, emotionVariables.getArousal().toString()));

        Rule rule = RulesBuilder.buildDisappointmentRule();
        Emotion emotion = new Emotion("Sadness", rule, THRESHOLD);

        if(emotionVariables.getDesirability() != null) {
            Variable desirability = new Variable(VariableType.DESIRABILITY, emotionVariables.getDesirability().toString());
            model.add(desirability);
        }

        if(emotionVariables.getLikelihood() != null) {
            Variable likelihood = new Variable(VariableType.LIKELIHOOD, emotionVariables.getLikelihood().toString());
            model.add(likelihood);
        }

        if(emotionVariables.getRealization() != null) {
            Variable realization = new Variable(VariableType.REALIZATION, emotionVariables.getRealization().toString());
            model.add(realization);
        }

        if(emotionVariables.getEffort() != null) {
            Variable effort = new Variable(VariableType.EFFORT, emotionVariables.getEffort().toString());
            model.add(effort);
        }

        try {
            boolean isSadness = Evaluator.evaluate(emotion, model);
            if(isSadness) {
                return emotion;
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    static Emotion evaluateReproach(EmotionVariables emotionVariables) {

        Model model = new Model();
        model
                .add(new Variable(VariableType.SENSE_OF_REALITY, emotionVariables.getSenseOfReality().toString()))
                .add(new Variable(VariableType.PROXIMITY, emotionVariables.getProximity().toString()))
                .add(new Variable(VariableType.UNEXPECTEDNESS, emotionVariables.getUnexpectedness().toString()))
                .add(new Variable(VariableType.AROUSAL, emotionVariables.getArousal().toString()));

        Rule rule = RulesBuilder.buildReproachRule();
        Emotion emotion = new Emotion("Disgust", rule, THRESHOLD);

        if(emotionVariables.getPraiseworthiness() != null) {
            Variable praiseworthiness = new Variable(VariableType.PRAISEWORTHINESS, emotionVariables.getPraiseworthiness().toString());
            model.add(praiseworthiness);
        }

        if(emotionVariables.getExpectationDeviation() != null) {
            Variable expectationDeviation = new Variable(VariableType.EXPECTATION_DEVIATION, emotionVariables.getExpectationDeviation().toString());
            model.add(expectationDeviation);
        }

        try {
            boolean isDisgust = Evaluator.evaluate(emotion, model);
            if(isDisgust) {
                return emotion;
            }
        } catch (Exception e) {
            Log.e("Appraisal", "Exception while evaluating reproach", e);
        }

        return null;
    }

    static Emotion evaluateAnger(EmotionVariables emotionVariables) {

        Model model = new Model();
        model
                .add(new Variable(VariableType.SENSE_OF_REALITY, emotionVariables.getSenseOfReality().toString()))
                .add(new Variable(VariableType.PROXIMITY, emotionVariables.getProximity().toString()))
                .add(new Variable(VariableType.UNEXPECTEDNESS, emotionVariables.getUnexpectedness().toString()))
                .add(new Variable(VariableType.AROUSAL, emotionVariables.getArousal().toString()));

        Rule rule = RulesBuilder.buildAngerRule();
        Emotion emotion = new Emotion("Anger", rule, THRESHOLD);

        if(emotionVariables.getDesirability() != null) {
            Variable desirability = new Variable(VariableType.DESIRABILITY, emotionVariables.getDesirability().toString());
            model.add(desirability);
        }

        if(emotionVariables.getPraiseworthiness() != null) {
            Variable praiseworthiness = new Variable(VariableType.PRAISEWORTHINESS, emotionVariables.getPraiseworthiness().toString());
            model.add(praiseworthiness);
        }

        if(emotionVariables.getExpectationDeviation() != null) {
            Variable expectationDeviation = new Variable(VariableType.EXPECTATION_DEVIATION, emotionVariables.getExpectationDeviation().toString());
            model.add(expectationDeviation);
        }

        try {
            boolean isAnger = Evaluator.evaluate(emotion, model);
            if(isAnger) {
                return emotion;
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    static Emotion evaluateSatisfaction(EmotionVariables emotionVariables) {

        Model model = new Model();
        model
                .add(new Variable(VariableType.SENSE_OF_REALITY, emotionVariables.getSenseOfReality().toString()))
                .add(new Variable(VariableType.PROXIMITY, emotionVariables.getProximity().toString()))
                .add(new Variable(VariableType.UNEXPECTEDNESS, emotionVariables.getUnexpectedness().toString()))
                .add(new Variable(VariableType.AROUSAL, emotionVariables.getArousal().toString()));

        Rule rule = RulesBuilder.buildSatisfactionRule();
        Emotion emotion = new Emotion("Satisfaction", rule, THRESHOLD);

        if(emotionVariables.getDesirability() != null) {
            Variable desirability = new Variable(VariableType.DESIRABILITY, emotionVariables.getDesirability().toString());
            model.add(desirability);
        }

        if(emotionVariables.getLikelihood() != null) {
            Variable likelihood = new Variable(VariableType.LIKELIHOOD, emotionVariables.getLikelihood().toString());
            model.add(likelihood);
        }

        if(emotionVariables.getRealization() != null) {
            Variable realization = new Variable(VariableType.REALIZATION, emotionVariables.getRealization().toString());
            model.add(realization);
        }

        if(emotionVariables.getEffort() != null) {
            Variable effort = new Variable(VariableType.EFFORT, emotionVariables.getEffort().toString());
            model.add(effort);
        }

        try {
            boolean isSatisfaction = Evaluator.evaluate(emotion, model);
            if(isSatisfaction) {
                return emotion;
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    static Emotion evaluateDistress(EmotionVariables emotionVariables) {

        Model model = new Model();
        model
                .add(new Variable(VariableType.SENSE_OF_REALITY, emotionVariables.getSenseOfReality().toString()))
                .add(new Variable(VariableType.PROXIMITY, emotionVariables.getProximity().toString()))
                .add(new Variable(VariableType.UNEXPECTEDNESS, emotionVariables.getUnexpectedness().toString()))
                .add(new Variable(VariableType.AROUSAL, emotionVariables.getArousal().toString()));

        Rule rule = RulesBuilder.buildDistressRule();
        Emotion emotion = new Emotion("Distress", rule, THRESHOLD);

        if(emotionVariables.getDesirability() != null) {
            Variable desirability = new Variable(VariableType.DESIRABILITY, emotionVariables.getDesirability().toString());
            model.add(desirability);
        }

        try {
            boolean isDistress = Evaluator.evaluate(emotion, model);
            if(isDistress) {
                return emotion;
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    static Emotion evaluateGratitude(EmotionVariables emotionVariables) {

        Model model = new Model();
        model
                .add(new Variable(VariableType.SENSE_OF_REALITY, emotionVariables.getSenseOfReality().toString()))
                .add(new Variable(VariableType.PROXIMITY, emotionVariables.getProximity().toString()))
                .add(new Variable(VariableType.UNEXPECTEDNESS, emotionVariables.getUnexpectedness().toString()))
                .add(new Variable(VariableType.AROUSAL, emotionVariables.getArousal().toString()));

        Rule rule = RulesBuilder.buildGratitudeRule();
        Emotion emotion = new Emotion("Gratitude", rule, THRESHOLD);

        if(emotionVariables.getDesirability() != null) {
            Variable desirability = new Variable(VariableType.DESIRABILITY, emotionVariables.getDesirability().toString());
            model.add(desirability);
        }

        if(emotionVariables.getPraiseworthiness() != null) {
            Variable praiseworthiness = new Variable(VariableType.PRAISEWORTHINESS, emotionVariables.getPraiseworthiness().toString());
            model.add(praiseworthiness);
        }

        if(emotionVariables.getExpectationDeviation() != null) {
            Variable expectationDeviation = new Variable(VariableType.EXPECTATION_DEVIATION, emotionVariables.getExpectationDeviation().toString());
            model.add(expectationDeviation);
        }

        try {
            boolean isGratitude = Evaluator.evaluate(emotion, model);
            if(isGratitude) {
                return emotion;
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    public static int getEmotionIdByName(@NonNull String name) {
        switch (name) {
            case "Fear":
                return AppraisalConstants.EMOTION_FEAR;
            case "Joy":
                return AppraisalConstants.EMOTION_JOY;
            case "Distress":
                return AppraisalConstants.EMOTION_DISTRESS;
            case "Disgust":
                return AppraisalConstants.EMOTION_DISGUST;
            case "Anger":
                return AppraisalConstants.EMOTION_ANGER;
            case "Bored":
                return AppraisalConstants.EMOTION_BORED;
            case "Neutral":
                return AppraisalConstants.EMOTION_NEUTRAL;
            case "Sadness":
                return AppraisalConstants.EMOTION_SADNESS;
            case "Gratitude":
                return AppraisalConstants.EMOTION_GRATITUDE;
            case "Satisfaction":
                return AppraisalConstants.EMOTION_SATISFACTION;
        }
        return -1;
    }

    public static String getEmotionNameById(int id) {
        switch (id) {
            case AppraisalConstants.EMOTION_FEAR:
                return "Fear";
            case AppraisalConstants.EMOTION_JOY:
                return "Joy";
            case AppraisalConstants.EMOTION_DISTRESS:
                return "Distress";
            case AppraisalConstants.EMOTION_DISGUST:
                return "Disgust";
            case AppraisalConstants.EMOTION_ANGER:
                return "Anger";
            case AppraisalConstants.EMOTION_BORED:
                return "Bored";
            case AppraisalConstants.EMOTION_NEUTRAL:
                return "Neutral";
            case AppraisalConstants.EMOTION_SADNESS:
                return "Sadness";
            case AppraisalConstants.EMOTION_GRATITUDE:
                return "Gratitude";
            case AppraisalConstants.EMOTION_SATISFACTION:
                return "Satisfaction";
        }
        return null;
    }
}
