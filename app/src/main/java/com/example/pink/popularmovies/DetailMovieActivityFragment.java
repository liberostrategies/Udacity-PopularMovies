package com.example.pink.popularmovies;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.pink.popularmovies.util.NetworkUtil;

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

import static com.example.pink.popularmovies.util.ImageUtil.setImage;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailMovieActivityFragment extends Fragment {

    private static final String LOG_TAG = DetailMovieActivityFragment.class.getSimpleName();
    private String mMovieId;
    private String[] mMovieDetails;
    private String[] mTrailerVideoUrls;
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
    ListView mListViewTrailers;

    private Activity context;

    private String mApiKey = BuildConfig.THEMOVIEDB_API_KEY;

    static final String DETAIL_MOVIE_ID = "MOVIEID";

    // Construct the URL for the api.themoviedb.org query
    // Possible parameters are avaiable at API page, at
    // http://docs.themoviedb.apiary.io/#reference/configuration/configuration/get?console=1
    private String mMovieDetailsBaseUrl;
    final String API_KEY_PARAM = "api_key";
    Uri mBuiltUri;
    private TrailerAdapter mTrailerAdapter;

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

    private void fetchTrailerVideos() {
        FetchTrailerVideo trailerVideosTask = new FetchTrailerVideo();
        trailerVideosTask.execute(mTrailerVideoUrls);
    }

    /**
     * Take the String representing the complete trailer video query result in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Constructor takes the JSON string and converts it
     * into an String array containing the trailer video URLS.
     */
    private String[] getTrailerVideoDetailsFromJson(String trailerVideoJsonStr)
            throws JSONException {
        // https://www.youtube.com/watch?v=bvu-zlR5A8Q

        // These are the names of the JSON objects that need to be extracted.
        final String MOVIEDB_RESULTS = "results";
        final String MOVIEDB_SITE = "site";
        final String MOVIEDB_KEY = "key";

        JSONObject trailerVideoJson = new JSONObject(trailerVideoJsonStr);
        JSONArray trailerVideoArray = trailerVideoJson.getJSONArray(MOVIEDB_RESULTS);

        String site;
        String key;
        String trailerVideoUrl;
        int numTrailerVideos = trailerVideoArray.length();
        String[] trailerVideoUrls = new String[numTrailerVideos];

        for (int i=0; i<numTrailerVideos; i++) {
            trailerVideoUrl = "https://www.";
            JSONObject trailerVideo = trailerVideoArray.getJSONObject(i);
            site = trailerVideo.getString(MOVIEDB_SITE);
            key = trailerVideo.getString(MOVIEDB_KEY);
            trailerVideoUrl += (site + ".com/watch?v=" + key);
            Log.d(LOG_TAG, "Video " + i + " url = " + trailerVideoUrl);
            trailerVideoUrls[i] = trailerVideoUrl;
        }

        return trailerVideoUrls;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.context = this.getActivity();
        // Read the intent from the all movies grid view item selection.
        Bundle arguments = getArguments();
        View rootView = inflater.inflate(R.layout.fragment_detail_movie, container, false);

        if (arguments != null) {
            // Query for movie details in a thread in case network request blocks.
            mMovieId = arguments.getString(DetailMovieActivityFragment.DETAIL_MOVIE_ID);
            fetchMovieDetails();
            mTitle = (TextView) rootView.findViewById(R.id.title);
            mImageViewPoster = (ImageView) rootView.findViewById(R.id.poster_path);
            mReleaseDate = (TextView) rootView.findViewById(R.id.release_date);
            mVoteAverage = (TextView) rootView.findViewById(R.id.vote_average);
            mPlotSynopsis = (TextView) rootView.findViewById(R.id.plot_synopsis);

            fetchTrailerVideos();
            mListViewTrailers = (ListView) rootView.findViewById(R.id.listview_trailers);
            mTrailerAdapter = new TrailerAdapter(
                    getActivity(),
                    null,
                    0
            );
            mListViewTrailers.setAdapter(mTrailerAdapter);
            mListViewTrailers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // TO DO: Get url.
                    Log.d(LOG_TAG, "Clicked moview trailer, position=" + position + " + url = " + mTrailerVideoUrls[position]);
                    // TO DO: Set explicit intent? to launch browser/youtube to play video.
                }
            });
        }

        return rootView;
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

    public class FetchTrailerVideo extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = FetchTrailerVideo.class.getSimpleName();
        @Override
        protected String[] doInBackground(String... params) {
            // http://api.themoviedb.org/3/movie/135397/videos?api_key={MY_API_KEY}

            // Check if there is network connectivity.
            if (!NetworkUtil.isOnline(context)) {
                Log.d(LOG_TAG, "No network connectivity. Nothing will happen in the background.");
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String trailerVideoJsonString = null;

            mBuiltUri = Uri.parse(mMovieDetailsBaseUrl).buildUpon()
                    .appendPath("videos")
                    .appendQueryParameter(API_KEY_PARAM, mApiKey)
                    .build();

            try {
                URL url = new URL(mBuiltUri.toString());
                Log.v(LOG_TAG, "Built URI " + mBuiltUri.toString());
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
                trailerVideoJsonString = buffer.toString();
                Log.v(LOG_TAG, "Trailer Video JSON String: " + trailerVideoJsonString);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the traviler video data, there's no point in attemping
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

            // Retrieve trailer video data.
            try {
                String[] result = getTrailerVideoDetailsFromJson(trailerVideoJsonString);
                return result;
            } catch (JSONException je) {
                Log.e(LOG_TAG, "Error", je);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            Log.d(LOG_TAG, "post execute result=" + strings);
            if (strings != null) {
                mTrailerVideoUrls = strings;
//                mTitle.setText(mMovieDetails[IDX_TITLE]);
//                String posterPath = mMovieDetails[IDX_POSTER_PATH];
//                setImage(context, mImageViewPoster, posterPath);
//                mReleaseDate.setText("Release Date: " + mMovieDetails[IDX_RELEASE_DATE]);
//                mVoteAverage.setText("Vote Average: " + mMovieDetails[IDX_VOTE_AVERAGE]);
//                mPlotSynopsis.setText(mMovieDetails[IDX_PLOT_SYNOPSIS]);
            } else {
                mTitle.setText("Network Connectivity Lost");
            }
        }
    }

    public class FetchMovieDetails extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = FetchMovieDetails.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... params) {
            // http://api.themoviedb.org/3/movie/135397?api_key={MY_API_KEY}

            // Check if there is network connectivity.
            if (!NetworkUtil.isOnline(context)) {
                Log.d(LOG_TAG, "No network connectivity. Nothing will happen in the background.");
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieDetailsJsonString = null;

            mMovieDetailsBaseUrl = "http://api.themoviedb.org/3/movie/" + mMovieId + "?";
            mBuiltUri = Uri.parse(mMovieDetailsBaseUrl).buildUpon()
                    .appendQueryParameter(API_KEY_PARAM, mApiKey)
                    .build();

            try {
                URL url = new URL(mBuiltUri.toString());
                Log.v(LOG_TAG, "Built URI " + mBuiltUri.toString());
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

        @Override
        /**
         * Update the detail view with the background return strings of live data.
         */
        protected void onPostExecute(String[] result) {
            Log.d(LOG_TAG, "post execute result=" + result);
            if (result != null) {
                mMovieDetails = result;
                mTitle.setText(mMovieDetails[IDX_TITLE]);
                String posterPath = mMovieDetails[IDX_POSTER_PATH];
                setImage(context, mImageViewPoster, posterPath);
                mReleaseDate.setText("Release Date: " + mMovieDetails[IDX_RELEASE_DATE]);
                mVoteAverage.setText("Vote Average: " + mMovieDetails[IDX_VOTE_AVERAGE]);
                mPlotSynopsis.setText(mMovieDetails[IDX_PLOT_SYNOPSIS]);
            } else {
                mTitle.setText("Network Connectivity Lost");
            }
        }

    }
}
