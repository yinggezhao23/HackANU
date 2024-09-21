package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class VoiceInputManager {
    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord audioRecord;
    private boolean isRecording;
    private Context context;
    private File outputFile;
    private FileOutputStream outputStream;
    private OnRecordingFinishedListener listener;
    private VoiceWaveformView waveformView;
    private Handler mainHandler;

    public interface OnRecordingFinishedListener {
        void onRecordingFinished(File audioFile);
    }

    public VoiceInputManager(Context context) {
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public boolean checkPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void setWaveformView(VoiceWaveformView waveformView) {
        this.waveformView = waveformView;
    }

    public void startRecording() {
        if (isRecording) return;

        if (!checkPermission()) {
            return;
        }

        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            return;
        }

        try {
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize);

            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                return;
            }

            outputFile = new File(context.getCacheDir(), "audio_record_" + System.currentTimeMillis() + ".pcm");
            outputStream = new FileOutputStream(outputFile);

            audioRecord.startRecording();
            isRecording = true;

            new Thread(this::processAudio).start();
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
            cleanupRecording();
        }
    }

    public void stopRecording() {
        if (!isRecording) return;

        isRecording = false;
        cleanupRecording();

        if (listener != null && outputFile != null && outputFile.exists()) {
            listener.onRecordingFinished(outputFile);
        }
    }


    private void cleanupRecording() {
        if (audioRecord != null) {
            try {
                audioRecord.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            audioRecord.release();
            audioRecord = null;
        }

        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            outputStream = null;
        }
    }

    private void processAudio() {
        short[] buffer = new short[1024];
        while (isRecording) {
            int read = audioRecord.read(buffer, 0, buffer.length);
            if (read > 0) {
                try {
                    byte[] byteBuffer = new byte[read * 2];
                    for (int i = 0; i < read; i++) {
                        short sample = buffer[i];
                        byteBuffer[i * 2] = (byte) (sample & 0xff);
                        byteBuffer[i * 2 + 1] = (byte) ((sample >> 8) & 0xff);
                    }
                    outputStream.write(byteBuffer);

                    final float amplitude = calculateAmplitude(buffer, read);
                    mainHandler.post(() -> {
                        if (waveformView != null) {
                            waveformView.addAmplitude(amplitude);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private float calculateAmplitude(short[] buffer, int read) {
        long sum = 0;
        for (int i = 0; i < read; i++) {
            sum += Math.abs(buffer[i]);
        }
        return sum / (float) read;
    }

    public void setOnRecordingFinishedListener(OnRecordingFinishedListener listener) {
        this.listener = listener;
    }
}