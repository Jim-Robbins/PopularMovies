package com.android.nanodegree.jrobbins.popularmovies.app.views.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.nanodegree.jrobbins.popularmovies.app.BuildConfig;
import com.android.nanodegree.jrobbins.popularmovies.app.R;
import com.android.nanodegree.jrobbins.popularmovies.app.fragments.MovieFragment;
import com.android.nanodegree.jrobbins.popularmovies.app.services.MovieDataService;
import com.squareup.picasso.Picasso;

/**
 * Custom cursor adapter for displaying the movie posters grid list
 */
public class MovieCursorAdapter extends CursorAdapter {
    private static final String LOG_TAG = MovieCursorAdapter.class.getSimpleName();

    public MovieCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_item_movie, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // Get movie poster
        String posterPath = MovieDataService.getTheMovieDBApiPosterUri(MovieDataService.MOVIE_DB_IMG_SIZE_185, cursor.getString(MovieFragment.COL_MOVIE_POSTER));

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
                .into(viewHolder.posterView);
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final ImageView posterView;

        public ViewHolder(View view) {
            posterView = (ImageView) view.findViewById(R.id.grid_item_movie_poster);
        }
    }
}
