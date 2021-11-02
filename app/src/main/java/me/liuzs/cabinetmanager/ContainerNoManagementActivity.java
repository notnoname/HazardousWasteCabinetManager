package me.liuzs.cabinetmanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;

import me.liuzs.cabinetmanager.model.ContainerNoBatchInfo;
import me.liuzs.cabinetmanager.net.APIJSON;
import me.liuzs.cabinetmanager.net.ContainerNoBatchListJSON;
import me.liuzs.cabinetmanager.net.RemoteAPI;
import me.liuzs.cabinetmanager.ui.containernobatch.ContainerBatchListAdapter;

/**
 * 单号管理
 */
public class ContainerNoManagementActivity extends BaseActivity implements TextWatcher {

    public static final String TAG = "ContainerNoManagementActivity";
    private final ContainerBatchListAdapter mAdapter = new ContainerBatchListAdapter(this);
    private final ActivityResultLauncher<Intent> mNoCreateLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        hideInputMethod();
        int resultCode = result.getResultCode();
        if (resultCode == RESULT_OK) {
            Intent data = result.getData();
            assert data != null;
            String name = data.getStringExtra(ContainerNoCreateActivity.KEY_RESULT_BATCH_NAME);
            String count = data.getStringExtra(ContainerNoCreateActivity.KEY_RESULT_COUNT);
            assert name != null;
            assert count != null;
            createContainerNoBatch(name, count);
        }
    });
    private RecyclerView mRecyclerView;
    private static final int mPageSize = 20;
    private int mCurrentPage = 1, mTotalPage = 0;
    private EditText mBatchName, mOperator;
    private String mBatchNameValue = "", mOperatorValue = "";

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container_number_management);
        mBatchName = findViewById(R.id.etBatchName);
        mBatchName.addTextChangedListener(this);
        mOperator = findViewById(R.id.etOperator);
        mOperator.addTextChangedListener(this);
        mRecyclerView = findViewById(R.id.rvRecord);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                assert linearLayoutManager != null;
                if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == mAdapter.getItemCount() - 1 && mCurrentPage < mTotalPage) {
                    mCurrentPage++;
                    getContainerNoBatchList();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getContainerNoBatchList();
    }

    private void createContainerNoBatch(String name, String amount) {
        showProgressDialog();
        getExecutorService().submit(() -> {
            APIJSON<ContainerNoBatchInfo> json = RemoteAPI.ContainerNoManager.createContainerNoBatch(name, amount);
            if (json.status == APIJSON.Status.ok) {
                showToast("批次创建成功！");
            } else {
                showToast(json.error);
            }

            mHandler.post(() -> {
                mBatchName.setText("");
                mOperator.setText("");
                hideInputMethod();
                mCurrentPage = 1;
                getContainerNoBatchList();
            });
        });
    }

    private void getContainerNoBatchList() {
        showProgressDialog();
        getExecutorService().submit(() -> {
            APIJSON<ContainerNoBatchListJSON> json = RemoteAPI.ContainerNoManager.getContainerNoBatchList(mBatchNameValue, mOperatorValue, mPageSize, mCurrentPage);
            if (json.status == APIJSON.Status.ok) {
                if (json.data.batches != null) {
                    mCurrentPage = json.data.current_page;
                    mTotalPage = json.data.total_pages;
                    mHandler.post(() -> {
                        if (mCurrentPage == 1) {
                            mAdapter.clear();
                        }
                        mAdapter.add(json.data.batches);
                    });
                }
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

    public void onAddBatchButtonClick(View view) {
        Intent intent = new Intent(this, ContainerNoCreateActivity.class);
        mNoCreateLauncher.launch(intent);
    }

    public void onSearchButtonClick(View view) {
        mCurrentPage = 0;
        mTotalPage = 0;
        mAdapter.clear();
        getContainerNoBatchList();
    }

    public void onBackButtonClick(View view) {
        finish();
    }


    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (editable == mBatchName.getEditableText()) {
            mBatchNameValue = editable.toString();
        } else if (editable == mOperator.getEditableText()) {
            mOperatorValue = editable.toString();
        }
    }
}
