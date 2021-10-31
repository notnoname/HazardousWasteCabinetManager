package me.liuzs.cabinetmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import me.liuzs.cabinetmanager.model.DefaultDict;
import me.liuzs.cabinetmanager.model.DepositRecord;

/**
 * 存入模块
 */
public class DepositActivity extends BaseActivity {

    public final static String TAG = "DepositActivity";
    public final static String KEY_ORG_VALUE = "KEY_ORG_VALUE";
    private DepositRecord mDepositRecord;
    private boolean isNewCreate;
    private TextView mContainerSpecValue, mSourceValue, mHarmfulIngredientsValue;
    private EditText mContainerNoValue, mWeightValue, mShelfNoValue, mOtherInfoValue;
    private final ActivityResultLauncher<Intent> mHarmfulIngredientSelectLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        int resultCode = result.getResultCode();
        if (resultCode == RESULT_OK) {
            Intent data = result.getData();
            assert data != null;
            String selectValue = data.getStringExtra(SpinnerActivity.KEY_SELECT_VALUE);
            assert selectValue != null;
            Map<String, Boolean> select = CabinetCore.GSON.fromJson(selectValue, MultiSpinnerActivity.JSON_TYPE);
            DepositActivity.this.mDepositRecord.harmful_ingredients = createHarmfulIngredientString(select);
            showDepositRecord();
        }
    });

    private final ActivityResultLauncher<Intent> mContainerSpecSelectLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        int resultCode = result.getResultCode();
        if (resultCode == RESULT_OK) {
            Intent data = result.getData();
            assert data != null;
            String selectValue = data.getStringExtra(SpinnerActivity.KEY_SELECT_VALUE);
            assert selectValue != null;
            DepositActivity.this.mDepositRecord.container_spec = selectValue;
            showDepositRecord();
        }
    });

    private final ActivityResultLauncher<Intent> mWeightLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            int resultCode = result.getResultCode();
            if (resultCode == RESULT_OK) {
                Intent data = result.getData();
                assert data != null;
                String selectValue = data.getStringExtra(WeightActivity.KEY_SELECT_VALUE);
                assert selectValue != null;
                float weight = 0;
                try {
                    weight = Float.parseFloat(selectValue);
                } catch (Exception ignored) {
                }
                if (weight > 0 && weight < 1000) {
                    mDepositRecord.in_weight = String.valueOf(weight);
                } else {
                    showToast("称重结果异常!");
                }
                showDepositRecord();
            }
        }
    });

    public static void start(Context content, @Nullable String depositRecordJSON) {
        Intent intent = new Intent(content, DepositActivity.class);
        if (depositRecordJSON != null) {
            intent.putExtra(KEY_ORG_VALUE, depositRecordJSON);
        }
        if (!(content instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        content.startActivity(intent);
    }

    public static String createHarmfulIngredientString(Map<String, Boolean> harmfulIngredientMap) {
        if (harmfulIngredientMap != null) {
            StringBuilder stringBuilder = new StringBuilder();
            for (Map.Entry<String, Boolean> entry : harmfulIngredientMap.entrySet()) {
                if (entry.getValue()) {
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(",");
                    }
                    stringBuilder.append(entry.getKey());
                }
            }
            return stringBuilder.toString();
        } else {
            return null;
        }
    }

    public static Map<String, Boolean> createHarmfulIngredientMap(String harmfulIngredient) {
        List<String> harmfulIngredientList = new LinkedList<>();
        if (!TextUtils.isEmpty(harmfulIngredient)) {
            String[] his = harmfulIngredient.split(",");
            Collections.addAll(harmfulIngredientList, his);
        }
        Map<String, Boolean> result = new LinkedHashMap<>();
        for (String hi : DefaultDict.HarmfulIngredient) {
            boolean has = harmfulIngredientList.contains(hi);
            result.put(hi, has);
        }
        return result;
    }

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit);

        mContainerSpecValue = findViewById(R.id.tvContainerSpecValue);
        mSourceValue = findViewById(R.id.tvSourceValue);
        mHarmfulIngredientsValue = findViewById(R.id.tvHarmfulIngredientsValue);

        mContainerNoValue = findViewById(R.id.etContainerNoValue);
        mWeightValue = findViewById(R.id.etWeightValue);
        mShelfNoValue = findViewById(R.id.etShelfNoValue);
        mOtherInfoValue = findViewById(R.id.etOtherInfoValue);


        String depositRecordJSON = getIntent().getStringExtra(KEY_ORG_VALUE);
        if (depositRecordJSON != null) {
            isNewCreate = false;
            mDepositRecord = CabinetCore.GSON.fromJson(depositRecordJSON, DepositRecord.class);
        } else {
            isNewCreate = true;
            mDepositRecord = new DepositRecord();
            mDepositRecord.operator = CabinetCore.getCabinetUser(CabinetCore.RoleType.Operator);
            mDepositRecord.cabinet = CabinetCore.getCabinetInfo();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        showDepositRecord();
    }

    private void showDepositRecord() {
        mContainerSpecValue.setText(mDepositRecord.container_spec);
        mSourceValue.setText(mDepositRecord.source != null?mDepositRecord.source.name:"");
        mHarmfulIngredientsValue.setText(mDepositRecord.harmful_ingredients);

        mContainerNoValue.setText(mDepositRecord.container_no_info.no);
        mWeightValue.setText(mDepositRecord.in_weight);
        mShelfNoValue.setText(mDepositRecord.shelf_no);
        mOtherInfoValue.setText(mDepositRecord.other_info);
    }

    public void onBackButtonClick(View view) {
        finish();
    }

    public void onWeightButtonClick(View view) {
        Intent intent = new Intent(this, WeightActivity.class);
        mWeightLauncher.launch(intent);
    }

    public void onSubmitButtonClick(View view) {
    }

    public void onContainerSpecButtonClick(View view) {
        Intent intent = new Intent(this, SpinnerActivity.class);
        List<String> options = new LinkedList<>();
        Collections.addAll(options, DefaultDict.ContainerSpecName);
        intent.putExtra(SpinnerActivity.KEY_OPTIONS, CabinetCore.GSON.toJson(options));
        intent.putExtra(SpinnerActivity.KEY_TIP_INFO, "请选择容器规格：");
        mContainerSpecSelectLauncher.launch(intent);
    }

    public void onHarmfulIngredientsValueClick(View view) {
        Intent intent = new Intent(this, MultiSpinnerActivity.class);
        Map<String, Boolean> options = createHarmfulIngredientMap(mDepositRecord.harmful_ingredients);
        intent.putExtra(MultiSpinnerActivity.KEY_OPTIONS, CabinetCore.GSON.toJson(options));
        intent.putExtra(MultiSpinnerActivity.KEY_TIP_INFO, "请选择有害成分：");
        mHarmfulIngredientSelectLauncher.launch(intent);
    }


}