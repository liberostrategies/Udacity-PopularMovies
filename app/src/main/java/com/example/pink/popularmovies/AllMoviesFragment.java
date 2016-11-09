package com.example.pink.popularmovies;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;

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

    private String apiKey;

    /** Last movie selected. */
    private int mPosition = ListView.INVALID_POSITION;

    /** Saved instance state position key. */
    private static final String SELECTED_KEY = "selected_position";

    /**
     * Adapter linking all movies data to its grid list view.
     */
    private CustomImageListAdapter mAllMoviesAdapter;
    private String[] mMovieIds = new String[0];
    private GridView mGridViewAllMovies;

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

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(String movieId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onCreateView");

        mAllMoviesAdapter = new CustomImageListAdapter(getActivity(),
                new ArrayList<String>());
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mGridViewAllMovies = (GridView) rootView.findViewById(R.id.fragment_main_gridview);
        mGridViewAllMovies.setAdapter(mAllMoviesAdapter);
        // Link the adapter with the GridView.
        mGridViewAllMovies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String movieId = mAllMoviesAdapter.getItem(position);
                movieId = movieId.substring(0, movieId.indexOf(","));
//                Intent intent = new Intent(getActivity(), DetailMovieActivity.class)
//                        .putExtra(Intent.EXTRA_TEXT, movieId);
//                startActivity(intent);
//
                ((Callback) getActivity())
                        .onItemSelected(movieId);

                mPosition = position;
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        if (mPosition != ListView.INVALID_POSITION) {
            mGridViewAllMovies.smoothScrollToPosition(mPosition);
            mGridViewAllMovies.setSelection(mPosition);
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        if (mPosition != ListView.INVALID_POSITION) {
            mGridViewAllMovies.smoothScrollToPosition(mPosition);
            mGridViewAllMovies.setSelection(mPosition);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        if (mPosition != ListView.INVALID_POSITION) {
            // TO DO: For some reason, the restored position value is correct
            // but the grid view does not display the scroll to the selected position.
            mGridViewAllMovies.smoothScrollToPosition(mPosition);
            mGridViewAllMovies.setSelection(mPosition);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save list item, when table rotates.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
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
            // http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key={API_KEY}
            // http://api.themoviedb.org/3/movie/popular?api_key={API_KEY}
            // http://api.themoviedb.org/3/movie/top_rated?api_key={API_KEY}

            // Check if there is network connectivity.
            if (!NetworkUtil.isOnline(getActivity().getBaseContext())) {
                Log.d(LOG_TAG, "No network connectivity. Nothing will happen in the background.");
                return null;
            }

            String[] popularMovies = null;
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String popularMoviesJsonString = null;

            String format = "json";
            String sortBy = "popularity.desc";
            // Read sort by from preferences.
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String sortByPref = sharedPref.getString(getString(R.string.sort_by_list_key),
                    getString(R.string.pref_sort_by_default_value));

            // Get api key.
            apiKey = BuildConfig.THEMOVIEDB_API_KEY;

            try {
                // Construct the URL for the api.themoviedb.org query
                // Possible parameters are avaiable at API page, at
                // http://docs.themoviedb.apiary.io/#reference/configuration/configuration/get?console=1
                final String POPULARMOVIES_BASE_URL =
                        "http://api.themoviedb.org/3/movie";
                final String API_KEY_PARAM = "api_key";
                Uri builtUri = Uri.parse(POPULARMOVIES_BASE_URL).buildUpon()
                        .appendPath(sortByPref)
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
         * Take the String representing the complete movies query result in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getPopularMoviesIdsFromJson(String popularMoviesJsonStr)
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
                result[i] = movieId + ", " + posterPath;
            }

            for (String s : result) {
                Log.v(LOG_TAG, "Movie id: " + s);
            }
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
