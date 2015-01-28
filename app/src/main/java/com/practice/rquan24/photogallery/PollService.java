package com.practice.rquan24.photogallery;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;

/**
 * Created by rquan24 on 1/24/15.
 */
public class PollService extends IntentService
{
    private static final String TAG = "PollService";

    private static final int POLL_INTERVAL = 1000 * 60 * 5; // 5 minutes
    public static final String PREF_IS_ALARM_ON = "isAlarmOn";

    public static final String ACTION_SHOW_NOTIFICATION =
                "com.practice.rquan24.photogallery.SHOW_NOTIFICATION";

    public static final String PERM_PRIVATE = "com.practice.rquan24.photogallery.PRIVATE";

    public PollService()
    {
        super(TAG);
    }

    // A services way to handling intents sent to it by the system
    // Sometimes considered a "command"
    @Override
    protected void onHandleIntent(Intent intent)
    {
        // This is how you check for background network availability
        ConnectivityManager cm = (ConnectivityManager)
                                getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressWarnings("deprecation")
        boolean isNetworkAvailable = cm.getBackgroundDataSetting() &&
                                     cm.getActiveNetworkInfo() != null;

        if(!isNetworkAvailable) return;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String query = prefs.getString(FlickrFetcher.PREF_SEARCH_QUERY, null);
        String lastResultId = prefs.getString(FlickrFetcher.PREF_LAST_RESULT_ID, null);

        ArrayList<GalleryItem> items;

        if(query != null)
        {
            items = new FlickrFetcher().search(query);
        }
        else
        {
            items = new FlickrFetcher().fetchItems();
        }

        if(items.size() == 0) return;

        String resultId = items.get(0).getId();

        // If there is new photos, send a notification to the user
        if(!resultId.equals(lastResultId))
        {
            Resources r = getResources();
            // Intent that will be fired when user clicks on the notification
            PendingIntent pi = PendingIntent.
                    getActivity(this, 0, new Intent(this, PhotoGalleryActivity.class), 0);

            Notification notification = new NotificationCompat.Builder(this)
                .setTicker(r.getString(R.string.new_pictures_text))
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(r.getString(R.string.new_pictures_title))
                .setContentText(r.getString(R.string.new_pictures_text))
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();

            showBackgroundNotification(0, notification);

        }

        prefs.edit().putString(FlickrFetcher.PREF_LAST_RESULT_ID, resultId).commit();
    }

    /*
        PendingIntent packages up an invocation of Context.startService(Intent)
        Make the method static so other classes can invoke the alarm.
     */
    public static void setServiceAlarm(Context context, boolean isOn)
    {
        Intent i = new Intent(context, PollService.class);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);

        AlarmManager alarmManager = (AlarmManager)
                    context.getSystemService(context.ALARM_SERVICE);

        if(isOn)
        {
            alarmManager.setRepeating(AlarmManager.RTC,
                                      System.currentTimeMillis(), POLL_INTERVAL, pi);
        }
        else
        {
            alarmManager.cancel(pi);
            pi.cancel();
        }

        // Receiver will use this to know whether the alarm should be on or off
        PreferenceManager.getDefaultSharedPreferences(context)
                            .edit().putBoolean(PREF_IS_ALARM_ON, isOn).commit();

    }

    /*
        Each pending intent requested with a specific intent will always be one to one
        to that same intent. passing in the flag: if the pending intent does not already
        exist return null.
     */
    public static boolean isServiceOn(Context context)
    {
        Intent i = new Intent(context, PollService.class);
        PendingIntent pi = PendingIntent.getService(
                             context, 0, i, PendingIntent.FLAG_NO_CREATE);

        return pi != null;
    }

    void showBackgroundNotification(int requestCode, Notification notification)
    {
        Intent i = new Intent(ACTION_SHOW_NOTIFICATION);
        i.putExtra("REQUEST_CODE", requestCode);
        i.putExtra("NOTIFICATION", notification);

        sendOrderedBroadcast(i, PERM_PRIVATE, null, null, Activity.RESULT_OK, null, null);
    }
}













