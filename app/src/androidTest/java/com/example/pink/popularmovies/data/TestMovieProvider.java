package com.example.pink.popularmovies.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

/**
 * Created by pink on 11/9/2016.
 */

public class TestMovieProvider extends AndroidTestCase {
    public static final String LOG_TAG = TestMovieProvider.class.getSimpleName();

    /*
   This helper function deletes all records from both database tables using the ContentProvider.
   It also queries the ContentProvider to make sure that the database has been successfully
   deleted, so it cannot be used until the Query and Delete functions have been written
   in the ContentProvider.

   Students: Replace the calls to deleteAllRecordsFromDB with this one after you have written
   the delete functionality in the ContentProvider.
 */
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Favorite Movie table during delete", 0, cursor.getCount());
        cursor.close();

    }

    /*
       This helper function deletes all records from database tables using the database
       functions only.  This is designed to be used to reset the state of the database until the
       delete functionality is available in the ContentProvider.
     */
    public void deleteAllRecordsFromDB() {
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(MovieContract.MovieEntry.TABLE_NAME, null, null);
        db.close();
    }

    public void deleteAllRecords() {
        deleteAllRecordsFromDB();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    /*
            This test doesn't touch the database.  It verifies that the ContentProvider returns
            the correct type for each type of URI that it can handle.
            Students: Uncomment this test to verify that your implementation of GetType is
            functioning correctly.
         */
    public void testGetType() {
        // content://com.example.pink.popularmovies/favoritemovie/
        String type = mContext.getContentResolver().getType(MovieContract.MovieEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.pink.popularmovies/favoritemovie
        assertEquals("Error: the MovieEntry CONTENT_URI should return MovieEntry.CONTENT_TYPE",
                MovieContract.MovieEntry.CONTENT_TYPE, type);

        long testFavoriteMovie = 207932;
        // content://com.example.pink.popularmovies/favoritemovie/207932
        type = mContext.getContentResolver().getType(
                MovieContract.MovieEntry.buildFavoriteMovieUri(testFavoriteMovie));
        // vnd.android.cursor.dir/com.example.pink.popularmovies/favoritemovie/favoritemovie/207932
        assertEquals("Error: the MovieEntry CONTENT_URI with location should return MovieEntry.CONTENT_TYPE",
                MovieContract.MovieEntry.CONTENT_ITEM_TYPE, type);
    }

    /*
    This test uses the database directly to insert and then uses the ContentProvider to
    read out the data.  Uncomment this test to see if the basic weather query functionality
    given in the ContentProvider is working correctly.
 */
    public void testBasicWeatherQuery() {
        // insert our test records into the database
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createFavoriteMovieValues();
        long favoriteMovieRowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, testValues);

        assertTrue("Unable to Insert WeatherEntry into the Database", favoriteMovieRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor favoriteMovieCursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicWeatherQuery", favoriteMovieCursor, testValues);
    }


    // Make sure we can still delete after adding/updating stuff
    //
    // Student: Uncomment this test after you have completed writing the insert functionality
    // in your provider.  It relies on insertions with testInsertReadProvider, so insert and
    // query functionality must also be complete before this test can be used.
    public void testInsertReadProvider() {
        ContentValues testValues = TestUtilities.createFavoriteMovieValues();

        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieContract.MovieEntry.CONTENT_URI, true, tco);
        Uri favoriteMovieUri = mContext.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, testValues);

        // Did our content observer get called?  Students:  If this fails, your insert location
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long favoriteMovieRowId = ContentUris.parseId(favoriteMovieUri);

        // Verify we got a row back.
        assertTrue(favoriteMovieRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating MovieEntry.",
                cursor, testValues);
    }

    // Make sure we can still delete after adding/updating stuff
    //
    // Student: Uncomment this test after you have completed writing the delete functionality
    // in your provider.  It relies on insertions with testInsertReadProvider, so insert and
    // query functionality must also be complete before this test can be used.
    public void testDeleteRecords() {
        testInsertReadProvider();

        // Register a content observer for our location delete.
        TestUtilities.TestContentObserver favoriteMovieObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieContract.MovieEntry.CONTENT_URI, true, favoriteMovieObserver);

        deleteAllRecordsFromProvider();

        // Students: If either of these fail, you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in the ContentProvider
        // delete.  (only if the insertReadProvider is succeeding)
        favoriteMovieObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(favoriteMovieObserver);
    }


    /*
    This test uses the provider to insert and then update the data. Uncomment this test to
    see if your update location is functioning correctly.
 */
    public void testUpdateLocation() {
        // Create a new map of values, where column names are the keys
        ContentValues values = TestUtilities.createFavoriteMovieValues();

        Uri locationUri = mContext.getContentResolver().
                insert(MovieContract.MovieEntry.CONTENT_URI, values);
        long favoriteMovieRowId = ContentUris.parseId(locationUri);

        // Verify we got a row back.
        assertTrue(favoriteMovieRowId != -1);
        Log.d(LOG_TAG, "New row id: " + favoriteMovieRowId);

        ContentValues updatedValues = new ContentValues(values);
//        updatedValues.put(MovieContract.MovieEntry._ID, favoriteMovieRowId);
        updatedValues.put(MovieContract.MovieEntry.COLUMN_USER_RATING, 9999.99);

        // Create a cursor with observer to make sure that the content provider is notifying
        // the observers as expected
        Cursor locationCursor = mContext.getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI, null, null, null, null);

//        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
//        locationCursor.registerContentObserver(tco);

        int count = mContext.getContentResolver().update(
                MovieContract.MovieEntry.CONTENT_URI, updatedValues, MovieContract.MovieEntry.COLUMN_MOVIEDB_ID + "= ?",
                new String[] { Long.toString(favoriteMovieRowId)});
        assertEquals(count, 1);

        // Test to make sure our observer is called.  If not, we throw an assertion.
        //
        // Students: If your code is failing here, it means that your content provider
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
//        tco.waitForNotificationOrFail();
//
//        locationCursor.unregisterContentObserver(tco);
        locationCursor.close();

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                null,   // projection
                MovieContract.MovieEntry.COLUMN_MOVIEDB_ID + " = " + favoriteMovieRowId,
                null,   // Values for the "where" clause
                null    // sort order
        );

        TestUtilities.validateCursor("testUpdateLocation.  Error validating location entry update.",
                cursor, updatedValues);

        cursor.close();
    }

}
