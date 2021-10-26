package me.liuzs.cabinetmanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class ContainerNoCreateActivity extends AppCompatActivity {

    public static final String KEY_RESULT_VALUE = "KEY_RESULT_VALUE";
    public static final String TAG = "ContainerNoCreateActivity";
    private EditText mBatchName, mNoCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setContentView(R.layout.activity_container_no_create);
        mBatchName = findViewById(R.id.etBatchName);
        mNoCount = findViewById(R.id.etBatchNoCount);
        String name = "BN" + System.currentTimeMillis();
        name = name.substring(0, name.length() - 3);
        mBatchName.setText(name);
    }

    public void onOKButtonClick(View v) {
        Intent data = new Intent();
        try {
            data.putExtra(KEY_RESULT_VALUE, "创建成功!");
            setResult(RESULT_OK, data);
            finish();
        } catch (Exception ignored) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onCancelButtonClick(View v) {
        setResult(RESULT_CANCELED);
        finish();
    }
}