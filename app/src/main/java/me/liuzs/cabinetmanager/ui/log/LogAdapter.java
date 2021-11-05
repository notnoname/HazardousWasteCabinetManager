package me.liuzs.cabinetmanager.ui.log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;
import java.util.List;

import me.liuzs.cabinetmanager.R;

public class LogAdapter extends RecyclerView.Adapter<LogItemViewHolder> {

    private final List<String> mLogList = new LinkedList<>();

    @Override
    public void onBindViewHolder(LogItemViewHolder viewHolder, int position) {
        viewHolder.log = mLogList.get(position);
        viewHolder.show();
    }

    @NonNull
    @Override
    public LogItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item_log, parent, false);
        return new LogItemViewHolder(itemView);
    }

    public void addLog(List<String> logList) {
        int size = mLogList.size();
        mLogList.addAll(logList);
        notifyItemChanged(size);
    }

    public void clear() {
        int size = mLogList.size();
        mLogList.clear();
        notifyItemRangeRemoved(0, size);
    }

    @Override
    public int getItemCount() {
        return mLogList.size();
    }
}