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

public class HardwareSetupActiveActivity extends BaseActivity implements CabinetCore.CheckARCActiveListener {

    public static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final String TAG = "HardwareSetupActiveActivity";
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

    private TextView mARCState, mARCInfo, mPrinterName, mTitle, mScalesName;
    private Button mARCActive, mWeight, mCalibration, mModbusTest;
    private EditText mBarcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hardware_setup);
        Util.fullScreen(this);
        mARCState = findViewById(R.id.tvARCStateValue);
        mARCInfo = findViewById(R.id.tvActiveInfo);
        mARCActive = findViewById(R.id.btnARCActive);
        mWeight = findViewById(R.id.btnWeight);
        mCalibration = findViewById(R.id.btnCalibration);
        mPrinterName = findViewById(R.id.tvPrinterNameValue);
        mScalesName = findViewById(R.id.tvScalesName);
        mModbusTest = findViewById(R.id.btnModbusTest);

        mBarcode = findViewById(R.id.etBarcode);
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mBarcode.getWindowToken(), 0);
        CabinetCore.validateARCActive( this);
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
    }

    private void showScalesDeviceName() {
        String name = Config.ScalesDeviceName[CabinetCore.getCurrentScalesDevice()];
        mScalesName.setText(name);
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
        CabinetCore.validateARCActive( this);
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

    public void onModbusTestButtonClick(View view) {
        List<String> options = new ArrayList<>();
        options.add(String.valueOf(1));
        options.add(String.valueOf(2));

        Intent intent = new Intent(this, SpinnerActivity.class);
        intent.putExtra(SpinnerActivity.KEY_OPTIONS, CabinetCore.GSON.toJson(options));
        intent.putExtra(SpinnerActivity.KEY_TIP_INFO, "请选主TVOC模块数量：");

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

    public void onTitleClick(View view) {
        Intent intent = new Intent(this, LogViewActivity.class);
        startActivity(intent);
    }
}