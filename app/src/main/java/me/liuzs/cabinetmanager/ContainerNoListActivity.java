package me.liuzs.cabinetmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;
import java.util.List;

import me.liuzs.cabinetmanager.model.ContainerNoBatchInfo;
import me.liuzs.cabinetmanager.model.ContainerNoInfo;
import me.liuzs.cabinetmanager.net.APIJSON;
import me.liuzs.cabinetmanager.net.ContainerNoBatchListJSON;
import me.liuzs.cabinetmanager.net.RemoteAPI;
import me.liuzs.cabinetmanager.ui.containernolist.ContainerNoListAdapter;

/**
 * 单号管理
 */
public class ContainerNoListActivity extends BaseActivity {

    public static final String TAG = "ContainerNoManagementActivity";
    private final ContainerNoListAdapter mAdapter = new ContainerNoListAdapter(this);
    private RecyclerView mRecyclerView;
    private static final String KEY_ID = "BATCH_ID";

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {
    }

    public static void start(Context context, String batchId) {
        Intent intent = new Intent(context, ContainerNoListActivity.class);
        intent.putExtra(KEY_ID, batchId);
        if(!(context instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    private String mBatchId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container_no_list);
        mBatchId = getIntent().getStringExtra(KEY_ID);
        mRecyclerView = findViewById(R.id.rvRecord);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
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
            APIJSON<List<ContainerNoInfo>> json = RemoteAPI.ContainerNoManager.getContainerNoList(batchId);
            if (json.status == APIJSON.Status.ok) {

            }
            List<ContainerNoInfo> resultList = new LinkedList<>();
            for (int i = 0; i < 20; i++) {
                ContainerNoInfo info = new ContainerNoInfo();
                info.id = String.valueOf(i);
                info.no = "单号:" + i;
                info.agency_name = "清华大学";
                info.operator = "刘座宿";
                info.create_time = "2020-07-16 21:19:45";
                info.batch_id = "CNDDSADASFASFDASFDAS";
                info.batch_name = "CNDASFADFASDS";
                resultList.add(info);
            }
            dismissProgressDialog();
            mHandler.post(() -> mAdapter.add(resultList));
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void onPrintButtonClick(View view) {

    }

    public void onBackButtonClick(View view) {
        finish();
    }
}
