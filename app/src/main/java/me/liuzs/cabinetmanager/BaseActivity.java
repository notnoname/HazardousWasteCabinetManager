package me.liuzs.cabinetmanager;

import static me.liuzs.cabinetmanager.ui.NewProgressDialog.THEME_CIRCLE_PROGRESS;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.liuzs.cabinetmanager.ui.NewProgressDialog;

abstract class BaseActivity extends AppCompatActivity {

    protected final Handler mHandler = new Handler();
    protected NewProgressDialog mProgressDialog;
    private ExecutorService executorService;
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
            if (type == CabinetCore.RoleType.Operator) {
                mPreAuthTime = System.currentTimeMillis();
            }
        } else if (resultCode == RESULT_CANCELED) {
            mAuthListener.onAuthCancel();
        }
    });

    private static long mPreAuthTime = 0;

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
        mHandler.post(() -> {
            if (mToast == null) {
                mToast = Toast.makeText(BaseActivity.this, showInfo, Toast.LENGTH_SHORT);
                LinearLayout layout = (LinearLayout) mToast.getView();
                layout.setBackgroundResource(R.drawable.background_toast);
                TextView v = mToast.getView().findViewById(android.R.id.message);
                v.setPadding(10, 10, 10, 10);
                v.setTextColor(Color.WHITE);
                v.setTextSize(TypedValue.COMPLEX_UNIT_PX, 30);
                v.setGravity(Gravity.CENTER);
                mToast.setGravity(Gravity.CENTER, 0, 0);
            }
            mToast.setText(info);
            mToast.show();
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
        } else {
            return super.onKeyDown(keyCode, event);
        }
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
            mDate.setText(CabinetCore._YearFormatter.format(date));
        }
        if (mTime != null) {
            mTime.setText(CabinetCore._HourFormatter.format(date));
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        executorService = Executors.newFixedThreadPool(1);
    }

    private void logOpt() {
        TextView titleView = findViewById(R.id.toolbar_title);
        if (titleView != null) {
            String title = titleView.getText().toString();
            if (!TextUtils.isEmpty(title) && !title.contains("登陆")) {
                if (title.contains("设置")) {
                    CabinetCore.logOpt(CabinetCore.RoleType.Admin, "打开", title);
                } else {
                    CabinetCore.logOpt(CabinetCore.RoleType.Operator, "打开", title);
                }
            }
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        logOpt();
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

    protected void showAuthActivity(CabinetCore.RoleType type, AuthListener lsn) {
        mAuthListener = lsn;
        if (type == CabinetCore.RoleType.Operator) {
            if (System.currentTimeMillis() - mPreAuthTime >= CabinetCore.getAuthTimeOut()) {
                Intent intent = new Intent(BaseActivity.this, AuthActivity.class);
                intent.putExtra(AuthActivity.KEY_AUTH_TYPE, type);
                mLauncher.launch(intent);
            } else {
                mAuthListener.onAuthSuccess(type);
            }
        } else {
            Intent intent = new Intent(BaseActivity.this, AuthActivity.class);
            intent.putExtra(AuthActivity.KEY_AUTH_TYPE, type);
            mLauncher.launch(intent);
        }
    }

    public void hideInputMethod() {
        mHandler.postDelayed(() -> {
            InputMethodManager manager = ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE));
            if (manager != null && getCurrentFocus() != null) {
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        }, 0);

    }

    @Override
    protected void onDestroy() {
        if (!executorService.isShutdown()) {
            executorService.shutdownNow();
        }
        super.onDestroy();
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        } else {
            //showSystemUI();
        }
    }

    protected void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    protected void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public interface AuthListener {
        void onAuthCancel();

        void onAuthSuccess(CabinetCore.RoleType roleType);
    }
}
