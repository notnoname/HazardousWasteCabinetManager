package me.liuzs.cabinetmanager.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.apache.log4j.BasicConfigurator;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import io.moquette.broker.Server;
import io.moquette.broker.config.MemoryConfig;
import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.InterceptAcknowledgedMessage;
import io.moquette.interception.messages.InterceptConnectMessage;
import io.moquette.interception.messages.InterceptConnectionLostMessage;
import io.moquette.interception.messages.InterceptDisconnectMessage;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.moquette.interception.messages.InterceptSubscribeMessage;
import io.moquette.interception.messages.InterceptUnsubscribeMessage;

public class MQTTBrokerService extends Service {

    public static String TAG = "MQTTService";

    public static String InterceptHandlerID = "MQTTServiceInterceptHandler";

    private final Server mMQTTServer;
    private final InterceptHandler mInterceptHandler = new InterceptHandler() {

        @Override
        public String getID() {
            return InterceptHandlerID;
        }

        @Override
        public Class<?>[] getInterceptedMessageTypes() {
            return null;
        }

        @Override
        public void onConnect(InterceptConnectMessage message) {
            String clientId = message.getClientID();
            Log.d(TAG, "Connect, Client id:" + clientId);
        }

        @Override
        public void onDisconnect(InterceptDisconnectMessage message) {
            String clientId = message.getClientID();
            Log.d(TAG, "Disconnect, Client id:" + clientId);
        }

        @Override
        public void onConnectionLost(InterceptConnectionLostMessage message) {
            String clientId = message.getClientID();
            Log.d(TAG, "Disconnect, Client id:" + clientId);
        }

        @Override
        public void onPublish(InterceptPublishMessage message) {
            String clientId = message.getClientID();
            Log.d(TAG, "Publish, Client id:" + clientId);
        }

        @Override
        public void onSubscribe(InterceptSubscribeMessage message) {
            String clientId = message.getClientID();
            Log.d(TAG, "Subscribe, Client id:" + clientId);
        }

        @Override
        public void onUnsubscribe(InterceptUnsubscribeMessage message) {
            String clientId = message.getClientID();
            Log.d(TAG, "Unsubscribe, Client id:" + clientId);
        }

        @Override
        public void onMessageAcknowledged(InterceptAcknowledgedMessage message) {
            String topic = message.getTopic();
            Log.d(TAG, "MessageAcknowledged, topic" + topic);
        }
    };

    public MQTTBrokerService() {
        mMQTTServer = new Server();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        BasicConfigurator.configure();
        Log.d(TAG, "startServer");
        try {
            MemoryConfig memoryConfig = new MemoryConfig(new Properties());
            List<InterceptHandler> handlers = new LinkedList<>();
            handlers.add(mInterceptHandler);
            mMQTTServer.startServer(memoryConfig, handlers);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            mMQTTServer.stopServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "stopServer");
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}