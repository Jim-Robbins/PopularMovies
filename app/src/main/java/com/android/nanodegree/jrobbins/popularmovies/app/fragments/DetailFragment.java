package com.android.nanodegree.jrobbins.popularmovies.app.fragments;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.nanodegree.jrobbins.popularmovies.app.BuildConfig;
import com.android.nanodegree.jrobbins.popularmovies.app.R;
import com.android.nanodegree.jrobbins.popularmovies.app.data.MoviesContract;
import com.android.nanodegree.jrobbins.popularmovies.app.data.MoviesContract.MovieEntry;
import com.android.nanodegree.jrobbins.popularmovies.app.models.MovieTrailer;
import com.android.nanodegree.jrobbins.popularmovies.app.services.MovieDataService;
import com.android.nanodegree.jrobbins.popularmovies.app.utils.Utility;
import com.android.nanodegree.jrobbins.popularmovies.app.views.adapters.MovieTrailerAdaptor;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import static android.os.Build.VERSION_CODES.M;
import static com.android.nanodegree.jrobbins.popularmovies.app.services.MovieDataService.MOVIE_DB_KEY_RESULTS;

/**
 * Display the movie detail information
 * Created by jim.robbins on 9/8/16.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    public static final String DETAIL_URI = "URI";

    private Uri mUri;
    private String mMovieID;

    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            MovieEntry.TABLE_NAME + "." + MovieEntry.COLUMN_MOVIE_ID,
            MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_OVERVIEW,
            MovieEntry.COLUMN_POSTER,
            MovieEntry.COLUMN_BACKDROP_PATH,
            MovieEntry.COLUMN_VOTE_AVG,
            MovieEntry.COLUMN_RELEASE_DATE,
            MovieEntry.COLUMN_HOMEPAGE,
            MovieEntry.COLUMN_IMDB_ID,
            MovieEntry.COLUMN_POPULARITY,
            MovieEntry.COLUMN_PRODUCTION_COMPANIES,
            MovieEntry.COLUMN_RUNTIME,
            MovieEntry.COLUMN_VOTE_COUNT,
            MovieEntry.COLUMN_GENRES,
            MovieEntry.COLUMN_TRAILERS,
            MovieEntry.COLUMN_REVIEWS

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
    public static final int COL_MOVIE_HOMEPAGE = 7;
    public static final int COL_MOVIE_IMDB = 8;
    public static final int COL_MOVIE_POPULARITY = 9;
    public static final int COL_MOVIE_PROD_COMP = 10;
    public static final int COL_MOVIE_RUNTIME = 11;
    public static final int COL_MOVIE_VOTE_COUNT = 12;
    public static final int COL_MOVIE_GENRES = 13;
    public static final int COL_MOVIE_TRAILERS = 14;
    public static final int COL_MOVIE_REVIEWS = 15;



    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    private TextView mTitleView;
    private TextView mVoteAvgView;
    private TextView mSummaryView;
    private TextView mReleaseYearView;
    private ImageView mPosterView;
    private TextView mRuntimeView;
    private TextView mHomepageView;
    private TextView mCompanyView;
    private TextView mGenresView;
    private TextView mImdbView;
    private ListView mTrailersListView;
    private Button mFavoriteButton;

    private MovieTrailer[] movieTrailers;

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

        if(mUri != null) {
            mMovieID = MoviesContract.MovieEntry.getMovieIdFromUri(mUri);
            getMovieDetails();
        }

        //Inflate the view from the fragment
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mTitleView = (TextView) rootView.findViewById(R.id.textview_movie_title);
        mPosterView = (ImageView) rootView.findViewById(R.id.imageview_movie_poster);
        mVoteAvgView = (TextView) rootView.findViewById(R.id.textview_movie_rating);
        mSummaryView = (TextView) rootView.findViewById(R.id.textview_movie_summary);
        mReleaseYearView = (TextView) rootView.findViewById(R.id.textview_movie_year);
        mRuntimeView = (TextView) rootView.findViewById(R.id.textview_movie_length);
        mHomepageView = (TextView) rootView.findViewById(R.id.textview_movie_homepage);
        mCompanyView = (TextView) rootView.findViewById(R.id.textview_movie_production);
        mGenresView = (TextView) rootView.findViewById(R.id.textview_movie_genres);
        mImdbView = (TextView) rootView.findViewById(R.id.textview_movie_imdb);
        mFavoriteButton = (Button) rootView.findViewById(R.id.button_mark_favorite);

        mTrailersListView = (ListView) rootView.findViewById(R.id.list_trailers);

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
        getMovieDetails();
        super.onActivityCreated(savedInstanceState);
    }

    public void reloadCursorData( ) {
        getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
    }

    private void getMovieDetails()
    {
        Log.d(LOG_TAG,"Get Movie Details, Trailer & Ratings");
        // If we don't have a connection, show message to user.
        if (Utility.isOnline() && Utility.isNetworkAvailable(getActivity())) {
            Intent intent = new Intent(getActivity(), MovieDataService.class);
            intent.setData(mUri);
            getActivity().startService(intent);
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

            String releaseYear = data.getString(COL_MOVIE_RELEASE_DATE).substring(0,4);
            mReleaseYearView.setText(releaseYear);

            hyperlinkTextView(mHomepageView, data.getString(COL_MOVIE_HOMEPAGE), data.getString(COL_MOVIE_HOMEPAGE));
            hyperlinkTextView(mImdbView, getString(R.string.detail_imdb_link, data.getString(COL_MOVIE_IMDB)), getString(R.string.detail_imdb_text));

            mRuntimeView.setText(getString(R.string.detail_movie_runtime, data.getInt(COL_MOVIE_RUNTIME)));

            mCompanyView.setText(data.getString(COL_MOVIE_PROD_COMP));
            mGenresView.setText(data.getString(COL_MOVIE_GENRES));

            if (data.getDouble(COL_MOVIE_VOTE_COUNT) > 0) {
                mVoteAvgView.setText(getString(R.string.detail_movie_user_rating_with_votes, data.getDouble(COL_MOVIE_VOTE_AVG), data.getInt(COL_MOVIE_VOTE_COUNT)));
            } else {
                mVoteAvgView.setText(getString(R.string.detail_movie_user_rating, data.getDouble(COL_MOVIE_VOTE_AVG)));
            }

            String sTrailers = data.getString(COL_MOVIE_TRAILERS);
            if(!TextUtils.isEmpty(sTrailers)) {
                try {
                    JSONObject jsonTrailers = new JSONObject(sTrailers);
                    if (jsonTrailers != null) {
                        ArrayList<MovieTrailer> movieTrailers = parseTrailersJSONArray(jsonTrailers.getJSONArray(MOVIE_DB_KEY_RESULTS));
                        MovieTrailerAdaptor trailerAdaptor = new MovieTrailerAdaptor(getActivity(), movieTrailers);

                        mTrailersListView.setAdapter(trailerAdaptor);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            String sReviews = data.getString(COL_MOVIE_REVIEWS);
            if(!TextUtils.isEmpty(sReviews)) {
                try {
                    JSONObject jsonReviews = new JSONObject(sReviews);
                    if (jsonReviews != null) {
                        parseReviewsJSONArray(jsonReviews.getJSONArray(MOVIE_DB_KEY_RESULTS));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

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
                String backdropPath = MovieDataService.getTheMovieDBApiPosterUri(MovieDataService.MOVIE_DB_IMG_SIZE_342, data.getString(COL_MOVIE_BACKDROP));
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

    private ArrayList<MovieTrailer> parseTrailersJSONArray(JSONArray jsonArray)
    {
        ArrayList<MovieTrailer> movieTrailers = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                MovieTrailer movieTrailer = new MovieTrailer(jsonArray.getJSONObject(i));
                if (movieTrailer != null) {
                    movieTrailers.add(movieTrailer);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }


        }

        return movieTrailers;
    }

    private void parseReviewsJSONArray(JSONArray jsonArray)
    {
        JSONObject jsonObject;

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                jsonObject = jsonArray.getJSONObject(i);
                Log.d(LOG_TAG, jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }

            //TODO::Create list to populate list view
        }
    }

    private void hyperlinkTextView(TextView txtView, String url, String linkText)
    {
        Spanned anchorText =  Html.fromHtml("<a href=\""+url+"\">"+linkText+"</a>");
        txtView.setText(anchorText);
        txtView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

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

    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "custom-event-name" is broadcasted.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            reloadCursorData();
        }
    };
}
