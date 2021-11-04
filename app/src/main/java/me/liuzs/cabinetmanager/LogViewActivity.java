package me.liuzs.cabinetmanager;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.Date;

import me.liuzs.cabinetmanager.model.HardwareValue;

public class LogViewActivity extends BaseActivity {

    public static final String TAG = "LogViewActivity";
    private final Gson mGson = new GsonBuilder().setPrettyPrinting().create();
    private TextView mLog;
    @SuppressWarnings("unused")
    private ScrollView mScrollView;

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {

    }

    public enum Type {
        Opt, Alert
    }

    public static void start(Context context, Type type) {

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_view);
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
                CabinetCore._YearFormatter.format(new Date()) +
                "\t" +
                CabinetCore._HourFormatter.format(new Date()) +
                "\n" +
                mGson.toJson(value);
        mLog.setText(newInfo);
    }
}