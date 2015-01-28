package com.practice.rquan24.photogallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by rquan24 on 1/25/15.
 */
public class StartupReceiver extends BroadcastReceiver
{
    private static String TAG = "BroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isOn = prefs.getBoolean(PollService.PREF_IS_ALARM_ON, false);
        PollService.setServiceAlarm(context, isOn);
    }
}
