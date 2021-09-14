package me.liuzs.cabinetmanager;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.text.TextUtils;

import com.puty.sdk.PrinterInstance;
import com.videogo.openapi.EZOpenSDK;

import me.liuzs.cabinetmanager.model.CabinetInfo;
import me.liuzs.cabinetmanager.model.DeviceInfo;
import me.liuzs.cabinetmanager.model.UserInfo;
import me.liuzs.cabinetmanager.service.HardwareService;
import me.liuzs.cabinetmanager.util.StorageUtility;
import me.liuzs.cabinetmanager.util.Util;

public class CabinetApplication extends Application {

    private static CabinetApplication INSTANCE = null;
    private static String mSerialNo;
    private UserInfo mAdminUser;
    private CabinetInfo mCabinetInfo;
    private boolean isInitHardwareManager = true;

    public static CabinetApplication getInstance() {
        return INSTANCE;
    }

    public static String getSerialNo() {
        return mSerialNo;
    }

    public static DeviceInfo getSingleDevice() {
        if (INSTANCE != null && INSTANCE.mCabinetInfo != null && INSTANCE.mCabinetInfo.tankType == 2) {
            return INSTANCE.mCabinetInfo.devices.get(0);
        } else {
            return null;
        }
    }

    public boolean isInitHardwareManager() {
        return isInitHardwareManager;
    }

    public void setInitHardwareManager(boolean init) {
        isInitHardwareManager = init;
    }

    public DeviceInfo getDeviceInfoById(String id) {
        if (mCabinetInfo != null) {
            for (DeviceInfo info : mCabinetInfo.devices) {
                if (TextUtils.equals(info.devId, id)) {
                    return info;
                }
            }
        }
        return null;
    }

    public UserInfo getAdminUser() {
        return mAdminUser;
    }

    public void setAdminUser(UserInfo user) {
        mAdminUser = user;
    }

    public CabinetInfo getCabinetInfo() {
        return mCabinetInfo;
    }

    public void setCabinetInfo(CabinetInfo cabinet) {
        mCabinetInfo = cabinet;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
        INSTANCE = this;
    }

    @SuppressLint("HardwareIds")
    private void init() {
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
        Util.upgradeRootPermission(getPackageCodePath());
        mSerialNo = android.os.Build.SERIAL;
        StorageUtility.init(this);
        PrinterInstance.init(this);
        CtrlFunc.intSystemSetup(this);

        /** * sdk日志开关，正式发布需要去掉 */
        EZOpenSDK.showSDKLog(true);
        /** * 设置是否支持P2P取流,详见api */
        EZOpenSDK.enableP2P(true);
        /** * APP_KEY请替换成自己申请的 */
        EZOpenSDK.initLib(this, Config.EZ_APP_KEY);
    }


    @Override
    public void onTerminate() {
        INSTANCE = null;
//        stopService(new Intent(this, MQTTService.class));
        stopService(new Intent(this, HardwareService.class));
        super.onTerminate();
    }
}
