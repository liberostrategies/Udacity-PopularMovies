package com.example.pink.popularmovies;

import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

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
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AllMoviesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AllMoviesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AllMoviesFragment extends Fragment {
    private final String LOG_TAG = AllMoviesFragment.class.getSimpleName();

    /**
     * Adapter linking all movies data to its grid list view.
     */
    private CustomImageListAdapter mAllMoviesAdapter;
    private String[] mMovieIds = new String[0];

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AllMoviesFragment.
     */
    // TODO: Rename and change types and number of parameters

    public static AllMoviesFragment newInstance(String param1, String param2) {
        AllMoviesFragment fragment = new AllMoviesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public AllMoviesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        fetchMovies();
    }

    private void fetchMovies() {
        FetchAllMoviesTask moviesTask = new FetchAllMoviesTask();
        // TO DO: Read sort_by setting from preferences.
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortBy = sharedPref.getString("sortby", "popularity.desc");
        moviesTask.execute(sortBy);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    // Prepare some dummy data for gridview
    private ArrayList<ImageItem> getData() {
        final ArrayList<ImageItem> imageItems = new ArrayList<>();
        TypedArray imgs = getResources().obtainTypedArray(R.array.image_test_urls);
        int lengthTest = 13;
        for (int i = 0; i < lengthTest; i++) {
//            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imgs.getResourceId(i, -1));
            imageItems.add(new ImageItem(imgs.getString(i), "Image#" + i));
        }
        return imageItems;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onCreateView");

        // TO DO: Query for movie titles and IDs? to get poster.
        mAllMoviesAdapter = new CustomImageListAdapter(getActivity(),
//                mMovieIds);
//                new String []{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14"});
                new ArrayList<String>());
// ListView Implementation
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
//        ListView listViewAllMovies = (ListView) rootView.findViewById(R.id.fragment_main_listview);
//        listViewAllMovies.setAdapter(adapter);
// GridView implementation
        GridView gridViewAllMovies = (GridView) rootView.findViewById(R.id.fragment_main_gridview);
        gridViewAllMovies.setAdapter(mAllMoviesAdapter);

        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    public class FetchAllMoviesTask extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = FetchAllMoviesTask.class.getSimpleName();

        @Override
        // Sort options: most popular, highest rated

        protected String[] doInBackground(String... params) {
            // http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=ec842fdd2a58bc4d60d0e08a6576cb52
            // TO DO: Use my API key
            String[] popularMovies = null;
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String popularMoviesJsonString = null;

            String format = "json";
            String sortBy = "popularity.desc";
            String apiKey = "ec842fdd2a58bc4d60d0e08a6576cb52";
            try {
                // Construct the URL for the api.themoviedb.org query
                // Possible parameters are avaiable at API page, at
                // http://docs.themoviedb.apiary.io/#reference/configuration/configuration/get?console=1
                final String POPULARMOVIES_BASE_URL =
                        "http://api.themoviedb.org/3/discover/movie?";
                final String SORT_BY_PARAM = "sort_by";
                final String API_KEY_PARAM = "api_key";
                Uri builtUri = Uri.parse(POPULARMOVIES_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_BY_PARAM, params[0])
                        .appendQueryParameter(API_KEY_PARAM, apiKey)
                        .build();
//                URL url = new URL("http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=ec842fdd2a58bc4d60d0e08a6576cb52");
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
                popularMoviesJsonString = buffer.toString();
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
//                List<String> listMovies = getPopularMoviesIdsFromJson(popularMoviesJsonString);
//                String[] result = new String[listMovies.size()];
//                result = listMovies.toArray(result);
//                return result;
                String[] result = getPopularMoviesIdsFromJson(popularMoviesJsonString);
                mMovieIds = result;
                return result;
            } catch (JSONException je) {
                Log.e(LOG_TAG, "Error", je);
            }
            return null;

            //return new String[0];
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getPopularMoviesIdsFromJson(String popularMoviesJsonStr)
//        private List<String> getPopularMoviesIdsFromJson(String popularMoviesJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String MOVIEDB_ID = "id";
            final String MOVIEDB_RESULTS = "results";
            final String MOVIEDB_POSTER_PATH = "poster_path";
            final String MOVIEDB_TOTAL_RESULTS = "total_results";

            JSONObject forecastJson = new JSONObject(popularMoviesJsonStr);
            JSONArray moviesArray = forecastJson.getJSONArray(MOVIEDB_RESULTS);
            int totalResults = forecastJson.getInt(MOVIEDB_TOTAL_RESULTS);

            // themoviedb returns popular movies ids based upon the sort by parameter.

            List<String> resultStrs = new ArrayList<String>();
            String[] result = new String[moviesArray.length()];
            for(int i = 0; i < moviesArray.length(); i++) {
                String movieId;
                String posterPath;

                // Get the JSON object representing the day
                JSONObject movie = moviesArray.getJSONObject(i);

                movieId = movie.getString(MOVIEDB_ID);
                posterPath = movie.getString(MOVIEDB_POSTER_PATH);
//                resultStrs.add(movieId);
                result[i] = movieId + ", " + posterPath;
            }

            for (String s : result) {
//            for (String s : resultStrs) {
                Log.v(LOG_TAG, "Movie id: " + s);
            }
//            return resultStrs;
//            List<String> listMovies = getPopularMoviesIdsFromJson(popularMoviesJsonString);
//            String[] result = new String[listMovies.size()];
//            result = listMovies.toArray(result);
//            String[] result = new String[resultStrs.size()];
//            result = resultStrs.toArray(result);
            return result;
        }

        @Override
        /**
         * Update the list view with the background return strings of live data.
         */
        protected void onPostExecute(String[] result) {
            if (result != null) {
                mAllMoviesAdapter.clear();
                for (String movieIdStr: result) {
                    mAllMoviesAdapter.add(movieIdStr);
                }
            }
        }
    }
}
