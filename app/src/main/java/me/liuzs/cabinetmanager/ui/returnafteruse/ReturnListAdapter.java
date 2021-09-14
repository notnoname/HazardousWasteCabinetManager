package me.liuzs.cabinetmanager.ui.returnafteruse;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.ReturnAfterUseActivity;
import me.liuzs.cabinetmanager.model.UsageItemInfo;

public class ReturnListAdapter extends RecyclerView.Adapter<ReturnItemViewHolder> {

    private List<UsageItemInfo> mItems = new LinkedList<>();

    private ReturnAfterUseActivity mActivity;

    private ReturnListFragment mFragment;

    public ReturnListAdapter(ReturnAfterUseActivity activity, ReturnListFragment fragment) {
        super();
        mActivity = activity;
        mFragment = fragment;
    }

    @NonNull
    @NotNull
    @Override
    public ReturnItemViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_usage, parent, false);
        return new ReturnItemViewHolder(itemView);
    }

    public void notifyItemsChanged() {
        mItems.clear();
        mItems.addAll(mActivity.getUsageInfo().items);
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
    public void onBindViewHolder(@NonNull @NotNull ReturnItemViewHolder holder, int position) {

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
                holder.usageInfo.setVisibility(View.GONE);
                break;
            }
            case 1: {
                holder.item = mItems.get(position);
                mActivity.setDicTypeLabel(holder.item);
                holder.add.setVisibility(View.GONE);
                holder.usageInfo.setVisibility(View.VISIBLE);
                UsageItemInfo record = mItems.get(position);
                holder.containerNo.setText(record.conNo);
                holder.useWeight.setText("入柜 " + record.putWeight + "g");
                holder.createTime.setText(record.createTime);
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size() + 1;
    }
}
