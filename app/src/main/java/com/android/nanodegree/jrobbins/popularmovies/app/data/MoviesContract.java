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
    public static final String PATH_MOVIES           = "movies";
    public static final String PATH_MOVIES_DETAIL    = "detail";
    public static final String PATH_MOVIES_LIST      = "list";
    public static final String PATH_MOVIES_FAVORITES = "favorites";

    /* Inner class that defines the table contents of the favorites table */
//    public static final class FavoriteEntry implements BaseColumns {
//
//        public static final Uri CONTENT_URI =
//                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITE).build();
//
//        public static final String CONTENT_TYPE =
//                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITE;
//        public static final String CONTENT_ITEM_TYPE =
//                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITE;
//
//        // Table name
//        public static final String TABLE_NAME = "favorite_movies";
//
//        // The movie id that user selects as a favorite
//        public static final String COLUMN_MOVIE_ID = "movie_id";
//
//        /**
//         * Build uri to access record in favorites table
//         * @param id movie_id
//         * @return Uri
//         */
//        public static Uri buildFavoriteUri(long id) {
//            return ContentUris.withAppendedId(CONTENT_URI, id);
//        }
//    }

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
        public static final String COLUMN_GENRE_IDS = "genre_ids";
        public static final String COLUMN_HAS_VIDEO = "has_video";
        public static final String COLUMN_CREATE_DATE = "create_date";

        /**
         * Build uri to access movie by id
         * @param movie_id String value
         * @return Uri
         */
        public static Uri buildMovieWithMovieIdUri(String movie_id) {
            return CONTENT_URI.buildUpon().appendPath(PATH_MOVIES_DETAIL).appendPath(movie_id).build();
        }

        public static Uri buildMovieWithRowIdUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMoviesWithListTypeUri(String listType) {
            return CONTENT_URI.buildUpon().appendPath(PATH_MOVIES_LIST).appendPath(listType).build();
        }

        public static Uri buildMoviesUri() {
            return CONTENT_URI;
        }

        public static long getRowIdFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }

        public static String getMovieIdFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }
        public static String getListTypeFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }

    /* Inner class that defines the table contents of the popular filtered table */
//    public static final class MovieListsEntry implements BaseColumns {
//        public static final Uri CONTENT_URI =
//                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE_LIST).build();
//
//        public static final String CONTENT_TYPE =
//                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE_LIST;
//        public static final String CONTENT_ITEM_TYPE =
//                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE_LIST;
//
//        // Table name
//        public static final String TABLE_NAME = "movie_lists";
//
//        // The movie id that user selects as a favorite
//        public static final String COLUMN_MOVIE_ID = "movie_id";
//        public static final String COLUMN_LIST_ID = "list_id";
//
//        /**
//         * Build uri to access movies list by filter
//         * @param filter the filter we want to use to build our list by
//         * @return Uri
//         */
//        public static Uri buildMovieList(String filter) {
//            return CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).appendPath(filter).build();
//        }
//
//        public static String getListIdFromUri(Uri uri) {
//            return uri.getPathSegments().get(2);
//        }
//    }
//
//    /* Inner class that defines the table contents of the movie reviews & trailers table */
//    public static final class MovieDetailsEntry implements BaseColumns {
//
//        public static final Uri CONTENT_URI =
//                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE_DETAIL).build();
//
//        public static final String CONTENT_TYPE =
//                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE_DETAIL;
//        public static final String CONTENT_ITEM_TYPE =
//                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE_DETAIL;
//
//        // Table name
//        public static final String TABLE_NAME = "movie_details";
//
//        // The movie id that user selects as a favorite
//        public static final String COLUMN_MOVIE_ID = "movie_id";
//        public static final String COLUMN_DETAIL_TYPE = "detail_type";
//        public static final String COLUMN_DETAIL = "detail";
//
//        /**
//         * Build uri to access movies list by filter
//         * @param filter the filter we want to use to build our list by
//         * @return Uri
//         */
//        public static Uri buildMovieDetail(String filter) {
//            return CONTENT_URI.buildUpon().appendPath(PATH_MOVIE_DETAIL).appendPath(filter).build();
//        }
//    }
}
