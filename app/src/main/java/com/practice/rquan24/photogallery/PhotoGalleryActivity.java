package com.practice.rquan24.photogallery;

import android.app.SearchManager;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;

public class PhotoGalleryActivity extends SingleFragmentActivity
{
    private static final String TAG = "PhotoGalleryActivity";

    @Override
    protected Fragment createFragment()
    {
        return new PhotoGalleryFragment();
    }

    // When you receive a new intent such as the search intent you want to update

    @Override
    public void onNewIntent(Intent intent)
    {
        PhotoGalleryFragment fragment = (PhotoGalleryFragment) getSupportFragmentManager()
                                                               .findFragmentById(R.id.fragmentContainer);

        if(Intent.ACTION_SEARCH.equals(intent.getAction()))
        {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.i(TAG, "Received a new search query: " + query );

            PreferenceManager.getDefaultSharedPreferences(this)
                             .edit()
                             .putString(FlickrFetcher.PREF_SEARCH_QUERY, query)
                             .commit();
        }

        fragment.updateItems();
    }
}