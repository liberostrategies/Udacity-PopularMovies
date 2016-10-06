package com.example.pink.popularmovies;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

import static com.example.pink.popularmovies.util.ImageUtil.setImage;

/**
 * Created by pink on 9/28/2015.
 */
public class CustomImageListAdapter extends ArrayAdapter<String> {

    private final Activity context;
//    private final String[] itemname;
    private final ArrayList<String> mMovieData;

    /**
     *
     * @param context
     * @param movieData Comprised of "movie id" and "/poster art image name."
     */
    public CustomImageListAdapter(Activity context, ArrayList<String> movieData) {
//    public CustomImageListAdapter(Activity context, String[] itemname) {
        super(context, R.layout.list_movie_item, movieData);
//        super(context, R.layout.list_movie_item, itemname);
        this.context = context;
        this.mMovieData = movieData;
//        this.itemname = itemname;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_movie_item, null, true);

        String movieData = mMovieData.get(position);
        int idxComma = movieData.indexOf(",");

        ImageView imageView = (ImageView) rowView.findViewById(R.id.all_list_poster);
        String posterPath = movieData.substring(idxComma+2);
        setImage(context, imageView, posterPath);

        return rowView;

    }

}
