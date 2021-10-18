package me.liuzs.cabinetmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.LinkedList;
import java.util.List;

import me.liuxy.cabinet.SubBoard;
import me.liuzs.cabinetmanager.model.Cabinet;
import me.liuzs.cabinetmanager.model.HardwareValue;
import me.liuzs.cabinetmanager.model.LockerStatus;
import me.liuzs.cabinetmanager.util.Util;

/**
 * 控制面板
 */
public class ControlPanelActivity extends BaseActivity {

    public static final String TAG = "ControlPanelActivity";

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_panel);
        Util.fullScreen(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void onBackButtonClick(View view) {
        finish();
    }
}
