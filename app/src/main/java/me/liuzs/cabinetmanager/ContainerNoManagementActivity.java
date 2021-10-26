package me.liuzs.cabinetmanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;
import java.util.List;

import me.liuzs.cabinetmanager.model.Cabinet;
import me.liuzs.cabinetmanager.model.ContainerNoBatchInfo;
import me.liuzs.cabinetmanager.net.APIJSON;
import me.liuzs.cabinetmanager.net.RemoteAPI;
import me.liuzs.cabinetmanager.service.ModbusService;
import me.liuzs.cabinetmanager.ui.singlenumbermanagement.ContainerBatchListAdapter;
import me.liuzs.cabinetmanager.ui.singlenumbermanagement.ContainerBatchItemViewHolder;

/**
 * 单号管理
 */
public class ContainerNoManagementActivity extends BaseActivity {

    public static final String TAG = "ContainerNoManagementActivity";
    private final ContainerBatchListAdapter mAdapter = new ContainerBatchListAdapter(this);
    private final List<ContainerNoBatchInfo> mContainerNoBatchInfo = new LinkedList<>();
    private RecyclerView mRecyclerView;

    private final ActivityResultLauncher<Intent> mNoCreateLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            int resultCode = result.getResultCode();
            if (resultCode == RESULT_OK) {
                Intent data = result.getData();
                assert data != null;
                String selectValue = data.getStringExtra(ContainerNoCreateActivity.KEY_RESULT_VALUE);
                assert selectValue != null;
                showToast("单号创建:" + selectValue);
            }
        }
    });

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

        mAdapter.setResult(mContainerNoBatchInfo);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void getContainerNoList(String name, String count) {
        getExecutorService().submit(() -> {
            APIJSON<ContainerNoBatchInfo> json = RemoteAPI.ContainerNoManager.createContainerNoBatch(name, count);
            if(json.status == APIJSON.Status.ok) {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void onAddBatchButtonClick(View view) {
        Intent intent = new Intent(this, ContainerNoCreateActivity.class);
        mNoCreateLauncher.launch(intent);
    }

    public void onBackButtonClick(View view) {
        finish();
    }

    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

}
