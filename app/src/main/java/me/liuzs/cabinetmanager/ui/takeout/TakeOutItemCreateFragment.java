package me.liuzs.cabinetmanager.ui.takeout;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import me.liuzs.cabinetmanager.CabinetCore;
import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.SpinnerActivity;
import me.liuzs.cabinetmanager.TakeOutActivity;
import me.liuzs.cabinetmanager.model.Cabinet;
import me.liuzs.cabinetmanager.model.DictType;
import me.liuzs.cabinetmanager.model.TakeOutItemInfo;
import me.liuzs.cabinetmanager.net.APIJSON;
import me.liuzs.cabinetmanager.net.RemoteAPI;
import me.liuzs.cabinetmanager.service.HardwareService;

public class TakeOutItemCreateFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "TakeOutItemCreateFragment";
    private final Gson mGson = new Gson();
    private final List<TakeOutItemInfo> mContainerNoList = new LinkedList<>();
    private final TakeOutItemInfo mItemInfo = new TakeOutItemInfo();
    private final ActivityResultLauncher<Intent> mContainerSelectLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        int resultCode = result.getResultCode();
        if (resultCode == RESULT_OK) {
            Intent data = result.getData();
            assert data != null;
            String selectValue = data.getStringExtra(SpinnerActivity.KEY_SELECT_VALUE);
            try {
                String conNo = selectValue.split(" - ")[0];
                onContainerNoSelect(conNo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });
    private TakeOutActivity mActivity;
    private final ServiceConnection mLockerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "LockerServiceConnected success");
            HardwareService.HardwareServiceBinder binder = (HardwareService.HardwareServiceBinder) service;
            Cabinet info = CabinetCore.getCabinetInfo();
//            int index = CabinetCore.getDevIndex(info, mItemInfo.devId);
//            if (info.tankType == 1 && index != -1) {
//                SubBoard.ControlResult result = binder.getHardwareService().switchLockerControl(index);
//                mActivity.showToast(result);
//            } else {
//                binder.getHardwareService().switchLockerControl(true);
//            }
            mActivity.unbindService(this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mActivity.unbindService(mLockerServiceConnection);
            Log.d(TAG, "LockerServiceConnected fail");
        }
    };
    private TextView mDeviceName, mChemicalName, mControlCategory, mPurity, mUnit, mPackingSpec, mInvWeightValue, mTakeOutWeightValue, mPurposeValue;
    private Button mUnlock;
    private EditText mContainerNo;
    private final ActivityResultLauncher<Intent> mPurposeSelectLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        int resultCode = result.getResultCode();
        if (resultCode == RESULT_OK) {
            Intent data = result.getData();
            assert data != null;
            String selectValue = data.getStringExtra(SpinnerActivity.KEY_SELECT_VALUE);
            DictType type = mActivity.getDicTypeByLabel(mActivity.getPurposeTypes(), selectValue);
            if (type != null) {
                mItemInfo.purposeLabel = selectValue;
                mItemInfo.purpose = type.value;
            }
            showItem();
        }
    });

    private ImageButton mContainerSelect;

    public TakeOutItemCreateFragment(TakeOutActivity activity) {
        super();
        mActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_take_out_item_create, container, false);
        mContainerNo = view.findViewById(R.id.tvReagentContainerIdValue);
        mContainerNo.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                onContainerNoSelect(v.getText().toString());
            }
            return true;
        });
        mDeviceName = view.findViewById(R.id.tvDeviceName);
        mChemicalName = view.findViewById(R.id.tvChemicalNameValue);
        mControlCategory = view.findViewById(R.id.tvControlCategoryValue);
        mPurity = view.findViewById(R.id.tvPurityValue);
        mPurity.setOnClickListener(this);
        mUnit = view.findViewById(R.id.tvUnitValue);

        mPackingSpec = view.findViewById(R.id.tvPackingSpecValue);
        mInvWeightValue = view.findViewById(R.id.tvWeightValue);
        mTakeOutWeightValue = view.findViewById(R.id.tvRemainWeightValue);
        mUnlock = view.findViewById(R.id.btnUnlock);
        mUnlock.setOnClickListener(this);
        mContainerSelect = view.findViewById(R.id.btnContainerSelect);
        mContainerSelect.setOnClickListener(this);
        mPurposeValue = view.findViewById(R.id.tvPurposeValue);
        mPurposeValue.setOnClickListener(this);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "OnStart");
    }

    private void onContainerNoSelect(String conNo) {
        new GetContainerDetailTask().execute(conNo);
    }

    private void updateTakeOutItemInfo(TakeOutItemInfo detail) {
        if (detail != null) {
            mItemInfo.conNo = detail.conNo;
            mItemInfo.casNo = detail.casNo;
            mItemInfo.chemicalId = detail.chemicalId;
            mItemInfo.chemicalName = detail.chemicalName;
            mItemInfo.chemicalType = detail.chemicalType;
            mItemInfo.controlType = detail.controlType;
            mItemInfo.specification = detail.specification;
            mItemInfo.purity = detail.purity;
            mItemInfo.unit = detail.unit;
            mItemInfo.weight = detail.weight;
            mItemInfo.outWeight = detail.weight;
        } else {
            reset();
            mActivity.showToast("无数据");
        }
        showItem();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "OnResume");
        onFragmentVisible();
    }

    private void onFragmentVisible() {
        mItemInfo.devName = mActivity.getTakeOutInfo().devName;
        mItemInfo.devId = mActivity.getTakeOutInfo().devId;
        mActivity.showTakeOutInfo();
        mActivity.hideInputMethod();
        new GetContainerListTask().execute();
        showItem();
    }

    private void showItem() {
        mActivity.setDicTypeLabel(mItemInfo);
        mDeviceName.setText(mItemInfo.devName);
        mContainerNo.setText(mItemInfo.conNo);
        mChemicalName.setText(mItemInfo.chemicalName);
        if (TextUtils.isEmpty(mItemInfo.controlType)) {
            mControlCategory.setHint("无");
            mControlCategory.setText("");
        } else {
            mControlCategory.setText(mItemInfo.controlType);
        }
        mPurity.setText(mItemInfo.purityLabel);
        mUnit.setText(mItemInfo.unitLabel);
        mPackingSpec.setText(mItemInfo.specification);
        mInvWeightValue.setText(mItemInfo.weight);
        mTakeOutWeightValue.setText(mItemInfo.outWeight);
        mPurposeValue.setText(mItemInfo.purposeLabel);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {
        if (v == mUnlock) {
            if (mItemInfo.devId == null) {
                mActivity.showToast("请先选择容器");
                return;
            }
            Intent intent = new Intent();
            intent.setClassName(mActivity.getPackageName(), HardwareService.class.getName());
            mActivity.bindService(intent, mLockerServiceConnection, Context.BIND_AUTO_CREATE);
        } else if (v == mContainerSelect) {
            List<String> options = new ArrayList<>();
            for (TakeOutItemInfo info : mContainerNoList) {
                options.add(info.conNo + " - " + info.chemicalName);
            }
            if (options.size() == 0) {
                mActivity.showToast("无数据");
            } else {
                Intent intent = new Intent(mActivity, SpinnerActivity.class);
                intent.putExtra(SpinnerActivity.KEY_OPTIONS, mGson.toJson(options));
                intent.putExtra(SpinnerActivity.KEY_TIP_INFO, "请选择容器：");
                mContainerSelectLauncher.launch(intent);
            }
        } else if (v == mPurposeValue) {
            List<String> options = new ArrayList<>();
            for (DictType dictType : mActivity.getPurposeTypes()) {
                options.add(dictType.label);
            }
            if (options.size() == 0) {
                mActivity.showToast("无数据");
            } else {
                Intent intent = new Intent(mActivity, SpinnerActivity.class);
                intent.putExtra(SpinnerActivity.KEY_OPTIONS, mGson.toJson(options));
                intent.putExtra(SpinnerActivity.KEY_TIP_INFO, "请选择使用类型：");
                mPurposeSelectLauncher.launch(intent);
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.i(TAG, "OnHiddenChanged:" + hidden);
        if (!hidden) {
            onFragmentVisible();
        }
    }

    private boolean checkItemInfo() {
        return !TextUtils.isEmpty(mItemInfo.chemicalId)
                && !TextUtils.isEmpty(mItemInfo.purity)
                && !TextUtils.isEmpty(mItemInfo.unit)
                && !TextUtils.isEmpty(mItemInfo.specification)
                && !TextUtils.isEmpty(mItemInfo.weight)
                && !TextUtils.isEmpty(mItemInfo.devId)
                && !TextUtils.isEmpty(mItemInfo.purpose);
    }

    public void submitItem() {
        if (checkItemInfo()) {
            mItemInfo.outId = mActivity.getTakeOutInfo().outId;
            new SubmitItemTask(this, mActivity).execute(mItemInfo);
        } else {
            mActivity.showToast("数据不完整，请检查！");
        }
    }

    public void reset() {
        mItemInfo.conNo = null;
        mItemInfo.casNo = null;
        mItemInfo.chemicalId = null;
        mItemInfo.chemicalName = null;
        mItemInfo.chemicalType = null;
        mItemInfo.controlType = null;
        mItemInfo.specification = null;
        mItemInfo.purity = null;
        mItemInfo.unit = null;
        mItemInfo.weight = null;
        mItemInfo.outWeight = null;
    }

    static class SubmitItemTask extends AsyncTask<TakeOutItemInfo, Void, APIJSON<String>> {

        private final WeakReference<TakeOutItemCreateFragment> mFragment;
        private final WeakReference<TakeOutActivity> mActivity;

        public SubmitItemTask(TakeOutItemCreateFragment fragment, TakeOutActivity activity) {
            this.mFragment = new WeakReference<>(fragment);
            this.mActivity = new WeakReference<>(activity);
        }

        @Override
        protected APIJSON<String> doInBackground(TakeOutItemInfo... item) {
            return RemoteAPI.TakeOut.saveTakeOutItemDetail(item[0]);
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
            if (json.status == APIJSON.Status.ok) {
                if (mFragment.get().mItemInfo != null) {
                    mActivity.get().getTakeOutInfo().items.remove(mFragment.get().mItemInfo);
                }
                mActivity.get().getTakeOutInfo().items.add(mFragment.get().mItemInfo);
                CabinetCore.saveUnSubmitTakeOutInfo(mActivity.get(), mActivity.get().getTakeOutInfo());
                mActivity.get().transToTakeOutListFragment();
            } else {
                mActivity.get().showToast("服务器错误：" + json.status);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    class GetContainerDetailTask extends AsyncTask<String, Void, TakeOutItemInfo> {

        public GetContainerDetailTask() {
        }

        @Override
        protected TakeOutItemInfo doInBackground(String... v) {
            APIJSON<List<TakeOutItemInfo>> takeOutItemInfoJson = RemoteAPI.TakeOut.getContainerDetail(v[0]);
            if (takeOutItemInfoJson.status == APIJSON.Status.ok && takeOutItemInfoJson.data.size() > 0) {
                return takeOutItemInfoJson.data.get(0);
            } else {
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mActivity.showProgressDialog();
        }

        @Override
        protected void onPostExecute(TakeOutItemInfo info) {
            super.onPostExecute(info);
            mActivity.dismissProgressDialog();
            updateTakeOutItemInfo(info);
            mActivity.showTakeOutInfo();
        }
    }

    @SuppressLint("StaticFieldLeak")
    class GetContainerListTask extends AsyncTask<Void, Void, Boolean> {

        public GetContainerListTask() {
        }

        @Override
        protected Boolean doInBackground(Void... v) {
            APIJSON<List<TakeOutItemInfo>> inventoryItemListJSON = RemoteAPI.TakeOut.getContainerNoList(mActivity.getTakeOutInfo().devId);
            if (inventoryItemListJSON.status == APIJSON.Status.ok) {
                mContainerNoList.clear();
                mContainerNoList.addAll(inventoryItemListJSON.data);
                return true;
            } else {
                return false;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mActivity.showProgressDialog();
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            mActivity.dismissProgressDialog();
            if (success) {
                if (mContainerNoList.size() == 0) {
                    mActivity.showToast("当前存储柜没有可出柜化学品！");
                    mActivity.transToTakeOutListFragment();
                } else {
                    showItem();
                }
            } else {
                mActivity.showToast("服务器错误!");
                mActivity.transToTakeOutListFragment();
            }
            mActivity.showTakeOutInfo();
        }
    }
}