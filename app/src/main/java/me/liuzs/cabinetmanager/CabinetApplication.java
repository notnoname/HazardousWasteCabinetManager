package me.liuzs.cabinetmanager;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;

import com.puty.sdk.PrinterInstance;
import com.videogo.openapi.EZOpenSDK;

import me.liuzs.cabinetmanager.db.CDatabase;
import me.liuzs.cabinetmanager.service.HardwareService;
import me.liuzs.cabinetmanager.util.StorageUtility;
import me.liuzs.cabinetmanager.util.Util;

public class CabinetApplication extends Application {

    private static CabinetApplication INSTANCE = null;
    private static String mSerialNo;
    private boolean isInitHardwareManager = true;

    public static CabinetApplication getInstance() {
        return INSTANCE;
    }

    public static String getSerialNo() {
        return mSerialNo;
    }

    public boolean isInitHardwareManager() {
        return isInitHardwareManager;
    }

    public void setInitHardwareManager(boolean init) {
        isInitHardwareManager = init;
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
        CabinetCore.init(this);

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
        CDatabase.getInstance().close();
        super.onTerminate();
    }
}
