package me.liuzs.cabinetmanager;

import static me.liuzs.cabinetmanager.ui.NewProgressDialog.THEME_CIRCLE_PROGRESS;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.feasycom.bean.BluetoothDeviceWrapper;
import com.puty.sdk.PrinterInstance;
import com.puty.sdk.callback.DeviceFoundImp;
import com.puty.sdk.callback.PrinterInstanceApi;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.liuzs.cabinetmanager.printer.DpUtil;
import me.liuzs.cabinetmanager.printer.PrinterBluetoothInfo;
import me.liuzs.cabinetmanager.printer.RecycleViewDivider;
import me.liuzs.cabinetmanager.printer.StringListAdapter;
import me.liuzs.cabinetmanager.ui.NewProgressDialog;
import me.liuzs.cabinetmanager.util.Util;

public class DeviceBluetoothListActivity extends BaseActivity {

    @BindView(R.id.toolbar_title)
    TextView mTVTitle;
    @BindView(R.id.rv_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.srl_swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    private StringListAdapter mListAdapter;
    private PrinterInstanceApi mPrinterInstance;

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_bluetooth_list);
        Util.fullScreen(this);
        ButterKnife.bind(this);
        mTVTitle.setText(R.string.connect_the_printer_machine);
        initRecyclerView();
        mPrinterInstance = PrinterInstance.getInstance();
        if (mPrinterInstance != null) {
            setCallBacks();
        }
        initBluetooth();
        ;
    }

    private void initBluetooth() {//???????????????
        //????????????????????????
        if (!mPrinterInstance.isBtEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(intent);
        } else {
            //????????????
            searchBluetooth();
        }
    }

    void searchBluetooth() {//????????????
        if (mPrinterInstance.isBtEnabled() == false) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 2);
        }
        mSwipeRefreshLayout.setRefreshing(true);
        mPrinterInstance.startScan(5 * 1000);//????????????   5???????????????    ?????????8???
        mSwipeRefreshLayout.postDelayed(new Runnable() {//???????????????????????????
            @Override
            public void run() {
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(false);//????????????
                }
            }
        }, 3 * 1000);
    }

    /**
     * ??????????????????
     */
    private void setCallBacks() {
        mPrinterInstance.setDeviceFoundImp(new DeviceFoundImp() {
            @Override
            public void sppDeviceFound(BluetoothDeviceWrapper device, int rssi) {//??????????????????
                if (device == null) return;
                String deviceName = device.getName();
                if (TextUtils.isEmpty(deviceName)) return;
                //????????????
                if (!device.getName().toLowerCase().startsWith("pt") &&
                        !device.getName().toLowerCase().startsWith("c") &&
                        !device.getName().toLowerCase().startsWith("s") &&
                        !device.getName().toLowerCase().startsWith("t") &&
                        !device.getName().toLowerCase().startsWith("k") &&
                        !device.getName().toLowerCase().startsWith("id") &&
                        !device.getName().toLowerCase().startsWith("op38")) {//id???????????????????????????????????????&&!device.getName().toLowerCase().startsWith("id")
                    return;
                } else {
                    addBluetoothBean(new PrinterBluetoothInfo(device.getName(), device.getAddress()));
                }
            }

            @Override
            public void sppConnected(BluetoothDevice device) {//????????????????????????
                PrinterBluetoothInfo info = new PrinterBluetoothInfo(device.getName(), device.getAddress());
                PrintActivity.CurrentPrinterInfo = info;
                CabinetCore.saveConnectedPrinterInfo(CabinetApplication.getInstance(), info);
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
                finish();
            }
        });
    }

    void addBluetoothBean(PrinterBluetoothInfo bean) {//??????????????????????????????????????????
        if (mRecyclerView != null && bean != null) {
            boolean isEqual = false;
            for (int i = 0; i < mListAdapter.getItemCount(); i++) {
                if (bean.address.equals(mListAdapter.getData().get(i).address)) {
                    isEqual = true;
                    break;
                }
            }
            if (!isEqual) {
                mListAdapter.addData(bean);
                mListAdapter.notifyDataSetChanged();
            }
        }
    }

    private void initRecyclerView() {
        mSwipeRefreshLayout.setColorSchemeColors(Color.rgb(47, 223, 189));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager.HORIZONTAL, DpUtil.dip2px(this, 1), 0xffe5e5e5));
        mListAdapter = new StringListAdapter();
        mRecyclerView.setAdapter(mListAdapter);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                searchBluetooth();//????????????
            }
        });
        mListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                connectBluetooth(mListAdapter.getItem(position));//????????????
            }
        });
    }

    public void connectBluetooth(PrinterBluetoothInfo info) {//????????????
        if (info == null || TextUtils.isEmpty(info.address) ||
                (PrintActivity.CurrentPrinterInfo != null && info.address.equals(PrintActivity.CurrentPrinterInfo.address))) {//???????????????????????????
            new AlertDialog.Builder(this).setMessage("????????????????????????????????????????????????????????????").setNegativeButton("??????",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mPrinterInstance.closeConnection();
                            PrintActivity.CurrentPrinterInfo = null;
                            //????????????????????????????????????????????????
                            new Handler().postDelayed(() -> {
                                connectBluetooth(info);
                            }, 400);
                        }
                    }).setNeutralButton("??????", null).show();
            return;
        }
        mPrinterInstance.closeConnection();//????????????
        PrintActivity.CurrentPrinterInfo = null;
        if (mProgressDialog == null) {
            mProgressDialog = new NewProgressDialog(this, getString(R.string.connection), THEME_CIRCLE_PROGRESS);
        } else {
            mProgressDialog.dismiss();
        }
        //????????????????????????????????????????????????
        new Handler().postDelayed(() -> {
            mProgressDialog.show();
            mPrinterInstance.connect(info.address);//????????????
        }, 400);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPrinterInstance.setDeviceFoundImp(null);
    }

    public void onBackButtonClick(View view) {
        finish();
    }
}
