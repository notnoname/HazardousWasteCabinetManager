package me.liuzs.cabinetmanager.ui.storage;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import me.liuzs.cabinetmanager.CabinetApplication;
import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.StorageActivity;
import me.liuzs.cabinetmanager.model.DepositItem;
import me.liuzs.cabinetmanager.model.DeviceInfo;

public class DepositRecordAdapter extends RecyclerView.Adapter<DepositRecordViewHolder> {

    private List<DepositItem> mItems = new LinkedList<>();

    private StorageActivity mActivity;

    private FirstDepositFragment mFragment;

    public DepositRecordAdapter(StorageActivity activity, FirstDepositFragment fragment) {
        super();
        mActivity = activity;
        mFragment = fragment;
    }

    @NonNull
    @NotNull
    @Override
    public DepositRecordViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_deposit_record, parent, false);
        return new DepositRecordViewHolder(itemView);
    }

    public void notifyItemsChanged() {
        mItems.clear();
        mItems.addAll(mActivity.getDepositRecord().items);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull DepositRecordViewHolder holder, int position) {

        holder.activity = mActivity;
        holder.fragment = mFragment;
        int i = position % 2;
        if (i == 0) {
            holder.itemView.setBackgroundColor(Color.parseColor("#00000000"));
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#30000000"));
        }

        int type = getItemViewType(position);
        switch (type) {
            case 0: {
                holder.item = null;
                holder.add.setVisibility(View.VISIBLE);
                holder.depositNoInfo.setVisibility(View.GONE);
                holder.depositInfo.setVisibility(View.GONE);
                break;
            }
            case 1: {
                holder.item = mItems.get(position);
                holder.add.setVisibility(View.GONE);
                holder.depositNoInfo.setVisibility(View.VISIBLE);
                holder.depositInfo.setVisibility(View.VISIBLE);
                DepositItem record = mItems.get(position);
                holder.depositNo.setText("试剂" + (position + 1));
                holder.containerNo.setText(record.conNo);
                holder.chemicalName.setText(record.chemicalName);
//                DeviceInfo info = CabinetApplication.getInstance().getDeviceInfoById(record.devId);
//                if (info != null) {
//                    record.devName = info.devName;
//                }
                holder.deviceName.setText(record.devName);
                //holder.weight.setText(record.specification + (record.measureSpec != null ? record.measureSpec : ""));
                holder.weight.setText(record.weight + "g");
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size() + 1;
    }
}
