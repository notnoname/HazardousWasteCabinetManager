package me.liuzs.cabinetmanager;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.feasycom.bean.BluetoothDeviceWrapper;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.puty.sdk.PrinterInstance;
import com.puty.sdk.callback.DeviceFoundImp;
import com.puty.sdk.callback.PrinterInstanceApi;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import me.liuzs.cabinetmanager.model.ContainerNoInfo;
import me.liuzs.cabinetmanager.printer.PrinterBluetoothInfo;
import me.liuzs.cabinetmanager.util.Util;

public class PrintActivity extends BaseActivity {

    public static final String TAG = "PrintActivity";
    private static final String KEY_VALUE_LIST = "KEY_VALUE_LIST";
    public static PrinterBluetoothInfo CurrentPrinterInfo;
    private static final Type JSON_TYPE = new TypeToken<List<ContainerNoInfo>>() {
    }.getType();
    public static final int PageWidth = 400;
    public static final int PageHeight = 559;
    private final Handler mHandler = new Handler();
    private TextView mPrinterName, mTip, mBatchName, mOperator, mAgency, mBarcode;
    private PrinterInstanceApi mPrinterInstance;
    private ImageView mPrinterState, mBarcodeImage;
    private boolean isFoundSavedPrinter = false;
    private int mCurrentInfo = 0;
    private final List<ContainerNoInfo> mContainerNoInfoList = new LinkedList<>();

    public static void startPrintContainerLabel(Context context, List<ContainerNoInfo> contentList) {
        Intent intent = new Intent(context, PrintActivity.class);
        intent.putExtra(KEY_VALUE_LIST, CabinetCore.GSON.toJson(contentList));
        context.startActivity(intent);
    }

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setContentView(R.layout.activity_print);
        mPrinterState = findViewById(R.id.ivPrinterState);
        mBarcodeImage = findViewById(R.id.ivBarCode);
        mPrinterName = findViewById(R.id.tvPrinterName);
        mTip = findViewById(R.id.tvTip);
        mBatchName = findViewById(R.id.tvBatcbNoValue);
        mOperator = findViewById(R.id.tvOperatorValue);
        mAgency = findViewById(R.id.tvOrgValue);
        mBarcode = findViewById(R.id.tvBarcodeValue);

        String value = getIntent().getStringExtra(KEY_VALUE_LIST);
        List<ContainerNoInfo> labels = CabinetCore.GSON.fromJson(value, JSON_TYPE);
        mContainerNoInfoList.addAll(labels);
        showContainerNoInfo();
        initPrinter();
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void showContainerNoInfo() {
        if (mContainerNoInfoList.size() > 0) {
            mTip.setText(String.format("%s (%d/%d)", getString(R.string.print_label), (mCurrentInfo + 1), mContainerNoInfoList.size()));
            ContainerNoInfo containerNoInfo = mContainerNoInfoList.get(mCurrentInfo);
            Bitmap bitmap = CreateBarcodeImage(containerNoInfo.no);
            mBarcodeImage.setImageBitmap(bitmap);
            mBatchName.setText("批次:" + containerNoInfo.batch_name);
            mOperator.setText("创建人:" + containerNoInfo.creator);
            mAgency.setText("机构:" + containerNoInfo.org);
            mBarcode.setText(containerNoInfo.no);
        }
    }

    private void showInfo() {
        mHandler.post(() -> {
            if (CurrentPrinterInfo != null) {
                mPrinterState.setImageResource(R.drawable.ic_connect_devices_true);
                mPrinterName.setText(CurrentPrinterInfo.name);
            } else {
                mPrinterState.setImageResource(R.drawable.ic_connect_devices_false);
                mPrinterName.setText("");
            }
        });
    }

    public void onPrintButtonClick(View v) {
        if (mPrinterInstance == null || CurrentPrinterInfo == null) {
            showToast("请先连接打印机！");
            return;
        }
        int pageWidth = (int) Math.round(Util.dpiToPix(203, 50));
        int pageHeight = (int) Math.round(Util.dpiToPix(203, 70));
        Log.d(TAG, "Page Size:" + pageWidth + "-" + pageHeight);
        showProgressDialog();
        new Thread(() -> {
            for (mCurrentInfo = 0; mCurrentInfo < mContainerNoInfoList.size(); mCurrentInfo++) {
                mHandler.post(() -> showContainerNoInfo());
                printInfo(mContainerNoInfoList.get(mCurrentInfo));
                Util.sleep(1000);
            }
            dismissProgressDialog();
            mHandler.post(() -> showToast("打印结束"));
            Util.sleep(2000);
            mHandler.post(this::finish);
        }).start();
    }

    private void printInfo(ContainerNoInfo info) {
        /**
         * CPCL开始命令
         * @param offset 整个标签的水平偏移量,点为单位
         * @param height 标签最大高度，点为单位
         * @param page   打印标签的数量，最大 1024 张
         */
        PrinterInstance.getInstance().startCpcl(0, PageHeight, 1);
        /**
         * 文本命令
         * @param direction     旋转角度 0、90、180、270
         * @param font          24    字符(12*24)，汉字(24*24)
         *                      55    字符(8*16)，汉字(16*16)
         * @param size          字符高度选择 ----> 0 1 2 3 4 5 6 7 纵向放大 size 纵向放大 1(正常) 2(2倍高) 3 4 5 6 7 8
         *                      字符宽度选择 ----> 0 10 20 30 40 50 60 70 横向放大 size  1(正常) 2(2倍高) 3 4 5 6 7 8
         *                      示列:当传入3时，纵向放大3倍
         *                      当传入30时，横向放大3倍
         *                      当传入33时，横纵向同时放大3倍
         * @param x             x坐标，点为单位
         * @param y             y坐标，点为单位
         * @param content       文本内容
         */
        PrinterInstance.getInstance().pringTextCpcl(270, 55, 11, 340, 30, "批次名:" + info.batch_name + "存单号:" + info.no);
        PrinterInstance.getInstance().pringTextCpcl(270, 55, 11, 300, 30, "创建人:" + info.creator);
        PrinterInstance.getInstance().pringTextCpcl(270, 55, 11, 260, 30, "机构名:" + info.org);
        PrinterInstance.getInstance().pringTextCpcl(270, 55, 11, 220, 30, "存单号:" + info.no);


        /**
         * 一维条码
         *
         * @param isTransverse true    打印横向条码
         *                     false   打印纵向条码
         * @param type         type 条码类型，一维条码的类型有:
         *                     Type值     条码类型
         *                     UPCA       UPC-A
         *                     UPCE       UPC-E
         *                     EAN13      JAN13 (EAN13)
         *                     EAN8       JAN 8 (EAN8)
         *                     39         CODE39
         *                     CODABAR    CODABAR
         *                     93         CODE93
         *                     128        CODE128(Auto)
         * @param width        条码宽度. 点为单位   宽度是根据内容及宽条宽和窄条宽比率定
         * @param ratio        宽条宽和窄条宽比率
         *                     0 = 1.5:1   20 = 2.0:1  25 = 2.5:1  30 = 3.0:1
         *                     1 = 2.0:1   21 = 2.1:1  26 = 2.6:1
         *                     2 = 2.5:1   22 = 2.2:1  27 = 2.7:1
         *                     3 = 3.0:1   23 = 2.3:1  28 = 2.8:1
         *                     4 = 3.5:1   24 = 2.4:1  29 = 2.9:1
         * @param height       条码高度. 点为单位
         * @param x            x坐标，点为单位
         * @param y            y坐标，点为单位
         * @param data         一维码内容
         */
        PrinterInstance.getInstance().pringCodeCpcl(true, "128", 1, 2, 100, 30, 30, info.no);
        PrinterInstance.getInstance().calibrateCpcl();
        /**
         * 打印命令
         */
        PrinterInstance.getInstance().printCpcl();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onCancelButtonClick(View v) {
        finish();
    }

    private void initPrinter() {
        mPrinterInstance = PrinterInstance.getInstance();
        if (mPrinterInstance != null) {
            setCallBacks();
            //检测蓝牙是否打开
            if (!mPrinterInstance.isBtEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(intent);
            } else {
                if (CurrentPrinterInfo != null) {
                    showInfo();
                } else {
                    //搜索蓝牙
                    searchBluetooth();
                }
            }
        }
    }

    private void searchBluetooth() {//搜索蓝牙
        Log.d(TAG, "Start search printer.");
        showProgressDialog();
        int searchTime = 5 * 1000;
        mPrinterInstance.startScan(searchTime);//搜索蓝牙   5秒自动关闭    默认是8秒
        isFoundSavedPrinter = false;
        mHandler.postDelayed(() -> {
            Log.d(TAG, "Check connected printer state.");
            if (isFoundSavedPrinter) {
                PrinterBluetoothInfo info = CabinetCore.getConnectedPrinterInfo(CabinetApplication.getInstance());
                if (info != null) {
                    connectPrinter(info);
                } else {
                    showInfo();
                    dismissProgressDialog();
                }
            } else {
                CurrentPrinterInfo = null;
                showInfo();
                dismissProgressDialog();
            }
        }, searchTime);
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
                        !device.getName().toLowerCase().startsWith("op38")) {
                    //id这个是俄外加的搞完后删除掉&&!device.getName().toLowerCase().startsWith("id")
                    Log.d(TAG, "发现未知打印机");
                } else {
                    matchSavedPrinter(new PrinterBluetoothInfo(device.getName(), device.getAddress()));
                }
            }

            @Override
            public void sppConnected(BluetoothDevice device) {//蓝牙连接成功回调
                PrinterBluetoothInfo info = new PrinterBluetoothInfo(device.getName(), device.getAddress());
                CurrentPrinterInfo = info;
                CabinetCore.saveConnectedPrinterInfo(CabinetApplication.getInstance(), info);
                showInfo();
                dismissProgressDialog();
            }
        });
    }

    private void matchSavedPrinter(PrinterBluetoothInfo bean) {//添加设备的时候把重复的过滤掉
        PrinterBluetoothInfo info = CabinetCore.getConnectedPrinterInfo(CabinetApplication.getInstance());
        if (info != null && bean.address.equals(info.address)) {
            isFoundSavedPrinter = true;
        }
    }

    public void connectPrinter(PrinterBluetoothInfo info) {//连接蓝牙
        if (info == null || TextUtils.isEmpty(info.address) ||
                (CurrentPrinterInfo != null && info.address.equals(CurrentPrinterInfo.address))) {//同一个蓝牙重复连接
            return;
        }
        mPrinterInstance.closeConnection();//断开连接
        CurrentPrinterInfo = null;
        showInfo();
        //建议断开后连接后不要马上连接蓝牙
        mHandler.postDelayed(() -> {
            Log.d(TAG, "Start connect printer:." + info.address);
            mPrinterInstance.connect(info.address);//连接蓝牙
        }, 400);
    }

    public void onBluetoothListButtonClick(View view) {
        Intent intent = new Intent(this, DeviceBluetoothListActivity.class);
        startActivity(intent);
    }

    public void printText(ContainerNoInfo info) {
        /**
         * CPCL开始命令
         * @param offset 整个标签的水平偏移量,点为单位
         * @param height 标签最大高度，点为单位
         * @param page   打印标签的数量，最大 1024 张
         */
        PrinterInstance.getInstance().startCpcl(0, 700, 1);
        /**
         * 字体加粗开始指令(中间可以包含多条text指令)
         * @param size  >1 设置打印字体为粗体    0 取消粗体模式
         */
//        PrinterInstance.getInstance().boldFontStartCpcl(0);
        /**
         * 文本命令
         * @param direction     旋转角度 0、90、180、270
         * @param font          24    字符(12*24)，汉字(24*24)
         *                      55    字符(8*16)，汉字(16*16)
         * @param size          字符高度选择 ----> 0 1 2 3 4 5 6 7 纵向放大 size 纵向放大 1(正常) 2(2倍高) 3 4 5 6 7 8
         *                      字符宽度选择 ----> 0 10 20 30 40 50 60 70 横向放大 size  1(正常) 2(2倍高) 3 4 5 6 7 8
         *                      示列:当传入3时，纵向放大3倍
         *                      当传入30时，横向放大3倍
         *                      当传入33时，横纵向同时放大3倍
         * @param x             x坐标，点为单位
         * @param y             y坐标，点为单位
         * @param content       文本内容
         */
        PrinterInstance.getInstance().pringTextCpcl(270, 55, 11, 360, 20, "批次名:" + info.batch_name);
        PrinterInstance.getInstance().pringTextCpcl(270, 55, 11, 320, 20, "创建人:" + info.creator);
        PrinterInstance.getInstance().pringTextCpcl(270, 55, 11, 280, 20, "机构名:" + info.org);
        PrinterInstance.getInstance().pringTextCpcl(270, 55, 11, 50, 20, "存单号:" + info.no);

        /**
         * 字体加粗结束指令
         */
//        PrinterInstance.getInstance().boldFontEndCpcl();

        /**
         * 打印命令
         */
        PrinterInstance.getInstance().printCpcl();
    }

    /**
     * @param offset 整个标签的水平偏移量,点为单位
     * @param count  打印标签的数量，最大 1024 张
     * @param x      水平起始位置
     * @param y      垂直起始位置
     * @param bitmap 图形数据,图片（不要打透明底色图片）
     */
    public void printImage(int offset, int count, int x, int y, Bitmap bitmap) {

        Log.d(TAG, "x, y, width, height:" + x + " - " + y + " - " + bitmap.getWidth() + " - " + bitmap.getHeight());
        PrinterInstance.getInstance().startCpcl(offset, y + bitmap.getHeight(), count);
        PrinterInstance.getInstance().pringBmpCpcl(x, y, bitmap);
        PrinterInstance.getInstance().calibrateCpcl();
        PrinterInstance.getInstance().printCpcl();
    }

    /**
     * 一维条码
     *
     * @param isTransverse true    打印横向条码
     *                     false   打印纵向条码
     * @param type         type 条码类型，一维条码的类型有:
     *                     Type值     条码类型
     *                     UPCA       UPC-A
     *                     UPCE       UPC-E
     *                     EAN13      JAN13 (EAN13)
     *                     EAN8       JAN 8 (EAN8)
     *                     39         CODE39
     *                     CODABAR    CODABAR
     *                     93         CODE93
     *                     128        CODE128(Auto)
     * @param width        条码宽度. 点为单位   宽度是根据内容及宽条宽和窄条宽比率定
     * @param ratio        宽条宽和窄条宽比率
     *                     0 = 1.5:1   20 = 2.0:1  25 = 2.5:1  30 = 3.0:1
     *                     1 = 2.0:1   21 = 2.1:1  26 = 2.6:1
     *                     2 = 2.5:1   22 = 2.2:1  27 = 2.7:1
     *                     3 = 3.0:1   23 = 2.3:1  28 = 2.8:1
     *                     4 = 3.5:1   24 = 2.4:1  29 = 2.9:1
     * @param height       条码高度. 点为单位
     * @param x            x坐标，点为单位
     * @param y            y坐标，点为单位
     * @param data         一维码内容
     */
    private void printBarcode(boolean isTransverse, String type, int width, int ratio, int height, int x, int y, String data) {
        PrinterInstance.getInstance().startCpcl(0, 150, 1);
        PrinterInstance.getInstance().pringCodeCpcl(isTransverse, type, width, ratio, height, x, y, data);
        PrinterInstance.getInstance().printCpcl();
    }

    public final static int ImageWidth = 600;
    public final static int ImageHeight = 200;

    public static Bitmap CreateBarcodeImage(String no) {
        Bitmap bitmap = Bitmap.createBitmap(ImageWidth, ImageHeight, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(0xffffffff);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, bitmap.getWidth(), bitmap.getHeight(), paint);

        try {
            int barCodeHeight = ImageHeight - 20;
            int barCodeWidth = ImageWidth - 20;
            int x = 10, y = 10;
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 0); /* default = 4 */
            BarcodeFormat format = BarcodeFormat.CODE_128;
            String code = new String(no.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
            BitMatrix matrix = new MultiFormatWriter().encode(code, format, barCodeWidth, barCodeHeight, hints);
            int width = matrix.getWidth();
            int height = matrix.getHeight();
            int[] pixel = new int[width * height];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (matrix.get(j, i))
                        pixel[i * width + j] = 0xff000000;
                    else
                        pixel[i * width + j] = 0xffFFFFFF;
                }
            }
            bitmap.setPixels(pixel, 0, width, x, y, width, height);
            canvas.save();

        } catch (WriterException e) {
            e.printStackTrace();
        }

        return bitmap;
    }
}