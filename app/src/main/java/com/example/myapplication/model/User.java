package com.example.myapplication.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String password;
    private List<MedicalRecord> medicalRecords;  // 存储用户的病例信息

    // 构造方法
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.medicalRecords = new ArrayList<>();  // 初始化病例列表
    }

    // Getter 和 Setter 方法
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // 添加病例
    public void addMedicalRecord(MedicalRecord medicalRecord) {
        medicalRecords.add(medicalRecord);
    }

    // 获取所有病例
    public List<MedicalRecord> getMedicalRecords() {
        return medicalRecords;
    }

    // 获取某个病例（通过索引）
    public MedicalRecord getMedicalRecord(int index) {
        if (index >= 0 && index < medicalRecords.size()) {
            return medicalRecords.get(index);
        } else {
            throw new IndexOutOfBoundsException("Invalid index for medical records");
        }
    }

    // 验证登录
    public boolean validateLogin(String inputUsername, String inputPassword) {
        return username.equals(inputUsername) && password.equals(inputPassword);
    }
}


