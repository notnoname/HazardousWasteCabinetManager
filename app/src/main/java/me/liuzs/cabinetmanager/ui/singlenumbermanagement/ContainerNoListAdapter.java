package me.liuzs.cabinetmanager.ui.singlenumbermanagement;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.ContainerNoManagementActivity;
import me.liuzs.cabinetmanager.model.SubBoardStatusInfo;

public class ContainerNoListAdapter extends RecyclerView.Adapter<SubBoardStatusItemViewHolder> {

    private final List<SubBoardStatusInfo> mResultList = new LinkedList<>();
    private ContainerNoManagementActivity mActivity;

    public ContainerNoListAdapter(ContainerNoManagementActivity activity) {
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
