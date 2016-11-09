package com.example.pink.popularmovies.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

/**
 * Created by pink on 11/9/2016.
 */

public class TestMovieDb extends AndroidTestCase {
    public static final String LOG_TAG = TestMovieDb.class.getSimpleName();

    // Start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
    }

    /*
    This function gets called before each test is executed to delete the database.  This makes
    sure that we always have a clean test.
    */
    public void setUp() {
        deleteTheDatabase();
    }

    /*
    Note that this only tests that the favorite movies table has the correct columns
    */
    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(MovieContract.MovieEntry.TABLE_NAME);

        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new MovieDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain the favorite movie entry table.
        assertTrue("Error: Your database was created without both the favorite movie entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + MovieContract.MovieEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> favoriteMovieColumnHashSet = new HashSet<String>();
        favoriteMovieColumnHashSet.add(MovieContract.MovieEntry._ID);
        favoriteMovieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_TITLE);
        favoriteMovieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_POSTER);
        favoriteMovieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_SYNOPSIS);
        favoriteMovieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_USER_RATING);
        favoriteMovieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_RELEASE_DATE);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            favoriteMovieColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required
        // favorite movie entry columns
        assertTrue("Error: The database doesn't contain all of the required favorite movie entry columns",
                favoriteMovieColumnHashSet.isEmpty());
        db.close();
    }

    /*
    Insert and query the favorite movie database.
    */
    public void testFavoriteMovieTable() {
        insertFavoriteMovie();
    }

    /*
    Helper method for the testFavoriteMovieTable.
    */
    public long insertFavoriteMovie() {
        // First step: Get reference to writable database
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create ContentValues of what you want to insert
        // (you can use the createNorthPoleLocationValues if you wish)
        ContentValues testValues = TestUtilities.createFavoriteMovieValues();

        // Insert ContentValues into database and get a row ID back
        long favoriteMovieRowId;
        favoriteMovieRowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, testValues);

        // Verify a row was returned.
        assertTrue(favoriteMovieRowId != -1);

        // Query the database and receive a Cursor back
        Cursor cursor = db.query(
                MovieContract.MovieEntry.TABLE_NAME,
                null, // all columns
                null, // columns for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Move the cursor to a valid database row
        assertTrue("Error: No Records returned from favorite movie query", cursor.moveToFirst());

        // Validate com.example.android.sunshine.app.data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("Error: Favorite Movie Query Validation Failed",
                cursor, testValues);

        // Move cursor to check there is only one record in database.
        assertFalse("Error: More than on record returned from favorite movie query",
                cursor.moveToNext());

        // Finally, close the cursor and database
        cursor.close();
        db.close();
        return favoriteMovieRowId;
    }

}
