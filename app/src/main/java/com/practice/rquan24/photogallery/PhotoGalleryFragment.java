package com.practice.rquan24.photogallery;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by rquan24 on 1/14/15.
 */
public class PhotoGalleryFragment extends VisibleFragment
{
    private static final String TAG = "PhotoGalleryFragment";

    GridView mGridView;
    ArrayList<GalleryItem> mItems;
    ThumbnailDownloader<ImageView> mThumbnailThread;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        updateItems();

        /*
            Making a background thread with a handler loyal to the main thread.
            setting the listener in the background thread that talks to the
            main threa's handler that we passed in so that the background thread will
            communicate through our set interface
         */
        mThumbnailThread = new ThumbnailDownloader<ImageView>(new Handler());
        mThumbnailThread.setListener(new ThumbnailDownloader.Listener<ImageView>()
        {
            @Override
            public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail)
            {
                if(isVisible())
                {
                    imageView.setImageBitmap(thumbnail);
                }
            }
        });

        mThumbnailThread.start();
        mThumbnailThread.getLooper();

    }

    public void updateItems()
    {
        new FetchItemsTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mGridView = (GridView) v.findViewById(R.id.gridView);



        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                GalleryItem item = mItems.get(i);

                Uri photoPageUri = Uri.parse(item.getPhotoUrl());

                // Implicit intent was a cop out
                //Intent intent = new Intent(Intent.ACTION_VIEW, photoPageUri);

                Intent intent = new Intent(getActivity(), PhotoPageActivity.class);
                intent.setData(photoPageUri);
                startActivity(intent);
            }
        });
        setUpAdapter();

        return v;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mThumbnailThread.quit();
    }
    void setUpAdapter()
    {
        if(getActivity() == null || mGridView == null) return;

        if(mItems != null)
        {
            mGridView.setAdapter(new GalleryItemAdapter(mItems));
        }
        else
        {
            mGridView.setAdapter(null);
        }

    }

    /* first -> type of input parameter...execute(*) -> doInBackground(*)
       second ->type of input for progress
       third -> return type from doInBackground -> onPostExecute

       Easiest way to get a background thread, BUT it is fundamentally ill-suited for
       repetitive and long runnning work.

       When you want the other reasons, create a dedicated background thread
     */
    private class FetchItemsTask extends AsyncTask<Void, Void, ArrayList<GalleryItem>>
    {

        @Override
        protected ArrayList<GalleryItem> doInBackground(Void... params)
        {
            Activity activity = getActivity();
            if(activity == null)
            {
                return new ArrayList<GalleryItem>();
            }

            String query = PreferenceManager.getDefaultSharedPreferences(activity)
                            .getString(FlickrFetcher.PREF_SEARCH_QUERY, null);

            if(query != null)
            {
                return new FlickrFetcher().search(query);
            }
            else
            {
                return new FlickrFetcher().fetchItems();
            }
        }

        /*
         This method is ran after doInBackground, it runs on the main thread
         so typically update the UI through here.
         */
        @Override
        protected void onPostExecute(ArrayList<GalleryItem> items)
        {
            mItems = items;
            setUpAdapter();
        }
    }

    private class GalleryItemAdapter extends ArrayAdapter<GalleryItem>
    {
        public GalleryItemAdapter(ArrayList<GalleryItem> items)
        {
            super(getActivity(), 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if(convertView == null)
            {
                convertView = getActivity().getLayoutInflater()
                              .inflate(R.layout.gallery_item, parent, false);
            }

            ImageView imageView = (ImageView)convertView.findViewById(R.id.gallery_item_imageView);
           // When we need the image, we post it to the message queue of the background thread
            GalleryItem item = getItem(position);
            mThumbnailThread.queueThumbnail(imageView, item.getUrl());
            return convertView;
        }

    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        mThumbnailThread.clearQueue();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.menu_item_search:
                Log.v(TAG, "clicked search");
                /*
                    This is where the magic happens!
                    Aside from this you need to set the manifest and have a xml
                    search config.
                 */
                getActivity().onSearchRequested();
                return true;
            case R.id.menu_item_clear:
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                                 .edit()
                                 .putString(FlickrFetcher.PREF_SEARCH_QUERY, null)
                                 .commit();
                updateItems();
                return true;
            case R.id.menu_item_toggle_polling:
                boolean shouldStartService = !PollService.isServiceOn(getActivity());
                PollService.setServiceAlarm(getActivity(), shouldStartService);
                Log.i(TAG, "Clicked polling option: " + shouldStartService);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                {
                    Log.i(TAG, "Above HoneyComb");
                    getActivity().invalidateOptionsMenu();
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    /*
        options menu are not inflated every time they are used, so we need something
        to trigger it to reflect the state of the application.

        on pre 3.0 devices this method is called every time the menu is presented
        on post you have to invalidate the options menu
     */

    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);

        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
        if(PollService.isServiceOn(getActivity()))
        {
            toggleItem.setTitle(R.string.stop_polling);
        }
        else
        {
            toggleItem.setTitle(R.string.start_polling);
        }
    }
}































