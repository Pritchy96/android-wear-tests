package com.example.tom.weartest;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import org.jraf.android.androidwearcolorpicker.app.ColorPickActivity;

public class MainActivity extends WearableActivity {

    MqttInstance mqttInstance;
    private Switch stateSelector;
    private Button colorSelector;

    final String serverUri = "tcp://test.mosquitto.org:1883";
    String clientId = "Wear Client"; //todo: Add UID for client.

    final int REQUEST_PICK_COLOR = 1;
    int ARGBcolor = 0;

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
                Intent intent = new ColorPickActivity.IntentBuilder().oldColor(ARGBcolor).build(getApplicationContext());
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

                //0xAARRGGBB
                ARGBcolor = ColorPickActivity.getPickedColor(data);

                int ARGB_a = (ARGBcolor >> 24) & 0xff; // or color >>> 24
                int ARGB_r = (ARGBcolor >> 16) & 0xff;
                int ARGB_g = (ARGBcolor >> 8) & 0xff;
                int ARGB_b = (ARGBcolor) & 0xff;

                //LED strip only takes RGB, no alpha channel.
                int RGB_r, RGB_g, RGB_b;
                if (ARGB_a == 255) {
                    RGB_r = ARGB_r;
                    RGB_g = ARGB_g;
                    RGB_b = ARGB_b;
                } else {
                    double alpha = ARGB_a / 0xff;
                    double diff = (1.0 - alpha) * 0xff;
                    RGB_r = (int)((ARGB_r * alpha) + diff);
                    RGB_g = (int)((ARGB_g * alpha) + diff);
                    RGB_b = (int)((ARGB_b * alpha) + diff);
                }

                String publishMessage = "#" + String.format("%02X", RGB_r)
                        + String.format("%02x", RGB_g) + String.format("%02x", RGB_b);

                ColorStateList selectedColorStateList = ColorStateList.valueOf(ARGBcolor);
                colorSelector.setBackgroundTintList(selectedColorStateList);
                colorSelector.setTextColor(Color.rgb(255-RGB_r, 255-RGB_b, 255-RGB_b));
                stateSelector.setThumbTintList(selectedColorStateList);

                mqttInstance.publishMessage("home/ledstrip/color", publishMessage);
                break;
        }
    }
}
