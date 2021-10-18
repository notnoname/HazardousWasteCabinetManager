package me.liuzs.cabinetmanager.ui.controlpanel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import me.liuzs.cabinetmanager.ControlPanelActivity;
import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.model.LockerStatus;

public class LockerListAdapter extends RecyclerView.Adapter<LockerItemViewHolder> {

    private List<LockerStatus> mResultList;
    private ControlPanelActivity mActivity;

    public LockerListAdapter(ControlPanelActivity activity) {
        super();
        mActivity = activity;
    }

    @NonNull
    @NotNull
    @Override
    public LockerItemViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_locker, parent, false);
        return new LockerItemViewHolder(itemView);
    }

    public void setResult(List<LockerStatus> items) {
        mResultList = items;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull LockerItemViewHolder holder, int position) {
        holder.activity = mActivity;
        LockerStatus item = mResultList.get(position);
        holder.lockerStatus = item;
        holder.deviceName.setText(item.deviceName);
        switch (item.lock) {
            case Lock:
                holder.lock.setEnabled(false);
                holder.unlock.setEnabled(true);
                holder.status.setImageResource(R.drawable.ic_red_circle);
                break;
            case Unlock:
                holder.lock.setEnabled(true);
                holder.unlock.setEnabled(false);
                holder.status.setImageResource(R.drawable.ic_green_circle);
                break;
            case Unknown:
                holder.lock.setEnabled(false);
                holder.unlock.setEnabled(false);
                holder.status.setImageResource(R.drawable.ic_red_circle);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mResultList.size();
    }
}
