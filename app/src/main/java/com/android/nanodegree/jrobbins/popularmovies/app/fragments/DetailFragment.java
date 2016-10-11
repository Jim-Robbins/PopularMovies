package com.android.nanodegree.jrobbins.popularmovies.app.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.nanodegree.jrobbins.popularmovies.app.BuildConfig;
import com.android.nanodegree.jrobbins.popularmovies.app.data.MoviesContract;
import com.android.nanodegree.jrobbins.popularmovies.app.data.MoviesContract.MovieEntry;
import com.android.nanodegree.jrobbins.popularmovies.app.services.MovieDataService;
import com.android.nanodegree.jrobbins.popularmovies.app.models.Movie;
import com.android.nanodegree.jrobbins.popularmovies.app.R;
import com.android.nanodegree.jrobbins.popularmovies.app.utils.Utility;
import com.squareup.picasso.Picasso;

import static android.R.attr.x;
import static com.android.nanodegree.jrobbins.popularmovies.app.R.id.container;

/**
 * Display the movie detail information
 * Created by jim.robbins on 9/8/16.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final String MOVIE_LIST_INTENT = "movieVOIntent";
    public static final String DETAIL_URI = "URI";

    private Uri mUri;

    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            MovieEntry.TABLE_NAME + "." + MovieEntry.COLUMN_MOVIE_ID,
            MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_OVERVIEW,
            MovieEntry.COLUMN_POSTER,
            MovieEntry.COLUMN_BACKDROP_PATH,
            MovieEntry.COLUMN_VOTE_AVG,
            MovieEntry.COLUMN_RELEASE_DATE,
            MovieEntry.COLUMN_GENRE_IDS
            
            // This works because the WeatherProvider returns location data joined with
            // weather data, even though they're stored in two different tables.
            //MoviesContract.LocationEntry.COLUMN_LOCATION_SETTING
    };

    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
    // must change.
    public static final int COL_MOVIE_ID = 0;
    public static final int COL_MOVIE_TITLE = 1;
    public static final int COL_MOVIE_OVERVIEW = 2;
    public static final int COL_MOVIE_POSTER = 3;
    public static final int COL_MOVIE_BACKDROP = 4;
    public static final int COL_MOVIE_VOTE_AVG = 5;
    public static final int COL_MOVIE_RELEASE_DATE = 6;
    public static final int COL_MOVIE_GENRE_IDS = 7;
    
    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    private TextView mTitleView;
    private TextView mVoteAvgView;
    private TextView mSummaryView;
    private TextView mReleaseYearView;
    private ImageView mPosterView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        } else {
            Intent intent = getActivity().getIntent();
            if (intent != null) {
                mUri = intent.getData();
            }
        }

        //Inflate the view from the fragment
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mTitleView = (TextView) rootView.findViewById(R.id.textview_movie_title);
        mPosterView = (ImageView) rootView.findViewById(R.id.imageview_movie_poster);
        mVoteAvgView = (TextView) rootView.findViewById(R.id.textview_movie_rating);
        mSummaryView = (TextView) rootView.findViewById(R.id.textview_movie_summary);
        mReleaseYearView = (TextView) rootView.findViewById(R.id.textview_movie_year);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.detailfragment, menu);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    void onListTypeChanged( String newListType ) {
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if (null != uri) {
            String listType = MovieEntry.getListTypeFromUri(uri);
            Uri updatedUri = MovieEntry.buildMoviesWithListTypeUri(listType);
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if ( null != mUri ) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {

            mTitleView.setText(data.getString(COL_MOVIE_TITLE));
            mSummaryView.setText(data.getString(COL_MOVIE_OVERVIEW));
            mReleaseYearView.setText(data.getString(COL_MOVIE_RELEASE_DATE).substring(0, 4));
            mVoteAvgView.setText(getString(R.string.detail_movie_user_rating, data.getFloat(COL_MOVIE_VOTE_AVG)));

            //Load poster into image view using Picasso
            if (BuildConfig.DEBUG) {
                Picasso.with(getActivity()).setIndicatorsEnabled(true);
                Picasso.with(getActivity()).setLoggingEnabled(true);
            }
            String posterPath = MovieDataService.getTheMovieDBApiPosterUri(MovieDataService.MOVIE_DB_IMG_SIZE_154, data.getString(COL_MOVIE_POSTER));
            Picasso.with(getActivity())
                    .load(posterPath)
                    .fit()
                    .placeholder(R.drawable.progress_animation)
                    .error(R.drawable.no_poster_available)
                    .into(mPosterView);

                /* Something I am playing with for Stage 2
                String backdropPath = MovieDataService.getTheMovieDBApiPosterUri(MovieDataService.MOVIE_DB_IMG_SIZE_342, movie.getBackdropPath());
                Picasso.with(getActivity())
                        .load(backdropPath)
                        .into(new Target(){

                            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                titleTxt.setBackground(new BitmapDrawable(getResources(), bitmap));
                            }

                            @Override
                            public void onBitmapFailed(final Drawable errorDrawable) {
                                Log.d(LOG_TAG, "FAILED");
                            }

                            @Override
                            public void onPrepareLoad(final Drawable placeHolderDrawable) {
                                Log.d(LOG_TAG, "Prepare Load");
                            }
                        });*/

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

}
