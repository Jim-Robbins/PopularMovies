package com.android.nanodegree.jrobbins.popularmovies.app.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * Created by jim.robbins on 9/29/16.
 */

public class MoviesProvider extends ContentProvider {
    private final String LOG_TAG = MoviesProvider.class.getSimpleName();
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoviesDbHelper mOpenHelper;

    public static final int MOVIES = 100;
    public static final int MOVIES_ROW_ID = 101;
    public static final int MOVIES_MOVIE_ID = 102;
    public static final int MOVIES_LIST_FILTER = 103;
    public static final int MOVIE_FAVORITES_LIST = 200;
    public static final int MOVIE_FAVORITE_ID = 201;

    /**
     * Table definition for getting our movie details, LEFT OUTER JOIN favorites
     */
    private static final SQLiteQueryBuilder sMovieDetailQueryBuilder;

    static {
        sMovieDetailQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //movies LEFT OUTER JOIN favorite_movies ON movies.movie_id = favorite_movies.movie_id
        sMovieDetailQueryBuilder.setTables(
                MoviesContract.MovieEntry.TABLE_NAME + " LEFT OUTER JOIN " +
                        MoviesContract.FavoritesEntry.TABLE_NAME +
                        " ON " + MoviesContract.MovieEntry.TABLE_NAME +
                        "." + MoviesContract.MovieEntry.COLUMN_MOVIE_ID +
                        " = " + MoviesContract.FavoritesEntry.TABLE_NAME +
                        "." + MoviesContract.FavoritesEntry.COLUMN_IS_FAVORITE);
    }

    /**
     * This selection argument is for selecting a specific movie from the movies_list
     */
    private static final String sMovieDetailSelection =
            MoviesContract.MovieEntry.TABLE_NAME +
                    "." + MoviesContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ";

    /**
     * This selection argument is for selecting all the movies listed in our favorite_movies table
     */
    private static final String sMovieFavoritesSelection =
            MoviesContract.FavoritesEntry.TABLE_NAME +
                    "." + MoviesContract.FavoritesEntry.COLUMN_IS_FAVORITE + " IS NOT NULL ";

    /**
     * This definition is for linking our movies table via LEFT OUTER JOIN to our movie_lists
     */
    private static final SQLiteQueryBuilder sMovieListsByTypeQueryBuilder;

    static {
        sMovieListsByTypeQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //movies LEFT OUTER JOIN movies_list ON movies.movie_id = movies_list.movie_id
        sMovieListsByTypeQueryBuilder.setTables(
                MoviesContract.MovieEntry.TABLE_NAME + " LEFT OUTER JOIN " +
                        MoviesContract.MovieListsEntry.TABLE_NAME +
                        " ON " + MoviesContract.MovieEntry.TABLE_NAME +
                        "." + MoviesContract.MovieEntry.COLUMN_MOVIE_ID +
                        " = " + MoviesContract.MovieListsEntry.TABLE_NAME +
                        "." + MoviesContract.MovieListsEntry.COLUMN_MOVIE_ID);
    }

    /**
     * This selection argument allows us to filter our movie list buy list type
     */
    private static final String sMovieListByTypeSelection =
            MoviesContract.MovieListsEntry.TABLE_NAME +
                    "." + MoviesContract.MovieListsEntry.COLUMN_LIST_ID + " = ? ";

    /**
     * Check the incoming URIs to make sure they match valid calls
     *
     * @return
     */
    static UriMatcher buildUriMatcher() {
        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoviesContract.CONTENT_AUTHORITY;

        // For each type of URI create a corresponding code.
        matcher.addURI(authority, MoviesContract.PATH_MOVIES, MOVIES);
        matcher.addURI(authority, MoviesContract.PATH_MOVIES + "/#", MOVIES_ROW_ID);
        matcher.addURI(authority, MoviesContract.PATH_MOVIES + "/" + MoviesContract.PATH_MOVIES_DETAIL + "/*", MOVIES_MOVIE_ID);
        matcher.addURI(authority, MoviesContract.PATH_MOVIES + "/" + MoviesContract.PATH_MOVIES_LIST + "/*", MOVIES_LIST_FILTER);
        matcher.addURI(authority, MoviesContract.PATH_MOVIES_FAVORITES + "/*", MOVIE_FAVORITE_ID);
        matcher.addURI(authority, MoviesContract.PATH_MOVIES_FAVORITES, MOVIE_FAVORITES_LIST);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MoviesDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIES_MOVIE_ID:
                return MoviesContract.MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIES_ROW_ID:
                return MoviesContract.MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIES:
                return MoviesContract.MovieEntry.CONTENT_TYPE;
            case MOVIES_LIST_FILTER:
                return MoviesContract.MovieEntry.CONTENT_TYPE;
            case MOVIE_FAVORITE_ID:
                return MoviesContract.MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIE_FAVORITES_LIST:
                return MoviesContract.MovieEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "movie/detail/*"
            case MOVIES_MOVIE_ID:
                retCursor = getMovieByMovieId(uri, projection, sortOrder);
                break;
            // "movies/list/*"
            case MOVIES_LIST_FILTER: {
                retCursor = getMoviesByFilter(uri, projection, sortOrder);
                break;
            }
            // "favorites/"
            case MOVIE_FAVORITES_LIST: {
                retCursor = getMovieFavorites(projection, sortOrder);
                break;
            }
            // "movies"
            case MOVIES: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.MovieEntry.TABLE_NAME,
                        projection,
                        null,
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /**
     * Load list of movies from db filtering by movie list type
     */
    private Cursor getMoviesByFilter(Uri uri, String[] projection, String sortOrder) {
        String listIdFromUri = MoviesContract.MovieEntry.getListTypeFromUri(uri);
        Log.d(LOG_TAG, listIdFromUri);

        Cursor cursor = sMovieListsByTypeQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sMovieListByTypeSelection,
                new String[]{listIdFromUri},
                null,
                null,
                sortOrder
        );
        return cursor;
    }

    /**
     * Load specific movie record to display details
     */
    private Cursor getMovieByMovieId(Uri uri, String[] projection, String sortOrder) {
        String movie_id = MoviesContract.MovieEntry.getMovieIdFromUri(uri);

        return sMovieDetailQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sMovieDetailSelection,
                new String[]{movie_id},
                null,
                null,
                sortOrder
        );
    }

    /**
     * Load list of movies marked as favorites, based on LEFT OUTER JOIN where is_favorite is not null
     */
    private Cursor getMovieFavorites(String[] projection, String sortOrder) {

        return sMovieDetailQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sMovieFavoritesSelection,
                null,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri = null;
        long insertId;

        switch (match) {
            case MOVIES: {
                insertId = insertOrReplace(
                        db,
                        uri,
                        values);
                if (insertId > 0) {
                    returnUri = MoviesContract.MovieEntry.buildMovieWithMovieIdUri(values.getAsString(MoviesContract.MovieEntry.COLUMN_MOVIE_ID));
                }
                break;
            }
            case MOVIE_FAVORITE_ID: {
                long _id = db.insert(MoviesContract.FavoritesEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MoviesContract.FavoritesEntry.buildFavoriteWithIdUri(values.getAsString(MoviesContract.FavoritesEntry.COLUMN_IS_FAVORITE));
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case MOVIES_LIST_FILTER: {
                insertId = insertOrReplace(
                        db,
                        uri,
                        values);
                if (insertId > 0) {
                    returnUri = MoviesContract.MovieEntry.buildMovieWithMovieIdUri(values.getAsString(MoviesContract.MovieEntry.COLUMN_MOVIE_ID));
                    String movieId = values.getAsString(MoviesContract.MovieEntry.COLUMN_MOVIE_ID);
                    String listType = MoviesContract.MovieEntry.getListTypeFromUri(uri);
                    insertListType(db, movieId, listType);
                }
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIES_LIST_FILTER:
                return doBulkInsert(uri, values);
            default:
                return super.bulkInsert(uri, values);
        }
    }

    private int doBulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int returnCount = 0;

        db.beginTransaction();
        try {
            for (ContentValues value : values) {
                long _id = insertOrReplace(
                        db,
                        uri,
                        value);
                if (_id != -1) {
                    returnCount++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnCount;
    }

    /**
     * In case of a conflict when inserting the values, another update query is sent.
     * http://stackoverflow.com/questions/23417476/use-insert-or-replace-in-contentprovider
     *
     * @param db     Database to insert to.
     * @param uri    Content provider uri.
     * @param values The values to insert to.
     * @throws android.database.SQLException
     */
    private long insertOrReplace(SQLiteDatabase db, Uri uri, ContentValues values) throws SQLException {
        long returnId = -1;
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIES:
                try {
                    returnId = db.replaceOrThrow(MoviesContract.MovieEntry.TABLE_NAME, null, values);
                } catch (SQLiteConstraintException e) {
                    throw e;
                }
                break;
            case MOVIES_LIST_FILTER:
                try {
                    returnId = db.replaceOrThrow(MoviesContract.MovieEntry.TABLE_NAME, null, values);
                } catch (SQLiteConstraintException e) {
                    throw e;
                }
                String movieId = values.getAsString(MoviesContract.MovieEntry.COLUMN_MOVIE_ID);
                String listType = MoviesContract.MovieEntry.getListTypeFromUri(uri);
                insertListType(db, movieId, listType);
                break;
            case MOVIE_FAVORITE_ID:
                try {
                    returnId = db.replaceOrThrow(MoviesContract.FavoritesEntry.TABLE_NAME, null, values);
                } catch (SQLiteConstraintException e) {
                    throw e;
                }
                break;
        }

        return returnId;
    }

    private long insertListType(SQLiteDatabase db, String movieId, String listType) {
        long returnId;

        ContentValues values = new ContentValues();

        values.put(MoviesContract.MovieListsEntry.COLUMN_MOVIE_ID, movieId);
        values.put(MoviesContract.MovieListsEntry.COLUMN_LIST_ID, listType);

        try {
            returnId = db.replaceOrThrow(MoviesContract.MovieListsEntry.TABLE_NAME, null, values);
        } catch (SQLiteConstraintException e) {
            throw e;
        }

        return returnId;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";
        switch (match) {
            case MOVIES:
                rowsDeleted = db.delete(
                        MoviesContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOVIE_FAVORITE_ID:
                rowsDeleted = db.delete(
                        MoviesContract.FavoritesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOVIES_LIST_FILTER:
                rowsDeleted = db.delete(
                        MoviesContract.MovieListsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOVIE_FAVORITES_LIST:
                rowsDeleted = db.delete(
                        MoviesContract.FavoritesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOVIES_LIST_FILTER:
                rowsUpdated = db.update(MoviesContract.MovieEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case MOVIES_MOVIE_ID:
                rowsUpdated = db.update(MoviesContract.MovieEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
