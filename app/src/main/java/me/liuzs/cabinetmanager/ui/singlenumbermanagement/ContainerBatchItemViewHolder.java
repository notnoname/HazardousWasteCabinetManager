package me.liuzs.cabinetmanager.ui.singlenumbermanagement;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.model.ContainerNoBatchInfo;

public class ContainerBatchItemViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "ContainerBatchItemViewHolder";
    TextView batchId, batchName, noCount, org, createTime, operator;
    public ContainerNoBatchInfo containerNoBatchInfo;

    public ContainerBatchItemViewHolder(@NonNull View itemView) {
        super(itemView);
        batchId = itemView.findViewById(R.id.tvId);
        batchName = itemView.findViewById(R.id.tvBatchName);
        noCount = itemView.findViewById(R.id.tvNoCount);
        org = itemView.findViewById(R.id.tvOrg);
        createTime = itemView.findViewById(R.id.tvCreateTime);
        operator = itemView.findViewById(R.id.tvOperator);
    }

    public void show() {
        if (containerNoBatchInfo != null) {
            batchId.setText(containerNoBatchInfo.id);
            batchName.setText(containerNoBatchInfo.name);
            noCount.setText(containerNoBatchInfo.count);
            org.setText(containerNoBatchInfo.agencyName);
            createTime.setText(containerNoBatchInfo.createTime);
            operator.setText(containerNoBatchInfo.userName);
            batchId.setBackgroundResource(R.drawable.background_grid_content);
            batchName.setBackgroundResource(R.drawable.background_grid_content);
            noCount.setBackgroundResource(R.drawable.background_grid_content);
            org.setBackgroundResource(R.drawable.background_grid_content);
            createTime.setBackgroundResource(R.drawable.background_grid_content);
            operator.setBackgroundResource(R.drawable.background_grid_content);
        } else {
            batchId.setText("批次ID");
            batchName.setText("批次名");
            noCount.setText("单号数量");
            org.setText("所属机构");
            createTime.setText("创建时间");
            operator.setText("创建人员");
            batchId.setBackgroundResource(R.drawable.background_grid_title);
            batchName.setBackgroundResource(R.drawable.background_grid_title);
            noCount.setBackgroundResource(R.drawable.background_grid_title);
            org.setBackgroundResource(R.drawable.background_grid_title);
            createTime.setBackgroundResource(R.drawable.background_grid_title);
            operator.setBackgroundResource(R.drawable.background_grid_title);
        }

    }
}