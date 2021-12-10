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

import java.util.Date;
import java.util.Objects;

import me.liuzs.cabinetmanager.model.DepositRecord;
import me.liuzs.cabinetmanager.net.APIJSON;
import me.liuzs.cabinetmanager.net.DepositRecordListJSON;
import me.liuzs.cabinetmanager.net.RemoteAPI;

/**
 * 存入模块
 */
public class TakeOutActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener, TextWatcher {

    private DepositRecord mDepositRecord;
    private TextView mContainerSpecValue, mSourceValue, mHarmfulIngredientsValue, mInWeightValue, mShelfNoValue, mOtherInfoValue;
    private EditText mContainerNoValue, mOutWeightValue;
    private Button mWeight, mSubmit;
    private SwitchButton mOfflineModel;
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

    /**
     * @param context 上下文
     */
    public static void start(Context context) {
        Intent intent = new Intent(context, TakeOutActivity.class);
        if (!(context instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
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

        mSubmit = findViewById(R.id.btnOK);

        mContainerNoValue.addTextChangedListener(this);
        mOutWeightValue.addTextChangedListener(this);
        mShelfNoValue.addTextChangedListener(this);
        mOtherInfoValue.addTextChangedListener(this);

        reset();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void showDepositRecord() {
        mHandler.post(() -> {
            if (TextUtils.isEmpty(mDepositRecord.storage_no)) {
                mContainerNoValue.setEnabled(true);
                mWeight.setEnabled(false);
                mOutWeightValue.setEnabled(false);
            } else {
                mContainerNoValue.setEnabled(false);
                mWeight.setEnabled(!mDepositRecord.has_output_weight);
                mOutWeightValue.setEnabled(!mDepositRecord.has_output_weight);
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
        if (!validateDeposit()) {
            showToast("请完善信息再提交.");
            return;
        }
        if (mOfflineModel.isChecked()) {
            mDepositRecord.output_time = CabinetCore._SecondFormatter.format(new Date(System.currentTimeMillis()));
            CabinetCore.saveDepositRecord(mDepositRecord);
            showToast("提交成功");
            reset();
        } else {
            showProgressDialog();
            getExecutorService().submit(() -> {
                APIJSON<DepositRecord> apijson = RemoteAPI.Deposit.takeOutDeposit(mDepositRecord);
                dismissProgressDialog();
                if (apijson.status == APIJSON.Status.ok) {
                    showToast("出柜成功");
                    mDepositRecord.isSent = true;
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

    public void onResetButtonClick(View view) {
        reset();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    private void onContainerNoTextChange(String containerNo) {
        if (mOfflineModel.isChecked()) {
            DepositRecord record = CabinetCore.getDepositRecord(containerNo);
            if (record != null) {
                record.has_storage_rack = !TextUtils.isEmpty(record.storage_rack);
                record.has_input_weight = !TextUtils.isEmpty(record.input_weight);
                record.has_output_weight = !TextUtils.isEmpty(record.output_weight);
                if(record.has_output_weight) {
                    showToast("此单号已经有记录，请勿重复提交.");
                    reset();
                } else {
                    record.output_operator = mDepositRecord.output_operator;
                    mDepositRecord = record;
                    showToast("离线存单将不校验数据.");
                    showDepositRecord();
                }
            } else {
                showToast("本机未查询到入柜记录,请先入柜.");
                reset();
            }
        } else {
            getRemoteDepositRecord(containerNo);
        }
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
        } else if (editable == mOutWeightValue.getEditableText()) {
            String weightStr = editable.toString();
            if (!TextUtils.isEmpty(weightStr)) {
                try {
                    float weight = Float.parseFloat(weightStr);
                    mDepositRecord.output_weight = String.valueOf(weight);
                    if (mOfflineModel.isChecked()) {
                        if (!mDepositRecord.has_input_weight) {
                            mDepositRecord.input_weight = mDepositRecord.output_weight;
                            mInWeightValue.setText(mDepositRecord.input_weight);
                        }
                    }
                } catch (Exception e) {
                    mOutWeightValue.setText(null);
                    showToast("请输入正确的出柜重量！");
                }
            }
        } else if (editable == mShelfNoValue.getEditableText()) {
            if (mOfflineModel.isChecked()) {
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
            }
        } else if (editable == mOtherInfoValue.getEditableText()) {
            if (mOfflineModel.isChecked()) {
                mDepositRecord.remark = editable.toString();
            }
        }
    }

    private void getRemoteDepositRecord(String no) {
        showProgressDialog();
        getExecutorService().submit(() -> {
            APIJSON<DepositRecord> depositJSON = RemoteAPI.Deposit.getDeposit(no);
            dismissProgressDialog();
            if (depositJSON.status == APIJSON.Status.ok) {
                if (depositJSON.data == null || depositJSON.data.id == null) {
                    showToast("未查询到入柜记录,请先入柜.");
                    reset();
                } else {
                    DepositRecord record = depositJSON.data;
                    record.has_storage_rack = !TextUtils.isEmpty(record.storage_rack);
                    record.has_input_weight = !TextUtils.isEmpty(record.input_weight);
                    record.has_output_weight = !TextUtils.isEmpty(record.output_weight);
                    if(!record.has_storage_rack) {
                        showToast("未查询到入柜记录,请先入柜.");
                    } else if(record.has_output_weight) {
                        showToast("此单号已经出柜，请勿重复出柜.");
                    } else {
                        record.output_operator = mDepositRecord.output_operator;
                        record.output_weight = record.input_weight;
                        mDepositRecord = record;
                        showDepositRecord();
                    }
                }

            } else if (depositJSON.status == APIJSON.Status.error) {
                showToast("单号错误.");
                reset();
            } else {
                showToast(depositJSON.error);
                showModelSwitchDialog();
            }
        });
    }

    private void showModelSwitchDialog() {
        new AlertDialog.Builder(TakeOutActivity.this).setMessage(R.string.switch_offline_model_tip).setNegativeButton(R.string.ok, (dialog, which) -> mOfflineModel.setChecked(true)).setNeutralButton(R.string.cancel, (dialogInterface, i) -> finish()).show();
    }

    private void reset() {
        initDepositRecord();
        mContainerNoValue.setText(null);
        showDepositRecord();
    }

    private void initDepositRecord() {
        mDepositRecord = new DepositRecord();
        mDepositRecord.storage_id = Objects.requireNonNull(CabinetCore.getCabinetInfo()).id;
        mDepositRecord.output_operator = Objects.requireNonNull(CabinetCore.getCabinetUser(CabinetCore.RoleType.Operator)).name;

    }
}