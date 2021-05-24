package com.example.beefclassifier;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public  class PostViewHolder extends RecyclerView.ViewHolder {
    TextView titleView;
    TextView WriterNameView;
    TextView WriteDayView;

    public PostViewHolder(@NonNull View itemView) {
        super(itemView);
        titleView = itemView.findViewById(R.id.PostTitle);
        WriterNameView = itemView.findViewById(R.id.WriterName);
        WriteDayView = itemView.findViewById(R.id.WriteDay);

    }
}