/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.nanodegree.jrobbins.popularmovies.app.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(MoviesDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }

    /*
        Note that this only tests that the movies_list table has the correct columns, since we
        give you the code for the Movies table.  This test does not look at the
     */
    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(MoviesContract.MovieEntry.TABLE_NAME);
        tableNameHashSet.add(MoviesContract.FavoritesEntry.TABLE_NAME);
        tableNameHashSet.add(MoviesContract.MovieListsEntry.TABLE_NAME);

        mContext.deleteDatabase(MoviesDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new MoviesDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain both the movies_list entry
        // and Movies entry tables
        assertTrue("Error: Your database was created without both the movies_list entry and Movies entry tables",
                tableNameHashSet.isEmpty());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> moviesColumnHashSet = new HashSet<String>();
        moviesColumnHashSet.add(MoviesContract.MovieEntry._ID);
        moviesColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_MOVIE_ID);
        moviesColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_TITLE);
        moviesColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_POSTER);
        moviesColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_BACKDROP_PATH);
        moviesColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_OVERVIEW);
        moviesColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_VOTE_AVG);
        moviesColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE);
        moviesColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_GENRES);
        moviesColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_CREATE_DATE);
        moviesColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_HOMEPAGE);
        moviesColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_IMDB_ID);
        moviesColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_POPULARITY);
        moviesColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_PRODUCTION_COMPANIES);
        moviesColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_VOTE_COUNT);
        moviesColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_REVIEWS);
        moviesColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_RUNTIME);
        moviesColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_TRAILERS);

        checkColumns(MoviesContract.MovieEntry.TABLE_NAME, c, db, moviesColumnHashSet);

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> moviesListColumnHashSet = new HashSet<String>();
        moviesListColumnHashSet.add(MoviesContract.MovieListsEntry._ID);
        moviesListColumnHashSet.add(MoviesContract.MovieListsEntry.COLUMN_MOVIE_ID);
        moviesListColumnHashSet.add(MoviesContract.MovieListsEntry.COLUMN_LIST_ID);

        checkColumns(MoviesContract.MovieListsEntry.TABLE_NAME, c, db, moviesListColumnHashSet);

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> favoritesColumnHashSet = new HashSet<String>();
        favoritesColumnHashSet.add(MoviesContract.FavoritesEntry._ID);
        favoritesColumnHashSet.add(MoviesContract.FavoritesEntry.COLUMN_IS_FAVORITE);

        checkColumns(MoviesContract.FavoritesEntry.TABLE_NAME, c, db, favoritesColumnHashSet);

        db.close();
    }

    private void checkColumns(String tableName, Cursor c, SQLiteDatabase db, HashSet<String> columnHashSet)
    {
        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + tableName + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            columnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, means that the database doesn't contain all of the required entry columns
        assertTrue("Error: The database table " + tableName +
                        ", doesn't contain all of the required entry columns",
                columnHashSet.isEmpty());
    }

    /*
        Test that we can insert and query the database.  Look in TestUtilities
        where you can use the "createMovieValues" function.  You can
        also make use of the validateCurrentRecord function from within TestUtilities.
     */
    public void testMoviesTable() {

        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        MoviesDbHelper dbHelper = new MoviesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step (Movies): Create movie values
        ContentValues movieValues = TestUtilities.createMovieValues();

        // Third Step (Movies): Insert ContentValues into database and get a row ID back
        long movieRowId = db.insert(MoviesContract.MovieEntry.TABLE_NAME, null, movieValues);
        assertTrue(movieRowId != -1);

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                MoviesContract.MovieEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        // Move the cursor to the first valid database row and check to see if we have any rows
        assertTrue( "Error: No Records returned from movies_list query", cursor.moveToFirst() );

        // Fifth Step: Validate the movies_list Query
        TestUtilities.validateCurrentRecord("testInsertReadDb MoviesEntry failed to validate",
                cursor, movieValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse( "Error: More than one record returned from Movies query",
                cursor.moveToNext() );

        insertListType();

        // Sixth Step: Close cursor and database
        cursor.close();
        dbHelper.close();
    }

    public long insertListType() {
        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        MoviesDbHelper dbHelper = new MoviesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step: Create ContentValues of what you want to insert
        // (you can use the createFavoritesValues if you wish)
        ContentValues testValues = TestUtilities.createListTypeValues();

        // Third Step: Insert ContentValues into database and get a row ID back
        long rowId;
        rowId = db.insert(MoviesContract.MovieListsEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(rowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                MoviesContract.MovieListsEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Move the cursor to a valid database row and check to see if we got any records back
        // from the query
        assertTrue( "Error: No Records returned from movies_list query", cursor.moveToFirst() );

        // Fifth Step: Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("Error: movies_list Query Validation Failed",
                cursor, testValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse( "Error: More than one record returned from movies_list query",
                cursor.moveToNext() );

        // Sixth Step: Close Cursor and Database
        cursor.close();
        db.close();
        return rowId;
    }
}
