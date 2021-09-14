package me.liuzs.cabinetmanager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Lifecycle;

import java.lang.ref.WeakReference;
import java.util.Objects;

import me.liuzs.cabinetmanager.model.CabinetInfo;
import me.liuzs.cabinetmanager.model.DepositItem;
import me.liuzs.cabinetmanager.model.DepositRecord;
import me.liuzs.cabinetmanager.model.UserInfo;
import me.liuzs.cabinetmanager.net.APIJSON;
import me.liuzs.cabinetmanager.net.RemoteAPI;
import me.liuzs.cabinetmanager.ui.storage.DepositItemCreateFragment;
import me.liuzs.cabinetmanager.ui.storage.FirstDepositFragment;
import me.liuzs.cabinetmanager.util.Util;

/**
 * 初次存入模块
 */
public class StorageActivity extends BaseActivity {

    public final static String TAG = "StorageActivity";
    private final FirstDepositFragment mFirstDepositFragment = new FirstDepositFragment(this);
    private final DepositItemCreateFragment mDepositItemCreateFragment = new DepositItemCreateFragment(this);
    private DepositRecord mDepositRecord;
    private TextView mLaboratoryName, mDepositUser, mDepositRecorderId, mDepositItemCount;
    private ImageButton mToolBack;

    public DepositRecord getDepositRecord() {
        return mDepositRecord;
    }

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(null);
        Util.fullScreen(this);

        mLaboratoryName = findViewById(R.id.tvDepositUnitValue);
        mDepositUser = findViewById(R.id.tvDepositUserValue);
        mDepositRecorderId = findViewById(R.id.tvDepositNoValue);
        mDepositItemCount = findViewById(R.id.tvDepositCountValue);
        mToolBack = findViewById(R.id.toolbar_back);

        mDepositRecord = CtrlFunc.getUnSubmitDepositRecord(this);
        if (mDepositRecord == null) {
            CabinetInfo cabinetInfo = CabinetApplication.getInstance().getCabinetInfo();
            UserInfo userInfo = CabinetApplication.getInstance().getAdminUser();
            mDepositRecord = new DepositRecord();
            mDepositRecord.labName = cabinetInfo.labName;
            mDepositRecord.labId = cabinetInfo.labId;
            mDepositRecord.userId = userInfo.userId;
            mDepositRecord.userName = userInfo.userName;
            ((TextView) findViewById(R.id.toolbar_title)).setText(R.string.first_save_in);
            new CreateDepositTask(this).execute();
        } else {
            if (savedInstanceState == null) {
                transToFirstDepositFragment();
            }
        }
    }

    public void showDepositRecord() {
        mLaboratoryName.setText(mDepositRecord.labName);
        mDepositUser.setText(mDepositRecord.userName);
        mDepositRecorderId.setText(mDepositRecord.putNo);
        mDepositItemCount.setText(String.valueOf(mDepositRecord.items.size()));

        if (!mFirstDepositFragment.isHidden()) {
            mToolBack.setVisibility(View.VISIBLE);
        } else if (!mDepositItemCreateFragment.isHidden()) {
            mToolBack.setVisibility(View.INVISIBLE);
        }
    }

    private void hideAllFragment() {
        if (mDepositItemCreateFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction().hide(mDepositItemCreateFragment).commit();
        }
        if (mFirstDepositFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction().hide(mFirstDepositFragment).commit();
        }
    }

    public void transToDepositItemCreateFragment(DepositItem item) {
        hideAllFragment();
        mDepositItemCreateFragment.setOriginItem(item);
        if (mDepositItemCreateFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction().show(mDepositItemCreateFragment).setMaxLifecycle(mDepositItemCreateFragment, Lifecycle.State.RESUMED).commit();
        } else {
            getSupportFragmentManager().beginTransaction().add(R.id.container, mDepositItemCreateFragment).commit();
        }
    }

    public void transToFirstDepositFragment() {
        hideAllFragment();
        if (mFirstDepositFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction().show(mFirstDepositFragment).setMaxLifecycle(mFirstDepositFragment, Lifecycle.State.RESUMED).commit();
        } else {
            getSupportFragmentManager().beginTransaction().add(R.id.container, mFirstDepositFragment).commit();
        }
    }

    public void onBackButtonClick(View view) {
        if (mFirstDepositFragment.isHidden()) {
            transToFirstDepositFragment();
        } else {
            finish();
        }
    }

    public void onSubmitButtonClick(View view) {
        if (mFirstDepositFragment.isHidden()) {
            mDepositItemCreateFragment.submitItem();
        } else {
            mFirstDepositFragment.submitDeposit();
        }
    }

    static class CreateDepositTask extends AsyncTask<String, Void, String[]> {

        private final WeakReference<StorageActivity> mActivity;

        public CreateDepositTask(StorageActivity activity) {
            this.mActivity = new WeakReference<>(activity);
        }

        @Override
        protected String[] doInBackground(String... strings) {
            String[] result = new String[2];
            APIJSON<String> putNoJson = RemoteAPI.Storage.getDepositNo();
            if (putNoJson.code != 200.) {
                return null;
            } else {
                result[0] = putNoJson.data;
            }
            APIJSON<String> putIdJson = RemoteAPI.Storage.saveDeposit(result[0], "1", "1");
            if (putIdJson.code != 200.) {
                return null;
            } else {
                result[1] = putIdJson.data;
                try {
                    Integer.parseInt(result[1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return result;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mActivity.get().showProgressDialog();
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);
            mActivity.get().dismissProgressDialog();
            if (result != null) {
                mActivity.get().mDepositRecord.putNo = result[0];
                mActivity.get().mDepositRecord.putId = Integer.parseInt(result[1]);
                CtrlFunc.saveUnSubmitDepositRecord(mActivity.get(), mActivity.get().mDepositRecord);
                mActivity.get().transToFirstDepositFragment();
                mActivity.get().showDepositRecord();
            } else {
                mActivity.get().showToast("服务器错误!");
                mActivity.get().finish();
            }
        }
    }

}