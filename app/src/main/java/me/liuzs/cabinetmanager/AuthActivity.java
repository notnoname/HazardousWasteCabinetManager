package me.liuzs.cabinetmanager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class AuthActivity extends AppCompatActivity {

    public static final String KEY_AUTH_SUCCESS_ID = "KEY_AUTH_SUCCESS_ID";
    public static final String KEY_AUTH_TYPE = "KEY_AUTH_TYPE";
    public static final String TAG = "AuthActivity";
    private final Handler mHandler = new Handler();
    private BaseActivity.AuthType mType = BaseActivity.AuthType.Admin;
    private Button mOK, mCancel, mFaceId;
    private EditText mPassword;
    private TextView mInfo;
    private final ActivityResultLauncher<Intent> mLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            int resultCode = result.getResultCode();
            if (resultCode == RESULT_OK) {
                mInfo.setText("验证成功！");
                mPassword.setEnabled(false);
                mOK.setEnabled(false);
                mCancel.setEnabled(false);
                mFaceId.setEnabled(false);
                mHandler.postDelayed(() -> {
                    String id = CtrlFunc.getAdminUserID(AuthActivity.this);
                    Intent data = new Intent();
                    data.putExtra(KEY_AUTH_SUCCESS_ID, id);
                    setResult(RESULT_OK, data);
                    finish();
                }, 1000);

            } else {
                mInfo.setText("人脸识别验证已取消！");
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setContentView(R.layout.activity_auth);
        mType = (BaseActivity.AuthType) getIntent().getSerializableExtra(KEY_AUTH_TYPE);
        mOK = findViewById(R.id.btnOK);
        mCancel = findViewById(R.id.btnCancel);
        mFaceId = findViewById(R.id.btnFaceId);
        mPassword = findViewById(R.id.editTextTextPassword);
        mInfo = findViewById(R.id.tvInfo);
    }

    public void onCancelButtonClick(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void onOKButtonClick(View view) {
        String id = CtrlFunc.checkAdminUser(this, mPassword.getText().toString());
        if (id != null) {
            mInfo.setText("验证成功！");
            mPassword.setEnabled(false);
            mOK.setEnabled(false);
            mCancel.setEnabled(false);
            mFaceId.setEnabled(false);
            mHandler.postDelayed(() -> {
                Intent data = new Intent();
                data.putExtra(KEY_AUTH_SUCCESS_ID, id);
                setResult(RESULT_OK, data);
                finish();
            }, 1000);

        } else {
            mInfo.setText("密码错误，请重新输入！");
        }
    }

    public void onFaceIDButtonClick(View view) {
        Intent intent = new Intent(this, RegisterAndRecognizeActivity.class);
        intent.putExtra(RegisterAndRecognizeActivity.PARAM_IS_FOR_REG, false);
        intent.putExtra(RegisterAndRecognizeActivity.PARAM_NAME, CtrlFunc.getAdminUserID(this));
        mLauncher.launch(intent);
    }
}