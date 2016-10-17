package com.android.nanodegree.jrobbins.popularmovies.app.views.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.nanodegree.jrobbins.popularmovies.app.R;
import com.android.nanodegree.jrobbins.popularmovies.app.models.MovieTrailer;

import java.util.List;

import static android.R.attr.resource;

/**
 * Created by jim on 10/17/16.
 */

public class MovieTrailerAdaptor extends ArrayAdapter<MovieTrailer> {

    private static final String LOG_TAG = MovieTrailerAdaptor.class.getSimpleName();

    public MovieTrailerAdaptor(Context context, List<MovieTrailer> movieTrailers) {
        super(context, 0, movieTrailers);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MovieTrailer movieTrailer = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_movie_trailer, parent, false);
        }

        ImageView iconView = (ImageView) convertView.findViewById(R.id.list_item_trailer_icon);
        iconView.setImageResource(R.drawable.trailer);

        TextView trailerName = (TextView) convertView.findViewById(R.id.list_item_trailer_name);
        trailerName.setText(movieTrailer.name);

        return super.getView(position, convertView, parent);
    }
}
