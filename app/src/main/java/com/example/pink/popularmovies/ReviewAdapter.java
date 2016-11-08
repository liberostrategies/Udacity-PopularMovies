package com.example.pink.popularmovies;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;


/**
 * Created by pink on 11/8/2016.
 */

public class ReviewAdapter extends ArrayAdapter {
    private final String LOG_TAG = ReviewAdapter.class.getSimpleName();
    ArrayList<MovieReview> mReviews;

    public ReviewAdapter(Context context, ArrayList<MovieReview> reviews) {
        super(context, 0, reviews);
        mReviews = reviews;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_review_item, parent, false);
        TrailerAdapter.ViewHolder viewHolder = new TrailerAdapter.ViewHolder(convertView);
//        viewHolder.reviewContent.setHeight(viewHolder.reviewContent.getLineHeight() * viewHolder.reviewContent.getLineCount());
        final String author = mReviews.get(position).getmAuthor();
        final String review = mReviews.get(position).getmContent();
        viewHolder.reviewAuthor.setText("-" + author);
        viewHolder.reviewContent.append("\"" + review + "\"");
        viewHolder.reviewContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "Clicked " + position + " with review from " + author);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder
                    // TO DO: Internationalize the hard-coded strings.
                    .setTitle(author + "'s Review")
                    .setMessage(review)
                    .setPositiveButton("Back to Details", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                });

                AlertDialog alertD = builder.create();
//                alertD.getListView().setPadding(0,0,0,0);
                alertD.show();
            }
        });
        return convertView;
    }
}
