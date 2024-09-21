package com.example.myapplication.model;

import java.util.ArrayList;
import java.util.List;

// 药物类
class Medication {
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

// 病例类
class MedicalRecord {
    private String diseaseName;  // 病名
    private String diseaseCourse;  // 病程
    private List<Medication> medicationPlan;  // 服药计划
    private List<String> symptoms;  // 病情描述

    // 构造方法
    public MedicalRecord(String diseaseName, String diseaseCourse) {
        this.diseaseName = diseaseName;
        this.diseaseCourse = diseaseCourse;
        this.medicationPlan = new ArrayList<>();
        this.symptoms = new ArrayList<>();
    }

    // 添加药物到服药计划
    public void addMedication(Medication medication) {
        medicationPlan.add(medication);
    }

    // 添加病情描述
    public void addSymptom(String symptom) {
        symptoms.add(symptom);
    }

    // Getter 和 Setter 方法
    public String getDiseaseName() {
        return diseaseName;
    }

    public void setDiseaseName(String diseaseName) {
        this.diseaseName = diseaseName;
    }

    public String getDiseaseCourse() {
        return diseaseCourse;
    }

    public void setDiseaseCourse(String diseaseCourse) {
        this.diseaseCourse = diseaseCourse;
    }

    public List<Medication> getMedicationPlan() {
        return medicationPlan;
    }

    public void setMedicationPlan(List<Medication> medicationPlan) {
        this.medicationPlan = medicationPlan;
    }

    public List<String> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(List<String> symptoms) {
        this.symptoms = symptoms;
    }
}

