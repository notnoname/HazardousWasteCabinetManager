package me.liuzs.cabinetmanager.service;

import static me.liuzs.cabinetmanager.net.RemoteAPI.MQTT.PublishTopicPre;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
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
        public void onSuccess(IMqttToken asyncActionToken) {
            Log.d(MQTT, "Publish success");
            Object value = asyncActionToken.getUserContext();
            if (value instanceof List) {
                Log.d(MQTT, "Publish Hardware value: " + CabinetCore.GSON.toJson(value));
            } else if (value instanceof MQTTCommand) {
                Log.d(MQTT, "Publish Open door command: " + CabinetCore.GSON.toJson(value));
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            if (exception != null) {
                Log.i(MQTT, "Publish failure: " + exception.getMessage());
            } else {
                Log.i(MQTT, "Publish failure");
            }
            Object value = asyncActionToken.getUserContext();
            if (value instanceof List) {
                CabinetCore.saveSentFailHardwareValueList((List<HardwareValue>) value);
                Log.d(MQTT, "Save failure: " + CabinetCore.GSON.toJson(value));
            } else if (value instanceof MQTTCommand) {
                Log.d(MQTT, "Publish Open door command failure: " + CabinetCore.GSON.toJson(value));
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
            } else {
                Log.i(MQTT, "Subscript failure");
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
            } else {
                Log.i(MQTT, "UnSubscript failure");
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

        @SuppressWarnings("unchecked")
        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            Object value = token.getUserContext();
            if (value instanceof List) {
                Log.i(MQTT, CabinetCore.GSON.toJson(value));
                CabinetCore.setHardwareValueSent((List<HardwareValue>) value);
            }
            Log.i(MQTT, "Callback-" + "deliver complete");
        }
    };
    private final IMqttMessageListener mMessageListener = (topic, message) -> {
        if (!TextUtils.isEmpty(topic)) {
            Log.i(MQTT, "Message Topic:" + topic);
            String msg = new String(message.getPayload());
            Log.i(MQTT, "Message:" + msg);
            if (topic.startsWith(RemoteAPI.MQTT.Topic_SetupValue_Subscribe)) {
                processSetupCommand(msg);
            } else if (topic.startsWith(RemoteAPI.MQTT.Topic_Control_Subscribe)) {
                processControlCommand(msg);
            }
        }

    };
    private Timer mHardwareValueQueryTimer;
    private MqttAndroidClient mMQTTClient;
    private final IMqttActionListener mMQTTConnectAction = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            Log.d(MQTT, "Connect success");
            String id = asyncActionToken.getUserContext().toString();
            subScriptTopics(id);
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
        if (mMQTTClient == null) {
            mMQTTClient = new MqttAndroidClient(this, RemoteAPI.MQTT.MQTT_ROOT, MQTTClientID);
            mMQTTClient.setCallback(mMqttCallback);
        }
        if (!mMQTTClient.isConnected()) {
            Cabinet cabinet = CabinetCore.getCabinetInfo();
            if (cabinet == null || TextUtils.isEmpty(cabinet.id)) {
                return;
            }
            try {
                MqttConnectOptions connOpts = setUpConnectionOptions();
                mMQTTClient.connect(connOpts, cabinet.id, mMQTTConnectAction);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void subScriptTopics(String cabinetId) {
        String[] topicFilter = new String[]{RemoteAPI.MQTT.Topic_Control_Subscribe + cabinetId, RemoteAPI.MQTT.Topic_SetupValue_Subscribe + cabinetId};
        IMqttMessageListener[] messageListener = new IMqttMessageListener[]{mMessageListener, mMessageListener};
        int[] qos = {Qos, Qos};
        try {
            mMQTTClient.unsubscribe(topicFilter, null, mMQTTUnSubscriptTopicAction);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            mMQTTClient.subscribe(topicFilter, qos, null, mMQTTSubscriptTopicAction, messageListener);
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
                if (value != null) {
                    values.add(value);
                }
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
            if (mMQTTClient.isConnected()) {
                try {
                    String payload = CabinetCore.GSON.toJson(valueList);
                    MqttMessage message = new MqttMessage();
                    message.setPayload(payload.getBytes());
                    message.setQos(Qos);
                    mMQTTClient.publish(RemoteAPI.MQTT.Topic_Hardware_Value_Publish, message, valueList, mMQTTPublishAction);
                } catch (Exception e) {
                    e.printStackTrace();
                    CabinetCore.saveSentFailHardwareValueList(valueList);
                }
            } else {
                CabinetCore.saveSentFailHardwareValueList(valueList);
            }
        }
    }

    public boolean unlockRemoteDoor() {
        if (mMQTTClient.isConnected()) {
            try {
                long time = System.currentTimeMillis();
                MQTTCommand mc = new MQTTCommand();
                mc.t = CabinetCore.getDoorAccessMacAddress();
                mc.mi = String.valueOf(time / 1000);
                OpenDoorCommand openDoorCommand = new OpenDoorCommand();
                openDoorCommand.time = time / 1000;
                mc.m = openDoorCommand;
                String payload = CabinetCore.GSON.toJson(mc);
                MqttMessage message = new MqttMessage();
                message.setPayload(payload.getBytes());
                message.setQos(Qos);
                mMQTTClient.publish(PublishTopicPre + CabinetCore.getDoorAccessMacAddress(), message, mc, mMQTTPublishAction);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
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

    private void processControlCommand(String mqttMessage) {

    }

    private void processSetupCommand(String mqttMessage) {
        try {
            SetupValue setupValue = CabinetCore.GSON.fromJson(mqttMessage, SetupValue.class);
            ModbusService.saveSetupValue(setupValue);
            CabinetCore.logOpt("保存", "设置");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class OpenDoorCommand {
        public final String cmd = "open";
        public final int open_s = 1;
        public long time;
    }

    public static class MQTTCommand {
        public final int c = 12;
        public final String f = "server";
        public String t;
        public String mi;
        public OpenDoorCommand m;
    }

    public class HardwareServiceBinder extends Binder {
        public HardwareService getHardwareService() {
            return HardwareService.this;
        }
    }
}