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

import com.videogo.util.IPAddressUtil;

import java.util.LinkedList;
import java.util.List;

import me.liuzs.cabinetmanager.service.HardwareService;
import me.liuzs.cabinetmanager.service.ModbusService;

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

    private TextView mARCState, mARCInfo, mPrinterName, mScalesName;
    private Button mARCActive, mWeight, mCalibration;
    private EditText mBarcode,mModbusAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hardware_setup);
        mARCState = findViewById(R.id.tvARCStateValue);
        mARCInfo = findViewById(R.id.tvActiveInfo);
        mARCActive = findViewById(R.id.btnARCActive);
        mWeight = findViewById(R.id.btnWeight);
        mCalibration = findViewById(R.id.btnCalibration);
        mPrinterName = findViewById(R.id.tvPrinterNameValue);
        mScalesName = findViewById(R.id.tvScalesName);
        mModbusAddress = findViewById(R.id.etModbusAddress);

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
        showModbusAddress();
    }

    private void showScalesDeviceName() {
        String name = Config.ScalesDeviceName[CabinetCore.getCurrentScalesDevice()];
        mScalesName.setText(name);
    }

    private void showModbusAddress() {
        String address = CabinetCore.getModbusAddress();
        mModbusAddress.setText(address);
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

    public void onModbusAddressSaveButtonClick(View view) {
        String address = mModbusAddress.getEditableText().toString();
        if(IPAddressUtil.isIPv4LiteralAddress(address)){
            CabinetCore.saveModbusAddress(address);
            ModbusService.setModbusAddress(address);
            showToast("保存成功！");
            hideInputMethod();
            showModbusAddress();
        } else {
            showToast("地址不正确！");
        }
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
        PrintActivity.ContainerLabel cl = new PrintActivity.ContainerLabel();
        cl.batch_name = "PC00100";
        cl.agency_name = "清华大学";
        cl.operator = "刘道衡";
        cl.no = "CN101120201218001";
        List<PrintActivity.ContainerLabel> cls = new LinkedList<>();
        PrintActivity.startPrintContainerLabel(this, cls);
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