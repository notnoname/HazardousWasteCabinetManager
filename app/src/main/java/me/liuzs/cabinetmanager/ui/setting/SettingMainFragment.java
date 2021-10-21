package me.liuzs.cabinetmanager.ui.setting;


import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import me.liuzs.cabinetmanager.CabinetBindActivity;
import me.liuzs.cabinetmanager.CabinetCore;
import me.liuzs.cabinetmanager.HardwareSetupActiveActivity;
import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.SystemSettingActivity;
import me.liuzs.cabinetmanager.faceid.FaceServer;


public class SettingMainFragment extends Fragment implements View.OnClickListener {

    private AppCompatButton mCabinetSetup, mEquipmentManage, mEquipmentLog, mHardwareSetup, mReset;

    private SystemSettingActivity mActivity;

    public SettingMainFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_setting_main, container, false);
        mCabinetSetup = rootView.findViewById(R.id.btnCabinetSet);
        mEquipmentManage = rootView.findViewById(R.id.btnHardwareManage);
        mEquipmentLog = rootView.findViewById(R.id.btnLog);
        mHardwareSetup = rootView.findViewById(R.id.btnHardwareSetup);
        mReset = rootView.findViewById(R.id.btnFCReset);
        mCabinetSetup.setOnClickListener(this);
        mEquipmentManage.setOnClickListener(this);
        mEquipmentLog.setOnClickListener(this);
        mHardwareSetup.setOnClickListener(this);
        mReset.setOnClickListener(this);
        mActivity = (SystemSettingActivity) getActivity();
        return rootView;
    }

    @Override
    public void onClick(View v) {
        if (mCabinetSetup.equals(v)) {
            CabinetBindActivity.start(getActivity(), CabinetCore.getCabinetInfo());
        } else if (mEquipmentManage.equals(v)) {
            mActivity.transToEquipmentManageFragment();
        } else if (mEquipmentLog.equals(v)) {
//TODO
        } else if (mHardwareSetup.equals(v)) {
            Intent intent = new Intent(mActivity, HardwareSetupActiveActivity.class);
            mActivity.startActivity(intent);
        } else if (mReset.equals(v)) {
            new AlertDialog.Builder(mActivity).setMessage("初始化将删除所有配置信息，请谨慎操作！").setNegativeButton("确认", (dialog, which) -> reset()).setNeutralButton("取消", null).show();
        }
    }

    private void reset() {
        CabinetCore.clearCabinetUser(CabinetCore.RoleType.Admin);
        CabinetCore.clearCabinetUser(CabinetCore.RoleType.Operator);
        CabinetCore.removeUnSubmitDepositRecord(getActivity());
        CabinetCore.clearCabinetInfo();
        CabinetCore.removeUnSubmitTakeOutInfo(getActivity());
        CabinetCore.removeUnSubmitUsageInfo(getActivity());
        CabinetCore.removeConnectedPrinterInfo(getActivity());
        FaceServer.getInstance().clearAllFaces(getActivity());
        CabinetCore.restart();
    }
}