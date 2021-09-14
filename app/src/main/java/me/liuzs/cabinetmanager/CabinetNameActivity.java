package me.liuzs.cabinetmanager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.Toolbar;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import me.liuzs.cabinetmanager.model.CabinetInfo;
import me.liuzs.cabinetmanager.model.DeviceInfo;
import me.liuzs.cabinetmanager.model.UserInfo;
import me.liuzs.cabinetmanager.net.APIJSON;
import me.liuzs.cabinetmanager.net.RemoteAPI;
import me.liuzs.cabinetmanager.service.HardwareService;
import me.liuzs.cabinetmanager.util.Util;

public class CabinetNameActivity extends BaseActivity {

    public static final String TAG = "CabinetNameActivity";
    public static final String KEY_INIT = "KEY_INIT";
    private final Gson mGson = new Gson();
    private CabinetInfo mCabinetInfo;
    private List<CabinetInfo> mCabinetInfoList;
    private TextView mUserName, mUserMobile, mTankSelect, mTankBaseInfo, mTankOrgInfo, mDeviceSelect;
    private final ActivityResultLauncher<Intent> mTankSelectLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            int resultCode = result.getResultCode();
            if (resultCode == RESULT_OK) {
                Intent data = result.getData();
                assert data != null;
                String selectValue = data.getStringExtra(SpinnerActivity.KEY_SELECT_VALUE);
                assert selectValue != null;
                Log.d(TAG, selectValue);
                CabinetInfo info = getCabinetInfoByName(selectValue);
                if (info != null && TextUtils.isEmpty(info.labId)) {
                    showToast("暂不支持库房设备！");
                } else {
                    mCabinetInfo = info;
                }
                showInfo();
            }
        }
    });

    public static String getTankTypeValue(int tankType) {
        if (tankType == 1) {
            return "主控组合式";
        }
        if (tankType == 2) {
            return "单体式";
        }
        if (tankType == 3) {
            return "非联网式";
        }
        return "未知设备类型";
    }

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cabinet_name);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(null);
        Util.fullScreen(this);
        ((TextView) findViewById(R.id.toolbar_title)).setText(R.string.title_activity_cabinet_name);

        mUserName = findViewById(R.id.tvSafetyPersonValue);
        mUserMobile = findViewById(R.id.tvMobileValue);
        mTankSelect = findViewById(R.id.tvSelect);
        mTankBaseInfo = findViewById(R.id.tvTankNameValue);
        mTankOrgInfo = findViewById(R.id.tvOrgInfoValue);
        mDeviceSelect = findViewById(R.id.tvDeviceSelect);
        String extInfo = getIntent().getStringExtra(KEY_INIT);

        if (extInfo != null) {
            mCabinetInfo = mGson.fromJson(extInfo, CabinetInfo.class);
        }

        showInfo();
        UserInfo info = CabinetApplication.getInstance().getAdminUser();
        new GetTankListTask().execute(info.userName, info.phone);
    }

    @SuppressLint("SetTextI18n")
    private void showInfo() {
        UserInfo user = CabinetApplication.getInstance().getAdminUser();
        mUserName.setText(user.userName);
        mUserMobile.setText(user.phone);
        if (mCabinetInfo == null) {
            mTankSelect.setText("");
            mTankBaseInfo.setText("");
            mTankOrgInfo.setText("");
        } else {
            mTankSelect.setText(mCabinetInfo.tankName);
            mTankBaseInfo.setText(mCabinetInfo.tankName + "/" + mCabinetInfo.tankNo + "/" + getTankTypeValue(mCabinetInfo.tankType));
            mTankOrgInfo.setText(mCabinetInfo.orgName + "/" + mCabinetInfo.labName);
        }

        if (mCabinetInfo == null || mCabinetInfo.devices.size() == 0) {
            mDeviceSelect.setText("");
        } else {
            StringBuilder sb = new StringBuilder();
            for (DeviceInfo info : mCabinetInfo.devices) {
                sb.append(info.devName).append("(ID:").append(info.devId).append("/Address:").append(String.valueOf(info.hardwareAddress)).append(")").append("\n\r");
            }
            mDeviceSelect.setText(sb.toString());
        }
    }

    public void onCabinetSelectButtonClick(View view) {
        if (mCabinetInfoList == null) {
            showToast("没有找到任何一体柜信息！");
            return;
        }
        Intent intent = new Intent(this, SpinnerActivity.class);
        List<String> options = new ArrayList<>();
        for (CabinetInfo info : mCabinetInfoList) {
            options.add(info.tankName);
        }
        intent.putExtra(SpinnerActivity.KEY_OPTIONS, mGson.toJson(options));
        intent.putExtra(SpinnerActivity.KEY_TIP_INFO, "请选择一体柜：");
        mTankSelectLauncher.launch(intent);
    }

    public void onSaveButtonClick(View view) {
        if (mCabinetInfo == null) {
            showToast("请选择一体柜！");
            return;
        }
        CtrlFunc.saveCabinetInfo(this, mCabinetInfo);
        Intent intent = new Intent(this, LauncherActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private CabinetInfo getCabinetInfoByName(String name) {
        if (mCabinetInfoList != null) {
            for (CabinetInfo info : mCabinetInfoList) {
                if (name.equals(info.tankName)) {
                    return info;
                }
            }
        }
        return null;
    }

    public void onBackButtonClick(View view) {
        finish();
    }

    @SuppressLint("StaticFieldLeak")
    private class GetTankListTask extends AsyncTask<String, Void, APIJSON<List<CabinetInfo>>> {
        @Override
        protected APIJSON<List<CabinetInfo>> doInBackground(String... strings) {
            APIJSON<List<CabinetInfo>> result = RemoteAPI.System.getTankList(strings[0], strings[1]);
            if (result.code == 200) {
                for (CabinetInfo info : result.data) {
                    APIJSON<List<DeviceInfo>> deviceInfoListJSON = RemoteAPI.System.getDeviceList(info.tankId);
                    if (deviceInfoListJSON.code == 200) {
                        List<DeviceInfo> devices = deviceInfoListJSON.data;
                        devices.sort((o1, o2) -> {
                            int id1 = 0;
                            int id2 = 0;
                            try {
                                id1 = Integer.parseInt(o1.devId);
                                id2 = Integer.parseInt(o2.devId);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return id1 - id2;
                        });
                        for (int i = 0; i < devices.size(); i++) {
                            DeviceInfo deviceInfo = devices.get(i);
                            deviceInfo.hardwareAddress = i + 1;
                        }
                        info.devices.addAll(devices);
                    }
                }
            }
            return result;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected void onPostExecute(APIJSON<List<CabinetInfo>> json) {
            super.onPostExecute(json);
            dismissProgressDialog();
            if (json.code == 200) {
                mCabinetInfoList = json.data;
            } else {
                showToast("服务器错误");
            }
        }
    }
}