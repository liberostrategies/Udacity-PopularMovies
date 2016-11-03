package com.example.pink.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity
    implements AllMoviesFragment.Callback {

    private boolean mTwoPane;
    static final String DETAILFRAGMENT_TAG = "DFTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.movie_list_item_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.movie_list_item_container, new DetailMovieActivityFragment())
                        .commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }

        AllMoviesFragment allMoviesFragment = ((AllMoviesFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_forecast));
//        allMoviesFragment.setUseTodayLayout(!mTwoPane);

//        if (BuildConfig.DEBUG) {
//            Stetho.initialize(
//                    Stetho.newInitializerBuilder(this)
//                            .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
//                            .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
//                            .build());
//        }
    }

    @Override
    public void onItemSelected(String movieId) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putString(DetailMovieActivityFragment.DETAIL_MOVIE_ID, movieId);

            DetailMovieActivityFragment fragment = new DetailMovieActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_list_item_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
//            Intent intent = new Intent(this, DetailMovieActivityFragment.class)
//                    .setData(movieId);
//            startActivity(intent);

            Intent intent = new Intent(this, DetailMovieActivity.class)
                    .putExtra(Intent.EXTRA_TEXT, movieId);
            startActivity(intent);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



}
