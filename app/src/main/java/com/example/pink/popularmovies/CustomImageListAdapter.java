package com.example.pink.popularmovies;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by pink on 9/28/2015.
 */
public class CustomImageListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] itemname;
//    private final Integer[] imgid;

    public CustomImageListAdapter(Activity context, String[] itemname) {
//        public CustomListAdapter(Activity context, String[] itemname, Integer[] imgid) {
        super(context, R.layout.list_movie_item, itemname);
        // TODO Auto-generated constructor stub

        this.context = context;
        this.itemname = itemname;
//            this.imgid = imgid;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_movie_item, null, true);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.all_list_title);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.all_list_poster);
        setImage(imageView);

        txtTitle.setText(itemname[position]);
//        imageView.setImageResource(imgid[position]);
        return rowView;

    }

    private void setImage(ImageView imgMovie) {
        // TO DO: Look up poster for movie title.
        Picasso.with(
                context)
                .load("http://image.tmdb.org/t/p/w185//nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg")
                .resize(100, 100)
                .into(imgMovie);
    }
}
