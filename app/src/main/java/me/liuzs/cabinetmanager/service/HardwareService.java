package me.liuzs.cabinetmanager.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import me.liuxy.cabinet.CabinetManager;
import me.liuxy.cabinet.Steelyard;
import me.liuxy.cabinet.SubBoard;
import me.liuxy.cabinet.TDA09C;
import me.liuxy.cabinet.TVOCs;
import me.liuzs.cabinetmanager.BuildConfig;
import me.liuzs.cabinetmanager.CabinetApplication;
import me.liuzs.cabinetmanager.Config;
import me.liuzs.cabinetmanager.CtrlFunc;
import me.liuzs.cabinetmanager.model.CabinetInfo;
import me.liuzs.cabinetmanager.model.DeviceInfo;
import me.liuzs.cabinetmanager.model.HardwareValue;
import me.liuzs.cabinetmanager.model.SetupValue;
import me.liuzs.cabinetmanager.model.TVOCValue;
import me.liuzs.cabinetmanager.model.TVOCsValue;
import me.liuzs.cabinetmanager.net.RemoteAPI;
import me.liuzs.cabinetmanager.util.Util;

public class HardwareService extends Service {

    public static final String TAG = "HardwareService";
    @SuppressLint("SimpleDateFormat")
    public static final SimpleDateFormat mLogTimeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    private final static Object mWaitLocker = new Object();
    private static final AtomicLong mHardwareOPTime = new AtomicLong(0L);
    public static int Qos = 2;
    private static CabinetManager mManager;
    public final String PublisherID = MqttClient.generateClientId();
    private final HardwareServiceBinder mBinder = new HardwareServiceBinder();
    private final Gson mGson = new Gson();
    private final IMqttActionListener mPublishActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            Log.d(TAG, "MQTT Publish  success");
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            if (exception != null) {
                Log.i(TAG, "MQTT Publish failure: " + exception.getMessage());
            }
        }
    };
    private final AtomicBoolean isDoHardwareValueQuery = new AtomicBoolean(false);
    private final Map<String, Timer> mLockerAutoUnlockTimer;
    private int[] mPeriods;
    private Timer mHardwareValueQueryTimer;
    private MqttAndroidClient mPublishClient;
    private boolean isSingleDevice = true;
    //副柜数量
    private int mSubCabinetSize = 0;
    //副柜红绿灯和风扇状态
    private boolean[] isSubDevicesLightGreen, isSubDevicesLightRed, isSubDevicesFanRun;
    //一主多副状态下主柜风扇和红绿灯状态
    private boolean isMainDeviceLightGreen, isMainDeviceLightRed, isMainDeviceFanRun;
    //主柜风扇起止时间
    private long mMainFanWorkStartTime = System.currentTimeMillis(), mMainFanWorkEndTime = System.currentTimeMillis();
    //副柜风扇起止时间
    private long[] mSubFanWorkStartTime, mSubFanWorkEndTime;
    private Timer mAutoLockTimer;
    private long mHardwareValueQueryInterval;

    public HardwareService() {
        super();
        mLockerAutoUnlockTimer = Collections.synchronizedMap(new HashMap<>());
    }

    public synchronized static void weight(Steelyard.SteelyardCallback callback) {
        if (mManager == null) {
            return;
        }
        try {
            Steelyard steelyard = mManager.getSteelyardInstance();
            if (steelyard != null) {
                steelyard.setCallback(callback);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void registerHardwareOPTime(String opName) {
        mHardwareOPTime.set(System.currentTimeMillis());
        Log.d(TAG, "Register hardware op time:" + opName + ", Time:" + mLogTimeFormat.format(new Date(System.currentTimeMillis())));
    }

    public synchronized static void steelyardCalibration(int value) {
        if (mManager == null) {
            return;
        }
        try {
            TDA09C c = mManager.getTDA09CInstance(0);
            c.Calibrate(TDA09C.CalibrateType.ECALIBRATE_ZERO);
            c.SetGain(value);
            c.Calibrate(TDA09C.CalibrateType.ECALIBRATE_GAIN);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized static float getWeight() {
        try {
            TDA09C c = mManager.getTDA09CInstance(0);
            c.Calibrate(TDA09C.CalibrateType.ECALIBRATE_ZERO);
            return c.getWeight() / 10f;
        } catch (Exception e) {
            return Float.MIN_VALUE;
        }
    }

    private static MqttConnectOptions setUpConnectionOptions() {
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        connOpts.setUserName(RemoteAPI.MQTT.MQTT_USER);
        connOpts.setPassword(RemoteAPI.MQTT.MQTT_PASSWORD.toCharArray());
        return connOpts;
    }

    public static void sleep() {
        try {
            Thread.sleep(35);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void waitForHardwareOPGap(String opName) {
        Log.d(TAG, "Start wait for hardware op :" + opName + ", Time:" + mLogTimeFormat.format(new Date(System.currentTimeMillis())));
        long gap = System.currentTimeMillis() - mHardwareOPTime.get();
        if (gap >= mPeriods[0]) {
            gap = 0;
        } else {
            gap = mPeriods[0] - gap;
        }
        if (gap != 0) {
            try {
                Thread.sleep(gap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "End wait for hardware op :" + opName + ", Time:" + mLogTimeFormat.format(new Date(System.currentTimeMillis())));
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private void initCabinetManager() {
        CabinetInfo cabinetInfo = CabinetApplication.getInstance().getCabinetInfo();
        if (cabinetInfo == null || !CabinetApplication.getInstance().isInitHardwareManager()) {
            return;
        }
        if (mManager != null) {
            return;
        }
        if (BuildConfig.DEBUG) {
            return;
        }
        isSingleDevice = cabinetInfo.tankType == 2;
        mSubCabinetSize = isSingleDevice ? 0 : cabinetInfo.devices.size();
        isSubDevicesFanRun = new boolean[mSubCabinetSize];
        isSubDevicesLightRed = new boolean[mSubCabinetSize];
        isSubDevicesLightGreen = new boolean[mSubCabinetSize];
        mSubFanWorkStartTime = new long[mSubCabinetSize];
        mSubFanWorkEndTime = new long[mSubCabinetSize];

        CabinetManager.Settings settings = new CabinetManager.Settings();
        if (isSingleDevice) {
            settings.SwitchesDeviceName = "/dev/ttysWK0";
            settings.SubBoardConfigs = new CabinetManager.SubBoardConfig[0];
        } else {
            //Address:0的子板用于室内风扇和红绿灯控制，子板数比副柜数多1；
            settings.SubBoardConfigs = new CabinetManager.SubBoardConfig[cabinetInfo.devices.size() + 1];
            for (int i = 0; i < mSubCabinetSize; i++) {
                settings.SubBoardConfigs[i] = new CabinetManager.SubBoardConfig((byte) cabinetInfo.devices.get(i).hardwareAddress);//按实际设备地址填写
            }
            //最后一块板子地址为0；
            settings.SubBoardConfigs[mSubCabinetSize] = new CabinetManager.SubBoardConfig((byte) 0);
        }
        settings.TDA09C485Configs = new CabinetManager.TDA09C485Config[1];
        int c485 = CtrlFunc.getTDA09C485Config(this);
        settings.TDA09C485Configs[0] = new CabinetManager.TDA09C485Config((byte) c485);//按实际设备地址填写
        settings.TVOCsDeviceName = new String[2];
        settings.TVOCsDeviceName[0] = "/dev/ttysWK2";
        settings.TVOCsDeviceName[1] = "/dev/ttysWK3";
        settings.SHT3xDeviceName = "/dev/i2c-7";
        mManager = new CabinetManager(settings);
        lightGreen();
        turnOffFan();
        if (!isSingleDevice) {
            turnOnAllSubFan();
            lightGreenAllSub();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "Service StartCommand");
        if (mPublishClient == null) {
            mPublishClient = new MqttAndroidClient(this, RemoteAPI.MQTT.MQTT_ROOT, PublisherID);
        }
        mPeriods = CtrlFunc.getSubBoardPeriod(this);
//        initMQTTPublishClient();
        initCabinetManager();
        initHardwareValueQueryTimer(mPeriods[2]);
        return super.onStartCommand(intent, flags, startId);
    }

    private void initMQTTPublishClient() {
        if (!mPublishClient.isConnected()) {
            try {
                MqttConnectOptions connOpts = setUpConnectionOptions();
                mPublishClient.connect(connOpts);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    private void initHardwareValueQueryTimer(int interval) {
        if (mHardwareValueQueryTimer != null) {
            if (mHardwareValueQueryInterval != interval) {
                isDoHardwareValueQuery.set(false);
                mHardwareValueQueryTimer.cancel();
                mHardwareValueQueryTimer = null;
            } else {
                return;
            }
        }
        mHardwareValueQueryInterval = interval;
        Log.d(TAG, "Init Hardware Value QueryTimer");
        mHardwareValueQueryTimer = new Timer("HardwareValueQueryTime Thread");
        mHardwareValueQueryTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isDoHardwareValueQuery.getAndSet(true)) {
                    return;
                }
                Log.d(TAG, "Do hardware value query");
                initMQTTPublishClient();
                HardwareValue value = getHardwareValue();
                doHardwarePolicy(value);
                notifyValue(value);
                publishValue(value);
                isDoHardwareValueQuery.set(false);
            }
        }, 0, mHardwareValueQueryInterval);
    }

    private void notifyValue(HardwareValue value) {
        if (value == null) {
            return;
        }
        Intent intent = new Intent(Config.ACTION_HARDWARE_VALUE_SEND);
        String valueJSON = mGson.toJson(value);
        Log.d(TAG, "Hardware JSON:" + valueJSON);
        intent.putExtra(Config.KEY_HARDWARE_VALUE, valueJSON);
        sendBroadcast(intent);
    }

    private void publishValue(HardwareValue value) {
        CabinetInfo info = CabinetApplication.getInstance().getCabinetInfo();
        if (info != null) {
            DeviceInfo single = CabinetApplication.getSingleDevice();
            String mainDevId = single != null ? single.devId : null;
            List<String> devIds = new LinkedList<>();
            for (DeviceInfo device : info.devices) {
                devIds.add(device.devId);
            }
            publishValue(value, String.valueOf(info.tankType), info.tankId, mainDevId, devIds);
        }
    }

    private void publishValue(HardwareValue value, String tankType, String tankId, String mainDeviceId, List<String> subDeviceId) {
        if (mPublishClient.isConnected() && value != null) {
            try {
                JsonObject jsValue = new JsonObject();
                jsValue.addProperty("tankType", tankType);
                jsValue.addProperty("tankId", tankId);
                jsValue.addProperty("createTime", value.createTime);

                JsonObject tvoCsValue = new JsonObject();
                tvoCsValue.addProperty("deviceId", mainDeviceId);
                tvoCsValue.addProperty("fan", value.fan ? 1 : 2);
                tvoCsValue.addProperty("lock", value.lock ? 1 : 2);
                tvoCsValue.addProperty("voc1", value.tvoCsValue.TVOC1.ppb / 1000f);
                tvoCsValue.addProperty("voc2", value.tvoCsValue.TVOC2.ppb / 1000f);
                tvoCsValue.addProperty("hum", value.tvoCsValue.TVOC1.humi / 100f);
                tvoCsValue.addProperty("tem", value.tvoCsValue.TVOC1.temperature / 100f);
                jsValue.add("tvoCsValue", tvoCsValue);

                JsonArray subBoardStatusData = new JsonArray();
                for (int i = 0; i < value.subBoardStatusData.size(); i++) {
                    SubBoard.StatusData data = value.subBoardStatusData.get(i);
                    String devId = subDeviceId.get(i);
                    JsonObject subValue = new JsonObject();
                    subValue.addProperty("deviceId", devId);
                    subValue.addProperty("fan", 1);
                    subValue.addProperty("lock", data.door_lock == 1 ? 2 : 1);
                    subValue.addProperty("voc1", data.concentration_ugpm3 / 10f);
                    subValue.addProperty("voc2", 0);
                    subValue.addProperty("hum", data.humi / 10f);
                    subValue.addProperty("tem", data.temp / 10f);
                    subBoardStatusData.add(subValue);
                }
                jsValue.add("subBoardStatusData", subBoardStatusData);

                String payload = mGson.toJson(jsValue);

                Log.d(TAG, "MQTT JSON:" + payload);
                MqttMessage message = new MqttMessage();
                message.setPayload(payload.getBytes());
                message.setQos(Qos);
                mPublishClient.publish(RemoteAPI.MQTT.MQTT_HARDWARE_PUBLISH_TOPIC, message, null, mPublishActionListener);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    private MaxValue getMainCabinetMaxValue(HardwareValue value) {
        MaxValue result = new MaxValue();
        long ppb = Math.max(value.tvoCsValue.TVOC1.ppb, value.tvoCsValue.TVOC2.ppb);
        float temp = Math.max(value.tvoCsValue.TVOC1.temperature, value.tvoCsValue.TVOC2.temperature);
        result.ppb = ppb;
        result.temp = temp;
        return result;
    }

    private void doHardwarePolicy(HardwareValue value) {
        SetupValue setup = CtrlFunc.getSetupValue(this);
        if (setup == null || value == null) {
            return;
        }

        boolean threshold = false;
        float thresholdPPB = setup.thresholdPPM * 1000;
        MaxValue maxValue = getMainCabinetMaxValue(value);
        long ppb = maxValue.ppb;
        float vocTemp = maxValue.temp;
        vocTemp = vocTemp / 100;
        Log.d(TAG, vocTemp + ":" + setup.thresholdTemp + "     " + ppb + ":" + thresholdPPB);
        if (vocTemp >= setup.thresholdTemp || ppb >= thresholdPPB) {
            threshold = true;
        }

        if (threshold) {
            if (!value.redLight) {
                lightRed();
            }
        } else {
            if (!value.greenLight) {
                lightGreen();
            }
        }

        for (int index = 0; index < mSubCabinetSize; index++) {
            SubBoard.StatusData data = value.subBoardStatusData.get(index);
            float subPPb = data.concentration_ugpm3 * 100;
            float subTemp = data.temp / 10f;
            boolean subThreshold = false;
            if (subTemp >= setup.thresholdTemp || subPPb >= thresholdPPB) {
                subThreshold = true;
            }
            if (subThreshold) {
                if (!isSubDevicesLightRed[index]) {
                    lightRed(index);
                }
            } else {
                if (!isSubDevicesLightGreen[index]) {
                    lightGreen(index);
                }
            }
        }

        if (setup.fanAuto) {
            if (threshold) {
                if (!value.fan) {
                    turnOnFan();
                    value.fan = true;
                }
            } else {
                long currentTime = System.currentTimeMillis();
                if (!value.fan) {
                    long stopTime = setup.fanStopTime * 60 * 1000L;
                    if (currentTime - mMainFanWorkEndTime >= stopTime) {
                        turnOnFan();
                        value.fan = true;
                    }
                }
                if (value.fan) {
                    long workTime = setup.fanWorkTime * 60 * 1000L;
                    if (currentTime - mMainFanWorkStartTime >= workTime) {
                        turnOffFan();
                        value.fan = false;
                    }
                }
            }
        }
    }

    private HardwareValue getHardwareValue() {
        Log.d(TAG, "Get Hardware value: " + mLogTimeFormat.format(new Date(System.currentTimeMillis())));
        try {
            TVOCs tvocs1 = mManager.getTVOCsInstance(0);
            TVOCs.ENVData env1 = tvocs1.getEnvdata();
            Log.d(TAG, "TVOCs1 value PPB:" + env1.concentration_ppb + " Range:" + env1.range + " PM3:" + env1.concentration_ugpmm3 + " TEMP:" + env1.temp + " HUMI:" + env1.humi);
            TVOCValue value1 = new TVOCValue();
            value1.humi = env1.humi;
            value1.ppb = env1.concentration_ppb;
            value1.ugpmm3 = env1.concentration_ugpmm3;
            value1.range = env1.range;
            value1.temperature = env1.temp;

            TVOCValue value2 = new TVOCValue();
            if (CtrlFunc.getMainTVOCModelCount(HardwareService.this) == 2) {
                TVOCs tvocs2 = mManager.getTVOCsInstance(1);
                TVOCs.ENVData env2 = tvocs2.getEnvdata();
                Log.d(TAG, "TVOCs2 value PPB:" + env2.concentration_ppb + " Range:" + env2.range + " PM3:" + env2.concentration_ugpmm3 + " TEMP:" + env2.temp + " HUMI:" + env2.humi);
                value2.humi = env2.humi;
                value2.ppb = env2.concentration_ppb;
                value2.ugpmm3 = env2.concentration_ugpmm3;
                value2.range = env2.range;
                value2.temperature = env2.temp;
            }

            boolean fan, red, green, lock;
            if (isSingleDevice) {
                byte s = mManager.getSwitches().Query();
                byte[] switchStates = Util.getBooleanArray(s);
                fan = switchStates[7] == 1;
                red = switchStates[6] == 1;
                green = switchStates[5] == 1;
                lock = switchStates[4] == 1;
            } else {
                fan = isMainDeviceFanRun;
                red = isMainDeviceLightRed;
                green = isMainDeviceLightGreen;
                lock = true;
            }

            TVOCsValue value = new TVOCsValue();
            value.TVOC1 = value1;
            value.TVOC2 = value2;

            HardwareValue hValue = new HardwareValue();
            hValue.createTime = System.currentTimeMillis();
            hValue.tvoCsValue = value;
            hValue.fan = fan;
            hValue.lock = lock;
            hValue.redLight = red;
            hValue.greenLight = green;

            if (!isSingleDevice) {
                synchronized (mWaitLocker) {
                    waitForHardwareOPGap("Get Hardware Value");
                    for (int i = 0; i < mSubCabinetSize; i++) {
                        SubBoard subBoard = mManager.getSubBoardInstance(i);
                        SubBoard.StatusData data = subBoard.getStatus();
                        sleep();
                        hValue.subBoardStatusData.add(data);
                    }
                    registerHardwareOPTime("Get Hardware Value");
                }
            }

            Log.d(TAG, "Fan value :" + fan + " Locker value::" + lock);
            HardwareValue._Cache = hValue;
            return hValue;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void turnOnFan() {
        if (mManager == null) {
            return;
        }
        if (isSingleDevice) {
            mManager.getSwitches().SwitchControl(0, true);
        } else {
            synchronized (mWaitLocker) {
                waitForHardwareOPGap("Turn on fan");
                SubBoard subBoard = mManager.getSubBoardInstance(mSubCabinetSize);
                subBoard.Control(SubBoard.ControlType.ECONTROL_FAN, true);
                isMainDeviceFanRun = true;
                registerHardwareOPTime("Turn on fan");
            }
        }
        mMainFanWorkStartTime = System.currentTimeMillis();
    }

    private void turnOnFan(int index) {
        if (mManager != null && !isSingleDevice) {
            synchronized (mWaitLocker) {
                waitForHardwareOPGap("Turn on fan");
                SubBoard subBoard = mManager.getSubBoardInstance(index);
                subBoard.Control(SubBoard.ControlType.ECONTROL_FAN, true);
                registerHardwareOPTime("Turn on fan");
                isSubDevicesFanRun[index] = true;
                mSubFanWorkStartTime[index] = System.currentTimeMillis();
            }
        }
    }

    private void turnOnAllSubFan() {
        if (mManager != null && !isSingleDevice) {
            synchronized (mWaitLocker) {
                waitForHardwareOPGap("Turn on fan");
                for (int index = 0; index < mSubCabinetSize; index++) {
                    SubBoard subBoard = mManager.getSubBoardInstance(index);
                    subBoard.Control(SubBoard.ControlType.ECONTROL_FAN, true);
                    isSubDevicesFanRun[index] = true;
                    mSubFanWorkStartTime[index] = System.currentTimeMillis();
                    sleep();
                }
                registerHardwareOPTime("Turn on fan");
            }
        }
    }

    private void turnOffFan() {
        if (mManager == null) {
            return;
        }
        if (isSingleDevice) {
            mManager.getSwitches().SwitchControl(0, false);
        } else {
            synchronized (mWaitLocker) {
                waitForHardwareOPGap("Turn off fan");
                SubBoard subBoard = mManager.getSubBoardInstance(mSubCabinetSize);
                subBoard.Control(SubBoard.ControlType.ECONTROL_FAN, false);
                registerHardwareOPTime("Turn off fan");
                isMainDeviceFanRun = false;
            }
        }
        mMainFanWorkEndTime = System.currentTimeMillis();
    }

    private void turnOffFan(int index) {
        if (mManager != null && !isSingleDevice) {
            synchronized (mWaitLocker) {
                waitForHardwareOPGap("Turn off fan");
                SubBoard subBoard = mManager.getSubBoardInstance(index);
                subBoard.Control(SubBoard.ControlType.ECONTROL_FAN, false);
                registerHardwareOPTime("Turn off fan");
                isSubDevicesFanRun[index] = false;
                mSubFanWorkEndTime[index] = System.currentTimeMillis();
            }
        }

    }

    private void lightGreen() {
        if (mManager == null) {
            return;
        }
        if (isSingleDevice) {
            mManager.getSwitches().SwitchControl(1, false);
            mManager.getSwitches().SwitchControl(2, true);
        } else {
            synchronized (mWaitLocker) {
                waitForHardwareOPGap("Light green");
                SubBoard subBoard = mManager.getSubBoardInstance(mSubCabinetSize);
                subBoard.Control(SubBoard.ControlType.ECONTROL_ALARM, false);
                registerHardwareOPTime("Light green");
                isMainDeviceLightGreen = true;
                isMainDeviceLightRed = false;
            }
        }
    }

    private void lightGreen(int index) {
        if (mManager != null && !isSingleDevice) {
            synchronized (mWaitLocker) {
                waitForHardwareOPGap("Light green");
                SubBoard subBoard = mManager.getSubBoardInstance(index);
                subBoard.Control(SubBoard.ControlType.ECONTROL_ALARM, false);
                registerHardwareOPTime("Light green");
                isSubDevicesLightGreen[index] = true;
                isSubDevicesLightRed[index] = false;
            }
        }
    }

    private void lightGreenAllSub() {
        if (mManager != null && !isSingleDevice) {
            synchronized (mWaitLocker) {
                waitForHardwareOPGap("Light green all sub");
                for (int index = 0; index < mSubCabinetSize; index++) {
                    SubBoard subBoard = mManager.getSubBoardInstance(index);
                    subBoard.Control(SubBoard.ControlType.ECONTROL_ALARM, false);
                    isSubDevicesLightGreen[index] = true;
                    isSubDevicesLightRed[index] = false;
                    sleep();
                }
                registerHardwareOPTime("Light green all sub");
            }
        }
    }

    private void lightRed() {
        if (mManager == null) {
            return;
        }
        if (isSingleDevice) {
            mManager.getSwitches().SwitchControl(1, true);
            mManager.getSwitches().SwitchControl(2, false);
        } else {
            synchronized (mWaitLocker) {
                waitForHardwareOPGap("Light red");
                SubBoard subBoard = mManager.getSubBoardInstance(mSubCabinetSize);
                subBoard.Control(SubBoard.ControlType.ECONTROL_ALARM, true);
                registerHardwareOPTime("Light red");
                isMainDeviceLightGreen = false;
                isMainDeviceLightRed = true;
            }
        }
    }

    private void lightRed(int index) {
        if (mManager != null && !isSingleDevice) {
            synchronized (mWaitLocker) {
                waitForHardwareOPGap("Light red");
                SubBoard subBoard = mManager.getSubBoardInstance(index);
                subBoard.Control(SubBoard.ControlType.ECONTROL_ALARM, true);
                registerHardwareOPTime("Light red");
                isSubDevicesLightGreen[index] = false;
                isSubDevicesLightRed[index] = true;
            }
        }
    }


    public synchronized void switchFanControl(boolean onOff) {
        if (onOff) {
            turnOnFan();
        } else {
            turnOffFan();
        }
        HardwareValue value = getHardwareValue();
        notifyValue(value);
    }

    /**
     * 子板开锁
     *
     * @param index 子板index
     * @return 子板操作结果
     */
    public SubBoard.ControlResult switchLockerControl(int index) {

        synchronized (mWaitLocker) {
            if (mManager == null) {
                return null;
            }
            String indexKey = String.valueOf(index);
            if (mLockerAutoUnlockTimer.containsKey(indexKey)) {
                Objects.requireNonNull(mLockerAutoUnlockTimer.get(indexKey)).cancel();
                mLockerAutoUnlockTimer.remove(indexKey);
            }
            waitForHardwareOPGap("Switch locker ,index:" + index);
            SubBoard.ControlResult result = mManager.getSubBoardInstance(index).control(SubBoard.ControlType.ECONTROL_LOCK, true);
            registerHardwareOPTime("Switch locker ,index:" + index);
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    synchronized (mWaitLocker) {
                        waitForHardwareOPGap("Switch unlock ,index:" + index);
                        mManager.getSubBoardInstance(index).control(SubBoard.ControlType.ECONTROL_LOCK, false);
                        registerHardwareOPTime("Switch unlock ,index:" + index);
                    }
                    mLockerAutoUnlockTimer.remove(indexKey);
                }
            }, mPeriods[1] * 1000L);
            mLockerAutoUnlockTimer.put(indexKey, timer);
            return result;
        }
    }

    /**
     * 子板关锁
     *
     * @param index 子板index
     * @return 子板操作结果
     */
    public SubBoard.ControlResult switchLockerControlUnlock(int index) {

        synchronized (mWaitLocker) {
            if (mManager == null) {
                return null;
            }
            String indexKey = String.valueOf(index);
            if (mLockerAutoUnlockTimer.containsKey(indexKey)) {
                Objects.requireNonNull(mLockerAutoUnlockTimer.get(indexKey)).cancel();
                mLockerAutoUnlockTimer.remove(indexKey);
            }
            waitForHardwareOPGap("Switch unlock,index:" + index);
            SubBoard.ControlResult result = mManager.getSubBoardInstance(index).control(SubBoard.ControlType.ECONTROL_LOCK, false);
            registerHardwareOPTime("Switch unlock,index:" + index);
            return result;
        }
    }

    public synchronized void switchLockerControl(boolean onOff) {
        if (mManager == null) {
            return;
        }
        mManager.getSwitches().SwitchControl(3, onOff);
        if (mAutoLockTimer != null) {
            mAutoLockTimer.cancel();
        }
        if (onOff) {
            mAutoLockTimer = new Timer();
            mAutoLockTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    switchLockerControl(false);
                }
            }, 120 * 1000);
        }
        HardwareValue value = getHardwareValue();
        notifyValue(value);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Destroy Service");
        try {
            if (mHardwareValueQueryTimer != null) {
                isDoHardwareValueQuery.set(false);
                mHardwareValueQueryTimer.cancel();
                mHardwareValueQueryTimer = null;
            }
            if (mManager != null) {
                mManager = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (mAutoLockTimer != null) {
                mAutoLockTimer.cancel();
                mAutoLockTimer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private static class MaxValue {
        long ppb;
        float temp;
    }

    public class HardwareServiceBinder extends Binder {
        public HardwareService getHardwareService() {
            return HardwareService.this;
        }
    }
}