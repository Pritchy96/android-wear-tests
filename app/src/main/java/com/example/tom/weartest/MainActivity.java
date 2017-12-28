package com.example.tom.weartest;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import org.jraf.android.androidwearcolorpicker.app.ColorPickActivity;

public class MainActivity extends WearableActivity {

    MqttInstance mqttInstance;
    private ToggleButton stateSelector;
    private Button colorSelector;

    final String serverUri = "tcp://test.mosquitto.org:1883";
    String clientId = "Wear Client"; //todo: Add UID for client.

    final int REQUEST_PICK_COLOR = 1;
    int ledColor = 0;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.led_control);
        mqttInstance = new MqttInstance(getApplicationContext(), serverUri, clientId);

        stateSelector = findViewById(R.id.stateSelector);
        colorSelector = findViewById(R.id.colourButton);

        stateSelector.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                logger.log(Boolean.toString(isChecked), getApplicationContext());
                mqttInstance.publishMessage("home/ledstrip/relay/0", isChecked ? "1" : "0");
            }
        });

        colorSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logger.log("Color", getApplicationContext());
                Intent intent = new ColorPickActivity.IntentBuilder().oldColor(ledColor).build(getApplicationContext());
                startActivityForResult(intent, REQUEST_PICK_COLOR);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_PICK_COLOR:
                if (resultCode == RESULT_CANCELED) {
                    // The user pressed 'Cancel'
                    break;
                }

                //#AARRGGBB (in Hex)
                ledColor = ColorPickActivity.getPickedColor(data);

                logger.log("pickedColor=" + Integer.toHexString(ledColor), this.getApplicationContext());
                mqttInstance.publishMessage("home/ledstrip/color", "#" + Integer.toHexString(ledColor).substring(2));
                break;
        }
    }
}
