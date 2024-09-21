package com.example.myapplication;

// PatientInfo.java
public class PatientInfo {
    private String diseaseName;
    private String diseaseProgress;
    private String symptoms;

    public PatientInfo(String diseaseName, String diseaseProgress, String symptoms) {
        this.diseaseName = diseaseName;
        this.diseaseProgress = diseaseProgress;
        this.symptoms = symptoms;
    }

    // Getters and setters
    public String getDiseaseName() {
        return diseaseName;
    }

    public String getDiseaseProgress() {
        return diseaseProgress;
    }

    public String getSymptoms() {
        return symptoms;
    }
}