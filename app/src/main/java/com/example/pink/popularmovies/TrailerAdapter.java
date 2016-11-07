package com.example.pink.popularmovies;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by pink on 11/7/2016.
 */

public class TrailerAdapter extends ArrayAdapter {
    private final String LOG_TAG = TrailerAdapter.class.getSimpleName();
    ArrayList<String> mTrailerVideoUrls;

    public TrailerAdapter(Context context, ArrayList<String> trailerVideosUrls) {
        super(context, 0, trailerVideosUrls);
        mTrailerVideoUrls = trailerVideosUrls;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        String trailerVideoUrl = (String)getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
//        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_trailervideo_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(convertView);
            viewHolder.trailerLabel.append(" " + (position+1));
            viewHolder.btnPlayTrailerVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TO DO: Get url.
                    Log.d(LOG_TAG, "Clicked movie trailer, position=" + position + " + url = " + mTrailerVideoUrls.get(position));
                    // TO DO: Set explicit intent? to launch browser/youtube to play video.
                }
            });
//        }
        return convertView;
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

}
