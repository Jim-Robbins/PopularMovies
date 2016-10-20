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
        createMoviesListTables(db);
    }

    private void createFavoritesTable(SQLiteDatabase db)
    {
        // Create a table to hold favorites.  A favorite consists of a boolean supplied by the user and an movie id
        final String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE " + FavoritesEntry.TABLE_NAME + "(" +
                FavoritesEntry._ID + " INTEGER PRIMARY KEY," +
                FavoritesEntry.COLUMN_IS_FAVORITE + " TEXT UNIQUE NOT NULL " +
                " );";

        db.execSQL(SQL_CREATE_FAVORITES_TABLE);
    }

    private void createMoviesTable(SQLiteDatabase db)
    {
        // Create a table to hold list of movies.
        final String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + "(" +
                MovieEntry._ID + " INTEGER PRIMARY KEY," +
                MovieEntry.COLUMN_MOVIE_ID + " TEXT UNIQUE NOT NULL, " +
                MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_POSTER + " TEXT, " +
                MovieEntry.COLUMN_BACKDROP_PATH + " TEXT, " +
                MovieEntry.COLUMN_RELEASE_DATE + " TEXT, " +
                MovieEntry.COLUMN_VOTE_AVG + " REAL, " +
                MovieEntry.COLUMN_HOMEPAGE + " TEXT, " +
                MovieEntry.COLUMN_IMDB_ID + " TEXT, " +
                MovieEntry.COLUMN_POPULARITY + " INTEGER, " +
                MovieEntry.COLUMN_PRODUCTION_COMPANIES + " TEXT, " +
                MovieEntry.COLUMN_RUNTIME + " REAL, " +
                MovieEntry.COLUMN_VOTE_COUNT + " INTEGER, " +
                MovieEntry.COLUMN_GENRES + " TEXT, " +
                MovieEntry.COLUMN_TRAILERS + " TEXT, " +
                MovieEntry.COLUMN_REVIEWS + " TEXT, " +
                MovieEntry.COLUMN_CREATE_DATE + " INTEGER DEFAULT CURRENT_TIMESTAMP " +
                " );";

        db.execSQL(SQL_CREATE_FAVORITES_TABLE);
    }

    private void createMoviesListTables(SQLiteDatabase db)
    {
        // Create a tables to hold movie list filter results
        final String SQL_CREATE_MOVIES_LISTS_TABLE = "CREATE TABLE " + MovieListsEntry.TABLE_NAME + "(" +
                MovieListsEntry._ID + " INTEGER PRIMARY KEY," +
                MovieListsEntry.COLUMN_MOVIE_ID + " TEXT NOT NULL, " +
                MovieListsEntry.COLUMN_LIST_ID + " INTEGER NOT NULL, " +
                "UNIQUE(" + MovieListsEntry.COLUMN_MOVIE_ID + "," + MovieListsEntry.COLUMN_LIST_ID +") ON CONFLICT REPLACE" +
                " );";

        db.execSQL(SQL_CREATE_MOVIES_LISTS_TABLE);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Wipe all db directly pulled from online cache if DB changes
        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieListsEntry.TABLE_NAME);

        createMoviesCacheTables(db);

        //If we need to update Favorites table, we need to handle that separately
    }

    @Override
    public synchronized void close() {
        super.close();
    }
}
