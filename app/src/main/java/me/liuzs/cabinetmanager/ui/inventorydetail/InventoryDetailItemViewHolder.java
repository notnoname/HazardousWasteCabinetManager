package me.liuzs.cabinetmanager.ui.inventorydetail;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import me.liuzs.cabinetmanager.InventoryDetailActivity;
import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.model.InventoryDetail;

public class InventoryDetailItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    TextView containerNo, purity, packingSpec, weight, supplier, deviceName;
    InventoryDetail inventoryDetail;
    InventoryDetailActivity activity;
    Button more;
    ConstraintLayout itemInfo;


    public InventoryDetailItemViewHolder(@NonNull View itemView) {
        super(itemView);
        itemInfo = itemView.findViewById(R.id.csDetailInfo);
        containerNo = itemView.findViewById(R.id.tvConNoValue);
        supplier = itemView.findViewById(R.id.tvSupplierValue);
        purity = itemView.findViewById(R.id.etWeightValue);
        packingSpec = itemView.findViewById(R.id.tvPackSpecValue);
        weight = itemView.findViewById(R.id.tvWeightValue);
        deviceName = itemView.findViewById(R.id.tvDeviceValue);
        more = itemView.findViewById(R.id.btnMore);
        more.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == more) {
            activity.onMoreButtonClick(v);
        }
    }
}