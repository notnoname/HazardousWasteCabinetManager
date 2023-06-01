package me.liuzs.cabinetmanager;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.puty.sdk.PrinterInstance;
import com.umeng.commonsdk.UMConfigure;
import com.videogo.openapi.EZOpenSDK;

import me.liuzs.cabinetmanager.db.CabinetDatabase;
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
        UMConfigure.preInit(this, Config.UMENG_APP_KEY, "Default");
        UMConfigure.setLogEnabled(true);
        //初始化组件化基础库, 所有友盟业务SDK都必须调用此初始化接口。
        UMConfigure.init(this,  Config.UMENG_APP_KEY, "Default", UMConfigure.DEVICE_TYPE_BOX, "");
        Util.upgradeRootPermission(getPackageCodePath());
        mSerialNo = android.os.Build.SERIAL;
        StorageUtility.init(this);
        try {
            PrinterInstance.init(this);
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
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
        CabinetDatabase.getInstance().close();
        super.onTerminate();
    }
}
