package me.liuzs.cabinetmanager.ui.takeout;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.TakeOutActivity;
import me.liuzs.cabinetmanager.model.TakeOutItemInfo;

public class TakeOutItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    TextView containerNo, outType, outWeight, createTime;
    ImageButton add;
    View takeOutItemInfo;
    TakeOutItemInfo item;
    TakeOutActivity activity;
    TakeOutListFragment fragment;


    public TakeOutItemViewHolder(@NonNull View itemView) {
        super(itemView);
        add = itemView.findViewById(R.id.btnAdd);
        add.setOnClickListener(this);
        takeOutItemInfo = itemView.findViewById(R.id.csDetailInfo);
        takeOutItemInfo.setOnClickListener(this);
        containerNo = itemView.findViewById(R.id.tvConNoValue);
        outType = itemView.findViewById(R.id.tvControlValue);
        outWeight = itemView.findViewById(R.id.tvOutWeightValue);
        createTime = itemView.findViewById(R.id.tvCreateTimeValue);
    }

    @Override
    public void onClick(View v) {
        if (v == add) {
            activity.transToTakeOutItemCreateFragment();
        }
//        else if (v == takeOutItemInfo) {
//            activity.transToTakeOutItemCreateFragment(item);
//        }
    }
}