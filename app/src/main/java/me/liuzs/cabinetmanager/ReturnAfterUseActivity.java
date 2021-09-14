package me.liuzs.cabinetmanager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Lifecycle;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;

import me.liuzs.cabinetmanager.model.CabinetInfo;
import me.liuzs.cabinetmanager.model.DictType;
import me.liuzs.cabinetmanager.model.StorageLaboratoryDetail;
import me.liuzs.cabinetmanager.model.UsageInfo;
import me.liuzs.cabinetmanager.model.UsageItemInfo;
import me.liuzs.cabinetmanager.net.APIJSON;
import me.liuzs.cabinetmanager.net.RemoteAPI;
import me.liuzs.cabinetmanager.ui.returnafteruse.ReturnItemCreateFragment;
import me.liuzs.cabinetmanager.ui.returnafteruse.ReturnListFragment;
import me.liuzs.cabinetmanager.util.Util;

public class ReturnAfterUseActivity extends BaseActivity {

    public final static String TAG = "ReturnAfterUseActivity";
    private final ReturnListFragment mReturnListFragment = new ReturnListFragment(this);
    private final ReturnItemCreateFragment mReturnItemCreateFragment = new ReturnItemCreateFragment(this);
    private UsageInfo mUsageInfo;
    private TextView mLaboratoryName, mUser, mUsageNo, mUsageCount;
    private LinearLayout mBottomMenu;
    private ImageButton mToolBack;
    private List<DictType> mUnitTypes, mPurityTypes, mMeasureSpecs;

    public UsageInfo getUsageInfo() {
        return mUsageInfo;
    }

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return_after_user);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(null);
        Util.fullScreen(this);

        mLaboratoryName = findViewById(R.id.tvUsageUnitValue);
        mUser = findViewById(R.id.tvUserValue);
        mUsageNo = findViewById(R.id.tvUsageNoValue);
        mUsageCount = findViewById(R.id.tvUsageCountValue);
        mBottomMenu = findViewById(R.id.llBottom);
        mToolBack = findViewById(R.id.toolbar_back);
        new GetBaseInfoTask(this).execute();
    }

    public void showUsageInfo() {
        mLaboratoryName.setText(mUsageInfo.labName);
        mUser.setText(mUsageInfo.userName);
        mUsageNo.setText(mUsageInfo.putNo);
        mUsageCount.setText(String.valueOf(mUsageInfo.items.size()));

        if (!mReturnListFragment.isHidden()) {
            mToolBack.setVisibility(View.VISIBLE);
            mBottomMenu.setVisibility(View.VISIBLE);
        } else if (!mReturnItemCreateFragment.isHidden()) {
            mBottomMenu.setVisibility(View.VISIBLE);
            mToolBack.setVisibility(View.INVISIBLE);
        }
    }

    private void hideAllFragment() {
        if (mReturnItemCreateFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction().hide(mReturnItemCreateFragment).commit();
        }
        if (mReturnListFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction().hide(mReturnListFragment).commit();
        }
    }

    public void transToReturnItemCreateFragment() {
        hideAllFragment();
        mReturnItemCreateFragment.initItem(mUsageInfo.putId, mUsageInfo.tankId);
        if (mReturnItemCreateFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction().show(mReturnItemCreateFragment).setMaxLifecycle(mReturnItemCreateFragment, Lifecycle.State.RESUMED).commit();
        } else {
            getSupportFragmentManager().beginTransaction().add(R.id.container, mReturnItemCreateFragment).commit();
        }
    }

    public void transToReturnListFragment() {
        hideAllFragment();
        if (mReturnListFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction().show(mReturnListFragment).setMaxLifecycle(mReturnListFragment, Lifecycle.State.RESUMED).commit();
        } else {
            getSupportFragmentManager().beginTransaction().add(R.id.container, mReturnListFragment).commit();
        }
    }

    public void onBackButtonClick(View view) {
        if (mReturnListFragment.isHidden()) {
            transToReturnListFragment();
        } else {
            finish();
        }
    }

    public void onMenuBackButtonClick(View view) {
        finish();
    }

    public void onSubmitButtonClick(View view) {
        if (mReturnListFragment.isHidden()) {
            mReturnItemCreateFragment.submitItem();
        } else {
            mReturnListFragment.submit();
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

    public DictType getUnitLabelByValue(String value) {
        return getDictTypeByValue(mUnitTypes, value);
    }

    public DictType getPurityLabelByValue(String value) {
        return getDictTypeByValue(mPurityTypes, value);
    }

    public DictType getMeasureSpecLabelByValue(String value) {
        return getDictTypeByValue(mMeasureSpecs, value);
    }

    public void setDicTypeLabel(UsageItemInfo info) {
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

        if (TextUtils.isEmpty(info.measureSpec)) {
            info.measureSpec = null;
            info.measureSpecLabel = null;
        } else if (!TextUtils.isEmpty(info.measureSpec)) {
            DictType measureSpec = getMeasureSpecLabelByValue(info.measureSpec);
            if (measureSpec != null) {
                info.measureSpecLabel = measureSpec.label;
            }
        }
    }

    static class CreateUsageInfoTask extends AsyncTask<Void, Void, UsageInfo> {

        private final WeakReference<ReturnAfterUseActivity> mActivity;

        public CreateUsageInfoTask(ReturnAfterUseActivity activity) {
            this.mActivity = new WeakReference<>(activity);
        }

        @Override
        protected UsageInfo doInBackground(Void... voids) {
            CabinetInfo cabinetInfo = CabinetApplication.getInstance().getCabinetInfo();
            UsageInfo result = new UsageInfo();
            APIJSON<StorageLaboratoryDetail> storageLaboratoryDetailJSON = RemoteAPI.ReturnAfterUse.getStorageLaboratoryDetail();
            if (storageLaboratoryDetailJSON.code != 200.) {
                return null;
            } else {
                StorageLaboratoryDetail detail = storageLaboratoryDetailJSON.data;
                result.putLabId = detail.labId;
                result.labName = detail.labName;
                result.useAddress = detail.address;
                result.roomNum = detail.roomNum;
                result.building = detail.building;
//                result.devId = cabinetInfo.devId;
                result.tankId = cabinetInfo.tankId;
            }

            APIJSON<String> usageNoJSON = RemoteAPI.ReturnAfterUse.createUsageTask();
            if (usageNoJSON.code != 200) {
                return null;
            }

            UsageInfo usageInfo = null;
            APIJSON<List<UsageInfo>> usageInfoListJson = RemoteAPI.ReturnAfterUse.getLastUsageTaskList();
            if (usageInfoListJson.code != 200 || usageInfoListJson.data == null || usageInfoListJson.data.size() == 0) {
                return null;
            } else {
                for (UsageInfo info : usageInfoListJson.data) {
                    if (TextUtils.equals(info.putLabId, cabinetInfo.labId)) {
                        usageInfo = info;
                        break;
                    }
                }
            }

            if (usageInfo == null) {
                return null;
            }
            result.createTime = usageInfo.createTime;
            result.labName = usageInfo.labName;
            result.putId = usageInfo.putId;
            result.putLabId = usageInfo.putLabId;
            result.putNo = usageInfo.putNo;
            result.tenantId = usageInfo.tenantId;
            result.userName = usageInfo.userName;
            return result;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mActivity.get().showProgressDialog();
        }

        @Override
        protected void onPostExecute(UsageInfo result) {
            super.onPostExecute(result);
            mActivity.get().dismissProgressDialog();
            if (result != null) {
                mActivity.get().mUsageInfo = result;
                CtrlFunc.saveUnSubmitUsageInfo(mActivity.get(), mActivity.get().mUsageInfo);
                mActivity.get().transToReturnListFragment();
                mActivity.get().showUsageInfo();
            } else {
                mActivity.get().showToast("服务器错误!");
                mActivity.get().finish();
            }
        }
    }

    static class GetBaseInfoTask extends AsyncTask<Void, Void, Boolean> {

        private final WeakReference<ReturnAfterUseActivity> mActivity;

        public GetBaseInfoTask(ReturnAfterUseActivity activity) {
            this.mActivity = new WeakReference<>(activity);
        }

        @Override
        protected Boolean doInBackground(Void... v) {
            APIJSON<List<DictType>> unitTypes = RemoteAPI.BaseInfo.getUnitDictCode();
            if (unitTypes.code != 200) {
                return false;
            } else {
                mActivity.get().mUnitTypes = unitTypes.data;
            }
            APIJSON<List<DictType>> purityTypes = RemoteAPI.BaseInfo.getPurityDictCode();
            if (purityTypes.code != 200) {
                return false;
            } else {
                mActivity.get().mPurityTypes = purityTypes.data;
            }

            APIJSON<List<DictType>> measureSpecs = RemoteAPI.BaseInfo.getMeasureSpecDictCode();
            if (measureSpecs.code != 200) {
                return false;
            } else {
                mActivity.get().mMeasureSpecs = measureSpecs.data;
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
                mActivity.get().mUsageInfo = CtrlFunc.getUnSubmitUsageInfo(mActivity.get());
                if (mActivity.get().mUsageInfo == null) {
                    new CreateUsageInfoTask(mActivity.get()).execute();
                } else {
                    mActivity.get().transToReturnListFragment();
                    mActivity.get().showUsageInfo();
                }
            } else {
                mActivity.get().showToast("服务器错误!");
                mActivity.get().finish();
            }
        }
    }
}