package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.model.MedicalRecord;
import com.example.myapplication.model.PatientInfo;

import java.util.List;

// ConversationAdapter.java
public class MedicalRecordAdapter extends RecyclerView.Adapter<MedicalRecordAdapter.ConversationViewHolder> {
    private List<MedicalRecord> medicalRecords;

    public MedicalRecordAdapter(List<MedicalRecord> medicalRecords) {
        this.medicalRecords = medicalRecords;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        MedicalRecord medicalRecord = medicalRecords.get(position);
        holder.bind(medicalRecord);
    }

    @Override
    public int getItemCount() {
        return medicalRecords.size();
    }

    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        private TextView diseaseNameTextView;
        private TextView diseaseProgressTextView;
        private TextView symptomsTextView;
        private TextView doctorResponseTextView;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            diseaseNameTextView = itemView.findViewById(R.id.disease_name_text_view);
            diseaseProgressTextView = itemView.findViewById(R.id.disease_progress_text_view);
            symptomsTextView = itemView.findViewById(R.id.symptoms_text_view);
            doctorResponseTextView = itemView.findViewById(R.id.doctor_response_text_view);
        }

        public void bind(MedicalRecord medicalRecord) {
            PatientInfo patientInfo = medicalRecord.getPatientInfo();
            diseaseNameTextView.setText("病名: " + patientInfo.getDiseaseName());
            diseaseProgressTextView.setText("病程: " + patientInfo.getDiseaseProgress());
            symptomsTextView.setText("病症: " + patientInfo.getSymptoms());
            // 用药剂量
            doctorResponseTextView.setText("医生回复: " + medicalRecord.getDoctorResponse());
        }
    }
}
