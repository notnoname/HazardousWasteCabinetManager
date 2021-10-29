package me.liuzs.cabinetmanager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

public class MultiSpinnerActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String KEY_SELECT_VALUE = "KEY_SELECT_VALUE";
    public static final String KEY_OPTIONS = "KEY_OPTIONS";
    public static final String KEY_TIP_INFO = "KEY_TIP_INFO";
    public static final String TAG = "MultiSpinnerActivity";
    public static final String SELECTED = "✓";
    public static final String UNSELECTED = " ";
    public static final Type JSON_TYPE = new TypeToken<Map<String, Boolean>>() {
    }.getType();
    private LinearLayout mLLOptions;
    private TextView mTip;
    private Map<String, Boolean> mOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setContentView(R.layout.activity_spinner);
        findViewById(R.id.btnOK).setVisibility(View.VISIBLE);
        mTip = findViewById(R.id.tvTip);
        mTip.setText(getIntent().getStringExtra(KEY_TIP_INFO));
        mLLOptions = findViewById(R.id.llOptions);
        String json = getIntent().getStringExtra(KEY_OPTIONS);

        mOptions = CabinetCore.GSON.fromJson(json, JSON_TYPE);
        showOptions();
    }

    @SuppressLint("SetTextI18n")
    private void showOptions() {
        mLLOptions.removeAllViews();
        for (Map.Entry<String, Boolean> entry : mOptions.entrySet()) {
            TextView textView = new TextView(this);
            textView.setTag(entry);
            textView.setBackgroundResource(R.drawable.background_info_area);
            textView.setOnClickListener(this);
            textView.setText((entry.getValue() ? SELECTED : UNSELECTED) + entry.getKey());
            textView.setTextColor(Color.WHITE);
            textView.setPadding(10, 10, 10, 10);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 28);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.topMargin = 10;
            lp.bottomMargin = 10;
            mLLOptions.addView(textView, lp);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onClick(View v) {
        Map.Entry<String, Boolean> entry = (Map.Entry<String, Boolean>) v.getTag();
        entry.setValue(!entry.getValue());
        ((TextView) v).setText((entry.getValue() ? SELECTED : UNSELECTED) + entry.getKey());
    }

    public void onOKButtonClick(View v) {
        Intent data = new Intent();
        Map<String, Boolean> result = new LinkedHashMap<>();
        for (int index = 0; index < mLLOptions.getChildCount(); index++) {
            View child = mLLOptions.getChildAt(index);
            Map.Entry<String, Boolean> entry = (Map.Entry<String, Boolean>) child.getTag();
            result.put(entry.getKey(), entry.getValue());
        }
        data.putExtra(KEY_SELECT_VALUE, CabinetCore.GSON.toJson(result));
        setResult(RESULT_OK, data);
        finish();
    }
}