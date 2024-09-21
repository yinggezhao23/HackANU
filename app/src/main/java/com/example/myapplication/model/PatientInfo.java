package com.example.myapplication.model;

import java.util.ArrayList;
import java.util.List;

// PatientInfo.java
public class PatientInfo {
    private String diseaseName;
    private String diseaseProgress;
    private String symptoms;
    private List<Medication> medicationPlan;  // 服药计划

    public PatientInfo(String diseaseName, String diseaseProgress, String symptoms) {
        this.diseaseName = diseaseName;
        this.diseaseProgress = diseaseProgress;
        this.symptoms = symptoms;
        this.medicationPlan = new ArrayList<>();
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

    public List<Medication> getMedicationPlan() {
        return medicationPlan;
    }
}