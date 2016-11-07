package com.example.pink.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Created by pink on 11/7/2016.
 */

public class TrailerAdapter extends CursorAdapter {
    public TrailerAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    /**
     * Fill in the views with the contents of the cursor.
     */
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder)view.getTag();

//        viewHolder.btnPlayTrailerVideo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // TO DO: Get url.
//                // TO DO: Set explicit intent? to launch browser/youtube to play video.
//            }
//        });

        viewHolder.trailerLabel.append((cursor.getPosition()+1) + "");
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final ImageButton btnPlayTrailerVideo;
        public final TextView trailerLabel;

        public ViewHolder(View view) {
            btnPlayTrailerVideo = (ImageButton) view.findViewById(R.id.btnPlayTrailerVideo);
            trailerLabel = (TextView) view.findViewById(R.id.txt_trailer_label);
        }
    }

    @Override
    /**
     * Remember that these views are reused as needed.
     */
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_trailervideo_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }
}
