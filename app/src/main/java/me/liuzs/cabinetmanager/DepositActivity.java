package me.liuzs.cabinetmanager;

import android.app.Activity;
import android.content.Context;
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
import androidx.appcompat.app.AlertDialog;

import com.kyleduo.switchbutton.SwitchButton;

import java.util.Collections;
import java.util.Date;
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
import me.liuzs.cabinetmanager.net.RemoteAPI;

/**
 * 存入模块
 */
public class DepositActivity extends BaseActivity implements TextWatcher, CompoundButton.OnCheckedChangeListener {

    private final List<Laboratory> mLaboratoryList = new LinkedList<>();
    private DepositRecord mDepositRecord;
    private TextView mContainerSpecValue, mSourceValue, mHarmfulIngredientsValue;
    private EditText mContainerNoValue, mWeightValue, mShelfNoValue, mOtherInfoValue;
    private Button mWeight, mSubmit;
    private final ActivityResultLauncher<Intent> mHarmfulIngredientSelectLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        int resultCode = result.getResultCode();
        if (resultCode == RESULT_OK) {
            Intent data = result.getData();
            assert data != null;
            String selectValue = data.getStringExtra(SpinnerActivity.KEY_SELECT_VALUE);
            assert selectValue != null;
            Map<String, Boolean> select = CabinetCore.GSON.fromJson(selectValue, MultiSpinnerActivity.JSON_TYPE);
            DepositActivity.this.mDepositRecord.harmful_infos = createHarmfulIngredientString(select);
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
            String selectIndex = data.getStringExtra(SpinnerActivity.KEY_SELECT_INDEX);
            assert selectIndex != null;
            DepositActivity.this.mDepositRecord.laboratory_id = mLaboratoryList.get(Integer.parseInt(selectIndex)).id;
            DepositActivity.this.mDepositRecord.laboratory = mLaboratoryList.get(Integer.parseInt(selectIndex)).name;
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
                    mDepositRecord.input_weight = String.valueOf(weight);
                } else {
                    showToast("称重结果异常!");
                }
                showDepositRecord();
            }
        }
    });

    private SwitchButton mOfflineModel;

    /**
     * @param context 上下文
     */
    public static void start(Context context) {
        Intent intent = new Intent(context, DepositActivity.class);
        if (!(context instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
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

        mSubmit = findViewById(R.id.btnOK);

        mContainerNoValue.addTextChangedListener(this);
        mWeightValue.addTextChangedListener(this);
        mShelfNoValue.addTextChangedListener(this);
        mOtherInfoValue.addTextChangedListener(this);

        getLaboratoryList();
    }

    private void getRemoteLaboratoryList() {
        showProgressDialog();
        getExecutorService().submit(() -> {
            APIJSON<Agency> agencyAPIJSON = RemoteAPI.System.getLaboratoryList();
            dismissProgressDialog();
            if (agencyAPIJSON.status == APIJSON.Status.ok) {
                if (agencyAPIJSON.data != null && agencyAPIJSON.data.laboratories != null && agencyAPIJSON.data.laboratories.size() > 0) {
                    List<Laboratory> laboratoryList = agencyAPIJSON.data.laboratories;
                    mLaboratoryList.clear();
                    mLaboratoryList.addAll(laboratoryList);
                    CabinetCore.saveLaboratoryListCache(mLaboratoryList);
                    initDepositRecord();
                } else {
                    showToast(getString(R.string.no_laboratory_tip));
                    finish();
                }
            } else {
                showToast(agencyAPIJSON.error);
                showModelSwitchDialog();
            }
        });
    }

    private void showModelSwitchDialog() {
        new AlertDialog.Builder(DepositActivity.this).setMessage(R.string.switch_offline_model_tip).setNegativeButton(R.string.ok, (dialog, which) -> mOfflineModel.setChecked(true)).setNeutralButton(R.string.cancel, (dialogInterface, i) -> finish()).show();
    }

    private void getLocalLaboratoryList() {
        List<Laboratory> laboratoryList = CabinetCore.getLaboratoryListCache();
        if (laboratoryList != null) {
            mLaboratoryList.clear();
            mLaboratoryList.addAll(laboratoryList);
            initDepositRecord();
        } else {
            showToast(getString(R.string.local_info_poor_tip));
            finish();
        }
    }

    private void getLaboratoryList() {
        if (mOfflineModel.isChecked()) {
            getLocalLaboratoryList();
        } else {
            getRemoteLaboratoryList();
        }
    }

    private void initDepositRecord() {
        mDepositRecord = new DepositRecord();
        mDepositRecord.storage_id = Objects.requireNonNull(CabinetCore.getCabinetInfo()).id;
        mDepositRecord.input_operator = Objects.requireNonNull(CabinetCore.getCabinetUser(CabinetCore.RoleType.Operator)).name;
        showDepositRecord();
    }

    private void getRemoteDepositRecord(String no) {
        showProgressDialog();
        getExecutorService().submit(() -> {
            APIJSON<DepositRecord> depositJSON = RemoteAPI.Deposit.getDeposit(no);
            dismissProgressDialog();
            if (depositJSON.status == APIJSON.Status.ok) {
                if (depositJSON.data == null || depositJSON.data.id == null) {
                    mDepositRecord.storage_no = no;
                } else {
                    DepositRecord record = depositJSON.data;
                    record.has_storage_rack = !TextUtils.isEmpty(record.storage_rack);
                    record.has_input_weight = !TextUtils.isEmpty(record.input_weight);
                    record.input_operator = mDepositRecord.input_operator;
                    mDepositRecord = record;
                    if (mDepositRecord.has_storage_rack) {
                        showToast("此单号已经入柜，无法再次提交.");
                        CabinetCore.saveDepositRecord(record);
                        reset();
                    }
                }
                showDepositRecord();
            } else if (depositJSON.status == APIJSON.Status.error) {
                showToast(depositJSON.error);
                reset();
            } else if (depositJSON.status == APIJSON.Status.other) {
                showToast(depositJSON.error);
                showModelSwitchDialog();
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
                mContainerSpecValue.setEnabled(!mDepositRecord.has_storage_rack);
                mSourceValue.setEnabled(!mDepositRecord.has_storage_rack);
                mHarmfulIngredientsValue.setEnabled(!mDepositRecord.has_storage_rack);
                mWeight.setEnabled(!mDepositRecord.has_input_weight);
                mWeightValue.setEnabled(!mDepositRecord.has_input_weight);
                mShelfNoValue.setEnabled(!mDepositRecord.has_storage_rack);
                mOtherInfoValue.setEnabled(!mDepositRecord.has_storage_rack);
            }

            mContainerSpecValue.setText(mDepositRecord.container_size != null ? mDepositRecord.container_size + "升" : null);
            if (TextUtils.isEmpty(mDepositRecord.laboratory)) {
                mSourceValue.setText(getLaboratoryName(mDepositRecord.laboratory_id));
            } else {
                mSourceValue.setText(mDepositRecord.laboratory);
                if (TextUtils.isEmpty(mDepositRecord.laboratory_id)) {
                    mDepositRecord.laboratory_id = getLaboratoryId(mDepositRecord.laboratory);
                }
            }

            mHarmfulIngredientsValue.setText(mDepositRecord.harmful_infos);

            mContainerNoValue.setText(mDepositRecord.storage_no);
            mWeightValue.setText(mDepositRecord.input_weight);
            mShelfNoValue.setText(mDepositRecord.storage_rack);
            mOtherInfoValue.setText(mDepositRecord.remark);
        });

    }

    private String getLaboratoryName(String laboratory_id) {
        for (Laboratory laboratory : mLaboratoryList) {
            if (TextUtils.equals(laboratory_id, laboratory.id)) {
                return laboratory.name;
            }
        }
        return null;
    }

    private String getLaboratoryId(String laboratory_name) {
        for (Laboratory laboratory : mLaboratoryList) {
            if (TextUtils.equals(laboratory_name, laboratory.name)) {
                return laboratory.id;
            }
        }
        return null;
    }

    public void onBackButtonClick(View view) {
        finish();
    }

    public void onResetButtonClick(View view) {
        reset();
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
        if (TextUtils.isEmpty(mDepositRecord.input_weight)) {
            return false;
        }
        if (TextUtils.isEmpty(mDepositRecord.laboratory_id)) {
            return false;
        }
        if (TextUtils.isEmpty(mDepositRecord.harmful_infos)) {
            return false;
        }
        if (TextUtils.isEmpty(mDepositRecord.container_size)) {
            return false;
        }
        return !TextUtils.isEmpty(mDepositRecord.storage_rack);
    }

    public void onSubmitButtonClick(View view) {
        if (!validateDeposit()) {
            showToast("请完善信息再提交");
            return;
        }
        if (mOfflineModel.isChecked()) {
            mDepositRecord.input_time = CabinetCore._SecondFormatter.format(new Date(System.currentTimeMillis()));
            CabinetCore.saveDepositRecord(mDepositRecord);
            showToast("提交成功");
            reset();
        } else {
            showProgressDialog();
            getExecutorService().submit(() -> {
                APIJSON<DepositRecord> apijson = RemoteAPI.Deposit.takeInDeposit(mDepositRecord);
                dismissProgressDialog();
                if (apijson.status == APIJSON.Status.ok) {
                    showToast("提交成功");
                    CabinetCore.saveDepositRecord(mDepositRecord);
                    reset();
                } else if (apijson.status == APIJSON.Status.error) {
                    showToast(apijson.error);
                    showModelSwitchDialog();
                } else {
                    showToast(apijson.error);
                    showModelSwitchDialog();
                }
            });
        }

    }

    private void reset() {
        initDepositRecord();
        showDepositRecord();
        mContainerNoValue.setText(null);
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
        Map<String, Boolean> options = createHarmfulIngredientMap(mDepositRecord.harmful_infos);
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
                    if (containerNo.startsWith("NO") && TextUtils.isDigitsOnly(containerNo.substring(2))) {
                        onContainerNoTextChange(containerNo);
                    } else {
                        showToast(getString(R.string.invalidate_no));
                    }
                }
            }
        } else if (editable == mWeightValue.getEditableText()) {
            String weightStr = editable.toString();
            if (!TextUtils.isEmpty(weightStr)) {
                try {
                    float weight = Float.parseFloat(weightStr);
                    mDepositRecord.input_weight = String.valueOf(weight);
                } catch (Exception e) {
                    mWeightValue.setText(null);
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
                    mShelfNoValue.setText(null);
                    showToast("货架号必须是数字！");
                }
            }
        } else if (editable == mOtherInfoValue.getEditableText()) {
            mDepositRecord.remark = editable.toString();
        }
    }

    private void onContainerNoTextChange(String containerNo) {
        if (mOfflineModel.isChecked()) {
            DepositRecord record = CabinetCore.getDepositRecord(containerNo);
            if (record != null) {
                showToast("次单号已经有记录，请勿重复提交.");
                reset();
            } else {
                showToast("离线存单将不校验数据.");
                mDepositRecord.storage_no = containerNo;
                showDepositRecord();
            }
        } else {
            getRemoteDepositRecord(containerNo);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton == mOfflineModel) {
            if (b) {
                mSubmit.setText(R.string.save);
            } else {
                mSubmit.setText(R.string.submit);
            }
            reset();

        }
    }
}