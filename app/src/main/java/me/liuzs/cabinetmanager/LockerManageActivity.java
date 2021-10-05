package me.liuzs.cabinetmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.LinkedList;
import java.util.List;

import me.liuxy.cabinet.SubBoard;
import me.liuzs.cabinetmanager.model.Cabinet;
import me.liuzs.cabinetmanager.model.DeviceInfo;
import me.liuzs.cabinetmanager.model.HardwareValue;
import me.liuzs.cabinetmanager.model.LockerStatus;
import me.liuzs.cabinetmanager.ui.lockermanage.LockerListAdapter;
import me.liuzs.cabinetmanager.util.Util;

/**
 * 门锁管理
 */
public class LockerManageActivity extends BaseActivity {

    public static final String TAG = "LockerManageActivity";
    private final LockerListAdapter mAdapter = new LockerListAdapter(this);
    private final Gson mGson = new Gson();
    private final List<LockerStatus> mLockerStatus = new LinkedList<>();
    private RecyclerView mRecyclerView;
    private HardwareValueBroadcastReceiver mHardwareValueBroadcastReceiver;

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locker_manage);
        Util.fullScreen(this);

        mRecyclerView = findViewById(R.id.rvRecord);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        Cabinet cabinet = CabinetCore.getCabinetInfo();

//        if (cabinet != null) {
//            for (DeviceInfo info : cabinet.devices) {
//                LockerStatus status = new LockerStatus();
//                status.deviceName = info.devName;
//                status.devId = info.devId;
//                mLockerStatus.add(status);
//            }
//        }

        mAdapter.setResult(mLockerStatus);

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
            List<SubBoard.StatusData> dataList = value.subBoardStatusData;
            if (dataList.size() == mLockerStatus.size()) {
                for (int index = 0; index < dataList.size(); index++) {
                    SubBoard.StatusData data = dataList.get(index);
                    LockerStatus status = mLockerStatus.get(index);
                    if (data.door_lock == 1) {
                        status.lock = LockerStatus.Status.Lock;
                    } else {
                        status.lock = LockerStatus.Status.Unlock;
                    }
                }
            }
            mAdapter.notifyDataSetChanged();
        }
    }

}
