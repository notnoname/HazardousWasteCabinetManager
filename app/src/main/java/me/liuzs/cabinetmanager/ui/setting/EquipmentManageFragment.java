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

import me.liuzs.cabinetmanager.CabinetCore;
import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.SystemSettingActivity;
import me.liuzs.cabinetmanager.model.modbus.SetupValue;
import me.liuzs.cabinetmanager.service.ModbusService;

/**
 * 参数设置
 */
public class EquipmentManageFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    public static final String TAG = "EquipmentManageFragment";
    private final DecimalFormat mDecimalFormat = new DecimalFormat("0.00");
    private SystemSettingActivity mActivity;
    private SetupValue mValue;
    private SwitchButton mAlertVOC, mAlertFG, mAlertTempHigh, mAlertTempLow, mAlertHumidityHigh, mAlertHumidityLow, mAlertSoundLight;
    private EditText mUnionFanRunTimeValue, mUnionFanStopTimeValue, mUnionFanFrequencyValue, mUnionVOCHigh, mUnionVOCLow, mAlertVOCValue, mAlertFGValue, mAlertTempHighValue, mAlertTempLowValue, mAlertHumidityHighValue, mAlertHumidityLowValue;
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

        mUnionFanRunTimeValue = view.findViewById(R.id.etFanRunTimeValue);
        mUnionFanStopTimeValue = view.findViewById(R.id.etFanStopTimeValue);
        mUnionFanFrequencyValue = view.findViewById(R.id.etFanFrequencyValue);

        mUnionVOCHigh = view.findViewById(R.id.etFanVOCMaxValue);
        mUnionVOCLow = view.findViewById(R.id.etFanVOCMinValue);

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
        mActivity.getExecutorService().submit(() -> {
            mValue = ModbusService.readSetupValue();
            mActivity.runOnUiThread(this::showValue);
        });
    }

    private void showValue() {
        if (mValue.e != null) {
            mActivity.showToast(mValue.e.toString());
            return;
        }

        mUnionFanRunTimeValue.setText(String.valueOf(mValue.fanUnionWorkTime));
        mUnionFanStopTimeValue.setText(String.valueOf(mValue.fanUnionStopTime));
        mUnionFanFrequencyValue.setText(String.valueOf(mValue.fanUnionFrequency));
        mUnionVOCHigh.setText(String.valueOf(mValue.vocUnionMax));
        mUnionVOCLow.setText(String.valueOf(mValue.vocUnionMin));


        mAlertVOC.setChecked(mValue.vocAlertAuto);
        mAlertVOCValue.setText(String.valueOf(mValue.vocAlertAutoThreshold));

        mAlertFGValue.setText(String.valueOf(mValue.fgAlertThreshold));

        mAlertTempHigh.setChecked(mValue.tempHighAlertAuto);
        mAlertTempHighValue.setText(String.valueOf(mValue.tempHighAlertThreshold));


        mAlertTempLow.setChecked(mValue.tempLowAlertAuto);
        mAlertTempLowValue.setText(String.valueOf(mValue.tempLowAlertThreshold));

        mAlertHumidityHigh.setChecked(mValue.humidityHighAlertAuto);
        mAlertHumidityHighValue.setText(String.valueOf(mValue.tempHighAlertThreshold));


        mAlertHumidityLow.setChecked(mValue.humidityLowAlertAuto);
        mAlertHumidityLowValue.setText(String.valueOf(mValue.tempLowAlertThreshold));

        mAlertSoundLight.setChecked(mValue.alertSoundLight);

    }

    private SetupValue checkValue() {
        SetupValue result = null;
        try {
            float unionFanRunTime = Float.parseFloat(mUnionFanRunTimeValue.getEditableText().toString());
            float unionFanStopTime = Float.parseFloat(mUnionFanStopTimeValue.getEditableText().toString());
            float unionFC = Float.parseFloat(mUnionFanFrequencyValue.getEditableText().toString());
            float unionVOCMax = Float.parseFloat(mUnionVOCHigh.getEditableText().toString());
            float unionVOCMin = Float.parseFloat(mUnionVOCLow.getEditableText().toString());

            float vocAlertValue = Float.parseFloat(mAlertVOCValue.getEditableText().toString());
            boolean vocAlert = mAlertVOC.isChecked();
            float tempHighAlertValue = Float.parseFloat(mAlertTempHighValue.getEditableText().toString());
            boolean tempHighAlert = mAlertTempHigh.isChecked();
            float tempLowAlertValue = Float.parseFloat(mAlertTempLowValue.getEditableText().toString());
            boolean tempLowAlert = mAlertTempLow.isChecked();
            float humidityHighAlertValue = Float.parseFloat(mAlertHumidityHighValue.getEditableText().toString());
            boolean humidityHighAlert = mAlertHumidityHigh.isChecked();
            float humidityLowAlertValue = Float.parseFloat(mAlertHumidityLowValue.getEditableText().toString());
            boolean humidityLowAlert = mAlertHumidityLow.isChecked();
            float fgAlertValue = Float.parseFloat(mAlertFGValue.getEditableText().toString());

            result = new SetupValue();
            result.fanUnionWorkTime = (int) unionFanRunTime;
            result.fanUnionStopTime = (int) unionFanStopTime;
            result.fanUnionFrequency = unionFC;
            result.vocUnionMax = unionVOCMax;
            result.vocUnionMin = unionVOCMin;

            result.vocAlertAutoThreshold = vocAlertValue;
            result.vocAlertAuto = vocAlert;
            //result.fgAlertAuto = true;
            result.fgAlertThreshold = fgAlertValue;
            result.tempHighAlertThreshold = tempHighAlertValue;
            result.tempHighAlertAuto = tempHighAlert;
            result.tempLowAlertThreshold = tempLowAlertValue;
            result.tempLowAlertAuto = tempLowAlert;
            result.humidityHighAlertThreshold = humidityHighAlertValue;
            result.humidityHighAlertAuto = humidityHighAlert;
            result.humidityLowAlertThreshold = humidityLowAlertValue;
            result.humidityLowAlertAuto = humidityLowAlert;


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

    @Override
    public void onClick(View view) {
        if (view == mSave) {
            SetupValue newValue = checkValue();
            if (newValue != null) {
                mSave.setEnabled(false);
                mValue = newValue;
                showValue();
                mActivity.getExecutorService().submit(() -> {
                    final boolean result = ModbusService.saveSetupValue(mValue);
                    CabinetCore.logOpt(CabinetCore.RoleType.Admin, "保存", "设置");
                    mActivity.runOnUiThread(() -> {
                        showValue();
                        if(result) {
                            mActivity.showToast("设置保存成功!");
                        } else {
                            mActivity.showToast("设置保存失败!");
                        }
                        mSave.setEnabled(true);
                    });
                });

            }
        }
    }
}