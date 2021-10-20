package me.liuzs.cabinetmanager;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.Toolbar;

import com.kyleduo.switchbutton.SwitchButton;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import me.liuzs.cabinetmanager.model.modbus.AirConditionerStatus;
import me.liuzs.cabinetmanager.model.modbus.SetupValue;
import me.liuzs.cabinetmanager.model.modbus.StatusOption;
import me.liuzs.cabinetmanager.service.ModbusService;
import me.liuzs.cabinetmanager.util.Util;

/**
 * 控制面板
 */
public class ControlPanelActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {

    public static final String TAG = "ControlPanelActivity";

    public static final int[] ACWorkModelNameResId = {R.string.ac_work_model_auto, R.string.ac_work_model_refrigeration, R.string.ac_work_model_heating, R.string.ac_work_model_dehumidification, R.string.ac_work_model_fan};
    private TextView mFanWorkModelValue, mSoundLightAlertValue, mFanCtrlValue, mOxygenValue, mACCtrlModelValue, mACWorkModelValue, mACPowerValue, mRemoteCtrlModelValue;
    private Button mFCReset;
    private SwitchButton mFanWorkModel, mSoundLightAlert, mFanCtrl, mOxygenCtrl, mACCtrlModel, mACPower;
    private AirConditionerStatus mAirConditionerStatus;
    private final ActivityResultLauncher<Intent> mACWorkModelSelectLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        int resultCode = result.getResultCode();
        if (resultCode == RESULT_OK) {
            try {
                Intent data = result.getData();
                String selectIndex = data.getStringExtra(SpinnerActivity.KEY_SELECT_INDEX);
                Integer index = Integer.parseInt(selectIndex);
                boolean success = ModbusService.setACWorkModel(AirConditionerStatus.WorkModel.values()[index]);
                if (success) {
                    showToast("空调模式设置成功!");
                    mAirConditionerStatus = ModbusService.readAirConditionerStatus();
                } else {
                    showToast("空调模式设置失败!");
                }
            } catch (Exception e) {

            }

        }
    });
    private SetupValue mSetupValue;
    private StatusOption mStatusOption;

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        compoundButton.setChecked(b);
        if (compoundButton == mFanWorkModel) {

        } else if (compoundButton == mSoundLightAlert) {

        } else if (compoundButton == mFanCtrl) {

        } else if (compoundButton == mOxygenCtrl) {

        } else if (compoundButton == mACCtrlModel) {

        } else if (compoundButton == mACPower) {

        }
    }

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
        mOxygenCtrl = findViewById(R.id.swOxygenCtrl);
        mFanWorkModel.setOnCheckedChangeListener(this);
        mSoundLightAlert.setOnCheckedChangeListener(this);
        mFanCtrl.setOnCheckedChangeListener(this);
        mOxygenCtrl.setOnCheckedChangeListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        new GetModbusInfo(this).execute();
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

    static class GetModbusInfo extends AsyncTask<Void, Void, Boolean> {

        private final WeakReference<ControlPanelActivity> mActivity;

        public GetModbusInfo(ControlPanelActivity activity) {
            this.mActivity = new WeakReference<>(activity);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            mActivity.get().mSetupValue = ModbusService.readSetupValue();
            mActivity.get().mStatusOption = ModbusService.readStatusOption();
            mActivity.get().mAirConditionerStatus = ModbusService.readAirConditionerStatus();
            return true;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mActivity.get().showProgressDialog();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mActivity.get().dismissProgressDialog();
            if (mActivity.get().mAirConditionerStatus.e != null || mActivity.get().mSetupValue.e != null || mActivity.get().mStatusOption.e != null) {
                mActivity.get().showToast("设备信息读取失败，请立即检查设备！");
                mActivity.get().mHandler.postDelayed(() -> mActivity.get().finish(), 1000);
            }
            mActivity.get().showStatusOption();

        }
    }
}
