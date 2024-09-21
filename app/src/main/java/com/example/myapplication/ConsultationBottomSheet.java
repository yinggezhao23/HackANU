package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.core.app.ActivityCompat;

import com.example.myapplication.model.PatientInfo;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class ConsultationBottomSheet {
    private BottomSheetDialog bottomSheetDialog;
    private VoiceInputManager voiceInputManager;
    private VoiceWaveformView voiceWaveformView;
    private Button recordButton;
    private Activity activity;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    public interface OnSubmitListener {
        void onSubmit(PatientInfo patientInfo);
    }

    public ConsultationBottomSheet(Activity activity, VoiceInputManager voiceInputManager) {
        this.activity = activity;
        this.voiceInputManager = voiceInputManager;
        setupBottomSheet();
    }

    private void setupBottomSheet() {
        bottomSheetDialog = new BottomSheetDialog(activity);
        View bottomSheetView = LayoutInflater.from(activity).inflate(R.layout.bottom_sheet_consultation, null);
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

        setupRecordButton();

        submitButton.setOnClickListener(v -> {
            String diseaseName = diseaseNameEditText.getText().toString();
            String diseaseProgress = diseaseProgressEditText.getText().toString();
            String symptoms = symptomsEditText.getText().toString();

            PatientInfo patientInfo = new PatientInfo(diseaseName, diseaseProgress, symptoms);
            if (onSubmitListener != null) {
                onSubmitListener.onSubmit(patientInfo);
            }
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

    private OnSubmitListener onSubmitListener;

    public void setOnSubmitListener(OnSubmitListener listener) {
        this.onSubmitListener = listener;
    }

    public void show() {
        bottomSheetDialog.show();
    }

    private void requestRecordPermission() {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_RECORD_AUDIO_PERMISSION);
    }
}
