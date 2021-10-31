package me.liuzs.cabinetmanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Date;

public class ContainerNoCreateActivity extends AppCompatActivity {

    public static final String KEY_RESULT_BATCH_NAME = "KEY_RESULT_BATCH_NAME";
    public static final String KEY_RESULT_COUNT = "KEY_RESULT_COUNT";
    public static final String TAG = "ContainerNoCreateActivity";
    private EditText mBatchName, mNoCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setContentView(R.layout.activity_container_no_create);
        mBatchName = findViewById(R.id.etBatchName);
        mNoCount = findViewById(R.id.etBatchNoCount);
        String name = "NB" + CabinetCore._NoSplitYearFormatter.format(new Date(System.currentTimeMillis()));
        mBatchName.setText(name);
    }

    public void onOKButtonClick(View v) {
        Intent data = new Intent();
        try {
            String name = mBatchName.getText().toString();
            String countStr = mNoCount.getText().toString();
            int count = Integer.parseInt(countStr);
            if(TextUtils.isEmpty(name)) {
                return;
            }
            if(count > 50) {
                mNoCount.setText("");
                return;
            }
            data.putExtra(KEY_RESULT_BATCH_NAME, name);
            data.putExtra(KEY_RESULT_COUNT, String.valueOf(count));
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