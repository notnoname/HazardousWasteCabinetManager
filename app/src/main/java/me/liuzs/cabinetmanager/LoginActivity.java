package me.liuzs.cabinetmanager;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import me.liuzs.cabinetmanager.model.Cabinet;
import me.liuzs.cabinetmanager.model.User;
import me.liuzs.cabinetmanager.net.APIJSON;
import me.liuzs.cabinetmanager.net.RemoteAPI;

public class LoginActivity extends BaseActivity {

    public static final String KEY_AUTH_TYPE = "KEY_AUTH_TYPE";
    public static final String TAG = "LoginActivity";
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
        mUser.faceId = mUser.username;
        CabinetCore.saveCabinetUser(mUser, mType);
        CabinetCore.restart();
    }

    public void onBackButtonClick(View view) {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setting);
        mAccountID = findViewById(R.id.etAccountIDValue);
        mAdminPassword = findViewById(R.id.etAccountPasswordValue);
        mFaceId = findViewById(R.id.btnFaceId);
        mTitleView = findViewById(R.id.toolbar_title);
        mLabelAccountID = findViewById(R.id.tvAccountIDLabel);
        mType = (CabinetCore.RoleType) getIntent().getSerializableExtra(KEY_AUTH_TYPE);
        User user = null;
        Cabinet cabinet = CabinetCore.getCabinetInfo();
        switch (mType) {
            case Admin:
                mTitleView.setText(R.string.title_login_admin);
                mLabelAccountID.setText(R.string.admin_id);
                user = CabinetCore.getCabinetUser(CabinetCore.RoleType.Admin);
                break;
            case Operator:
                mTitleView.setText(R.string.title_login_operator);
                mLabelAccountID.setText(R.string.operator_id);
                user = CabinetCore.getCabinetUser(CabinetCore.RoleType.Operator);
                break;
        }
        //切换用户时候显示返回按钮
        if (user != null && cabinet != null) {
            findViewById(R.id.toolbar_back).setVisibility(View.VISIBLE);
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
        login(id, password);
    }

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {

    }

    private void login(String username, String password) {
        showProgressDialog();
        getExecutorService().submit(() -> {
            APIJSON<User> userJSON = RemoteAPI.System.login(username, password);
            if (userJSON.status == APIJSON.Status.ok) {
                User user = userJSON.data;
                user.password = password;
                Cabinet cabinet = CabinetCore.getCabinetInfo();
                if (mType == CabinetCore.RoleType.Operator && !isHaveOptRight(user, cabinet)) {
                    showToast("此用户无此暂存柜操作权限，请重新登录");
                } else {
                    if (mType == CabinetCore.RoleType.Admin && cabinet != null && !isHaveAdminRight(user, CabinetCore.getCabinetInfo())) {
                        showToast("此用户无此暂存柜管理权限，请重新登录");
                    } else {
                        showToast("用户鉴权成功，将进行人像识别");
                        mUser = user;
                        if (CabinetCore.isDebugState()) {
                            onFaceIDRegisterSuccess();
                        } else {
                            startFaceIdRegister(user.username);
                        }
                    }
                }
            } else {
                showToast(userJSON.error);
            }
            mFaceId.setClickable(true);
            dismissProgressDialog();
        });
    }

    private boolean isHaveOptRight(User user, Cabinet cabinet) {
        if (user == null || user.opt_storages == null || user.opt_storages.length == 0 || cabinet == null) {
            return false;
        }
        for (String id : user.opt_storages) {
            if (cabinet.id.equals(id)) {
                return true;
            }
        }
        return false;
    }

    private boolean isHaveAdminRight(User user, Cabinet cabinet) {
        if (user == null || user.own_storages == null || user.own_storages.length == 0 || cabinet == null) {
            return false;
        }
        for (String id : user.own_storages) {
            if (cabinet.id.equals(id)) {
                return true;
            }
        }
        return false;
    }
}