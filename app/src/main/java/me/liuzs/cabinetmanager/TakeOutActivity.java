package me.liuzs.cabinetmanager;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Lifecycle;

import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;

import me.liuzs.cabinetmanager.model.Cabinet;
import me.liuzs.cabinetmanager.model.DictType;
import me.liuzs.cabinetmanager.model.StorageLaboratoryDetail;
import me.liuzs.cabinetmanager.model.TakeOutInfo;
import me.liuzs.cabinetmanager.model.TakeOutItemInfo;
import me.liuzs.cabinetmanager.net.APIJSON;
import me.liuzs.cabinetmanager.net.RemoteAPI;
import me.liuzs.cabinetmanager.ui.takeout.TakeOutItemCreateFragment;
import me.liuzs.cabinetmanager.ui.takeout.TakeOutListFragment;
import me.liuzs.cabinetmanager.util.Util;

public class TakeOutActivity extends BaseActivity {

    public final static String TAG = "TakeOutActivity";
    private final TakeOutListFragment mTakeOutListFragment = new TakeOutListFragment(this);
    private final TakeOutItemCreateFragment mTakeOutItemCreateFragment = new TakeOutItemCreateFragment(this);
    private final Gson mGson = new Gson();
    private final ActivityResultLauncher<Intent> mDeviceSelectLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        int resultCode = result.getResultCode();
        if (resultCode == RESULT_OK) {
            Intent data = result.getData();
            assert data != null;
            String selectValue = data.getStringExtra(SpinnerActivity.KEY_SELECT_VALUE);
            String devId = selectValue.split(" - ")[0];
            new CreateTakeOutInfoTask(this).execute(devId);
        } else {
            finish();
        }
    });
    private TakeOutInfo mTakeOutInfo;
    private TextView mUser, mTakeOutNo, mTakeOutCount, mCreateTime, mDeviceName;
    private LinearLayout mBottomMenu;
    private ImageButton mToolBack;
    private List<DictType> mUnitTypes, mPurityTypes, mPurposeTypes;

    public TakeOutInfo getTakeOutInfo() {
        return mTakeOutInfo;
    }

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_out);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(null);
        Util.fullScreen(this);

        mUser = findViewById(R.id.tvUserValue);
        mDeviceName = findViewById(R.id.tvDeviceName);
        mTakeOutNo = findViewById(R.id.tvNoValue);
        mTakeOutCount = findViewById(R.id.tvTakeOutCountValue);
        mCreateTime = findViewById(R.id.tvCreateTimeValue);
        mBottomMenu = findViewById(R.id.llBottom);
        mToolBack = findViewById(R.id.toolbar_back);
        new GetBaseInfoTask(this).execute();
    }

    public void showTakeOutInfo() {
        mDeviceName.setText(mTakeOutInfo.devName);
        mUser.setText(mTakeOutInfo.userName);
        mTakeOutNo.setText(mTakeOutInfo.outNo);
        mTakeOutCount.setText(String.valueOf(mTakeOutInfo.items.size()));
        mCreateTime.setText(mTakeOutInfo.createTime);

        if (!mTakeOutListFragment.isHidden()) {
            mBottomMenu.setVisibility(View.VISIBLE);
            mToolBack.setVisibility(View.VISIBLE);
        } else if (!mTakeOutItemCreateFragment.isHidden()) {
            mBottomMenu.setVisibility(View.VISIBLE);
            mToolBack.setVisibility(View.INVISIBLE);
        }
    }

    private void hideAllFragment() {
        if (mTakeOutItemCreateFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction().hide(mTakeOutItemCreateFragment).commit();
        }
        if (mTakeOutListFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction().hide(mTakeOutListFragment).commit();
        }
    }

    public void transToTakeOutItemCreateFragment() {
        hideAllFragment();
        if (mTakeOutItemCreateFragment.isAdded()) {
            mTakeOutItemCreateFragment.reset();
            getSupportFragmentManager().beginTransaction().show(mTakeOutItemCreateFragment).setMaxLifecycle(mTakeOutItemCreateFragment, Lifecycle.State.RESUMED).commit();
        } else {
            getSupportFragmentManager().beginTransaction().add(R.id.container, mTakeOutItemCreateFragment).commit();
        }
    }

    public void transToTakeOutListFragment() {
        hideAllFragment();
        if (mTakeOutListFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction().show(mTakeOutListFragment).setMaxLifecycle(mTakeOutListFragment, Lifecycle.State.RESUMED).commit();
        } else {
            getSupportFragmentManager().beginTransaction().add(R.id.container, mTakeOutListFragment).commit();
        }
    }

    public void onBackButtonClick(View view) {
        if (mTakeOutListFragment.isHidden()) {
            transToTakeOutListFragment();
        } else {
            finish();
        }
    }

    public void onMenuBackButtonClick(View view) {
        finish();
    }

    public void onSubmitButtonClick(View view) {
        if (mTakeOutListFragment.isHidden()) {
            mTakeOutItemCreateFragment.submitItem();
        } else {
            mTakeOutListFragment.submit();
        }
    }

    public DictType getDictTypeByValue(List<DictType> dictTypes, String value) {
        for (DictType dt : dictTypes) {
            if (value.equals(dt.value)) {
                return dt;
            }
        }
        return null;
    }

    public DictType getDicTypeByLabel(List<DictType> dictTypes, String label) {
        for (DictType dt : dictTypes) {
            if (label.equals(dt.label)) {
                return dt;
            }
        }
        return null;
    }

    public DictType getUnitLabelByValue(String value) {
        return getDictTypeByValue(mUnitTypes, value);
    }

    public DictType getPurityLabelByValue(String value) {
        return getDictTypeByValue(mPurityTypes, value);
    }

    public DictType getPurposeLabelByValue(String value) {
        return getDictTypeByValue(mPurposeTypes, value);
    }

    public void setDicTypeLabel(TakeOutItemInfo info) {
        if (TextUtils.isEmpty(info.purity)) {
            info.purity = null;
            info.purityLabel = null;
        } else if (!TextUtils.isEmpty(info.purity)) {
            DictType purityType = getPurityLabelByValue(info.purity);
            if (purityType != null) {
                info.purityLabel = purityType.label;
            }
        }

        if (TextUtils.isEmpty(info.unit)) {
            info.unit = null;
            info.unitLabel = null;
        } else if (!TextUtils.isEmpty(info.unit)) {
            DictType unitType = getUnitLabelByValue(info.unit);
            if (unitType != null) {
                info.unitLabel = unitType.label;
            }
        }

        if (TextUtils.isEmpty(info.purpose)) {
            info.purpose = null;
            info.purposeLabel = null;
        } else if (!TextUtils.isEmpty(info.purpose)) {
            DictType purposeType = getPurposeLabelByValue(info.purpose);
            if (purposeType != null) {
                info.purposeLabel = purposeType.label;
            }
        }
    }

    public List<DictType> getPurposeTypes() {
        return mPurposeTypes;
    }

    static class CreateTakeOutInfoTask extends AsyncTask<String, Void, TakeOutInfo> {

        private final WeakReference<TakeOutActivity> mActivity;

        public CreateTakeOutInfoTask(TakeOutActivity activity) {
            this.mActivity = new WeakReference<>(activity);
        }

        @Override
        protected TakeOutInfo doInBackground(String... args) {
            Cabinet cabinet = CabinetCore.getCabinetInfo();
            TakeOutInfo result = new TakeOutInfo();

            APIJSON<StorageLaboratoryDetail> storageLaboratoryDetailJSON = RemoteAPI.TakeOut.getStorageLaboratoryDetail();
            if (storageLaboratoryDetailJSON.status != APIJSON.Status.ok) {
                return null;
            } else {
                StorageLaboratoryDetail detail = storageLaboratoryDetailJSON.data;
                result.userName = detail.safetyPerson;
                result.outLabId = detail.labId;
                result.address = detail.address;
                result.labName = detail.labName;
            }

            APIJSON<String> takeOutNoJson = RemoteAPI.TakeOut.createTakeOutTask(result.address, args[0], 0);
            if (takeOutNoJson.status != APIJSON.Status.ok) {
                return null;
            }

            TakeOutInfo takeOutInfo = null;
            APIJSON<List<TakeOutInfo>> takeOutInfoListJson = RemoteAPI.TakeOut.getLastTakeOutTaskList();
            if (takeOutInfoListJson.status != APIJSON.Status.ok || takeOutInfoListJson.data == null || takeOutInfoListJson.data.size() == 0) {
                return null;
            } else {
//                for (TakeOutInfo info : takeOutInfoListJson.data) {
//                    if (TextUtils.equals(info.devId, args[0]) && TextUtils.equals(info.tankId, cabinet.tankId)) {
//                        takeOutInfo = info;
//                        break;
//                    }
//                }
            }

            if (takeOutInfo == null) {
                return null;
            }
            result.createTime = takeOutInfo.createTime;
            result.devId = takeOutInfo.devId;
            result.devName = takeOutInfo.devName;
            result.outId = takeOutInfo.outId;
            result.outLabId = takeOutInfo.outLabId;
            result.outNo = takeOutInfo.outNo;
            result.tankId = takeOutInfo.tankId;
            result.tankName = takeOutInfo.tankName;
            result.userName = takeOutInfo.userName;
            return result;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mActivity.get().showProgressDialog();
        }

        @Override
        protected void onPostExecute(TakeOutInfo result) {
            super.onPostExecute(result);
            mActivity.get().dismissProgressDialog();
            if (result != null) {
                mActivity.get().mTakeOutInfo = result;
                CabinetCore.saveUnSubmitTakeOutInfo(mActivity.get(), mActivity.get().mTakeOutInfo);
                mActivity.get().transToTakeOutListFragment();
                mActivity.get().showTakeOutInfo();
            } else {
                mActivity.get().showToast("服务器错误!");
                mActivity.get().finish();
            }
        }
    }

    static class GetBaseInfoTask extends AsyncTask<Void, Void, Boolean> {

        private final WeakReference<TakeOutActivity> mActivity;

        public GetBaseInfoTask(TakeOutActivity activity) {
            this.mActivity = new WeakReference<>(activity);
        }

        @Override
        protected Boolean doInBackground(Void... v) {
            APIJSON<List<DictType>> unitTypes = RemoteAPI.BaseInfo.getUnitDictCode();
            if (unitTypes.status != APIJSON.Status.ok) {
                return false;
            } else {
                mActivity.get().mUnitTypes = unitTypes.data;
            }
            APIJSON<List<DictType>> purityTypes = RemoteAPI.BaseInfo.getPurityDictCode();
            if (purityTypes.status != APIJSON.Status.ok) {
                return false;
            } else {
                mActivity.get().mPurityTypes = purityTypes.data;
            }

            APIJSON<List<DictType>> purposeTypes = RemoteAPI.BaseInfo.getPurposeDictCode();
            if (purposeTypes.status != APIJSON.Status.ok) {
                return false;
            } else {
                mActivity.get().mPurposeTypes = purposeTypes.data;
            }
            return true;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mActivity.get().showProgressDialog();
        }

        @Override
        protected void onPostExecute(Boolean info) {
            super.onPostExecute(info);
            mActivity.get().dismissProgressDialog();
            if (info) {
                mActivity.get().mTakeOutInfo = CabinetCore.getUnSubmitTakeOutInfo(mActivity.get());
                if (mActivity.get().mTakeOutInfo == null) {
//                    DeviceInfo deviceInfo = CabinetApplication.getSingleDevice();
//                    if (deviceInfo != null) {
//                        new CreateTakeOutInfoTask(mActivity.get()).execute(deviceInfo.devId);
//                    } else {
//                        List<String> options = new ArrayList<>();
//                        for (DeviceInfo dev : CabinetApplication.getInstance().getCabinetInfo().devices) {
//                            options.add(dev.devId + " - " + dev.devName);
//                        }
//                        Intent intent = new Intent(mActivity.get(), SpinnerActivity.class);
//                        intent.putExtra(SpinnerActivity.KEY_OPTIONS, mActivity.get().mGson.toJson(options));
//                        intent.putExtra(SpinnerActivity.KEY_TIP_INFO, "请先选择存储区域：");
//                        mActivity.get().mDeviceSelectLauncher.launch(intent);
//                    }
                } else {
                    mActivity.get().transToTakeOutListFragment();
                    mActivity.get().showTakeOutInfo();
                }
            } else {
                mActivity.get().showToast("服务器错误!");
                mActivity.get().finish();
            }
        }
    }

}