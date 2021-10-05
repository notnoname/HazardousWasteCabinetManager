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

    private void initBluetooth() {//初始化蓝牙
        //检测蓝牙是否打开
        if (!mPrinterInstance.isBtEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(intent);
        } else {
            //搜索蓝牙
            searchBluetooth();
        }
    }

    void searchBluetooth() {//搜索蓝牙
        if (mPrinterInstance.isBtEnabled() == false) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 2);
        }
        mSwipeRefreshLayout.setRefreshing(true);
        mPrinterInstance.startScan(5 * 1000);//搜索蓝牙   5秒自动关闭    默认是8秒
        mSwipeRefreshLayout.postDelayed(new Runnable() {//五秒后关闭搜索提示
            @Override
            public void run() {
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(false);//完成刷新
                }
            }
        }, 3 * 1000);
    }

    /**
     * 设置蓝牙监听
     */
    private void setCallBacks() {
        mPrinterInstance.setDeviceFoundImp(new DeviceFoundImp() {
            @Override
            public void sppDeviceFound(BluetoothDeviceWrapper device, int rssi) {//搜索蓝牙回调
                if (device == null) return;
                String deviceName = device.getName();
                if (TextUtils.isEmpty(deviceName)) return;
                //排除机型
                if (!device.getName().toLowerCase().startsWith("pt") &&
                        !device.getName().toLowerCase().startsWith("c") &&
                        !device.getName().toLowerCase().startsWith("s") &&
                        !device.getName().toLowerCase().startsWith("t") &&
                        !device.getName().toLowerCase().startsWith("k") &&
                        !device.getName().toLowerCase().startsWith("id") &&
                        !device.getName().toLowerCase().startsWith("op38")) {//id这个是俄外加的搞完后删除掉&&!device.getName().toLowerCase().startsWith("id")
                    return;
                } else {
                    addBluetoothBean(new PrinterBluetoothInfo(device.getName(), device.getAddress()));
                }
            }

            @Override
            public void sppConnected(BluetoothDevice device) {//蓝牙连接成功回调
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

    void addBluetoothBean(PrinterBluetoothInfo bean) {//添加设备的时候把重复的过滤掉
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
                searchBluetooth();//搜索蓝牙
            }
        });
        mListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                connectBluetooth(mListAdapter.getItem(position));//连接蓝牙
            }
        });
    }

    public void connectBluetooth(PrinterBluetoothInfo info) {//连接蓝牙
        if (info == null || TextUtils.isEmpty(info.address) ||
                (PrintActivity.CurrentPrinterInfo != null && info.address.equals(PrintActivity.CurrentPrinterInfo.address))) {//同一个蓝牙重复连接
            new AlertDialog.Builder(this).setMessage("当前设备可能正处于连接中，是否重新连接？").setNegativeButton("确认",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mPrinterInstance.closeConnection();
                            PrintActivity.CurrentPrinterInfo = null;
                            //建议断开后连接后不要马上连接蓝牙
                            new Handler().postDelayed(() -> {
                                connectBluetooth(info);
                            }, 400);
                        }
                    }).setNeutralButton("取消", null).show();
            return;
        }
        mPrinterInstance.closeConnection();//断开连接
        PrintActivity.CurrentPrinterInfo = null;
        if (mProgressDialog == null) {
            mProgressDialog = new NewProgressDialog(this, getString(R.string.connection), THEME_CIRCLE_PROGRESS);
        } else {
            mProgressDialog.dismiss();
        }
        //建议断开后连接后不要马上连接蓝牙
        new Handler().postDelayed(() -> {
            mProgressDialog.show();
            mPrinterInstance.connect(info.address);//连接蓝牙
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
