package com.example.pink.popularmovies;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.pink.popularmovies.data.MovieContract;
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
import java.util.Arrays;
import java.util.List;

import static com.example.pink.popularmovies.R.string.label_favorites_off;
import static com.example.pink.popularmovies.R.string.label_favorites_on;
import static com.example.pink.popularmovies.util.ImageUtil.setImage;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailMovieActivityFragment extends Fragment {

    private static final String LOG_TAG = DetailMovieActivityFragment.class.getSimpleName();
    private String mMovieId;
    private String[] mMovieDetails;
    private String[] mTrailerVideoUrls;
    private MovieReview[] mReviews;
    protected final static int IDX_TITLE = 0;
    protected final static int IDX_POSTER_PATH = 1;
    protected final static int IDX_RELEASE_DATE = 2;
    protected final static int IDX_VOTE_AVERAGE = 3;
    protected final static int IDX_PLOT_SYNOPSIS = 4;
    TextView mTitle;
    ImageView mImageViewPoster;
    String mPosterPath;
    TextView mReleaseDate;
    TextView mVoteAverage;
    TextView mPlotSynopsis;
    TextView mFavoritesHint;
    ImageButton mbtnFavorite;
    private static final String BTN_FAVORITE_ON = "star_on";
    private static final String BTN_FAVORITE_OFF = "star_off";
    ListView mListViewTrailers;
    ListView mListViewReviews;

    private Activity context;

    private String mApiKey = BuildConfig.THEMOVIEDB_API_KEY;

    static final String DETAIL_MOVIE_ID = "MOVIEID";

    // Construct the URL for the api.themoviedb.org query
    // Possible parameters are avaiable at API page, at
    // http://docs.themoviedb.apiary.io/#reference/configuration/configuration/get?console=1
    private String mMovieDetailsBaseUrl;
    private final String API_KEY_PARAM = "api_key";
    private final String TRAILER_VIDEOS_PARAM = "videos";
    private final String REVIEWS_PARAM = "reviews";
    Uri mBuiltUri;
    private TrailerAdapter mTrailerAdapter;
    private ReviewAdapter mReviewAdapter;

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

    private void fetchReviews() {
        FetchReviews reviewsTask = new FetchReviews();
        reviewsTask.execute(mMovieId);
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

    /**
     * Take the String representing the complete trailer video query result in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Constructor takes the JSON string and converts it
     * into an String array containing the reviews.
     */
    private MovieReview[] getReviewDetailsFromJson(String reviewsJsonStr)
            throws JSONException {
        // These are the names of the JSON objects that need to be extracted.
        final String MOVIEDB_RESULTS = "results";
        final String MOVIEDB_AUTHOR = "author";
        final String MOVIEDB_CONTENT = "content";

        JSONObject reviewsJson = new JSONObject(reviewsJsonStr);
        JSONArray reviewsJsonArray = reviewsJson.getJSONArray(MOVIEDB_RESULTS);
        int numReviews = reviewsJsonArray.length();
        String author;
        String content;

        MovieReview[] result = new MovieReview[numReviews];
        for (int i=0; i<numReviews; i++) {
            JSONObject reviewJsonObj = reviewsJsonArray.getJSONObject(i);
            author = reviewJsonObj.getString(MOVIEDB_AUTHOR);
            content = reviewJsonObj.getString(MOVIEDB_CONTENT);
            Log.v(LOG_TAG, "Review data: " + author + ": "  + content);
            MovieReview review = new MovieReview(author, content);
            result[i] = review;
        }

        return result;
    }

    /**
     * Mark the button and label, if movie is a favorite.
     */
    private void displayFavorite(boolean isFavorite) {
        if (isFavorite) {
            // if favorite, init display as favorite.
            mFavoritesHint.setText(getResources().getString(label_favorites_off));
            mbtnFavorite.setImageResource(android.R.drawable.star_on);
            mbtnFavorite.setTag(BTN_FAVORITE_ON);
        } else {
            // If not favorite, init display as not favorite.
            mFavoritesHint.setText(getResources().getString(label_favorites_on));
            mbtnFavorite.setImageResource(android.R.drawable.star_off);
            mbtnFavorite.setTag(BTN_FAVORITE_OFF);
        }
    }

    /**
     * Check database if movie is a favorite.
     * @return
     */
    private boolean isFavoriteInDB() {
        Cursor cursor = context.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                null, // projection; leaving "columns" null just returns all the columns.
                MovieContract.MovieEntry.COLUMN_MOVIEDB_ID + " = " + mMovieId,
                null,   // Values for the "where" clause
                null    // sort order
        );

        if (cursor.moveToFirst()) {
            return true;
        }
        return false;
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
            mFavoritesHint = (TextView) rootView.findViewById(R.id.txtFavoritesHint);
            mbtnFavorite = (ImageButton) rootView.findViewById(R.id.imgbtnFavorite);
            displayFavorite(isFavoriteInDB());
            mbtnFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String favoriteTag = (String)mbtnFavorite.getTag();
                    Log.d(LOG_TAG, "favorite button image name = " + favoriteTag);
                    // TO DO: Toggle as favorite to DB.
                    if (favoriteTag.equals("star_off")) {
                        // If movie is not favorite,
                        // add it as favorite,
                        // set the image button to star on.
                        markFavorite(true);
                    } else {
                        // If movie is favorite,
                        // set it as not a favorite,
                        // set the image button to star off.
                        markFavorite(false);
                    }
                }
            });

            fetchTrailerVideos();
            mListViewTrailers = (ListView) rootView.findViewById(R.id.listview_trailers);
            ((TextView) rootView.findViewById(R.id.txtTrailersLabel)).setText(getResources().getString(R.string.label_trailers));

            fetchReviews();
            mListViewReviews = (ListView) rootView.findViewById(R.id.listview_reviews);
            ((TextView) rootView.findViewById(R.id.txtReviewsLabel)).setText(getResources().getString(R.string.label_reviews));
        }

        return rootView;
    }

    private long addFavorite() {
        ContentValues valuesFavorite = new ContentValues();
        valuesFavorite.put(MovieContract.MovieEntry.COLUMN_MOVIEDB_ID, mMovieId);
        valuesFavorite.put(MovieContract.MovieEntry.COLUMN_POSTER, mPosterPath);
        valuesFavorite.put(MovieContract.MovieEntry.COLUMN_TITLE, mTitle.getText().toString());
        valuesFavorite.put(MovieContract.MovieEntry.COLUMN_SYNOPSIS, mPlotSynopsis.getText().toString());
        valuesFavorite.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, mReleaseDate.getText().toString());
        valuesFavorite.put(MovieContract.MovieEntry.COLUMN_USER_RATING, mVoteAverage.getText().toString());
        Uri uriInserted = context.getContentResolver().insert(
                MovieContract.MovieEntry.CONTENT_URI,
                valuesFavorite
        );
        // The resulting URI contains the ID for the row.  Extract the locationId from the Uri.
        long insertedRowId = ContentUris.parseId(uriInserted);
        return insertedRowId;
    }

    private long removeFavorite() {
        long numDeletedRows = context.getContentResolver().delete(
                MovieContract.MovieEntry.CONTENT_URI,
                MovieContract.MovieEntry.COLUMN_MOVIEDB_ID + " = ?",
                new String[] {mMovieId}
        );
        return numDeletedRows;
    }

    private long markFavorite(boolean doMark) {
        displayFavorite(doMark);
        if (doMark) {
            return addFavorite();
        } else {
            return removeFavorite();
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
                    .appendPath(TRAILER_VIDEOS_PARAM)
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
                // If the code didn't successfully get the trailer video data, there's no point in attemping
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
                if (mTrailerAdapter == null) {
                    mTrailerAdapter = new TrailerAdapter(
                            getActivity(),
                            new ArrayList<String>(Arrays.asList(mTrailerVideoUrls))
                    );
                    mListViewTrailers.setAdapter(mTrailerAdapter);
                    setListViewHeightBasedOnChildren(mListViewTrailers);
                    mTrailerAdapter.notifyDataSetChanged();
                }
            } else {
                mTitle.setText("Network Connectivity Lost");
            }
        }
    }

    /**
     * Snipped from http://stackoverflow.com/questions/27646209/disable-scrolling-for-listview-and-enable-for-whole-layout.
     * @param listView
     */
    public static void setListViewHeightBasedOnChildren(ListView listView)
    {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight=0;
        View view = null;

        for (int i = 0; i < listAdapter.getCount(); i++)
        {
            view = listAdapter.getView(i, view, listView);

            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth,
                        ViewGroup.LayoutParams.MATCH_PARENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();

        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + ((listView.getDividerHeight()) * (listAdapter.getCount()));

        listView.setLayoutParams(params);
        listView.requestLayout();

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
                mPosterPath = posterPath;
                mReleaseDate.setText("Release Date: " + mMovieDetails[IDX_RELEASE_DATE]);
                mVoteAverage.setText("Vote Average: " + mMovieDetails[IDX_VOTE_AVERAGE]);
                mPlotSynopsis.setText(mMovieDetails[IDX_PLOT_SYNOPSIS]);
            } else {
                mTitle.setText("Network Connectivity Lost");
            }
        }
    }

    public class FetchReviews extends AsyncTask<String, Void, MovieReview[]> {
        private final String LOG_TAG = FetchReviews.class.getSimpleName();

        @Override
        protected MovieReview[] doInBackground(String... params) {
            // http://api.themoviedb.org/3/movie/135397/reviews?api_key={MY_API_KEY}

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
            String reviewJsonString = null;

            mBuiltUri = Uri.parse(mMovieDetailsBaseUrl).buildUpon()
                    .appendPath(REVIEWS_PARAM)
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
                reviewJsonString = buffer.toString();
                Log.v(LOG_TAG, "Review JSON String: " + reviewJsonString);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the review data, there's no point in attemping
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

            // Retrieve review data.
            try {
                MovieReview[] result = getReviewDetailsFromJson(reviewJsonString);
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
        protected void onPostExecute(MovieReview[] result) {
            Log.d(LOG_TAG, "post execute result=" + result);
            if (result != null) {
                mReviews = result;
                if (mReviewAdapter == null) {
                    mReviewAdapter = new ReviewAdapter(
                            getActivity(),
                            new ArrayList<MovieReview>(Arrays.asList(mReviews))
                    );
                    mListViewReviews.setAdapter(mReviewAdapter);
                    setListViewHeightBasedOnChildren(mListViewReviews);
                    mReviewAdapter.notifyDataSetChanged();
                }
            } else {
                mTitle.setText("Network Connectivity Lost");
            }
        }

    }
}
