package me.liuzs.cabinetmanager;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import me.liuzs.cabinetmanager.net.APIJSON;
import me.liuzs.cabinetmanager.net.DepositRecordListJSON;
import me.liuzs.cabinetmanager.net.RemoteAPI;
import me.liuzs.cabinetmanager.ui.standingbook.StandingBookListAdapter;

/**
 * 库存、台账
 */
public class StandingBookActivity extends BaseActivity {

    public static final String TAG = "StandingBookActivity";
    private final StandingBookListAdapter mAdapter = new StandingBookListAdapter(this);
    private DetailType mDetailType = DetailType.Inventories;
    private int mCurrentPage = 1, mTotalPage = 0;
    private static final int _PageSize = 20;

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standing_book);
        TabLayout mTabLayout = findViewById(R.id.tabLayout);

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mCurrentPage = 1;
                mTotalPage = 0;
                switch (tab.getPosition()) {
                    case 1:
                        mDetailType = DetailType.Deposit;
                        break;
                    case 2:
                        mDetailType = DetailType.TakeOut;
                        break;
                    default:
                        mDetailType = DetailType.Inventories;
                }
                getDepositRecordList(mDetailType);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        RecyclerView mRecyclerView = findViewById(R.id.rvRecord);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                assert linearLayoutManager != null;
                if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == mAdapter.getItemCount() - 1 && mCurrentPage < mTotalPage) {
                    mCurrentPage++;
                    getDepositRecordList(mDetailType);
                }
            }
        });
        getDepositRecordList(mDetailType);
    }

    public void getDepositRecordList(DetailType detailType) {
        showProgressDialog();
        getExecutorService().submit(() -> {
            APIJSON<DepositRecordListJSON> depositRecordListJSONAPIJSON = RemoteAPI.DepositRecordQuery.queryDepositList(detailType, _PageSize, mCurrentPage);
            dismissProgressDialog();
            if (depositRecordListJSONAPIJSON.status == APIJSON.Status.ok) {
                DepositRecordListJSON depositRecordListJSON = depositRecordListJSONAPIJSON.data;
                if (depositRecordListJSON != null) {
                    mCurrentPage = depositRecordListJSON.current_page;
                    mTotalPage = depositRecordListJSON.total_pages;
                    if (mCurrentPage == 1) {
                        mAdapter.setResult(depositRecordListJSON.storage_records);
                    } else {
                        mAdapter.addResult(depositRecordListJSON.storage_records);
                    }
                } else {
                    showToast("无数据！");
                }
            } else {
                showToast(depositRecordListJSONAPIJSON.error);
            }
        });
    }

    public void onBackButtonClick(View view) {
        finish();
    }

    public enum DetailType {
        Inventories, Deposit, TakeOut
    }
}
