package me.liuzs.cabinetmanager;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import me.liuzs.cabinetmanager.ui.mqttclient.MQTTClientFragment;

public class MQTTClientActivity extends AppCompatActivity {

    public static final String TAG = "MQTTClient";
    public static final String BrokerHost = "tcp://localhost:1883";
    public static final String PublisherID = MqttClient.generateClientId();
    public static final String SubscriberID = MqttClient.generateClientId();
    public static final String CabinetTopic = "CabinetManagerTopic";
    public static int Qos = 2;
    private MqttAndroidClient mSubscribeClient = null;
    private MqttAndroidClient mPublishClient = null;
    private IMqttActionListener mSubscribeActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            String[] topics = asyncActionToken.getTopics();
            if (topics != null) {
                for (String topic : topics) {
                    Log.d(TAG, "Subscribe Topic:\"" + topic + "\" success");
                }
            }
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            if (exception != null) {
                Log.i(TAG, "Subscribe failure: " + exception.getMessage());
            }
        }
    };

    private IMqttActionListener mPublishActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {

            Log.d(TAG, "Publish  success");
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            if (exception != null) {
                Log.i(TAG, "Publish failure: " + exception.getMessage());
            }
        }
    };

    private MqttCallback mCallback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            Log.i(TAG, "Connection lost");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            Log.i(TAG, "Topic: " + topic + ", msg: " + new String(message.getPayload()));
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            Log.i(TAG, "Message delivered");
        }
    };
    private int PayloadCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqtt_client);
        mSubscribeClient = new MqttAndroidClient(this, BrokerHost, SubscriberID);
        mPublishClient = new MqttAndroidClient(this, BrokerHost, PublisherID);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MQTTClientFragment.newInstance())
                    .commitNow();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mqtt_client_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_subscribe:
                Toast.makeText(this, "Start MQTT subscribe.", Toast.LENGTH_SHORT).show();
                processSubscribeTopic();
                break;
            case R.id.action_publish_tvocs:
                Toast.makeText(this, "Start TVOCs publish.", Toast.LENGTH_SHORT).show();
                processPublishTopic(CabinetTopic + "/" + "TVOCs", "TVOCs Sample payload" + PayloadCount++);
                break;
            case R.id.action_publish_fan:
                Toast.makeText(this, "Start Fan publish.", Toast.LENGTH_SHORT).show();
                processPublishTopic(CabinetTopic + "/" + "FAN", "Fan Sample payload" + PayloadCount++);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean processSubscribeTopic() {
        try {
            if (!mSubscribeClient.isConnected()) {
                mSubscribeClient.connect(null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        try {
                            mSubscribeClient.setCallback(mCallback);
                            mSubscribeClient.subscribe(CabinetTopic + "/#", Qos, null, mSubscribeActionListener);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        try {
                            mSubscribeClient.disconnect();
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void publishTopic(String topic, String payload) {
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(payload.getBytes());
            message.setQos(Qos);
            mPublishClient.publish(topic, message, null, mPublishActionListener);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private boolean processPublishTopic(String topic, String payload) {
        try {
            if (mPublishClient.isConnected()) {
                publishTopic(topic, payload);
            } else {
                mPublishClient.connect(null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        publishTopic(topic, payload);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        try {
                            mPublishClient.disconnect();
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPublishClient.isConnected()) {
            try {
                mPublishClient.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        if (mSubscribeClient.isConnected()) {
            try {
                mSubscribeClient.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }
}