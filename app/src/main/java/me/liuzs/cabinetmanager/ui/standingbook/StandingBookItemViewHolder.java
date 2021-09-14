package me.liuzs.cabinetmanager.ui.standingbook;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.StandingBookActivity;
import me.liuzs.cabinetmanager.model.StandingBookItem;

public class StandingBookItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    TextView chemicalName, containerNo, weight, createTime, deviceName;
    StandingBookItem depositItem;
    StandingBookActivity activity;
    Button more;
    ConstraintLayout itemInfo;


    public StandingBookItemViewHolder(@NonNull View itemView) {
        super(itemView);
        itemInfo = itemView.findViewById(R.id.csDetailInfo);
        chemicalName = itemView.findViewById(R.id.tvChemicalNameValue);
        containerNo = itemView.findViewById(R.id.tvConNoValue);
        deviceName = itemView.findViewById(R.id.tvDeviceName);
        createTime = itemView.findViewById(R.id.tvCreateTimeValue);
        weight = itemView.findViewById(R.id.tvWeightValue);
        more = itemView.findViewById(R.id.btnMore);
        more.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == more) {
            activity.onMoreButtonClick(v);
        }
    }
}