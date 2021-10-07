package me.liuzs.cabinetmanager.ui.takeout;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.List;

import me.liuzs.cabinetmanager.CabinetCore;
import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.TakeOutActivity;
import me.liuzs.cabinetmanager.model.TakeOutItemInfo;
import me.liuzs.cabinetmanager.net.APIJSON;
import me.liuzs.cabinetmanager.net.RemoteAPI;


public class TakeOutListFragment extends Fragment {

    public static final String TAG = "TakeOutListFragment";

    private final TakeOutListAdapter mAdapter;

    private final TakeOutActivity mActivity;

    public TakeOutListFragment(TakeOutActivity activity) {
        super();
        mActivity = activity;
        mAdapter = new TakeOutListAdapter(activity, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_take_out_list, container, false);
        RecyclerView mRecyclerView = view.findViewById(R.id.rvRecord);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "OnStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "OnResume");
        if (!isHidden()) {
            new GetTakeOutItemListTask(this, mActivity).execute();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.i(TAG, "OnHiddenChanged:" + hidden);
        if (!hidden && !isHidden()) {
            new GetTakeOutItemListTask(this, mActivity).execute();
        }
    }

    public void submit() {
        CabinetCore.removeUnSubmitTakeOutInfo(mActivity);
        mActivity.finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    static class GetTakeOutItemListTask extends AsyncTask<String, Void, List<TakeOutItemInfo>> {

        private final WeakReference<TakeOutActivity> mActivity;
        private final WeakReference<TakeOutListFragment> mFragment;

        public GetTakeOutItemListTask(TakeOutListFragment fragment, TakeOutActivity activity) {
            this.mActivity = new WeakReference<>(activity);
            this.mFragment = new WeakReference<>(fragment);
        }

        @Override
        protected List<TakeOutItemInfo> doInBackground(String... strings) {
            APIJSON<List<TakeOutItemInfo>> listJson = RemoteAPI.TakeOut.getTakeOutItemList(String.valueOf(mActivity.get().getTakeOutInfo().outId));
            if (listJson.status == APIJSON.Status.ok) {
                return listJson.data;
            } else {
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mActivity.get().showProgressDialog();
        }

        @Override
        protected void onPostExecute(List<TakeOutItemInfo> result) {
            super.onPostExecute(result);
            mActivity.get().dismissProgressDialog();
            if (result != null) {
                mActivity.get().getTakeOutInfo().items.clear();
                mActivity.get().getTakeOutInfo().items.addAll(result);
                mFragment.get().mAdapter.notifyItemsChanged();
                mActivity.get().showTakeOutInfo();
            } else {
                mActivity.get().showToast("服务器错误!");
                mActivity.get().finish();
            }
        }
    }
}