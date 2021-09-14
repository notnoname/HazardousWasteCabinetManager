package me.liuzs.cabinetmanager.ui.subboardinfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import me.liuxy.cabinet.SubBoard;
import me.liuzs.cabinetmanager.Config;
import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.SubBoardInfoActivity;
import me.liuzs.cabinetmanager.model.HardwareValue;
import me.liuzs.cabinetmanager.model.SubBoardStatusInfo;

public class SubBoardListAdapter extends RecyclerView.Adapter<SubBoardStatusItemViewHolder> {

    private final List<SubBoardStatusInfo> mResultList = new LinkedList<>();
    private SubBoardInfoActivity mActivity;

    public SubBoardListAdapter(SubBoardInfoActivity activity) {
        super();
        mActivity = activity;
    }

    @NonNull
    @NotNull
    @Override
    public SubBoardStatusItemViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_sub_board_info, parent, false);
        return new SubBoardStatusItemViewHolder(itemView);
    }

    public void setResult(List<SubBoardStatusInfo> items) {
        mResultList.clear();
        mResultList.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull SubBoardStatusItemViewHolder holder, int position) {
        SubBoardStatusInfo item = mResultList.get(position);
        holder.subBoardStatus = item;
        holder.show();
    }

    @Override
    public int getItemCount() {
        return mResultList.size();
    }
}
