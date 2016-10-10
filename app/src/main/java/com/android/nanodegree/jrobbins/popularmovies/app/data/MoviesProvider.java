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

    private static final int MOVIES = 100;
    private static final int MOVIES_ROW_ID = 101;
    private static final int MOVIES_MOVIE_ID = 102;
    private static final int MOVIES_LIST_FILTER = 103;
    private static final int MOVIE_FAVORITES = 200;

//    static final String FILTER_FAVORITE = "favorites";
//    static final String FILTER_POPULAR = "popular";
//    static final String FILTER_TOP_RATED = "top_rated";

//    private static final SQLiteQueryBuilder sMoviesByFavoriteQueryBuilder;
//    static{
//        sMoviesByFavoriteQueryBuilder = new SQLiteQueryBuilder();
//
//        //This is an inner join which looks like
//        //movies LEFT OUTER JOIN popular_movies ON movies.movie_id = popular_movies.movie_id
//        sMoviesByFavoriteQueryBuilder.setTables(
//                MoviesContract.MovieEntry.TABLE_NAME + " LEFT OUTER JOIN " +
//                        MoviesContract.FavoriteEntry.TABLE_NAME +
//                        " ON " + MoviesContract.MovieEntry.TABLE_NAME +
//                        "." + MoviesContract.MovieEntry.COLUMN_MOVIE_ID +
//                        " = " + MoviesContract.FavoriteEntry.TABLE_NAME +
//                        "." + MoviesContract.FavoriteEntry.COLUMN_MOVIE_ID);
//    }
//
//    private static final SQLiteQueryBuilder sMovieListsByTypeQueryBuilder;
//    static{
//        sMovieListsByTypeQueryBuilder = new SQLiteQueryBuilder();
//
//        //This is an inner join which looks like
//        //movies LEFT OUTER JOIN favorites ON movies.movie_id = favorites.movie_id
//        sMovieListsByTypeQueryBuilder.setTables(
//                MoviesContract.MovieEntry.TABLE_NAME + " LEFT OUTER JOIN " +
//                        MoviesContract.MovieListsEntry.TABLE_NAME +
//                        " ON " + MoviesContract.MovieEntry.TABLE_NAME +
//                        "." + MoviesContract.MovieEntry.COLUMN_MOVIE_ID +
//                        " = " + MoviesContract.MovieListsEntry.TABLE_NAME +
//                        "." + MoviesContract.MovieListsEntry.COLUMN_MOVIE_ID);
//    }
//
//    private static final String sMovieListSelection =
//            MoviesContract.MovieEntry.TABLE_NAME+
//                    "." + MoviesContract.MovieListsEntry.COLUMN_LIST_ID + " = ? ";
//
//
//    private static final SQLiteQueryBuilder sMovieDetailQueryBuilder;
//    static{
//        sMovieDetailQueryBuilder = new SQLiteQueryBuilder();
//
//        //This is an inner join which looks like
//        //movies LEFT OUTER JOIN favorites ON movies.movie_id = favorites.movie_id
//        //       LEFT OUTER JOIN movie_reviews ON movies.movie_id = movie_reviews.movie_id
//        //       LEFT OUTER JOIN movie_trailers ON movies.movie_id = movie_trailers.movie_id
//        sMovieDetailQueryBuilder.setTables(
//                MoviesContract.MovieEntry.TABLE_NAME + " LEFT OUTER JOIN " +
//                        MoviesContract.FavoriteEntry.TABLE_NAME +
//                        " ON " + MoviesContract.MovieEntry.TABLE_NAME +
//                        "." + MoviesContract.MovieEntry.COLUMN_MOVIE_ID +
//                        " = " + MoviesContract.FavoriteEntry.TABLE_NAME +
//                        "." + MoviesContract.FavoriteEntry.COLUMN_MOVIE_ID +
//                        " LEFT OUTER JOIN " +
//                        MoviesContract.MovieDetailsEntry.TABLE_NAME +
//                        " ON " + MoviesContract.MovieEntry.TABLE_NAME +
//                        "." + MoviesContract.MovieEntry.COLUMN_MOVIE_ID +
//                        " = " + MoviesContract.MovieDetailsEntry.TABLE_NAME +
//                        "." + MoviesContract.MovieDetailsEntry.COLUMN_MOVIE_ID);
//    }
//
//    private static final String sMovieDetailSelection =
//            MoviesContract.MovieEntry.TABLE_NAME+
//                    "." + MoviesContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ";

    private static final SQLiteQueryBuilder sMovieQueryBuilder;
        static {
            sMovieQueryBuilder = new SQLiteQueryBuilder();
            sMovieQueryBuilder.setTables(
                MoviesContract.MovieEntry.TABLE_NAME );
    }

    private static final String sMovieSelection =
            MoviesContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ";

//    private static final String sMoviesListTypeSelection =
//            MoviesContract.MovieEntry.COLUMN_LIST_TYPE + " = ? ";

    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoviesContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, MoviesContract.PATH_MOVIES, MOVIES);
        matcher.addURI(authority, MoviesContract.PATH_MOVIES + "/#", MOVIES_ROW_ID);
        matcher.addURI(authority, MoviesContract.PATH_MOVIES + "/" + MoviesContract.PATH_MOVIES_DETAIL + "/*", MOVIES_MOVIE_ID);
        matcher.addURI(authority, MoviesContract.PATH_MOVIES + "/" + MoviesContract.PATH_MOVIES_LIST + "/*", MOVIES_LIST_FILTER);
        matcher.addURI(authority, MoviesContract.PATH_MOVIES + "/" + MoviesContract.PATH_MOVIES_FAVORITES, MOVIE_FAVORITES);
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
            case MOVIES_ROW_ID:
                return MoviesContract.MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIES:
            case MOVIES_LIST_FILTER:
            case MOVIE_FAVORITES:
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
            // "movies/#"
            case MOVIES_ROW_ID:
            {
                retCursor = getMovieByRowId(uri, projection, sortOrder);
                break;
            }
            // "movies/list/*"
            case MOVIES_LIST_FILTER: {

                retCursor = getMoviesByFilter(uri, projection, sortOrder);
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

    private Cursor getMoviesByFilter(Uri uri, String[] projection, String sortOrder) {
        String listIdFromUri = MoviesContract.MovieEntry.getListTypeFromUri(uri);
        Log.d(LOG_TAG,listIdFromUri);
        SQLiteQueryBuilder sqLiteQueryBuilder = sMovieQueryBuilder;
//        String sqlSelection = null;
//        String[] sqlFilters = new String[]{};

//        switch (listIdFromUri)
//        {
//            case FILTER_FAVORITE:
//                sqLiteQueryBuilder = sMoviesByFavoriteQueryBuilder;
//                break;
//            case FILTER_POPULAR:
//            case FILTER_TOP_RATED:
//            default:
//                sqLiteQueryBuilder = sMovieListsByTypeQueryBuilder;
//                sqlSelection = sMovieListSelection;
//                sqlFilters = new String[]{listIdFromUri};
//                break;
//        }

        return sqLiteQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                null,
                null,
                null,
                null,
                null
        );
    }

    private Cursor getMovieByMovieId(Uri uri, String[] projection, String sortOrder) {
        String movie_id = MoviesContract.MovieEntry.getMovieIdFromUri(uri);

        return sMovieQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sMovieSelection,
                new String[]{movie_id},
                null,
                null,
                sortOrder
        );
    }

    private Cursor getMovieByRowId(Uri uri, String[] projection, String sortOrder) {
        long _id = MoviesContract.MovieEntry.getRowIdFromUri(uri);

        return sMovieQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sMovieSelection,
                new String[]{Long.toString(_id)},
                null,
                null,
                sortOrder
        );
    }

//    private Cursor getMoviesByListType(Uri uri, String[] projection, String sortOrder) {
//        String list_type = MoviesContract.MovieEntry.getListTypeFromUri(uri);
//
//        return sMovieQueryBuilder.query(mOpenHelper.getReadableDatabase(),
//                projection,
//                sMovieSelection,
//                new String[]{list_type},
//                null,
//                null,
//                sortOrder
//        );
//    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri = null;

        switch (match) {
            case MOVIES: {
                long insertId = insertOrUpdateById(
                        db,
                        uri,
                        MoviesContract.MovieEntry.TABLE_NAME,
                        values,
                        MoviesContract.MovieEntry.COLUMN_MOVIE_ID);
                if(insertId > 0) {
                    returnUri = MoviesContract.MovieEntry.buildMovieWithMovieIdUri(values.getAsString(MoviesContract.MovieEntry.COLUMN_MOVIE_ID));
                }
                break;
            }
//            case FAVORITE: {
//                long _id = db.insert(MoviesContract.FavoriteEntry.TABLE_NAME, null, values);
//                if ( _id > 0 )
//                    returnUri = MoviesContract.FavoriteEntry.buildFavoriteUri(_id);
//                else
//                    throw new android.database.SQLException("Failed to insert row into " + uri);
//                break;
//            }
//            case MOVIES_LIST_BY_TYPE: {
//                long _id = db.insert(MoviesContract.MovieListsEntry.TABLE_NAME, null, values);
//                if ( _id > 0 )
//                    returnUri = null; //MoviesContract.MovieTrailerEntry.buildTrailerUri(_id);
//                else
//                    throw new android.database.SQLException("Failed to insert row into " + uri);
//                break;
//            }
//            case MOVIE_DETAIL: {
//                long _id = db.insert(MoviesContract.MovieDetailsEntry.TABLE_NAME, null, values);
//                if ( _id > 0 )
//                    returnUri = null; //MoviesContract.MovieReviewEntry.buildReviewUri(_id);
//                else
//                    throw new android.database.SQLException("Failed to insert row into " + uri);
//                break;
//            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    /**
     * In case of a conflict when inserting the values, another update query is sent.
     * http://stackoverflow.com/questions/23417476/use-insert-or-replace-in-contentprovider
     *
     * @param db     Database to insert to.
     * @param uri    Content provider uri.
     * @param table  Table to insert to.
     * @param values The values to insert to.
     * @param column Column to identify the object.
     * @throws android.database.SQLException
     */
    private long insertOrUpdateById(SQLiteDatabase db, Uri uri, String table,
                                    ContentValues values, String column) throws SQLException {
        long returnId;
        try {
            Log.d(LOG_TAG, "inserting:" + values.toString());
            returnId = db.insertOrThrow(table, null, values);
        } catch (SQLiteConstraintException e) {
            Log.d(LOG_TAG, "updating:" + values.toString());
            returnId = update(uri, values, column + "=?",
                    new String[]{values.getAsString(column)});
            if (returnId == 0)
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
        if ( null == selection ) selection = "1";
        switch (match) {
            case MOVIES:
                rowsDeleted = db.delete(
                        MoviesContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
//            case FAVORITE:
//                rowsDeleted = db.delete(
//                        MoviesContract.FavoriteEntry.TABLE_NAME, selection, selectionArgs);
//                break;
//            case MOVIES_LIST_BY_TYPE:
//                rowsDeleted = db.delete(
//                        MoviesContract.MovieListsEntry.TABLE_NAME, selection, selectionArgs);
//                break;
//            case MOVIE_DETAIL:
//                rowsDeleted = db.delete(
//                        MoviesContract.MovieDetailsEntry.TABLE_NAME, selection, selectionArgs);
//                break;
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
            case MOVIES:
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

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIES:
                return doBulkInsert(MoviesContract.MovieEntry.TABLE_NAME, uri, values);
//            case MOVIES_LIST_BY_TYPE:
//                return doBulkInsert(MoviesContract.MovieListsEntry.TABLE_NAME, uri, values);
//            case MOVIE_DETAIL:
//                return doBulkInsert(MoviesContract.MovieDetailsEntry.TABLE_NAME, uri, values);
            default:
                return super.bulkInsert(uri, values);
        }
    }

    private int doBulkInsert(String tableName, Uri uri, ContentValues[] values)
    {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int returnCount = 0;

        db.beginTransaction();
        try {
            for (ContentValues value : values) {
                long _id = insertOrUpdateById(
                        db,
                        uri,
                        tableName,
                        value,
                        MoviesContract.MovieEntry.COLUMN_MOVIE_ID);
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

    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
