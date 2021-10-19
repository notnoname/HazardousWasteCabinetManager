package me.liuzs.cabinetmanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.Toolbar;

import com.kyleduo.switchbutton.SwitchButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import me.liuzs.cabinetmanager.model.AirConditionerStatus;
import me.liuzs.cabinetmanager.model.SetupValue;
import me.liuzs.cabinetmanager.service.ModbusService;
import me.liuzs.cabinetmanager.util.Util;

/**
 * 控制面板
 */
public class ControlPanelActivity extends BaseActivity {

    public static final String TAG = "ControlPanelActivity";

    public static final int[] ACWorkModelNameResId = {R.string.ac_work_model_auto, R.string.ac_work_model_refrigeration, R.string.ac_work_model_heating, R.string.ac_work_model_dehumidification, R.string.ac_work_model_fan};
    private final ActivityResultLauncher<Intent> mACWorkModelSelectLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        int resultCode = result.getResultCode();
        if (resultCode == RESULT_OK) {
            try {
                Intent data = result.getData();
                String selectIndex = data.getStringExtra(SpinnerActivity.KEY_SELECT_INDEX);
                Integer index = Integer.parseInt(selectIndex);
                if (this.mAirConditionerStatus.e == null) {
                    this.mAirConditionerStatus.workModel = AirConditionerStatus.WorkModel.values()[index];
                }
            } catch (Exception e) {
            }

        }
    });
    private TextView mFanWorkModelValue, mSoundLightAlertValue, mFanCtrlValue, mOxygenValue, mACCtrlModelValue, mACWorkModelValue, mACPowerValue, mRemoteCtrlModelValue;
    private Button mFCReset;
    private SwitchButton mFanWorkModel, mSoundLightAlert, mFanCtrl, mOxygen, mACCtrlModel, mACPower;
    private AirConditionerStatus mAirConditionerStatus;
    private SetupValue mSetupValue;

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_panel);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(null);
        Util.fullScreen(this);
        ((TextView) findViewById(R.id.toolbar_title)).setText(R.string.control_panel);
        mFanWorkModelValue = findViewById(R.id.tvFanWorkModelValue);
        mSoundLightAlertValue = findViewById(R.id.tvSoundLightAlertValue);
        mFanCtrlValue = findViewById(R.id.tvFanCtrlValue);
        mOxygenValue = findViewById(R.id.tvOxygenCtrlValue);

        mFanWorkModel = findViewById(R.id.swFanModel);
        mSoundLightAlert = findViewById(R.id.swSoundLightAlert);
        mFanCtrl = findViewById(R.id.swFanCtrl);
        mOxygen = findViewById(R.id.swOxygenCtrl);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAirConditionerStatus = ModbusService.readAirConditionerStatus();
        mSetupValue = ModbusService.readSetupValue();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void onBackButtonClick(View view) {
        finish();
    }

    public void onFCResetButtonClick(View view) {
        finish();
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
        finish();
    }
}
