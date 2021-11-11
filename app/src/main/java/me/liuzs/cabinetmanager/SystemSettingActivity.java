package me.liuzs.cabinetmanager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import me.liuzs.cabinetmanager.net.RemoteAPI;
import me.liuzs.cabinetmanager.ui.setting.EquipmentManageFragment;
import me.liuzs.cabinetmanager.ui.setting.SettingMainFragment;

public class SystemSettingActivity extends BaseActivity {

    private final SettingMainFragment mMainFragment = new SettingMainFragment();
    private final EquipmentManageFragment mEquipmentManageFragment = new EquipmentManageFragment();

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_setting);
        if (savedInstanceState == null) {
            transToMainFragment();
        }
    }

    public void onBackButtonClick(View view) {
        hideInputMethod();
        if (!mMainFragment.isHidden()) {
            finish();
        } else {
            transToMainFragment();
        }
    }

    public void onDownloadButtonClick(View view) {
        Uri uri = Uri.parse(RemoteAPI.APP_DOWNLOAD);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public void transToMainFragment() {
        hideAllFragment();
        if (mMainFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction().show(mMainFragment).commit();
        } else {
            getSupportFragmentManager().beginTransaction().add(R.id.container, mMainFragment).commit();
        }
    }

    public void transToEquipmentManageFragment() {
        hideAllFragment();
        if (mEquipmentManageFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction().show(mEquipmentManageFragment).commit();
        } else {
            getSupportFragmentManager().beginTransaction().add(R.id.container, mEquipmentManageFragment).commit();
        }
    }

    private void hideAllFragment() {
        if (mMainFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction().hide(mMainFragment).commit();
        }
        if (mEquipmentManageFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction().hide(mEquipmentManageFragment).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            navigateUpTo(new Intent(this, MainActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}