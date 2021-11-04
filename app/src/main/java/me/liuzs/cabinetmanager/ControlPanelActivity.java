package me.liuzs.cabinetmanager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.kyleduo.switchbutton.SwitchButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.liuzs.cabinetmanager.model.modbus.AirConditionerStatus;
import me.liuzs.cabinetmanager.model.modbus.FrequencyConverterStatus;
import me.liuzs.cabinetmanager.model.modbus.StatusOption;
import me.liuzs.cabinetmanager.service.ModbusService;
import me.liuzs.cabinetmanager.util.Util;

/**
 * 控制面板
 */
public class ControlPanelActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener, View.OnTouchListener {

    public static final String TAG = "ControlPanelActivity";

    public static final int[] ACWorkModelNameResId = {R.string.ac_work_model_auto, R.string.ac_work_model_refrigeration, R.string.ac_work_model_heating, R.string.ac_work_model_dehumidification, R.string.ac_work_model_fan};
    public static final int[] ACRemoteWorkModelNameResId = {R.string.ac_remote_ctrl_model_infrared_receive, R.string.ac_remote_ctrl_model_infrared_launch, R.string.ac_remote_ctrl_model_universal_pair, R.string.ac_remote_ctrl_model_universal};
    private TextView mFanWorkModelValue, mSoundLightAlertValue, mACCtrlModelValue, mACWorkModelValue, mACPowerValue, mRemoteCtrlModelValue;
    private Button mFanOpen, mFanClose, mOxygenOpen, mOxygenClose;
    private SwitchButton mFanWorkModel, mACCtrlModel, mACPower;
    private AirConditionerStatus mAirConditionerStatus;
    private StatusOption mStatusOption;
    private final ActivityResultLauncher<Intent> mACWorkModelSelectLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        int resultCode = result.getResultCode();
        if (resultCode == RESULT_OK) {
            try {
                Intent data = result.getData();
                assert data != null;
                String selectIndex = data.getStringExtra(SpinnerActivity.KEY_SELECT_INDEX);
                Integer index = Integer.parseInt(selectIndex);
                setACWorkModelOption(AirConditionerStatus.WorkModel.values()[index]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });
    private final ActivityResultLauncher<Intent> mACRemoteWorkModelSelectLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        int resultCode = result.getResultCode();
        if (resultCode == RESULT_OK) {
            try {
                Intent data = result.getData();
                assert data != null;
                String selectIndex = data.getStringExtra(SpinnerActivity.KEY_SELECT_INDEX);
                Integer index = Integer.parseInt(selectIndex);
                setACRemoteWorkModelOption(AirConditionerStatus.RemoteWorkModel.values()[index]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        compoundButton.setChecked(b);
        if (compoundButton == mFanWorkModel) {
            setHardware(StatusOption.UnionWorkModelAddress, 0, b ? StatusOption.FanWorkModel.Auto.ordinal() : StatusOption.FanWorkModel.Manual.ordinal());
        } else if (compoundButton == mACCtrlModel) {
            setHardware(AirConditionerStatus.ACCtrlModelAddress, AirConditionerStatus.ACSetCommitAddress, b ? 1 : 0);
        } else if (compoundButton == mACPower) {
            setHardware(AirConditionerStatus.ACPowerSetAddress, AirConditionerStatus.ACSetCommitAddress, b ? 1 : 0);
        }
    }

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_panel);
        mFanWorkModelValue = findViewById(R.id.tvFanWorkModelValue);
        mSoundLightAlertValue = findViewById(R.id.tvSoundLightAlertValue);
        mACPowerValue = findViewById(R.id.tvACPowerValue);
        mACCtrlModelValue = findViewById(R.id.tvACCtrlModelValue);
        mACWorkModelValue = findViewById(R.id.tvACWorkModelValue);
        mRemoteCtrlModelValue = findViewById(R.id.tvACRemoteCtrlValue);


        mFanOpen = findViewById(R.id.btnFanOpen);
        mFanClose = findViewById(R.id.btnFanClose);
        mOxygenOpen = findViewById(R.id.btnOxygenOpen);
        mOxygenClose = findViewById(R.id.btnOxygenClose);
        mFanOpen.setOnTouchListener(this);
        mFanClose.setOnTouchListener(this);
        mOxygenOpen.setOnTouchListener(this);
        mOxygenClose.setOnTouchListener(this);

        mFanWorkModel = findViewById(R.id.swFanModel);
        mACCtrlModel = findViewById(R.id.swACCtrlModel);
        mACPower = findViewById(R.id.swACPower);
        mFanWorkModel.setOnCheckedChangeListener(this);
        mACPower.setOnCheckedChangeListener(this);
        mACCtrlModel.setOnCheckedChangeListener(this);

    }

    private void setACWorkModelOption(AirConditionerStatus.WorkModel workModel) {
        getExecutorService().submit(() -> {
            boolean success = ModbusService.setHardwareHoldingRegisterOption(AirConditionerStatus.ACWorkModelAddress, workModel.ordinal()) && ModbusService.setHardwareCoilStatusOption(AirConditionerStatus.ACSetCommitAddress, true);
            if (success) {
                showToast("空调模式设置成功!");
            } else {
                showToast("空调模式设置失败!");
            }
            mStatusOption = ModbusService.readStatusOption();
            mAirConditionerStatus = ModbusService.readAirConditionerStatus();
            mHandler.post(this::showStatusOption);
        });
    }

    private void setACRemoteWorkModelOption(AirConditionerStatus.RemoteWorkModel workModel) {
        getExecutorService().submit(() -> {
            boolean success = ModbusService.setHardwareHoldingRegisterOption(AirConditionerStatus.ACWorkModelAddress, workModel.ordinal()) && ModbusService.setHardwareCoilStatusOption(AirConditionerStatus.ACRemoteWorkModelSetCommitAddress, true);
            if (success) {
                showToast("遥控器工作模式设置成功!");
            } else {
                showToast("遥控器工作模式设置失败!");
            }
            mStatusOption = ModbusService.readStatusOption();
            mAirConditionerStatus = ModbusService.readAirConditionerStatus();
            mHandler.post(this::showStatusOption);
        });
    }

    private void getHardwareStatusInfo() {
        getExecutorService().submit(() -> {
            mStatusOption = ModbusService.readStatusOption();
            mAirConditionerStatus = ModbusService.readAirConditionerStatus();
            if (mAirConditionerStatus.e != null || mStatusOption.e != null) {
                mHandler.post(() -> new AlertDialog.Builder(ControlPanelActivity.this).setMessage("设备信息读取失败，请立即检查设备！").setNegativeButton("确认", (dialog, which) ->
                        finish()).show());
            } else {
                mHandler.post(this::showStatusOption);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getHardwareStatusInfo();
    }

    private void showStatusOption() {
        switch (mStatusOption.fanWorkModel) {
            case None:
                mFanWorkModelValue.setText(R.string.none);
                mFanWorkModelValue.setBackgroundResource(R.drawable.background_state_red);
                mFanWorkModel.setChecked(false);
                break;
            case Auto:
                mFanWorkModelValue.setText(R.string.auto);
                mFanWorkModelValue.setBackgroundResource(R.drawable.background_state_green);
                mFanWorkModel.setChecked(true);
                break;
            case Manual:
                mFanWorkModelValue.setText(R.string.manual);
                mFanWorkModelValue.setBackgroundResource(R.drawable.background_state_green);
                mFanWorkModel.setChecked(true);
                break;
        }
        if (mStatusOption.fireAlert) {
            mSoundLightAlertValue.setText(R.string.open);
            mSoundLightAlertValue.setBackgroundResource(R.drawable.background_state_red);
        } else {
            mSoundLightAlertValue.setText(R.string.close);
            mSoundLightAlertValue.setBackgroundResource(R.drawable.background_state_green);
        }
        if (mAirConditionerStatus.autoCtrl) {
            mACCtrlModelValue.setText(R.string.auto);
            mACCtrlModelValue.setBackgroundResource(R.drawable.background_state_green);
            mACCtrlModel.setChecked(true);
        } else {
            mACCtrlModelValue.setText(R.string.manual);
            mACCtrlModelValue.setBackgroundResource(R.drawable.background_state_green);
            mACCtrlModel.setChecked(false);
        }
        if (mAirConditionerStatus.powerOn) {
            mACPowerValue.setText(R.string.open);
            mACPowerValue.setBackgroundResource(R.drawable.background_state_green);
            mACPower.setChecked(true);
        } else {
            mACPowerValue.setText(R.string.close);
            mACPowerValue.setBackgroundResource(R.drawable.background_state_red);
            mACPower.setChecked(false);
        }
        mACWorkModelValue.setText(getString(ACWorkModelNameResId[mAirConditionerStatus.workModel.ordinal()]));
        mRemoteCtrlModelValue.setText(getString(ACRemoteWorkModelNameResId[mAirConditionerStatus.remoteWorkModel.ordinal()]));
    }

    public void onBackButtonClick(View view) {
        finish();
    }

    public void onFCResetButtonClick(View view) {
        setHardware(FrequencyConverterStatus.FCResetAddress, 0, 1);
        Util.sleep(250);
    }

    public void onOptLogButtonClick(View view) {
        LogViewActivity.start(this, LogViewActivity.Type.Alert);
    }

    public void onACWorkModelButtonClick(View view) {
        Intent intent = new Intent(this, SpinnerActivity.class);
        List<String> options = new ArrayList<>();
        for (int resId : ACWorkModelNameResId) {
            options.add(getString(resId));
        }

        intent.putExtra(SpinnerActivity.KEY_OPTIONS, CabinetCore.GSON.toJson(options));
        intent.putExtra(SpinnerActivity.KEY_TIP_INFO, "请选择空调工作模式：");
        mACWorkModelSelectLauncher.launch(intent);
    }

    public void onRemoteCtrlWorkModelButtonClick(View view) {
        Intent intent = new Intent(this, SpinnerActivity.class);
        List<String> options = new ArrayList<>();
        for (int resId : ACRemoteWorkModelNameResId) {
            options.add(getString(resId));
        }

        intent.putExtra(SpinnerActivity.KEY_OPTIONS, CabinetCore.GSON.toJson(options));
        intent.putExtra(SpinnerActivity.KEY_TIP_INFO, "请选择空调遥控器工作模式：");
        mACRemoteWorkModelSelectLauncher.launch(intent);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (mStatusOption.fanWorkModel != StatusOption.FanWorkModel.Auto) {
            showToast("自动模式下才能操作！");
        } else {
            int address = 0;
            if (view == mFanOpen) {
                address = StatusOption.FanStartAddress;
            } else if (view == mFanClose) {
                address = StatusOption.FanStopAddress;
            } else if (view == mOxygenOpen) {
                address = StatusOption.OxygenStartAddress;
            } else if (view == mOxygenClose) {
                address = StatusOption.OxygenStopAddress;
            }
            if (address != 0) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    controlResetHardware(address);
                    Util.sleep(250);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    controlHardware(address);
                    Util.sleep(250);
                }
            }
        }
        return false;
    }

    private void setHardware(int valueAddress, int commitAddress, int value) {
        getExecutorService().submit(() -> {
            boolean success = ModbusService.setHardwareHoldingRegisterOption(valueAddress, value) && (commitAddress == 0 || ModbusService.setHardwareCoilStatusOption(commitAddress, true));
            if (!success) {
                showToast("操作失败!");
                mHandler.post(this::showStatusOption);
                return;
            }
            mStatusOption = ModbusService.readStatusOption();
            mAirConditionerStatus = ModbusService.readAirConditionerStatus();
            if (mStatusOption.e != null || mAirConditionerStatus.e != null) {
                showToast("信息获取失败");
                mHandler.postDelayed(this::finish, 1000);
            } else {
                mHandler.post(this::showStatusOption);
            }
        });
    }

    private void controlHardware(int address) {
        getExecutorService().submit(() -> {
            boolean success = ModbusService.setHardwareCoilStatusOption(address, true);
            if (!success) {
                showToast("操作失败!");
                mHandler.post(this::showStatusOption);
            }
        });
    }

    private void controlResetHardware(int address) {
        getExecutorService().submit(() -> {
            boolean success = ModbusService.setHardwareCoilStatusOption(address, false);
            if (!success) {
                showToast("复位失败!");
                mHandler.post(this::showStatusOption);
            }
            mStatusOption = ModbusService.readStatusOption();
            mAirConditionerStatus = ModbusService.readAirConditionerStatus();
            mHandler.post(this::showStatusOption);
        });
    }
}
