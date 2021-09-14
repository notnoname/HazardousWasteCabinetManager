package me.liuzs.cabinetmanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import me.liuzs.cabinetmanager.util.Util;

public class LauncherActivity extends BaseActivity {

    private int mClickCount = 0;

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_launcher);
        Util.fullScreen(this);
        ((TextView) findViewById(R.id.tvVersion)).setText("Version:" + Util.getVersionName(this) + "(" + Util.getVersionCode(this) + ")");
        mHandler.postDelayed(() -> {
            Intent intent = new Intent(LauncherActivity.this, MainActivity.class);
            startActivity(intent);
        }, 2000);
    }

    public void onVersionClick(View view) {
        mClickCount++;
        if (mClickCount > 4) {
            showToast("将不启动硬件驱动！");
        }
        CabinetApplication.getInstance().setInitHardwareManager(false);
    }
}