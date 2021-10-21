package me.liuzs.cabinetmanager;

import static me.liuzs.cabinetmanager.ui.NewProgressDialog.THEME_CIRCLE_PROGRESS;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import me.liuxy.cabinet.SubBoard;
import me.liuzs.cabinetmanager.ui.NewProgressDialog;

abstract class BaseActivity extends AppCompatActivity {

    public static final String DATE_TO_STRING_TIME_PATTERN = "HH:mm a";
    public static final String DATE_TO_STRING_YEAR_PATTERN = "yyyy-MM-dd";
    @SuppressLint("SimpleDateFormat")
    public static final SimpleDateFormat mYearFormat = new SimpleDateFormat(DATE_TO_STRING_YEAR_PATTERN);
    @SuppressLint("SimpleDateFormat")
    public static final SimpleDateFormat mTimeFormat = new SimpleDateFormat(DATE_TO_STRING_TIME_PATTERN);

    protected final Handler mHandler = new Handler();
    protected NewProgressDialog mProgressDialog;
    private Toast mToast;
    private TextView mTime, mDate;
    private Timer mTimer;
    private AuthListener mAuthListener;
    private final ActivityResultLauncher<Intent> mLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        int resultCode = result.getResultCode();
        if (resultCode == RESULT_OK) {
            Intent data = result.getData();
            assert data != null;
            CabinetCore.RoleType type = (CabinetCore.RoleType) data.getSerializableExtra(AuthActivity.KEY_AUTH_TYPE);
            mAuthListener.onAuthSuccess(type);
        } else if (resultCode == RESULT_CANCELED) {
            mAuthListener.onAuthCancel();
        }
    });

    public synchronized void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new NewProgressDialog(this, THEME_CIRCLE_PROGRESS);
        } else {
            mProgressDialog.dismiss();
        }
        mHandler.post(() -> mProgressDialog.show());
    }

    public synchronized void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mHandler.post(() -> mProgressDialog.dismiss());
        }
    }

    public synchronized void showToast(CharSequence info) {
        final CharSequence showInfo = info == null ? "" : info;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mToast == null) {
                    mToast = Toast.makeText(BaseActivity.this, showInfo, Toast.LENGTH_SHORT);
                    TextView v = mToast.getView().findViewById(android.R.id.message);
                    v.setPadding(10, 10, 10, 10);
                    v.setTextColor(Color.BLACK);
                    v.setTextSize(40);
                    v.setGravity(Gravity.CENTER);
                    mToast.setGravity(Gravity.CENTER, 0, 0);
                }
                mToast.setText(info);
                mToast.show();
            }
        });

    }

    public void showToast(int infoId) {
        showToast(getText(infoId));
    }

    /**
     * 权限检查
     *
     * @param neededPermissions 需要的权限
     * @return 是否全部被允许
     */
    protected boolean checkPermissions(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(this, neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isAllGranted = true;
        for (int grantResult : grantResults) {
            isAllGranted &= (grantResult == PackageManager.PERMISSION_GRANTED);
        }
        afterRequestPermission(requestCode, isAllGranted);
    }

    abstract void afterRequestPermission(int requestCode, boolean isAllGranted);

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(getClass().getSimpleName(), "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(getClass().getSimpleName(), "onPause");
    }

    protected void updateDateAndTime() {
        Date date = new Date(System.currentTimeMillis());
        if (mDate != null) {
            mDate.setText(mYearFormat.format(date));
        }
        if (mTime != null) {
            mTime.setText(mTimeFormat.format(date));
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(getClass().getSimpleName(), "OnStart");
        mDate = findViewById(R.id.toolbar_date);
        mTime = findViewById(R.id.toolbar_time);
        updateDateAndTime();
        try {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mHandler.post(() -> updateDateAndTime());
                }
            }, 1000, 5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(getClass().getSimpleName(), "OnStop");
        try {
            if (mTimer != null) {
                mTimer.cancel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("SameParameterValue")
    protected void showAuthActivity(CabinetCore.RoleType type, AuthListener lsn) {
        Intent intent = new Intent(BaseActivity.this, AuthActivity.class);
        intent.putExtra(AuthActivity.KEY_AUTH_TYPE, type);
        mAuthListener = lsn;
        mLauncher.launch(intent);
    }

    public void hideInputMethod() {
        mHandler.postDelayed(() -> {
            InputMethodManager manager = ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE));
            if (manager != null && getCurrentFocus() != null) {
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        }, 0);

    }

    public void showToast(SubBoard.ControlResult result) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        if (result == null) {
            return;
        }
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(result.result);
            sb.append("\r\n").append(Arrays.toString(result.cmd));
            if (result.response != null) {
                int bSize = Math.min(result.response.length, result.size);
                byte[] tb = new byte[bSize];
                System.arraycopy(result.response, 0, tb, 0, bSize);
                sb.append("\r\n").append(Arrays.toString(tb));
            }
            showToast(sb.toString());
        } catch (Exception ignored) {
        }
    }

    public interface AuthListener {
        void onAuthCancel();

        void onAuthSuccess(CabinetCore.RoleType roleType);
    }
}
