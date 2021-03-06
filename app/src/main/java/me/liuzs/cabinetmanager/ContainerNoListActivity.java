package me.liuzs.cabinetmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;
import java.util.List;

import me.liuzs.cabinetmanager.model.ContainerNoBatchInfo;
import me.liuzs.cabinetmanager.model.ContainerNoInfo;
import me.liuzs.cabinetmanager.net.APIJSON;
import me.liuzs.cabinetmanager.net.RemoteAPI;
import me.liuzs.cabinetmanager.ui.containernolist.ContainerNoListAdapter;

/**
 * 单号管理
 */
public class ContainerNoListActivity extends BaseActivity {

    public static final String TAG = "ContainerNoManagementActivity";
    private static final String KEY_ID = "BATCH_ID";
    private static final String KEY_NAME = "BATCH_NAME";
    private final ContainerNoListAdapter mAdapter = new ContainerNoListAdapter(this);
    private RecyclerView mRecyclerView;
    private TextView mBatchName;
    private String mBatchId;
    private final List<ContainerNoInfo> mContainerNoList = new LinkedList<>();

    public static void start(Context context, String batchId, String batchName) {
        Intent intent = new Intent(context, ContainerNoListActivity.class);
        intent.putExtra(KEY_ID, batchId);
        intent.putExtra(KEY_NAME, batchName);
        if(!(context instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container_no_list);
        mBatchId = getIntent().getStringExtra(KEY_ID);
        mRecyclerView = findViewById(R.id.rvRecord);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
        mBatchName = findViewById(R.id.tvBatchName);
        mBatchName.setText(getIntent().getStringExtra(KEY_NAME));
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAdapter.clear();
        getContainerNoList(mBatchId);
    }


    private void getContainerNoList(String batchId) {
        showProgressDialog();
        getExecutorService().submit(() -> {
            APIJSON<ContainerNoBatchInfo> json = RemoteAPI.ContainerNoManager.getContainerNoList(batchId);
            if (json.status == APIJSON.Status.ok) {
                mContainerNoList.clear();
                mContainerNoList.addAll(json.data.storage_nos);
                mHandler.post(() -> mAdapter.add(mContainerNoList));
            } else {
                showToast(json.error);
            }
            dismissProgressDialog();

        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void onPrintButtonClick(View view) {
        PrintActivity.startPrintContainerLabel(this, mContainerNoList);
    }

    public void onBackButtonClick(View view) {
        finish();
    }
}
