package com.example.tom.weartest;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.wear.widget.WearableLinearLayoutManager;
import android.support.wearable.activity.WearableActivity;
import android.support.wear.widget.WearableRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends WearableActivity {

    MqttInstance mqttInstance;
    private List<ItemEntry> itemList = new ArrayList<>();
    private WearableRecyclerView recyclerView;
    private testAdapter listAdapter;
    private OnItemClickListener listClickListener;

    final String serverUri = "tcp://test.mosquitto.org:1883";
    String clientId = "TAC";
    final String publishTopic = "TestTopic";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mqttInstance = new MqttInstance(getApplicationContext(), serverUri, clientId);

        recyclerView = findViewById(R.id.recycler_view);

        listClickListener = new OnItemClickListener() {
            @Override
            public void onItemClick(ItemEntry item) {
                logger.log("Item Clicked: " + item.getText(), getApplicationContext());
                mqttInstance.publishMessage(publishTopic, item.getText());
            }
        };

        listAdapter = new testAdapter(itemList, listClickListener);

        recyclerView.setLayoutManager(new WearableLinearLayoutManager(getApplicationContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setCircularScrollingGestureEnabled(true);
        recyclerView.setEdgeItemsCenteringEnabled(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, WearableLinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(listAdapter);

        for (int i = 0; i < 60; i++) {
            itemList.add(new ItemEntry(Integer.toString(i)));
        }

        listAdapter.notifyDataSetChanged();
    }
}
