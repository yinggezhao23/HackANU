package com.example.myapplication;

// Conversation.java
public class Conversation {
    private PatientInfo patientInfo;
    private String doctorResponse;

    public Conversation(PatientInfo patientInfo, String doctorResponse) {
        this.patientInfo = patientInfo;
        this.doctorResponse = doctorResponse;
    }

    // Getters and setters
    public PatientInfo getPatientInfo() {
        return patientInfo;
    }

    public String getDoctorResponse() {
        return doctorResponse;
    }
}