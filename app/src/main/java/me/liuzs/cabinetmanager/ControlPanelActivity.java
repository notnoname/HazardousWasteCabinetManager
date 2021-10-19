package me.liuzs.cabinetmanager;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.kyleduo.switchbutton.SwitchButton;

import java.util.Objects;

import me.liuzs.cabinetmanager.util.Util;

/**
 * 控制面板
 */
public class ControlPanelActivity extends BaseActivity {

    public static final String TAG = "ControlPanelActivity";

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {
    }

    private TextView mFanWorkModelValue, mSoundLightAlertValue, mFanPowerValue, mOxygenValue, mACCtrlModelValue, mACWorkModelValue, mACPowerValue, mRemoteCtrlModelValue;
    private Button mFCReset;
    private SwitchButton mFanWorkModel, mSoundLightAlert, mFanPower, mOxygen, mACCtrlModel, mACPower;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_panel);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(null);
        Util.fullScreen(this);
        ((TextView) findViewById(R.id.toolbar_title)).setText(R.string.control_panel);
        mFanWorkModelValue = findViewById(R.id.tvFanWorkModelValue);

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

    public void onFCResetButtonClick(View view) {
        finish();
    }

    public void onACWorkModelButtonClick(View view) {
        finish();
    }

    public void onRemoteCtrlWorkModelButtonClick(View view) {
        finish();
    }
}
