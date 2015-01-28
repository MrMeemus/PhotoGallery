package com.practice.rquan24.photogallery;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;


import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rquan24 on 1/18/15.
 */

/*
    So the way I understand so far, is you have a Handler that can consume
    and post messages, those messages are all taken care of by a middle man
    called a looper. Each handler is attached to one looper which has a queue of
    messages. That queue can contain a bunch of messages targeted to different handlers.
    Each message can only be targeted to one handler.

    I think the message dictates the creation of a handler.
 */
public class ThumbnailDownloader<Token> extends HandlerThread
{
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;

    Handler mHandler;
    Map<Token, String> requestMap = Collections.synchronizedMap(new HashMap<Token, String>());
    Handler mResponseHandler;
    Listener<Token> mListener;

   // private LruCache<String, Bitmap> mCache;

    /*
        Making this a interface so that the main thread can implement the behavior
        it wants and this background thread would access the main thread through this interface
     */
    public interface Listener<Token>
    {
        void onThumbnailDownloaded(Token token, Bitmap thumbnail);
    }

    public void setListener(Listener<Token> listener)
    {
        mListener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler)
    {
        super(TAG);

        mResponseHandler = responseHandler;

    /*    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mCache = new LruCache<String, Bitmap>(cacheSize)
        {
            @SuppressLint("NewApi")
            @Override
            protected int sizeOf(String key, Bitmap bitmap)
            {
                    // The cache size will be measured in kilobytes rather than
                    // number of items.
                return bitmap.getByteCount() / 1024;
            }
         }; */
    }
/*
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mCache.get(key);
    }
*/
    /*
        This method is called before the looper checks the queue for
        the first time, good place to implement handleMessage(Message m)
        call back method here.
     */
    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared()
    {
        mHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                if(msg.what == MESSAGE_DOWNLOAD)
                {
                    @SuppressWarnings("unchecked")
                    Token token = (Token)msg.obj;
                    handleRequest(token);
                }
            }
        };
    }


    public void queueThumbnail(Token token, String url)
    {
        Log.i(TAG, "Got an URL: " + url );
        //Keeping track of url requests with the correct image
        requestMap.put(token, url);
        // Posting message to message queue with target handler as myself
        mHandler.obtainMessage(MESSAGE_DOWNLOAD, token).sendToTarget();
    }

    private void handleRequest(final Token token)
    {
        try
        {
            final String url = requestMap.get(token);
            if(url == null) return;

            byte[] bitmapBytes = new FlickrFetcher().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory
                                  .decodeByteArray(bitmapBytes, 0, bitmapBytes.length);

            /*
                When a message has its callback field set, instead of being
                run by its target handler, the runnable callback is run instead.

                So when the background thread handles it, it has a reference to the
                main thread so it wants to use the interface set by the main thread
                rather than have the main thread handle it.
                This will be ran on the main (UI) thread.
             */
            mResponseHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    if(requestMap.get(token) != url)
                        return;
                    requestMap.remove(token);
                    mListener.onThumbnailDownloaded(token, bitmap);
                }
            });

            /*
                If we didn't do the post new runnable, I would have had to subclass Handler
                in the main thread with a custom handleMessage, then send a custom message from
                background thread to the main thread through the handler referenced from the main
                thread.
             */
        }
        catch(IOException ioe)
        {

        }
    }

    public void clearQueue()
    {
        mHandler.removeMessages(MESSAGE_DOWNLOAD);
        requestMap.clear();
    }
}
