package me.liuzs.cabinetmanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import me.liuzs.cabinetmanager.service.HardwareService;
import me.liuzs.cabinetmanager.util.Util;

public class HardwareSetupActiveActivity extends BaseActivity implements CtrlFunc.CheckARCActiveListener {

    public static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final String TAG = "HardwareSetupActiveActivity";
    private final Gson _Gson = new Gson();
    private final ActivityResultLauncher<Intent> mWeightLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            int resultCode = result.getResultCode();
            if (resultCode == RESULT_OK) {
                Intent data = result.getData();
                assert data != null;
                String selectValue = data.getStringExtra(WeightActivity.KEY_SELECT_VALUE);
                assert selectValue != null;
                showToast("Weight:" + selectValue);
            }
        }
    });
    private final ActivityResultLauncher<Intent> mScalesDeviceSelectLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            int resultCode = result.getResultCode();
            if (resultCode == RESULT_OK) {
                try {
                    Intent data = result.getData();
                    assert data != null;
                    String selectValue = data.getStringExtra(SpinnerActivity.KEY_SELECT_VALUE);
                    assert selectValue != null;
                    String index = selectValue.split(" - ")[0];
                    int sIndex = Integer.parseInt(index);
                    if (sIndex != CtrlFunc.getCurrentScalesDevice(HardwareSetupActiveActivity.this)) {
                        CtrlFunc.saveCurrentScalesDevice(HardwareSetupActiveActivity.this, sIndex);
                        stopService(new Intent(HardwareSetupActiveActivity.this, HardwareService.class));
                        startService(new Intent(HardwareSetupActiveActivity.this, HardwareService.class));
                    }
                } catch (Exception e) {

                }
                showScalesDeviceName();
            }
        }
    });

    private final ActivityResultLauncher<Intent> mMainTVOCModelSelectLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            int resultCode = result.getResultCode();
            if (resultCode == RESULT_OK) {
                try {
                    Intent data = result.getData();
                    assert data != null;
                    String selectValue = data.getStringExtra(SpinnerActivity.KEY_SELECT_VALUE);
                    assert selectValue != null;
                    CtrlFunc.saveMainTVOCModelCount(getApplicationContext(), Integer.parseInt(selectValue));
                } catch (Exception e) {

                }
                showMainTVOCModelCount();
            }
        }
    });

    private TextView mARCState, mARCInfo, mScalesSelect, mPrinterName, mTitle, mTVOCSelectCount;
    private Button mARCActive, mWeight, mCalibration, mTVOCSelect;
    private EditText mBarcode, mSubBoardPeriod, mLockDelay, mGetNEVPeriod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hardware_setup);
        Util.fullScreen(this);
        mARCState = findViewById(R.id.tvARCStateValue);
        mARCInfo = findViewById(R.id.tvActiveInfo);
        mScalesSelect = findViewById(R.id.tvWeightSelect);
        mARCActive = findViewById(R.id.btnARCActive);
        mWeight = findViewById(R.id.btnWeight);
        mCalibration = findViewById(R.id.btnCalibration);
        mPrinterName = findViewById(R.id.tvPrinterNameValue);
        mTVOCSelect = findViewById(R.id.btnTvocSelect);
        mTVOCSelectCount = findViewById(R.id.tvTvocsNameValue);

        mBarcode = (EditText) findViewById(R.id.etBarcode);
        mSubBoardPeriod = (EditText) findViewById(R.id.etSubBoardPeriod);
        mLockDelay = (EditText) findViewById(R.id.etSubBoardLockDelayPeriod);
        mGetNEVPeriod = (EditText) findViewById(R.id.etLoadEnvPeriod);
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mBarcode.getWindowToken(), 0);
        CtrlFunc.checkARCActive(this, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (PrintActivity.CurrentPrinterInfo != null) {
            mPrinterName.setText(PrintActivity.CurrentPrinterInfo.name);
        } else {
            mPrinterName.setText("");
        }
        showScalesDeviceName();
        showMainTVOCModelCount();
        showSubBoardPeriod();
    }

    private void showScalesDeviceName() {
        String name = Config.ScalesDeviceName[CtrlFunc.getCurrentScalesDevice(this)];
        mScalesSelect.setText(name);
        if (CtrlFunc.getCurrentScalesDevice(this) == 1) {
            mCalibration.setVisibility(View.VISIBLE);
        } else {
            mCalibration.setVisibility(View.GONE);
        }
    }

    private void showMainTVOCModelCount() {
        int count = CtrlFunc.getMainTVOCModelCount(this);
        mTVOCSelectCount.setText(String.valueOf(count));
    }

    private void showSubBoardPeriod() {
        int[] periods = CtrlFunc.getSubBoardPeriod(this);
        mSubBoardPeriod.setText(String.valueOf(periods[0]));
        mLockDelay.setText(String.valueOf(periods[1]));
        mGetNEVPeriod.setText(String.valueOf(periods[2]));
    }

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public void onFinishButtonClick(View view) {
        finish();
    }

    public void onWeightButtonClick(View view) {
        Intent intent = new Intent(this, WeightActivity.class);
        mWeightLauncher.launch(intent);
    }

    public void onActiveButtonClick(View view) {
        mARCActive.setEnabled(false);
        CtrlFunc.checkARCActive(this, this);
    }

    @Override
    public void onCheckARCActiveFailure(String info, int code) {
        mARCState.setText(R.string.unactivated);
        mARCState.setBackgroundResource(R.drawable.background_state_green);
        mARCInfo.setText(info);
        mARCActive.setEnabled(true);
    }

    @Override
    public void onCheckARCActiveSuccess() {
        mARCState.setText(R.string.activated);
        mARCState.setBackgroundResource(R.drawable.background_state_green);
        mARCActive.setEnabled(true);
    }

    @Override
    protected void onDestroy() {
        HardwareService.weight(null);
        super.onDestroy();
    }

    public void onPrinterButtonClick(View view) {
        PrintActivity.ContainerLabel pc = new PrintActivity.ContainerLabel();
        pc.chemicalCASNO = "60-57-1";
        pc.chemicalName = "(1R,4S,4aS,5R,6R,7S,8S,8aR)-1,2,3,4,10,10-六氯-1,4,4a,5,6,7,8,8a-八氢-6,7-环氧-1,4,5,8-二亚甲基萘[含量2%～90%]";
        pc.controlType = "易制爆危险化学品";
        pc.containerNo = "101120201218001";
        PrintActivity.startPrintContainerLabel(this, pc);
    }

    public void onTVOCSelectButtonClick(View view) {
        List<String> options = new ArrayList<>();
        options.add(String.valueOf(1));
        options.add(String.valueOf(2));

        Intent intent = new Intent(this, SpinnerActivity.class);
        intent.putExtra(SpinnerActivity.KEY_OPTIONS, _Gson.toJson(options));
        intent.putExtra(SpinnerActivity.KEY_TIP_INFO, "请选主TVOC模块数量：");
        mMainTVOCModelSelectLauncher.launch(intent);
    }

    public void onSubBoardSaveButtonClick(View view) {
        try {
            int period1 = Integer.parseInt(mSubBoardPeriod.getText().toString());
            int period2 = Integer.parseInt(mLockDelay.getText().toString());
            int period3 = Integer.parseInt(mGetNEVPeriod.getText().toString());
            CtrlFunc.saveSubBoardPeriod(this, period1, period2, period3);
            stopService(new Intent(HardwareSetupActiveActivity.this, HardwareService.class));
            startService(new Intent(HardwareSetupActiveActivity.this, HardwareService.class));
        } catch (Exception e) {
            showToast("输入有误，请检查");
        }
        showSubBoardPeriod();
    }

    public void onCalibrationButtonClick(View view) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(R.string.calibration);
        alertDialog.setMessage("请放置500g标定砝码后，点击确定按钮");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "确定",
                (dialog, which) -> {
                    HardwareService.steelyardCalibration(5000);
                    dialog.dismiss();
                });
        alertDialog.show();
    }

    public void onScalesSelectClick(View view) {
        List<String> options = new ArrayList<>();
        for (int i = 0; i < Config.ScalesDeviceName.length; i++) {
            options.add(i + " -  " + Config.ScalesDeviceName[i]);
        }
        Intent intent = new Intent(this, SpinnerActivity.class);
        intent.putExtra(SpinnerActivity.KEY_OPTIONS, _Gson.toJson(options));
        intent.putExtra(SpinnerActivity.KEY_TIP_INFO, "请选择秤：");
        mScalesDeviceSelectLauncher.launch(intent);
    }

    public void onTitleClick(View view) {
        Intent intent = new Intent(this, LogViewActivity.class);
        startActivity(intent);
    }
}