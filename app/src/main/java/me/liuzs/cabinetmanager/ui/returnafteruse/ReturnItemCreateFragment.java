package me.liuzs.cabinetmanager.ui.returnafteruse;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.liuzs.cabinetmanager.CabinetCore;
import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.ReturnAfterUseActivity;
import me.liuzs.cabinetmanager.SpinnerActivity;
import me.liuzs.cabinetmanager.WeightActivity;
import me.liuzs.cabinetmanager.model.Cabinet;
import me.liuzs.cabinetmanager.model.UsageItemInfo;
import me.liuzs.cabinetmanager.net.APIJSON;
import me.liuzs.cabinetmanager.net.RemoteAPI;
import me.liuzs.cabinetmanager.service.HardwareService;

public class ReturnItemCreateFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "ReturnItemCreateFragment";
    private final Gson mGson = new Gson();
    private final ReturnAfterUseActivity mActivity;
    private final Map<String, UsageItemInfo> mContainerNoList = new HashMap<>();
    private final ActivityResultLauncher<Intent> mContainerSelectLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        int resultCode = result.getResultCode();
        if (resultCode == RESULT_OK) {
            Intent data = result.getData();
            assert data != null;
            String selectValue = data.getStringExtra(SpinnerActivity.KEY_SELECT_VALUE).split(" - ")[0];
            UsageItemInfo info = mContainerNoList.get(selectValue);
            if (info != null) {
                onContainerNoSelect(info.conNo, info.outWeight);
            }
        }
    });
    private UsageItemInfo mItem;
    private final ServiceConnection mLockerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "LockerServiceConnected success");
            HardwareService.HardwareServiceBinder binder = (HardwareService.HardwareServiceBinder) service;
            Cabinet info = CabinetCore.getCabinetInfo();
//            int index = CabinetCore.getDevIndex(info, mItem.devId);
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
    private TextView mDeviceName, mChemicalName, mControlCategory, mPurity, mUnit, mPackingSpec, mOutWeightValue, mUsedWeightValue;
    private Button mWeightBtn, mUnlock;
    private EditText mReagentContainerNo, mRemainWeightValue;
    private final ActivityResultLauncher<Intent> mWeightLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            int resultCode = result.getResultCode();
            if (resultCode == RESULT_OK) {
                Intent data = result.getData();
                assert data != null;
                String selectValue = data.getStringExtra(WeightActivity.KEY_SELECT_VALUE);
                assert selectValue != null;
                Log.d(TAG, selectValue);
                mRemainWeightValue.setText(selectValue);
                showItem(false);
            }
        }
    });
    private ImageButton mContainerSelect;

    public ReturnItemCreateFragment(ReturnAfterUseActivity activity) {
        super();
        mActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_usage_item_create, container, false);
        mReagentContainerNo = view.findViewById(R.id.tvReagentContainerIdValue);
        mReagentContainerNo.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                UsageItemInfo info = mContainerNoList.get(v.getText().toString());
                if (info != null) {
                    onContainerNoSelect(info.conNo, info.outWeight);
                } else {
                    resetItem();
                    showItem(true);
                }
            }
            return true;
        });
        mDeviceName = view.findViewById(R.id.tvDeviceValue);
        mChemicalName = view.findViewById(R.id.tvChemicalNameValue);
        mControlCategory = view.findViewById(R.id.tvControlCategoryValue);
        mPurity = view.findViewById(R.id.tvPurityValue);
        mPurity.setOnClickListener(this);
        mUnit = view.findViewById(R.id.tvUnitValue);

        mPackingSpec = view.findViewById(R.id.tvPackingSpecValue);
        mOutWeightValue = view.findViewById(R.id.tvWeightValue);
        mUsedWeightValue = view.findViewById(R.id.tvUsedWeightValue);
        mWeightBtn = view.findViewById(R.id.btnWight);
        mWeightBtn.setOnClickListener(this);
        mUnlock = view.findViewById(R.id.btnUnlock);
        mUnlock.setOnClickListener(this);
        mContainerSelect = view.findViewById(R.id.btnContainerSelect);
        mContainerSelect.setOnClickListener(this);
        mRemainWeightValue = view.findViewById(R.id.etRemainWeightValue);
        mRemainWeightValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                setRemainWeight(s.toString());
                showItem(false);
            }
        });
        return view;
    }

    private void setRemainWeight(String s) {
        try {
            float remWeight = Float.parseFloat(s);
            float outWeight = Float.parseFloat(mItem.outWeight);
            if (remWeight < 0) {
                remWeight = 0;
            }
            if (remWeight > outWeight) {
                remWeight = outWeight;
            }
            mItem.putWeight = String.valueOf(remWeight);
            mItem.useWeight = String.valueOf(outWeight - remWeight);
        } catch (Exception e) {
            mItem.putWeight = null;
            mItem.useWeight = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "OnStart");
    }

    private void onContainerNoSelect(String conNo, String outWeight) {
        new GetContainerDetailTask().execute(conNo, outWeight);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "OnResume");
        onFragmentVisible();
    }

    private void onFragmentVisible() {
        mActivity.showUsageInfo();
        mActivity.hideInputMethod();
        new GetContainerListTask().execute();
    }

    private void showItem(boolean refreshRemainWeight) {
        mActivity.setDicTypeLabel(mItem);
        mReagentContainerNo.setText(mItem.conNo);
        mDeviceName.setText(mItem.devName);
        mChemicalName.setText(mItem.chemicalName);
        if (TextUtils.isEmpty(mItem.controlType)) {
            mControlCategory.setHint("无");
            mControlCategory.setText("");
        } else {
            mControlCategory.setText(mItem.controlType);
        }
        mPurity.setText(mItem.purityLabel);
        mUnit.setText(mItem.unitLabel);
        mPackingSpec.setText(mItem.specification);
        mOutWeightValue.setText(mItem.outWeight);
        if (refreshRemainWeight) {
            mRemainWeightValue.setText(mItem.putWeight);
        }
        mUsedWeightValue.setText(mItem.useWeight);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void initItem(String putId, String tankId) {
        mItem = new UsageItemInfo();
        mItem.putId = putId;
        mItem.tankId = tankId;
    }

    @Override
    public void onClick(View v) {
        if (v == mWeightBtn) {
            if (TextUtils.isEmpty(mItem.chemicalName)) {
                mActivity.showToast("请先选择容器！");
            } else {
                Intent intent = new Intent(mActivity, WeightActivity.class);
                mWeightLauncher.launch(intent);
            }
        } else if (v == mUnlock) {
            if (mItem.devId == null) {
                mActivity.showToast("请先选择容器");
                return;
            }
            Intent intent = new Intent();
            intent.setClassName(mActivity.getPackageName(), HardwareService.class.getName());
            mActivity.bindService(intent, mLockerServiceConnection, Context.BIND_AUTO_CREATE);
        } else if (v == mContainerSelect) {
            List<String> options = new ArrayList<>(mContainerNoList.size());
            for (UsageItemInfo info : mContainerNoList.values()) {
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
        return !TextUtils.isEmpty(mItem.conNo)
                && !TextUtils.isEmpty(mItem.putId)
                && !TextUtils.isEmpty(mItem.devId)
                && !TextUtils.isEmpty(mItem.putWeight)
                && !TextUtils.isEmpty(mItem.tankId);
    }

    public void submitItem() {
        if (checkItemInfo()) {
            new SubmitItemTask(this, mActivity).execute(mItem);
        } else {
            mActivity.showToast("数据不完整，请检查！");
        }
    }

    private void resetItem() {
        mItem.devId = null;
        mItem.devName = null;
        mItem.chemicalName = null;
        mItem.chemicalId = null;
        mItem.controlType = null;
        mItem.conNo = null;
        mItem.unit = null;
        mItem.unitLabel = null;
        mItem.purity = null;
        mItem.purityLabel = null;
        mItem.specification = null;
        mItem.outWeight = null;
        mItem.useWeight = null;
    }

    static class SubmitItemTask extends AsyncTask<UsageItemInfo, Void, APIJSON<String>> {

        private final WeakReference<ReturnItemCreateFragment> mFragment;
        private final WeakReference<ReturnAfterUseActivity> mActivity;

        public SubmitItemTask(ReturnItemCreateFragment fragment, ReturnAfterUseActivity activity) {
            this.mFragment = new WeakReference<>(fragment);
            this.mActivity = new WeakReference<>(activity);
        }

        @Override
        protected APIJSON<String> doInBackground(UsageItemInfo... item) {
           // return RemoteAPI.ContainerNoManager.saveUsageItemDetail(item[0]);
            return null;
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
                mActivity.get().getUsageInfo().items.add(mFragment.get().mItem);
                CabinetCore.saveUnSubmitUsageInfo(mActivity.get(), mActivity.get().getUsageInfo());
                mActivity.get().transToReturnListFragment();
            } else {
                mActivity.get().showToast("服务器错误：" + json.status);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    class GetContainerDetailTask extends AsyncTask<String, Void, UsageItemInfo> {

        public GetContainerDetailTask() {
        }

        @Override
        protected UsageItemInfo doInBackground(String... v) {
            APIJSON<List<UsageItemInfo>> infoListJson = null; //RemoteAPI.ContainerNoManager.getContainerDetail(v[0]);
//            if (infoListJson.code == 200 && infoListJson.data.size() > 0) {
//                UsageItemInfo result = infoListJson.data.get(0);
//                if (CabinetCore.isInThisTank(CabinetApplication.getInstance().getCabinetInfo(), result.devId)) {
//                    result.conNo = v[0];
//                    result.outWeight = v[1];
//                    return result;
//                } else {
//                    return null;
//                }
//            } else {
//                return null;
//            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mActivity.showProgressDialog();
        }

        @Override
        protected void onPostExecute(UsageItemInfo info) {
            super.onPostExecute(info);
            mActivity.dismissProgressDialog();
            if (info == null) {
                resetItem();
                mActivity.showToast("此容器不属于此存储柜，请确认后再入柜！");
            } else {
                mItem.conNo = info.conNo;
                mItem.chemicalName = info.chemicalName;
                mItem.controlType = info.controlType;
                mItem.purity = info.purity;
                mItem.unit = info.unit;
                mItem.specification = info.specification;
                mItem.outWeight = info.outWeight;
                mItem.putWeight = "";
                mItem.useWeight = "";
                mItem.devName = info.devName;
                mItem.devId = info.devId;
            }
            showItem(true);
            mActivity.showUsageInfo();
        }
    }

    @SuppressLint("StaticFieldLeak")
    class GetContainerListTask extends AsyncTask<Void, Void, Boolean> {

        public GetContainerListTask() {
        }

        @Override
        protected Boolean doInBackground(Void... v) {
            APIJSON<List<UsageItemInfo>> listJSON = null; // RemoteAPI.ContainerNoManager.getContainerNoList();
            if (listJSON.status == APIJSON.Status.ok) {
                mContainerNoList.clear();
                for (UsageItemInfo info : listJSON.data) {
                    mContainerNoList.put(info.conNo, info);
                }
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
                    mActivity.showToast("当前存储柜没有可入柜化学品！");
                    mActivity.transToReturnListFragment();
                }
            } else {
                mActivity.showToast("服务器错误!");
                mActivity.transToReturnListFragment();
            }
            showItem(false);
        }
    }
}