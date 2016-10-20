package com.android.nanodegree.jrobbins.popularmovies.app.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

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
    public static final String PATH_MOVIES           = "movies";
    public static final String PATH_MOVIES_DETAIL    = "detail";
    public static final String PATH_MOVIES_LIST      = "list";
    public static final String PATH_MOVIES_FAVORITES = "favorites";

    /* Inner class that defines the table contents of the favorites table */
    public static final class FavoritesEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES_FAVORITES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES_FAVORITES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES_FAVORITES;

        // Table name
        public static final String TABLE_NAME = "favorite_movies";

        // The movie id that user selects as a favorite
        public static final String COLUMN_IS_FAVORITE = "is_favorite";

        /**
         * Build uri to access record in favorites table
         * @param movie_id
         * @return Uri
         */
        public static Uri buildFavoriteWithIdUri(String movie_id) {
            return CONTENT_URI.buildUpon().appendPath(movie_id).build();
        }

        public static Uri buildFavoritesUri() {
            return CONTENT_URI;
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

        // Movie id as returned by API, to identify the posters, ratings and videos to load
        public static final String COLUMN_MOVIE_ID = "movie_id";

        // Movie Details
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_POSTER = "poster";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_VOTE_AVG= "vote_average";
        public static final String COLUMN_BACKDROP_PATH = "backdrop_path";
        public static final String COLUMN_CREATE_DATE = "create_date";
        public static final String COLUMN_HOMEPAGE = "homepage";
        public static final String COLUMN_IMDB_ID = "imdb_id";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_PRODUCTION_COMPANIES = "production_companies";
        public static final String COLUMN_RUNTIME = "runtime";
        public static final String COLUMN_VOTE_COUNT = "vote_count";
        public static final String COLUMN_GENRES = "genres";
        public static final String COLUMN_TRAILERS = "trailers";
        public static final String COLUMN_REVIEWS = "reviews";

        /**
         * Build uri to access movie by id
         * @param movie_id String value
         * @return Uri
         */
        public static Uri buildMovieWithMovieIdUri(String movie_id) {
            return CONTENT_URI.buildUpon().appendPath(PATH_MOVIES_DETAIL).appendPath(movie_id).build();
        }

        public static Uri buildMoviesWithListTypeUri(String listType) {
            return CONTENT_URI.buildUpon().appendPath(PATH_MOVIES_LIST).appendPath(listType).build();
        }

        public static String getMovieIdFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }
        public static String getListTypeFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }

    /* Inner class that defines the table contents of the popular filtered table */
    public static final class MovieListsEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                MovieEntry.CONTENT_URI.buildUpon().appendPath(PATH_MOVIES_FAVORITES).build();

        // Table name
        public static final String TABLE_NAME = "movie_lists";

        // The movie id that user selects as a favorite
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_LIST_ID = "list_id";

    }
}
