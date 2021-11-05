package me.liuzs.cabinetmanager.ui.log;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import me.liuzs.cabinetmanager.R;

public class LogItemViewHolder extends RecyclerView.ViewHolder {
    public long id;
    public String log;
    public TextView logView;
    public LogItemViewHolder(@NonNull View itemView) {
        super(itemView);
        logView = itemView.findViewById(R.id.logRow);
    }

    public void show(){
        logView.setText(log);
    }

}
