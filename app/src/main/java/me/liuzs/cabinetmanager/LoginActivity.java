package me.liuzs.cabinetmanager;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.Toolbar;

import java.lang.ref.WeakReference;

import me.liuzs.cabinetmanager.model.Cabinet;
import me.liuzs.cabinetmanager.model.User;
import me.liuzs.cabinetmanager.net.APIJSON;
import me.liuzs.cabinetmanager.net.RemoteAPI;
import me.liuzs.cabinetmanager.util.Util;

public class LoginActivity extends BaseActivity {

    public static final String KEY_AUTH_TYPE = "KEY_AUTH_TYPE";
    private static final String TAG = "LoginActivity";
    private EditText mAccountID, mAdminPassword;
    private User mUser;
    private CabinetCore.RoleType mType = CabinetCore.RoleType.Admin;
    private final ActivityResultLauncher<Intent> mLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        int resultCode = result.getResultCode();
        if (resultCode == RESULT_OK) {
            onFaceIDRegisterSuccess();
        }
    });
    private Button mFaceId;
    private TextView mTitleView, mLabelAccountID;

    public static void start(Context context, CabinetCore.RoleType type) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(LoginActivity.KEY_AUTH_TYPE, type);
        if (context instanceof Application) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    private void onFaceIDRegisterSuccess() {
        showToast(getResources().getString(R.string.login_success));
        mUser.faceId = mUser.account;
        CabinetCore.saveCabinetUser(mUser, mType);
        CabinetCore.restart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setting);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Util.fullScreen(this);

        mAccountID = findViewById(R.id.etAccountIDValue);
        mAdminPassword = findViewById(R.id.etAccountPasswordValue);
        mFaceId = findViewById(R.id.btnFaceId);
        mTitleView = findViewById(R.id.toolbar_title);
        mLabelAccountID = findViewById(R.id.tvAccountIDLabel);
        mType = (CabinetCore.RoleType) getIntent().getSerializableExtra(KEY_AUTH_TYPE);
        switch (mType) {
            case Admin:
                mTitleView.setText(R.string.title_login_admin);
                mLabelAccountID.setText(R.string.admin_id);
                break;
            case Operator:
                mTitleView.setText(R.string.title_login_operator);
                mLabelAccountID.setText(R.string.operator_id);
                break;
        }
    }

    private boolean isAccountValidate(String id, String password) {
        boolean result = true;
        if (TextUtils.isEmpty(id) || TextUtils.isEmpty(password)) {
            showToast(R.string.input_error_tip);
            result = false;
        }
        return result;
    }

    private void startFaceIdRegister(String account) {
        Intent intent = new Intent(LoginActivity.this, RegisterAndRecognizeActivity.class);
        intent.putExtra(RegisterAndRecognizeActivity.PARAM_IS_FOR_REG, true);
        intent.putExtra(RegisterAndRecognizeActivity.PARAM_AUTH_ACCOUNT, account);
        mLauncher.launch(intent);
    }

    public void onFaceIdInputClick(final View view) {
        String id = mAccountID.getText().toString();
        String password = mAdminPassword.getText().toString();
        if (!isAccountValidate(id, password)) {
            return;
        }
        new LoginTask(this).execute(id, password);
    }

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {

    }

    static class LoginTask extends AsyncTask<String, Void, APIJSON<User>> {

        private final WeakReference<LoginActivity> mActivity;

        public LoginTask(LoginActivity activity) {
            this.mActivity = new WeakReference<>(activity);
        }

        @Override
        protected APIJSON<User> doInBackground(String... strings) {
            return RemoteAPI.System.login(strings[0], Util.md5(strings[1]));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mActivity.get().mFaceId.setClickable(true);
        }

        private boolean isHaveOptRight(User user, Cabinet cabinet) {
            if (user == null || user.opt == null || user.opt.length == 0 || cabinet == null) {
                return false;
            }
            for (String id : user.opt) {
                if (cabinet.id.equals(id)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(APIJSON<User> json) {
            super.onPostExecute(json);
            if (json.status == APIJSON.Status.ok) {
                User user = json.data;
                if (mActivity.get().mType == CabinetCore.RoleType.Operator && !isHaveOptRight(user, CabinetCore.getCabinetInfo())) {
                    mActivity.get().showToast("此用户无此暂存柜操作权限，请重新登录");
                } else {
                    mActivity.get().showToast("用户鉴权成功，将进行人像识别");
                    mActivity.get().mUser = user;
                    if (BuildConfig.DEBUG) {
                        mActivity.get().onFaceIDRegisterSuccess();
                    } else {
                        mActivity.get().startFaceIdRegister(user.account);
                    }
                }
            } else {
                mActivity.get().showToast(json.errors);
            }
            mActivity.get().mFaceId.setClickable(true);
        }
    }
}