package me.liuzs.cabinetmanager.ui.returnafteruse;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.ReturnAfterUseActivity;
import me.liuzs.cabinetmanager.model.UsageItemInfo;

public class ReturnItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    TextView containerNo, useWeight, createTime;
    ImageButton add;
    View usageInfo;
    UsageItemInfo item;
    ReturnAfterUseActivity activity;
    ReturnListFragment fragment;


    public ReturnItemViewHolder(@NonNull View itemView) {
        super(itemView);
        add = itemView.findViewById(R.id.btnAdd);
        add.setOnClickListener(this);
        usageInfo = itemView.findViewById(R.id.csDetailInfo);
        usageInfo.setOnClickListener(this);
        containerNo = itemView.findViewById(R.id.tvConNoValue);
        useWeight = itemView.findViewById(R.id.tvUseWeightValue);
        createTime = itemView.findViewById(R.id.tvCreateTimeValue);
    }

    @Override
    public void onClick(View v) {
        if (v == add) {
            activity.transToReturnItemCreateFragment();
        } else if (v == usageInfo) {
//            activity.transToReturnItemCreateFragment(item);
        }
    }
}