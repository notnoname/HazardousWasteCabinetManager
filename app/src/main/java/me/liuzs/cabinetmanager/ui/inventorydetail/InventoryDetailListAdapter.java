package me.liuzs.cabinetmanager.ui.inventorydetail;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import me.liuzs.cabinetmanager.InventoryDetailActivity;
import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.model.DictType;
import me.liuzs.cabinetmanager.model.InventoryDetail;

public class InventoryDetailListAdapter extends RecyclerView.Adapter<InventoryDetailItemViewHolder> {

    private final Map<String, String> purityValues = new HashMap<>();
    private final Map<String, String> unitValues = new HashMap<>();
    private final Map<String, String> measureSpecValues = new HashMap<>();
    private boolean isShowMore = false;
    private List<InventoryDetail> mResultList = new LinkedList<>();
    private int mTotalCount = 0;
    private InventoryDetailActivity mActivity;

    public InventoryDetailListAdapter(InventoryDetailActivity activity) {
        super();
        mActivity = activity;
    }

    public boolean isSetDict() {
        return purityValues.size() > 0 || unitValues.size() > 0l || measureSpecValues.size() > 0;
    }

    private void setDict(Map<String, String> map, List<DictType> list) {
        for (DictType type : list) {
            map.put(type.value, type.label);
        }
    }

    public void setDict(List<DictType> purityDict, List<DictType> unitDict, List<DictType> measureSpec) {
        setDict(purityValues, purityDict);
        setDict(unitValues, unitDict);
        setDict(measureSpecValues, measureSpec);
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
    public InventoryDetailItemViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_inventory_detail, parent, false);
        return new InventoryDetailItemViewHolder(itemView);
    }

    public void addResult(List<InventoryDetail> item, int totalCount) {
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

    public void addResult(InventoryDetail item, int totalCount) {
        mResultList.add(item);
        updateTotalCountAndMoreButton(totalCount);
        notifyDataSetChanged();
    }

    public void setResult(List<InventoryDetail> items, int totalCount) {
        mResultList.clear();
        mResultList.addAll(items);
        updateTotalCountAndMoreButton(totalCount);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull InventoryDetailItemViewHolder holder, int position) {
        if (isShowMore && position == getItemCount() - 1) {
            holder.activity = mActivity;
            holder.inventoryDetail = null;
            holder.more.setVisibility(View.VISIBLE);
            holder.itemInfo.setVisibility(View.GONE);
            holder.itemView.setBackgroundResource(0);
        } else {
            holder.activity = mActivity;
            InventoryDetail item = mResultList.get(position);
            holder.inventoryDetail = item;
            holder.containerNo.setText(item.conNo);
            if (TextUtils.isEmpty(item.supplier)) {
                holder.supplier.setText("未明确供应商");
            } else {
                holder.supplier.setText(item.supplier);
            }

            holder.purity.setText(purityValues.get(item.purity));
            holder.packingSpec.setText(item.specification + "g/" + unitValues.get(item.unit));
            holder.weight.setText(item.weight + "g");
            if (TextUtils.isEmpty(item.devName)) {
                holder.deviceName.setText("未明确存储位置");
            } else {
                holder.deviceName.setText(item.devName);
            }
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
