package me.liuzs.cabinetmanager.ui.subboardinfo;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.model.SetupValue;
import me.liuzs.cabinetmanager.model.SubBoardStatusInfo;

public class SubBoardStatusItemViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "SubBoardStatusItemViewHolder";
    TextView name, temp, humi, tvoc, lock, fan;
    ImageView lockStatus, fanStatus;
    SubBoardStatusInfo subBoardStatus;
    public static SetupValue setup;

    public SubBoardStatusItemViewHolder(@NonNull View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.tvCabinetNameValue);
        temp = itemView.findViewById(R.id.tvTemperatureValue);
        humi = itemView.findViewById(R.id.tvHumidityValue);
        tvoc = itemView.findViewById(R.id.tvTVOCs1Value);
        lock = itemView.findViewById(R.id.tvLockerStatus);
        fan = itemView.findViewById(R.id.tvFanStatus);
        lockStatus = itemView.findViewById(R.id.ivACCtrlModelValue);
        fanStatus = itemView.findViewById(R.id.ivFanStatusValue);
    }

    public void show() {
        name.setText(subBoardStatus.name);
        if (subBoardStatus.statusData != null) {
            temp.setText(String.valueOf(subBoardStatus.statusData.temp / 10f));
            humi.setText(String.valueOf(subBoardStatus.statusData.humi / 10f));
            tvoc.setText(String.valueOf(subBoardStatus.statusData.concentration_ugpm3 / 10f));
            if (setup != null) {
                long thresholdTemp = (long) setup.tempHighAlertThreshold;
                if (subBoardStatus.statusData.temp / 10f > thresholdTemp) {
                    temp.setBackgroundResource(R.drawable.background_corner_orange);
                } else {
                    temp.setBackgroundResource(R.drawable.background_corner_white);
                }
                float thresholdPPM = setup.tempHighAlertThreshold;
                if (subBoardStatus.statusData.concentration_ugpm3 / 10f > thresholdPPM) {
                    tvoc.setBackgroundResource(R.drawable.background_corner_orange);
                } else {
                    tvoc.setBackgroundResource(R.drawable.background_corner_white);
                }
            } else {
                temp.setBackgroundResource(R.drawable.background_corner_white);
                tvoc.setBackgroundResource(R.drawable.background_corner_white);
            }
            if (subBoardStatus.statusData.door_lock == 1) {
                lockStatus.setImageResource(R.drawable.ic_red_circle);
                lock.setText("关闭");
            } else {
                lockStatus.setImageResource(R.drawable.ic_green_circle);
                lock.setText("打开");
            }
            fanStatus.setImageResource(R.drawable.ic_green_circle);
            fan.setText("运行");
        } else {
            temp.setBackgroundResource(R.drawable.background_corner_white);
            tvoc.setBackgroundResource(R.drawable.background_corner_white);
            temp.setText("");
            humi.setText("");
            tvoc.setText("");
            lockStatus.setImageResource(R.drawable.ic_red_circle);
            lock.setText("未知");
            fanStatus.setImageResource(R.drawable.ic_red_circle);
            fan.setText("未知");
        }
    }
}