package me.liuzs.cabinetmanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import me.liuzs.cabinetmanager.model.Cabinet;
import me.liuzs.cabinetmanager.model.DefaultDict;
import me.liuzs.cabinetmanager.model.DepositRecord;

/**
 * 存入模块
 */
public class DepositActivity extends BaseActivity {

    public final static String TAG = "DepositActivity";
    private DepositRecord mDepositRecord;
    private TextView mContainerSpecValue, mSourceValue, mHarmfulIngredientsValue;
    private EditText mContainerNoValue, mWeightValue, mShelfNoValue, mOtherInfoValue;

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

    public void onWeightButtonClick(View view) {

    }

    public void onSubmitButtonClick(View view) {

    }

    public void onHarmfulIngredientsValueClick(View view) {
        Intent intent = new Intent(this, MultiSpinnerActivity.class);
        Map<String, Boolean> options = new LinkedHashMap<>();
        for (String hi : DefaultDict.HarmfulIngredient) {
            options.put(hi, false);
        }
        intent.putExtra(MultiSpinnerActivity.KEY_OPTIONS, CabinetCore.GSON.toJson(options));
        intent.putExtra(MultiSpinnerActivity.KEY_TIP_INFO, "请选择有害成分：");
        mHarmfulIngredientSelectLauncher.launch(intent);
    }

    private final List<String> mHarmfulIngredient = new LinkedList<>();

    private final ActivityResultLauncher<Intent> mHarmfulIngredientSelectLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        int resultCode = result.getResultCode();
        if (resultCode == RESULT_OK) {
            Intent data = result.getData();
            assert data != null;
            String selectValue = data.getStringExtra(SpinnerActivity.KEY_SELECT_VALUE);
            assert selectValue != null;
            Map<String, Boolean> select = CabinetCore.GSON.fromJson(selectValue, MultiSpinnerActivity.JSON_TYPE);
            mHarmfulIngredient.clear();
            for (Map.Entry<String, Boolean> entry : select.entrySet()) {
                if(entry.getValue()) {
                    mHarmfulIngredient.add(entry.getKey());
                }
            }
        }
    });

}