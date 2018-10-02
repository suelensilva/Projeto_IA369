package br.com.ia369.bichinhovirtual;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.ia369.bichinhovirtual.model.TranslationResponse;
import br.com.ia369.bichinhovirtual.retrofit.IbmNluService;
import br.com.ia369.bichinhovirtual.retrofit.ServiceGenerator;
import br.com.ia369.bichinhovirtual.retrofit.TranslateService;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements RecognitionListener {

    private TextView mTextView;
    private TextView mSentimentReportTextView;
    private TextView mEmotionReportTextView;
    private SpeechRecognizer speechRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = findViewById(R.id.sample_text);
        mSentimentReportTextView = findViewById(R.id.sentiment_report_text);
        mEmotionReportTextView = findViewById(R.id.emotion_report_text);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(this);
    }

    public void startSpeechRecognizer(View view) {

        mSentimentReportTextView.setVisibility(View.GONE);
        mEmotionReportTextView.setVisibility(View.GONE);

        mTextView.setText("Ouvindo...");

        Intent intent = new Intent
                (RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        speechRecognizer.startListening(intent);
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float v) {

    }

    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int i) {

    }

    @Override
    public void onResults(Bundle bundle) {
        List<String> results = bundle.getStringArrayList
                    ("results_recognition");
        if(results != null) {
            String mAnswer = results.get(0);
            mTextView.setText(mAnswer);
            translateToEnglish(mAnswer);
        }

        speechRecognizer.stopListening();
    }

    @Override
    public void onPartialResults(Bundle bundle) {
    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }


    private void translateToEnglish(String portugueseText) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://translate.yandex.net/api/v1.5/tr.json/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TranslateService service = retrofit.create(TranslateService.class);
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("key", "trnsl.1.1.20180930T235915Z.de076450d8e5aaf8.eb3477709ac41d3e9d7cac25568dff613f1b6270");
        queryMap.put("lang", "pt-en");
        queryMap.put("text", portugueseText);

        Call<TranslationResponse> responseCallback = service.translate(queryMap);
        responseCallback.enqueue(new Callback<TranslationResponse>() {
            @Override
            public void onResponse(Call<TranslationResponse> call, Response<TranslationResponse> response) {
                TranslationResponse translation = response.body();
                if(translation != null) {
                    String translatedText = translation.text[0];
                    classifyEmotion(translatedText);
                }
            }

            @Override
            public void onFailure(Call<TranslationResponse> call, Throwable t) {

            }
        });
    }

    private void classifyEmotion(String text) {
        IbmNluService ibmNluService = ServiceGenerator.createService(
                IbmNluService.class,
                "8eab499b-fe59-4e10-ac17-41b6ae981e53", "lUVcZ14sGgfb");

        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("version", "2018-03-19");

        JSONObject bodyJsonObject = new JSONObject();
        JSONObject featuresJsonObject = new JSONObject();
        JSONObject sentimentJsonObject = new JSONObject();
        JSONObject emotionJsonObject = new JSONObject();

        try {
            bodyJsonObject.put("language", "en");
            bodyJsonObject.put("text", text);

            featuresJsonObject.put("sentiment", sentimentJsonObject);
            featuresJsonObject.put("emotion", emotionJsonObject);

            bodyJsonObject.put("features", featuresJsonObject);

            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    bodyJsonObject.toString());

            Call<ResponseBody> responseCallback = ibmNluService.analyse(queryMap, requestBody);
            responseCallback.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    ResponseBody responseBody = response.body();
                    if(responseBody != null) {
                        parseNluResponse(responseBody);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.d(MainActivity.class.getSimpleName(), "Failure");
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void parseNluResponse(ResponseBody responseBody) {
        try {
            String sentimentReport;
            String emotionReport;

            JSONObject responseJsonObject = new JSONObject(responseBody.string());
            JSONObject sentimentJsonObject = responseJsonObject.getJSONObject("sentiment");

            if(sentimentJsonObject != null) {
                JSONObject documentJsonObject = sentimentJsonObject.getJSONObject("document");
                String score = String.valueOf(documentJsonObject.getDouble("score"));
                String label = documentJsonObject.getString("label");

                sentimentReport = getString(R.string.sentiment_report, label, score);

                mSentimentReportTextView.setText(sentimentReport);
                mSentimentReportTextView.setVisibility(View.VISIBLE);
            }
            JSONObject emotionJsonObject = responseJsonObject.getJSONObject("emotion");
            if(emotionJsonObject != null) {
                JSONObject documentJsonObject = emotionJsonObject.getJSONObject("document");
                JSONObject innerEmotionJsonObject = documentJsonObject.getJSONObject("emotion");
                String sadness = String.valueOf(innerEmotionJsonObject.getDouble("sadness"));
                String joy = String.valueOf(innerEmotionJsonObject.getDouble("joy"));
                String fear = String.valueOf(innerEmotionJsonObject.getDouble("fear"));
                String disgust = String.valueOf(innerEmotionJsonObject.getDouble("disgust"));
                String anger = String.valueOf(innerEmotionJsonObject.getDouble("anger"));

                emotionReport = getString(R.string.emotion_report, sadness, joy, fear, disgust, anger);

                mEmotionReportTextView.setText(emotionReport);
                mEmotionReportTextView.setVisibility(View.VISIBLE);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}
