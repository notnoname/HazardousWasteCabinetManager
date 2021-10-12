package me.liuzs.cabinetmanager.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

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
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import me.liuxy.cabinet.CabinetManager;
import me.liuxy.cabinet.Steelyard;
import me.liuxy.cabinet.SubBoard;
import me.liuxy.cabinet.TDA09C;
import me.liuxy.cabinet.TVOCs;
import me.liuzs.cabinetmanager.BuildConfig;
import me.liuzs.cabinetmanager.CabinetApplication;
import me.liuzs.cabinetmanager.CabinetCore;
import me.liuzs.cabinetmanager.Config;
import me.liuzs.cabinetmanager.model.Cabinet;
import me.liuzs.cabinetmanager.model.HardwareValue;
import me.liuzs.cabinetmanager.model.TVOCValue;
import me.liuzs.cabinetmanager.model.TVOCsValue;
import me.liuzs.cabinetmanager.net.RemoteAPI;
import me.liuzs.cabinetmanager.util.Util;

public class HardwareService extends Service {

    public static final String TAG = "HardwareService";
    @SuppressLint("SimpleDateFormat")
    public static final SimpleDateFormat mLogTimeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    public static int Qos = 2;
    private static CabinetManager mManager;
    private final String PublisherID = MqttClient.generateClientId();
    private final HardwareServiceBinder mBinder = new HardwareServiceBinder();
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
    private int[] mPeriods;
    private Timer mHardwareValueQueryTimer;
    private MqttAndroidClient mPublishClient;
    //主柜风扇起止时间
    private long mMainFanWorkStartTime = System.currentTimeMillis(), mMainFanWorkEndTime = System.currentTimeMillis();
    private Timer mAutoLockTimer;
    private long mHardwareValueQueryInterval;

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

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private void initCabinetManager() {
        Cabinet cabinet = CabinetCore.getCabinetInfo();
        if (cabinet == null || !CabinetApplication.getInstance().isInitHardwareManager()) {
            return;
        }
        if (mManager != null) {
            return;
        }
        if (BuildConfig.DEBUG) {
            return;
        }

        CabinetManager.Settings settings = new CabinetManager.Settings();
        settings.SwitchesDeviceName = "/dev/ttysWK0";
        settings.SubBoardConfigs = new CabinetManager.SubBoardConfig[0];
        settings.TDA09C485Configs = new CabinetManager.TDA09C485Config[1];
        int c485 = CabinetCore.getTDA09C485Config(this);
        settings.TDA09C485Configs[0] = new CabinetManager.TDA09C485Config((byte) c485);//按实际设备地址填写
        settings.TVOCsDeviceName = new String[2];
        settings.TVOCsDeviceName[0] = "/dev/ttysWK2";
        settings.TVOCsDeviceName[1] = "/dev/ttysWK3";
        settings.SHT3xDeviceName = "/dev/i2c-7";
        mManager = new CabinetManager(settings);
        lightGreen();
        turnOffFan();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "Service StartCommand");
        if (mPublishClient == null) {
            mPublishClient = new MqttAndroidClient(this, RemoteAPI.MQTT.MQTT_ROOT, PublisherID);
        }
        mPeriods = CabinetCore.getSubBoardPeriod(this);
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
        String valueJSON = CabinetCore.GSON.toJson(value);
        Log.d(TAG, "Hardware JSON:" + valueJSON);
        intent.putExtra(Config.KEY_HARDWARE_VALUE, valueJSON);
        sendBroadcast(intent);
    }

    private void publishValue(HardwareValue value) {
        Cabinet info = CabinetCore.getCabinetInfo();
//        if (info != null) {
//            DeviceInfo single = CabinetApplication.getSingleDevice();
//            String mainDevId = single != null ? single.devId : null;
//            List<String> devIds = new LinkedList<>();
//            for (DeviceInfo device : info.devices) {
//                devIds.add(device.devId);
//            }
//            publishValue(value, 1, info.id, mainDevId, devIds);
//        }
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

                String payload = CabinetCore.GSON.toJson(jsValue);

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
            if (CabinetCore.getMainTVOCModelCount(HardwareService.this) == 2) {
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
            byte s = mManager.getSwitches().Query();
            byte[] switchStates = Util.getBooleanArray(s);
            fan = switchStates[7] == 1;
            red = switchStates[6] == 1;
            green = switchStates[5] == 1;
            lock = switchStates[4] == 1;

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
        mManager.getSwitches().SwitchControl(0, true);
        mMainFanWorkStartTime = System.currentTimeMillis();
    }

    private void turnOffFan() {
        if (mManager == null) {
            return;
        }
        mManager.getSwitches().SwitchControl(0, false);
        mMainFanWorkEndTime = System.currentTimeMillis();
    }

    private void lightGreen() {
        if (mManager == null) {
            return;
        }
        mManager.getSwitches().SwitchControl(1, false);
        mManager.getSwitches().SwitchControl(2, true);
    }

    private void lightRed() {
        if (mManager == null) {
            return;
        }
        mManager.getSwitches().SwitchControl(1, true);
        mManager.getSwitches().SwitchControl(2, false);
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