package br.com.ia369.bichinhovirtual;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import br.com.ia369.bichinhovirtual.appraisal.AppraisalConstants;
import br.com.ia369.bichinhovirtual.appraisal.EmotionEngineService;

public class CreatureView extends android.support.v7.widget.AppCompatImageView {

    int mCurrEmotion = AppraisalConstants.EMOTION_NEUTRAL;
    int mCurrPersonality = AppraisalConstants.PERSONALITY_EXTROVERT;

    Animation mFloatingAnim;
    
    public CreatureView(Context context) {
        super(context);
        setup();
    }

    public CreatureView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public CreatureView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    private void startFloatingAnimation() {
        mFloatingAnim = AnimationUtils.loadAnimation(getContext(), R.anim.floating_anim);
        setAnimation(mFloatingAnim);
    }
    
    public void updateCreature(int personality, int emotion) {
        mCurrPersonality = personality;
        mCurrEmotion = emotion;
        stopAnimation();
        init();
    }

    private void setup() {
        startFloatingAnimation();
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(mCurrEmotion != AppraisalConstants.EMOTION_BORED &&
                        mCurrEmotion != AppraisalConstants.EMOTION_NEUTRAL) {
                    Intent intent = new Intent(getContext(), EmotionEngineService.class);
                    intent.setAction(EmotionEngineService.RESET_TO_NEUTRAL_REQUEST);
                    getContext().startService(intent);
                    return true;
                }
                return false;
            }
        });
    }

    private void init() {

        if(mCurrPersonality == AppraisalConstants.PERSONALITY_EXTROVERT) {

            switch (mCurrEmotion) {
                case AppraisalConstants.EMOTION_FEAR:
                    setImageResource(R.drawable.extrov_medo);
                    break;
                case AppraisalConstants.EMOTION_JOY:
                    setImageResource(R.drawable.extrov_felicidade);
                    break;
                case AppraisalConstants.EMOTION_SADNESS:
                    setImageResource(R.drawable.extrov_tristeza);
                    break;
                case AppraisalConstants.EMOTION_DISGUST:
                    setImageResource(R.drawable.extrov_nojo);
                    break;
                case AppraisalConstants.EMOTION_ANGER:
                    setImageResource(R.drawable.extrov_raiva);
                    break;
                case AppraisalConstants.EMOTION_SATISFACTION:
                    setImageResource(R.drawable.extrov_satisfacao);
                    break;
                case AppraisalConstants.EMOTION_DISTRESS:
                    setImageResource(R.drawable.extrov_tristeza);
                    break;
                case AppraisalConstants.EMOTION_GRATITUDE:
                    setImageResource(R.drawable.extrov_gratidao);
                    break;
                case AppraisalConstants.EMOTION_NEUTRAL:
                    setImageResource(R.drawable.extrov_neutral_anim);
                    initAnimation();
                    break;
                case AppraisalConstants.EMOTION_BORED:
                    setImageResource(R.drawable.extrov_bored_anim);
                    initAnimation();
                    break;
            }
        } else if(mCurrPersonality == AppraisalConstants.PERSONALITY_NEUROTIC){

            switch (mCurrEmotion) {
                case AppraisalConstants.EMOTION_FEAR:
                    setImageResource(R.drawable.neuro_medo);
                    break;
                case AppraisalConstants.EMOTION_JOY:
                    setImageResource(R.drawable.neuro_alegria);
                    break;
                case AppraisalConstants.EMOTION_SADNESS:
                    setImageResource(R.drawable.neuro_tristeza);
                    break;
                case AppraisalConstants.EMOTION_DISGUST:
                    setImageResource(R.drawable.neuro_nojo);
                    break;
                case AppraisalConstants.EMOTION_ANGER:
                    setImageResource(R.drawable.neuro_raiva);
                    break;
                case AppraisalConstants.EMOTION_SATISFACTION:
                    setImageResource(R.drawable.neuro_satisfacao);
                    break;
                case AppraisalConstants.EMOTION_DISTRESS:
                    setImageResource(R.drawable.neuro_tristeza);
                    break;
                case AppraisalConstants.EMOTION_GRATITUDE:
                    setImageResource(R.drawable.neuro_gratidao);
                    break;
                case AppraisalConstants.EMOTION_NEUTRAL:
                    setImageResource(R.drawable.neuro_neutral_anim);
                    initAnimation();
                    break;
                case AppraisalConstants.EMOTION_BORED:
                    setImageResource(R.drawable.neuro_bored_anim);
                    initAnimation();
                    break;
            }
        }
    }

    private void initAnimation() {
        final AnimationDrawable creatureAnimation = (AnimationDrawable) getDrawable();
        creatureAnimation.start();
    }

    private void stopAnimation() {
        if(getDrawable() instanceof AnimationDrawable) {
            ((AnimationDrawable) getDrawable()).stop();
        }
    }
}
