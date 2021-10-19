package me.liuzs.cabinetmanager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class SpinnerActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String KEY_SELECT_VALUE = "KEY_SELECT_VALUE";
    public static final String KEY_SELECT_INDEX = "KEY_SELECT_INDEX";
    public static final String KEY_OPTIONS = "KEY_OPTIONS";
    public static final String KEY_TIP_INFO = "KEY_TIP_INFO";
    public static final String TAG = "SpinnerActivity";
    private LinearLayout mLLOptions;
    private TextView mTip;
    private List<String> mOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setContentView(R.layout.activity_spinner);
        mTip = findViewById(R.id.tvTip);
        mTip.setText(getIntent().getStringExtra(KEY_TIP_INFO));
        mLLOptions = findViewById(R.id.llOptions);
        String json = getIntent().getStringExtra(KEY_OPTIONS);
        Type jsonType = new TypeToken<List<String>>() {
        }.getType();
        mOptions = CabinetCore.GSON.fromJson(json, jsonType);

        showOptions();
    }

    private void showOptions() {
        mLLOptions.removeAllViews();
        int index = 0;
        for (String option : mOptions) {
            TextView textView = new TextView(this);
            textView.setTag(index);
            textView.setBackgroundResource(R.drawable.background_info_area);
            textView.setOnClickListener(this);
            textView.setText(option);
            textView.setTextColor(Color.WHITE);
            textView.setPadding(10, 10, 10, 10);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 28);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.topMargin = 10;
            lp.bottomMargin = 10;
            mLLOptions.addView(textView, lp);
        }
    }

    @Override
    public void onClick(View v) {
        TextView tv = (TextView) v;
        Intent data = new Intent();
        data.putExtra(KEY_SELECT_VALUE, tv.getText());
        data.putExtra(KEY_SELECT_INDEX, tv.getTag().toString());
        setResult(RESULT_OK, data);
        finish();
    }
}