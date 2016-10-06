package com.example.pink.popularmovies.util;

import android.content.Context;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by pink on 10/6/2016.
 */

public class ImageUtil {
    /**
     * Look up poster for movie id.
     * @param imgMovie
     * @param aPosterPath
     */
    public static void setImage(Context context, ImageView imgMovie, String aPosterPath) {
        Picasso.with(
                context)
                .load("http://image.tmdb.org/t/p/w185/" + aPosterPath)
//                .load("http://image.tmdb.org/t/p/w185//nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg")
                .resize(600,600)
                .centerInside()
                .into(imgMovie);
    }
}
