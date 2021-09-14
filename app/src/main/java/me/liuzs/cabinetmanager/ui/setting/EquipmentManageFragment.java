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

import me.liuzs.cabinetmanager.CtrlFunc;
import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.SystemSettingActivity;
import me.liuzs.cabinetmanager.model.SetupValue;

public class EquipmentManageFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    public static final String TAG = "EquipmentManageFragment";
    private final DecimalFormat mDecimalFormat = new DecimalFormat("0.00");
    private SystemSettingActivity mActivity;
    private SetupValue mValue;
    private SwitchButton mFanAuto;
    private EditText mWorkTime, mStopTime, mTemp, mPPM;
    private TextView mFanFunc;
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
        mFanAuto = view.findViewById(R.id.swFan);
        mWorkTime = view.findViewById(R.id.etFanStartTimeValue);
        mStopTime = view.findViewById(R.id.etFanStopTimeValue);
        mTemp = view.findViewById(R.id.etFanStartTempValue);
        mPPM = view.findViewById(R.id.etFanStartPPMValue);
        mFanFunc = view.findViewById(R.id.tvFanSetupValue);
        mSave = view.findViewById(R.id.btnSave);
        mSave.setOnClickListener(this);
        mFanAuto.setOnCheckedChangeListener(this);
        mActivity = (SystemSettingActivity) getActivity();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mValue = CtrlFunc.getSetupValue(mActivity);
        showValue();
    }

    private void showValue() {
        mFanAuto.setChecked(mValue.fanAuto);
        mWorkTime.setText(String.valueOf(mValue.fanWorkTime));
        mStopTime.setText(String.valueOf(mValue.fanStopTime));
        mTemp.setText(String.valueOf(mValue.thresholdTemp));
        mPPM.setText(mDecimalFormat.format(mValue.thresholdPPM));
        if (mValue.fanAuto) {
            mWorkTime.setEnabled(true);
            mStopTime.setEnabled(true);
            mFanFunc.setText(R.string.auto);
            mFanFunc.setBackgroundResource(R.drawable.background_state_green);
        } else {
            mWorkTime.setEnabled(false);
            mStopTime.setEnabled(false);
            mFanFunc.setText(R.string.manual);
            mFanFunc.setBackgroundResource(R.drawable.background_state_red);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mSave) {
            SetupValue newValue = checkValue();
            if (newValue != null) {
                mSave.setEnabled(false);
                newValue.fanAuto = mValue.fanAuto;
                mValue = newValue;
                CtrlFunc.saveSetupValue(mActivity, mValue);
                showValue();
                mActivity.showToast("设置保存成功!");
                mSave.setEnabled(true);
            }
        }
    }

    private SetupValue checkValue() {
        SetupValue result = null;
        try {
            String work = mWorkTime.getEditableText().toString();
            String stop = mStopTime.getEditableText().toString();
            String temp = mTemp.getEditableText().toString();
            String ppm = mPPM.getEditableText().toString();
            int workTime = Integer.parseInt(work);
            int stopTime = Integer.parseInt(stop);
            int tempValue = Integer.parseInt(temp);
            float ppmValue = Float.parseFloat(ppm);
            result = new SetupValue();
            result.fanWorkTime = workTime;
            result.fanStopTime = stopTime;
            result.thresholdTemp = tempValue;
            result.thresholdPPM = ppmValue;
        } catch (Exception e) {
            e.printStackTrace();
            mActivity.showToast("输入有误，请检查！");
        }
        return result;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mValue.fanAuto = isChecked;
        showValue();
    }
}