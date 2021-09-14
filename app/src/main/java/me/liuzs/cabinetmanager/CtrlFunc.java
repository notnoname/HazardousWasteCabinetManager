package me.liuzs.cabinetmanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.arcsoft.face.ActiveFileInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.enums.RuntimeABI;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import me.liuzs.cabinetmanager.model.CabinetInfo;
import me.liuzs.cabinetmanager.model.DepositRecord;
import me.liuzs.cabinetmanager.model.DeviceInfo;
import me.liuzs.cabinetmanager.model.SetupValue;
import me.liuzs.cabinetmanager.model.TakeOutInfo;
import me.liuzs.cabinetmanager.model.UsageInfo;
import me.liuzs.cabinetmanager.model.UserInfo;
import me.liuzs.cabinetmanager.printer.PrinterBluetoothInfo;
import me.liuzs.cabinetmanager.util.Util;

public class CtrlFunc {

    private static final String TAG = "CtrlFunc";
    private final static Gson mGson = new Gson();

    public static void checkARCActive(Context context, CheckARCActiveListener lsn) {
        Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
            try {
                RuntimeABI runtimeABI = FaceEngine.getRuntimeABI();
                Log.i(TAG, "subscribe: getRuntimeABI() " + runtimeABI);
                long start = System.currentTimeMillis();
                int activeCode = FaceEngine.activeOnline(context, Config.ARC_APP_ID, Config.ARC_SDK_KEY);
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
                    Toast.makeText(context, R.string.active_success, Toast.LENGTH_SHORT).show();
                    lsn.onCheckARCActiveSuccess();
                } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                    lsn.onCheckARCActiveSuccess();
                } else {
                    Toast.makeText(context, context.getString(R.string.active_failed, activeCode), Toast.LENGTH_SHORT).show();
                    lsn.onCheckARCActiveFailure(context.getString(R.string.active_failed, activeCode), activeCode);
                }
                ActiveFileInfo activeFileInfo = new ActiveFileInfo();
                int res = FaceEngine.getActiveFileInfo(context, activeFileInfo);
                if (res == ErrorInfo.MOK) {
                    Log.i(TAG, activeFileInfo.toString());
                }
            }

            @Override
            public void onError(@NotNull Throwable e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                lsn.onCheckARCActiveFailure(e.getMessage(), -1);
            }

            @Override
            public void onComplete() {
            }
        });
    }

    public static UserInfo getAdministratorInfo(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        String admin1 = sp.getString(Config.SYSPRE_Administrator1_Name, null);
        String admin1Id = sp.getString(Config.SYSPRE_Administrator1_ID, null);
        String admin1UserInfo = sp.getString(Config.SYSPRE_Administrator1_USER_INFO, null);
        String admin1Face = sp.getString(Config.SYSPRE_Administrator1_FACE_ID, null);
        String admin1PasswordMD5 = sp.getString(Config.SYSPRE_Administrator1_PASSWORD_MD5, null);
        String admin1Token = sp.getString(Config.SYSPRE_Administrator1_TOKEN, null);
        if (TextUtils.isEmpty(admin1) || TextUtils.isEmpty(admin1Id) || TextUtils.isEmpty(admin1UserInfo) || TextUtils.isEmpty(admin1Face) || TextUtils.isEmpty(admin1PasswordMD5) || TextUtils.isEmpty(admin1Token)) {
            return null;
        } else {
            UserInfo result = mGson.fromJson(admin1UserInfo, UserInfo.class);
            result.token = admin1Token;
            return result;
        }
    }

    public static CabinetInfo getCabinetInfo(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        String cabinetInfo = sp.getString(Config.SYSPRE_CABINET_INFO, null);
        if (TextUtils.isEmpty(cabinetInfo)) {
            return null;
        } else {
            return mGson.fromJson(cabinetInfo, CabinetInfo.class);
        }
    }

    public static void saveCabinetInfo(Context context, CabinetInfo info) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (info == null) {
            editor.remove(Config.SYSPRE_CABINET_INFO);
        } else {
            editor.putString(Config.SYSPRE_CABINET_INFO, mGson.toJson(info));
        }
        editor.apply();
    }

    /**
     * 全量保存
     *
     * @param context     上下文
     * @param name        用户名
     * @param id          用户id
     * @param passwordMD5 MD5后的用户密码
     * @param token       token
     * @param userInfo    用户信息
     * @param faceId      FaceId名
     */
    public static void saveAdmin1(Context context, String name, String id, String passwordMD5, String token, UserInfo userInfo, String faceId) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Config.SYSPRE_Administrator1_Name, name);
        editor.putString(Config.SYSPRE_Administrator1_ID, id);
        editor.putString(Config.SYSPRE_Administrator1_PASSWORD_MD5, passwordMD5);
        editor.putString(Config.SYSPRE_Administrator1_TOKEN, token);
        editor.putString(Config.SYSPRE_Administrator1_USER_INFO, mGson.toJson(userInfo));
        editor.putString(Config.SYSPRE_Administrator1_FACE_ID, faceId);
        editor.apply();
    }

    /**
     * 保存部分信息
     *
     * @param context  上下文
     * @param token    token
     * @param userInfo 用户信息
     */
    public static void saveAdmin1(Context context, String token, UserInfo userInfo) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Config.SYSPRE_Administrator1_TOKEN, token);
        editor.putString(Config.SYSPRE_Administrator1_USER_INFO, mGson.toJson(userInfo));
        editor.apply();
    }

    public static void removeAdminUserInfo(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(Config.SYSPRE_Administrator1_Name);
        editor.remove(Config.SYSPRE_Administrator1_ID);
        editor.remove(Config.SYSPRE_Administrator1_PASSWORD_MD5);
        editor.remove(Config.SYSPRE_Administrator1_TOKEN);
        editor.remove(Config.SYSPRE_Administrator1_USER_INFO);
        editor.remove(Config.SYSPRE_Administrator1_FACE_ID);
        editor.apply();
    }

    public static void removeCabinetInfo(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(Config.SYSPRE_CABINET_INFO);
        editor.apply();
    }

    public static String getAdminUserID(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return sp.getString(Config.SYSPRE_Administrator1_ID, null);
    }

    public static String getAdminPasswordMD5(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return sp.getString(Config.SYSPRE_Administrator1_PASSWORD_MD5, null);
    }

    public static String checkAdminUser(Context context, String password) {
        if (password == null) {
            return null;
        }
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        String id = sp.getString(Config.SYSPRE_Administrator1_ID, null);
        String md5 = sp.getString(Config.SYSPRE_Administrator1_PASSWORD_MD5, null);
        String inputMd5 = Util.md5(password);
        if (inputMd5.equals(md5)) {
            return id;
        } else {
            return null;
        }
    }

    public static void intSystemSetup(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        String option = sp.getString(Config.SYSPRE_SYSTEM_SETUP, null);
        if (option == null) {
            SetupValue sv = new SetupValue();
            sv.fanAuto = true;
            sv.fanStopTime = Config.DEFAULT_FAN_STOP_TIME;
            sv.fanWorkTime = Config.DEFAULT_FAN_WORK_TIME;
            sv.thresholdTemp = Config.DEFAULT_FAN_TEMP_THRESHOLD;
            sv.thresholdPPM = Config.DEFAULT_FAN_PPM_THRESHOLD;
            String optionStr = mGson.toJson(sv);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(Config.SYSPRE_SYSTEM_SETUP, optionStr);
            editor.apply();
        }
    }

    public synchronized static SetupValue getSetupValue(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        String option = sp.getString(Config.SYSPRE_SYSTEM_SETUP, null);
        if (option == null) {
            return null;
        } else {
            return mGson.fromJson(option, SetupValue.class);
        }
    }

    public synchronized static void saveSetupValue(Context context, SetupValue value) {
        if (value == null) {
            return;
        }
        String os = mGson.toJson(value);
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Config.SYSPRE_SYSTEM_SETUP, os);
        editor.apply();
    }

    public synchronized static String getCameraVerifyCode(Context context, String sn, int channel) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        String option = sp.getString(Config.SYSPRE_CAMERA_VERIFY_CODE + sn + channel, "");
        return option;
    }

    public synchronized static void saveCameraVerifyCode(Context context, String sn, int channel, String code) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Config.SYSPRE_CAMERA_VERIFY_CODE + sn + channel, code == null ? "" : code);
        editor.apply();
    }

    public static DepositRecord getUnSubmitDepositRecord(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        String info = sp.getString(Config.UN_SUBMIT_DEPOSIT_INFO, null);
        if (info != null) {
            return mGson.fromJson(info, DepositRecord.class);
        } else {
            return null;
        }
    }

    public static UsageInfo getUnSubmitUsageInfo(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        String info = sp.getString(Config.UN_SUBMIT_USAGE_INFO, null);
        if (info != null) {
            return mGson.fromJson(info, UsageInfo.class);
        } else {
            return null;
        }
    }

    public static TakeOutInfo getUnSubmitTakeOutInfo(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        String info = sp.getString(Config.UN_SUBMIT_TAKE_OUT_INFO, null);
        if (info != null) {
            return mGson.fromJson(info, TakeOutInfo.class);
        } else {
            return null;
        }
    }

    public static void saveUnSubmitDepositRecord(Context context, DepositRecord record) {
        String json = mGson.toJson(record);
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Config.UN_SUBMIT_DEPOSIT_INFO, json);
        editor.apply();
    }

    public static void saveUnSubmitTakeOutInfo(Context context, TakeOutInfo info) {
        String json = mGson.toJson(info);
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Config.UN_SUBMIT_TAKE_OUT_INFO, json);
        editor.apply();
    }

    public static void saveUnSubmitUsageInfo(Context context, UsageInfo info) {
        String json = mGson.toJson(info);
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Config.UN_SUBMIT_USAGE_INFO, json);
        editor.apply();
    }

    public static void removeUnSubmitDepositRecord(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(Config.UN_SUBMIT_DEPOSIT_INFO);
        editor.apply();
    }

    public static void removeUnSubmitUsageInfo(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(Config.UN_SUBMIT_USAGE_INFO);
        editor.apply();
    }

    public static void removeUnSubmitTakeOutInfo(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(Config.UN_SUBMIT_TAKE_OUT_INFO);
        editor.apply();
    }


    public static void saveConnectedPrinterInfo(Context context, PrinterBluetoothInfo info) {
        String json = mGson.toJson(info);
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

    public static int getMainTVOCModelCount(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        int info = sp.getInt(Config.MAIN_TVOC_COUNT, 1);
        return info;
    }

    public static void saveMainTVOCModelCount(Context context, int count) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(Config.MAIN_TVOC_COUNT, count);
        editor.apply();
    }

    public static int[] getSubBoardPeriod(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        String periodStr = sp.getString(Config.SUB_BOARD_PERIOD, "50,10,2000");
        String[] period = periodStr.split(",");
        int[] result = new int[3];
        result[0] = Integer.parseInt(period[0]);
        result[1] = Integer.parseInt(period[1]);
        result[2] = Integer.parseInt(period[2]);
        return result;
    }

    public static void saveSubBoardPeriod(Context context, int subBoardPeriod, int lockDelay, int envPeriod) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Config.SUB_BOARD_PERIOD, subBoardPeriod + "," + lockDelay + "," + envPeriod);
        editor.apply();
    }

//    public static void saveSubBordConfig(Context context, int address) {
//        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sp.edit();
//        editor.putInt(Config.SUB_BOARD_CONFIG, address);
//        editor.apply();
//    }

//    public static int getSubBoardConfig(Context context) {
//        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
//        int info = sp.getInt(Config.SUB_BOARD_CONFIG, Config.DEFAULT_SUB_BOARD_ADDRESS);
//        return info;
//    }

    public static void saveTDA09C485Config(Context context, int address) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(Config.TDA09C485_CONFIG, address);
        editor.apply();
    }

    public static int getCurrentScalesDevice(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        int info = sp.getInt(Config.SCALES_DEVICE, 0);
        return info;
    }

    public static void saveCurrentScalesDevice(Context context, int deviceIndex) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(Config.SCALES_DEVICE, deviceIndex);
        editor.apply();
    }

    public static int getTDA09C485Config(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        int info = sp.getInt(Config.TDA09C485_CONFIG, Config.DEFAULT_TDA09C485_ADDRESS);
        return info;
    }

    public static PrinterBluetoothInfo getConnectedPrinterInfo(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        String info = sp.getString(Config.PRINTER_BLUETOOTH_INFO, null);
        if (info != null) {
            return mGson.fromJson(info, PrinterBluetoothInfo.class);
        } else {
            return null;
        }
    }

    public static boolean isInThisTank(CabinetInfo cabinetInfo, String devId) {
        if (cabinetInfo == null) {
            return false;
        }
        for (DeviceInfo info : cabinetInfo.devices) {
            if (TextUtils.equals(info.devId, devId)) {
                return true;
            }
        }
        return false;
    }

    public static int getDevIndex(CabinetInfo cabinetInfo, String devId) {
        if (cabinetInfo == null) {
            return -1;
        }
        int index = 0;
        for (DeviceInfo info : cabinetInfo.devices) {
            if (TextUtils.equals(info.devId, devId)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    interface CheckARCActiveListener {
        void onCheckARCActiveFailure(String info, int code);

        void onCheckARCActiveSuccess();
    }

}
