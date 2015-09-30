package com.example.pink.popularmovies;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by pink on 9/8/2015.
 */
public class ImageItem {
    private final String LOG_TAG = ImageItem.class.getSimpleName();
    private String mImageUrl;
    private String mTitle;

    public ImageItem(String imageUrl, String title) {
        super();
        this.mImageUrl = imageUrl;
        this.mTitle = title;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.mImageUrl = imageUrl;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public void getImageWithPicasso(Context context, ImageView imgView) {
        Log.i(LOG_TAG, "Getting movie from Picasso");
        Picasso.with(
                context)
                .load(mImageUrl)//"http://image.tmdb.org/t/p/w185//nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg")
                .resize(100, 100)
                .into(imgView);
        Log.i(LOG_TAG, "Got movie from Picasso");

    }
}