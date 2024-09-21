package com.example.myapplication.model;

// 药物类
public class Medication {
    private String name;  // 药物名称
    private String dosage;  // 剂量
    private String frequency;  // 服药频率

    public Medication(String name, String dosage, String frequency) {
        this.name = name;
        this.dosage = dosage;
        this.frequency = frequency;
    }

    // Getter 和 Setter 方法
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }
}
