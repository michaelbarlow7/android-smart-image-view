package com.loopj.android.image;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

public class WebImage implements SmartImage {
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 10000;

    private static WebImageCache webImageCache;

    private String url;

    private int sampleSize;

    public WebImage(String url) {
        this.url = url;
        this.sampleSize = 1;
    }

    public WebImage(String url, int sampleSize) {
        this.url = url;
        this.sampleSize = sampleSize;
    }

    public Bitmap getBitmap(Context context) {
        // Don't leak context
        if(webImageCache == null) {
            webImageCache = new WebImageCache(context);
        }

        // Try getting bitmap from cache first
        Bitmap bitmap = null;
        if(url != null) {
            bitmap = webImageCache.get(url);
            if(bitmap == null) {
                bitmap = getBitmapFromUrl(url);
                if(bitmap != null){
                    webImageCache.put(url, bitmap);
                }
            }
        }

        return bitmap;
    }

    private Bitmap getBitmapFromUrl(String url) {
        Bitmap bitmap = null;

        try {
            URLConnection conn = new URL(url).openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            //bitmap = BitmapFactory.decodeStream((InputStream) conn.getContent());
            BitmapFactory.Options options = new BitmapFactory.Options();
            //options.inSampleSize = 8
            options.inPurgeable = true;
            if (this.sampleSize > 1)
                options.inSampleSize = this.sampleSize;
            Rect rect = new Rect(-1,-1,-1,-1);
            bitmap = BitmapFactory.decodeStream((InputStream) conn.getContent(), rect, options);
        }
        catch(OutOfMemoryError e) {
            e.printStackTrace();
            bitmap = null;
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public static void removeFromCache(String url) {
        if(webImageCache != null) {
            webImageCache.remove(url);
        }
    }
}
