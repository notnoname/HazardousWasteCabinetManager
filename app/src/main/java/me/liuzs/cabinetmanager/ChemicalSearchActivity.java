package me.liuzs.cabinetmanager;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.TreeMap;

import me.liuzs.cabinetmanager.model.Chemical;
import me.liuzs.cabinetmanager.net.APIJSON;
import me.liuzs.cabinetmanager.net.RemoteAPI;
import me.liuzs.cabinetmanager.ui.chemicalsearch.ChemicalSearchResultAdapter;
import me.liuzs.cabinetmanager.util.Util;

public class ChemicalSearchActivity extends BaseActivity {

    public static final String KEY_CHEMICAL_NAME = "KEY_CHEMICAL_NAME";
    public static final String KEY_CHEMICAL_JSON = "KEY_CHEMICAL_JSON";
    public static final String TAG = "ChemicalSearchActivity";
    private final ChemicalSearchResultAdapter mAdapter = new ChemicalSearchResultAdapter(this);
    private RecyclerView mRecyclerView;
    private SearchView mSearchView;

    private static void sortData(APIJSON<List<Chemical>> json) {
        List<Chemical> data = json.data;
        TreeMap<Integer, Chemical> map = new TreeMap<>();
        for (Chemical chemical : data) {
            int id = chemical.chineseName.length() * 1000;
            while (map.containsKey(id)) {
                id++;
            }
            map.put(id, chemical);
        }

        data.clear();
        data.addAll(map.values());
    }

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chemical_search);
        Util.fullScreen(this);

        mSearchView = findViewById(R.id.searchView);
        int id = mSearchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView textView = (TextView) mSearchView.findViewById(id);
        textView.setTextColor(Color.BLACK);
        textView.setHintTextColor(Color.parseColor("#CCCCCC"));

        mRecyclerView = findViewById(R.id.rvRecord);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                new QueryChemicalTask(ChemicalSearchActivity.this).execute(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    public void onBackButtonClick(View view) {
        finish();
    }

    static class QueryChemicalTask extends AsyncTask<String, Void, APIJSON<List<Chemical>>> {

        private final WeakReference<ChemicalSearchActivity> mActivity;

        public QueryChemicalTask(ChemicalSearchActivity activity) {
            this.mActivity = new WeakReference<>(activity);
        }

        @Override
        protected APIJSON<List<Chemical>> doInBackground(String... strings) {
            return RemoteAPI.BaseInfo.queryChemical(strings[0]);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mActivity.get().showProgressDialog();
        }

        @Override
        protected void onPostExecute(APIJSON<List<Chemical>> json) {
            super.onPostExecute(json);
            if (json.code == 200) {
                sortData(json);
                mActivity.get().mAdapter.setResult(json.data);
            } else {
                mActivity.get().showToast(json.message != null ? json.message : json.msg);
            }
            mActivity.get().dismissProgressDialog();
        }
    }
}
