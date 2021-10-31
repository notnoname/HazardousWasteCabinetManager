package me.liuzs.cabinetmanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

import me.liuzs.cabinetmanager.model.Cabinet;
import me.liuzs.cabinetmanager.model.User;
import me.liuzs.cabinetmanager.net.APIJSON;
import me.liuzs.cabinetmanager.net.CabinetListJSON;
import me.liuzs.cabinetmanager.net.RemoteAPI;

public class CabinetBindActivity extends BaseActivity {

    public static final String TAG = "CabinetBindActivity";
    private static final String KEY_ORG_CABINET_INFO = "KEY_ORG_CABINET_INFO";
    private boolean isInit = false;
    private Cabinet mCabinetInfo;
    private List<Cabinet> mCabinetInfoList;
    private TextView mUserName, mUserMobile, mTankSelect, mTankBaseInfo, mTankOrgInfo;
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
                mCabinetInfo = getCabinetInfoByName(selectValue);
                showInfo();
            }
        }
    });

    public static void start(Context context, Cabinet cabinet) {
        Intent intent = new Intent(context, CabinetBindActivity.class);
        if (cabinet != null) {
            intent.putExtra(CabinetBindActivity.KEY_ORG_CABINET_INFO, CabinetCore.GSON.toJson(CabinetCore.getCabinetInfo()));
        }
        if (context instanceof CabinetApplication) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cabinet_name);

        mUserName = findViewById(R.id.tvSafetyPersonValue);
        mUserMobile = findViewById(R.id.tvMobileValue);
        mTankSelect = findViewById(R.id.tvSelect);
        mTankBaseInfo = findViewById(R.id.tvTankNameValue);
        mTankOrgInfo = findViewById(R.id.tvOrgInfoValue);
        String extInfo = getIntent().getStringExtra(KEY_ORG_CABINET_INFO);

        if (extInfo != null) {
            mCabinetInfo = CabinetCore.GSON.fromJson(extInfo, Cabinet.class);
            isInit = false;
        } else {
            isInit = true;
        }

        showInfo();
        User info = CabinetCore.getCabinetUser(CabinetCore.RoleType.Admin);
        assert info != null;
        getCabinetList(info.id);
    }

    @SuppressLint("SetTextI18n")
    private void showInfo() {
        User user = CabinetCore.getCabinetUser(CabinetCore.RoleType.Admin);
        assert user != null;
        mUserName.setText(user.name);
        mUserMobile.setText(user.phone);
        if (mCabinetInfo == null) {
            mTankSelect.setText("");
            mTankBaseInfo.setText("");
            mTankOrgInfo.setText("");
        } else {
            mTankSelect.setText(mCabinetInfo.name);
            mTankBaseInfo.setText(mCabinetInfo.name + "/" + mCabinetInfo.code + "/" + mCabinetInfo.type);
            mTankOrgInfo.setText(mCabinetInfo.org.full_name + "/" + mCabinetInfo.org.code);
        }
    }

    public void onCabinetSelectButtonClick(View view) {
        if (mCabinetInfoList == null) {
            showToast("该用户无可管理的一体柜！");
            return;
        }
        Intent intent = new Intent(this, SpinnerActivity.class);
        List<String> options = new ArrayList<>();
        for (Cabinet info : mCabinetInfoList) {
            options.add(info.name);
        }
        intent.putExtra(SpinnerActivity.KEY_OPTIONS, CabinetCore.GSON.toJson(options));
        intent.putExtra(SpinnerActivity.KEY_TIP_INFO, "请选择需要绑定的暂存柜：");
        mTankSelectLauncher.launch(intent);
    }

    public void onSaveButtonClick(View view) {
        if (mCabinetInfo == null) {
            showToast("请选择需要绑定的暂存柜！");
            return;
        }
        CabinetCore.saveCabinetInfo(mCabinetInfo);
        CabinetCore.restart();
    }

    private Cabinet getCabinetInfoByName(String name) {
        if (mCabinetInfoList != null) {
            for (Cabinet info : mCabinetInfoList) {
                if (name.equals(info.name)) {
                    return info;
                }
            }
        }
        return null;
    }

    public void onBackButtonClick(View view) {
        if (isInit) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle(R.string.tip);
            alertDialog.setMessage("是否返回管理员登录页面？");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "是",
                    (dialog, which) -> {
                        CabinetCore.clearCabinetUser(CabinetCore.RoleType.Admin);
                        dialog.dismiss();
                        CabinetCore.restart();
                    });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "否", (dialog, which) -> dialog.dismiss());
            alertDialog.show();
        } else {
            finish();
        }
    }

    private void getCabinetList(String userId) {
        showProgressDialog();
        getExecutorService().submit(() -> {
            APIJSON<CabinetListJSON> listJSON = RemoteAPI.System.getCabinetList(userId);
            if (listJSON.status == APIJSON.Status.ok) {
                mCabinetInfoList = listJSON.data.storages;
            } else {
                showToast(listJSON.errors);
            }
            dismissProgressDialog();
        });
    }
}