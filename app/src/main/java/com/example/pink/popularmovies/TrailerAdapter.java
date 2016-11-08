package com.example.pink.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
                    // Get trailer video URL.
                    Log.d(LOG_TAG, "Clicked movie trailer, position=" + position + " + url = " + mTrailerVideoUrls.get(position));
                    // Set implicit intent to launch browser/youtube to play movie trailer.
                    openWebPage(mTrailerVideoUrls.get(position));
                }
            });
//        }
        return convertView;
    }

    private void openWebPage(String url) {
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            getContext().startActivity(intent);
        }
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final ImageButton btnPlayTrailerVideo;
        public final TextView trailerLabel;
        public final TextView reviewAuthor;
        public final TextView reviewContent;

        public ViewHolder(View view) {
            btnPlayTrailerVideo = (ImageButton) view.findViewById(R.id.btnPlayTrailerVideo);
            trailerLabel = (TextView) view.findViewById(R.id.txt_trailer_label);
            reviewAuthor = (TextView) view.findViewById(R.id.txt_author);
            reviewContent = (TextView) view.findViewById(R.id.txt_review_content);
        }
    }

}
