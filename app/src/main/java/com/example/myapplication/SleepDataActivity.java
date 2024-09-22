package com.example.myapplication;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class SleepDataActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_data); // 使用XML布局

        // 获取自定义的 SleepDataView，并加载数据
        SleepDataView sleepDataView = findViewById(R.id.sleepDataView);
        sleepDataView.setData(DataLoader.loadDataFromAsset(this)); // 设置数据
    }
}
