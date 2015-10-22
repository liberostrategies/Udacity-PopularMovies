package com.example.pink.popularmovies;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailMovieActivityFragment extends Fragment {

    private static final String LOG_TAG = DetailMovieActivityFragment.class.getSimpleName();
    private String mMovieId;
    private String[] mMovieDetails;
    protected final static int IDX_TITLE = 0;
    protected final static int IDX_POSTER_PATH = 1;
    protected final static int IDX_RELEASE_DATE = 2;
    protected final static int IDX_VOTE_AVERAGE = 3;
    protected final static int IDX_PLOT_SYNOPSIS = 4;
    TextView mTitle;
    ImageView mImageViewPoster;
    TextView mReleaseDate;
    TextView mVoteAverage;
    TextView mPlotSynopsis;

    private Activity context;

    public DetailMovieActivityFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void fetchMovieDetails() {
        FetchMovieDetails moviesTask = new FetchMovieDetails();
        moviesTask.execute(mMovieId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.context = this.getActivity();
        // Read the intent from the all movies grid view item selection.
        Intent intent = getActivity().getIntent();
        View rootView = inflater.inflate(R.layout.fragment_detail_movie, container, false);

        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT) ) {
            // TO DO: Query for movie details in a thread in case network request blocks.
            mMovieId = intent.getStringExtra(Intent.EXTRA_TEXT);
            fetchMovieDetails();
//            fetchMyMovieDetails(mMovieId);
//            ((TextView) rootView.findViewById(R.id.detail_text)).setText(mMovieId);
            mTitle = (TextView) rootView.findViewById(R.id.title);
            mImageViewPoster = (ImageView) rootView.findViewById(R.id.poster_path);
            mReleaseDate = (TextView) rootView.findViewById(R.id.release_date);
            mVoteAverage = (TextView) rootView.findViewById(R.id.vote_average);
            mPlotSynopsis = (TextView) rootView.findViewById(R.id.plot_synopsis);

//            ((TextView) rootView.findViewById(R.id.title)).setText(mMovieDetails[IDX_TITLE]);
//            ImageView imageViewPoster = (ImageView) rootView.findViewById(R.id.poster_path);
//            String posterPath = mMovieDetails[IDX_POSTER_PATH];
//            setImage(imageViewPoster, posterPath);
//            ((TextView) rootView.findViewById(R.id.release_date)).setText(mMovieDetails[IDX_RELEASE_DATE]);
//            ((TextView) rootView.findViewById(R.id.vote_average)).setText(mMovieDetails[IDX_VOTE_AVERAGE]);
//            ((TextView) rootView.findViewById(R.id.plot_synopsis)).setText(mMovieDetails[IDX_PLOT_SYNOPSIS]);
        }

        return rootView;
    }

    private void fetchMyMovieDetails(String movieId) {
        // http://api.themoviedb.org/3/movie/135397?api_key=ec842fdd2a58bc4d60d0e08a6576cb52

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String movieDetailsJsonString = null;

        // TO DO: Make this api key constant for app.
        String apiKey = "ec842fdd2a58bc4d60d0e08a6576cb52";
        try {
            // Construct the URL for the api.themoviedb.org query
            // Possible parameters are available at API page, at
            // http://docs.themoviedb.apiary.io/#reference/configuration/configuration/get?console=1
            final String MOVIEDETAILS_BASE_URL =
                    "http://api.themoviedb.org/3/movie/" + mMovieId + "?";
            final String API_KEY_PARAM = "api_key";
            Uri builtUri = Uri.parse(MOVIEDETAILS_BASE_URL).buildUpon()
                    .appendQueryParameter(API_KEY_PARAM, apiKey)
                    .build();
            URL url = new URL(builtUri.toString());
            Log.v(LOG_TAG, "Built URI " + builtUri.toString());
            // Create the request to themoviedb, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                Log.v(LOG_TAG, "Input stream was null");
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                Log.v(LOG_TAG, "Stream was empty. No point in parsing.");
                return;
            }
            movieDetailsJsonString = buffer.toString();
            //Log.v(LOG_TAG, "Forecast JSON String: " + forecastJsonStr);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the popular movies data, there's no point in attemping
            // to parse it.
            return;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        // Retrieve popular movies data.
        try {
            String[] result = getPopularMovieDetailsFromJson(movieDetailsJsonString);
            mMovieDetails = result;
        } catch (JSONException je) {
            Log.e(LOG_TAG, "Error", je);
        }
    }

    /**
     * Take the String representing the complete movies query result in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private String[] getPopularMovieDetailsFromJson(String popularMoviesJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String MOVIEDB_ID = "id";
        final String MOVIEDB_TITLE = "original_title";
        final String MOVIEDB_POSTER_PATH = "poster_path";
        final String MOVIEDB_RELEASE_DATE = "release_date";
        final String MOVIEDB_VOTE_AVERAGE = "vote_average";
        final String MOVIEDB_PLOT_SYNOPSIS = "overview";

        JSONObject movieDetailsJson = new JSONObject(popularMoviesJsonStr);
        String title = movieDetailsJson.getString(MOVIEDB_TITLE);
        String posterPath = movieDetailsJson.getString(MOVIEDB_POSTER_PATH);
        String releaseDate = movieDetailsJson.getString(MOVIEDB_RELEASE_DATE);
        String voteAverage = movieDetailsJson.getString(MOVIEDB_VOTE_AVERAGE);
        String plotSynopsis = movieDetailsJson.getString(MOVIEDB_PLOT_SYNOPSIS);

        List<String> resultStrs = new ArrayList<String>();
        int countData = 5;
        String[] result = new String[countData];
        result[IDX_TITLE] = title;
        result[IDX_POSTER_PATH] = posterPath;
        result[IDX_RELEASE_DATE] = releaseDate;
        result[IDX_VOTE_AVERAGE] = voteAverage;
        result[IDX_PLOT_SYNOPSIS] = plotSynopsis;

        for (String s : result) {
            Log.v(LOG_TAG, "Movie data: " + s);
        }
        return result;
    }

    /**
     * Look up poster for movie id.
     * @param imgMovie
     * @param aPosterPath
     */
    private void setImage(ImageView imgMovie, String aPosterPath) {
        Picasso.with(
                context)
                .load("http://image.tmdb.org/t/p/w185/" + aPosterPath)
//                .load("http://image.tmdb.org/t/p/w185//nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg")
                .into(imgMovie);
    }

    public class FetchMovieDetails extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = FetchMovieDetails.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... params) {
            // http://api.themoviedb.org/3/movie/135397?api_key=ec842fdd2a58bc4d60d0e08a6576cb52

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieDetailsJsonString = null;

            // TO DO: Make this api key constant for app.
            String apiKey = "ec842fdd2a58bc4d60d0e08a6576cb52";
            try {
                // Construct the URL for the api.themoviedb.org query
                // Possible parameters are avaiable at API page, at
                // http://docs.themoviedb.apiary.io/#reference/configuration/configuration/get?console=1
                final String MOVIEDETAILS_BASE_URL =
                        "http://api.themoviedb.org/3/movie/" + mMovieId + "?";
                final String API_KEY_PARAM = "api_key";
                Uri builtUri = Uri.parse(MOVIEDETAILS_BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, apiKey)
                        .build();
                URL url = new URL(builtUri.toString());
                Log.v(LOG_TAG, "Built URI " + builtUri.toString());
                // Create the request to themoviedb, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                movieDetailsJsonString = buffer.toString();
                //Log.v(LOG_TAG, "Forecast JSON String: " + forecastJsonStr);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the popular movies data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            // Retrieve popular movies data.
            try {
                String[] result = getPopularMovieDetailsFromJson(movieDetailsJsonString);
                return result;
            } catch (JSONException je) {
                Log.e(LOG_TAG, "Error", je);
            }
            return null;
        }


        /**
         * Take the String representing the complete movies query result in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
/*
        private String[] getPopularMovieDetailsFromJson(String popularMoviesJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String MOVIEDB_ID = "id";
            final String MOVIEDB_TITLE = "original_title";
            final String MOVIEDB_POSTER_PATH = "poster_path";
            final String MOVIEDB_RELEASE_DATE = "release_date";
            final String MOVIEDB_VOTE_AVERAGE = "vote_average";
            final String MOVIEDB_PLOT_SYNOPSIS = "overview";

            JSONObject movieDetailsJson = new JSONObject(popularMoviesJsonStr);
            String title = movieDetailsJson.getString(MOVIEDB_TITLE);
            String posterPath = movieDetailsJson.getString(MOVIEDB_POSTER_PATH);
            String releaseDate = movieDetailsJson.getString(MOVIEDB_RELEASE_DATE);
            String voteAverage = movieDetailsJson.getString(MOVIEDB_VOTE_AVERAGE);
            String plotSynopsis = movieDetailsJson.getString(MOVIEDB_PLOT_SYNOPSIS);

            List<String> resultStrs = new ArrayList<String>();
            int countData = 5;
            String[] result = new String[countData];
            result[IDX_TITLE] = title;
            result[IDX_POSTER_PATH] = posterPath;
            result[IDX_RELEASE_DATE] = releaseDate;
            result[IDX_VOTE_AVERAGE] = voteAverage;
            result[IDX_PLOT_SYNOPSIS] = plotSynopsis;

            for (String s : result) {
                Log.v(LOG_TAG, "Movie data: " + s);
            }
            return result;
        }
*/
  //      @Override
        /**
         * Update the detail view with the background return strings of live data.
         */

        protected void onPostExecute(String[] result) {
            Log.d(LOG_TAG, "post execute result=" + result);
            if (result != null) {
                mMovieDetails = result;
                mTitle.setText(mMovieDetails[IDX_TITLE]);
                String posterPath = mMovieDetails[IDX_POSTER_PATH];
                setImage(mImageViewPoster, posterPath);
                mReleaseDate.setText("Release Date: " + mMovieDetails[IDX_RELEASE_DATE]);
                mVoteAverage.setText("Vote Average: " + mMovieDetails[IDX_VOTE_AVERAGE]);
                mPlotSynopsis.setText(mMovieDetails[IDX_PLOT_SYNOPSIS]);
           }
        }

    }
}
