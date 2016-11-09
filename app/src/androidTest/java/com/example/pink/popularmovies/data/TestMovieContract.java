package com.example.pink.popularmovies.data;

import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by pink on 11/9/2016.
 */

public class TestMovieContract extends AndroidTestCase {
    private static final long TEST_FAVORITE_MOVIE = 207932;

    public void testBuildFavoriteMovie(){
        Uri favoriteMovieUri = MovieContract.MovieEntry.buildFavoriteMovieUri(TEST_FAVORITE_MOVIE);
        assertNotNull("Error: Null Uri returned.  You must fill-in buildFavoriteMovie in " +
                        "MovieContract.",
                favoriteMovieUri);
        assertEquals("Error: Favorite movie not properly appended to the end of the Uri",
                Long.toString(TEST_FAVORITE_MOVIE), favoriteMovieUri.getLastPathSegment());
        assertEquals("Error: Favorite movie Uri doesn't match our expected result",
                favoriteMovieUri.toString(),
                "content://com.example.pink.popularmovies/favoritemovie/207932");

    }
}
