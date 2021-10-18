package me.liuzs.cabinetmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.LinkedList;
import java.util.List;

import me.liuxy.cabinet.SubBoard;
import me.liuzs.cabinetmanager.model.Cabinet;
import me.liuzs.cabinetmanager.model.HardwareValue;
import me.liuzs.cabinetmanager.model.SubBoardStatusInfo;
import me.liuzs.cabinetmanager.service.ModbusService;
import me.liuzs.cabinetmanager.ui.singlenumbermanagement.SubBoardListAdapter;
import me.liuzs.cabinetmanager.ui.singlenumbermanagement.SubBoardStatusItemViewHolder;
import me.liuzs.cabinetmanager.util.Util;

/**
 * 单号管理
 */
public class SingleNumberManagementActivity extends BaseActivity {

    public static final String TAG = "SingleNumberManagementActivity";
    private final SubBoardListAdapter mAdapter = new SubBoardListAdapter(this);
    private final Gson mGson = new Gson();
    private final List<SubBoardStatusInfo> mSubBoardStatusInfo = new LinkedList<>();
    private RecyclerView mRecyclerView;
    private HardwareValueBroadcastReceiver mHardwareValueBroadcastReceiver;

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_number_management);
        Util.fullScreen(this);

        mRecyclerView = findViewById(R.id.rvRecord);
        mRecyclerView.setAdapter(mAdapter);

        int spanCount = 2;

        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            spanCount = 4;
        }
        GridLayoutManager gm = new GridLayoutManager(this, spanCount);
        mRecyclerView.setLayoutManager(gm);


        Cabinet cabinet = CabinetCore.getCabinetInfo();

        if (cabinet != null) {
//            for (DeviceInfo info : cabinet.devices) {
//                SubBoardStatusInfo statusInfo = new SubBoardStatusInfo();
//                statusInfo.name = info.devName;
//                mSubBoardStatusInfo.add(statusInfo);
//            }
        }

        if (HardwareValue._Cache != null) {
            List<SubBoard.StatusData> datas = new LinkedList<>();
            for (int i = 0; i < mSubBoardStatusInfo.size(); i++) {
                SubBoard.StatusData data = datas.get(i);
                SubBoardStatusInfo info = mSubBoardStatusInfo.get(i);
                info.statusData = data;
            }
        }

        SubBoardStatusItemViewHolder.setup = ModbusService.readSetupValue();
        mAdapter.setResult(mSubBoardStatusInfo);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mHardwareValueBroadcastReceiver = new HardwareValueBroadcastReceiver();
        IntentFilter filter = new IntentFilter(Config.ACTION_HARDWARE_VALUE_SEND);
        registerReceiver(mHardwareValueBroadcastReceiver, filter);
        Log.d(TAG, "RegisterReceiver");
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mHardwareValueBroadcastReceiver);
        Log.d(TAG, "UnregisterReceiver");
    }

    public void onBackButtonClick(View view) {
        finish();
    }

    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

    private class HardwareValueBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "OnReceiver");
            String json = intent.getStringExtra(Config.KEY_HARDWARE_VALUE);
            HardwareValue value = mGson.fromJson(json, HardwareValue.class);
            List<SubBoard.StatusData> datas = new LinkedList<>();
            for (int i = 0; i < mSubBoardStatusInfo.size(); i++) {
                SubBoard.StatusData data = datas.get(i);
                SubBoardStatusInfo info = mSubBoardStatusInfo.get(i);
                info.statusData = data;
            }
            mAdapter.setResult(mSubBoardStatusInfo);
        }
    }


}
