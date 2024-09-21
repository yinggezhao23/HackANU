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


}
