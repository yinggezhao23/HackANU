package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class VoiceWaveformView extends View {
    private static final int MAX_AMPLITUDES = 100;
    private List<Float> amplitudes;
    private Paint paint;
    private Path path;

    public VoiceWaveformView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        amplitudes = new ArrayList<>();
        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5f);
        paint.setAntiAlias(true);

        path = new Path();
    }

    public void addAmplitude(float amplitude) {
        amplitudes.add(amplitude);
        if (amplitudes.size() > MAX_AMPLITUDES) {
            amplitudes.remove(0);
        }
        invalidate();
    }

    public void clear() {
        amplitudes.clear();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (amplitudes.isEmpty()) return;

        float width = getWidth();
        float height = getHeight();
        float midHeight = height / 2;
        float maxAmplitude = getMaxAmplitude();

        path.reset();
        float stepX = width / (MAX_AMPLITUDES - 1);
        float x = 0;

        for (int i = 0; i < amplitudes.size(); i++) {
            float amplitude = amplitudes.get(i);
            float scaledAmplitude = (amplitude / maxAmplitude) * (height / 2);
            float y = midHeight - scaledAmplitude;

            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
            x += stepX;
        }

        canvas.drawPath(path, paint);
    }

    private float getMaxAmplitude() {
        float max = 0;
        for (float amplitude : amplitudes) {
            if (amplitude > max) {
                max = amplitude;
            }
        }
        return Math.max(max, 1); // 避免除以零
    }
}