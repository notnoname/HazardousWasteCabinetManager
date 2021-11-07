package me.liuzs.cabinetmanager.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import me.liuxy.cabinet.CabinetManager;
import me.liuxy.cabinet.Steelyard;
import me.liuxy.cabinet.TDA09C;
import me.liuzs.cabinetmanager.CabinetApplication;
import me.liuzs.cabinetmanager.CabinetCore;
import me.liuzs.cabinetmanager.Config;
import me.liuzs.cabinetmanager.model.Cabinet;
import me.liuzs.cabinetmanager.model.HardwareValue;
import me.liuzs.cabinetmanager.model.modbus.AirConditionerStatus;
import me.liuzs.cabinetmanager.model.modbus.EnvironmentStatus;
import me.liuzs.cabinetmanager.model.modbus.FrequencyConverterStatus;
import me.liuzs.cabinetmanager.model.modbus.SetupValue;
import me.liuzs.cabinetmanager.model.modbus.StatusOption;
import me.liuzs.cabinetmanager.net.RemoteAPI;

public class HardwareService extends Service {

    public static final String TAG = "HardwareService";
    public static final String MQTT = "MQTT";
    @SuppressLint("SimpleDateFormat")
    public static final SimpleDateFormat mLogTimeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    private static final long mHardwareValueQueryInterval = 10000;
    public static int Qos = 2;
    private static CabinetManager mManager;
    private final String MQTTClientID = MqttClient.generateClientId();
    private final HardwareServiceBinder mBinder = new HardwareServiceBinder();
    private final IMqttActionListener mMQTTPublishAction = new IMqttActionListener() {
        @Override
        @SuppressWarnings("unchecked")
        public void onSuccess(IMqttToken asyncActionToken) {
            String[] topics = asyncActionToken.getTopics();
            if (topics != null) {
                for (String topic : topics) {
                    Log.d(MQTT, "Publish topic:" + topic);
                }
            }
            Object value = asyncActionToken.getUserContext();
            if (value instanceof List) {
                CabinetCore.setHardwareValueSent((List<HardwareValue>) value);
            }
            Log.d(MQTT, "Publish success");
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            if (exception != null) {
                Log.i(MQTT, "Publish failure: " + exception.getMessage());
            }
            Object value = asyncActionToken.getUserContext();
            if (value instanceof List) {
                CabinetCore.saveSentFailHardwareValueList((List<HardwareValue>) value);
            }

        }
    };
    private final IMqttActionListener mMQTTSubscriptTopicAction = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            String[] topics = asyncActionToken.getTopics();
            if (topics != null) {
                for (String topic : topics) {
                    Log.d(MQTT, "Subscript topic:" + topic);
                }
            }
            Log.d(MQTT, "Subscript success");
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            if (exception != null) {
                Log.i(MQTT, "Subscript failure: " + exception.getMessage());
            }
        }
    };
    private final IMqttActionListener mMQTTUnSubscriptTopicAction = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            String[] topics = asyncActionToken.getTopics();
            if (topics != null) {
                for (String topic : topics) {
                    Log.d(MQTT, "UnSubscript Topic:" + topic);
                }
            }
            Log.d(MQTT, "UnSubscript success");
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            if (exception != null) {
                Log.i(MQTT, "UnSubscript failure: " + exception.getMessage());
            }
        }
    };
    private final AtomicBoolean isDoHardwareValueQuery = new AtomicBoolean(false);
    private final MqttCallback mMqttCallback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            Log.i(MQTT, "Callback-" + "connectionLost");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            Log.i(MQTT, "Callback topic-" + topic);
            Log.i(MQTT, "Callback message-" + new String(message.getPayload()));
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            Log.i(MQTT, "Callback-" + "deliverComplete");
        }
    };
    private final IMqttMessageListener mMessageListener = (topic, message) -> {
        Log.i(MQTT, "Message Topic:" + topic);
        Log.i(MQTT, "Message:" + new String(message.getPayload()));
    };
    private Timer mHardwareValueQueryTimer;
    private MqttAndroidClient mPublishClient;
    private final IMqttActionListener mMQTTConnectAction = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            Log.d(MQTT, "Connect success");
            subScriptTopics();
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            if (exception != null) {
                Log.i(MQTT, "Connect failure:" + exception.getMessage());
            }
        }
    };
    private Timer mAutoLockTimer;

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
        connOpts.setAutomaticReconnect(true);
        return connOpts;
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

        CabinetManager.Settings settings = new CabinetManager.Settings();
        settings.SubBoardConfigs = new CabinetManager.SubBoardConfig[0];
        settings.TDA09C485Configs = new CabinetManager.TDA09C485Config[1];
        int c485 = CabinetCore.getTDA09C485Config(this);
        //按实际设备地址填写
        settings.TDA09C485Configs[0] = new CabinetManager.TDA09C485Config((byte) c485);
        settings.TVOCsDeviceName = new String[0];
        mManager = new CabinetManager(settings);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "Service StartCommand");
        initMQTTClient();
        initCabinetManager();
        initHardwareValueQueryTimer();
        return super.onStartCommand(intent, flags, startId);
    }

    private void initMQTTClient() {
        if (mPublishClient == null) {
            mPublishClient = new MqttAndroidClient(this, RemoteAPI.MQTT.MQTT_ROOT, MQTTClientID);
            mPublishClient.setCallback(mMqttCallback);
        }
        if (!mPublishClient.isConnected()) {
            try {
                MqttConnectOptions connOpts = setUpConnectionOptions();
                mPublishClient.connect(connOpts, null, mMQTTConnectAction);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void subScriptTopics() {
        String[] topicFilter = new String[]{RemoteAPI.MQTT.MQTT_ControlTopic, RemoteAPI.MQTT.MQTT_SetupValueTopic};
        IMqttMessageListener[] messageListener = new IMqttMessageListener[]{mMessageListener, mMessageListener};
        int[] qos = {Qos, Qos};
        try {
            mPublishClient.unsubscribe(topicFilter, null, mMQTTUnSubscriptTopicAction);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            mPublishClient.subscribe(topicFilter, qos, null, mMQTTSubscriptTopicAction, messageListener);
        } catch (Exception e) {
            e.printStackTrace();
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
                initMQTTClient();
                List<HardwareValue> values = CabinetCore.getUnSentHardwareValueList();
                HardwareValue value = getHardwareValue();
                notifyValue(value);
                values.add(value);
                publishValue(values);
                isDoHardwareValueQuery.set(false);
            }
        }, 1000, mHardwareValueQueryInterval);
    }

    private void notifyValue(HardwareValue value) {
        if (value == null) {
            return;
        }
        Intent intent = new Intent(Config.ACTION_HARDWARE_VALUE_SEND);
        String valueJSON = CabinetCore.GSON.toJson(value);
        intent.putExtra(Config.KEY_HARDWARE_VALUE, valueJSON);
        sendBroadcast(intent);
    }

    private void publishValue(List<HardwareValue> valueList) {
        Cabinet info = CabinetCore.getCabinetInfo();
        if (info != null && valueList != null) {
            if (mPublishClient.isConnected()) {
                try {
                    String payload = CabinetCore.GSON.toJson(valueList);
                    MqttMessage message = new MqttMessage();
                    message.setPayload(payload.getBytes());
                    message.setQos(Qos);
                    mPublishClient.publish(RemoteAPI.MQTT.MQTT_HARDWARE_PUBLISH_TOPIC, message, valueList, mMQTTPublishAction);
                } catch (Exception e) {
                    e.printStackTrace();
                    CabinetCore.saveSentFailHardwareValueList(valueList);
                }
            } else {
                CabinetCore.saveSentFailHardwareValueList(valueList);
            }
        }
    }

    private HardwareValue getHardwareValue() {
        Cabinet cabinet = CabinetCore.getCabinetInfo();
        if (cabinet == null) {
            return null;
        }
        long createTime = System.currentTimeMillis();
        Log.d(TAG, "Get HardwareValue: " + mLogTimeFormat.format(new Date(createTime)));
        try {
            HardwareValue hValue = new HardwareValue();
            SetupValue setupValue = ModbusService.readSetupValue();
            if (setupValue.e == null) {
                hValue.setupValue = setupValue;
            } else {
                CabinetCore.logAlert("设置参数获取失败");
            }
            EnvironmentStatus environmentStatus = ModbusService.readEnvironmentStatus();
            if (environmentStatus.e == null) {
                hValue.environmentStatus = environmentStatus;
            } else {
                CabinetCore.logAlert("环境信息获取失败");
            }
            AirConditionerStatus airConditionerStatus = ModbusService.readAirConditionerStatus();
            if (airConditionerStatus.e == null) {
                hValue.airConditionerStatus = airConditionerStatus;
            } else {
                CabinetCore.logAlert("控台状态获取失败");
            }
            FrequencyConverterStatus frequencyConverterStatus = ModbusService.readFrequencyConverterStatus();
            if (frequencyConverterStatus.e == null) {
                hValue.frequencyConverterStatus = frequencyConverterStatus;
            } else {
                CabinetCore.logAlert("变频器状态获取失败");
            }
            StatusOption statusOption = ModbusService.readStatusOption();
            if (statusOption.e == null) {
                hValue.statusOption = statusOption;
            } else {
                CabinetCore.logAlert("状态参数获取失败");
            }
            hValue.createTime = createTime;
            hValue.cabinet_id = cabinet.id;

            if (hValue.setupValue == null && hValue.statusOption == null && hValue.airConditionerStatus == null && hValue.environmentStatus == null && hValue.frequencyConverterStatus == null) {
                return null;
            } else {
                Log.d(TAG, "HardwareValue:" + CabinetCore.GSON.toJson(hValue));
                return hValue;
            }
        } catch (Exception e) {
            CabinetCore.logAlert("获取硬件数据异常");
            return null;
        }
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

    public class HardwareServiceBinder extends Binder {
        public HardwareService getHardwareService() {
            return HardwareService.this;
        }
    }
}