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

@SuppressLint({"DefaultLocale", "SetTextI18n"})
public class PrintActivity extends BaseActivity {

    public static final String TAG = "PrintActivity";
    public static final int PageWidth = 400;
    public static final int PageHeight = 559;
    public final static int ImageWidth = 600;
    public final static int ImageHeight = 200;
    private static final String KEY_VALUE_LIST = "KEY_VALUE_LIST";
    private static final Type JSON_TYPE = new TypeToken<List<ContainerNoInfo>>() {
    }.getType();
    public static PrinterBluetoothInfo CurrentPrinterInfo;
    private final Handler mHandler = new Handler();
    private final List<ContainerNoInfo> mContainerNoInfoList = new LinkedList<>();
    private TextView mPrinterName, mTip, mBatchName, mOperator, mAgency, mBarcode;
    private PrinterInstanceApi mPrinterInstance;
    private ImageView mPrinterState, mBarcodeImage;
    private boolean isFoundSavedPrinter = false;
    private int mCurrentInfo = 0;

    public static void startPrintContainerLabel(Context context, List<ContainerNoInfo> contentList) {
        Intent intent = new Intent(context, PrintActivity.class);
        intent.putExtra(KEY_VALUE_LIST, CabinetCore.GSON.toJson(contentList));
        context.startActivity(intent);
    }

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

    private void showContainerNoInfo() {
        if (mContainerNoInfoList.size() > 0) {
            mTip.setText(String.format("%s (%d/%d)", getString(R.string.print_label), (mCurrentInfo + 1), mContainerNoInfoList.size()));
            ContainerNoInfo containerNoInfo = mContainerNoInfoList.get(mCurrentInfo);
            Bitmap bitmap = CreateBarcodeImage(containerNoInfo.no);
            mBarcodeImage.setImageBitmap(bitmap);
            mBatchName.setText("?????????:" + containerNoInfo.batch_name);
            mOperator.setText("?????????:" + containerNoInfo.creator);
            mAgency.setText("?????????:" + containerNoInfo.org);
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
            showToast("????????????????????????");
            return;
        }
        int pageWidth = (int) Math.round(Util.dpiToPix(203, 50));
        int pageHeight = (int) Math.round(Util.dpiToPix(203, 70));
        Log.d(TAG, "Page Size:" + pageWidth + "-" + pageHeight);
        showProgressDialog();
        new Thread(() -> {
            for (mCurrentInfo = 0; mCurrentInfo < mContainerNoInfoList.size(); mCurrentInfo++) {
                mHandler.post(this::showContainerNoInfo);
                printInfo(mContainerNoInfoList.get(mCurrentInfo));
                Util.sleep(1000);
            }
            dismissProgressDialog();
            mHandler.post(() -> showToast("????????????"));
            Util.sleep(2000);
            mHandler.post(this::finish);
        }).start();
    }

    private void printInfo(ContainerNoInfo info) {

        /*
          CPCL????????????
          @param offset ??????????????????????????????,????????????
         * @param height ?????????????????????????????????
         * @param page   ?????????????????????????????? 1024 ???
         */
        PrinterInstance.getInstance().startCpcl(0, PageHeight, 1);

        /*
          ????????????
          @param direction     ???????????? 0???90???180???270
         * @param font          24    ??????(12*24)?????????(24*24)
         *                      55    ??????(8*16)?????????(16*16)
         * @param size          ?????????????????? ----> 0 1 2 3 4 5 6 7 ???????????? size ???????????? 1(??????) 2(2??????) 3 4 5 6 7 8
         *                      ?????????????????? ----> 0 10 20 30 40 50 60 70 ???????????? size  1(??????) 2(2??????) 3 4 5 6 7 8
         *                      ??????:?????????3??????????????????3???
         *                      ?????????30??????????????????3???
         *                      ?????????33???????????????????????????3???
         * @param x             x?????????????????????
         * @param y             y?????????????????????
         * @param content       ????????????
         */
        PrinterInstance.getInstance().pringTextCpcl(270, 55, 11, 340, 20, "??????:" + info.batch_name);
        PrinterInstance.getInstance().pringTextCpcl(270, 55, 11, 300, 20, "??????:" + info.creator);
        PrinterInstance.getInstance().pringTextCpcl(270, 55, 11, 260, 20, "??????:" + info.org);
        PrinterInstance.getInstance().pringTextCpcl(270, 55, 11, 200, 20, "??????:" + info.no);

        /*
          ????????????

          @param isTransverse true    ??????????????????
         *                     false   ??????????????????
         * @param type         type ???????????????????????????????????????:
         *                     Type???     ????????????
         *                     UPCA       UPC-A
         *                     UPCE       UPC-E
         *                     EAN13      JAN13 (EAN13)
         *                     EAN8       JAN 8 (EAN8)
         *                     39         CODE39
         *                     CODABAR    CODABAR
         *                     93         CODE93
         *                     128        CODE128(Auto)
         * @param width        ????????????. ????????????   ??????????????????????????????????????????????????????
         * @param ratio        ???????????????????????????
         *                     0 = 1.5:1   20 = 2.0:1  25 = 2.5:1  30 = 3.0:1
         *                     1 = 2.0:1   21 = 2.1:1  26 = 2.6:1
         *                     2 = 2.5:1   22 = 2.2:1  27 = 2.7:1
         *                     3 = 3.0:1   23 = 2.3:1  28 = 2.8:1
         *                     4 = 3.5:1   24 = 2.4:1  29 = 2.9:1
         * @param height       ????????????. ????????????
         * @param x            x?????????????????????
         * @param y            y?????????????????????
         * @param data         ???????????????
         */
        PrinterInstance.getInstance().pringCodeCpcl(true, "128", 1, 2, 100, 30, 30, info.no);
        PrinterInstance.getInstance().calibrateCpcl();
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
            //????????????????????????
            if (!mPrinterInstance.isBtEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(intent);
            } else {
                if (CurrentPrinterInfo != null) {
                    showInfo();
                } else {
                    //????????????
                    searchBluetooth();
                }
            }
        }
    }

    private void searchBluetooth() {//????????????
        Log.d(TAG, "Start search printer.");
        showProgressDialog();
        int searchTime = 5 * 1000;
        mPrinterInstance.startScan(searchTime);//????????????   5???????????????    ?????????8???
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
                        !device.getName().toLowerCase().startsWith("op38")) {
                    //id???????????????????????????????????????&&!device.getName().toLowerCase().startsWith("id")
                    Log.d(TAG, "?????????????????????");
                } else {
                    matchSavedPrinter(new PrinterBluetoothInfo(device.getName(), device.getAddress()));
                }
            }

            @Override
            public void sppConnected(BluetoothDevice device) {//????????????????????????
                PrinterBluetoothInfo info = new PrinterBluetoothInfo(device.getName(), device.getAddress());
                CurrentPrinterInfo = info;
                CabinetCore.saveConnectedPrinterInfo(CabinetApplication.getInstance(), info);
                showInfo();
                dismissProgressDialog();
            }
        });
    }

    private void matchSavedPrinter(PrinterBluetoothInfo bean) {//??????????????????????????????????????????
        PrinterBluetoothInfo info = CabinetCore.getConnectedPrinterInfo(CabinetApplication.getInstance());
        if (info != null && bean.address.equals(info.address)) {
            isFoundSavedPrinter = true;
        }
    }

    public void connectPrinter(PrinterBluetoothInfo info) {//????????????
        if (info == null || TextUtils.isEmpty(info.address) ||
                (CurrentPrinterInfo != null && info.address.equals(CurrentPrinterInfo.address))) {//???????????????????????????
            return;
        }
        mPrinterInstance.closeConnection();//????????????
        CurrentPrinterInfo = null;
        showInfo();
        //????????????????????????????????????????????????
        mHandler.postDelayed(() -> {
            Log.d(TAG, "Start connect printer:." + info.address);
            mPrinterInstance.connect(info.address);//????????????
        }, 400);
    }

    public void onBluetoothListButtonClick(View view) {
        Intent intent = new Intent(this, DeviceBluetoothListActivity.class);
        startActivity(intent);
    }

    /**
     * @param offset ??????????????????????????????,????????????
     * @param count  ?????????????????????????????? 1024 ???
     * @param x      ??????????????????
     * @param y      ??????????????????
     * @param bitmap ????????????,???????????????????????????????????????
     */
    public void printImage(int offset, int count, int x, int y, Bitmap bitmap) {

        Log.d(TAG, "x, y, width, height:" + x + " - " + y + " - " + bitmap.getWidth() + " - " + bitmap.getHeight());
        PrinterInstance.getInstance().startCpcl(offset, y + bitmap.getHeight(), count);
        PrinterInstance.getInstance().pringBmpCpcl(x, y, bitmap);
        PrinterInstance.getInstance().calibrateCpcl();
        PrinterInstance.getInstance().printCpcl();
    }
}