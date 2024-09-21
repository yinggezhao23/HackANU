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


public class MedicalRecordListActivity extends AppCompatActivity implements VoiceInputManager.OnRecordingFinishedListener {
    private RecyclerView medicalRecordRecyclerView;
    private MedicalRecordAdapter medicalRecordAdapter;
    private List<MedicalRecord> medicalRecords;
    private Button startConsultationButton;
    private VoiceInputManager voiceInputManager;

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
}