package me.liuzs.cabinetmanager.ui.setting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.kyleduo.switchbutton.SwitchButton;

import java.text.DecimalFormat;

import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.SystemSettingActivity;
import me.liuzs.cabinetmanager.model.modbus.SetupValue;
import me.liuzs.cabinetmanager.service.ModbusService;

/**
 * 参数设置
 */
public class EquipmentManageFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    public static final String TAG = "EquipmentManageFragment";
    private final DecimalFormat mDecimalFormat = new DecimalFormat("0.00");
    private SystemSettingActivity mActivity;
    private SetupValue mValue;
    private SwitchButton mAlertVOC, mAlertFG, mAlertTempHigh, mAlertTempLow, mAlertHumidityHigh, mAlertHumidityLow, mAlertSoundLight;
    private EditText mFanRunTimeValue, mFanStopTimeValue, mFanFrequencyValue, mUnionVOCHigh, mUnionVOCLow, mAlertVOCValue, mAlertFGValue, mAlertTempHighValue, mAlertTempLowValue, mAlertHumidityHighValue, mAlertHumidityLowValue;
    private Button mSave;

    public EquipmentManageFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_equipment_manage, container, false);

        mFanRunTimeValue = view.findViewById(R.id.etFanRunTimeValue);
        mFanStopTimeValue = view.findViewById(R.id.etFanStopTimeValue);
        mFanFrequencyValue = view.findViewById(R.id.etFanFrequencyValue);

        mUnionVOCHigh = view.findViewById(R.id.etFanHighVOCValue);
        mUnionVOCLow = view.findViewById(R.id.etFanLowVOCValue);

        mAlertVOC = view.findViewById(R.id.swAlertVOC);
        mAlertVOCValue = view.findViewById(R.id.etAlertVOCValue);

        mAlertFG = view.findViewById(R.id.swAlertFG);
        mAlertFGValue = view.findViewById(R.id.etAlertFGValue);

        mAlertTempHigh = view.findViewById(R.id.swAlertTemHigh);
        mAlertTempHighValue = view.findViewById(R.id.etAlertTempHighValue);
        mAlertTempLow = view.findViewById(R.id.swAlertTempLow);
        mAlertTempLowValue = view.findViewById(R.id.etAlertTempLowValue);

        mAlertHumidityHigh = view.findViewById(R.id.swAlertHumidityHigh);
        mAlertHumidityHighValue = view.findViewById(R.id.etAlertHumidityHighValue);
        mAlertHumidityLow = view.findViewById(R.id.swAlertHumidityLow);
        mAlertHumidityLowValue = view.findViewById(R.id.etAlertHumidityLowValue);

        mAlertSoundLight = view.findViewById(R.id.swAlertSoundLight);


        mSave = view.findViewById(R.id.btnSave);
        mSave.setOnClickListener(this);
        mAlertVOC.setOnCheckedChangeListener(this);
        mAlertTempHigh.setOnCheckedChangeListener(this);
        mAlertTempLow.setOnCheckedChangeListener(this);
        mAlertHumidityHigh.setOnCheckedChangeListener(this);
        mAlertHumidityLow.setOnCheckedChangeListener(this);
        mActivity = (SystemSettingActivity) getActivity();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        new Thread(() -> {
            mValue = ModbusService.readSetupValue();
            mActivity.runOnUiThread(() -> showValue());
        }).start();

    }

    private void showValue() {
        if (mValue.e != null) {
            mActivity.showToast(mValue.e.toString());
            return;
        }

        mFanRunTimeValue.setText(String.valueOf(mValue.fanUnionWorkTime));
        mFanStopTimeValue.setText(String.valueOf(mValue.fanUnionStopTime));
        mFanFrequencyValue.setText(String.valueOf(mValue.fanUnionFrequency));
        mUnionVOCHigh.setText(String.valueOf(mValue.vocUnionMax));
        mUnionVOCLow.setText(String.valueOf(mValue.vocUnionMin));


        mAlertVOC.setChecked(mValue.vocAlertAuto);
        mAlertVOCValue.setEnabled(mValue.vocAlertAuto);
        mAlertVOCValue.setText(String.valueOf(mValue.vocAlertAutoThreshold));

        mAlertFGValue.setText(String.valueOf(mValue.fgAlertThreshold));

        mAlertTempHigh.setChecked(mValue.tempHighAlertAuto);
        mAlertTempHighValue.setEnabled(mValue.tempHighAlertAuto);
        mAlertTempHighValue.setText(String.valueOf(mValue.tempHighAlertThreshold));


        mAlertTempLow.setChecked(mValue.tempLowAlertAuto);
        mAlertTempLowValue.setEnabled(mValue.tempLowAlertAuto);
        mAlertTempLowValue.setText(String.valueOf(mValue.tempLowAlertThreshold));

        mAlertHumidityHigh.setChecked(mValue.humidityHighAlertAuto);
        mAlertHumidityHighValue.setEnabled(mValue.humidityHighAlertAuto);
        mAlertHumidityHighValue.setText(String.valueOf(mValue.tempHighAlertThreshold));


        mAlertHumidityLow.setChecked(mValue.humidityLowAlertAuto);
        mAlertHumidityLowValue.setEnabled(mValue.humidityLowAlertAuto);
        mAlertHumidityLowValue.setText(String.valueOf(mValue.tempLowAlertThreshold));

        mAlertSoundLight.setChecked(mValue.alertSoundLight);

    }

    @Override
    public void onClick(View v) {
        if (v == mSave) {
            SetupValue newValue = checkValue();
            if (newValue != null) {
                mSave.setEnabled(false);
                mValue = newValue;
                showValue();
                mActivity.showToast("设置保存成功!");
                mSave.setEnabled(true);
            }
        }
    }

    private SetupValue checkValue() {
        SetupValue result = null;
        try {
            String work = mFanRunTimeValue.getEditableText().toString();
            String stop = mFanStopTimeValue.getEditableText().toString();
            String temp = mAlertTempHighValue.getEditableText().toString();
            String ppm = mUnionVOCHigh.getEditableText().toString();
            int workTime = Integer.parseInt(work);
            int stopTime = Integer.parseInt(stop);
            int tempValue = Integer.parseInt(temp);
            float ppmValue = Float.parseFloat(ppm);
            result = new SetupValue();
            result.fanUnionWorkTime = workTime;
            result.fanUnionStopTime = stopTime;

        } catch (Exception e) {
            e.printStackTrace();
            mActivity.showToast("输入有误，请检查！");
        }
        return result;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == mAlertVOC) {
            mValue.vocAlertAuto = isChecked;
        } else if (buttonView == mAlertTempHigh) {
            mValue.tempHighAlertAuto = isChecked;
        } else if (buttonView == mAlertTempLow) {
            mValue.tempLowAlertAuto = isChecked;
        } else if (buttonView == mAlertHumidityHigh) {
            mValue.humidityHighAlertAuto = isChecked;
        } else if (buttonView == mAlertHumidityLow) {
            mValue.humidityLowAlertAuto = isChecked;
        }
        showValue();
    }
}