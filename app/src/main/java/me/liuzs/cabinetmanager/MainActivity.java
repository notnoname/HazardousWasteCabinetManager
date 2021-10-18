package me.liuzs.cabinetmanager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import java.text.DecimalFormat;
import java.util.Objects;

import me.liuzs.cabinetmanager.model.AirConditionerStatus;
import me.liuzs.cabinetmanager.model.AlertStatus;
import me.liuzs.cabinetmanager.model.Cabinet;
import me.liuzs.cabinetmanager.model.EnvironmentStatus;
import me.liuzs.cabinetmanager.model.FrequencyConverterStatus;
import me.liuzs.cabinetmanager.model.HardwareValue;
import me.liuzs.cabinetmanager.model.SetupValue;
import me.liuzs.cabinetmanager.util.Util;

public class MainActivity extends BaseActivity {

    public static final String TAG = "MainActivity";

    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION
    };
    private final DecimalFormat mDecimalFormat = new DecimalFormat("0.00");
    private final DecimalFormat mHumiDecimalFormat = new DecimalFormat("0");
    private final DecimalFormat mTempDecimalFormat = new DecimalFormat("0.0");
    private TextView mVOCLowPart, mVOCUpperPart, mFGLowPart, mFGUpperPart, mHumidityA, mHumidityB, mTemperatureA, mTemperatureB, mCabinetName, mACTargetTemp, mFanSpeed;
    private ImageView mNetworkStatus, mACStatus, mACCtrlModel, mACWorkModel, mFanStatus, mFanStatusDetail;
    private HardwareValueBroadcastReceiver mHardwareValueBroadcastReceiver;
    private HardwareValue mHardwareValue = null;
    private LinearLayout mAlertStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(null);
        Util.fullScreen(this);
        mNetworkStatus = findViewById(R.id.ivNetworkStatusValue);
        mCabinetName = findViewById(R.id.tvCabinetNameValue);

        mVOCLowPart = findViewById(R.id.tvTVOCs1Value);
        mVOCUpperPart = findViewById(R.id.tvTVOCs2Value);
        mFGLowPart = findViewById(R.id.tvFGValueAValue);
        mFGUpperPart = findViewById(R.id.tvFGValueBValue);
        mHumidityA = findViewById(R.id.tvHumidityAValue);
        mHumidityB = findViewById(R.id.tvHumidityBValue);
        mTemperatureA = findViewById(R.id.tvTempAValue);
        mTemperatureB = findViewById(R.id.tvTempBValue);

        mACStatus = findViewById(R.id.ivACStatusValue);
        mACCtrlModel = findViewById(R.id.ivACCtrlModelValue);
        mACWorkModel = findViewById(R.id.ivACWorkModelValue);
        mACTargetTemp = findViewById(R.id.tvACTempValue);

        mFanStatus = findViewById(R.id.ivFanStatusValue);
        mFanStatusDetail = findViewById(R.id.ivFanStatusDetailValue);
        mFanSpeed = findViewById(R.id.tvFanSpeedValue);

        mAlertStatus = findViewById(R.id.llAlertStatus);
    }

    private void showHardwareValue(HardwareValue value) {

        if (Util.isNetworkConnected(this)) {
            mNetworkStatus.setImageResource(R.drawable.ic_green_circle);
        } else {
            mNetworkStatus.setImageResource(R.drawable.ic_red_circle);
        }

        Cabinet info = CabinetCore.getCabinetInfo();
        if (info != null) {
            mCabinetName.setText(info.name);
        }
        mHardwareValue = value;
        if (mHardwareValue == null) {
            return;
        }
        SetupValue setupValue = mHardwareValue.setupValue;
        if (setupValue == null) {
            return;
        }
        EnvironmentStatus environmentStatus = mHardwareValue.environmentStatus;
        if (environmentStatus != null) {
            mVOCLowPart.setText(mDecimalFormat.format(environmentStatus.vocLowerPart));
            mVOCUpperPart.setText(mDecimalFormat.format(environmentStatus.vocUpperPart));
            mFGLowPart.setText(mDecimalFormat.format(environmentStatus.fgLowerPart));
            mFGUpperPart.setText(mDecimalFormat.format(environmentStatus.fgUpperPart));
            mTemperatureA.setText(mTempDecimalFormat.format(environmentStatus.tempA));
            mTemperatureB.setText(mTempDecimalFormat.format(environmentStatus.tempB));
            mHumidityA.setText(mHumiDecimalFormat.format(environmentStatus.humidityA));
            mHumidityB.setText(mHumiDecimalFormat.format(environmentStatus.humidityB));
        }

        AirConditionerStatus airConditionerStatus = mHardwareValue.airConditionerStatus;
        if (airConditionerStatus != null) {
            if (airConditionerStatus.powerOn) {
                mACStatus.setImageResource(R.drawable.ic_run);
            } else {
                mACStatus.setImageResource(R.drawable.ic_stop);
            }
            if (airConditionerStatus.autoCtrl) {
                mACCtrlModel.setImageResource(R.drawable.ic_manual);
            }
            switch (airConditionerStatus.workModel) {
                case Auto:
                    mACWorkModel.setImageResource(R.drawable.ic_ac_work_model_auto);
                    break;
                case Refrigeration:
                    mACWorkModel.setImageResource(R.drawable.ic_ac_work_model_refrigeration);
                    break;
                case Heating:
                    mACWorkModel.setImageResource(R.drawable.ic_ac_work_model_heating);
                    break;
                case Dehumidification:
                    mACWorkModel.setImageResource(R.drawable.ic_ac_work_model_dehumidification);
                    break;
                case AirSupply:
                    mACWorkModel.setImageResource(R.drawable.ic_ac_work_model_air_supply);
                    break;
            }
            mACTargetTemp.setText(String.valueOf(airConditionerStatus.targetTemp));
        }

        FrequencyConverterStatus frequencyConverterStatus = mHardwareValue.frequencyConverterStatus;
        if (frequencyConverterStatus != null) {
            if (frequencyConverterStatus.status == FrequencyConverterStatus.Status.Clockwise || frequencyConverterStatus.status == FrequencyConverterStatus.Status.Counterclockwise) {
                mFanStatus.setImageResource(R.drawable.ic_run);
            } else {
                mFanStatus.setImageResource(R.drawable.ic_stop);
            }
            switch (frequencyConverterStatus.status) {
                case Clockwise:
                    mFanStatusDetail.setImageResource(R.drawable.ic_clock_wise);
                    break;
                case Counterclockwise:
                    mFanStatusDetail.setImageResource(R.drawable.ic_counter_colock_wise);
                    break;
                case Stop:
                    mFanStatusDetail.setImageResource(R.drawable.ic_fc_stop);
                    break;
                case PowerOff:
                    mFanStatusDetail.setImageResource(R.drawable.ic_power_off);
                    break;
                case Fault:
                    mFanStatusDetail.setImageResource(R.drawable.ic_fault);
                    break;
            }
            mFanSpeed.setText(String.valueOf(frequencyConverterStatus.rotatingSpeed));
        }

        AlertStatus alertStatus = mHardwareValue.alertStatus;
        if (alertStatus != null) {
            mAlertStatus.removeAllViews();
            boolean safe = !alertStatus.vocAlert && !alertStatus.fgAlert && !alertStatus.tempHighAlert && !alertStatus.tempLowAlert && !alertStatus.humidityHighAlert && !alertStatus.humidityLowAlert && !alertStatus.fireAlert;
            if (safe) {
                addAlertStatusTextViewToContainer("无异常", R.drawable.background_state_green);
            } else {
                if (alertStatus.vocAlert) {
                    addAlertStatusTextViewToContainer("VOC浓度高", R.drawable.background_state_red);
                }
                if (alertStatus.fgAlert) {
                    addAlertStatusTextViewToContainer("可燃气体浓度高", R.drawable.background_state_red);
                }
                if (alertStatus.tempHighAlert) {
                    addAlertStatusTextViewToContainer("温度高", R.drawable.background_state_red);
                }
                if (alertStatus.tempLowAlert) {
                    addAlertStatusTextViewToContainer("温度低", R.drawable.background_state_red);
                }
                if (alertStatus.humidityHighAlert) {
                    addAlertStatusTextViewToContainer("湿度高", R.drawable.background_state_red);
                }
                if (alertStatus.humidityLowAlert) {
                    addAlertStatusTextViewToContainer("湿度低", R.drawable.background_state_red);
                }
                if (alertStatus.fireAlert) {
                    addAlertStatusTextViewToContainer("火警", R.drawable.background_state_red);
                }
            }

        }
    }

    public void addAlertStatusTextViewToContainer(String text, int bgRes) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(24);
        textView.setPadding(10, 10, 10, 10);
        textView.setGravity(Gravity.CENTER);
        textView.setBackgroundResource(bgRes);
        LinearLayout.LayoutParams lps = new LinearLayout.LayoutParams(180, LinearLayout.LayoutParams.WRAP_CONTENT);
        lps.setMargins(10, 10, 10, 10);
        mAlertStatus.addView(textView, lps);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        } else {
            CabinetCore.startValidateSystem();
        }
        mHardwareValueBroadcastReceiver = new HardwareValueBroadcastReceiver();
        IntentFilter filter = new IntentFilter(Config.ACTION_HARDWARE_VALUE_SEND);
        registerReceiver(mHardwareValueBroadcastReceiver, filter);
        Log.d(TAG, "RegisterReceiver");

    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mHardwareValueBroadcastReceiver);
        Log.d(TAG, "UnregisterReceiver");
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setting:
                Toast.makeText(this, "Start MQTT subscribe.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_mqtt:
                Toast.makeText(this, "Start TVOCs publish.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, MQTTClientActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            if (isAllGranted) {
                CabinetCore.startValidateSystem();
            } else {
                showToast(getString(R.string.permission_denied));
            }
        }
    }

    public void onSubCabinetLockerButtonClick(View view) {
        if (mHardwareValue == null) {
            return;
        }
        showAuthActivity(CabinetCore.RoleType.Admin, new AuthListener() {
            @Override
            public void onAuthCancel() {
            }

            @Override
            public void onAuthSuccess(CabinetCore.RoleType type) {
                Intent intent = new Intent(MainActivity.this, ControlPanelActivity.class);
                startActivity(intent);
            }
        });
    }


    public void onControlPanelButtonClick(View view) {

        if (TextUtils.equals(BuildConfig.BUILD_TYPE, "debug")) {
            Intent intent = new Intent(MainActivity.this, ControlPanelActivity.class);
            startActivity(intent);
        } else {
            showAuthActivity(CabinetCore.RoleType.Operator, new AuthListener() {
                @Override
                public void onAuthCancel() {
                }

                @Override
                public void onAuthSuccess(CabinetCore.RoleType type) {
                    Intent intent = new Intent(MainActivity.this, ControlPanelActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    public void onSettingButtonClick(View view) {
        if (TextUtils.equals(BuildConfig.BUILD_TYPE, "debug")) {
            Intent intent = new Intent(MainActivity.this, SystemSettingActivity.class);
            startActivity(intent);
        } else {
            showAuthActivity(CabinetCore.RoleType.Admin, new AuthListener() {
                @Override
                public void onAuthCancel() {
                }

                @Override
                public void onAuthSuccess(CabinetCore.RoleType type) {
                    Intent intent = new Intent(MainActivity.this, SystemSettingActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    public void onTakeOutButtonClick(View view) {
        if (TextUtils.equals(BuildConfig.BUILD_TYPE, "debug")) {
            Intent intent = new Intent(MainActivity.this, TakeOutActivity.class);
            startActivity(intent);
        } else {
            showAuthActivity(CabinetCore.RoleType.Operator, new AuthListener() {
                @Override
                public void onAuthCancel() {
                }

                @Override
                public void onAuthSuccess(CabinetCore.RoleType type) {
                    Intent intent = new Intent(MainActivity.this, TakeOutActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    public void onCameraButtonClick(View view) {
        Intent intent = new Intent(MainActivity.this, CameraListActivity.class);
        startActivity(intent);
    }

    public void onFirstTakeInButtonClick(View view) {
        if (TextUtils.equals(BuildConfig.BUILD_TYPE, "debug")) {
            Intent intent = new Intent(MainActivity.this, StorageActivity.class);
            startActivity(intent);
        } else {
            showAuthActivity(CabinetCore.RoleType.Operator, new AuthListener() {
                @Override
                public void onAuthCancel() {
                }

                @Override
                public void onAuthSuccess(CabinetCore.RoleType type) {
                    Intent intent = new Intent(MainActivity.this, StorageActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    public void onInventoryQueryButtonClick(View view) {
        Intent intent = new Intent(MainActivity.this, InventoryQueryActivity.class);
        startActivity(intent);
    }

    public void onStandingBookQueryButtonClick(View view) {
        if (TextUtils.equals(BuildConfig.BUILD_TYPE, "debug")) {
            Intent intent = new Intent(MainActivity.this, StandingBookActivity.class);
            startActivity(intent);
        } else {
            showAuthActivity(CabinetCore.RoleType.Operator, new AuthListener() {
                @Override
                public void onAuthCancel() {
                }

                @Override
                public void onAuthSuccess(CabinetCore.RoleType type) {
                    Intent intent = new Intent(MainActivity.this, StandingBookActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    public void onReturnAfterUseButtonClick(View view) {
        if (TextUtils.equals(BuildConfig.BUILD_TYPE, "debug")) {
            Intent intent = new Intent(MainActivity.this, ReturnAfterUseActivity.class);
            startActivity(intent);
        } else {
            showAuthActivity(CabinetCore.RoleType.Operator, new AuthListener() {
                @Override
                public void onAuthCancel() {
                }

                @Override
                public void onAuthSuccess(CabinetCore.RoleType type) {
                    Intent intent = new Intent(MainActivity.this, ReturnAfterUseActivity.class);
                    startActivity(intent);
                }
            });
        }
    }


    private class HardwareValueBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "OnReceiver");
            String json = intent.getStringExtra(Config.KEY_HARDWARE_VALUE);
            HardwareValue value = CabinetCore.GSON.fromJson(json, HardwareValue.class);
            mHandler.post(() -> showHardwareValue(value));
        }
    }
}