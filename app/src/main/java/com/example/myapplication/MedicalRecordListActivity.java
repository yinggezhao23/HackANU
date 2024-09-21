package com.example.myapplication;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
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

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MedicalRecordListActivity extends AppCompatActivity implements VoiceInputManager.OnRecordingFinishedListener {
    private RecyclerView conversationRecyclerView;
    private ConversationAdapter conversationAdapter;
    private List<Conversation> conversations;
    private Button startConsultationButton;
    private VoiceInputManager voiceInputManager;
    private BottomSheetDialog bottomSheetDialog;
    private Button recordButton;
    private VoiceWaveformView voiceWaveformView;

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
        conversationRecyclerView = findViewById(R.id.conversation_recycler_view);
        startConsultationButton = findViewById(R.id.start_consultation_button);
    }

    private void setupRecyclerView() {
        conversations = new ArrayList<>();
        conversationAdapter = new ConversationAdapter(conversations);
        conversationRecyclerView.setAdapter(conversationAdapter);
        conversationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
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

        // TODO: Send audio file to server or process it
    }

    private void showConsultationBottomSheet() {
        bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_consultation, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // 设置软键盘模式为 SOFT_INPUT_ADJUST_PAN
        if (bottomSheetDialog.getWindow() != null) {
            bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        EditText diseaseNameEditText = bottomSheetView.findViewById(R.id.disease_name_edit_text);
        EditText diseaseProgressEditText = bottomSheetView.findViewById(R.id.disease_progress_edit_text);
        EditText symptomsEditText = bottomSheetView.findViewById(R.id.symptoms_edit_text);
        Button submitButton = bottomSheetView.findViewById(R.id.submit_button);
        recordButton = bottomSheetView.findViewById(R.id.record_button);
        voiceWaveformView = bottomSheetView.findViewById(R.id.voice_waveform_view);
        voiceInputManager.setWaveformView(voiceWaveformView);

        setupSubmitButton(submitButton, diseaseNameEditText, diseaseProgressEditText, symptomsEditText);
        setupRecordButton();

        bottomSheetDialog.show();
    }

    private void setupSubmitButton(Button submitButton, EditText diseaseNameEditText,
                                   EditText diseaseProgressEditText, EditText symptomsEditText) {
        submitButton.setOnClickListener(v -> {
            String diseaseName = diseaseNameEditText.getText().toString();
            String diseaseProgress = diseaseProgressEditText.getText().toString();
            String symptoms = symptomsEditText.getText().toString();

            PatientInfo patientInfo = new PatientInfo(diseaseName, diseaseProgress, symptoms);
            addNewConversation(patientInfo);
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

    private void addNewConversation(PatientInfo patientInfo) {
        Conversation newConversation = new Conversation(patientInfo, "");
        conversations.add(0, newConversation);
        conversationAdapter.notifyItemInserted(0);
        conversationRecyclerView.scrollToPosition(0);
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