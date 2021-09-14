package me.liuzs.cabinetmanager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

import me.liuxy.cabinet.Steelyard;
import me.liuzs.cabinetmanager.service.HardwareService;

public class WeightActivity extends AppCompatActivity implements Steelyard.SteelyardCallback {

    public static final String KEY_SELECT_VALUE = "KEY_SELECT_VALUE";
    public static final String TAG = "WeightActivity";
    private final Handler mHandler = new Handler();
    private TextView mWeight, mTip;
    private Timer mTimer;
    private final TimerTask mWeightTask = new TimerTask() {
        @Override
        public void run() {
            float value = HardwareService.getWeight();
            if (value != Float.MIN_VALUE) {
                WeightActivity.this.OnData(value);
            } else {
                WeightActivity.this.OnError(Steelyard.ErrorCode.ERROR_DEVICE_CANNOT_OPEN);
            }
        }
    };

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setContentView(R.layout.activity_weight);
        mWeight = findViewById(R.id.tvWeightValue);
        mTip = findViewById(R.id.tvTip);
        int index = CtrlFunc.getCurrentScalesDevice(this);
        String scalesName = Config.ScalesDeviceName[index];
        mTip.setText("称重[" + scalesName + "]");
        if (index == 0) {
            HardwareService.weight(this);
        } else if (index == 1) {
            mTimer = new Timer();
            mTimer.schedule(mWeightTask, 1000, 3000);
        }
    }

    public void onOKButtonClick(View v) {
        Intent data = new Intent();
        String s = mWeight.getText().toString();
        try {
            float weight = Float.parseFloat(s);
            data.putExtra(KEY_SELECT_VALUE, String.valueOf(weight));
            setResult(RESULT_OK, data);
            finish();
        } catch (Exception ignored) {

        }
    }

    @Override
    protected void onDestroy() {
        HardwareService.weight(null);
        try {
            if (mTimer != null) {
                mTimer.cancel();
            }
        } catch (Exception ignored) {

        }
        super.onDestroy();
    }

    public void onCancelButtonClick(View v) {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void OnData(float v) {
        try {
            mHandler.post(() -> mWeight.setText(String.valueOf(v)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void OnError(Steelyard.ErrorCode errorCode) {
        try {
            mHandler.post(() -> mWeight.setText("ErrorCode:" + errorCode));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}