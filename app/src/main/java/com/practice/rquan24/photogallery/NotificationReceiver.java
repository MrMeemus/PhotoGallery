package com.practice.rquan24.photogallery;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by rquan24 on 1/25/15.
 * Because the dynamic received within PhotoGalleryFragment
 * is short lived, we need the received to be standalone such as this
 */
public class NotificationReceiver extends BroadcastReceiver
{
    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(getResultCode() != Activity.RESULT_OK)
        {
            // a foreground activity cancelled the broad cast
            return;
        }

        int requestCode = intent.getIntExtra("REQUEST_CODE", 0);
        Notification notification = (Notification) intent.getParcelableExtra("Notification");

        NotificationManager notificationManager = (NotificationManager) context
                                            .getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(requestCode, notification);
    }
}
