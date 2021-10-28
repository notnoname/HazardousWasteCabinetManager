package me.liuzs.cabinetmanager;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import me.liuzs.cabinetmanager.model.DepositRecord;

/**
 * 初次存入模块
 */
public class DepositActivity extends BaseActivity {

    public final static String TAG = "DepositActivity";
    private DepositRecord mDepositRecord;
    private TextView mLaboratoryName, mDepositUser, mDepositRecorderId, mDepositItemCount;

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit);
    }

    public void onBackButtonClick(View view) {
        finish();
    }

    public void onSubmitButtonClick(View view) {

    }

}