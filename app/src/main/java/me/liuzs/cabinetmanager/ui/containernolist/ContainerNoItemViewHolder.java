package me.liuzs.cabinetmanager.ui.containernolist;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import me.liuzs.cabinetmanager.ContainerNoListActivity;
import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.model.ContainerNoInfo;

public class ContainerNoItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView no, batchId, batchName, org, createTime, operator;
    public ContainerNoInfo containerNoInfo;
    public ContainerNoListActivity activity;

    public ContainerNoItemViewHolder(@NonNull View itemView, ContainerNoListActivity activity) {
        super(itemView);
        itemView.setOnClickListener(this);
        no = itemView.findViewById(R.id.tvNO);
        batchId = itemView.findViewById(R.id.tvBatchId);
        batchName = itemView.findViewById(R.id.tvBatchName);
        org = itemView.findViewById(R.id.tvOrg);
        createTime = itemView.findViewById(R.id.tvCreateTime);
        operator = itemView.findViewById(R.id.tvOperator);
        this.activity = activity;
    }

    public void show() {
        if (containerNoInfo != null) {
            no.setText(containerNoInfo.no);
            batchId.setText(containerNoInfo.batch_id);
            batchName.setText(containerNoInfo.batch_name);
            org.setText(containerNoInfo.agency_name);
            createTime.setText(containerNoInfo.create_time);
            operator.setText(containerNoInfo.operator);
            no.setBackgroundResource(R.drawable.background_grid_content);
            batchId.setBackgroundResource(R.drawable.background_grid_content);
            batchName.setBackgroundResource(R.drawable.background_grid_content);
            org.setBackgroundResource(R.drawable.background_grid_content);
            createTime.setBackgroundResource(R.drawable.background_grid_content);
            operator.setBackgroundResource(R.drawable.background_grid_content);
        } else {
            no.setText("单号");
            batchId.setText("批次ID");
            batchName.setText("批次名");
            org.setText("所属机构");
            createTime.setText("创建时间");
            operator.setText("创建人员");
            no.setBackgroundResource(R.drawable.background_grid_title);
            batchId.setBackgroundResource(R.drawable.background_grid_title);
            batchName.setBackgroundResource(R.drawable.background_grid_title);
            org.setBackgroundResource(R.drawable.background_grid_title);
            createTime.setBackgroundResource(R.drawable.background_grid_title);
            operator.setBackgroundResource(R.drawable.background_grid_title);
        }

    }

    @Override
    public void onClick(View view) {
        if(containerNoInfo != null) {

        }
    }
}