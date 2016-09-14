package com.android.nanodegree.jrobbins.popularmovies.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Display the movie detail information
 * Created by jim.robbins on 9/8/16.
 */
public class DetailFragment extends Fragment {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final String MOVIE_LIST_INTENT = "movieVOIntent";

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate the view from the fragment
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);


        //the detail Activity called via intent. Inspect the intent for parcelable movie data
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(MOVIE_LIST_INTENT)) {
            Movie movie = intent.getParcelableExtra(MOVIE_LIST_INTENT);

            if (movie != null) {
                final TextView titleTxt = (TextView) rootView.findViewById(R.id.textview_movie_title);
                titleTxt.setText(movie.getTitle());
                /* Something I am playing with for Stage 2
                String backdropPath = MovieDataParser.getMoviePosterUrl(MovieDataParser.MOVIE_DB_IMG_SIZE_342, movie.getBackdropPath());
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

                ((TextView) rootView.findViewById(R.id.textview_movie_rating)).setText(getString(R.string.detail_movie_user_rating, movie.getMovieRating()));
                ((TextView) rootView.findViewById(R.id.textview_movie_summary)).setText(movie.getOverview());
                ((TextView) rootView.findViewById(R.id.textview_movie_year)).setText(movie.getReleaseYear());

                ImageView posterView = (ImageView) rootView.findViewById(R.id.imageview_movie_poster);
                //Load poster into image view using Picasso
                if (BuildConfig.DEBUG) {
                    Picasso.with(getActivity()).setIndicatorsEnabled(true);
                    Picasso.with(getActivity()).setLoggingEnabled(true);
                }
                String posterPath = MovieDataParser.getMoviePosterUrl(MovieDataParser.MOVIE_DB_IMG_SIZE_154, movie.getPosterPathStr());
                Picasso.with(getActivity())
                        .load(posterPath)
                        .fit()
                        .placeholder(R.drawable.progress_animation)
                        .error(R.drawable.no_poster_available)
                        .into(posterView);

            }
        }
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.detailfragment, menu);
    }
}
