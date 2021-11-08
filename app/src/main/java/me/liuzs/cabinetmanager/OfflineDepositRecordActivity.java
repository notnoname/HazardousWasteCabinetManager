package me.liuzs.cabinetmanager;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;
import java.util.List;

import me.liuzs.cabinetmanager.model.DepositRecord;
import me.liuzs.cabinetmanager.net.APIJSON;
import me.liuzs.cabinetmanager.net.DepositRecordListJSON;
import me.liuzs.cabinetmanager.net.RemoteAPI;
import me.liuzs.cabinetmanager.ui.offline.OfflineDepositRecordListAdapter;
import me.liuzs.cabinetmanager.util.Util;

/**
 * 库存、台账
 */
public class OfflineDepositRecordActivity extends BaseActivity {

    public static final String TAG = "StandingBookActivity";
    private final OfflineDepositRecordListAdapter mAdapter = new OfflineDepositRecordListAdapter();
    private final List<DepositRecord> mDepositRecordList = new LinkedList<>();

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_deposit_record);

        RecyclerView mRecyclerView = findViewById(R.id.rvRecord);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        getUnSubmitDepositRecordList();
    }

    public void getUnSubmitDepositRecordList() {
        getExecutorService().submit(() -> {
            List<DepositRecord> depositRecordList = CabinetCore.getUnSubmitDepositRecord();
            mDepositRecordList.clear();
            mDepositRecordList.addAll(depositRecordList);
            mAdapter.setResult(depositRecordList);
        });
    }

    public void onSubmitButtonClick(View view) {
        showProgressDialog();
        getExecutorService().submit(() -> {
            for (DepositRecord record : mDepositRecordList) {
                submitDepositRecord(record);
                Util.sleep(1000);
            }
            dismissProgressDialog();
            getUnSubmitDepositRecordList();
        });
    }

    private void submitDepositRecord(DepositRecord record) {
        APIJSON<DepositRecordListJSON> json = RemoteAPI.Deposit.getDeposit(record.storage_no, 20, 1);
        if (json.status == APIJSON.Status.ok) {
            if (json.data == null || json.data.storage_records == null || json.data.storage_records.size() == 0) {
                submitCorrectDepositRecord(record);
            } else {
                DepositRecord remoteRecord = json.data.storage_records.get(0);
                remoteRecord.has_storage_rack = !TextUtils.isEmpty(remoteRecord.storage_rack);
                remoteRecord.has_input_weight = !TextUtils.isEmpty(remoteRecord.input_weight);
                remoteRecord.has_output_weight = !TextUtils.isEmpty(remoteRecord.output_weight);
                if (remoteRecord.has_output_weight) {
                    showToast("离线存单:" + record.storage_no + ",远端已出柜，本地记录将删除.");
                    CabinetCore.deleteDepositRecord(record.localId);
                } else {
                    if (remoteRecord.has_input_weight) {
                        record.input_weight = remoteRecord.input_weight;
                    }
                    if (remoteRecord.has_storage_rack) {
                        if (!record.has_storage_rack) {
                            showToast("离线存单:" + record.storage_no + ",远端已入柜，本地记录将删除.");
                            CabinetCore.deleteDepositRecord(record.localId);
                        } else {
                            submitCorrectDepositRecord(record);
                        }
                    } else {
                        submitCorrectDepositRecord(record);
                    }
                }
            }
        } else if (json.status == APIJSON.Status.error) {
            showToast("离线存单:" + record.storage_no + ",非法单号，本条离线记录将被删除");
            CabinetCore.deleteDepositRecord(record.localId);
        } else {
            showToast("离线存单:" + record.storage_no + ",远端访问异常:" + json.error);
        }
    }

    private void submitCorrectDepositRecord(DepositRecord record) {
        APIJSON<DepositRecord> submitJSON = RemoteAPI.Deposit.submitDeposit(record);
        dismissProgressDialog();
        if (submitJSON.status == APIJSON.Status.ok) {
            showToast("提交成功");
            CabinetCore.deleteDepositRecord(record.localId);
        } else if (submitJSON.status == APIJSON.Status.error) {
            showToast("离线存单:" + record.storage_no + ",远端访问异常:" + submitJSON.error);
        } else {
            showToast("离线存单:" + record.storage_no + ",远端访问异常:" + submitJSON.error);
        }
    }

    public void onBackButtonClick(View view) {
        finish();
    }
}
