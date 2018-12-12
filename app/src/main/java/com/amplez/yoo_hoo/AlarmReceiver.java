package com.amplez.yoo_hoo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;

public class AlarmReceiver extends BroadcastReceiver {
    public static String FROM_ALARM = "fromAlarm";
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, WebSocketService.class);
        i.putExtra(FROM_ALARM,true);
        ContextCompat.startForegroundService(context,i);
    }
}
