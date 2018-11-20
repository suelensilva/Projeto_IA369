package br.com.ia369.bichinhovirtual;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.ia369.bichinhovirtual.appraisal.AppraisalConstants;
import br.com.ia369.bichinhovirtual.appraisal.EmotionEngineService;
import br.com.ia369.bichinhovirtual.model.Creature;
import br.com.ia369.bichinhovirtual.model.TranslationResponse;
import br.com.ia369.bichinhovirtual.modelview.CreatureViewModel;
import br.com.ia369.bichinhovirtual.retrofit.IbmNluService;
import br.com.ia369.bichinhovirtual.retrofit.ServiceGenerator;
import br.com.ia369.bichinhovirtual.retrofit.TranslateService;
import br.com.ia369.bichinhovirtual.util.BitmapUtils;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements RecognitionListener, SensorEventListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 1;

    private static final String FILE_PROVIDER_AUTHORITY = "br.com.ia369.bichinhovirtual.fileprovider";

    private static final double SMILING_PROB_THRESHOLD = .15;

    private View mRootView;
    private TextView mTextView;
    private TextView mSentimentReportTextView;
    private TextView mEmotionReportTextView;
    private EditText mEditText;
    private ImageView mCreatureImageView;
    private SpeechRecognizer speechRecognizer;

    private String mTempPhotoPath;

    private SensorManager mSensorManager;
    private Sensor mProximity;
    private PowerManager.WakeLock mWakeLock;

    private Creature mCreature;

    private TextView.OnEditorActionListener mOnEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            String text = mEditText.getText().toString();

            if(!TextUtils.isEmpty(text)) {
                showProgressView();
                translateToEnglish(text);
            }

            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mRootView = findViewById(R.id.root_view);
        mTextView = findViewById(R.id.sample_text);
        mSentimentReportTextView = findViewById(R.id.sentiment_report_text);
        mEmotionReportTextView = findViewById(R.id.emotion_report_text);
        mEditText = findViewById(R.id.input_edit_text);
        mEditText.setOnEditorActionListener(mOnEditorActionListener);
        mCreatureImageView = findViewById(R.id.creature);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(this);

        CreatureViewModel mCreatureViewModel = ViewModelProviders.of(this).get(CreatureViewModel.class);
        mCreatureViewModel.getCreature().observe(this, new Observer<Creature>() {
            @Override
            public void onChanged(@Nullable Creature creature) {
                if (creature != null) {
                    updateCreature(creature);
                }
            }
        });

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (mSensorManager != null) {
            mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null && powerManager.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)) {
            Log.d(TAG, "Screen off wake lock supported");
            mWakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, TAG);
        } else {
            Log.d(TAG, "Screen off wake lock not supported");
            mWakeLock = null;
        }

        scheduleEmotionEngineJob();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(this);
        super.onPause();
    }

    private void scheduleEmotionEngineJob() {
        EmotionEngineService.scheduleEmotionEngineJob(this);
    }

    private void updateCreature(Creature creature) {
        Log.d(TAG, "Updating creature emotion...");

        int emotion = creature.getEmotion();

        if(creature.getPersonality() == AppraisalConstants.PERSONALITY_EXTROVERT) {

            mRootView.setBackgroundResource(R.drawable.background);

            switch (emotion) {
                case AppraisalConstants.EMOTION_FEAR:
                    mCreatureImageView.setImageResource(R.drawable.extrov_medo);
                    break;
                case AppraisalConstants.EMOTION_JOY:
                    mCreatureImageView.setImageResource(R.drawable.extrov_felicidade);
                    break;
                case AppraisalConstants.EMOTION_SADNESS:
                    mCreatureImageView.setImageResource(R.drawable.extrov_tristeza);
                    break;
                case AppraisalConstants.EMOTION_DISGUST:
                    mCreatureImageView.setImageResource(R.drawable.extrov_nojo);
                    break;
                case AppraisalConstants.EMOTION_ANGER:
                    mCreatureImageView.setImageResource(R.drawable.extrov_raiva);
                    break;
                case AppraisalConstants.EMOTION_SATISFACTION:
                    mCreatureImageView.setImageResource(R.drawable.extrov_animado);
                    break;
                case AppraisalConstants.EMOTION_DISTRESS:
                    mCreatureImageView.setImageResource(R.drawable.extrov_tristeza);
                    break;
                case AppraisalConstants.EMOTION_GRATITUDE:
                    mCreatureImageView.setImageResource(R.drawable.extrov_felicidade);
                    break;
                case AppraisalConstants.EMOTION_NEUTRAL:
                    mCreatureImageView.setImageResource(R.drawable.extrov_neutro);
                    break;
                case AppraisalConstants.EMOTION_BORED:
                    mCreatureImageView.setImageResource(R.drawable.extrov_entediado);
                    break;
            }
        } else if(creature.getPersonality() == AppraisalConstants.PERSONALITY_NEUROTIC){

            mRootView.setBackgroundResource(R.drawable.background2);

            switch (emotion) {
                case AppraisalConstants.EMOTION_FEAR:
                    mCreatureImageView.setImageResource(R.drawable.neuro_medo);
                    break;
                case AppraisalConstants.EMOTION_JOY:
                    mCreatureImageView.setImageResource(R.drawable.neuro_felicidade);
                    break;
                case AppraisalConstants.EMOTION_SADNESS:
                    mCreatureImageView.setImageResource(R.drawable.neuro_tristeza);
                    break;
                case AppraisalConstants.EMOTION_DISGUST:
                    mCreatureImageView.setImageResource(R.drawable.neuro_nojo);
                    break;
                case AppraisalConstants.EMOTION_ANGER:
                    mCreatureImageView.setImageResource(R.drawable.neuro_raiva);
                    break;
                case AppraisalConstants.EMOTION_SATISFACTION:
                    mCreatureImageView.setImageResource(R.drawable.neuro_animado);
                    break;
                case AppraisalConstants.EMOTION_DISTRESS:
                    mCreatureImageView.setImageResource(R.drawable.neuro_tristeza);
                    break;
                case AppraisalConstants.EMOTION_GRATITUDE:
                    mCreatureImageView.setImageResource(R.drawable.neuro_felicidade);
                    break;
                case AppraisalConstants.EMOTION_NEUTRAL:
                    mCreatureImageView.setImageResource(R.drawable.neuro_neutro);
                    break;
                case AppraisalConstants.EMOTION_BORED:
                    mCreatureImageView.setImageResource(R.drawable.neuro_entediado);
                    break;
            }
        }

        // Atualiza a instancia do bichinho na activity
        mCreature = creature;
    }

    public void openSettings(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public void startSpeechRecognizer(View view) {

        mSentimentReportTextView.setVisibility(View.GONE);
        mEmotionReportTextView.setVisibility(View.GONE);

        mTextView.setText(R.string.listening);

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
        Log.d(TAG, "onEndOfSpeech");
    }

    @Override
    public void onError(int i) {
        Log.d(TAG, "onError "+i);
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

        // TODO remover string de key do versionamento
        queryMap.put("key", "trnsl.1.1.20180930T235915Z.de076450d8e5aaf8.eb3477709ac41d3e9d7cac25568dff613f1b6270");
        queryMap.put("lang", "pt-en");
        queryMap.put("text", portugueseText);

        Call<TranslationResponse> responseCallback = service.translate(queryMap);
        responseCallback.enqueue(new Callback<TranslationResponse>() {
            @Override
            public void onResponse(@NonNull Call<TranslationResponse> call, @NonNull Response<TranslationResponse> response) {
                TranslationResponse translation = response.body();
                if(translation != null) {
                    String translatedText = translation.text[0];
                    classifyEmotion(translatedText);
                }
            }

            @Override
            public void onFailure(@NonNull Call<TranslationResponse> call, @NonNull Throwable t) {

            }
        });
    }

    private void classifyEmotion(String text) {

        // TODO remover username e password do versionamento
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
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    ResponseBody responseBody = response.body();
                    if(responseBody != null) {
                        parseNluResponse(responseBody);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
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

                Double[] scores = new Double[] {
                        Double.parseDouble(sadness),
                        Double.parseDouble(joy),
                        Double.parseDouble(fear),
                        Double.parseDouble(disgust),
                        Double.parseDouble(anger)
                };
                calculateMoreRelevantEmotionAndShowNewFace(scores);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        mEditText.setText("");
    }

    private void calculateMoreRelevantEmotionAndShowNewFace(Double[] emotionScores) {
        int relevantEmotionIndex = -1;
        double currRelevantScore = -1;
        for(int i = 0; i < emotionScores.length; i++) {
            if(emotionScores[i] > currRelevantScore) {
                currRelevantScore = emotionScores[i];
                relevantEmotionIndex = i;
            }
        }

        switch (relevantEmotionIndex) {
            case 0: // sadness
                triggerInputAction(AppraisalConstants.INPUT_TEXT_SADNESS);
                break;
            case 1: // joy
                triggerInputAction(AppraisalConstants.INPUT_TEXT_JOY);
                break;
            case 2: // fear
                triggerInputAction(AppraisalConstants.INPUT_TEXT_FEAR);
                break;
            case 3: // disgust
                triggerInputAction(AppraisalConstants.INPUT_TEXT_DISGUST);
                break;
            case 4: // anger
                triggerInputAction(AppraisalConstants.INPUT_TEXT_ANGER);
                break;
        }

        dismissProgressView();
    }

    private void showProgressView() {
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
    }

    private void dismissProgressView() {
        findViewById(R.id.progress).setVisibility(View.GONE);
    }

    public void analyzeFace(View view) {
        // Check for the external storage permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // If you do not have permission, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        } else {
            // Launch the camera if the permission exists
            launchCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Called when you request permission to read and write to external storage
        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // If you get permission, launch the camera
                    launchCamera();
                } else {
                    // If you do not get permission, show a Toast
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    /**
     * Creates a temporary image file and captures a picture to store in it.
     */
    private void launchCamera() {

        // Create the capture image intent
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
        takePictureIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the temporary File where the photo should go
            File photoFile = null;
            try {
                photoFile = BitmapUtils.createTempImageFile(this);
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {

                // Get the path of the temporary file
                mTempPhotoPath = photoFile.getAbsolutePath();

                // Get the content URI for the image file
                Uri photoURI = FileProvider.getUriForFile(this,
                        FILE_PROVIDER_AUTHORITY,
                        photoFile);

                // Add the URI so the camera can store the image
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // Launch the camera activity
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If the image capture activity was called and was successful
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            new ProcessImageAsyncTask(this).execute();
        } else {
            // Otherwise, delete the temporary image file
            BitmapUtils.deleteImageFile(this, mTempPhotoPath);
        }
    }

    boolean processImage() {
        // Resample the saved image to fit the ImageView
        Bitmap resultBitmap = BitmapUtils.resamplePic(this, mTempPhotoPath);
        return isSmiling(resultBitmap);
    }

    private void showFaceResult(boolean isSmiling) {
        if(isSmiling) {
            triggerInputAction(AppraisalConstants.INPUT_FACE_POSITIVE);
        } else {
            triggerInputAction(AppraisalConstants.INPUT_FACE_NEGATIVE);
        }
    }

    private boolean isSmiling(Bitmap picture) {
        // Create the face detector, disable tracking and enable classifications
        FaceDetector detector = new FaceDetector.Builder(this)
                .setTrackingEnabled(false)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setProminentFaceOnly(true)
                .build();

        // Build the frame
        Frame frame = new Frame.Builder().setBitmap(picture).build();

        // Detect the faces
        SparseArray<Face> faces = detector.detect(frame);

        if (faces.size() == 0) {
            Toast.makeText(this, "Nenhuma face encontrada.", Toast.LENGTH_SHORT).show();
        } else {
            Face face = faces.valueAt(0);
            return face.getIsSmilingProbability() > SMILING_PROB_THRESHOLD;
        }
        return false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mWakeLock == null) {
            return;
        }

        // O sensor pode ser booleano e o valor ser 0.0(perto) ou 1.0(longe), ou
        // ter um range. Se tiver um range maior que 1.0, considera-se perto um valor
        // menor que 5.0
        boolean isNear;
        if (event.sensor.getMaximumRange() == 1) {
            isNear = event.values[0] == 0.0f;
        } else {
            isNear = event.values[0] < 5.0f;
        }

        if (isNear && !mWakeLock.isHeld()) {
            mWakeLock.acquire(1000 * 60 * 60);// 1 hora
            setupIdDarkMode();
        } else if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }

    private void setupIdDarkMode() {
//        mCreatureImageView.setImageResource(R.drawable.extrov_medo);
//        Appraisal appraisal = new Appraisal();
//        try {
//            double intensity = appraisal.evaluateFear();
//            Log.d(TAG, "Fear intensity = "+intensity);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        triggerInputAction(AppraisalConstants.INPUT_PROXIMITY_SENSOR);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void triggerInputAction(int type) {
        Intent intent = new Intent(this, EmotionEngineService.class);
        intent.setAction(AppraisalConstants.ACTIVE_INPUT_ACTION);
        intent.putExtra(AppraisalConstants.INPUT_TYPE_EXTRA, type);
        startService(intent);
    }

    static class ProcessImageAsyncTask extends AsyncTask<Void, Void, Boolean> {

        private WeakReference<MainActivity> activity;

        ProcessImageAsyncTask(MainActivity activity) {
            this.activity = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            activity.get().showProgressView();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            // Process the image and set it to the TextView
            return activity.get().processImage();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            MainActivity mainActivity = activity.get();
            if(mainActivity != null) {
                mainActivity.dismissProgressView();
                mainActivity.showFaceResult(aBoolean);
            }
        }
    }
}
