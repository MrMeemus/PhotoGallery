package com.practice.rquan24.photogallery;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

/**
 * Created by rquan24 on 1/14/15.
 */
public abstract class SingleFragmentActivity extends FragmentActivity
   {
       protected abstract Fragment createFragment();

       @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_fragment);
            FragmentManager fm = getSupportFragmentManager();
            Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);

        /*
            Same as before with supper fragment manager for backwards capability support
            but this time we made the creation of the fragment abstract
            so subclasses will override it since this code is basic and generic we wouldn't
            want to rewrite it every time we make a new fragment with hosting activity.
         */
            if(fragment == null)
            {
                fragment = createFragment();
                fm.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
            }
        }
    }


