package me.liuzs.cabinetmanager.ui.standingbook;

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

import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.StandingBookActivity;
import me.liuzs.cabinetmanager.model.DepositRecord;
import me.liuzs.cabinetmanager.model.DictType;
import me.liuzs.cabinetmanager.model.StandingBookItem;

public class StandingBookListAdapter extends RecyclerView.Adapter<StandingBookItemViewHolder> {

    private final List<DepositRecord> mResultList = new LinkedList<>();
    private boolean isShowMore = false;
    private int mTotalCount = 0;
    private StandingBookActivity mActivity;

    public StandingBookListAdapter(StandingBookActivity activity) {
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
    public StandingBookItemViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_standing_book, parent, false);
        return new StandingBookItemViewHolder(itemView);
    }

    public void addResult(List<DepositRecord> item, int totalCount) {
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

    public void addResult(StandingBookItem item, int totalCount) {
        mResultList.add(item);
        updateTotalCountAndMoreButton(totalCount);
        notifyDataSetChanged();
    }

    public void setResult(List<StandingBookItem> items, int totalCount) {
        mResultList.clear();
        mResultList.addAll(items);
        updateTotalCountAndMoreButton(totalCount);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull StandingBookItemViewHolder holder, int position) {
        if (isShowMore && position == getItemCount() - 1) {
            holder.activity = mActivity;
            holder.depositItem = null;
            holder.more.setVisibility(View.VISIBLE);
            holder.itemInfo.setVisibility(View.GONE);
            holder.itemView.setBackgroundResource(0);
        } else {
            holder.activity = mActivity;
            StandingBookItem item = mResultList.get(position);
            holder.depositItem = item;
            if (item.chemicalName != null) {
                holder.chemicalName.setText(item.chemicalName);
            } else {
                holder.chemicalName.setText(item.chineseName);
            }
            holder.containerNo.setText(item.conNo);
            if (item.time != null) {
                holder.createTime.setText(item.time);
            } else {
                holder.createTime.setText(item.createTime);
            }

            if (item.devName != null) {
                holder.deviceName.setText(item.devName);
            } else {
                holder.deviceName.setText("未明确存储位置");
            }

            String weight = "0g";
            if (item.weight != null) {
                weight = item.weight;
            } else if (item.outWeight != null) {
                weight = item.outWeight;
            } else if (item.putWeight != null) {
                weight = item.putWeight;
            }
            holder.weight.setText(weight + "g");
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
