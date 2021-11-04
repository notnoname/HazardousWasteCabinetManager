package me.liuzs.cabinetmanager.ui.standingbook;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.StandingBookActivity;
import me.liuzs.cabinetmanager.model.DepositRecord;

public class StandingBookListAdapter extends RecyclerView.Adapter<StandingBookItemViewHolder> {

    private final List<DepositRecord> mResultList = new LinkedList<>();

    public StandingBookListAdapter() {
        super();
    }

    @NonNull
    @NotNull
    @Override
    public StandingBookItemViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_standing_book, parent, false);
        return new StandingBookItemViewHolder(itemView);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clear() {
        mResultList.clear();
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setResult(List<DepositRecord> data) {
        mResultList.clear();
        mResultList.addAll(data);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addResult(List<DepositRecord> data) {
        mResultList.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull StandingBookItemViewHolder holder, int position) {
        if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#40000000"));
        }
        if (position != 0) {
            holder.depositRecord = mResultList.get(position - 1);
            holder.show(false);
        } else {
            holder.depositRecord = null;
            holder.show(true);
        }
    }

    @Override
    public int getItemCount() {
        return mResultList.size() + 1;
    }
}
