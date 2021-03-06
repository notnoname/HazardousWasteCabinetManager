package me.liuzs.cabinetmanager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
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

import me.liuzs.cabinetmanager.model.ContainerNoInfo;
import me.liuzs.cabinetmanager.service.HardwareService;
import me.liuzs.cabinetmanager.service.ModbusService;

public class HardwareSetupActiveActivity extends BaseActivity implements CabinetCore.CheckARCActiveListener {

    private static final String TAG = "HardwareSetupActiveActivity";
    private final ActivityResultLauncher<Intent> mWeightLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        int resultCode = result.getResultCode();
        if (resultCode == RESULT_OK) {
            Intent data = result.getData();
            assert data != null;
            String selectValue = data.getStringExtra(WeightActivity.KEY_SELECT_VALUE);
            assert selectValue != null;
            showToast("重量:" + selectValue + "kg");
        }
    });

    private TextView mARCState, mARCInfo, mPrinterName, mScalesName;
    private Button mARCActive, mWeight, mCalibration;
    private EditText mBarcode, mModbusAddress, mDoorMacAddress;

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
        mDoorMacAddress = findViewById(R.id.etDoorMacAddress);

        mBarcode = findViewById(R.id.etBarcode);
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mBarcode.getWindowToken(), 0);
        CabinetCore.validateARCActive(this);
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
        showDoorAccessMacAddress();
    }

    private void showScalesDeviceName() {
        String name = Config.ScalesDeviceName[CabinetCore.getCurrentScalesDevice()];
        mScalesName.setText(name);
    }

    private void showModbusAddress() {
        String address = CabinetCore.getModbusAddress();
        mModbusAddress.setText(address);
    }

    private void showDoorAccessMacAddress() {
        String address = CabinetCore.getDoorAccessMacAddress();
        mDoorMacAddress.setText(address);
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
        CabinetCore.validateARCActive(this);
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
        if (IPAddressUtil.isIPv4LiteralAddress(address)) {
            CabinetCore.saveModbusAddress(address);
            ModbusService.setModbusAddress(address);
            showToast("保存成功！");
            hideInputMethod();
            showModbusAddress();
        } else {
            showToast("地址不正确！");
        }
    }

    public void onModbusTestButtonClick(View view) {
        ModbusDebugActivity.start(this);
    }

    public void onDoorAccessMacAddressSaveButtonClick(View view) {
        String address = mDoorMacAddress.getEditableText().toString();
        CabinetCore.saveDoorAccessMacAddress(address);
        showToast("保存成功！");
        showDoorAccessMacAddress();
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
        ContainerNoInfo cl = new ContainerNoInfo();
        cl.batch_name = "NB20211101015";
        cl.org = "埃德伯格实验室";
        cl.creator = "刘道衡";
        cl.no = "NO202110312056450021";
        List<ContainerNoInfo> cls = new LinkedList<>();
        cls.add(cl);
        PrintActivity.startPrintContainerLabel(this, cls);
    }

    public void onCalibrationButtonClick(View view) {
        HardwareService.steelyardInit();
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

    public void onOpenDoorButtonClick(View view) {
        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                HardwareService.HardwareServiceBinder binder = (HardwareService.HardwareServiceBinder) service;
                if(binder.getHardwareService().unlockRemoteDoor()) {
                    showToast("门禁打开指令发送成功");
                } else {
                    showToast("门禁打开指令发送失败");
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        };
        bindService(new Intent(this, HardwareService.class),
                serviceConnection, Context.BIND_AUTO_CREATE);

    }

    public void onTitleClick(View view) {
        Intent intent = new Intent(this, LogViewActivity.class);
        startActivity(intent);
    }
}