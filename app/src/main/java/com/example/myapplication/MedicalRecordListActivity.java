package com.example.myapplication;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.model.MedicalRecord;
import com.example.myapplication.model.PatientInfo;

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

    private static final String API_KEY = "your-api-key-here";

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

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
        ConsultationBottomSheet consultationBottomSheet = new ConsultationBottomSheet(this, voiceInputManager);
        consultationBottomSheet.setOnSubmitListener(patientInfo -> {
            addNewMedicalRecord(patientInfo);
        });
        consultationBottomSheet.show();
    }


    private void addNewMedicalRecord(PatientInfo patientInfo) {
        MedicalRecord newMedicalRecord = new MedicalRecord(patientInfo, "");
        medicalRecords.add(0, newMedicalRecord);
        medicalRecordAdapter.notifyItemInserted(0);
        medicalRecordRecyclerView.scrollToPosition(0);
    }

    private void uploadAudioAndGetTranscription(File file) throws IOException {
        // 创建 OkHttpClient 实例
        OkHttpClient client = new OkHttpClient();

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
                .header("Authorization", "Bearer " + API_KEY)  // 使用你的 API Key
                .post(requestBody)
                .build();

        // 异步发送请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MedicalRecordListActivity.this, "Failed to upload audio", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MedicalRecordListActivity.this, "Failed to transcribe audio", Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }

                // 获取转录文本
                final String transcription = response.body().string();

                // 在主线程上更新 UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 显示转录结果
                        Toast.makeText(MedicalRecordListActivity.this, transcription, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}