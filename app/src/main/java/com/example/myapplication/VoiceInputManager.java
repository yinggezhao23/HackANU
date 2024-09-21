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
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class VoiceInputManager {
    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BITS_PER_SAMPLE = 16;
    private static final int CHANNELS = 1;

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

            outputFile = new File(context.getCacheDir(), "audio_record_" + System.currentTimeMillis() + ".wav");
            outputStream = new FileOutputStream(outputFile);

            // 写入WAV文件的头部
            writeWavHeader(outputStream, SAMPLE_RATE, CHANNELS, BITS_PER_SAMPLE);

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
                // 录音结束时写入WAV文件的最终大小信息
                updateWavHeader(outputFile);
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
                    byte[] byteBuffer = new byte[read * 2];  // 每个short占两个字节
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

    // 写入WAV文件头部信息
    private void writeWavHeader(FileOutputStream out, int sampleRate, int channels, int bitsPerSample) throws IOException {
        byte[] header = new byte[44];

        long byteRate = sampleRate * channels * bitsPerSample / 8;

        // Chunk ID
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';

        // Chunk Size (will be updated later)
        header[4] = 0;
        header[5] = 0;
        header[6] = 0;
        header[7] = 0;

        // Format
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';

        // Subchunk1 ID
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';

        // Subchunk1 Size
        header[16] = 16;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;

        // Audio Format (PCM = 1)
        header[20] = 1;
        header[21] = 0;

        // Num Channels
        header[22] = (byte) channels;
        header[23] = 0;

        // Sample Rate
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);

        // Byte Rate
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);

        // Block Align
        header[32] = (byte) (channels * bitsPerSample / 8);
        header[33] = 0;

        // Bits Per Sample
        header[34] = (byte) bitsPerSample;
        header[35] = 0;

        // Subchunk2 ID
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';

        // Subchunk2 Size (will be updated later)
        header[40] = 0;
        header[41] = 0;
        header[42] = 0;
        header[43] = 0;

        out.write(header, 0, 44);
    }

    // 更新WAV文件的头部信息（主要是文件大小）
    private void updateWavHeader(File wavFile) throws IOException {
        try (FileOutputStream out = new FileOutputStream(wavFile, true)) {
            byte[] sizes = new byte[8];

            long audioLength = wavFile.length() - 44;
            long fileSize = wavFile.length() - 8;

            ByteBuffer buffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            buffer.putInt((int) fileSize);
            buffer.putInt((int) audioLength);

            System.arraycopy(buffer.array(), 0, sizes, 0, 8);

            // Update ChunkSize and Subchunk2Size in the header
            try (RandomAccessFile raf = new RandomAccessFile(wavFile, "rw")) {
                raf.seek(4);
                raf.write(sizes, 0, 4); // ChunkSize
                raf.seek(40);
                raf.write(sizes, 4, 4); // Subchunk2Size
            }
        }
    }
}
