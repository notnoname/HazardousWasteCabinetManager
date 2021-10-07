package me.liuzs.cabinetmanager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.util.List;

import me.liuzs.cabinetmanager.model.DictType;
import me.liuzs.cabinetmanager.model.InventoryDetail;
import me.liuzs.cabinetmanager.model.InventoryItem;
import me.liuzs.cabinetmanager.net.APIJSON;
import me.liuzs.cabinetmanager.net.RemoteAPI;
import me.liuzs.cabinetmanager.ui.inventorydetail.InventoryDetailListAdapter;
import me.liuzs.cabinetmanager.util.Util;

/**
 * 库存查询模块，库存详情页。
 */
public class InventoryDetailActivity extends BaseActivity {

    public static final String KEY_INVENTORY_ITEM = "KEY_INVENTORY_ITEM";
    public static final String TAG = "InventoryActivity";
    private final InventoryDetailListAdapter mAdapter = new InventoryDetailListAdapter(this);
    private RecyclerView mRecyclerView;
    private int mCurrentPage = 1;
    private InventoryItem mInventoryItem;
    private TextView mChemicalName, mCASNo, mControlType;

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_detail);
        Util.fullScreen(this);

        mRecyclerView = findViewById(R.id.rvRecord);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        String json = getIntent().getStringExtra(KEY_INVENTORY_ITEM);
        mInventoryItem = new Gson().fromJson(json, InventoryItem.class);
        mChemicalName = findViewById(R.id.tvChemicalNameValue);
        mCASNo = findViewById(R.id.tvCASNoValue);
        mControlType = findViewById(R.id.tvControlTypeValue);
        mChemicalName.setText(mInventoryItem.chemicalName);
        mCASNo.setText(mInventoryItem.casNo);
        mControlType.setText(mInventoryItem.chemicalType == null ? "未明确化学品类型" : mInventoryItem.chemicalType);
        queryDefaultInventoryDetail();
    }

    private void queryDefaultInventoryDetail() {
        mCurrentPage = 1;
        new GetInventoryDetailTask(InventoryDetailActivity.this, false).execute(mInventoryItem.chemicalId, String.valueOf(mCurrentPage));
    }

    public void onMoreButtonClick(View view) {
        mCurrentPage++;
        new GetInventoryDetailTask(InventoryDetailActivity.this, true).execute(mInventoryItem.chemicalId, String.valueOf(mCurrentPage));
    }

    public void onBackButtonClick(View view) {
        finish();
    }

    static class GetInventoryDetailTask extends AsyncTask<String, Void, APIJSON<List<InventoryDetail>>> {

        private final boolean isLoadMore;
        private WeakReference<InventoryDetailActivity> mActivity;

        public GetInventoryDetailTask(InventoryDetailActivity activity, boolean isLoadMore) {
            this.isLoadMore = isLoadMore;
            this.mActivity = new WeakReference<>(activity);
        }

        @Override
        protected APIJSON<List<InventoryDetail>> doInBackground(String... strings) {
            if (!mActivity.get().mAdapter.isSetDict()) {
                List<DictType> mUnitTypes;
                List<DictType> mPurityTypes;
                List<DictType> mMeasureSpecTypes;
                APIJSON<List<InventoryDetail>> result = new APIJSON<>();
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
            return RemoteAPI.InventoryQuery.queryInventoryDetail(strings[0], strings[1]);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mActivity.get().showProgressDialog();
        }

        @Override
        protected void onPostExecute(APIJSON<List<InventoryDetail>> json) {
            super.onPostExecute(json);
            if (json.status == APIJSON.Status.ok) {
                if (json.data.size() > 0) {
                    InventoryDetail detail = json.data.get(0);
                    mActivity.get().mCASNo.setText(detail.casNo);
                    mActivity.get().mControlType.setText(detail.controlType);
                }
                if (isLoadMore) {
                    mActivity.get().mAdapter.addResult(json.data, 100);
                } else {
                    mActivity.get().mAdapter.setResult(json.data, 100);
                }
            } else {
                if (isLoadMore) {
                    mActivity.get().mCurrentPage--;
                }
                mActivity.get().showToast(json.errors);
            }
            mActivity.get().dismissProgressDialog();
            mActivity.get().hideInputMethod();
        }
    }
}
