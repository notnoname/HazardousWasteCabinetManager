package me.liuzs.cabinetmanager.ui.returnafteruse;

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
import me.liuzs.cabinetmanager.ReturnAfterUseActivity;
import me.liuzs.cabinetmanager.model.UsageItemInfo;
import me.liuzs.cabinetmanager.net.APIJSON;
import me.liuzs.cabinetmanager.net.RemoteAPI;


public class ReturnListFragment extends Fragment {

    public static final String TAG = "ReturnListFragment";

    private final ReturnListAdapter mAdapter;

    private final ReturnAfterUseActivity mActivity;

    public ReturnListFragment(ReturnAfterUseActivity activity) {
        super();
        mActivity = activity;
        mAdapter = new ReturnListAdapter(activity, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_return_list, container, false);
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
            new GetUsageItemListTask(this, mActivity).execute();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.i(TAG, "OnHiddenChanged:" + hidden);
        if (!hidden && !isHidden()) {
            new GetUsageItemListTask(this, mActivity).execute();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void submit() {
        CabinetCore.removeUnSubmitUsageInfo(mActivity);
        mActivity.finish();
    }

    static class GetUsageItemListTask extends AsyncTask<String, Void, List<UsageItemInfo>> {

        private final WeakReference<ReturnAfterUseActivity> mActivity;
        private final WeakReference<ReturnListFragment> mFragment;

        public GetUsageItemListTask(ReturnListFragment fragment, ReturnAfterUseActivity activity) {
            this.mActivity = new WeakReference<>(activity);
            this.mFragment = new WeakReference<>(fragment);
        }

        @Override
        protected List<UsageItemInfo> doInBackground(String... strings) {
            APIJSON<List<UsageItemInfo>> listJson = RemoteAPI.ReturnAfterUse.getUsageItemList(mActivity.get().getUsageInfo().putId);
            if (listJson.code == 200) {
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
        protected void onPostExecute(List<UsageItemInfo> result) {
            super.onPostExecute(result);
            mActivity.get().dismissProgressDialog();
            if (result != null) {
                mActivity.get().getUsageInfo().items.clear();
                mActivity.get().getUsageInfo().items.addAll(result);
                mFragment.get().mAdapter.notifyItemsChanged();
                mActivity.get().showUsageInfo();
            } else {
                mActivity.get().showToast("服务器错误!");
                mActivity.get().finish();
            }
        }
    }
}