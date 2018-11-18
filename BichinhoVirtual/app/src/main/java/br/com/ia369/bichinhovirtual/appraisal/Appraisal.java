package br.com.ia369.bichinhovirtual.appraisal;

import com.occ.common.Evaluator;
import com.occ.common.VariableType;
import com.occ.entities.Emotion;
import com.occ.entities.Variable;
import com.occ.models.infra.Model;
import com.occ.rules.infra.Rule;
import com.occ.rules.infra.RulesBuilder;

public class Appraisal {
    protected Emotion emotion;
    protected Model model;
    protected static final Double THRESHOLD = 0.3;
    protected static final Double GLOBAL_VALUES = 0.5;
    protected static final Double UNEXPECTEDNESS_FEAR = 1.0;
    protected static final Double AROUSAL_FEAR = 2.0;

    private static final Double POSITIVE_VALUE = 0.3;
    private static final Double NEGATIVE_VALUE = -1.0;//-0.3;

    private Variable desirability;
    private Variable likelihood;

    public Appraisal() {
        this.model = new Model();
        this.model
                .add(new Variable(VariableType.SENSE_OF_REALITY, GLOBAL_VALUES.toString()))
                .add(new Variable(VariableType.PROXIMITY, GLOBAL_VALUES.toString()))
                .add(new Variable(VariableType.UNEXPECTEDNESS, UNEXPECTEDNESS_FEAR.toString()/*GLOBAL_VALUES.toString()*/))
                .add(new Variable(VariableType.AROUSAL, AROUSAL_FEAR.toString()/*GLOBAL_VALUES.toString()*/));

        Rule rule = RulesBuilder.buildFearRule();
        this.emotion = new Emotion("Fear", rule, THRESHOLD);
    }

    public double evaluateFear() throws Exception {
        this.desirability = new Variable(VariableType.DESIRABILITY, NEGATIVE_VALUE.toString());
        this.likelihood = new Variable(VariableType.LIKELIHOOD, POSITIVE_VALUE.toString());
        this.model
                .add(this.desirability)
                .add(this.likelihood);

        boolean isFear = Evaluator.evaluate(this.emotion, this.model);
        if(isFear) {
            return this.emotion.getIntensity();
        } else {
            return 0.0f;
        }
    }
}
