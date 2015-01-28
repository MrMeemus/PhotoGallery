package com.practice.rquan24.photogallery;

import android.support.v4.app.Fragment;

/**
 * Created by rquan24 on 1/28/15.
 */
public class PhotoPageActivity extends SingleFragmentActivity
{
    @Override
    protected Fragment createFragment()
    {
        return new PhotoPageFragment();
    }
}
