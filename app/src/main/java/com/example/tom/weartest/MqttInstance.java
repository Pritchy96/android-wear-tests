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
            System.out.print("Connecting to " + serverUri);
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
                    System.out.print("Failed to connect to: " + serverUri);
                }
            });
        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }

    public void publishMessage(String topic, String content){
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(content.getBytes());
            mqttAndroidClient.publish(topic, message);
            System.out.print("Message Published");
            if(!mqttAndroidClient.isConnected()){
                System.out.print(mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
            }
        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
