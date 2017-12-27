package com.example.tom.weartest;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.wear.widget.WearableLinearLayoutManager;
import android.support.wearable.activity.WearableActivity;
import android.support.wear.widget.WearableRecyclerView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends WearableActivity {

    private List<itemEntry> itemList = new ArrayList<>();
    private WearableRecyclerView recyclerView;
    private testAdapter listAdapter;
    private OnItemClickListener listClickListener;
    MqttAndroidClient mqttAndroidClient;

    final String serverUri = "tcp://test.mosquitto.org:1883";
    String clientId = "TAC";
    final String subscriptionTopic = "TAST";
    final String publishTopic = "TAPT";
    final String publishContent = "TAPC";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listClickListener = new OnItemClickListener() {
            @Override
            public void onItemClick(itemEntry item) {
                Toast.makeText(getApplicationContext(), "Item Clicked: " + item.getText(), Toast.LENGTH_LONG).show();
            }
        };

        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);

        recyclerView.setCircularScrollingGestureEnabled(true);
        recyclerView.setEdgeItemsCenteringEnabled(true);

        listAdapter = new testAdapter(itemList, listClickListener);
        RecyclerView.LayoutManager mLayoutManager = new WearableLinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, WearableLinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(listAdapter);

        for (int i = 0; i < 60; i++) {
            itemList.add(new itemEntry(Integer.toString(i)));
        }
        listAdapter.notifyDataSetChanged();

        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                if (reconnect) {
                    addToHistory("Reconnected to : " + serverURI);
                } else {
                    addToHistory("Connected to: " + serverURI);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                addToHistory("The Connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                addToHistory("Incoming message: " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                addToHistory("Delivery complete");
            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);

        try {
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
                    publishMessage();
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


    private void addToHistory(String mainText){
        System.out.println("LOG: " + mainText);
        //Snackbar.make(findViewById(android.R.id.content), mainText, Snackbar.LENGTH_LONG)
          //      .setAction("Action", null).show();

    }

    public void publishMessage(){

        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(publishContent.getBytes());
            mqttAndroidClient.publish(publishTopic, message);
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
