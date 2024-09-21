package com.example.myapplication;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// ConversationAdapter.java
public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {
    private List<Conversation> conversations;

    public ConversationAdapter(List<Conversation> conversations) {
        this.conversations = conversations;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);
        holder.bind(conversation);
    }

    @Override
    public int getItemCount() {
        return conversations.size();
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

        public void bind(Conversation conversation) {
            PatientInfo patientInfo = conversation.getPatientInfo();
            diseaseNameTextView.setText("病名: " + patientInfo.getDiseaseName());
            diseaseProgressTextView.setText("病程: " + patientInfo.getDiseaseProgress());
            symptomsTextView.setText("病症: " + patientInfo.getSymptoms());
            doctorResponseTextView.setText("医生回复: " + conversation.getDoctorResponse());
        }
    }
}
