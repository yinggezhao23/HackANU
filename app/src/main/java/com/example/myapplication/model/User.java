package com.example.myapplication.model;

public class User {
    private String username;
    private String password;

    // 构造方法
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getter 方法
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    // 验证登录
    public boolean validateLogin(String inputUsername, String inputPassword) {
        return username.equals(inputUsername) && password.equals(inputPassword);
    }
}

