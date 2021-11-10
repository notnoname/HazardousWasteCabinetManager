package me.liuzs.cabinetmanager;

import static me.liuzs.cabinetmanager.CabinetCore.RoleType.Admin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.arcsoft.face.ActiveFileInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.enums.RuntimeABI;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.afinal.simplecache.ACache;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import me.liuzs.cabinetmanager.db.CabinetDatabase;
import me.liuzs.cabinetmanager.model.AlertLog;
import me.liuzs.cabinetmanager.model.Cabinet;
import me.liuzs.cabinetmanager.model.DepositRecord;
import me.liuzs.cabinetmanager.model.HardwareValue;
import me.liuzs.cabinetmanager.model.Laboratory;
import me.liuzs.cabinetmanager.model.OptLog;
import me.liuzs.cabinetmanager.model.User;
import me.liuzs.cabinetmanager.net.APIJSON;
import me.liuzs.cabinetmanager.net.RemoteAPI;
import me.liuzs.cabinetmanager.printer.PrinterBluetoothInfo;
import me.liuzs.cabinetmanager.service.HardwareService;
import me.liuzs.cabinetmanager.util.Util;

@SuppressLint({"SimpleDateFormat", "StaticFieldLeak"})
@SuppressWarnings({"unused"})
public class CabinetCore {

    public final static SimpleDateFormat _YearFormatter = new SimpleDateFormat("yyyy-MM-dd");
    public final static SimpleDateFormat _HourFormatter = new SimpleDateFormat("HH:mm a");
    public final static SimpleDateFormat _MilliSecondFormatter = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss.SSS");
    public final static SimpleDateFormat _SecondFormatter = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");
    public final static SimpleDateFormat _DayFormatter = new SimpleDateFormat(
            "yyyyMMdd");
    public final static SimpleDateFormat _NoSplitYearFormatter = new SimpleDateFormat(
            "yyyyMMddHHmmss");
    public final static Gson GSON = new Gson();
    private static final String TAG = "CabinetCore";
    private final static ExecutorService executorService = Executors.newFixedThreadPool(1);
    private static final String KEY_OPT_LOG_INDEX = "KEY_OPT_LOG_INDEX";
    private static final Type LaboratoryListType = new TypeToken<List<Laboratory>>() {
    }.getType();
    private static Timer mAuthTimer;
    private static Context mContext;
    private static ACache _ACache;

    public static void showToast(String text) {
        if (mContext != null) {
            Toast toast = Toast.makeText(mContext, text, Toast.LENGTH_LONG);
            toast.show();
        }
    }

    /**
     * 启动系统各项配置的校验，从ARC库开始。
     */
    public static void startValidateSystem() {
//检查是人像识别库是否全
        if (!Config.isLibraryExists(mContext)) {
            showToast(mContext.getString(R.string.library_not_found));
            return;
        }
        validateARCActive(new CabinetCore.CheckARCActiveListener() {
            @Override
            public void onCheckARCActiveFailure(String message, int code) {
                if (CabinetCore.isDebugState()) {
                    CabinetCore.validateAdminUserInfo();
                } else {
                    Intent intent = new Intent(mContext, HardwareSetupActiveActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
            }

            @Override
            public void onCheckARCActiveSuccess() {
                CabinetCore.validateAdminUserInfo();
            }
        });
    }

    public static void validateARCActive(CheckARCActiveListener lsn) {
        Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
            try {
                RuntimeABI runtimeABI = FaceEngine.getRuntimeABI();
                Log.i(TAG, "subscribe: getRuntimeABI() " + runtimeABI);
                long start = System.currentTimeMillis();
                int activeCode = FaceEngine.activeOnline(mContext, Config.ARC_APP_ID, Config.ARC_SDK_KEY);
                Log.i(TAG, "subscribe cost: " + (System.currentTimeMillis() - start));
                emitter.onNext(activeCode);
            } catch (Exception e) {
                e.printStackTrace();
                lsn.onCheckARCActiveFailure(e.getMessage(), -1);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(@NotNull Disposable d) {
            }

            @Override
            public void onNext(@NotNull Integer activeCode) {
                if (activeCode == ErrorInfo.MOK) {
                    Toast.makeText(mContext, R.string.active_success, Toast.LENGTH_SHORT).show();
                    lsn.onCheckARCActiveSuccess();
                } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                    lsn.onCheckARCActiveSuccess();
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.active_failed, activeCode), Toast.LENGTH_SHORT).show();
                    lsn.onCheckARCActiveFailure(mContext.getString(R.string.active_failed, activeCode), activeCode);
                }
                ActiveFileInfo activeFileInfo = new ActiveFileInfo();
                int res = FaceEngine.getActiveFileInfo(mContext, activeFileInfo);
                if (res == ErrorInfo.MOK) {
                    Log.i(TAG, activeFileInfo.toString());
                }
            }

            @Override
            public void onError(@NotNull Throwable e) {
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                lsn.onCheckARCActiveFailure(e.getMessage(), -1);
            }

            @Override
            public void onComplete() {
            }
        });
    }

    public static User getCabinetUser(RoleType type) {
        try {
            SharedPreferences sp = mContext.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
            String userInfoStr = sp.getString(Config.SYSPRE_Cabinet_User + type, null);
            return GSON.fromJson(userInfoStr, User.class);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getModbusAddress() {
        try {
            SharedPreferences sp = mContext.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
            return sp.getString(Config.MODBUS_ADDRESS, "127.0.0.1");
        } catch (Exception e) {
            return null;
        }
    }

    public static void saveModbusAddress(String address) {
        SharedPreferences sp = mContext.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Config.MODBUS_ADDRESS, address);
        editor.apply();
    }

    public static Cabinet getCabinetInfo() {
        SharedPreferences sp = mContext.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        String cabinetInfo = sp.getString(Config.SYSPRE_CABINET_INFO, null);
        if (TextUtils.isEmpty(cabinetInfo)) {
            return null;
        } else {
            return GSON.fromJson(cabinetInfo, Cabinet.class);
        }
    }


    public static void saveCabinetInfo(Cabinet info) {
        SharedPreferences sp = mContext.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (info == null) {
            editor.remove(Config.SYSPRE_CABINET_INFO);
        } else {
            editor.putString(Config.SYSPRE_CABINET_INFO, GSON.toJson(info));
        }
        editor.apply();
    }

    public static void saveLaboratoryListCache(List<Laboratory> laboratoryList) {
        SharedPreferences sp = mContext.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Config.SYSPRE_Laboratory_Cache, GSON.toJson(laboratoryList));
        editor.apply();
    }

    public static List<Laboratory> getLaboratoryListCache() {
        SharedPreferences sp = mContext.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        String cabinetInfo = sp.getString(Config.SYSPRE_Laboratory_Cache, null);
        if (TextUtils.isEmpty(cabinetInfo)) {
            return null;
        } else {
            return GSON.fromJson(cabinetInfo, LaboratoryListType);
        }
    }

    public static void saveCabinetUser(User user, RoleType type, boolean log) {
        if (log) {
            CabinetCore.logOpt(type, "登陆" + (type == Admin ? "(管理员)" : "(操作员)"), "系统");
        }
        SharedPreferences sp = mContext.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Config.SYSPRE_Cabinet_User + type, GSON.toJson(user));
        editor.apply();
    }

    public static void clearCabinetUser(RoleType type) {
        SharedPreferences sp = mContext.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(Config.SYSPRE_Cabinet_User + type);
        editor.apply();
    }

    public static void clearCabinetInfo() {
        SharedPreferences sp = mContext.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(Config.SYSPRE_CABINET_INFO);
        editor.apply();
    }

    public static boolean validateCabinetUser(String input, RoleType type) {
        if (input == null) {
            return false;
        }
//        String inputMd5 = Util.md5(input);
        User user = getCabinetUser(type);
        return user != null && TextUtils.equals(input, user.password);
    }

    public static void init(CabinetApplication application) {
        mContext = application;
        _ACache = ACache.get(mContext);
    }

    public static ACache getACache() {
        return _ACache;
    }

    public synchronized static String getCameraVerifyCode(Context context, String sn, int channel) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return sp.getString(Config.SYSPRE_CAMERA_VERIFY_CODE + sn + channel, "");
    }

    public synchronized static void saveCameraVerifyCode(Context context, String sn, int channel, String code) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Config.SYSPRE_CAMERA_VERIFY_CODE + sn + channel, code == null ? "" : code);
        editor.apply();
    }

    public static void saveConnectedPrinterInfo(Context context, PrinterBluetoothInfo info) {
        String json = GSON.toJson(info);
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Config.PRINTER_BLUETOOTH_INFO, json);
        editor.apply();
    }

    public static void removeConnectedPrinterInfo(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(Config.PRINTER_BLUETOOTH_INFO);
        editor.apply();
    }

    public static int getCurrentScalesDevice() {
        SharedPreferences sp = mContext.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return sp.getInt(Config.SCALES_DEVICE, 0);
    }

    public static int getTDA09C485Config(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return sp.getInt(Config.TDA09C485_CONFIG, Config.DEFAULT_TDA09C485_ADDRESS);
    }

    public static PrinterBluetoothInfo getConnectedPrinterInfo(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        String info = sp.getString(Config.PRINTER_BLUETOOTH_INFO, null);
        if (info != null) {
            return GSON.fromJson(info, PrinterBluetoothInfo.class);
        } else {
            return null;
        }
    }

    /**
     * 检查管理员登录信息
     */
    public static void validateAdminUserInfo() {
        User user = CabinetCore.getCabinetUser(Admin);
        if (user == null) {
            LoginActivity.start(mContext, Admin);
        } else {
            validateCabinetInfo();
        }
    }

    /**
     * 检查管理员登录信息
     */
    public static void validateOperatorUserInfo() {
        User user = CabinetCore.getCabinetUser(RoleType.Operator);
        if (user == null) {
            LoginActivity.start(mContext, RoleType.Operator);
        } else {
            //startService(new Intent(this, MQTTService.class));
            mContext.startService(new Intent(mContext, HardwareService.class));
            startAuthTimer();
        }
    }

    public static void validateCabinetInfo() {
        Cabinet cabinet = CabinetCore.getCabinetInfo();
        if (cabinet == null) {
            CabinetBindActivity.start(mContext, null);
        } else {
            validateOperatorUserInfo();
        }
    }

    /**
     * 鉴权信息会过期，每半个小时重新登录一次
     */
    private static void startAuthTimer() {
        if (mAuthTimer == null) {
            mAuthTimer = new Timer();
            mAuthTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    loginValidate();
                }
            }, 20 * 1000, 30 * 60 * 1000);
        }
    }

    /**
     * 重启App
     */
    public static void restart() {
        new Thread(() -> {
            Util.sleep(500);
            Intent intent = new Intent(mContext, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            android.os.Process.killProcess(android.os.Process.myPid());
        }).start();
    }

    private static void loginValidate() {
        executorService.submit(() -> {
            User user = getCabinetUser(Admin);
            if (user != null) {
                APIJSON<User> userAPIJSON = RemoteAPI.System.login(user.username, user.password);
                if (userAPIJSON.status == APIJSON.Status.ok && userAPIJSON.data != null) {
                    userAPIJSON.data.password = user.password;
                    saveCabinetUser(userAPIJSON.data, Admin, false);
                } else if (userAPIJSON.status == APIJSON.Status.error) {
                    clearCabinetUser(Admin);
                    clearCabinetUser(RoleType.Operator);
                    LoginActivity.start(mContext, Admin);
                }
            }
            user = getCabinetUser(RoleType.Operator);
            if (user != null) {
                APIJSON<User> userAPIJSON = RemoteAPI.System.login(user.username, user.password);
                if (userAPIJSON.status == APIJSON.Status.ok && userAPIJSON.data != null) {
                    userAPIJSON.data.password = user.password;
                    saveCabinetUser(userAPIJSON.data, RoleType.Operator, false);
                } else if (userAPIJSON.status == APIJSON.Status.error) {
                    clearCabinetUser(RoleType.Operator);
                    LoginActivity.start(mContext, RoleType.Operator);
                }
            }
        });
    }

    public static boolean isDebugState() {
        return BuildConfig.DEBUG || TextUtils.equals(BuildConfig.BUILD_TYPE, "test");
    }


    public static DepositRecord getDepositRecord(String containerNo) {
        List<DepositRecord> depositRecordList = CabinetDatabase.getInstance().getDepositRecordList(CabinetDatabase.Filter.All, containerNo, false, Integer.MAX_VALUE, 0);
        for (DepositRecord depositRecord : depositRecordList) {
            if (TextUtils.equals(depositRecord.storage_no, containerNo)) {
                return depositRecord;
            }
        }
        return null;
    }

    public static List<DepositRecord> getUnSubmitDepositRecord() {
        return CabinetDatabase.getInstance().getDepositRecordList(CabinetDatabase.Filter.NoUpload, null, false, Integer.MAX_VALUE, 0);
    }

    public static void saveDepositRecord(DepositRecord depositRecord) {
        if (depositRecord.localId == -1) {
            CabinetDatabase.getInstance().deleteDepositRecord(depositRecord.localId);
        }
        DepositRecord record = getDepositRecord(depositRecord.storage_no);
        if (record != null) {
            CabinetDatabase.getInstance().deleteDepositRecord(record.localId);
        }
        CabinetDatabase.getInstance().addDepositRecord(depositRecord);
    }

    public static void deleteDepositRecord(long localId) {
        CabinetDatabase.getInstance().deleteDepositRecord(localId);
    }

    public static void logOpt(@NotNull RoleType roleType, @NotNull String opt, @NotNull String obj) {
        User user = getCabinetUser(roleType);
        String userName = user != null ? user.name : "Unknown User";
        String time = _SecondFormatter.format(new Date(System.currentTimeMillis()));
        OptLog optLog = new OptLog(userName, obj, opt, time);
        CabinetDatabase.getInstance().addOptLog(optLog);
    }

    public static void logOpt(@NotNull String opt, @NotNull String obj) {
        String userName = "服务端用户";
        String time = _SecondFormatter.format(new Date(System.currentTimeMillis()));
        OptLog optLog = new OptLog(userName, obj, opt, time);
        CabinetDatabase.getInstance().addOptLog(optLog);
    }

    public static void logAlert(String alertEvent) {
        AlertLog alertLog = new AlertLog();
        alertLog.alert = alertEvent;
        alertLog.time = _SecondFormatter.format(new Date(System.currentTimeMillis()));
        CabinetDatabase.getInstance().addAlertLog(alertLog);
    }

    public static void saveSentFailHardwareValueList(@NotNull List<HardwareValue> hardwareValueList) {
        for (HardwareValue hardwareValue : hardwareValueList) {
            if (hardwareValue.id == -1) {
                CabinetDatabase.getInstance().addHardwareValue(hardwareValue);
            }
        }
    }

    public static List<HardwareValue> getUnSentHardwareValueList() {
        return CabinetDatabase.getInstance().getHardwareValueList(CabinetDatabase.Filter.NoUpload, false, 20, 0);
    }

    public static void setHardwareValueSent(@NotNull List<HardwareValue> hardwareValueList) {
        List<String> ids = new LinkedList<>();
        for (HardwareValue value : hardwareValueList) {
            if (value.id != -1) {
                ids.add(String.valueOf(value.id));
            }
        }
        CabinetDatabase.getInstance().setDataSent(CabinetDatabase.Table.HardwareValue, ids);
    }

    public static String getDoorAccessMacAddress() {
        try {
            SharedPreferences sp = mContext.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
            return sp.getString(Config.DOOR_ACCESS_MAC_ADDRESS, "1E:95:10:BC:6E:58");
        } catch (Exception e) {
            return null;
        }
    }

    public static void saveDoorAccessMacAddress(String address) {
        SharedPreferences sp = mContext.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Config.DOOR_ACCESS_MAC_ADDRESS, address);
        editor.apply();
    }

    public static long getAuthTimeOut() {
        try {
            SharedPreferences sp = mContext.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
            return sp.getLong(Config.AUTH_TIME_OUT, 60 * 1000 * 10);
        } catch (Exception e) {
            return 60 * 1000 * 10;
        }
    }

    public static void saveAuthTimeOut(long timeOut) {
        SharedPreferences sp = mContext.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(Config.AUTH_TIME_OUT, timeOut);
        editor.apply();
    }

    public enum RoleType {
        Admin, Operator
    }

    interface CheckARCActiveListener {
        void onCheckARCActiveFailure(String info, int code);

        void onCheckARCActiveSuccess();
    }

}
