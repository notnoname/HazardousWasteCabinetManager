package me.liuzs.cabinetmanager.ui.setting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.kyleduo.switchbutton.SwitchButton;

import java.text.DecimalFormat;

import me.liuzs.cabinetmanager.CabinetCore;
import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.SystemSettingActivity;
import me.liuzs.cabinetmanager.model.SetupValue;
import me.liuzs.cabinetmanager.service.ModbusService;

/**
 * 参数设置
 */
public class EquipmentManageFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    public static final String TAG = "EquipmentManageFragment";
    private final DecimalFormat mDecimalFormat = new DecimalFormat("0.00");
    private SystemSettingActivity mActivity;
    private SetupValue mValue;
    private SwitchButton mFanWorkModel, mAlertVOC, mAlertFG, mAlertTempHigh, mAlertTempLow, mAlertHumidityHigh, mAlertHumidityLow, mAlertSoundLight;
    private EditText mFanRunTimeValue, mFanStopTimeValue, mFanFrequencyValue, mUnionVOCHigh, mUnionVOCLow, mAlertVOCValue, mAlertFGValue, mAlertTempHighValue, mAlertTempLowValue, mAlertHumidityHighValue, mAlertHumidityLowValue;
    private TextView mFanWorkModelValue;
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
        mFanWorkModel = view.findViewById(R.id.swFanModel);
        mFanWorkModelValue = view.findViewById(R.id.tvFanModelValue);

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


        mSave = view.findViewById(R.id.btnSave);
        mSave.setOnClickListener(this);
        mFanWorkModel.setOnCheckedChangeListener(this);
        mActivity = (SystemSettingActivity) getActivity();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mValue = ModbusService.getSetupValue();
        showValue();
    }

    private void showValue() {
        mFanWorkModel.setChecked(mValue.workModel == SetupValue.WorkModel.Auto);
        switch (mValue.workModel) {
            case Auto:
                mFanRunTimeValue.setEnabled(true);
                mFanStopTimeValue.setEnabled(true);
                mFanFrequencyValue.setEnabled(true);
                mUnionVOCHigh.setEnabled(true);
                mUnionVOCLow.setEnabled(true);
                mFanWorkModelValue.setText(R.string.auto);
                mFanWorkModelValue.setBackgroundResource(R.drawable.background_state_green);
                break;
            case Manual:
                mFanRunTimeValue.setEnabled(false);
                mFanStopTimeValue.setEnabled(false);
                mFanFrequencyValue.setEnabled(false);
                mUnionVOCHigh.setEnabled(false);
                mUnionVOCLow.setEnabled(false);
                mFanWorkModelValue.setText(R.string.manual);
                mFanWorkModelValue.setBackgroundResource(R.drawable.background_state_red);
                break;
            case None:
                mFanRunTimeValue.setEnabled(false);
                mFanStopTimeValue.setEnabled(false);
                mFanFrequencyValue.setEnabled(false);
                mUnionVOCHigh.setEnabled(false);
                mUnionVOCLow.setEnabled(false);
                mFanWorkModelValue.setText(R.string.none);
                mFanWorkModelValue.setBackgroundResource(R.drawable.background_state_red);
                break;
        }

        mFanRunTimeValue.setText(String.valueOf(mValue.workTime));
        mFanStopTimeValue.setText(String.valueOf(mValue.stopTime));
        mFanFrequencyValue.setText(String.valueOf(mValue.frequency));
        mUnionVOCHigh.setText(String.valueOf(mValue.vocThresholdMax));
        mUnionVOCLow.setText(String.valueOf(mValue.vocThresholdMin));


        mAlertVOC.setChecked(mValue.vocAlertAuto);
        if (mValue.vocAlertAuto) {
            mAlertVOCValue.setEnabled(true);
        } else {
            mAlertVOCValue.setEnabled(false);
        }
        mAlertVOCValue.setText(String.valueOf(mValue.vocAlertThreshold));


        mAlertTempHigh.setChecked(mValue.temperatureHighAlertAuto);
        mAlertTempLow.setChecked(mValue.temperatureLowAlertAuto);

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
            result.workTime = workTime;
            result.stopTime = stopTime;

        } catch (Exception e) {
            e.printStackTrace();
            mActivity.showToast("输入有误，请检查！");
        }
        return result;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == mFanWorkModel) {
            mValue.workModel = isChecked ? SetupValue.WorkModel.Auto : SetupValue.WorkModel.Manual;
        } else if (buttonView == mAlertVOC) {
            mValue.vocAlertAuto = isChecked;
        } else if (buttonView == mAlertFG) {
            mValue.vocAlertAuto = isChecked;
        } else if (buttonView == mAlertTempHigh) {
            mValue.vocAlertAuto = isChecked;
        } else if (buttonView == mAlertTempLow) {
            mValue.vocAlertAuto = isChecked;
        } else if (buttonView == mAlertHumidityHigh) {
            mValue.vocAlertAuto = isChecked;
        } else if (buttonView == mAlertHumidityLow) {
            mValue.vocAlertAuto = isChecked;
        }
        showValue();
    }
}