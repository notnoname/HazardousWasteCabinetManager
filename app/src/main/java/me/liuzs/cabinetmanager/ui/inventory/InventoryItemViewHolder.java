package me.liuzs.cabinetmanager.ui.inventory;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import me.liuzs.cabinetmanager.InventoryDetailActivity;
import me.liuzs.cabinetmanager.InventoryQueryActivity;
import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.model.InventoryItem;

public class InventoryItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private static Gson mGson = new Gson();
    TextView chemicalName, count, weight;
    InventoryItem inventoryItem;
    InventoryQueryActivity activity;
    Button more;
    LinearLayout itemInfo;


    public InventoryItemViewHolder(@NonNull View itemView) {
        super(itemView);
        itemInfo = itemView.findViewById(R.id.llItemInfo);
        itemInfo.setOnClickListener(this);
        chemicalName = itemView.findViewById(R.id.tvChemicalName);
        count = itemView.findViewById(R.id.tvWeight);
        weight = itemView.findViewById(R.id.tvCount);
        more = itemView.findViewById(R.id.btnMore);
        more.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == more) {
            activity.onMoreButtonClick(v);
        } else if (v == itemInfo) {
            Intent intent = new Intent(activity, InventoryDetailActivity.class);
            intent.putExtra(InventoryDetailActivity.KEY_INVENTORY_ITEM, mGson.toJson(inventoryItem));
            activity.startActivity(intent);
        }
    }
}