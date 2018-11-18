package br.com.ia369.bichinhovirtual.appraisal;

import com.occ.common.Evaluator;
import com.occ.common.VariableType;
import com.occ.entities.Emotion;
import com.occ.entities.Variable;
import com.occ.models.infra.Model;
import com.occ.rules.infra.Rule;
import com.occ.rules.infra.RulesBuilder;

import br.com.ia369.bichinhovirtual.model.EmotionVariables;

public class Appraisal {
    protected static final Double THRESHOLD = 0.3;
    protected static final Double GLOBAL_VALUES = 0.5;
    protected static final Double UNEXPECTEDNESS_FEAR = 1.0;
    protected static final Double AROUSAL_FEAR = 2.0;

    private static final Double POSITIVE_VALUE = 0.3;
    private static final Double NEGATIVE_VALUE = -1.0;//-0.3;

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
}
