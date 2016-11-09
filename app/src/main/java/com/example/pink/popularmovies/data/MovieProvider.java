package com.example.pink.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by pink on 11/9/2016.
 */

public class MovieProvider extends ContentProvider {
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final SQLiteQueryBuilder sFavoriteMovieQueryBuilder = new SQLiteQueryBuilder();

    static final int FAVORITE_MOVIE = 100;
    static final int FAVORITE_MOVIE_WITH_MOVIE_ID = 101;

    private MovieDbHelper mMovieDbHelper;

    /*
    This UriMatcher will match each URI to the WEATHER, WEATHER_WITH_LOCATION, WEATHER_WITH_LOCATION_AND_DATE,
    and LOCATION integer constants defined above.
    You can test this by uncommenting the testUriMatcher test within TestUriMatcher.
    */
    static UriMatcher buildUriMatcher() {
        // 1) The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case. Add the constructor below.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        // For readability:
        final String authority = MovieContract.CONTENT_AUTHORITY;

        // 2) Use the addURI function to match each of the types.  Use the constants from
        // MovieContract to help define the types to the UriMatcher.
        matcher.addURI(authority, MovieContract.PATH_FAVORITE_MOVIE, FAVORITE_MOVIE); // DIR - list all favorite movies
        matcher.addURI(authority, MovieContract.PATH_FAVORITE_MOVIE + "/*", FAVORITE_MOVIE_WITH_MOVIE_ID); // ITEM - 1 favorite movie

        // 3) Return the new matcher!
        return matcher;
    }

    //favorite_movies.movie_id = ?
    private static final String sFavoriteMovieSelection =
            MovieContract.MovieEntry.TABLE_NAME +
                    "." + MovieContract.MovieEntry.COLUMN_MOVIEDB_ID + " = ? ";

    @Override
    public boolean onCreate() {
        mMovieDbHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Given a URI, determines what kind of request it is and query the database accordingly.
        Cursor returnCursor;
        switch (sUriMatcher.match(uri)) {
            // "favoritemovie"
            case FAVORITE_MOVIE: // Returns multiple rows. Uses "DIR."
                returnCursor = mMovieDbHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            // "favoritemovie/*"
            case FAVORITE_MOVIE_WITH_MOVIE_ID: // Returns single row. Users "ITEM."
                returnCursor = getFavoriteMovieByMovieDbId(uri, projection, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return returnCursor;
    }

    private Cursor getFavoriteMovieByMovieDbId(Uri uri, String[] projection, String sortOrder) {
        long movieDbId = MovieContract.MovieEntry.getFavoriteMovieIdFromUri(uri);
        return sFavoriteMovieQueryBuilder.query(mMovieDbHelper.getReadableDatabase(),
                projection,
                sFavoriteMovieSelection,
                new String[]{Long.toString(movieDbId)},
                null,
                null,
                sortOrder
        );
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);
        switch (match) {
            // "favoritemovie"
            case FAVORITE_MOVIE: // Returns multiple rows. Uses "DIR."
                return MovieContract.MovieEntry.CONTENT_TYPE;
            // "favoritemovie/*"
            case FAVORITE_MOVIE_WITH_MOVIE_ID: // Returns single row. Users "ITEM."
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri returnUri;
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            // "favoritemovie"
            case FAVORITE_MOVIE: // Returns single row. Users "ITEM."
                long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MovieContract.MovieEntry.buildFavoriteMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                getContext().getContentResolver().notifyChange(uri, null);
                db.close();
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        db.close();
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted = 0;

        // Student: Start by getting a writable database
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();

        // Student: Use the uriMatcher to match the WEATHER and LOCATION URI's we are going to
        // handle.  If it doesn't match these, throw an UnsupportedOperationException.
        final int match = sUriMatcher.match(uri);
        // Makes delete all rows return the number of rows deleted.
        if (null == selection) selection = "1";
        switch (match) {
            // "favoritemovie/*"
            case FAVORITE_MOVIE_WITH_MOVIE_ID: // Returns single row. Users "ITEM."
                rowsDeleted = db.delete(MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Student: A null value deletes all rows.  In my implementation of this, I only notified
        // the uri listeners (using the content resolver) if the rowsDeleted != 0 or the selection
        // is null.
        // Oh, and you should notify the listeners here.
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Student: return the actual rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
            // Student: This is a lot like the delete function.  We return the number of rows impacted
            // by the update.

            int rowsUpdated = 0;

            // Student: Start by getting a writable database
            final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();

            // Student: Use the uriMatcher to match the WEATHER and LOCATION URI's we are going to
            // handle.  If it doesn't match these, throw an UnsupportedOperationException.
            final int match = sUriMatcher.match(uri);
            // Makes update all rows return the number of rows updated.
            if (null == selection) selection = "1";
            switch (match) {
                // "favoritemovie/*"
                case FAVORITE_MOVIE_WITH_MOVIE_ID: // Returns single row. Users "ITEM."
                    rowsUpdated = db.update(MovieContract.MovieEntry.TABLE_NAME, values, selection, selectionArgs);
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }

            // Student: A null value updates all rows.  In my implementation of this, I only notified
            // the uri listeners (using the content resolver) if the rowsUpdated != 0 or the selection
            // is null.
            // Oh, and you should notify the listeners here.
            if (rowsUpdated != 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }

            // Student: return the actual rows updated.
            return rowsUpdated;
    }
}
