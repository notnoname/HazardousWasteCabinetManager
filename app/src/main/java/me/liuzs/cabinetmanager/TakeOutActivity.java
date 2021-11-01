package me.liuzs.cabinetmanager;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.kyleduo.switchbutton.SwitchButton;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import me.liuzs.cabinetmanager.model.DefaultDict;
import me.liuzs.cabinetmanager.model.DepositRecord;
import me.liuzs.cabinetmanager.net.APIJSON;
import me.liuzs.cabinetmanager.net.DepositRecordListJSON;
import me.liuzs.cabinetmanager.net.RemoteAPI;

/**
 * 存入模块
 */
public class TakeOutActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener, TextWatcher {

    public final static String TAG = "DepositActivity";
    public final static String KEY_ORG_VALUE = "KEY_ORG_VALUE";
    private DepositRecord mDepositRecord;
    private boolean isRestore = false;
    private TextView mContainerSpecValue, mSourceValue, mHarmfulIngredientsValue, mInWeightValue, mShelfNoValue, mOtherInfoValue;
    private EditText mContainerNoValue, mOutWeightValue;
    private Button mWeight;
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
                    mDepositRecord.input_weight = String.valueOf(weight);
                } else {
                    showToast("称重结果异常!");
                }
                showDepositRecord();
            }
        }
    });
    private SwitchButton mOfflineModel;

    public static void start(Context content, @Nullable String depositRecordJSON) {
        Intent intent = new Intent(content, TakeOutActivity.class);
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
        setContentView(R.layout.activity_take_out);

        mOfflineModel = findViewById(R.id.swOfflineModel);
        mOfflineModel.setOnCheckedChangeListener(this);

        mWeight = findViewById(R.id.btnWeight);

        mContainerSpecValue = findViewById(R.id.tvContainerSpecValue);
        mSourceValue = findViewById(R.id.tvSourceValue);
        mHarmfulIngredientsValue = findViewById(R.id.tvHarmfulIngredientsValue);
        mShelfNoValue = findViewById(R.id.tvShelfNoValue);
        mOtherInfoValue = findViewById(R.id.tvOtherInfoValue);
        mInWeightValue = findViewById(R.id.tvInWeightValue);

        mContainerNoValue = findViewById(R.id.etContainerNoValue);
        mOutWeightValue = findViewById(R.id.etOutWeightValue);

        mContainerNoValue.addTextChangedListener(this);
        mOutWeightValue.addTextChangedListener(this);

        String depositRecordJSON = getIntent().getStringExtra(KEY_ORG_VALUE);
        if (depositRecordJSON != null) {
            mDepositRecord = CabinetCore.GSON.fromJson(depositRecordJSON, DepositRecord.class);
            isRestore = true;
        } else {
            mDepositRecord = new DepositRecord();
            mDepositRecord.storage_id = Objects.requireNonNull(CabinetCore.getCabinetInfo()).id;
            isRestore = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        showDepositRecord();
    }

    private void showDepositRecord() {
        mHandler.post(() -> {
            if (TextUtils.isEmpty(mDepositRecord.storage_no)) {
                mContainerNoValue.setEnabled(true);
                mWeight.setEnabled(false);
                mOutWeightValue.setEnabled(false);
            } else {
                mContainerNoValue.setEnabled(false);
                mWeight.setEnabled(mDepositRecord.has_storage_rack);
                mOutWeightValue.setEnabled(mDepositRecord.has_storage_rack);
            }

            mContainerSpecValue.setText(mDepositRecord.container_size != null ? mDepositRecord.container_size + "升" : null);
            mSourceValue.setText(mDepositRecord.laboratory);

            mHarmfulIngredientsValue.setText(mDepositRecord.harmful_infos);

            mContainerNoValue.setText(mDepositRecord.storage_no);
            mInWeightValue.setText(mDepositRecord.input_weight);
            mOutWeightValue.setText(mDepositRecord.output_weight);
            mShelfNoValue.setText(mDepositRecord.storage_rack);
            mOtherInfoValue.setText(mDepositRecord.remark);
        });

    }

    public void onBackButtonClick(View view) {
        finish();
    }

    public void onWeightButtonClick(View view) {
        Intent intent = new Intent(this, WeightActivity.class);
        mWeightLauncher.launch(intent);
    }

    private boolean validateDeposit() {
        if (mContainerNoValue.isEnabled()) {
            return false;
        }
        if (TextUtils.isEmpty(mDepositRecord.input_weight)) {
            return false;
        }
        if (TextUtils.isEmpty(mDepositRecord.laboratory)) {
            return false;
        }
        if (TextUtils.isEmpty(mDepositRecord.container_size)) {
            return false;
        }
        if (TextUtils.isEmpty(mDepositRecord.storage_rack)) {
            return false;
        }
        if (TextUtils.isEmpty(mDepositRecord.output_weight)) {
            return false;
        }
        return true;
    }

    public void onSubmitButtonClick(View view) {
        if (validateDeposit()) {
            showProgressDialog();
            getExecutorService().submit(() -> {
                APIJSON<DepositRecord> apijson = RemoteAPI.Deposit.takeOutDeposit(mDepositRecord);
                dismissProgressDialog();
                if (apijson.status == APIJSON.Status.ok) {
                    showToast("提交成功");
                    reset();
                } else {
                    showToast(apijson.error);
                }
            });
        } else {
            showToast("请完善信息再提交！");
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

    }

    public void onResetButtonClick(View view) {
        reset();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (editable == mContainerNoValue.getEditableText()) {
            if (mContainerNoValue.isEnabled()) {
                String containerNo = editable.toString();
                if (containerNo.length() >= 20) {
                    if (containerNo.startsWith("NO")) {
                        getDeposit(containerNo);
                    } else {
                        mContainerNoValue.setText(null);
                        showToast("暂存号不正确！");
                    }
                }
            }
        } else if (editable == mOutWeightValue.getEditableText()) {
            String weightStr = editable.toString();
            if (!TextUtils.isEmpty(weightStr)) {
                try {
                    float weight = Float.parseFloat(weightStr);
                    mDepositRecord.output_weight = String.valueOf(weight);
                } catch (Exception e) {
                    mOutWeightValue.setText(null);
                    showToast("请输入正确的入柜重量！");
                }
            }
        }
    }

    private void getDeposit(String no) {
        showProgressDialog();
        getExecutorService().submit(() -> {
            APIJSON<DepositRecordListJSON> depositJSON = RemoteAPI.Deposit.getDeposit(no, 20, 1);
            dismissProgressDialog();
            if (depositJSON.status == APIJSON.Status.ok) {
                if (depositJSON.data != null) {
                    if (depositJSON.data.storage_records == null || depositJSON.data.storage_records.size() == 0) {
                        showToast("此单号无暂存记录!");
                    } else {
                        if (isRestore) {

                        } else {
                            DepositRecord record = depositJSON.data.storage_records.get(0);
                            mDepositRecord.storage_no = record.storage_no;
                            mDepositRecord.input_weight = record.input_weight;
                            mDepositRecord.output_weight = record.input_weight;
                            mDepositRecord.remark = record.remark;
                            mDepositRecord.laboratory_id = record.laboratory_id;
                            mDepositRecord.laboratory = record.laboratory;
                            mDepositRecord.harmful_infos = record.harmful_infos;
                            mDepositRecord.storage_rack = record.storage_rack;
                            mDepositRecord.container_size = record.container_size;
                            mDepositRecord.has_storage_rack = !TextUtils.isEmpty(record.storage_rack);
                            mDepositRecord.has_input_weight = !TextUtils.isEmpty(record.input_weight);
                        }
                    }
                } else {
                    mContainerNoValue.setText(null);
                    showToast("单号错误！");
                }
                showDepositRecord();
            } else if (depositJSON.status == APIJSON.Status.error) {
                mContainerNoValue.setText(null);
                showToast("单号错误！");
                showDepositRecord();
            } else {
                showToast(depositJSON.error);
                new AlertDialog.Builder(TakeOutActivity.this).setMessage("获取实验列表失败，是否切换为离线模式？").setNegativeButton("确认", (dialog, which) -> {
                    mOfflineModel.setChecked(true);
                    showDepositRecord();
                }).setNeutralButton("取消", (dialogInterface, i) -> finish()).show();
            }
        });
    }
    private void reset() {
        mDepositRecord = new DepositRecord();
        mDepositRecord.storage_id = Objects.requireNonNull(CabinetCore.getCabinetInfo()).id;
        isRestore = false;
        mOfflineModel.setChecked(false);
        showDepositRecord();
    }
}