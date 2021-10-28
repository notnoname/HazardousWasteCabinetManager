package me.liuzs.cabinetmanager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.lifecycle.Lifecycle;

import java.lang.ref.WeakReference;

import me.liuzs.cabinetmanager.model.Cabinet;
import me.liuzs.cabinetmanager.model.DepositItem;
import me.liuzs.cabinetmanager.model.DepositRecord;
import me.liuzs.cabinetmanager.model.User;
import me.liuzs.cabinetmanager.net.APIJSON;
import me.liuzs.cabinetmanager.net.RemoteAPI;

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