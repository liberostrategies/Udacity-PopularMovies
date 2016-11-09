package com.example.pink.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Manages a local database for movie com.example.pink.popularmovies.data.
 */

public class MovieDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "movie.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_FAVORITE_MOVIE_TABLE = "CREATE TABLE " + MovieContract.MovieEntry.TABLE_NAME + " (" +
                MovieContract.MovieEntry._ID + " INTEGER PRIMARY KEY," +
                MovieContract.MovieEntry.COLUMN_MOVIEDB_ID + " REAL NOT NULL, " +
                MovieContract.MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MovieContract.MovieEntry.COLUMN_POSTER + " TEXT NOT NULL," +
                MovieContract.MovieEntry.COLUMN_SYNOPSIS + " TEXT NOT NULL," +
                MovieContract.MovieEntry.COLUMN_USER_RATING + " REAL NOT NULL," +
                MovieContract.MovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL" +
                " );";

        db.execSQL(SQL_CREATE_FAVORITE_MOVIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online com.example.pink.popularmovies.data, so its upgrade policy is
        // to simply to discard the com.example.pink.popularmovies.data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping com.example.pink.popularmovies.data, commenting out the next line
        // should be your top priority before modifying this method.
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.MovieEntry.TABLE_NAME);
        // NOTE: Is data is user generated, preserve it with ALTER TABLE instead to add new columns.

        onCreate(db);
    }
}
