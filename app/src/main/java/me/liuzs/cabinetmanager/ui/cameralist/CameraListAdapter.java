package me.liuzs.cabinetmanager.ui.cameralist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import me.liuzs.cabinetmanager.CameraListActivity;
import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.model.SurveillanceCamera;

public class CameraListAdapter extends RecyclerView.Adapter<CameraItemViewHolder> {

    private List<SurveillanceCamera> mResultList;
    private CameraListActivity mActivity;

    public CameraListAdapter(CameraListActivity activity) {
        super();
        mActivity = activity;
    }

    @NonNull
    @NotNull
    @Override
    public CameraItemViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_camera, parent, false);
        return new CameraItemViewHolder(itemView);
    }

    public void setResult(List<SurveillanceCamera> items) {
        mResultList = items;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull CameraItemViewHolder holder, int position) {
        SurveillanceCamera item = mResultList.get(position);
        holder.camera = item;
        holder.show();
    }

    @Override
    public int getItemCount() {
        return mResultList.size();
    }
}
