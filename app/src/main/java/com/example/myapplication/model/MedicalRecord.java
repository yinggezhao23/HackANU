package com.example.myapplication.model;

// 病例类
public class MedicalRecord {
    private PatientInfo patientInfo;
    private String doctorResponse;

    // 构造方法
    public MedicalRecord(PatientInfo patientInfo, String doctorResponse) {
        this.patientInfo = patientInfo;
        this.doctorResponse = doctorResponse;
    }

    // 添加药物到服药计划
    public void addMedication(Medication medication) {
//        medicationPlan.add(medication);
    }

    // 添加病情描述
    public void addSymptom(String symptom) {
//        symptoms.add(symptom);
    }

    public PatientInfo getPatientInfo() {
        return patientInfo;
    }

    public String getDoctorResponse() {
        return doctorResponse;
    }
}

