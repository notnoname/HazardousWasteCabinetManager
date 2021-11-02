package me.liuzs.cabinetmanager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import me.liuzs.cabinetmanager.model.DictType;
import me.liuzs.cabinetmanager.model.StandingBookItem;
import me.liuzs.cabinetmanager.net.APIJSON;
import me.liuzs.cabinetmanager.net.RemoteAPI;
import me.liuzs.cabinetmanager.ui.standingbook.StandingBookListAdapter;

/**
 * 库存、台账
 */
public class StandingBookActivity extends BaseActivity {

    class Pagination{
        /**
         * 每页记录数
         */
        public int page_size;
        /**
         * 总记录数
         */
        public int total_count;
        /**
         * 第几页,1开始
         */
        public int current_page;

        /**
         * 总页数
         */
        public int total_pages;
    }

    public static final String TAG = "StandingBookActivity";
    private final StandingBookListAdapter mAdapter = new StandingBookListAdapter(this);
    private int mCurrentPage = 1;
    private DetailType mDetailType = DetailType.Inventories;
    private final Map<DetailType, Pagination> mPagination = new LinkedHashMap<>();

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
                queryStandingBook(mDetailType, false);
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

        queryStandingBook(mDetailType, false);
    }

    private void queryStandingBook(DetailType type, boolean isMore) {
        if (isMore) {
            mCurrentPage++;
        } else {
            mCurrentPage = 1;
        }
        new GetStandingBookTask(StandingBookActivity.this, type, isMore).execute(String.valueOf(mCurrentPage));
    }

    public void onMoreButtonClick(View view) {
        queryStandingBook(mDetailType, true);
    }

    public void onBackButtonClick(View view) {
        finish();
    }

    enum DetailType {
        Inventories, Deposit, TakeOut
    }

    static class GetStandingBookTask extends AsyncTask<String, Void, APIJSON<List<StandingBookItem>>> {

        private final WeakReference<StandingBookActivity> mActivity;
        private final boolean isLoadMore;
        private final DetailType mType;

        public GetStandingBookTask(StandingBookActivity activity, DetailType type, boolean isLoadMore) {
            this.isLoadMore = isLoadMore;
            this.mType = type;
            this.mActivity = new WeakReference<>(activity);
        }

        @Override
        protected APIJSON<List<StandingBookItem>> doInBackground(String... strings) {
            if (!mActivity.get().mAdapter.isSetDict()) {
                List<DictType> mUnitTypes;
                List<DictType> mPurityTypes;
                List<DictType> mMeasureSpecTypes;
                APIJSON<List<StandingBookItem>> result = new APIJSON<>();
                APIJSON<List<DictType>> unitTypes = RemoteAPI.BaseInfo.getUnitDictCode();
                if (unitTypes.status != APIJSON.Status.ok) {
                    result.status = unitTypes.status;
                    return result;
                } else {
                    mUnitTypes = unitTypes.data;
                }
                APIJSON<List<DictType>> purityTypes = RemoteAPI.BaseInfo.getPurityDictCode();
                if (purityTypes.status != APIJSON.Status.ok) {
                    result.status = purityTypes.status;
                    return result;
                } else {
                    mPurityTypes = purityTypes.data;
                }

                APIJSON<List<DictType>> measureSpecs = RemoteAPI.BaseInfo.getMeasureSpecDictCode();
                if (measureSpecs.status != APIJSON.Status.ok) {
                    result.status = measureSpecs.status;
                    return result;
                } else {
                    mMeasureSpecTypes = measureSpecs.data;
                }
                mActivity.get().mAdapter.setDict(mPurityTypes, mUnitTypes, mMeasureSpecTypes);
            }
            switch (mType) {
                case Inventories:
                    return RemoteAPI.StandingBook.queryStandingBookDeposit(strings[0]);
                case TakeOut:
                    return RemoteAPI.StandingBook.queryStandingBookTakeOut(strings[0]);
                case Deposit:
                    return RemoteAPI.StandingBook.queryStandingBookTakeIn(strings[0]);
            }
            return RemoteAPI.StandingBook.queryStandingBookDeposit(strings[0]);
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mActivity.get().showProgressDialog();
        }

        @Override
        protected void onPostExecute(APIJSON<List<StandingBookItem>> json) {
            super.onPostExecute(json);
            if (json.status == APIJSON.Status.ok) {
                if (isLoadMore) {
                    mActivity.get().mAdapter.addResult(json.data, 100);
                } else {
                    mActivity.get().mAdapter.setResult(json.data, 100);
                }
            } else {
                if (isLoadMore) {
                    mActivity.get().mCurrentPage--;
                }
                mActivity.get().showToast(json.error);
            }
            mActivity.get().dismissProgressDialog();
            mActivity.get().hideInputMethod();
        }
    }
}
