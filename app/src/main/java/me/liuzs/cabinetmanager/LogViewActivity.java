package me.liuzs.cabinetmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.Date;
import java.util.Objects;

import me.liuzs.cabinetmanager.model.HardwareValue;
import me.liuzs.cabinetmanager.util.Util;

public class LogViewActivity extends BaseActivity {

    public static final String TAG = "LogViewActivity";
    private final Gson mGson = new GsonBuilder().setPrettyPrinting().create();
    private TextView mLog;
    @SuppressWarnings("unused")
    private ScrollView mScrollView;
    private HardwareValueBroadcastReceiver mHardwareValueBroadcastReceiver;

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        mHardwareValueBroadcastReceiver = new HardwareValueBroadcastReceiver();
        IntentFilter filter = new IntentFilter(Config.ACTION_HARDWARE_VALUE_SEND);
        registerReceiver(mHardwareValueBroadcastReceiver, filter);
        Log.d(TAG, "RegisterReceiver");
    }

    @Override
    protected void onStop() {
        unregisterReceiver(mHardwareValueBroadcastReceiver);
        Log.d(TAG, "UnregisterReceiver");
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(null);
        Util.fullScreen(this);

        mScrollView = findViewById(R.id.sv);
        mLog = findViewById(R.id.tvLog);
        startShowInfo();
    }

    private void startShowInfo() {
        File file = new File("/dev");
        StringBuilder sb = new StringBuilder();
        sb.append("/dev file list:");
        sb.append("\n");
        if (file.isDirectory()) {
            String[] subFileNames = file.list();
            if (subFileNames != null) {
                for (String subFileName : subFileNames) {
                    sb.append(subFileName);
                    sb.append("\t\t");
                }
            }
        }
        sb.append("\n");
        sb.append("-------------------");
        sb.append("\n");
        if (CabinetCore.getCabinetInfo() != null) {
            sb.append("Cabinet Info:");
            sb.append("\n");
            sb.append(mGson.toJson(CabinetCore.getCabinetInfo()));
            sb.append("\n");
        }
        mLog.setText(sb.toString());
    }

    public void onBackButtonClick(View view) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void showHardwareValue(HardwareValue value) {
        CharSequence info = mLog.getText();
        String newInfo = info +
                "\n" +
                mYearFormat.format(new Date()) +
                "\t" +
                mTimeFormat.format(new Date()) +
                "\n" +
                mGson.toJson(value);
        mLog.setText(newInfo);
//        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }

    private class HardwareValueBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "OnReceiver");
            String json = intent.getStringExtra(Config.KEY_HARDWARE_VALUE);
            HardwareValue value = mGson.fromJson(json, HardwareValue.class);
            mHandler.post(() -> showHardwareValue(value));
        }
    }
}