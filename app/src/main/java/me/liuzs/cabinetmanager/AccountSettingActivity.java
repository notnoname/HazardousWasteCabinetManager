package me.liuzs.cabinetmanager;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.Toolbar;

import java.lang.ref.WeakReference;

import me.liuzs.cabinetmanager.model.UserInfo;
import me.liuzs.cabinetmanager.net.APIJSON;
import me.liuzs.cabinetmanager.net.LoginJSON;
import me.liuzs.cabinetmanager.net.RemoteAPI;
import me.liuzs.cabinetmanager.util.Util;

public class AccountSettingActivity extends BaseActivity {

    public static final String KEY_IS_INIT = "KEY_IS_INIT";
    private static final String TAG = "AccountSettingActivity";
    private EditText mAdminName, mAdminID, mAdminPassword;

    private UserInfo mUserInfo;

    private Button mFaceId;

    private boolean isInit = true;
    private String mToken = null;
    private final ActivityResultLauncher<Intent> mLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            int resultCode = result.getResultCode();
            if (resultCode == RESULT_OK) {
                showToast("管理员初始化成功");
                String name = mAdminName.getText().toString();
                String id = mAdminID.getText().toString();
                String password = mAdminPassword.getText().toString();
                CtrlFunc.saveAdmin1(AccountSettingActivity.this, name, id, Util.md5(password), mToken, mUserInfo, id);
                finish();
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setting);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Util.fullScreen(this);

        mAdminName = findViewById(R.id.etAccountNameValue);
        mAdminID = findViewById(R.id.etAccountIDValue);
        mAdminPassword = findViewById(R.id.etAccountPasswordValue);
        mFaceId = findViewById(R.id.btnFaceId);
        isInit = getIntent().getBooleanExtra(KEY_IS_INIT, true);
        if (isInit) {
            ((TextView) findViewById(R.id.toolbar_title)).setText(R.string.init);
        }
    }

    private boolean isAccountValidate(String name, String id, String password) {
        boolean result = true;
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(id) || TextUtils.isEmpty(password)) {
            showToast(R.string.input_error_tip);
            result = false;
        }
        return result;
    }

    private void startFaceIdRegister() {
        Intent intent = new Intent(AccountSettingActivity.this, RegisterAndRecognizeActivity.class);
        intent.putExtra(RegisterAndRecognizeActivity.PARAM_IS_FOR_REG, true);
        intent.putExtra(RegisterAndRecognizeActivity.PARAM_NAME, mAdminID.getText().toString());
        mLauncher.launch(intent);
    }

    public void onFaceIdInputClick(final View view) {
        String name = mAdminName.getText().toString();
        String id = mAdminID.getText().toString();
        String password = mAdminPassword.getText().toString();
        if (!isAccountValidate(name, id, password)) {
            return;
        }
        new LoginTask(this).execute(id, password);
    }

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {

    }

    static class LoginTask extends AsyncTask<String, Void, APIJSON<LoginJSON>> {

        private final WeakReference<AccountSettingActivity> mActivity;

        public LoginTask(AccountSettingActivity activity) {
            this.mActivity = new WeakReference<>(activity);
        }

        @Override
        protected APIJSON<LoginJSON> doInBackground(String... strings) {
            Log.d(TAG, String.valueOf(strings.length));
            return RemoteAPI.System.login(strings[0], Util.md5(strings[1]));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mActivity.get().mFaceId.setClickable(true);
        }

        @Override
        protected void onPostExecute(APIJSON<LoginJSON> json) {
            super.onPostExecute(json);
            if (json.code == 200) {
                mActivity.get().showToast("用户鉴权成功，将进行人像识别");
                mActivity.get().mUserInfo = json.data.userInfo;
                mActivity.get().mUserInfo.token = json.data.token;
                mActivity.get().mToken = json.data.token;
                mActivity.get().startFaceIdRegister();
            } else {
                mActivity.get().showToast(json.message != null ? json.message : json.msg);
            }
            mActivity.get().mFaceId.setClickable(true);
        }
    }
}