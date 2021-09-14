package me.liuzs.cabinetmanager.ui.takeout;

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
import me.liuzs.cabinetmanager.TakeOutActivity;
import me.liuzs.cabinetmanager.model.TakeOutItemInfo;

public class TakeOutListAdapter extends RecyclerView.Adapter<TakeOutItemViewHolder> {

    private final List<TakeOutItemInfo> mItems = new LinkedList<>();

    private final TakeOutActivity mActivity;

    private final TakeOutListFragment mFragment;

    public TakeOutListAdapter(TakeOutActivity activity, TakeOutListFragment fragment) {
        super();
        mActivity = activity;
        mFragment = fragment;
    }

    @NonNull
    @Override
    public TakeOutItemViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_take_out, parent, false);
        return new TakeOutItemViewHolder(itemView);
    }

    public void notifyItemsChanged() {
        mItems.clear();
        mItems.addAll(mActivity.getTakeOutInfo().items);
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
    public void onBindViewHolder(@NonNull @NotNull TakeOutItemViewHolder holder, int position) {

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
                holder.takeOutItemInfo.setVisibility(View.GONE);
                break;
            }
            case 1: {
                holder.item = mItems.get(position);
                mActivity.setDicTypeLabel(holder.item);
                holder.add.setVisibility(View.GONE);
                holder.takeOutItemInfo.setVisibility(View.VISIBLE);
                TakeOutItemInfo record = mItems.get(position);
                holder.containerNo.setText(record.conNo);
                holder.outType.setText(record.purposeLabel);
                holder.outWeight.setText(record.outWeight + "g");
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
