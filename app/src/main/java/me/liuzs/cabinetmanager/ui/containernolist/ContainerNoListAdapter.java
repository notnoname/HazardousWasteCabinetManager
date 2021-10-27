package me.liuzs.cabinetmanager.ui.containernolist;

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

import me.liuzs.cabinetmanager.ContainerNoListActivity;
import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.model.ContainerNoInfo;

public class ContainerNoListAdapter extends RecyclerView.Adapter<ContainerNoItemViewHolder> {

    private final List<ContainerNoInfo> mResultList = new LinkedList<>();

    private ContainerNoListActivity activity;

    public ContainerNoListAdapter(ContainerNoListActivity activity) {
        super();
        this.activity = activity;
    }

    @NonNull
    @NotNull
    @Override
    public ContainerNoItemViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_container_no_info, parent, false);
        return new ContainerNoItemViewHolder(itemView, activity);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void add(List<ContainerNoInfo> items) {
        mResultList.addAll(items);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clear() {
        mResultList.clear();
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ContainerNoItemViewHolder holder, int position) {
        if (position != 0) {
            holder.containerNoInfo = mResultList.get(position - 1);
        } else {
            holder.containerNoInfo = null;
        }
        if(position % 2 ==0) {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#40000000"));
        }
        holder.show();
    }

    @Override
    public int getItemCount() {
        return mResultList.size() + 1;
    }
}
