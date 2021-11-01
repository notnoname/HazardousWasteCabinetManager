package me.liuzs.cabinetmanager;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.List;

import me.liuzs.cabinetmanager.model.InventoryItem;
import me.liuzs.cabinetmanager.net.APIJSON;
import me.liuzs.cabinetmanager.net.RemoteAPI;
import me.liuzs.cabinetmanager.ui.inventory.InventoryQueryListAdapter;
import me.liuzs.cabinetmanager.util.Util;

/**
 * 库存查询模块，库存列表
 */
public class InventoryQueryActivity extends BaseActivity {

    public static final String TAG = "InventoryActivity";
    private final InventoryQueryListAdapter mAdapter = new InventoryQueryListAdapter(this);
    private RecyclerView mRecyclerView;
    private SearchView mSearchView;
    private int mCurrentPage = 1;

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_query);
        Util.fullScreen(this);

        mSearchView = findViewById(R.id.searchView);
        int id = mSearchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView textView = mSearchView.findViewById(id);
        textView.setTextColor(Color.BLACK);
        textView.setHintTextColor(Color.parseColor("#CCCCCC"));

        mRecyclerView = findViewById(R.id.rvRecord);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mCurrentPage = 1;
                new QueryInventoryTask(InventoryQueryActivity.this, false).execute(query, String.valueOf(mCurrentPage));
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    mCurrentPage = 1;
                    new QueryInventoryTask(InventoryQueryActivity.this, false).execute("", String.valueOf(mCurrentPage));
                }
                return true;
            }
        });
        queryDefaultInventory();
    }

    private void queryDefaultInventory() {
        mCurrentPage = 1;
        new QueryInventoryTask(InventoryQueryActivity.this, false).execute("", String.valueOf(mCurrentPage));
    }

    public void onMoreButtonClick(View view) {
        mCurrentPage++;
        new QueryInventoryTask(InventoryQueryActivity.this, true).execute("", String.valueOf(mCurrentPage));
    }

    public void onBackButtonClick(View view) {
        finish();
    }

    static class QueryInventoryTask extends AsyncTask<String, Void, APIJSON<List<InventoryItem>>> {

        private final boolean isLoadMore;
        private final WeakReference<InventoryQueryActivity> mActivity;

        public QueryInventoryTask(InventoryQueryActivity activity, boolean isLoadMore) {
            this.isLoadMore = isLoadMore;
            this.mActivity = new WeakReference<>(activity);
        }

        @Override
        protected APIJSON<List<InventoryItem>> doInBackground(String... strings) {
            return RemoteAPI.InventoryQuery.queryInventoryList(strings[0], strings[1]);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mActivity.get().showProgressDialog();
        }

        @Override
        protected void onPostExecute(APIJSON<List<InventoryItem>> json) {
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
