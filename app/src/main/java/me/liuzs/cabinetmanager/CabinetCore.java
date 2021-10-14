package me.liuzs.cabinetmanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.arcsoft.face.ActiveFileInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.enums.RuntimeABI;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import me.liuzs.cabinetmanager.model.Cabinet;
import me.liuzs.cabinetmanager.model.DepositRecord;
import me.liuzs.cabinetmanager.model.TakeOutInfo;
import me.liuzs.cabinetmanager.model.UsageInfo;
import me.liuzs.cabinetmanager.model.User;
import me.liuzs.cabinetmanager.net.APIJSON;
import me.liuzs.cabinetmanager.net.RemoteAPI;
import me.liuzs.cabinetmanager.printer.PrinterBluetoothInfo;
import me.liuzs.cabinetmanager.service.HardwareService;
import me.liuzs.cabinetmanager.util.Util;

public class CabinetCore {
    public final static Gson GSON = new Gson();
    private static final String TAG = "CabinetCore";
    private static Timer mAuthTimer;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    /**
     * 启动系统各项配置的校验，从ARC库开始。
     */
    public static void startValidateSystem() {
//检查是人像识别库是否全
        if (!Config.isLibraryExists(mContext)) {
//            showToast(mContext.getString(R.string.library_not_found));
            return;
        }
        validateARCActive(new CabinetCore.CheckARCActiveListener() {
            @Override
            public void onCheckARCActiveFailure(String message, int code) {
                if (BuildConfig.DEBUG) {
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

    public static void saveCabinetUser(User user, RoleType type) {
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
        String inputMd5 = Util.md5(input);
        User user = getCabinetUser(type);
        return user != null && inputMd5.equals(user.password);
    }

    public static void init(CabinetApplication application) {
        mContext = application;
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

    public static DepositRecord getUnSubmitDepositRecord(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        String info = sp.getString(Config.UN_SUBMIT_DEPOSIT_INFO, null);
        if (info != null) {
            return GSON.fromJson(info, DepositRecord.class);
        } else {
            return null;
        }
    }

    public static UsageInfo getUnSubmitUsageInfo(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        String info = sp.getString(Config.UN_SUBMIT_USAGE_INFO, null);
        if (info != null) {
            return GSON.fromJson(info, UsageInfo.class);
        } else {
            return null;
        }
    }

    public static TakeOutInfo getUnSubmitTakeOutInfo(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        String info = sp.getString(Config.UN_SUBMIT_TAKE_OUT_INFO, null);
        if (info != null) {
            return GSON.fromJson(info, TakeOutInfo.class);
        } else {
            return null;
        }
    }

    public static void saveUnSubmitDepositRecord(Context context, DepositRecord record) {
        String json = GSON.toJson(record);
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Config.UN_SUBMIT_DEPOSIT_INFO, json);
        editor.apply();
    }

    public static void saveUnSubmitTakeOutInfo(Context context, TakeOutInfo info) {
        String json = GSON.toJson(info);
        SharedPreferences sp = context.getSharedPreferences(Config.SYSTEM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Config.UN_SUBMIT_TAKE_OUT_INFO, json);
        editor.apply();
    }

    public static void saveUnSubmitUsageInfo(Context context, UsageInfo info) {
        String json = GSON.toJson(info);
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
        User user = CabinetCore.getCabinetUser(RoleType.Admin);
        if (user == null) {
            LoginActivity.start(mContext, RoleType.Admin);
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
//                    new LoginTask(AuthType.Admin).execute(
//                            mOperator.id, mOperator.passwordMD5);
                }
            }, 10 * 1000, 30 * 60 * 1000);
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

    public enum RoleType {
        Admin, Operator
    }

    interface CheckARCActiveListener {
        void onCheckARCActiveFailure(String info, int code);

        void onCheckARCActiveSuccess();
    }

    private static class LoginTask extends AsyncTask<String, Void, APIJSON<User>> {

        private final RoleType type;

        public LoginTask(RoleType type) {
            this.type = type;
        }

        @Override
        protected APIJSON<User> doInBackground(String... strings) {
            return RemoteAPI.System.login(strings[0], strings[1]);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(APIJSON<User> json) {
            super.onPostExecute(json);
            if (json.status == APIJSON.Status.ok) {
                User user = json.data;
                User oUser = getCabinetUser(type);
                assert oUser != null;
                user.faceId = oUser.faceId;
                CabinetCore.saveCabinetUser(user, type);
            } else if (json.status == APIJSON.Status.error) {
                CabinetCore.clearCabinetUser(type);
                LoginActivity.start(mContext, type);
            }
        }
    }

}
