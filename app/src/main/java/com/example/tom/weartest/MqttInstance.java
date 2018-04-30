package com.example.tom.weartest;

/**
 * Created by tom on 12/27/17.
 */

import android.content.Context;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import static org.eclipse.paho.client.mqttv3.MqttConnectOptions.MQTT_VERSION_3_1;

public class MqttInstance {
    MqttAndroidClient mqttAndroidClient;
    final String serverUri, clientId;
    final Context context;

    public MqttInstance(final Context context, String ServerUri, String clientId) {
        this.serverUri = ServerUri;
        this.clientId = clientId;
        this.context = context;

        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                if (reconnect) {
                    logger.log("Reconnected to : " + serverURI, context);
                } else {
                    logger.log("Connected to: " + serverURI, context);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                logger.log("The Connection was lost.", context);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                logger.log("Incoming message: " + new String(message.getPayload()), context);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                logger.log("Delivery complete", context);
            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);

        try {
            //todo: set up logger system.
            logger.log("Connecting to " + serverUri, context);
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    //todo: Move this to a 'connections' topic, or remove,
                    //todo: Add UID for device in content.
                    publishMessage("TestTopic", "Wear Device Connected");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    logger.log("Failed to connect to: " + serverUri, context);
                }
            });
        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }

    public void publishMessage(String topic, String content){
        if (!mqttAndroidClient.isConnected()) {
            logger.log("Attempted a publish before connecting!", context);

            return;
        }
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(content.getBytes());
            mqttAndroidClient.publish(topic, message);
            logger.log("Message Published", context);
            if(!mqttAndroidClient.isConnected()){
                logger.log(mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.", context);
            }
        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
