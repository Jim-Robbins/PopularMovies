package com.android.nanodegree.jrobbins.popularmovies.app.views.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.nanodegree.jrobbins.popularmovies.app.R;
import com.android.nanodegree.jrobbins.popularmovies.app.models.MovieReview;

import java.util.List;

/**
 * Custom Array Cursor for displaying Movie Reviews
 */

public class MovieReviewAdaptor extends ArrayAdapter<MovieReview> {

    private static final String LOG_TAG = MovieReviewAdaptor.class.getSimpleName();

    public MovieReviewAdaptor(Context context, List<MovieReview> movieReviews) {
        super(context, 0, movieReviews);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MovieReview movieReview = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_movie_review, parent, false);
        }

        TextView authorTextView = (TextView) convertView.findViewById(R.id.list_item_review_author);
        authorTextView.setText(movieReview.author);

        TextView contentTextView = (TextView) convertView.findViewById(R.id.list_item_review_content);
        contentTextView.setText(movieReview.content);

        return convertView;
    }
}
