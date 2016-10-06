package com.android.nanodegree.jrobbins.popularmovies.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.android.nanodegree.jrobbins.popularmovies.app.data.MoviesContract.*;

/**
 * Created by jim.robbins on 9/29/16.
 */

public class MoviesDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "popular_movies.db";

    public MoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createFavoritesTable(db);
        createMoviesCacheTables(db);
    }

    private void createMoviesCacheTables(SQLiteDatabase db)
    {
        createMoviesTable(db);
        createFiltersTables(db);
        createVideosTable(db);
        createReviewsTable(db);
    }

    private void createFavoritesTable(SQLiteDatabase db)
    {
        // Create a table to hold favorites.  A favorite consists of a boolean supplied by the user and an movie id
        final String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE " + FavoriteEntry.TABLE_NAME + "(" +
                FavoriteEntry._ID + " INTEGER PRIMARY KEY," +
                FavoriteEntry.COLUMN_MOVIE_ID + " TEXT UNIQUE NOT NULL " +
                " );";

        db.execSQL(SQL_CREATE_FAVORITES_TABLE);
    }

    private void createMoviesTable(SQLiteDatabase db)
    {
        // Create a table to hold favorites.  A favorite consists of a boolean supplied by the user and an movie id
        final String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE " + FavoriteEntry.TABLE_NAME + "(" +
                FavoriteEntry._ID + " INTEGER PRIMARY KEY," +
                FavoriteEntry.COLUMN_MOVIE_ID + " TEXT UNIQUE NOT NULL " +
                " );";

        db.execSQL(SQL_CREATE_FAVORITES_TABLE);
    }

    private void createFiltersTables(SQLiteDatabase db)
    {
        // Create a tables to hold movie list filter results
        final String SQL_CREATE_POPULAR_MOVIES_TABLE = "CREATE TABLE " + PopularMovieEntry.TABLE_NAME + "(" +
                PopularMovieEntry._ID + " INTEGER PRIMARY KEY," +
                PopularMovieEntry.COLUMN_MOVIE_ID + " TEXT UNIQUE NOT NULL " +
                " );";

        final String SQL_CREATE_TOP_RATED_MOVIES_TABLE = "CREATE TABLE " + TopRatedMovieEntry.TABLE_NAME + "(" +
                TopRatedMovieEntry._ID + " INTEGER PRIMARY KEY," +
                TopRatedMovieEntry.COLUMN_MOVIE_ID + " TEXT UNIQUE NOT NULL " +
                " );";

        db.execSQL(SQL_CREATE_POPULAR_MOVIES_TABLE);
        db.execSQL(SQL_CREATE_TOP_RATED_MOVIES_TABLE);
    }

    private void createVideosTable(SQLiteDatabase db)
    {
        // Create a table to hold movie trailers.
        final String SQL_CREATE_TRAILERS_TABLE = "CREATE TABLE " + MovieTrailerEntry.TABLE_NAME + "(" +
                MovieTrailerEntry._ID + " INTEGER PRIMARY KEY," +
                MovieTrailerEntry.COLUMN_MOVIE_ID + " TEXT UNIQUE NOT NULL " +
                " );";

        db.execSQL(SQL_CREATE_TRAILERS_TABLE);
    }

    private void createReviewsTable(SQLiteDatabase db)
    {
        // Create a table to hold movie reviews.
        final String SQL_CREATE_REVIEWS_TABLE = "CREATE TABLE " + MovieReviewEntry.TABLE_NAME + "(" +
                MovieReviewEntry._ID + " INTEGER PRIMARY KEY," +
                MovieReviewEntry.COLUMN_MOVIE_ID + " TEXT UNIQUE NOT NULL " +
                " );";

        db.execSQL(SQL_CREATE_REVIEWS_TABLE);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Wipe all db directly pulled from online cache if DB changes
        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PopularMovieEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TopRatedMovieEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieTrailerEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieReviewEntry.TABLE_NAME);

        createMoviesCacheTables(db);

        //If we need to update Favorites table, we need to handle that separately
    }

    @Override
    public synchronized void close() {
        super.close();
    }
}
