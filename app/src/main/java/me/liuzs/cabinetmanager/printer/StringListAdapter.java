package me.liuzs.cabinetmanager.printer;

import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import me.liuzs.cabinetmanager.R;

public class StringListAdapter extends BaseQuickAdapter<PrinterBluetoothInfo, BaseViewHolder> {

    public StringListAdapter() {
        super(R.layout.listview_item_device_bluetooth_list);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, PrinterBluetoothInfo item) {
        if (TextUtils.isEmpty(item.address)) {
            baseViewHolder.setText(R.id.tv_device_name, item.name);
        } else {
            baseViewHolder.setText(R.id.tv_device_name, item.name + " - " + item.address);
        }
    }
}
