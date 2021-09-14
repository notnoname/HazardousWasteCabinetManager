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
import android.os.AsyncTask;
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
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import me.liuzs.cabinetmanager.model.CabinetInfo;
import me.liuzs.cabinetmanager.model.HardwareValue;
import me.liuzs.cabinetmanager.model.SetupValue;
import me.liuzs.cabinetmanager.model.UserInfo;
import me.liuzs.cabinetmanager.net.APIJSON;
import me.liuzs.cabinetmanager.net.LoginJSON;
import me.liuzs.cabinetmanager.net.RemoteAPI;
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
    private AppCompatButton mFan, mLocker;
    private LinearLayout mLTVOCs2, mLLocker, mLFan;
    private HardwareValueBroadcastReceiver mHardwareValueBroadcastReceiver;
    private HardwareValue mValue = null;
    private final ServiceConnection mFanServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "FanServiceConnected success");
            HardwareService.HardwareServiceBinder binder = (HardwareService.HardwareServiceBinder) service;
            mValue.fan = !mValue.fan;
            binder.getHardwareService().switchFanControl(mValue.fan);
            unbindService(this);
            mFan.setEnabled(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            unbindService(mFanServiceConnection);
            Log.d(TAG, "FanServiceConnected fail");
        }
    };
    private final ServiceConnection mLockerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "LockerServiceConnected success");
            HardwareService.HardwareServiceBinder binder = (HardwareService.HardwareServiceBinder) service;
            mValue.lock = !mValue.lock;
            binder.getHardwareService().switchLockerControl(mValue.lock);
            unbindService(this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            unbindService(mLockerServiceConnection);
            Log.d(TAG, "LockerServiceConnected fail");
        }
    };
    private Timer mAuthTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(null);
        Util.fullScreen(this);
        mLTVOCs2 = findViewById(R.id.llTVOCs2);
        mLLocker = findViewById(R.id.llLocker);
        mLFan = findViewById(R.id.llFanStatus);
        mTvoc1 = findViewById(R.id.tvTVOCs1Value);
        mTvoc2 = findViewById(R.id.tvTVOCs2Value);
        mHumi = findViewById(R.id.tvHumidityValue);
        mTemperature = findViewById(R.id.tvTemperatureValue);
        mAlert = findViewById(R.id.ivAlert);
        mFanState = findViewById(R.id.ivFanStatusValue);
        mLockerState = findViewById(R.id.ivLockerValue);
        mFan = findViewById(R.id.btnFan);
        mLocker = findViewById(R.id.btnLocker);
        mName = findViewById(R.id.tvName);
    }

    private boolean isTvocs2Exist() {
        return CtrlFunc.getMainTVOCModelCount(this) == 2;
    }

    private void showHardwareValue(HardwareValue value) {
        CabinetInfo info = CabinetApplication.getInstance().getCabinetInfo();
        if (info != null) {
            mName.setText(info.tankName);
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

        if (CabinetApplication.getSingleDevice() == null) {
            findViewById(R.id.llLocker).setVisibility(View.GONE);
            findViewById(R.id.llSubBoardStatus).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.llLocker).setVisibility(View.VISIBLE);
            findViewById(R.id.llSubBoardStatus).setVisibility(View.GONE);
            if (mValue.lock) {
                mLockerState.setImageResource(R.drawable.ic_green_circle);
                mLocker.setText(R.string.lock);
            } else {
                mLockerState.setImageResource(R.drawable.ic_red_circle);
                mLocker.setText(R.string.unlock);
            }
        }

        if (mValue.greenLight) {
            mAlert.setImageResource(R.drawable.ic_green_circle);
        } else {
            mAlert.setImageResource(R.drawable.ic_red_circle);
        }
        if (mValue.fan) {
            mFanState.setImageResource(R.drawable.ic_green_circle);
            mFan.setText(R.string.fan_stop);
        } else {
            mFanState.setImageResource(R.drawable.ic_red_circle);
            mFan.setText(R.string.fan_start);
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
            systemInti();
        }
        mHardwareValueBroadcastReceiver = new HardwareValueBroadcastReceiver();
        IntentFilter filter = new IntentFilter(Config.ACTION_HARDWARE_VALUE_SEND);
        registerReceiver(mHardwareValueBroadcastReceiver, filter);
        Log.d(TAG, "RegisterReceiver");

    }

    /**
     * 系统初始化
     */
    private void systemInti() {
        //检查是人像识别库是否全
//        if (!Config.isLibraryExists(this)) {
//            showToast(getString(R.string.library_not_found));
//            return;
//        }
        //检查人像识别库是否激活。
        CtrlFunc.checkARCActive(this, new CtrlFunc.CheckARCActiveListener() {
            @Override
            public void onCheckARCActiveFailure(String message, int code) {
                startActivity(new Intent(MainActivity.this, HardwareSetupActiveActivity.class));
            }

            @Override
            public void onCheckARCActiveSuccess() {
                startCheckAdminUserInfo();
            }
        });
    }

    /**
     * 检查用户登录信息
     */
    private void startCheckAdminUserInfo() {
        UserInfo userInfo = CtrlFunc.getAdministratorInfo(MainActivity.this);
//        userInfo = null;
        if (userInfo == null) {
            startActivity(new Intent(MainActivity.this, AccountSettingActivity.class));
        } else {
            CabinetApplication.getInstance().setAdminUser(userInfo);
            startCabinetNameSetting();
            startAuthTimer();
        }
    }

    /**
     * 鉴权信息会过期，每半个小时重新登录一次
     */
    private void startAuthTimer() {
        if (mAuthTimer == null) {
            mAuthTimer = new Timer();
            mAuthTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    new LoginTask().execute(
                            CtrlFunc.getAdminUserID(MainActivity.this), CtrlFunc.getAdminPasswordMD5(MainActivity.this));
                }
            }, 10 * 1000, 30 * 60 * 1000);
        }
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
                systemInti();
            } else {
                showToast(getString(R.string.permission_denied));
            }
        }
    }

    public void onSubCabinetLockerButtonClick(View view) {
        if (mValue == null) {
            return;
        }
        if (CabinetApplication.getSingleDevice() == null) {
            showAuthActivity(AuthType.Admin, new AuthListener() {
                @Override
                public void onAuthCancel() {
                }

                @Override
                public void onAuthSuccess(String id) {
                    Intent intent = new Intent(MainActivity.this, LockerManageActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    public void onLockerButtonClick(View view) {
        if (mValue == null) {
            return;
        }
        if (CabinetApplication.getSingleDevice() != null) {
            if (mValue.lock) {
                Intent intent = new Intent();
                intent.setClassName(getPackageName(), HardwareService.class.getName());
                bindService(intent, mLockerServiceConnection, BIND_AUTO_CREATE);
            } else {
                showAuthActivity(AuthType.Admin, new AuthListener() {
                    @Override
                    public void onAuthCancel() {
                    }

                    @Override
                    public void onAuthSuccess(String id) {
                        Intent intent = new Intent();
                        intent.setClassName(getPackageName(), HardwareService.class.getName());
                        bindService(intent, mLockerServiceConnection, BIND_AUTO_CREATE);
                    }
                });
            }
        }
    }

    public void onFanButtonClick(View view) {
        if (mValue == null) {
            return;
        }
        SetupValue setup = CtrlFunc.getSetupValue(this);
        if (mValue.fan) {
            assert setup != null;
            if (setup.fanAuto) {
                showToast("风机处于自动工作状态，不能手动关闭！");
                return;
            } else {
                showToast("手动模式下关闭风机将会将风机设置切换为自动模式！");
                setup.fanAuto = true;
                CtrlFunc.saveSetupValue(MainActivity.this, setup);
            }
        }
        view.setEnabled(false);
        Intent intent = new Intent();
        intent.setClassName(getPackageName(), HardwareService.class.getName());
        bindService(intent, mFanServiceConnection, BIND_AUTO_CREATE);
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
            showAuthActivity(AuthType.Admin, new AuthListener() {
                @Override
                public void onAuthCancel() {
                }

                @Override
                public void onAuthSuccess(String id) {
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
            showAuthActivity(AuthType.Admin, new AuthListener() {
                @Override
                public void onAuthCancel() {
                }

                @Override
                public void onAuthSuccess(String id) {
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
            showAuthActivity(AuthType.Admin, new AuthListener() {
                @Override
                public void onAuthCancel() {
                }

                @Override
                public void onAuthSuccess(String id) {
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
            showAuthActivity(AuthType.Admin, new AuthListener() {
                @Override
                public void onAuthCancel() {
                }

                @Override
                public void onAuthSuccess(String id) {
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
            showAuthActivity(AuthType.Admin, new AuthListener() {
                @Override
                public void onAuthCancel() {
                }

                @Override
                public void onAuthSuccess(String id) {
                    Intent intent = new Intent(MainActivity.this, ReturnAfterUseActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    private void startCabinetNameSetting() {
        CabinetInfo cabinetInfo = CtrlFunc.getCabinetInfo(this);
        if (cabinetInfo == null) {
            Intent intent = new Intent(this, CabinetNameActivity.class);
            startActivity(intent);
        } else {
            CabinetApplication.getInstance().setCabinetInfo(cabinetInfo);
            //        startService(new Intent(this, MQTTService.class));
            startService(new Intent(this, HardwareService.class));
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class LoginTask extends AsyncTask<String, Void, APIJSON<LoginJSON>> {

        @Override
        protected APIJSON<LoginJSON> doInBackground(String... strings) {
            return RemoteAPI.System.login(strings[0], strings[1]);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(APIJSON<LoginJSON> json) {
            super.onPostExecute(json);
            if (json.code == 200) {
                UserInfo userInfo = json.data.userInfo;
                String token = json.data.token;
                userInfo.token = token;
                CtrlFunc.saveAdmin1(MainActivity.this, token, userInfo);
                CabinetApplication.getInstance().setAdminUser(CtrlFunc.getAdministratorInfo(MainActivity.this));
            } else if (json.code == 102) {
                CtrlFunc.removeAdminUserInfo(MainActivity.this);
                startActivity(new Intent(MainActivity.this, AccountSettingActivity.class));
            }
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