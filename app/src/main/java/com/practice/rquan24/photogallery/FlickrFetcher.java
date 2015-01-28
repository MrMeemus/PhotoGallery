package com.practice.rquan24.photogallery;

import android.net.Uri;
import android.util.Log;
import android.widget.Gallery;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by rquan24 on 1/14/15.
 */

/*
    Responsible for making flickr api call along with parsing the
    xml into usable data for the model of Gallery Item.
 */
public class FlickrFetcher
{
    public static final String TAG = "FlickrFetcher";

    public static final String PREF_SEARCH_QUERY = "searchQuery";
    public static final String PREF_LAST_RESULT_ID = "lastResultId";
    private static final String ENDPOINT = "https://api.flickr.com/services/rest/";
    private static final String API_KEY = "a0a5c1c0b9e440c25a23d510e9a80cba";
    private static final String METHOD_GET_RECENT = "flickr.photos.getRecent";
    private static final String METHOD_SEARCH = "flickr.photo.search";
    private static final String PARAM_EXTRAS = "extras";
    private static final String PARAM_TEXT = "text";

    private static final String XML_PHOTO = "photo";

    private static final String EXTRA_SMALL_URL = "url_s";

    public String getUrl(String urlSpec) throws IOException
    {
        return new String(getUrlBytes(urlSpec));
    }

    byte[] getUrlBytes(String urlSpec) throws IOException
    {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK)
            {
                return null;
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while((bytesRead = in.read(buffer)) > 0)
            {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        }
        finally
        {
            connection.disconnect();
        }
    }

    public ArrayList<GalleryItem> downloadGalleyItems(String url)
    {
        ArrayList<GalleryItem> items = new ArrayList<GalleryItem>();

        try
        {
            String xmlString = getUrl(url);
            Log.i(TAG, "Receieved xml: " + xmlString);
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xmlString));

            parseItems(items, parser);
        }
        catch(IOException e)
        {
            Log.i(TAG, "Error in fetching items", e);
        }
        catch(XmlPullParserException xppe)
        {
            Log.i(TAG, "Failed to parse items", xppe);
        }

        return items;
    }

    public ArrayList<GalleryItem> fetchItems()
    {
        String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", METHOD_GET_RECENT)
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
                .build().toString();

        return downloadGalleyItems(url);
    }

    public ArrayList<GalleryItem> search(String query)
    {
        String url = Uri.parse(ENDPOINT).buildUpon()
                        .appendQueryParameter("method", METHOD_SEARCH)
                        .appendQueryParameter("api_key", API_KEY)
                        .appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
                        .appendQueryParameter(PARAM_TEXT, query)
                        .build().toString();

        return downloadGalleyItems(url);
    }

    void parseItems(ArrayList<GalleryItem> items, XmlPullParser parser) throws XmlPullParserException, IOException
    {
        int eventType = parser.next();

        while(eventType != XmlPullParser.END_DOCUMENT)
        {
            if(eventType == XmlPullParser.START_TAG && XML_PHOTO.equals(parser.getName()))
            {
                String id = parser.getAttributeValue(null, "id");
                String caption = parser.getAttributeValue(null, "title");
                String smallUrl = parser.getAttributeValue(null, EXTRA_SMALL_URL);
                String owner = parser.getAttributeValue(null, "owner");

                GalleryItem item = new GalleryItem();
                item.setId(id);
                item.setCaption(caption);
                item.setUrl(smallUrl);
                item.setOwner(owner);

                items.add(item);
            }

            eventType = parser.next();
        }
    }
}















