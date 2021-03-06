package me.liuzs.cabinetmanager.ui.offline;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.StandingBookActivity;
import me.liuzs.cabinetmanager.model.DepositRecord;

public class OfflineDepositRecordItemViewHolder extends RecyclerView.ViewHolder {

    TextView containerNo, containerSpec, source, harmful, shelfNo, inWeight, outWeight, inTime, outTime, inOpt, outOpt, otherInfo;
    DepositRecord depositRecord;
    StandingBookActivity activity;
    ConstraintLayout row;


    public OfflineDepositRecordItemViewHolder(@NonNull View itemView) {
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
            containerNo.setText("??????");
            containerSpec.setText("????????????(???)");
            source.setText("????????????");
            harmful.setText("????????????");
            shelfNo.setText("?????????");
            inWeight.setText("????????????(KG)");
            outWeight.setText("????????????(KG)");
            inTime.setText("????????????");
            outTime.setText("????????????");
            inOpt.setText("????????????");
            outOpt.setText("????????????");
            otherInfo.setText("????????????");
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