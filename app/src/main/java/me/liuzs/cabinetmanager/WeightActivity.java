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
import me.liuzs.cabinetmanager.util.Util;

public class WeightActivity extends AppCompatActivity implements Steelyard.SteelyardCallback {

    public static final String KEY_SELECT_VALUE = "KEY_SELECT_VALUE";
    public static final String TAG = "WeightActivity";
    private final Handler mHandler = new Handler();
    private TextView mWeight, mTip;
    private float mWeightValue;
    private Timer mTimer;
    private final TimerTask mWeightTask = new TimerTask() {
        @Override
        public void run() {
            float value = HardwareService.steelyardWeight();
            if (value != Float.MIN_VALUE) {
                WeightActivity.this.OnData(Util.getScaleFloat(value, 3));
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
        int index = CabinetCore.getCurrentScalesDevice();
        String scalesName = Config.ScalesDeviceName[index];
        mTip.setText("称重[" + scalesName + "]");
        mTimer = new Timer();
        mTimer.schedule(mWeightTask, 1000, 3000);
    }

    public void onOKButtonClick(View v) {
        Intent data = new Intent();
        data.putExtra(KEY_SELECT_VALUE, String.valueOf(mWeightValue));
        setResult(RESULT_OK, data);
        finish();
    }

    public void onClearButtonClick(View v) {
        HardwareService.steelyardClear();
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

    @SuppressLint("SetTextI18n")
    @Override
    public void OnData(float v) {
        mWeightValue = v;
        try {
            mHandler.post(() -> mWeight.setText(mWeightValue + "kg"));
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