package me.liuzs.cabinetmanager.ui.storage;

import static android.app.Activity.RESULT_OK;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import me.liuzs.cabinetmanager.CabinetCore;
import me.liuzs.cabinetmanager.ChemicalSearchActivity;
import me.liuzs.cabinetmanager.PrintActivity;
import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.SpinnerActivity;
import me.liuzs.cabinetmanager.StorageActivity;
import me.liuzs.cabinetmanager.WeightActivity;
import me.liuzs.cabinetmanager.model.Cabinet;
import me.liuzs.cabinetmanager.model.Chemical;
import me.liuzs.cabinetmanager.model.DepositItem;
import me.liuzs.cabinetmanager.model.DictType;
import me.liuzs.cabinetmanager.net.APIJSON;
import me.liuzs.cabinetmanager.net.RemoteAPI;
import me.liuzs.cabinetmanager.service.HardwareService;

public class DepositItemCreateFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "DepositItemCreateFragment";
    private final Gson mGson = new Gson();
    private final StorageActivity mActivity;
    private final ServiceConnection mLockerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "LockerServiceConnected success");
            HardwareService.HardwareServiceBinder binder = (HardwareService.HardwareServiceBinder) service;
            Cabinet info = CabinetCore.getCabinetInfo();
//            int index = CabinetCore.getDevIndex(info, mNewItem.devId);
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
    private DepositItem mOriginItem, mNewItem;
    private List<DictType> mUnitTypes, mPurityTypes, mMeasureSpecs;
    private TextView mReagentContainerNo, mDeviceName, mChemicalName, mControlCategory, mPurity, mUnit, mMeasureSpec;
    private Button mWeightBtn, mUnlock, mPrintLabel;
    private EditText mPackingSpec, mWeightValue;
    private final ActivityResultLauncher<Intent> mChemicalSearchLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            int resultCode = result.getResultCode();
            if (resultCode == RESULT_OK) {
                Intent data = result.getData();
                assert data != null;
                String chemicalJSON = data.getStringExtra(ChemicalSearchActivity.KEY_CHEMICAL_JSON);
                Chemical chemical = mGson.fromJson(chemicalJSON, Chemical.class);
                assert chemicalJSON != null;
                Log.d(TAG, chemicalJSON);
                mNewItem.casNo = chemical.casNo;
                mNewItem.chemicalId = chemical.chemicalId;
                mNewItem.chemicalName = chemical.chineseName;
                mNewItem.chemicalType = chemical.chemicalType;
                mNewItem.controlType = chemical.controlType;
                showItem();
            }
        }
    });
    private final ActivityResultLauncher<Intent> mUnitLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            int resultCode = result.getResultCode();
            if (resultCode == RESULT_OK) {
                Intent data = result.getData();
                assert data != null;
                String selectValue = data.getStringExtra(SpinnerActivity.KEY_SELECT_VALUE);
                assert selectValue != null;
                Log.d(TAG, selectValue);
                DictType dt = getDictTypeByLabel(mUnitTypes, selectValue);
                mNewItem.unit = dt.value;
                mNewItem.unitLabel = dt.label;
                showItem();
            }
        }
    });
    private final ActivityResultLauncher<Intent> mPurityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            int resultCode = result.getResultCode();
            if (resultCode == RESULT_OK) {
                Intent data = result.getData();
                assert data != null;
                String selectValue = data.getStringExtra(SpinnerActivity.KEY_SELECT_VALUE);
                assert selectValue != null;
                Log.d(TAG, selectValue);
                DictType dt = getDictTypeByLabel(mPurityTypes, selectValue);
                mNewItem.purity = dt.value;
                mNewItem.purityLabel = dt.label;
                showItem();
            }
        }
    });

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
                mNewItem.weight = selectValue;
                showItem();
            }
        }
    });

    private final ActivityResultLauncher<Intent> mMeasureSpecLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            int resultCode = result.getResultCode();
            if (resultCode == RESULT_OK) {
                Intent data = result.getData();
                assert data != null;
                String selectValue = data.getStringExtra(SpinnerActivity.KEY_SELECT_VALUE);
                assert selectValue != null;
                Log.d(TAG, selectValue);
                DictType dt = getDictTypeByLabel(mMeasureSpecs, selectValue);
                mNewItem.measureSpec = dt.value;
                mNewItem.measureSpecLabel = dt.label;
                showItem();
            }
        }
    });
    private final ActivityResultLauncher<Intent> mDeviceSelectLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        int resultCode = result.getResultCode();
        if (resultCode == RESULT_OK) {
            Intent data = result.getData();
            assert data != null;
            String selectValue = data.getStringExtra(SpinnerActivity.KEY_SELECT_VALUE);
            String[] devInfo = selectValue.split(" - ");
            String devId = devInfo[0];
            String devName = devInfo[1];
            mNewItem.devId = devId;
            mNewItem.devName = devName;
            showItem();
        }
    });
    private View mMask;

    public DepositItemCreateFragment(StorageActivity activity) {
        super();
        mActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_deposit_item_create, container, false);
        mMask = view.findViewById(R.id.vMask);
        mMask.setOnClickListener(v -> mActivity.showToast("此明细已经提交过，只能查看！"));
        mReagentContainerNo = view.findViewById(R.id.tvReagentContainerIdValue);
        mDeviceName = view.findViewById(R.id.tvDeviceName);
        mDeviceName.setOnClickListener(this);
        mChemicalName = view.findViewById(R.id.tvChemicalNameValue);
        mControlCategory = view.findViewById(R.id.tvControlCategoryValue);
        mChemicalName.setOnClickListener(this);
        mPurity = view.findViewById(R.id.tvPurityValue);
        mPurity.setOnClickListener(this);
        mUnit = view.findViewById(R.id.tvUnitValue);
        mUnit.setOnClickListener(this);
        mMeasureSpec = view.findViewById(R.id.tvMeasureSpecValue);
        mMeasureSpec.setOnClickListener(this);

        //暂时隐藏计量规格
        mMeasureSpec.setVisibility(View.GONE);
        view.findViewById(R.id.tvMeasureSpecLabel).setVisibility(View.GONE);

        mPackingSpec = view.findViewById(R.id.etPackingSpecValue);
        mPackingSpec.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mNewItem.specification = s.toString();
            }
        });
        mWeightValue = view.findViewById(R.id.etWeightValue);
        mWeightBtn = view.findViewById(R.id.btnWight);
        mWeightBtn.setOnClickListener(this);
        mUnlock = view.findViewById(R.id.btnUnlock);
        mUnlock.setOnClickListener(this);
        mPrintLabel = view.findViewById(R.id.btnPrint);
        mPrintLabel.setOnClickListener(this);
        mWeightValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mNewItem.weight = s.toString();
            }
        });
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
        onFragmentVisible();
    }

    private void onFragmentVisible() {
        mActivity.showDepositRecord();

//        DeviceInfo info = CabinetCore.getSingleDevice();
//        if (info != null) {
//            mDeviceName.setEnabled(false);
//            mNewItem.devId = info.devId;
//            mNewItem.devName = info.devName;
//        } else {
//            mDeviceName.setEnabled(true);
//        }

        if (!mNewItem.isInit) {
            if (mNewItem.conNo == null) {
                new GetContainerNoTask(this, mActivity).execute(String.valueOf(mActivity.getDepositRecord().putId));
            } else {
                new GetBaseInfoTask(this, mActivity).execute();
            }
        }
        mActivity.hideInputMethod();

        //当处于编辑状态的时候不给编辑，因为没有编辑接口
        if (mOriginItem != null) {
            mMask.setVisibility(View.VISIBLE);
        } else {
            mMask.setVisibility(View.INVISIBLE);
        }

    }

    private void showItem() {
        mReagentContainerNo.setText(mNewItem.conNo);
        mDeviceName.setText(mNewItem.devName);
        mChemicalName.setText(mNewItem.chemicalName);
        if (TextUtils.isEmpty(mNewItem.controlType)) {
            mControlCategory.setHint("无");
            mControlCategory.setText("");
        } else {
            mControlCategory.setText(mNewItem.controlType);
        }
        mPurity.setText(mNewItem.purityLabel);
        mUnit.setText(mNewItem.unitLabel);
        mMeasureSpec.setText(mNewItem.measureSpecLabel);
        mPackingSpec.setText(mNewItem.specification);
        mWeightValue.setText(mNewItem.weight);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void setOriginItem(DepositItem item) {
        this.mOriginItem = item;
        if (mOriginItem == null) {
            mNewItem = new DepositItem();
            mNewItem.putId = mActivity.getDepositRecord().putId;
        } else {
            String json = mGson.toJson(mOriginItem);
            mNewItem = mGson.fromJson(json, DepositItem.class);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mChemicalName) {
            Intent intent = new Intent(mActivity, ChemicalSearchActivity.class);
            intent.putExtra(ChemicalSearchActivity.KEY_CHEMICAL_NAME, mNewItem.chemicalName);
            mChemicalSearchLauncher.launch(intent);
        } else if (v == mDeviceName) {
            List<String> options = new ArrayList<>();
//            for (DeviceInfo dev : CabinetApplication.getInstance().getCabinetInfo().devices) {
//                options.add(dev.devId + " - " + dev.devName);
//            }
            Intent intent = new Intent(mActivity, SpinnerActivity.class);
            intent.putExtra(SpinnerActivity.KEY_OPTIONS, mGson.toJson(options));
            intent.putExtra(SpinnerActivity.KEY_TIP_INFO, "请选择存储区域：");
            mDeviceSelectLauncher.launch(intent);
        } else if (v == mUnit) {
            Intent intent = new Intent(mActivity, SpinnerActivity.class);
            List<String> options = new ArrayList<>();
            for (DictType dt : mUnitTypes) {
                options.add(dt.label);
            }
            intent.putExtra(SpinnerActivity.KEY_OPTIONS, mGson.toJson(options));
            intent.putExtra(SpinnerActivity.KEY_TIP_INFO, "请选择单位：");
            mUnitLauncher.launch(intent);
        } else if (v == mPurity) {
            Intent intent = new Intent(mActivity, SpinnerActivity.class);
            List<String> options = new ArrayList<>();
            for (DictType dt : mPurityTypes) {
                options.add(dt.label);
            }
            intent.putExtra(SpinnerActivity.KEY_OPTIONS, mGson.toJson(options));
            intent.putExtra(SpinnerActivity.KEY_TIP_INFO, "请选择纯度：");
            mPurityLauncher.launch(intent);
        } else if (v == mMeasureSpec) {
            Intent intent = new Intent(mActivity, SpinnerActivity.class);
            List<String> options = new ArrayList<>();
            for (DictType dt : mMeasureSpecs) {
                options.add(dt.label);
            }
            intent.putExtra(SpinnerActivity.KEY_OPTIONS, mGson.toJson(options));
            intent.putExtra(SpinnerActivity.KEY_TIP_INFO, "请选择计量规格：");
            mMeasureSpecLauncher.launch(intent);
        } else if (v == mWeightBtn) {
            Intent intent = new Intent(mActivity, WeightActivity.class);
            mWeightLauncher.launch(intent);
        } else if (v == mUnlock) {
            if (mNewItem.devId == null) {
                mActivity.showToast("请先选择容器");
                return;
            }
            Intent intent = new Intent();
            intent.setClassName(mActivity.getPackageName(), HardwareService.class.getName());
            mActivity.bindService(intent, mLockerServiceConnection, Context.BIND_AUTO_CREATE);
        } else if (v == mPrintLabel) {
            if (checkItemInfo()) {
//                PrintActivity.ContainerLabel label = new PrintActivity.ContainerLabel();
//                label.containerNo = mNewItem.conNo;
//                label.controlType = mNewItem.controlType;
//                label.chemicalCASNO = mNewItem.casNo;
//                label.chemicalName = mNewItem.chemicalName;
//                PrintActivity.startPrintContainerLabel(mActivity, label);
            } else {
                mActivity.showToast("信息不完整！");
            }
        }
    }

    public DictType getDictTypeByLabel(List<DictType> dictTypes, String label) {
        for (DictType dt : dictTypes) {
            if (label.equals(dt.label)) {
                return dt;
            }
        }
        return null;
    }

    public DictType getDictTypeByValue(List<DictType> dictTypes, String value) {
        for (DictType dt : dictTypes) {
            if (value.equals(dt.value)) {
                return dt;
            }
        }
        return null;
    }

    private void setDefaultValue() {
        if (mPurityTypes != null && mPurityTypes.size() > 0) {
            if (TextUtils.isEmpty(mNewItem.purity)) {
                DictType defaultPurityType = mPurityTypes.get(0);
                mNewItem.purity = defaultPurityType.value;
                mNewItem.purityLabel = defaultPurityType.label;
            } else if (!TextUtils.isEmpty(mNewItem.purity)) {
                DictType purityType = getDictTypeByValue(mPurityTypes, mNewItem.purity);
                if (purityType != null) {
                    mNewItem.purityLabel = purityType.label;
                }
            }
        }

        if (mUnitTypes != null && mUnitTypes.size() > 0) {
            if (TextUtils.isEmpty(mNewItem.unit)) {
                DictType defaultUnitType = mUnitTypes.get(0);
                mNewItem.unit = defaultUnitType.value;
                mNewItem.unitLabel = defaultUnitType.label;
            } else if (!TextUtils.isEmpty(mNewItem.unit)) {
                DictType unitType = getDictTypeByValue(mUnitTypes, mNewItem.unit);
                if (unitType != null) {
                    mNewItem.unitLabel = unitType.label;
                }
            }
        }

        if (mNewItem.specification == null) {
            mNewItem.specification = "500";
        }

        if (mMeasureSpecs != null && mMeasureSpecs.size() > 0) {
            if (TextUtils.isEmpty(mNewItem.measureSpec)) {
                DictType defaultMeasureSpec = mMeasureSpecs.get(0);
                mNewItem.measureSpec = defaultMeasureSpec.value;
                mNewItem.measureSpecLabel = defaultMeasureSpec.label;
            } else if (!TextUtils.isEmpty(mNewItem.measureSpec)) {
                DictType measureSpec = getDictTypeByValue(mMeasureSpecs, mNewItem.measureSpec);
                if (measureSpec != null) {
                    mNewItem.measureSpecLabel = measureSpec.label;
                }
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
        return !TextUtils.isEmpty(mNewItem.chemicalId)
                && !TextUtils.isEmpty(mNewItem.purity)
                && !TextUtils.isEmpty(mNewItem.unit)
                && !TextUtils.isEmpty(mNewItem.specification)
                && !TextUtils.isEmpty(mNewItem.measureSpec)
                && !TextUtils.isEmpty(mNewItem.devId)
                && !TextUtils.isEmpty(mNewItem.weight);
    }

    public void submitItem() {
        if (mOriginItem != null) {
            mActivity.showToast("已经提交过此明细！");
            return;
        }
        if (checkItemInfo()) {
            new SubmitItemTask(this, mActivity).execute(mNewItem);
        } else {
            mActivity.showToast("数据不完整，请检查！");
        }
    }

    static class BaseInfo {
        public APIJSON.Status status;
        public String reagentContainerNo;
        public List<DictType> unitTypes;
        public List<DictType> purityTypes;
        public List<DictType> measureSpecs;
    }

    static class GetContainerNoTask extends AsyncTask<String, Void, BaseInfo> {

        private final WeakReference<DepositItemCreateFragment> mFragment;
        private final WeakReference<StorageActivity> mActivity;

        public GetContainerNoTask(DepositItemCreateFragment fragment, StorageActivity activity) {
            this.mFragment = new WeakReference<>(fragment);
            this.mActivity = new WeakReference<>(activity);
        }

        @Override
        protected BaseInfo doInBackground(String... strings) {
            BaseInfo result = new BaseInfo();
            APIJSON<String> json = RemoteAPI.Storage.getContainerNo(strings[0]);
            if (json.status != APIJSON.Status.ok) {
                result.status = json.status;
                return result;
            } else {
                result.reagentContainerNo = json.data;
            }

            APIJSON<List<DictType>> unitTypes = RemoteAPI.BaseInfo.getUnitDictCode();
            if (unitTypes.status != APIJSON.Status.ok) {
                result.status = unitTypes.status;
                return result;
            } else {
                result.unitTypes = unitTypes.data;
            }
            APIJSON<List<DictType>> purityTypes = RemoteAPI.BaseInfo.getPurityDictCode();
            if (purityTypes.status != APIJSON.Status.ok) {
                result.status = purityTypes.status;
                return result;
            } else {
                result.purityTypes = purityTypes.data;
            }

            APIJSON<List<DictType>> measureSpecs = RemoteAPI.BaseInfo.getMeasureSpecDictCode();
            if (measureSpecs.status != APIJSON.Status.ok) {
                result.status = measureSpecs.status;
                return result;
            } else {
                result.measureSpecs = measureSpecs.data;
            }

            result.status = APIJSON.Status.ok;
            return result;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mActivity.get().showProgressDialog();
        }

        @Override
        protected void onPostExecute(BaseInfo info) {
            super.onPostExecute(info);
            mActivity.get().dismissProgressDialog();
            if (info.status == APIJSON.Status.ok) {
                mFragment.get().mNewItem.isInit = true;
                mFragment.get().mNewItem.conNo = info.reagentContainerNo;
                mFragment.get().mUnitTypes = info.unitTypes;
                mFragment.get().mPurityTypes = info.purityTypes;
                mFragment.get().mMeasureSpecs = info.measureSpecs;
                mFragment.get().setDefaultValue();
                mFragment.get().showItem();
            } else {
                mActivity.get().showToast("服务器错误：" + info.status);
                mActivity.get().transToFirstDepositFragment();
            }
        }
    }

    static class GetBaseInfoTask extends AsyncTask<Void, Void, BaseInfo> {

        private final WeakReference<DepositItemCreateFragment> mFragment;
        private final WeakReference<StorageActivity> mActivity;

        public GetBaseInfoTask(DepositItemCreateFragment fragment, StorageActivity activity) {
            this.mFragment = new WeakReference<>(fragment);
            this.mActivity = new WeakReference<>(activity);
        }

        @Override
        protected BaseInfo doInBackground(Void... v) {
            BaseInfo result = new BaseInfo();
            APIJSON<List<DictType>> unitTypes = RemoteAPI.BaseInfo.getUnitDictCode();
            if (unitTypes.status != APIJSON.Status.ok) {
                result.status = unitTypes.status;
                return result;
            } else {
                result.unitTypes = unitTypes.data;
            }
            APIJSON<List<DictType>> purityTypes = RemoteAPI.BaseInfo.getPurityDictCode();
            if (purityTypes.status != APIJSON.Status.ok) {
                result.status = purityTypes.status;
                return result;
            } else {
                result.purityTypes = purityTypes.data;
            }

            APIJSON<List<DictType>> measureSpecs = RemoteAPI.BaseInfo.getMeasureSpecDictCode();
            if (measureSpecs.status != APIJSON.Status.ok) {
                result.status = measureSpecs.status;
                return result;
            } else {
                result.measureSpecs = measureSpecs.data;
            }

            result.status = APIJSON.Status.ok;
            return result;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mActivity.get().showProgressDialog();
        }

        @Override
        protected void onPostExecute(BaseInfo info) {
            super.onPostExecute(info);
            mActivity.get().dismissProgressDialog();
            if (info.status == APIJSON.Status.ok) {
                mFragment.get().mNewItem.isInit = true;
                mFragment.get().mUnitTypes = info.unitTypes;
                mFragment.get().mPurityTypes = info.purityTypes;
                mFragment.get().mMeasureSpecs = info.measureSpecs;
                mFragment.get().setDefaultValue();
                mFragment.get().showItem();
            } else {
                mActivity.get().showToast("服务器错误：" + info.status);
                mActivity.get().transToFirstDepositFragment();
            }
        }
    }

    static class SubmitItemTask extends AsyncTask<DepositItem, Void, APIJSON<String>> {

        private final WeakReference<DepositItemCreateFragment> mFragment;
        private final WeakReference<StorageActivity> mActivity;

        public SubmitItemTask(DepositItemCreateFragment fragment, StorageActivity activity) {
            this.mFragment = new WeakReference<>(fragment);
            this.mActivity = new WeakReference<>(activity);
        }

        @Override
        protected APIJSON<String> doInBackground(DepositItem... item) {
            return RemoteAPI.Storage.saveDepositItemDetail(item[0]);
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
                if (mFragment.get().mOriginItem != null) {
                    mActivity.get().getDepositRecord().items.remove(mFragment.get().mOriginItem);
                }
                mActivity.get().getDepositRecord().items.add(mFragment.get().mNewItem);
                CabinetCore.saveUnSubmitDepositRecord(mActivity.get(), mActivity.get().getDepositRecord());
                mActivity.get().transToFirstDepositFragment();
            } else {
                mActivity.get().showToast("服务器错误：" + json.errors);
            }
        }
    }
}