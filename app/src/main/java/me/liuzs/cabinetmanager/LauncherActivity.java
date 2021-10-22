package me.liuzs.cabinetmanager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import me.liuzs.cabinetmanager.util.Util;

public class LauncherActivity extends BaseActivity {

    private int mClickCount = 0;

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        ((TextView) findViewById(R.id.tvVersion)).setText("版本号:" + Util.getVersionName(this) + "(" + Util.getVersionCode(this) + ")");
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