package me.liuzs.cabinetmanager.service;

import android.content.Context;
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

import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpHeaders;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpRequestBase;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.util.EntityUtils;
import me.liuzs.cabinetmanager.CabinetCore;
import me.liuzs.cabinetmanager.model.HardwareValue;
import me.liuzs.cabinetmanager.model.User;
import me.liuzs.cabinetmanager.net.RemoteAPI;

public class DoorAccess {
    public static final String TAG = "DoorAccess";
    private static final String ROOT = "http://49.234.194.98:30900/";
    private static final String API_AccessToken = ROOT + "oauth/token?clientId=testclientId&clientSecret=testclientSecret";
    public static final String PublishTopicPre = "/one2one/device/";
    public static String SubScribeTopic;
    public static String PreSubScribeTopic;
    private final IMqttActionListener mMQTTSubscriptTopicAction = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            String[] topics = asyncActionToken.getTopics();
            if (topics != null) {
                for (String topic : topics) {
                    Log.d(TAG, "Subscript topic:" + topic);
                }
            }
            Log.d(TAG, "Subscript success");
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            if (exception != null) {
                Log.i(TAG, "Subscript failure: " + exception.getMessage());
            }
        }
    };
    private final MqttCallback mMqttCallback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            Log.i(TAG, "Callback-" + "connectionLost");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            Log.i(TAG, "Callback topic-" + topic);
            Log.i(TAG, "Callback message-" + new String(message.getPayload()));
        }

        @SuppressWarnings("unchecked")
        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            Object value = token.getUserContext();
            if (value instanceof List) {
                CabinetCore.setHardwareValueSent((List<HardwareValue>) value);
            }
            Log.i(TAG, "Callback-" + "deliver complete");
        }
    };
    private final IMqttMessageListener mMessageListener = (topic, message) -> {
        Log.i(TAG, "Message Topic:" + topic);
        String msg = new String(message.getPayload());
        Log.i(TAG, "Message:" + msg);
    };
    private final IMqttActionListener mMQTTUnSubscriptTopicAction = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            String[] topics = asyncActionToken.getTopics();
            if (topics != null) {
                for (String topic : topics) {
                    Log.d(TAG, "UnSubscript Topic:" + topic);
                }
            }
            Log.d(TAG, "UnSubscript success");
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            if (exception != null) {
                Log.i(TAG, "UnSubscript failure: " + exception.getMessage());
            }
        }
    };
    private final String MQTTClientID = MqttClient.generateClientId();
    private final Context mContext;
    private final IMqttActionListener mMQTTPublishAction = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            Log.d(TAG, "Publish success");
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            if (exception != null) {
                Log.i(TAG, "Publish failure: " + exception.getMessage());
            }
        }
    };
    private MqttAndroidClient mMQTTClient;
    private final IMqttActionListener mMQTTConnectAction = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            Log.d(TAG, "Connect success");
            subScriptTopics();
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            if (exception != null) {
                Log.i(TAG, "Connect failure:" + exception.getMessage());
            }
        }
    };

    public DoorAccess(Context context) {
        mContext = context;
    }

    private static void generalOptBaseHeader(HttpRequestBase httpRequest) {
        Header accept = new BasicHeader(HttpHeaders.ACCEPT, "application/json");
        httpRequest.addHeader(accept);
        User user = CabinetCore.getCabinetUser(CabinetCore.RoleType.Operator);
        if (user != null) {
            Header authorization = new BasicHeader("token", user.token);
            httpRequest.addHeader(authorization);
        }
    }

    private static MqttConnectOptions setUpConnectionOptions() {
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setUserName("men");
        connOpts.setPassword("".toCharArray());
        connOpts.setCleanSession(true);
        connOpts.setAutomaticReconnect(true);
        return connOpts;
    }

    private void subScriptTopics() {
        String[] preTopicFilter = new String[]{PreSubScribeTopic};
        String[] topicFilter = new String[]{SubScribeTopic};
        IMqttMessageListener[] messageListener = new IMqttMessageListener[]{mMessageListener};
        int[] qos = {2};
        if (PreSubScribeTopic != null) {
            try {
                mMQTTClient.unsubscribe(preTopicFilter, null, mMQTTUnSubscriptTopicAction);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            mMQTTClient.subscribe(topicFilter, qos, null, mMQTTSubscriptTopicAction, messageListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initMQTTClient(String clientId, String secret) {
        if (mMQTTClient == null) {
            mMQTTClient = new MqttAndroidClient(mContext, RemoteAPI.MQTT.MQTT_ROOT, MQTTClientID);
            mMQTTClient.setCallback(mMqttCallback);
        }
        new Thread(() -> {
            while (true) {
                AccessTokenResponse response = getAccessToken(clientId, secret);
                int expiresIn = 10;
                if (response != null) {
                    expiresIn = response.expiresIn;
                    if (!mMQTTClient.isConnected()) {
                        try {
                            MqttConnectOptions connOpts = setUpConnectionOptions();
                            mMQTTClient.connect(connOpts, null, mMQTTConnectAction);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        PreSubScribeTopic = SubScribeTopic;
                        SubScribeTopic = "/one2one/server/" + response.accessToken;
                        subScriptTopics();
                    }
                }
                try {
                    Thread.sleep(expiresIn * 1000L);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private AccessTokenResponse getAccessToken(String clientId, String clientSecret) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet method = new HttpGet(API_AccessToken);
            Log.d(TAG, method.getURI().toString());
            HttpResponse httpResponse = httpClient.execute(method);
            int code = httpResponse.getStatusLine().getStatusCode();
            if (code == 200) {
                HttpEntity entity = httpResponse.getEntity();
                String content = EntityUtils.toString(entity, "UTF-8");
                Log.d(TAG, content);
                return CabinetCore.GSON.fromJson(content, AccessTokenResponse.class);
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public void openDoor() {
        if (mMQTTClient.isConnected()) {
            try {
                long time = System.currentTimeMillis();
                HardwareService.MQTTCommand mc = new HardwareService.MQTTCommand();
                mc.t = CabinetCore.getDoorAccessMacAddress();
                mc.mi = String.valueOf(time / 1000);
                HardwareService.OpenDoorCommand openDoorCommand = new HardwareService.OpenDoorCommand();
                openDoorCommand.time = time / 1000;
                mc.m = openDoorCommand;
                String payload = CabinetCore.GSON.toJson(mc);
                MqttMessage message = new MqttMessage();
                message.setPayload(payload.getBytes());
                message.setQos(2);
                mMQTTClient.publish(PublishTopicPre + CabinetCore.getDoorAccessMacAddress(), message, mc, mMQTTPublishAction);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class AccessTokenResponse {
        public String msg;
        public int expiresIn;
        public int code;
        public String accessToken;
    }

}
