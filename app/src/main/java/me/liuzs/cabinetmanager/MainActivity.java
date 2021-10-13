package me.liuzs.cabinetmanager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.util.Objects;

import me.liuzs.cabinetmanager.model.Cabinet;
import me.liuzs.cabinetmanager.model.HardwareValue;
import me.liuzs.cabinetmanager.model.SetupValue;
import me.liuzs.cabinetmanager.service.HardwareService;
import me.liuzs.cabinetmanager.util.Util;

public class MainActivity extends BaseActivity {

    public static final String TAG = "MainActivity";

    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION
    };
    private final Gson mGson = new Gson();
    private final DecimalFormat mDecimalFormat = new DecimalFormat("0.00");
    private final DecimalFormat mHumiDecimalFormat = new DecimalFormat("0");
    private final DecimalFormat mTempDecimalFormat = new DecimalFormat("0.0");
    private TextView mTvoc1, mTvoc2, mHumi, mTemperature, mName;
    private ImageView mFanState, mLockerState, mAlert;
    private LinearLayout mLTVOCs2, mLLocker, mLFan;
    private HardwareValueBroadcastReceiver mHardwareValueBroadcastReceiver;
    private HardwareValue mValue = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(null);
        Util.fullScreen(this);
        mLTVOCs2 = findViewById(R.id.llPG);
        mLLocker = findViewById(R.id.llAC);
        mLFan = findViewById(R.id.llFanStatus);
        mTvoc1 = findViewById(R.id.tvTVOCs1Value);
        mTvoc2 = findViewById(R.id.tvTVOCs2Value);
        mHumi = findViewById(R.id.tvHumidityValue);
        mTemperature = findViewById(R.id.tvTemperatureValue);
        mAlert = findViewById(R.id.ivNetworkStatus);
        mFanState = findViewById(R.id.ivFanStatusValue);
        mLockerState = findViewById(R.id.ivACCtrlOption);
        mName = findViewById(R.id.tvCabinetName);
    }

    private boolean isTvocs2Exist() {
        return CabinetCore.getMainTVOCModelCount(this) == 2;
    }

    private void showHardwareValue(HardwareValue value) {
        Cabinet info = CabinetCore.getCabinetInfo();
        if (info != null) {
            mName.setText(info.name);
        }
        mValue = value;
        if (mValue == null) {
            return;
        }
        float tvoc1value = mValue.tvoCsValue.TVOC1.ppb;
        mTvoc1.setText(mDecimalFormat.format(tvoc1value / 1000));
        if (isTvocs2Exist()) {
            mLTVOCs2.setVisibility(View.VISIBLE);
//            mLLocker.setBackground(new ColorDrawable(0x00000000));
//            mLFan.setBackground(new ColorDrawable(0x40000000));
            float tvoc2value = mValue.tvoCsValue.TVOC2.ppb;
            mTvoc2.setText(mDecimalFormat.format(tvoc2value / 1000));
        } else {
            mLTVOCs2.setVisibility(View.GONE);
//            mLLocker.setBackground(new ColorDrawable(0x40000000));
//            mLFan.setBackground(new ColorDrawable(0x00000000));
        }
        float humi = mValue.tvoCsValue.TVOC1.humi;

        mHumi.setText(mHumiDecimalFormat.format(humi / 100));
        float temp = mValue.tvoCsValue.TVOC1.temperature;
        mTemperature.setText(mTempDecimalFormat.format(temp / 100));

//        if (CabinetApplication.getSingleDevice() == null) {
//            findViewById(R.id.llLocker).setVisibility(View.GONE);
//            findViewById(R.id.llSubBoardStatus).setVisibility(View.VISIBLE);
//        } else {
//            findViewById(R.id.llLocker).setVisibility(View.VISIBLE);
//            findViewById(R.id.llSubBoardStatus).setVisibility(View.GONE);
//            if (mValue.lock) {
//                mLockerState.setImageResource(R.drawable.ic_green_circle);
//                mLocker.setText(R.string.lock);
//            } else {
//                mLockerState.setImageResource(R.drawable.ic_red_circle);
//                mLocker.setText(R.string.unlock);
//            }
//        }

        if (mValue.greenLight) {
            mAlert.setImageResource(R.drawable.ic_green_circle);
        } else {
            mAlert.setImageResource(R.drawable.ic_red_circle);
        }

        ConstraintLayout infoViewGroup = findViewById(R.id.constraintLayout);
        int childCount = infoViewGroup.getChildCount();
        boolean transform = true;
        for (int i = 0; i < childCount; i++) {
            View child = infoViewGroup.getChildAt(i);
            if (child instanceof LinearLayout && child.getVisibility() == View.VISIBLE) {
                if (transform) {
                    child.setBackground(new ColorDrawable(0x00000000));
                } else {
                    child.setBackground(new ColorDrawable(0x40000000));
                }
                transform = !transform;
            }
        }
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
        if (mValue == null) {
            return;
        }
        showAuthActivity(CabinetCore.RoleType.Admin, new AuthListener() {
            @Override
            public void onAuthCancel() {
            }

            @Override
            public void onAuthSuccess(CabinetCore.RoleType type) {
                Intent intent = new Intent(MainActivity.this, LockerManageActivity.class);
                startActivity(intent);
            }
        });
    }


    public void onSubBoardButtonClick(View view) {
        Intent intent = new Intent(MainActivity.this, SubBoardInfoActivity.class);
        startActivity(intent);
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
            HardwareValue value = mGson.fromJson(json, HardwareValue.class);
            mHandler.post(() -> showHardwareValue(value));
        }
    }
}