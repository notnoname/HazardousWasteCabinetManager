package me.liuzs.cabinetmanager;

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
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.puty.sdk.PrinterInstance;
import com.puty.sdk.callback.DeviceFoundImp;
import com.puty.sdk.callback.PrinterInstanceApi;

import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import me.liuzs.cabinetmanager.printer.PrinterBluetoothInfo;
import me.liuzs.cabinetmanager.util.Util;

public class PrintActivity extends BaseActivity {

    public static final String TAG = "PrintActivity";
    private static final String KEY_VALUE_LIST = "KEY_VALUE_LIST";
    public static PrinterBluetoothInfo CurrentPrinterInfo;
    private static Class<? extends List> mClass;
    private final Handler mHandler = new Handler();
    private TextView mPrinterName;
    private PrinterInstanceApi mPrinterInstance;
    private ImageView mPrinterState;
    private boolean isFoundSavedPrinter = false;
    private Bitmap mBitmap;

    public static void startPrintContainerLabel(Context context, List<ContainerLabel> contentList) {
        mClass = contentList.getClass();
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
        mPrinterName = findViewById(R.id.tvPrinterName);
        String value = getIntent().getStringExtra(KEY_VALUE_LIST);
        if (value != null && mClass != null) {
            List<ContainerLabel> labels = CabinetCore.GSON.fromJson(value, mClass);
            for (ContainerLabel label : labels) {
                mBitmap = label.createBarcodeImage();
                ImageView barcodeImage = findViewById(R.id.ivBarCode);
                barcodeImage.setImageBitmap(mBitmap);
                TextView batchNo = findViewById(R.id.tvBatcbNoValue);
                batchNo.setText("批次:" + label.containerNo);
                TextView operator = findViewById(R.id.tvOperatorValue);
                operator.setText("创建人:" + label.operator);
                TextView org = findViewById(R.id.tvOrgValue);
                org.setText("机构:" + org);
                TextView barCode = findViewById(R.id.tvBarcodeValue);
                barCode.setText(label.containerNo);
            }
        }
        initPrinter();
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
        if (mBitmap != null && !mBitmap.isRecycled()) {
            final Bitmap temp = Util.zoomImg(mBitmap, 350, 250);
            final Bitmap printBitmap = Util.rotateBitmap(temp, 90);
            temp.recycle();
            Log.d(TAG, "New Size:" + printBitmap.getWidth() + "-" + printBitmap.getHeight());
            int x = (pageWidth - printBitmap.getWidth()) >> 1;
            int y = (pageHeight - printBitmap.getHeight()) >> 1;
            //int x = 0;
            //int y = 0;
            showProgressDialog();
            new Thread(() -> {
                printImage(0, 1, x, y, printBitmap);
                dismissProgressDialog();
                mHandler.post(() -> showToast("打印结束"));
                Util.sleep(2000);
                mHandler.post(this::finish);
            }).start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }
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


    public static class ContainerLabel {
        public final static int ImageWidth = 600;
        public final static int ImageHeight = 200;

        public String containerNo;
        public String batchNo;
        public String operator;
        public String org;

        public Bitmap createBarcodeImage() {
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
                String code = new String(containerNo.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
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
}