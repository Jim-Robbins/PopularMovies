package com.android.nanodegree.jrobbins.popularmovies.app.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import static android.R.attr.id;

/**
 * Created by jim.robbins on 9/29/16.
 */

public class MoviesContract {
    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.android.nanodegree.jrobbins.popularmovies.app";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.android.nanodegree.jrobbins.popularmovies.app/movies/ is a valid path for
    // looking at movie data. content://com.android.nanodegree.jrobbins.popularmovies.app/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    public static final String PATH_MOVIES = "movies";
    public static final String PATH_FAVORITE = "favorite";
    public static final String PATH_FILTER = "filter";

    /* Inner class that defines the table contents of the favorites table */
    public static final class FavoriteEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITE;

        // Table name
        public static final String TABLE_NAME = "favorite_movies";

        // The movie id that user selects as a favorite
        public static final String COLUMN_MOVIE_ID = "movie_id";

        /**
         * Build uri to access favorites table
         * @param id movie_id
         * @return Uri
         */
        public static Uri buildFavoriteUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /* Inner class that defines the table contents of the movies table */
    public static final class MovieEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        public static final String TABLE_NAME = "movies";

        // Column with the foreign key into the favorites table.
        public static final String COLUMN_LOC_KEY = "favorite_id";

        // Movie id as returned by API, to identify the posters, ratings and videos to load
        public static final String COLUMN_MOVIE_ID = "movie_id";

        // Movie Details
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_POSTER = "poster";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_VOTE_AVG= "vote_average";
        public static final String COLUMN_BACKDROP_PATH = "backdrop_path";
        public static final String COLUMN_GENRE_IDS = "genre_ids";
        public static final String COLUMN_VIDEO = "video_id";

        /**
         * Build uri to access movie by id
         * @param id movie_id
         * @return Uri
         */
        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        /**
         * Build uri to access movies list by filter
         * @param filter the filter we want to use to build our list by
         * @return Uri
         */
        public static Uri buildMovieFilter(String filter) {
            return CONTENT_URI.buildUpon().appendPath(PATH_FILTER).appendPath(filter).build();
        }

        public static String getFilterSettingFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static String getIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    /* Inner class that defines the table contents of the popular filtered table */
    public static final class PopularMovieEntry implements BaseColumns {

        // Table name
        public static final String TABLE_NAME = "popular_movies";

        // The movie id that user selects as a favorite
        public static final String COLUMN_MOVIE_ID = "movie_id";
    }

    /* Inner class that defines the table contents of the top rated filtered table */
    public static final class TopRatedMovieEntry implements BaseColumns {

        // Table name
        public static final String TABLE_NAME = "top_rated_movies";

        // The movie id that user selects as a favorite
        public static final String COLUMN_MOVIE_ID = "movie_id";
    }

    /* Inner class that defines the table contents of the movie trailers table */
    public static final class MovieTrailerEntry implements BaseColumns {

        // Table name
        public static final String TABLE_NAME = "movie_trailers";

        // The movie id that user selects as a favorite
        public static final String COLUMN_MOVIE_ID = "movie_id";
    }

    /* Inner class that defines the table contents of the movie reviews table */
    public static final class MovieReviewEntry implements BaseColumns {

        // Table name
        public static final String TABLE_NAME = "movie_reviews";

        // The movie id that user selects as a favorite
        public static final String COLUMN_MOVIE_ID = "movie_id";
    }
}
