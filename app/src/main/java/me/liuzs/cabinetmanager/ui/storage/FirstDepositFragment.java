package me.liuzs.cabinetmanager.ui.storage;

import android.app.AlertDialog;
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

import me.liuzs.cabinetmanager.CtrlFunc;
import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.StorageActivity;
import me.liuzs.cabinetmanager.model.DepositItem;
import me.liuzs.cabinetmanager.net.APIJSON;
import me.liuzs.cabinetmanager.net.RemoteAPI;


public class FirstDepositFragment extends Fragment {

    public static final String TAG = "FirstDepositFragment";

    private final DepositRecordAdapter mAdapter;

    private final StorageActivity mActivity;

    public FirstDepositFragment(StorageActivity activity) {
        super();
        mActivity = activity;
        mAdapter = new DepositRecordAdapter(activity, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_first_deposit, container, false);
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
            new GetDepositItemListTask(this, mActivity).execute();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.i(TAG, "OnHiddenChanged:" + hidden);
        if (!hidden && !isHidden()) {
            new GetDepositItemListTask(this, mActivity).execute();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void deleteItem(DepositItem item) {
        new AlertDialog.Builder(mActivity).setMessage("是否删除当前入库条目？").setNegativeButton("确认", (dialog, which) -> new RemoveItemTask(FirstDepositFragment.this).execute(item)).setNeutralButton("取消", null).show();
    }

    public void submitDeposit() {
        if (mActivity.getDepositRecord().items.size() == 0) {
            mActivity.showToast("条目为空，请确认！");
        } else {
            new SubmitDepositTask(mActivity).execute();
        }
    }

    static class GetDepositItemListTask extends AsyncTask<String, Void, List<DepositItem>> {

        private final WeakReference<StorageActivity> mActivity;
        private final WeakReference<FirstDepositFragment> mFragment;

        public GetDepositItemListTask(FirstDepositFragment fragment, StorageActivity activity) {
            this.mActivity = new WeakReference<>(activity);
            this.mFragment = new WeakReference<>(fragment);
        }

        @Override
        protected List<DepositItem> doInBackground(String... strings) {
            APIJSON<List<DepositItem>> listJson = RemoteAPI.Storage.getDepositItemList(String.valueOf(mActivity.get().getDepositRecord().putId));
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
        protected void onPostExecute(List<DepositItem> result) {
            super.onPostExecute(result);
            mActivity.get().dismissProgressDialog();
            if (result != null) {
                mActivity.get().getDepositRecord().items.clear();
                mActivity.get().getDepositRecord().items.addAll(result);
                mFragment.get().mAdapter.notifyItemsChanged();
                mActivity.get().showDepositRecord();
            } else {
                mActivity.get().showToast("服务器错误!");
                mActivity.get().finish();
            }
        }
    }

    static class RemoveItemTask extends AsyncTask<DepositItem, Void, APIJSON<String>> {

        private final WeakReference<FirstDepositFragment> mFragment;
        private final WeakReference<StorageActivity> mActivity;
        private DepositItem mItem;

        public RemoveItemTask(FirstDepositFragment fragment) {
            this.mFragment = new WeakReference<>(fragment);
            this.mActivity = new WeakReference<>(fragment.mActivity);
        }

        @Override
        protected APIJSON<String> doInBackground(DepositItem... item) {
            this.mItem = item[0];
            return RemoteAPI.Storage.removeDepositItem(String.valueOf(item[0].detailId));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mActivity.get().showProgressDialog();
        }

        @Override
        protected void onPostExecute(APIJSON<String> json) {
            super.onPostExecute(json);
            mActivity.get().dismissProgressDialog();
            if (json.code == 200) {
                mActivity.get().getDepositRecord().items.remove(mItem);
                mActivity.get().showDepositRecord();
                mFragment.get().mAdapter.notifyItemsChanged();
            } else {
                mActivity.get().showToast("服务器错误：" + json.code);
            }
        }
    }

    static class SubmitDepositTask extends AsyncTask<Void, Void, APIJSON<String>> {

        private final WeakReference<StorageActivity> mActivity;

        public SubmitDepositTask(StorageActivity activity) {
            this.mActivity = new WeakReference<>(activity);
        }

        @Override
        protected APIJSON<String> doInBackground(Void... v) {
            return RemoteAPI.Storage.commitDeposit(String.valueOf(mActivity.get().getDepositRecord().putId));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mActivity.get().showProgressDialog();
        }

        @Override
        protected void onPostExecute(APIJSON<String> json) {
            super.onPostExecute(json);
            mActivity.get().dismissProgressDialog();
            if (json.code == 200) {
                CtrlFunc.removeUnSubmitDepositRecord(mActivity.get());
                mActivity.get().finish();
            } else {

                if (json.code == 500 && json.msg.contains("不允许重复入库")) {
                    CtrlFunc.removeUnSubmitDepositRecord(mActivity.get());
                    mActivity.get().showToast(json.msg);
                    mActivity.get().finish();
                } else {
                    mActivity.get().showToast("服务器错误：" + json.code);
                }
            }
        }
    }

}