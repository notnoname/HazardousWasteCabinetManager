package me.liuzs.cabinetmanager.ui.containernobatch;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import me.liuzs.cabinetmanager.ContainerNoListActivity;
import me.liuzs.cabinetmanager.ContainerNoManagementActivity;
import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.model.ContainerNoBatchInfo;

public class ContainerBatchItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView batchId, batchName, noCount, org, createTime, operator;
    public ContainerNoBatchInfo containerNoBatchInfo;
    public ContainerNoManagementActivity activity;

    public ContainerBatchItemViewHolder(@NonNull View itemView, ContainerNoManagementActivity activity) {
        super(itemView);
        itemView.setOnClickListener(this);
        batchId = itemView.findViewById(R.id.tvId);
        batchName = itemView.findViewById(R.id.tvBatchName);
        noCount = itemView.findViewById(R.id.tvNoCount);
        org = itemView.findViewById(R.id.tvOrg);
        createTime = itemView.findViewById(R.id.tvCreateTime);
        operator = itemView.findViewById(R.id.tvOperator);
        this.activity = activity;
    }

    public void show() {
        if (containerNoBatchInfo != null) {
            batchId.setText(containerNoBatchInfo.id);
            batchName.setText(containerNoBatchInfo.batch_name);
            noCount.setText(containerNoBatchInfo.amount);
            org.setText(containerNoBatchInfo.org);
            createTime.setText(containerNoBatchInfo.create_time);
            operator.setText(containerNoBatchInfo.creator);
            batchId.setBackgroundResource(R.drawable.background_grid_content);
            batchName.setBackgroundResource(R.drawable.background_grid_content);
            noCount.setBackgroundResource(R.drawable.background_grid_content);
            org.setBackgroundResource(R.drawable.background_grid_content);
            createTime.setBackgroundResource(R.drawable.background_grid_content);
            operator.setBackgroundResource(R.drawable.background_grid_content);
        } else {
            batchId.setText("??????ID");
            batchName.setText("?????????");
            noCount.setText("????????????");
            org.setText("????????????");
            createTime.setText("????????????");
            operator.setText("????????????");
            batchId.setBackgroundResource(R.drawable.background_grid_title);
            batchName.setBackgroundResource(R.drawable.background_grid_title);
            noCount.setBackgroundResource(R.drawable.background_grid_title);
            org.setBackgroundResource(R.drawable.background_grid_title);
            createTime.setBackgroundResource(R.drawable.background_grid_title);
            operator.setBackgroundResource(R.drawable.background_grid_title);
        }

    }

    @Override
    public void onClick(View view) {
        if(containerNoBatchInfo != null) {
            ContainerNoListActivity.start(activity, containerNoBatchInfo.id, containerNoBatchInfo.batch_name);
        }
    }
}