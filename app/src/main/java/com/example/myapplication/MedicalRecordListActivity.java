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
            Toast.makeText(this, "录音已完成并发送", Toast.LENGTH_SHORT).show();
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


    private void addNewMedicalRecord(PatientInfo patientInfo) {
        MedicalRecord newMedicalRecord = new MedicalRecord(patientInfo, "");
        medicalRecords.add(0, newMedicalRecord);
        medicalRecordAdapter.notifyItemInserted(0);
        medicalRecordRecyclerView.scrollToPosition(0);
    }

    // 上传音频并获取转录文本
    private void uploadAudioAndGetTranscription(File file) throws IOException {
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

//        // 异步发送请求
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                e.printStackTrace();
//                runOnUiThread(() ->
//                        Toast.makeText(MedicalRecordListActivity.this, "Failed to upload audio", Toast.LENGTH_LONG).show()
//                );
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if (!response.isSuccessful()) {
//                    runOnUiThread(() ->
//                            Toast.makeText(MedicalRecordListActivity.this, "Failed to transcribe audio", Toast.LENGTH_LONG).show()
//                    );
//                    return;
//                }
//
//                // 使用 Gson 将响应体转为 JsonObject
//                Gson gson = new Gson();
//                JsonObject jsonObject = gson.fromJson(response.body().charStream(), JsonObject.class);
//                String transcription = jsonObject.get("text").getAsString();
        String transcription = "病名: 慢性支气管炎\n" +
                "病程: 患者已反复发作3年，最近1个月病情加重，主要表现为晨起咳嗽、咳痰，尤其在天气寒冷或空气污染时症状明显加重。\n" +
                "病症描述: 患者每天早上起床后常有咳嗽，并伴随大量黄痰，偶有胸闷及轻微气喘。每次感冒后咳嗽症状持续时间较长，夜间咳嗽影响睡眠。过去一个月，咳嗽频率明显增加，痰液颜色变深，患者自觉体力下降。\n" +
                "用药方案:\n" +
                "阿莫西林: 每次500mg，每日三次，饭后服用，疗程7天。\n" +
                "氨溴索口服液: 每次30mg，每日三次，帮助化痰，持续使用两周。\n" +
                "沙丁胺醇吸入剂: 必要时吸入，每次两喷，每日不超过四次，用于缓解气喘症状。\n" +
                "注意事项: 继续避免接触冷空气和烟尘，定期复查肺功能，若症状未见改善需调整治疗方案。";

//                // 在主线程上更新 UI
//                runOnUiThread(() ->
//                        Toast.makeText(MedicalRecordListActivity.this, transcription, Toast.LENGTH_LONG).show()
//                );

        // 调用方法，将 transcription 发送到 ChatGPT API
        sendToChatGPT(transcription);
//            }
//        });
    }

    // 发送 transcription 到 ChatGPT，并获取 JSON 响应
    private void sendToChatGPT(String transcription) {
        // 构建请求体
        String prompt = "把这段话变成一段JSON，格式是{\n" +
                "  \"diseaseName\": \">sdfskjl\",\n" +
                "  \"diseaseProgress\": \"sdfsf d\",\n" +
                "  \"symptoms\": \"sdfsfdfds\"\n" +
                "}\n\n内容是：" + transcription;
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
                runOnUiThread(() ->
                        Toast.makeText(MedicalRecordListActivity.this, "Failed to get JSON from ChatGPT", Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(MedicalRecordListActivity.this, "Failed to get JSON from ChatGPT", Toast.LENGTH_LONG).show()
                    );
                    return;
                }

                // 解析 ChatGPT 的响应
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(response.body().charStream(), JsonObject.class);
                String chatGptResponse = jsonObject.getAsJsonArray("choices").get(0).getAsJsonObject()
                        .get("message").getAsJsonObject().get("content").getAsString();

                // 使用 Gson 将响应体转为 JsonObject
                jsonObject = gson.fromJson(chatGptResponse, JsonObject.class);
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
            }
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
                Toast.makeText(this, "录音权限已授予，请再次尝试", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "录音权限被拒绝", Toast.LENGTH_SHORT).show();
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

            PatientInfo patientInfo = new PatientInfo(diseaseName, diseaseProgress, symptoms);
            addNewMedicalRecord(patientInfo);
            bottomSheetDialog.dismiss();
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
            recordButton.setText("松开发送");
            showVoiceWaveform();
        } else {
            // This should be handled by the activity
            requestRecordPermission();
        }
    }

    private void handleRecordButtonRelease() {
        voiceInputManager.stopRecording();
        recordButton.setText("按住说话");
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