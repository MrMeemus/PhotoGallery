package com.practice.rquan24.photogallery;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.widget.Toast;

/**
 * Created by rquan24 on 1/25/15.
 */

/*
    Creating a fragment class that registers a listener to
    notify the user via toast if the fragment is visible in addition
    to the notification sent from Pollservice.

    Because If the user is running the app and gets new pictures
    it would be cumbersome to check the notifcation drawer in the middle
    of the experience, so we make a toast to tell them "hey btw we got new
    pics"
 */
public abstract class VisibleFragment extends Fragment
{
    public static final String TAG = "VisibleFragment";

    private BroadcastReceiver mOnShowNotification = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            // If we receive this, we're visible, so cancel notification
            setResultCode(Activity.RESULT_CANCELED);

        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        IntentFilter filter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        getActivity().registerReceiver(mOnShowNotification, filter, PollService.PERM_PRIVATE, null);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        getActivity().unregisterReceiver(mOnShowNotification);
    }
}
