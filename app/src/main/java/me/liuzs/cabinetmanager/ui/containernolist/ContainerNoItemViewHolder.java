package me.liuzs.cabinetmanager.ui.containernolist;

import android.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;

import me.liuzs.cabinetmanager.ContainerNoListActivity;
import me.liuzs.cabinetmanager.PrintActivity;
import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.model.ContainerNoInfo;

public class ContainerNoItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView no, batchName, org, createTime, operator, isUsed;
    public ContainerNoInfo containerNoInfo;
    public ContainerNoListActivity activity;

    public ContainerNoItemViewHolder(@NonNull View itemView, ContainerNoListActivity activity) {
        super(itemView);
        itemView.setOnClickListener(this);
        no = itemView.findViewById(R.id.tvNO);
        batchName = itemView.findViewById(R.id.tvBatchName);
        org = itemView.findViewById(R.id.tvOrg);
        createTime = itemView.findViewById(R.id.tvCreateTime);
        operator = itemView.findViewById(R.id.tvOperator);
        isUsed = itemView.findViewById(R.id.tvUsed);
        this.activity = activity;
    }

    public void show() {
        if (containerNoInfo != null) {
            no.setText(containerNoInfo.no);
            batchName.setText(containerNoInfo.batch_name);
            org.setText(containerNoInfo.org);
            createTime.setText(containerNoInfo.create_time);
            operator.setText(containerNoInfo.creator);
            isUsed.setText(containerNoInfo.state == 1 ? "是" : "否");
            no.setBackgroundResource(R.drawable.background_grid_content);
            batchName.setBackgroundResource(R.drawable.background_grid_content);
            org.setBackgroundResource(R.drawable.background_grid_content);
            createTime.setBackgroundResource(R.drawable.background_grid_content);
            operator.setBackgroundResource(R.drawable.background_grid_content);
            isUsed.setBackgroundResource(R.drawable.background_grid_content);
        } else {
            no.setText("单号");
            batchName.setText("批次名");
            org.setText("所属机构");
            createTime.setText("创建时间");
            operator.setText("创建人员");
            isUsed.setText("是否使用");
            no.setBackgroundResource(R.drawable.background_grid_title);
            batchName.setBackgroundResource(R.drawable.background_grid_title);
            org.setBackgroundResource(R.drawable.background_grid_title);
            createTime.setBackgroundResource(R.drawable.background_grid_title);
            operator.setBackgroundResource(R.drawable.background_grid_title);
            isUsed.setBackgroundResource(R.drawable.background_grid_title);
        }

    }

    @Override
    public void onClick(View view) {
        if (containerNoInfo != null) {
            new AlertDialog.Builder(activity).setMessage("是否打印此单号？").setNegativeButton("确认", (dialog, which) -> {
                LinkedList<ContainerNoInfo> list = new LinkedList<>();
                list.add(containerNoInfo);
                PrintActivity.startPrintContainerLabel(activity, list);
            }).setNeutralButton("取消", null).show();
        }
    }
}