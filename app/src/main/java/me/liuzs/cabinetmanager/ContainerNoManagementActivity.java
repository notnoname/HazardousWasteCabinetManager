package me.liuzs.cabinetmanager;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;
import java.util.List;

import me.liuxy.cabinet.SubBoard;
import me.liuzs.cabinetmanager.model.Cabinet;
import me.liuzs.cabinetmanager.model.HardwareValue;
import me.liuzs.cabinetmanager.model.SubBoardStatusInfo;
import me.liuzs.cabinetmanager.service.ModbusService;
import me.liuzs.cabinetmanager.ui.singlenumbermanagement.ContainerNoListAdapter;
import me.liuzs.cabinetmanager.ui.singlenumbermanagement.SubBoardStatusItemViewHolder;

/**
 * 单号管理
 */
public class ContainerNoManagementActivity extends BaseActivity {

    public static final String TAG = "ContainerNoManagementActivity";
    private final ContainerNoListAdapter mAdapter = new ContainerNoListAdapter(this);
    private final List<SubBoardStatusInfo> mSubBoardStatusInfo = new LinkedList<>();
    private RecyclerView mRecyclerView;

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container_number_management);
        mRecyclerView = findViewById(R.id.rvRecord);
        mRecyclerView.setAdapter(mAdapter);

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
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void onBackButtonClick(View view) {
        finish();
    }

    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

}
