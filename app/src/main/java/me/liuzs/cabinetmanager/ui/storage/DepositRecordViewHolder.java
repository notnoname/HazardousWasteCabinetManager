package me.liuzs.cabinetmanager.ui.storage;

import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import me.liuzs.cabinetmanager.PrintActivity;
import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.StorageActivity;
import me.liuzs.cabinetmanager.model.DepositItem;

public class DepositRecordViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    TextView containerNo, chemicalName, deviceName, weight, depositNo;
    ImageButton add, delete, print;
    LinearLayout depositInfo;
    DepositItem item;
    StorageActivity activity;
    ConstraintLayout depositNoInfo;
    FirstDepositFragment fragment;


    public DepositRecordViewHolder(@NonNull View itemView) {
        super(itemView);
        print = itemView.findViewById(R.id.btnPrint);
        print.setOnClickListener(this);
        add = itemView.findViewById(R.id.btnAdd);
        add.setOnClickListener(this);
        delete = itemView.findViewById(R.id.btnDelete);
        delete.setOnClickListener(this);
        depositNoInfo = itemView.findViewById(R.id.csTitle);
        depositNo = itemView.findViewById(R.id.tvDepositNo);
        depositInfo = itemView.findViewById(R.id.llDepositInfo);
        depositInfo.setOnClickListener(this);
        containerNo = itemView.findViewById(R.id.tvConId);
        chemicalName = itemView.findViewById(R.id.tvChemRecordName);
        deviceName = itemView.findViewById(R.id.tvDeviceName);
        weight = itemView.findViewById(R.id.tvConWeight);
    }

    @Override
    public void onClick(View v) {
        if (v == add) {
            activity.transToDepositItemCreateFragment(null);
        } else if (v == depositInfo) {
            activity.transToDepositItemCreateFragment(item);
        } else if (v == delete) {
            fragment.deleteItem(item);
        } else if (v == print) {
            PrintActivity.ContainerLabel label = new PrintActivity.ContainerLabel();
            label.containerNo = item.conNo;
            label.controlType = item.controlType;
            label.chemicalCASNO = item.casNo;
            label.chemicalName = item.chemicalName;
            PrintActivity.startPrintContainerLabel(activity, label);
        }
    }
}