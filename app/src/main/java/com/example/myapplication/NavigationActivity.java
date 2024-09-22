package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class NavigationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        // 获取第一个按钮并设置点击事件
        Button buttonMedicalRecord = findViewById(R.id.buttonMedicalRecord);
        buttonMedicalRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到 MedicalRecordListActivity
                Intent intent = new Intent(NavigationActivity.this, MedicalRecordListActivity.class);
                startActivity(intent);
            }
        });

        // 获取第二个按钮并设置点击事件
        Button buttonSleepData = findViewById(R.id.buttonSleepData);
        buttonSleepData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到 SleepDataActivity
                Intent intent = new Intent(NavigationActivity.this, SleepDataActivity.class);
                startActivity(intent);
            }
        });
    }
}