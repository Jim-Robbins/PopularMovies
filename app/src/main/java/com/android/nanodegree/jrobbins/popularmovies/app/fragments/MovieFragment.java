package com.android.nanodegree.jrobbins.popularmovies.app.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;

import com.android.nanodegree.jrobbins.popularmovies.app.R;
import com.android.nanodegree.jrobbins.popularmovies.app.data.MoviesContract;
import com.android.nanodegree.jrobbins.popularmovies.app.services.MovieDataService;
import com.android.nanodegree.jrobbins.popularmovies.app.utils.Utility;
import com.android.nanodegree.jrobbins.popularmovies.app.views.adapters.MovieCursorAdapter;


/**
 * Copied structure from Sunshine App example, Fragment to create Grid list of movie titles
 */
public class MovieFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MovieFragment.class.getSimpleName();

    private static final String SELECTED_KEY = "selected_position";
    private static final int MOVIES_LOADER = 0;

    private MovieCursorAdapter mMovieAdapter;
    private GridView mMovieGridList;

    private int mPosition = GridView.INVALID_POSITION;

    // For the movies list view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] MOVIES_COLUMNS = {
            MoviesContract.MovieEntry.TABLE_NAME + "." + MoviesContract.MovieEntry._ID,
            MoviesContract.MovieEntry.TABLE_NAME + "." + MoviesContract.MovieEntry.COLUMN_MOVIE_ID,
            MoviesContract.MovieEntry.TABLE_NAME + "." + MoviesContract.MovieEntry.COLUMN_POSTER
    };

    // These indices are tied to MOVIES_COLUMNS.  If MOVIES_COLUMNS changes, these
    // must change.
    public static final int COL_ID = 0;
    public static final int COL_MOVIE_ID = 1;
    public static final int COL_MOVIE_POSTER = 2;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * MovieFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri contentUri);
    }

    public MovieFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Create instance of our cursor Adapter
        mMovieAdapter = new MovieCursorAdapter(getActivity(), null, 0);

        // Get reference to our grid list and apply our adapter
        mMovieGridList = (GridView) rootView.findViewById(R.id.gridview_movies);
        mMovieGridList.setAdapter(mMovieAdapter);

        // We'll call our MainActivity
        mMovieGridList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    ((Callback) getActivity())
                            .onItemSelected(MoviesContract.MovieEntry.buildMovieWithMovieIdUri(
                                    cursor.getString(COL_MOVIE_ID)
                            ));
                }
                mPosition = position;
            }
        });

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The gridview probably hasn't even been populated yet.  Actually perform the
            // swap out in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        // Initialize our content loader
        getLoaderManager().initLoader(MOVIES_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Since we read the list type when we create the loader, all we need to do is restart things
     */
    public void onListTypeChanged() {
        updateMovies();
    }

    /**
     * Update the movie list by making a call to TMDb API
     */
    private void updateMovies()
    {
        Log.d(LOG_TAG,"Update Movies");
        // If we don't have a connection, show message to user.
        if (Utility.isOnline() && Utility.isNetworkAvailable(getActivity())) {
            Intent intent = new Intent(getActivity(), MovieDataService.class);
            intent.setData(
                    MoviesContract.MovieEntry.buildMoviesWithListTypeUri(
                            Utility.getPreferredMovieList(getActivity())
                    ));
            getActivity().startService(intent);
        }
        else
        {
            // If we don't have a connection, try to load the data from the local db
            reloadCursorData();
        }
    }

    /**
     * Reload our cursor when we are notified there has been a data update
     */
    public void reloadCursorData() {
        getLoaderManager().restartLoader(MOVIES_LOADER, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to gridview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        String listTypeSetting = Utility.getPreferredMovieList(getActivity());
        Uri moviesUri = MoviesContract.MovieEntry.buildMoviesWithListTypeUri(listTypeSetting);

        //TODO::Determine SORT ORDER by List Type
        
        return new CursorLoader(getActivity(),
                moviesUri,
                MOVIES_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMovieAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mMovieGridList.smoothScrollToPosition(mPosition);
        } else {
            data.moveToFirst();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver((mMessageReceiver),
                new IntentFilter(MovieDataService.API_RESULT_SUCCESS)
        );

    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    /**
     * Our handler for received Intents. This will be called whenever a status event is
     * broadcast from the MovieDataService
     */
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            reloadCursorData();
        }
    };
}
