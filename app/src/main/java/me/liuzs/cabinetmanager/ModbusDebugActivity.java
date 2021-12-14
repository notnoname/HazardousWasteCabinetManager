package me.liuzs.cabinetmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.serotonin.modbus4j.code.DataType;

import me.liuzs.cabinetmanager.service.ModbusService;
import me.liuzs.cabinetmanager.util.Util;

public class ModbusDebugActivity extends BaseActivity {


    public static final String TAG = "ModbusDebugActivity";
    private EditText mInfo, mReadNumberAddress, mReadBooleanAddress, mWriteNumberAddress, mWriteBooleanAddress, mWriteNumber, mWriteBoolean;

    public static void start(Context context) {
        Intent intent = new Intent(context, ModbusDebugActivity.class);
        if (!(context instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modbus_debug);
        mInfo = findViewById(R.id.etInfo);
        mReadNumberAddress = findViewById(R.id.etReadNumberAddress);
        mReadBooleanAddress = findViewById(R.id.etReadBooleanAddress);
        mWriteNumberAddress = findViewById(R.id.etWriteNumberAddress);
        mWriteNumber = findViewById(R.id.etWriteNumber);
        mWriteBooleanAddress = findViewById(R.id.etWriteBooleanAddress);
        mWriteBoolean = findViewById(R.id.etWriteBoolean);
    }

    private void outputNetworkAddressToInfo(StringBuilder sb) {
        sb.append("Local network address:").append("\n");
        String[] address = Util.getAllNetInterface();
        for (String add : address) {
            sb.append(add).append("\n");
        }
    }

    public void onReadNumberButtonClick(View view) {
        getExecutorService().submit(() -> {
            StringBuilder sb = new StringBuilder();
            try {
                sb.append("Modbus read number").append("\n");
                outputNetworkAddressToInfo(sb);
                sb.append("Modbus Address:").append(ModbusService.getModbusIP()).append("\n");
                int offSet = Integer.parseInt(mReadNumberAddress.getEditableText().toString());
                sb.append("Offset:").append(offSet);
                sb.append("\n");
                Number number = ModbusService.readHoldingRegister(offSet - 1, DataType.TWO_BYTE_INT_SIGNED);
                sb.append("Result:").append(number.toString());
                sb.append("\n");
            } catch (Exception e) {
                sb.append("Modbus read number exception:").append("\n").append(e.getMessage()).append("\n");
            } finally {
                mHandler.post(() -> mInfo.setText(sb.toString()));
                //showToast(sb.toString());
            }
        });
    }

    public void onReadBooleanButtonClick(View view) {
        getExecutorService().submit(() -> {
            StringBuilder sb = new StringBuilder();
            try {
                sb.append("Modbus read boolean").append("\n");
                outputNetworkAddressToInfo(sb);
                sb.append("Modbus address:").append(ModbusService.getModbusIP()).append("\n");
                int offSet = Integer.parseInt(mReadBooleanAddress.getEditableText().toString());
                sb.append("Offset:").append(offSet);
                sb.append("\n");
                Boolean b = ModbusService.readCoilStatus(offSet - 1);
                sb.append("Result:").append(b.toString());
                sb.append("\n");
            } catch (Exception e) {
                sb.append("Modbus read boolean exception:").append("\n").append(e.getMessage()).append("\n");
            } finally {
                mHandler.post(() -> mInfo.setText(sb.toString()));
                //showToast(sb.toString());
            }
        });
    }

    public void onWriteNumberButtonClick(View view) {
        getExecutorService().submit(() -> {
            StringBuilder sb = new StringBuilder();
            try {
                sb.append("Modbus write number").append("\n");
                outputNetworkAddressToInfo(sb);
                sb.append("Modbus address:").append(ModbusService.getModbusIP()).append("\n");
                int offSet = Integer.parseInt(mWriteNumberAddress.getEditableText().toString());
                sb.append("Offset:").append(offSet);
                sb.append("\n");
                Integer value = Integer.parseInt(mWriteNumber.getEditableText().toString());
                sb.append("Value:").append(value.toString());
                sb.append("\n");
                ModbusService.writeHoldingRegister(offSet - 1, value);
            } catch (Exception e) {
                sb.append("Modbus write number exception:").append("\n").append(e.getMessage()).append("\n");
            } finally {
                mHandler.post(() -> mInfo.setText(sb.toString()));
                //showToast(sb.toString());
            }
        });
    }

    public void onWriteBooleanButtonClick(View view) {
        getExecutorService().submit(() -> {
            StringBuilder sb = new StringBuilder();
            try {
                sb.append("Modbus write boolean").append("\n");
                outputNetworkAddressToInfo(sb);
                sb.append("Modbus address:").append(ModbusService.getModbusIP()).append("\n");
                int offSet = Integer.parseInt(mWriteBooleanAddress.getEditableText().toString());
                sb.append("Offset:").append(offSet);
                sb.append("\n");
                int value = Integer.parseInt(mWriteBoolean.getEditableText().toString());
                Boolean b = value == 0 ? Boolean.FALSE : Boolean.TRUE;
                sb.append("Value:").append(b.toString());
                sb.append("\n");
                ModbusService.writeCoilStatus(offSet - 1, b);
                sb.append("Write success").append("\n");
            } catch (Exception e) {
                sb.append("Modbus write boolean exception:").append("\n").append(e.getMessage()).append("\n");
            } finally {
                mHandler.post(() -> mInfo.setText(sb.toString()));
                //showToast(sb.toString());
            }
        });
    }

    public void onBackButtonClick(View view) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}