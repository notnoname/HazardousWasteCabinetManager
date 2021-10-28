package me.liuzs.cabinetmanager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.lifecycle.Lifecycle;

import java.lang.ref.WeakReference;

import me.liuzs.cabinetmanager.model.Cabinet;
import me.liuzs.cabinetmanager.model.DepositItem;
import me.liuzs.cabinetmanager.model.DepositRecord;
import me.liuzs.cabinetmanager.model.User;
import me.liuzs.cabinetmanager.net.APIJSON;
import me.liuzs.cabinetmanager.net.RemoteAPI;

/**
 * 初次存入模块
 */
public class DepositActivity extends BaseActivity {

    public final static String TAG = "DepositActivity";
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
        setContentView(R.layout.activity_deposit);
        mLaboratoryName = findViewById(R.id.tvDepositUnitValue);
        mDepositUser = findViewById(R.id.tvDepositUserValue);
        mDepositRecorderId = findViewById(R.id.tvDepositNoValue);
        mDepositItemCount = findViewById(R.id.tvDepositCountValue);
        mToolBack = findViewById(R.id.toolbar_back);

        mDepositRecord = CabinetCore.getUnSubmitDepositRecord(this);
        if (mDepositRecord == null) {
            Cabinet cabinet = CabinetCore.getCabinetInfo();
            User user = CabinetCore.getCabinetUser(CabinetCore.RoleType.Operator);
            mDepositRecord = new DepositRecord();
//            mDepositRecord.labName = cabinet.labName;
//            mDepositRecord.labId = cabinet.labId;
            mDepositRecord.userId = user.id;
            mDepositRecord.userName = user.name;
            ((TextView) findViewById(R.id.toolbar_title)).setText(R.string.no_manage);
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

        private final WeakReference<DepositActivity> mActivity;

        public CreateDepositTask(DepositActivity activity) {
            this.mActivity = new WeakReference<>(activity);
        }

        @Override
        protected String[] doInBackground(String... strings) {
            String[] result = new String[2];
            APIJSON<String> putNoJson = RemoteAPI.Storage.getDepositNo();
            if (putNoJson.status != APIJSON.Status.ok) {
                return null;
            } else {
                result[0] = putNoJson.data;
            }
            APIJSON<String> putIdJson = RemoteAPI.Storage.saveDeposit(result[0], "1", "1");
            if (putIdJson.status != APIJSON.Status.ok) {
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
                CabinetCore.saveUnSubmitDepositRecord(mActivity.get(), mActivity.get().mDepositRecord);
                mActivity.get().transToFirstDepositFragment();
                mActivity.get().showDepositRecord();
            } else {
                mActivity.get().showToast("服务器错误!");
                mActivity.get().finish();
            }
        }
    }

}