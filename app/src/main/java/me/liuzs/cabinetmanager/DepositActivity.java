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

import me.liuzs.cabinetmanager.model.Agency;
import me.liuzs.cabinetmanager.model.DefaultDict;
import me.liuzs.cabinetmanager.model.DepositRecord;
import me.liuzs.cabinetmanager.model.Laboratory;
import me.liuzs.cabinetmanager.net.APIJSON;
import me.liuzs.cabinetmanager.net.DepositRecordListJSON;
import me.liuzs.cabinetmanager.net.RemoteAPI;

/**
 * 存入模块
 */
public class DepositActivity extends BaseActivity implements TextWatcher, CompoundButton.OnCheckedChangeListener {

    public final static String TAG = "DepositActivity";
    public final static String KEY_ORG_VALUE = "KEY_ORG_VALUE";
    private DepositRecord mDepositRecord;
    private boolean isRestore = false;
    private TextView mContainerSpecValue, mSourceValue, mHarmfulIngredientsValue;
    private EditText mContainerNoValue, mWeightValue, mShelfNoValue, mOtherInfoValue;
    private Button mWeight;
    private SwitchButton mOfflineModel;
    private final List<Laboratory> mLaboratoryList = new LinkedList<>();
    private final ActivityResultLauncher<Intent> mHarmfulIngredientSelectLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        int resultCode = result.getResultCode();
        if (resultCode == RESULT_OK) {
            Intent data = result.getData();
            assert data != null;
            String selectValue = data.getStringExtra(SpinnerActivity.KEY_SELECT_VALUE);
            assert selectValue != null;
            Map<String, Boolean> select = CabinetCore.GSON.fromJson(selectValue, MultiSpinnerActivity.JSON_TYPE);
            DepositActivity.this.mDepositRecord.harmful_info = createHarmfulIngredientString(select);
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
            DepositActivity.this.mDepositRecord.container_size = selectValue.substring(0, selectValue.length() - 1);
            showDepositRecord();
        }
    });

    private final ActivityResultLauncher<Intent> mSourceSelectLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        int resultCode = result.getResultCode();
        if (resultCode == RESULT_OK) {
            Intent data = result.getData();
            assert data != null;
            String selectValue = data.getStringExtra(SpinnerActivity.KEY_SELECT_INDEX);
            assert selectValue != null;
            DepositActivity.this.mDepositRecord.laboratory_id = mLaboratoryList.get(Integer.parseInt(selectValue)).code;
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

    /**
     * @param content           上线文
     * @param depositRecordJSON 离线数据
     */
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

        mOfflineModel = findViewById(R.id.swOfflineModel);
        mOfflineModel.setOnCheckedChangeListener(this);

        mWeight = findViewById(R.id.btnWeight);

        mContainerSpecValue = findViewById(R.id.tvContainerSpecValue);
        mSourceValue = findViewById(R.id.tvSourceValue);
        mHarmfulIngredientsValue = findViewById(R.id.tvHarmfulIngredientsValue);

        mContainerNoValue = findViewById(R.id.etContainerNoValue);
        mWeightValue = findViewById(R.id.etWeightValue);
        mShelfNoValue = findViewById(R.id.etShelfNoValue);
        mOtherInfoValue = findViewById(R.id.etOtherInfoValue);

        mContainerNoValue.addTextChangedListener(this);
        mWeightValue.addTextChangedListener(this);
        mShelfNoValue.addTextChangedListener(this);
        mOtherInfoValue.addTextChangedListener(this);

        String depositRecordJSON = getIntent().getStringExtra(KEY_ORG_VALUE);
        if (depositRecordJSON != null) {
            mDepositRecord = CabinetCore.GSON.fromJson(depositRecordJSON, DepositRecord.class);
            isRestore = true;
        } else {
            mDepositRecord = new DepositRecord();
            mDepositRecord.storage_id = Objects.requireNonNull(CabinetCore.getCabinetInfo()).id;
            isRestore = false;
        }
        getLaboratoryList();
    }

    private void getLaboratoryList() {
        showProgressDialog();
        getExecutorService().submit(() -> {
            APIJSON<Agency> agencyAPIJSON = RemoteAPI.System.getLaboratoryList();
            dismissProgressDialog();
            if (agencyAPIJSON.status == APIJSON.Status.ok) {
                if (agencyAPIJSON.data != null && agencyAPIJSON.data.laboratories != null && agencyAPIJSON.data.laboratories.size() > 0) {
                    List<Laboratory> laboratoryList = agencyAPIJSON.data.laboratories;
                    mLaboratoryList.clear();
                    mLaboratoryList.addAll(laboratoryList);
                    showDepositRecord();
                } else {
                    showToast("机构下无实验室，请去平台创建！");
                    finish();
                }
            } else {
                showToast(agencyAPIJSON.errors);
                new AlertDialog.Builder(DepositActivity.this).setMessage("获取实验列表失败，是否切换为离线模式？").setNegativeButton("确认", (dialog, which) -> {
                    mOfflineModel.setChecked(true);
                    showDepositRecord();
                }).setNeutralButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }).show();
            }
        });
    }

    private void getDeposit(String no) {
        showProgressDialog();
        getExecutorService().submit(() -> {
            APIJSON<DepositRecordListJSON> depositJSON = RemoteAPI.Deposit.getDeposit(no);
            dismissProgressDialog();
            if (depositJSON.status == APIJSON.Status.ok) {
                if (depositJSON.data != null) {
                    if (depositJSON.data.storages == null || depositJSON.data.storages.size() == 0) {
                        if (isRestore) {

                        } else {
                            mDepositRecord.storage_no = no;
                        }
                    } else {
                        DepositRecord record = depositJSON.data.storages.get(0);
                        mDepositRecord.in_weight = record.in_weight;
                        mDepositRecord.remark = record.remark;
                        mDepositRecord.laboratory_id = record.laboratory_id;
                        mDepositRecord.harmful_info = record.harmful_info;
                        mDepositRecord.storage_rack = record.storage_rack;
                        mDepositRecord.container_size = record.container_size;
                    }
                } else {
                    mContainerNoValue.setText("");
                    showToast("单号错误！");
                }
                showDepositRecord();
            } else if (depositJSON.status == APIJSON.Status.error) {
                mContainerNoValue.setText("");
                showToast("单号错误！");
                showDepositRecord();
            } else {
                showToast(depositJSON.errors);
                new AlertDialog.Builder(DepositActivity.this).setMessage("获取实验列表失败，是否切换为离线模式？").setNegativeButton("确认", (dialog, which) -> {
                    mOfflineModel.setChecked(true);
                    showDepositRecord();
                }).setNeutralButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void showDepositRecord() {
        mHandler.post(() -> {
            if (TextUtils.isEmpty(mDepositRecord.storage_no)) {
                mContainerNoValue.setEnabled(true);
                mContainerSpecValue.setEnabled(false);
                mSourceValue.setEnabled(false);
                mHarmfulIngredientsValue.setEnabled(false);
                mWeight.setEnabled(false);
                mWeightValue.setEnabled(false);
                mShelfNoValue.setEnabled(false);
                mOtherInfoValue.setEnabled(false);
            } else {
                mContainerNoValue.setEnabled(false);
                mContainerSpecValue.setEnabled(true);
                mSourceValue.setEnabled(true);
                mHarmfulIngredientsValue.setEnabled(true);
                mWeight.setEnabled(true);
                mWeightValue.setEnabled(true);
                mShelfNoValue.setEnabled(true);
                mOtherInfoValue.setEnabled(true);
            }

            mContainerSpecValue.setText(mDepositRecord.container_size + "升");
            mSourceValue.setText(getLaboratoryName(mDepositRecord.laboratory_id));

            mHarmfulIngredientsValue.setText(mDepositRecord.harmful_info);

            mContainerNoValue.setText(mDepositRecord.storage_no);
            mWeightValue.setText(mDepositRecord.in_weight);
            mShelfNoValue.setText(mDepositRecord.storage_rack);
            mOtherInfoValue.setText(mDepositRecord.remark);
        });

    }

    private String getLaboratoryName(String laboratory_id) {
        for (Laboratory laboratory : mLaboratoryList) {
            if (TextUtils.equals(laboratory_id, laboratory.code)) {
                return laboratory.name;
            }
        }
        return null;
    }

    public void onBackButtonClick(View view) {
        finish();
    }

    public void onResetButtonClick(View view) {

    }

    public void onWeightButtonClick(View view) {
        Intent intent = new Intent(this, WeightActivity.class);
        mWeightLauncher.launch(intent);
    }

    public void onSourceButtonClick(View view) {
        Intent intent = new Intent(this, SpinnerActivity.class);
        List<String> options = new LinkedList<>();
        for (Laboratory laboratory : mLaboratoryList) {
            options.add(laboratory.name);
        }
        intent.putExtra(SpinnerActivity.KEY_OPTIONS, CabinetCore.GSON.toJson(options));
        intent.putExtra(SpinnerActivity.KEY_TIP_INFO, "请选择危废来源：");
        mSourceSelectLauncher.launch(intent);
    }

    private boolean validateDeposit() {
        if (mContainerNoValue.isEnabled()) {
            return false;
        }
        if (TextUtils.isEmpty(mDepositRecord.in_weight)) {
            return false;
        }
        if (TextUtils.isEmpty(mDepositRecord.laboratory_id)) {
            return false;
        }
        if (TextUtils.isEmpty(mDepositRecord.harmful_info)) {
            return false;
        }
        if (TextUtils.isEmpty(mDepositRecord.container_size)) {
            return false;
        }
        if (TextUtils.isEmpty(mDepositRecord.storage_rack)) {
            return false;
        }
        return true;
    }

    public void onSubmitButtonClick(View view) {
        if (validateDeposit()) {
            showProgressDialog();
            getExecutorService().submit(() -> {
                APIJSON<String> apijson = RemoteAPI.Deposit.submitDeposit(mDepositRecord);
                dismissProgressDialog();
                if (apijson.status == APIJSON.Status.ok) {
                    showToast("提交成功");
                } else {
                    showToast(apijson.errors);
                }
            });
        } else {
            showToast("请完善信息再提交！");
        }
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
        Map<String, Boolean> options = createHarmfulIngredientMap(mDepositRecord.harmful_info);
        intent.putExtra(MultiSpinnerActivity.KEY_OPTIONS, CabinetCore.GSON.toJson(options));
        intent.putExtra(MultiSpinnerActivity.KEY_TIP_INFO, "请选择有害成分：");
        mHarmfulIngredientSelectLauncher.launch(intent);
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
                        mContainerNoValue.setText("");
                        showToast("暂存号不正确！");
                    }
                }
            }
        } else if (editable == mWeightValue.getEditableText()) {
            String weightStr = editable.toString();
            if (!TextUtils.isEmpty(weightStr)) {
                try {
                    float weight = Float.parseFloat(weightStr);
                    mDepositRecord.in_weight = String.valueOf(weight);
                } catch (Exception e) {
                    mWeightValue.setText("");
                    showToast("请输入正确的入柜重量！");
                }
            }
        } else if (editable == mShelfNoValue.getEditableText()) {
            String shelfNoStr = editable.toString();
            if (!TextUtils.isEmpty(shelfNoStr)) {
                try {
                    int shelfNo = Integer.parseInt(shelfNoStr);
                    mDepositRecord.storage_rack = String.valueOf(shelfNo);
                } catch (Exception e) {
                    mShelfNoValue.setText("");
                    showToast("货架号必须是数字！");
                }
            }
        } else if (editable == mOtherInfoValue.getEditableText()) {
            mDepositRecord.remark = editable.toString();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        showDepositRecord();
    }
}