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
import me.liuzs.cabinetmanager.model.ContainerNoBatchInfo;

public class ContainerBatchListAdapter extends RecyclerView.Adapter<ContainerBatchItemViewHolder> {

    private final List<ContainerNoBatchInfo> mResultList = new LinkedList<>();
    private ContainerNoManagementActivity mActivity;

    public ContainerBatchListAdapter(ContainerNoManagementActivity activity) {
        super();
        mActivity = activity;
    }

    @NonNull
    @NotNull
    @Override
    public ContainerBatchItemViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_container_batch_info, parent, false);
        return new ContainerBatchItemViewHolder(itemView);
    }

    public void setResult(List<ContainerNoBatchInfo> items) {
        mResultList.clear();
        mResultList.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ContainerBatchItemViewHolder holder, int position) {
        if (position != 0) {
            ContainerNoBatchInfo item = mResultList.get(position - 1);
            holder.containerNoBatchInfo = item;
        }
        holder.show();
    }

    @Override
    public int getItemCount() {
        return mResultList.size() + 1;
    }
}
