package me.liuzs.cabinetmanager.ui.cameralist;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.VideoPlayerActivity;
import me.liuzs.cabinetmanager.model.SurveillanceCamera;

public class CameraItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private static final String TAG = "CameraItemViewHolder";
    TextView labName, status;
    Button watch;
    ImageView logo;
    SurveillanceCamera camera;

    public CameraItemViewHolder(@NonNull View itemView) {
        super(itemView);
        labName = itemView.findViewById(R.id.tvLabName);
        status = itemView.findViewById(R.id.tvStatus);
        watch = itemView.findViewById(R.id.btnWatch);
        watch.setOnClickListener(this);
    }

    public void show() {
        labName.setText(camera.videoName);
        status.setText(camera.status == 1 ? "在线" : "未知");
    }

    @Override
    public void onClick(View view) {
        VideoPlayerActivity.start(camera, itemView.getContext());
    }
}