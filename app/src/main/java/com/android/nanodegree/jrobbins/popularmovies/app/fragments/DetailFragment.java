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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.nanodegree.jrobbins.popularmovies.app.BuildConfig;
import com.android.nanodegree.jrobbins.popularmovies.app.R;
import com.android.nanodegree.jrobbins.popularmovies.app.data.MoviesContract;
import com.android.nanodegree.jrobbins.popularmovies.app.data.MoviesContract.FavoritesEntry;
import com.android.nanodegree.jrobbins.popularmovies.app.data.MoviesContract.MovieEntry;
import com.android.nanodegree.jrobbins.popularmovies.app.models.MovieReview;
import com.android.nanodegree.jrobbins.popularmovies.app.models.MovieTrailer;
import com.android.nanodegree.jrobbins.popularmovies.app.services.MovieDataService;
import com.android.nanodegree.jrobbins.popularmovies.app.utils.Utility;
import com.android.nanodegree.jrobbins.popularmovies.app.views.adapters.MovieReviewAdaptor;
import com.android.nanodegree.jrobbins.popularmovies.app.views.adapters.MovieTrailerAdaptor;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.android.nanodegree.jrobbins.popularmovies.app.services.MovieDataService.MOVIE_DB_KEY_RESULTS;

/**
 * Display the movie detail information
 * Created by jim.robbins on 9/8/16.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    public static final String DETAIL_URI = "URI";
    private static final int DETAIL_LOADER = 0;

    private Uri mUri;
    private Boolean mIsFavorite;

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
            MovieEntry.COLUMN_REVIEWS,
            FavoritesEntry.COLUMN_IS_FAVORITE
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
    public static final int COL_MOVIE_FAVORITE = 16;

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
    private ListView mReviewsListView;
    private ImageButton mFavoriteButton;

    public DetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Handle two-pane or single pane modes
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
        mRuntimeView = (TextView) rootView.findViewById(R.id.textview_movie_length);
        mHomepageView = (TextView) rootView.findViewById(R.id.textview_movie_homepage);
        mCompanyView = (TextView) rootView.findViewById(R.id.textview_movie_production);
        mGenresView = (TextView) rootView.findViewById(R.id.textview_movie_genres);
        mImdbView = (TextView) rootView.findViewById(R.id.textview_movie_imdb);
        mFavoriteButton = (ImageButton) rootView.findViewById(R.id.imagebutton_mark_favorite);

        mTrailersListView = (ListView) rootView.findViewById(R.id.list_trailers);
        mReviewsListView = (ListView) rootView.findViewById(R.id.list_reviews);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        // Make additional API call to get further movie details
        if (mUri != null) {
            getMovieDetails();
        }
        super.onActivityCreated(savedInstanceState);
    }

    private void getMovieDetails() {
        Log.d(LOG_TAG, "Get Movie Details, Trailer & Ratings");
        // If we don't have a connection, show message to user.
        if (Utility.isOnline() && Utility.isNetworkAvailable(getActivity())) {
            Intent intent = new Intent(getActivity(), MovieDataService.class);
            intent.setData(mUri);
            getActivity().startService(intent);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
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

    private void reloadCursorData() {
        getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            setTextViews(data);
            setTrailersListView(data.getString(COL_MOVIE_TRAILERS));
            setReviewsListView(data.getString(COL_MOVIE_REVIEWS));
            loadPosterIntoImageView(data.getString(COL_MOVIE_POSTER));

            mIsFavorite = (data.getString(COL_MOVIE_FAVORITE) != null);
            toggleFavoriteButton();
            addListenerOnButton();
        }
    }

    private void addListenerOnButton() {
        mFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateFavoriteStatus();
            }
        });
    }

    private void toggleFavoriteButton() {
        String iconName = (mIsFavorite) ? "btn_star_big_on" : "btn_star_big_off";
        mFavoriteButton.setImageResource(getResources().getIdentifier("android:drawable/" + iconName, null, null));
    }

    private void updateFavoriteStatus() {
        String movieId = MovieEntry.getMovieIdFromUri(mUri);
        Uri favoriteUri = MoviesContract.FavoritesEntry.buildFavoriteWithIdUri(movieId);

        if (mIsFavorite) {
            deleteMovieFavorite(movieId, favoriteUri);
        } else {
            addMovieFavorite(movieId, favoriteUri);
        }

        mIsFavorite = !mIsFavorite;
        toggleFavoriteButton();
    }

    private void deleteMovieFavorite(String movieId, Uri favoriteUri) {

        String sSelection = FavoritesEntry.COLUMN_IS_FAVORITE + " = ? ";

        int rowId = getActivity().getContentResolver().delete(
                favoriteUri,
                sSelection,
                new String[]{movieId}
        );

        Log.d(LOG_TAG, "MovieDB Detail update Complete. " + rowId + " deleted");
    }

    private void addMovieFavorite(String movieId, Uri favoriteUri) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(FavoritesEntry.COLUMN_IS_FAVORITE, movieId);

        Uri resultUri = getActivity().getContentResolver().insert(
                favoriteUri,
                contentValues
        );

        Log.d(LOG_TAG, "Favorite added:" + resultUri.toString());
    }

    /**
     * Convenience method to assign text to all our TextViews
     *
     * @param data cursor data that contains our values
     */
    private void setTextViews(Cursor data) {
        mTitleView.setText(data.getString(COL_MOVIE_TITLE));
        mSummaryView.setText(data.getString(COL_MOVIE_OVERVIEW));

        String releaseYear = data.getString(COL_MOVIE_RELEASE_DATE).substring(0, 4);
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
    }

    /**
     * Convenience method for hyperlinking a textView
     *
     * @param txtView
     * @param url
     * @param linkText
     */
    private void hyperlinkTextView(TextView txtView, String url, String linkText) {
        Spanned anchorText = Html.fromHtml("<a href=\"" + url + "\">" + linkText + "</a>");
        txtView.setText(anchorText);
        txtView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /**
     * Convenience method to setup our Trailers listView
     *
     * @param sTrailers JSON String of trailer data
     */
    private void setTrailersListView(String sTrailers) {
        if (!TextUtils.isEmpty(sTrailers)) {
            try {
                JSONObject jsonTrailers = new JSONObject(sTrailers);
                if (jsonTrailers != null) {
                    ArrayList<MovieTrailer> movieTrailers = parseTrailersJSONArray(jsonTrailers.getJSONArray(MOVIE_DB_KEY_RESULTS));
                    MovieTrailerAdaptor trailerAdaptor = new MovieTrailerAdaptor(getActivity(), movieTrailers);

                    mTrailersListView.setAdapter(trailerAdaptor);
                    mTrailersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                            MovieTrailer movieTrailer = (MovieTrailer) adapterView.getItemAtPosition(position);
                            if (movieTrailer != null) {
                                Intent videoIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.detail_youtube_url, movieTrailer.key)));
                                if (videoIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                                    startActivity(videoIntent);
                                }
                            }
                        }
                    });
                    
                    Utility.setItemHeightofListView(mTrailersListView, trailerAdaptor.getCount());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Parse the JSON string data as an array and store in typed array
     *
     * @param jsonArray
     * @return our array list of trailers
     */
    private ArrayList<MovieTrailer> parseTrailersJSONArray(JSONArray jsonArray) {
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

    /**
     * Convenience method to setup our Reviews listView
     *
     * @param sReviews JSON String of review data
     */
    private void setReviewsListView(String sReviews) {
        if (!TextUtils.isEmpty(sReviews)) {
            try {
                JSONObject jsonReviews = new JSONObject(sReviews);
                if (jsonReviews != null) {
                    ArrayList<MovieReview> movieReviews = parseReviewsJSONArray(jsonReviews.getJSONArray(MOVIE_DB_KEY_RESULTS));
                    MovieReviewAdaptor reviewAdaptor = new MovieReviewAdaptor(getActivity(), movieReviews);
                    mReviewsListView.setAdapter(reviewAdaptor);
                    Utility.setItemHeightofListView(mReviewsListView, reviewAdaptor.getCount());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Parse the JSON string data as an array and store in typed array
     *
     * @param jsonArray
     * @return our array list of reviews
     */
    private ArrayList<MovieReview> parseReviewsJSONArray(JSONArray jsonArray) {
        ArrayList<MovieReview> movieReviews = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                MovieReview movieReview = new MovieReview(jsonArray.getJSONObject(i));
                if (movieReview != null) {
                    movieReviews.add(movieReview);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }

        return movieReviews;
    }

    /**
     * Load the poster from TMDb site into our ImageView via Picasso
     *
     * @param posterData
     */
    private void loadPosterIntoImageView(String posterData) {
        //Load poster into image view using Picasso
        if (BuildConfig.DEBUG) {
            Picasso.with(getActivity()).setIndicatorsEnabled(true);
            Picasso.with(getActivity()).setLoggingEnabled(true);
        }
        String posterPath = MovieDataService.getTheMovieDBApiPosterUri(MovieDataService.MOVIE_DB_IMG_SIZE_154, posterData);
        Picasso.with(getActivity())
                .load(posterPath)
                .fit()
                .placeholder(R.drawable.progress_animation)
                .error(R.drawable.no_poster_available)
                .into(mPosterView);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onResume() {
        super.onResume();

        // Setup new local broadcast listener
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver((mMessageReceiver),
                new IntentFilter(MovieDataService.API_RESULT_DETAIL_SUCCESS)
        );
    }

    @Override
    public void onPause() {
        // Make sure we properly clean up our broadcast listener
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
