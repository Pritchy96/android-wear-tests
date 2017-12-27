package com.example.tom.weartest;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by tom on 12/27/17.
 */

public final class logger {
    final static boolean debugToWatch = false;
    public static void log(String mainText, Context context, boolean debugToWatch){
        System.out.println("LOG: " + mainText);
        if (debugToWatch) {
            Toast.makeText(context, "LOG: " + mainText, Toast.LENGTH_SHORT).show();
        }
    }

    public static void log(String mainText, Context context){
        System.out.println("LOG: " + mainText);
        if (debugToWatch) {
            Toast.makeText(context, "LOG: " + mainText, Toast.LENGTH_SHORT).show();
        }
    }
}
