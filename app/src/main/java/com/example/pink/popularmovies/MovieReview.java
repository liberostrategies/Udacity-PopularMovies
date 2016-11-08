package com.example.pink.popularmovies;

/**
 * Created by pink on 11/8/2016.
 */

public class MovieReview {
    private String mAuthor;
    private String mContent;
    public MovieReview(String author, String content) {
        mAuthor = author;
        mContent = content;
    }

    public String getmAuthor() {
        return mAuthor;
    }

    public void setmAuthor(String mAuthor) {
        this.mAuthor = mAuthor;
    }

    public String getmContent() {
        return mContent;
    }

    public void setmContent(String mContent) {
        this.mContent = mContent;
    }
}
