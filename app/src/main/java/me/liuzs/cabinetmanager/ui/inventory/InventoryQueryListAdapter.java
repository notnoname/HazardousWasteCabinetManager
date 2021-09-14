package me.liuzs.cabinetmanager.ui.inventory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import me.liuzs.cabinetmanager.InventoryQueryActivity;
import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.model.InventoryItem;

public class InventoryQueryListAdapter extends RecyclerView.Adapter<InventoryItemViewHolder> {

    private boolean isShowMore = false;
    private List<InventoryItem> mResultList = new LinkedList<>();
    private int mTotalCount = 0;
    private InventoryQueryActivity mActivity;

    public InventoryQueryListAdapter(InventoryQueryActivity activity) {
        super();
        mActivity = activity;
    }

    public boolean isShowMore() {
        return isShowMore;
    }

    public void setShowMore(boolean isShowMore) {
        this.isShowMore = isShowMore;
        notifyDataSetChanged();
    }

    @NonNull
    @NotNull
    @Override
    public InventoryItemViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_inventory_search, parent, false);
        return new InventoryItemViewHolder(itemView);
    }

    public void addResult(List<InventoryItem> item, int totalCount) {
        mResultList.addAll(item);
        updateTotalCountAndMoreButton(totalCount);
        notifyDataSetChanged();
    }

    private void updateTotalCountAndMoreButton(int totalCount) {
        mTotalCount = totalCount;
        if (mResultList.size() < totalCount) {
            isShowMore = true;
        } else {
            isShowMore = false;
        }
    }

    public void addResult(InventoryItem item, int totalCount) {
        mResultList.add(item);
        updateTotalCountAndMoreButton(totalCount);
        notifyDataSetChanged();
    }

    public void setResult(List<InventoryItem> items, int totalCount) {
        mResultList.clear();
        mResultList.addAll(items);
        updateTotalCountAndMoreButton(totalCount);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull InventoryItemViewHolder holder, int position) {
        if (isShowMore && position == getItemCount() - 1) {
            holder.activity = mActivity;
            holder.inventoryItem = null;
            holder.more.setVisibility(View.VISIBLE);
            holder.itemInfo.setVisibility(View.GONE);
            holder.itemView.setBackgroundResource(0);
        } else {
            holder.activity = mActivity;
            InventoryItem item = mResultList.get(position);
            holder.inventoryItem = item;
            holder.chemicalName.setText(item.chemicalName);
            holder.count.setText("数量：" + item.totalCount);
            holder.weight.setText("重量：" + item.totalWeight);
            holder.more.setVisibility(View.GONE);
            holder.itemInfo.setVisibility(View.VISIBLE);
            holder.itemView.setBackgroundResource(R.drawable.background_info_area);
        }
    }

    @Override
    public int getItemCount() {
        if (isShowMore) {
            return mResultList.size() + 1;
        } else {
            return mResultList.size();
        }

    }
}
