package com.example.chargemonitor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class InformationAdapter extends RecyclerView.Adapter<InformationAdapter.InformationViewHolder> {
    private final ArrayList<Information> data;
    private final Context context;

    public static class InformationViewHolder extends  RecyclerView.ViewHolder {
        public InformationViewHolder(@NonNull View itemView) { super(itemView); }
    }

    public InformationAdapter(ArrayList<Information> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @NonNull
    @Override
    public InformationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new InformationViewHolder(LayoutInflater.from(context).inflate(R.layout.information_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull InformationViewHolder holder, int position) {
        View itemView = holder.itemView;
        Information information = data.get(position);

        TextView textView = itemView.findViewById(R.id.description);
        textView.setText(information.getDescription());

        textView = itemView.findViewById(R.id.unit);
        textView.setText(information.getUnit());

        textView = itemView.findViewById(R.id.value);
        textView.setText(String.valueOf(information.getValue()));
    }

    @Override
    public int getItemCount() { return data.size(); }
}
