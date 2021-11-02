package me.liuzs.cabinetmanager;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import me.liuzs.cabinetmanager.ui.standingbook.StandingBookListAdapter;

/**
 * 库存、台账
 */
public class StandingBookActivity extends BaseActivity {

    public static final String TAG = "StandingBookActivity";
    private final StandingBookListAdapter mAdapter = new StandingBookListAdapter(this);
    private DetailType mDetailType = DetailType.Deposit;
    private int mCurrentPage = 1, mTotalPage = 0;
    private static final int pageSize = 20;

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
                }
            }
        });
    }

    public void getDepositRecordList(DetailType detailType) {
        String url = null;
        switch (detailType) {
            case Inventories:
                break;
            case Deposit:
                break;
            case TakeOut:
                break;
        }
    }

    public void onBackButtonClick(View view) {
        finish();
    }

    enum DetailType {
        Inventories, Deposit, TakeOut
    }
}
