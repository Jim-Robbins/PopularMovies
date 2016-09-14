package com.android.nanodegree.jrobbins.popularmovies.app;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by jim.robbins on 9/8/16.
 *
 * Adapted from udacity/android-custom-arrayadapter
 * https://github.com/udacity/android-custom-arrayadapter/blob/master/app/src/main/java/demo/example/com/customarrayadapter/AndroidFlavorAdapter.java
 */

public class MovieAdapter extends ArrayAdapter<Movie> {
    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();
    private Activity mContext;
    /**
     * Custom constructor (it doesn't mirror a superclass constructor).
     * The context is used to inflate the layout file, and the List is the data we want
     * to populate into the lists
     *
     * @param context        The current context. Used to inflate the layout file.
     * @param movies       A List of Movie objects to display in a list
     */
    public MovieAdapter(Activity context, List<Movie> movies) {
        // Here, we initialize the ArrayAdapter's internal storage for the context and the list.
        // Because this is a custom adapter for an ImageView, the adapter is not
        // going to use the second argument, so it can be any value.
        super(context, 0, movies);
        mContext = context;
    }

    /**
     * Provides a view for an AdapterView (ListView, GridView, etc.)
     *
     * @param position    The AdapterView position that is requesting a view
     * @param convertView The recycled view to populate.
     *                    (search online for "android view recycling" to learn more)
     * @param parent The parent ViewGroup that is used for inflation.
     * @return The View for the position in the AdapterView.
     */
    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Gets the Movie object from the ArrayAdapter at the appropriate position
        Movie movie = getItem(position);

        if (movie != null) {
            // Adapters recycle views to AdapterViews.
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item_movie, parent, false);
            }

            ImageView posterView = (ImageView) convertView.findViewById(R.id.grid_item_movie_poster);
            String posterPath = MovieDataParser.getMoviePosterUrl(MovieDataParser.MOVIE_DB_IMG_SIZE_185, movie.getPosterPathStr());

            //Use Picasso to load in the movie poster into the imageView
            if (BuildConfig.DEBUG) {
                Picasso.with(mContext).setIndicatorsEnabled(true);
                Picasso.with(mContext).setLoggingEnabled(true);
            }
            Picasso.with(mContext)
                    .load(posterPath)
                    .fit()
                    .placeholder(R.drawable.progress_animation)
                    .error(R.drawable.no_poster_available)
                    .into(posterView);
        }

        return convertView;
    }
}
