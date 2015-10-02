package com.example.pink.popularmovies;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by pink on 9/28/2015.
 */
public class CustomImageListAdapter extends ArrayAdapter<String> {

    private final Activity context;
//    private final String[] itemname;
    private final ArrayList<String> mMovieData;

    public CustomImageListAdapter(Activity context, ArrayList<String> movieData) {
//    public CustomImageListAdapter(Activity context, String[] itemname) {
        super(context, R.layout.list_movie_item, movieData);
//        super(context, R.layout.list_movie_item, itemname);
        // TODO Auto-generated constructor stub

        this.context = context;
        this.mMovieData = movieData;
//        this.itemname = itemname;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_movie_item, null, true);

        String movieData = mMovieData.get(position);
        int idxComma = movieData.indexOf(",");

        TextView txtTitle = (TextView) rowView.findViewById(R.id.all_list_title);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.all_list_poster);
        String posterPath = movieData.substring(idxComma+2);
        setImage(imageView, posterPath);

        String text = movieData.substring(0,idxComma);
        txtTitle.setText(text);
//        txtTitle.setText(itemname[position]);
        return rowView;

    }

//    private String getImageName(String movieId) {
//
//    }

    private void setImage(ImageView imgMovie, String aPosterPath) {
        // TO DO: Look up poster for movie id.
        Picasso.with(
                context)
                .load("http://image.tmdb.org/t/p/w185/" + aPosterPath)
//                .load("http://image.tmdb.org/t/p/w185//nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg")
                        .resize(100, 100)
                .into(imgMovie);
    }
}
