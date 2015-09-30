package com.example.pink.popularmovies;

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;

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
    private ArrayAdapter<String> mAllMoviesAdapter;

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
        CustomImageListAdapter adapter = new CustomImageListAdapter(getActivity(),
                new String []{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14"});
// ListView Implementation
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
//        ListView listViewAllMovies = (ListView) rootView.findViewById(R.id.fragment_main_listview);
//        listViewAllMovies.setAdapter(adapter);
// GridView implementation
        GridView gridViewAllMovies = (GridView) rootView.findViewById(R.id.fragment_main_gridview);
        gridViewAllMovies.setAdapter(adapter);

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
            return new String[0];
        }
    }
}
