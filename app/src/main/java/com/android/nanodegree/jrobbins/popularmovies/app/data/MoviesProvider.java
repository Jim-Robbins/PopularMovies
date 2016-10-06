package com.android.nanodegree.jrobbins.popularmovies.app.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by jim.robbins on 9/29/16.
 */

public class MoviesProvider extends ContentProvider {
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoviesDbHelper mOpenHelper;

    static final int MOVIE = 10;
    static final int MOVIE_WITH_ID = 101;
    static final int MOVIES_BY_FILTER = 102;
    static final int MOVIE_TRAILER = 201;
    static final int MOVIE_REVIEW = 202;
    static final int POPULAR = 301;
    static final int TOP_RATED = 302;
    static final int FAVORITE = 400;

    static final String FILTER_FAVORITE = "favorites";
    static final String FILTER_POPULAR = "popular";
    static final String FILTER_TOP_RATED = "top_rated";

    private static final SQLiteQueryBuilder sMoviesByFavoriteQueryBuilder;
    static{
        sMoviesByFavoriteQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //movies LEFT OUTER JOIN popular_movies ON movies.movie_id = popular_movies.movie_id
        sMoviesByFavoriteQueryBuilder.setTables(
                MoviesContract.MovieEntry.TABLE_NAME + " LEFT OUTER JOIN " +
                        MoviesContract.FavoriteEntry.TABLE_NAME +
                        " ON " + MoviesContract.MovieEntry.TABLE_NAME +
                        "." + MoviesContract.MovieEntry.COLUMN_MOVIE_ID +
                        " = " + MoviesContract.FavoriteEntry.TABLE_NAME +
                        "." + MoviesContract.FavoriteEntry.COLUMN_MOVIE_ID);
    }

    private static final SQLiteQueryBuilder sMoviesByPopularityQueryBuilder;
    static{
        sMoviesByPopularityQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //movies LEFT OUTER JOIN favorites ON movies.movie_id = favorites.movie_id
        sMoviesByPopularityQueryBuilder.setTables(
                MoviesContract.MovieEntry.TABLE_NAME + " LEFT OUTER JOIN " +
                        MoviesContract.PopularMovieEntry.TABLE_NAME +
                        " ON " + MoviesContract.MovieEntry.TABLE_NAME +
                        "." + MoviesContract.MovieEntry.COLUMN_MOVIE_ID +
                        " = " + MoviesContract.PopularMovieEntry.TABLE_NAME +
                        "." + MoviesContract.PopularMovieEntry.COLUMN_MOVIE_ID);
    }

    private static final SQLiteQueryBuilder sMoviesByTopRatedQueryBuilder;
    static{
        sMoviesByTopRatedQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //movies LEFT OUTER JOIN top_rated_movies ON movies.movie_id = top_rated_movies.movie_id
        sMoviesByTopRatedQueryBuilder.setTables(
                MoviesContract.MovieEntry.TABLE_NAME + " LEFT OUTER JOIN " +
                        MoviesContract.TopRatedMovieEntry.TABLE_NAME +
                        " ON " + MoviesContract.MovieEntry.TABLE_NAME +
                        "." + MoviesContract.MovieEntry.COLUMN_MOVIE_ID +
                        " = " + MoviesContract.TopRatedMovieEntry.TABLE_NAME +
                        "." + MoviesContract.TopRatedMovieEntry.COLUMN_MOVIE_ID);
    }

    private static final SQLiteQueryBuilder sMovieDetailQueryBuilder;
    static{
        sMovieDetailQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //movies LEFT OUTER JOIN favorites ON movies.movie_id = favorites.movie_id
        //       LEFT OUTER JOIN movie_reviews ON movies.movie_id = movie_reviews.movie_id
        //       LEFT OUTER JOIN movie_trailers ON movies.movie_id = movie_trailers.movie_id
        sMovieDetailQueryBuilder.setTables(
                MoviesContract.MovieEntry.TABLE_NAME + " LEFT OUTER JOIN " +
                        MoviesContract.FavoriteEntry.TABLE_NAME +
                        " ON " + MoviesContract.MovieEntry.TABLE_NAME +
                        "." + MoviesContract.MovieEntry.COLUMN_MOVIE_ID +
                        " = " + MoviesContract.FavoriteEntry.TABLE_NAME +
                        "." + MoviesContract.FavoriteEntry.COLUMN_MOVIE_ID +
                        " LEFT OUTER JOIN " +
                        MoviesContract.MovieTrailerEntry.TABLE_NAME +
                        " ON " + MoviesContract.MovieEntry.TABLE_NAME +
                        "." + MoviesContract.MovieEntry.COLUMN_MOVIE_ID +
                        " = " + MoviesContract.MovieTrailerEntry.TABLE_NAME +
                        "." + MoviesContract.MovieTrailerEntry.COLUMN_MOVIE_ID +
                        " LEFT OUTER JOIN " +
                        MoviesContract.MovieReviewEntry.TABLE_NAME +
                        " ON " + MoviesContract.MovieEntry.TABLE_NAME +
                        "." + MoviesContract.MovieEntry.COLUMN_MOVIE_ID +
                        " = " + MoviesContract.MovieReviewEntry.TABLE_NAME +
                        "." + MoviesContract.MovieReviewEntry.COLUMN_MOVIE_ID);
    }

    private static final String sMovieDetailSelection =
            MoviesContract.MovieEntry.TABLE_NAME+
                    "." + MoviesContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ";

    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoviesContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, MoviesContract.PATH_MOVIES + "/*", MOVIE_WITH_ID);
        matcher.addURI(authority, MoviesContract.PATH_MOVIES + "/*/#", MOVIES_BY_FILTER);

        matcher.addURI(authority, MoviesContract.PATH_FAVORITE, FAVORITE);
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
            // Student: Uncomment and fill out these two cases
            case MOVIE_WITH_ID:
                return MoviesContract.MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIES_BY_FILTER:
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
            // "movies/*"
            case MOVIE_WITH_ID:
            {
                retCursor = getMovieById(uri, projection, sortOrder);
                break;
            }
            // "movies/filter/*"
            case MOVIES_BY_FILTER: {
                retCursor = getMoviesByFilter(uri, projection, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    private Cursor getMoviesByFilter(Uri uri, String[] projection, String sortOrder) {
        String filterSetting = MoviesContract.MovieEntry.getFilterSettingFromUri(uri);

        SQLiteQueryBuilder sqLiteQueryBuilder;
        switch (filterSetting)
        {
            case FILTER_FAVORITE:
                sqLiteQueryBuilder = sMoviesByFavoriteQueryBuilder;
                break;
            case FILTER_POPULAR:
                sqLiteQueryBuilder = sMoviesByPopularityQueryBuilder;
                break;
            case FILTER_TOP_RATED:
            default:
                sqLiteQueryBuilder = sMoviesByTopRatedQueryBuilder;
                break;
        }

        return sqLiteQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getMovieById(Uri uri, String[] projection, String sortOrder) {
        String movie_id = MoviesContract.MovieEntry.getIdFromUri(uri);

        return sMovieDetailQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sMovieDetailSelection,
                new String[]{movie_id},
                null,
                null,
                sortOrder
        );
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIE: {
                long _id = db.insert(MoviesContract.MovieEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MoviesContract.MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case FAVORITE: {
                long _id = db.insert(MoviesContract.FavoriteEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MoviesContract.FavoriteEntry.buildFavoriteUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case MOVIE_TRAILER: {
                long _id = db.insert(MoviesContract.MovieTrailerEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = null; //MoviesContract.MovieTrailerEntry.buildTrailerUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case MOVIE_REVIEW: {
                long _id = db.insert(MoviesContract.MovieReviewEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = null; //MoviesContract.MovieReviewEntry.buildReviewUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case MOVIE:
                rowsDeleted = db.delete(
                        MoviesContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case FAVORITE:
                rowsDeleted = db.delete(
                        MoviesContract.FavoriteEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case POPULAR:
                rowsDeleted = db.delete(
                        MoviesContract.PopularMovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TOP_RATED:
                rowsDeleted = db.delete(
                        MoviesContract.TopRatedMovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOVIE_TRAILER:
                rowsDeleted = db.delete(
                        MoviesContract.TopRatedMovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOVIE_REVIEW:
                rowsDeleted = db.delete(
                        MoviesContract.TopRatedMovieEntry.TABLE_NAME, selection, selectionArgs);
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
            case MOVIE:
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
            case MOVIE:
                return doBulkInsert(MoviesContract.MovieEntry.TABLE_NAME, uri, values);
            case POPULAR:
                return doBulkInsert(MoviesContract.PopularMovieEntry.TABLE_NAME, uri, values);
            case TOP_RATED:
                return doBulkInsert(MoviesContract.TopRatedMovieEntry.TABLE_NAME, uri, values);
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
                long _id = db.insert(tableName, null, value);
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
