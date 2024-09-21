package com.example.myapplication;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.User;

public class MainActivity extends AppCompatActivity {

    // 创建一个用户对象（模拟数据）
    private User mockUser = new User("testUser", "123456");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // 关联到你的 XML 布局文件

        // 通过 ID 获取布局中的控件
        EditText usernameEditText = findViewById(R.id.username);
        EditText passwordEditText = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.loginButton);

        // 设置登录按钮的点击事件
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputUsername = usernameEditText.getText().toString();
                String inputPassword = passwordEditText.getText().toString();

                // 使用 User 类进行验证
                if (mockUser.validateLogin(inputUsername, inputPassword)) {
                    // 登录成功提示
                    Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                } else {
                    // 登录失败提示
                    Toast.makeText(MainActivity.this, "Invalid Username or Password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}