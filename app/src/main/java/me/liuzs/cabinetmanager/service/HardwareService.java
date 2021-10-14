package me.liuzs.cabinetmanager.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.serotonin.modbus4j.Modbus;

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
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import me.liuxy.cabinet.CabinetManager;
import me.liuxy.cabinet.Steelyard;
import me.liuxy.cabinet.TDA09C;
import me.liuzs.cabinetmanager.BuildConfig;
import me.liuzs.cabinetmanager.CabinetApplication;
import me.liuzs.cabinetmanager.CabinetCore;
import me.liuzs.cabinetmanager.Config;
import me.liuzs.cabinetmanager.model.AirConditionerStatus;
import me.liuzs.cabinetmanager.model.Cabinet;
import me.liuzs.cabinetmanager.model.EnvironmentStatus;
import me.liuzs.cabinetmanager.model.HardwareValue;
import me.liuzs.cabinetmanager.model.SetupValue;
import me.liuzs.cabinetmanager.net.RemoteAPI;

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
            Log.d(TAG, "MQTT Publish success");
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            if (exception != null) {
                Log.i(TAG, "MQTT Publish failure: " + exception.getMessage());
            }
        }
    };
    private final AtomicBoolean isDoHardwareValueQuery = new AtomicBoolean(false);
    private Timer mHardwareValueQueryTimer;
    private MqttAndroidClient mPublishClient;
    private Timer mAutoLockTimer;
    private static final long mHardwareValueQueryInterval = 5000;

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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "Service StartCommand");
        if (mPublishClient == null) {
            mPublishClient = new MqttAndroidClient(this, RemoteAPI.MQTT.MQTT_ROOT, PublisherID);
        }
//        initMQTTPublishClient();
        initCabinetManager();
        initHardwareValueQueryTimer();
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

    private void initHardwareValueQueryTimer() {
        if (mHardwareValueQueryTimer != null) {
            isDoHardwareValueQuery.set(false);
            mHardwareValueQueryTimer.cancel();
            mHardwareValueQueryTimer = null;
        }
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
                String payload = CabinetCore.GSON.toJson(value);
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

    private HardwareValue getHardwareValue() {
        long createTime = System.currentTimeMillis();
        Log.d(TAG, "Get HardwareValue: " + mLogTimeFormat.format(new Date(createTime)));
        try {
            HardwareValue hValue = new HardwareValue();
            SetupValue setupValue = ModbusService.readSetupValue();
            if (setupValue.e == null) {
                hValue.setupValue = setupValue;
            }
            EnvironmentStatus environmentStatus = ModbusService.readEnvironmentStatus();
            if (environmentStatus.e == null) {
                hValue.environmentStatus = environmentStatus;
            }
            AirConditionerStatus airConditionerStatus = ModbusService.readAirConditionerStatus();
            if (airConditionerStatus.e == null) {
                hValue.airConditionerStatus = airConditionerStatus;
            }
            HardwareValue._Cache = hValue;
            Log.d(TAG, "HardwareValue:" + CabinetCore.GSON.toJson(hValue));
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
    }

    private void turnOffFan() {
        if (mManager == null) {
            return;
        }
        mManager.getSwitches().SwitchControl(0, false);
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