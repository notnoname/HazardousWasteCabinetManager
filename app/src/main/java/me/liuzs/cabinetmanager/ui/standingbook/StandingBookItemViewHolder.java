package me.liuzs.cabinetmanager.ui.standingbook;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.StandingBookActivity;
import me.liuzs.cabinetmanager.model.DepositRecord;

public class StandingBookItemViewHolder extends RecyclerView.ViewHolder {

    TextView containerNo, containerSpec, source, harmful, shelfNo, inWeight, outWeight, inTime, outTime, inOpt, outOpt, otherInfo;
    DepositRecord depositRecord;
    StandingBookActivity activity;
    ConstraintLayout row;


    public StandingBookItemViewHolder(@NonNull View itemView) {
        super(itemView);
        row = itemView.findViewById(R.id.csRow);
        containerNo = itemView.findViewById(R.id.tvNo);
        containerSpec = itemView.findViewById(R.id.tvSpec);
        source = itemView.findViewById(R.id.tvSource);
        harmful = itemView.findViewById(R.id.tvHarmful);
        shelfNo = itemView.findViewById(R.id.tvShelfNo);
        inWeight = itemView.findViewById(R.id.tvInWeight);
        outWeight = itemView.findViewById(R.id.tvOutWeight);
        inTime = itemView.findViewById(R.id.tvInTime);
        outTime = itemView.findViewById(R.id.tvOutTime);
        inOpt = itemView.findViewById(R.id.tvInOperator);
        outOpt = itemView.findViewById(R.id.tvOutOperator);
        otherInfo = itemView.findViewById(R.id.tvInfo);
    }

    private void setBackground(boolean isTitle) {
        for (int i = 0; i < row.getChildCount(); i++) {
            TextView view = (TextView) row.getChildAt(i);
            if (isTitle) {
                view.setBackgroundResource(R.drawable.background_grid_title);
            } else {
                view.setBackgroundResource(R.drawable.background_grid_content);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    public void show(boolean isTitle) {
        setBackground(isTitle);
        if (isTitle) {
            containerNo.setText("单号");
            containerSpec.setText("容器规格(升)");
            source.setText("危废来源");
            harmful.setText("有害成分");
            shelfNo.setText("货架号");
            inWeight.setText("入柜重量(KG)");
            outWeight.setText("出柜重量(KG)");
            inTime.setText("入柜时间");
            outTime.setText("出柜时间");
            inOpt.setText("入柜人员");
            outOpt.setText("出柜人员");
            otherInfo.setText("其他信息");
        } else {
            containerNo.setText(depositRecord.storage_no);
            containerSpec.setText(depositRecord.container_size);
            source.setText(depositRecord.laboratory);
            harmful.setText(depositRecord.harmful_infos);
            shelfNo.setText(depositRecord.storage_rack);
            inWeight.setText(depositRecord.input_weight);
            outWeight.setText(depositRecord.output_weight);
            inTime.setText(depositRecord.input_time);
            outTime.setText(depositRecord.output_time);
            inOpt.setText(depositRecord.input_operator);
            outOpt.setText(depositRecord.output_operator);
            otherInfo.setText(depositRecord.remark);
        }

    }
}