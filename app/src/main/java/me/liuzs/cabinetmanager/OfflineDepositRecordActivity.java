package me.liuzs.cabinetmanager;

import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;
import java.util.List;

import me.liuzs.cabinetmanager.model.DepositRecord;
import me.liuzs.cabinetmanager.net.APIJSON;
import me.liuzs.cabinetmanager.net.RemoteAPI;
import me.liuzs.cabinetmanager.ui.offline.OfflineDepositRecordListAdapter;

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
                APIJSON<DepositRecord> apijson = RemoteAPI.Deposit.submitDeposit(record);
                dismissProgressDialog();
                if (apijson.status == APIJSON.Status.ok) {
                    showToast("提交成功");
                } else if (apijson.status == APIJSON.Status.error) {
                    showToast(apijson.error);
                } else {
                    showToast(apijson.error);
                }
            }
        });

    }

    public void onBackButtonClick(View view) {
        finish();
    }
}
