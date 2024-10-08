package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.model.MedicalRecord;
import com.example.myapplication.model.PatientInfo;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MedicalRecordListActivity extends AppCompatActivity implements VoiceInputManager.OnRecordingFinishedListener {
    private RecyclerView medicalRecordRecyclerView;
    private MedicalRecordAdapter medicalRecordAdapter;
    private List<MedicalRecord> medicalRecords;
    private Button startConsultationButton;
    private VoiceInputManager voiceInputManager;
    private BottomSheetDialog bottomSheetDialog;
    private VoiceWaveformView voiceWaveformView;
    private Button recordButton;
    private EditText diseaseNameEditText;
    private EditText symptomsEditText;
    private EditText diseaseProgressEditText;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private PatientInfo patientInfo;
    private String speakContent;

    private static final String API_KEY = "";
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_record_list);

        initializeViews();
        setupRecyclerView();
        setupConsultationButton();
        setupVoiceInputManager();

    }

    private void initializeViews() {
        medicalRecordRecyclerView = findViewById(R.id.medical_record_recycler_view);
        startConsultationButton = findViewById(R.id.start_consultation_button);
    }

    private void setupRecyclerView() {
        medicalRecords = new ArrayList<>();
        medicalRecordAdapter = new MedicalRecordAdapter(medicalRecords);
        medicalRecordRecyclerView.setAdapter(medicalRecordAdapter);
        medicalRecordRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupConsultationButton() {
        startConsultationButton.setOnClickListener(v -> showConsultationBottomSheet());
    }

    private void setupVoiceInputManager() {
        voiceInputManager = new VoiceInputManager(this);
        voiceInputManager.setOnRecordingFinishedListener(this);
    }

    @Override
    public void onRecordingFinished(File audioFile) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Recording recognition...", Toast.LENGTH_SHORT).show();
        });

        Log.i("onRecordingFinished", audioFile.toString());
        // TODO: Send audio file to server or process it
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    uploadAudioAndGetTranscription(audioFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private void showConsultationBottomSheet() {
        setupBottomSheet();
        bottomSheetDialog.show();
    }


    private void addNewMedicalRecord(PatientInfo patientInfo, String doctorResponse) {
        MedicalRecord newMedicalRecord = new MedicalRecord(patientInfo, doctorResponse);
        medicalRecords.add(0, newMedicalRecord);
        medicalRecordAdapter.notifyItemInserted(0);
        medicalRecordRecyclerView.scrollToPosition(0);
    }

    private void sendToWhisper(File file, Consumer<String> onSuccess) throws IOException {
        // 创建文件的请求体
        RequestBody fileBody = RequestBody.create(file, MediaType.parse("audio/wav"));

        // 创建 Multipart 请求体，包括文件和模型参数
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody)
                .addFormDataPart("model", "whisper-1")
                .build();

        // 构建请求
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/audio/transcriptions")
                .header("Authorization", "Bearer " + API_KEY)
                .post(requestBody)
                .build();

        // 异步发送请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(MedicalRecordListActivity.this, "Failed to upload audio", Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String test = response.body().string();
                Log.e("TEST", test);
                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(MedicalRecordListActivity.this, "Failed to transcribe audio", Toast.LENGTH_LONG).show()
                    );
                    return;
                }

                // 使用 Gson 将响应体转为 JsonObject
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(test, JsonObject.class);
                String transcription = jsonObject.get("text").getAsString();
                onSuccess.accept(transcription);
            }
        });
    }

    // 上传音频并获取转录文本
    private void uploadAudioAndGetTranscription(File file) throws IOException {
        sendToWhisper(file, (transcription) -> {
            speakContent = transcription;
            // 调用方法，将 transcription 发送到 ChatGPT API
            sendToChatGPT(transcription);
        });

//        String transcription = "我咳嗽很久了，应该有三年了吧，最近这一个月感觉咳得更厉害了，尤其早上起来的时候，咳得停不下来，还带痰。尤其是天气冷或者空气不好的时候，情况就更糟糕。\n" +
//                "\n" +
//                "每天早上起来我都会咳嗽，痰有点多，颜色也变得黄了些。有时候我还觉得胸口有点闷，喘不过气来。每次感冒之后，咳嗽就会持续很长时间，特别是晚上咳得睡不好觉。最近这段时间，咳嗽频率变多了，痰也变浓了，总感觉整个人越来越没力气。\n" +
//                "\n" +
//                "医生让我吃点药，比如阿莫西林，每天三次，饭后吃；还有一种化痰的药，每次吃30毫升，也是一天三次；有时候喘得厉害了，我还要用一种吸的药喷两下，缓解喘不过气的感觉。不过一天不能喷太多，最多四次。\n" +
//                "\n" +
//                "医生还提醒我，尽量别碰冷空气，也别接触烟尘，还得定期去复查肺功能。如果症状没改善，可能还得换别的药来治疗。";
//        sendToChatGPT(transcription);
    }

    private void sendToGpt3_5(String prompt, Consumer<String> onSuccess) {
        // 构建请求体
        JsonObject bodyJson = new JsonObject();
        bodyJson.addProperty("model", "gpt-3.5-turbo");
        bodyJson.add("messages", new Gson().toJsonTree(new Message[]{new Message("user", prompt)}));

        RequestBody requestBody = RequestBody.create(bodyJson.toString(), MediaType.parse("application/json"));

        // 构建请求
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + API_KEY)
                .post(requestBody)
                .build();

        // 异步发送请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(MedicalRecordListActivity.this, "Failed to get JSON from ChatGPT", Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(MedicalRecordListActivity.this, "Failed to get JSON from ChatGPT", Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                // 解析 ChatGPT 的响应
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(response.body().charStream(), JsonObject.class);
                String chatGptResponse = jsonObject.getAsJsonArray("choices").get(0).getAsJsonObject()
                        .get("message").getAsJsonObject().get("content").getAsString();

                // 在主线程上更新 UI
                onSuccess.accept(chatGptResponse);
            }
        });
    }

    // 发送 transcription 到 ChatGPT，并获取 JSON 响应
    private void sendToChatGPT(String transcription) {
        // 构建请求体
        String prompt = "Convert this paragraph into a JSON format {\n" +
                " \"diseaseName\": \"the content extract from the paragraph\",\n" +
                " \"diseaseProgress\": \"the content extract from the paragraph\",\n" +
                " \"symptoms\": \"the content extract from the paragraph\"\n" +
                "}\n\nThe paragraph is:" + transcription;

        sendToGpt3_5(prompt, (chatGptResponse) -> {
            Gson gson = new Gson();

            // 使用 Gson 将响应体转为 JsonObject
            JsonObject jsonObject = gson.fromJson(chatGptResponse, JsonObject.class);
            String diseaseName = jsonObject.get("diseaseName").getAsString();
            String diseaseProgress = jsonObject.get("diseaseProgress").getAsString();
            String symptoms = jsonObject.get("symptoms").getAsString();

            // 在主线程上更新 UI
            runOnUiThread(() ->
                    {
                        diseaseNameEditText.setText(diseaseName);
                        diseaseProgressEditText.setText(diseaseProgress);
                        symptomsEditText.setText(symptoms);
                    }
            );
        });
    }

    // 发送 transcription 到 ChatGPT，并获取 JSON 响应
    private void sendToChatGPT2(String transcription) {
        // 构建请求体
        String prompt = "Further analyze whether the patient's condition requires medical treatment, and respond to him in the tone of an AI doctor. There should not be any context in your response, and talk to me directly as if I were a patient. Based on the patient's description, first determine whether there are obvious health problems. If the patient's description may be just a joke or not serious, please respond in a humorous but polite manner, and do not give direct medical advice. If the description involves health risks, please provide appropriate advice based on the severity of the symptoms. The symptoms described by the patient are not necessarily serious or urgent. Please provide appropriate advice based on the severity of the symptoms, which may include some simple daily care, lifestyle adjustments, or if the symptoms are mild, suggest that there is no need to worry too much. If the symptoms are severe or risky, prompt the patient to seek medical attention in a timely manner.If user send nothing, just say it is empty\n\n" + transcription;
        bottomSheetDialog.dismiss();
        runOnUiThread(() ->
                {
                    Toast.makeText(this, "Content sent", Toast.LENGTH_SHORT).show();
                }
        );
        sendToGpt3_5(prompt, (chatGptResponse) -> {
            // 在主线程上更新 UI
            runOnUiThread(() ->
                    {
                        addNewMedicalRecord(patientInfo, chatGptResponse);
                    }
            );
        });
    }

    // 用于 OpenAI Chat API 的消息模型
    private class Message {
        String role;
        String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Recording permission has been granted, please try again", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Recording permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void setupBottomSheet() {
        bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_consultation, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // 设置软键盘模式为 SOFT_INPUT_ADJUST_PAN
        if (bottomSheetDialog.getWindow() != null) {
            bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        diseaseNameEditText = bottomSheetView.findViewById(R.id.disease_name_edit_text);
        diseaseProgressEditText = bottomSheetView.findViewById(R.id.disease_progress_edit_text);
        symptomsEditText = bottomSheetView.findViewById(R.id.symptoms_edit_text);
        Button submitButton = bottomSheetView.findViewById(R.id.submit_button);
        recordButton = bottomSheetView.findViewById(R.id.record_button);
        voiceWaveformView = bottomSheetView.findViewById(R.id.voice_waveform_view);
        voiceInputManager.setWaveformView(voiceWaveformView);

        setupRecordButton();

        submitButton.setOnClickListener(v -> {
            String diseaseName = diseaseNameEditText.getText().toString();
            String diseaseProgress = diseaseProgressEditText.getText().toString();
            String symptoms = symptomsEditText.getText().toString();

            patientInfo = new PatientInfo(diseaseName, diseaseProgress, symptoms, speakContent);
            sendToChatGPT2("diseaseName:" + diseaseName + "\ndiseaseProgress:" + diseaseProgress + "\nsymptoms:" + symptoms);
        });
    }

    private void setupRecordButton() {
        recordButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    handleRecordButtonPress();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    handleRecordButtonRelease();
                    break;
            }
            return true;
        });
    }

    private void handleRecordButtonPress() {
        if (voiceInputManager.checkPermission()) {
            voiceInputManager.startRecording();
            recordButton.setText("Release to send");
            showVoiceWaveform();
        } else {
            // This should be handled by the activity
            requestRecordPermission();
        }
    }

    private void handleRecordButtonRelease() {
        voiceInputManager.stopRecording();
        recordButton.setText("Press to speak");
        hideVoiceWaveform();
    }

    private void showVoiceWaveform() {
        voiceWaveformView.setVisibility(View.VISIBLE);
        voiceWaveformView.clear();
    }

    private void hideVoiceWaveform() {
        voiceWaveformView.setVisibility(View.GONE);
    }


    private void requestRecordPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_RECORD_AUDIO_PERMISSION);
    }
}