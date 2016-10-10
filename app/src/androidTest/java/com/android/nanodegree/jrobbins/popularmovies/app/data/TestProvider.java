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

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.android.nanodegree.jrobbins.popularmovies.app.data.MoviesContract.MovieEntry;

public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    /*
       This helper function deletes all records from both database tables using the ContentProvider.
       It also queries the ContentProvider to make sure that the database has been successfully
       deleted, so it cannot be used until the Query and Delete functions have been written
       in the ContentProvider.
     */
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                MovieEntry.CONTENT_URI,
                null,
                null
        );
//        mContext.getContentResolver().delete(
//                FavoriteEntry.CONTENT_URI,
//                null,
//                null
//        );
//        mContext.getContentResolver().delete(
//                MovieListsEntry.CONTENT_URI,
//                null,
//                null
//        );
//        mContext.getContentResolver().delete(
//                MovieDetailsEntry.CONTENT_URI,
//                null,
//                null
//        );

        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Movies table during delete", 0, cursor.getCount());
        cursor.close();

//        cursor = mContext.getContentResolver().query(
//                FavoriteEntry.CONTENT_URI,
//                null,
//                null,
//                null,
//                null
//        );
//        assertEquals("Error: Records not deleted from Favorites table during delete", 0, cursor.getCount());
//        cursor.close();
//
//        cursor = mContext.getContentResolver().query(
//                MovieListsEntry.CONTENT_URI,
//                null,
//                null,
//                null,
//                null
//        );
//        assertEquals("Error: Records not deleted from MovieListsEntry table during delete", 0, cursor.getCount());
//        cursor.close();
//
//        cursor = mContext.getContentResolver().query(
//                MovieDetailsEntry.CONTENT_URI,
//                null,
//                null,
//                null,
//                null
//        );
//        assertEquals("Error: Records not deleted from MovieDetailsEntry table during delete", 0, cursor.getCount());
//        cursor.close();
    }

    /*
        Student: Refactor this function to use the deleteAllRecordsFromProvider functionality once
        you have implemented delete functionality there.
     */
    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    /*
        This test checks to make sure that the content provider is registered correctly.
        Students: Uncomment this test to make sure you've correctly registered the MoviesProvider.
     */
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // MoviesProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                MoviesProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: MoviesProvider registered with authority: " + providerInfo.authority +
                    " instead of authority: " + MoviesContract.CONTENT_AUTHORITY,
                    providerInfo.authority, MoviesContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: MoviesProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    /*
            This test doesn't touch the database.  It verifies that the ContentProvider returns
            the correct type for each type of URI that it can handle.
            Students: Uncomment this test to verify that your implementation of GetType is
            functioning correctly.
         */
    public void testGetType() {
        // content://com.android.nanodegree.jrobbins.popularmovies.app/movies/
        // content://com.android.nanodegree.jrobbins.popularmovies.app/movies/12345

        String type = mContext.getContentResolver().getType(MovieEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.android.nanodegree.jrobbins.popularmovies.app/movies
        assertEquals("Error: the MovieEntry CONTENT_URI should return MovieEntry.CONTENT_TYPE",
                MovieEntry.CONTENT_TYPE, type);

        String testId = "12345";
        // content://com.android.nanodegree.jrobbins.popularmovies.app/movies/12345
        type = mContext.getContentResolver().getType(
                MovieEntry.buildMovieWithMovieIdUri(testId));
        // vnd.android.cursor.dir/com.example.android.sunshine.app/Movies
        assertEquals("Error: the MovieEntry CONTENT_URI with location should return MovieEntry.CONTENT_TYPE",
                MovieEntry.CONTENT_ITEM_TYPE, type);
    }

    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.
     */
    public void testBasicMoviesQuery() {
        // insert our test records into the database
        MoviesDbHelper dbHelper = new MoviesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues MoviesValues = TestUtilities.createMovieValues();

        long MoviesRowId = db.insert(MovieEntry.TABLE_NAME, null, MoviesValues);
        assertTrue("Unable to Insert MovieEntry into the Database", MoviesRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor MoviesCursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicMoviesQuery", MoviesCursor, MoviesValues);
    }

    /*
        This test uses the provider to insert and then update the data.
     */
    public void testUpdateMovie() {
        // Create a new map of values, where column names are the keys
        ContentValues values = TestUtilities.createMovieValues();

        Uri movieUri = mContext.getContentResolver().
                insert(MovieEntry.CONTENT_URI, values);
        long movieRowId = ContentUris.parseId(movieUri);

        // Verify we got a row back.
        assertTrue(movieRowId != -1);
        Log.d(LOG_TAG, "New row id: " + movieRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(MovieEntry._ID, movieRowId);
        updatedValues.put(MovieEntry.COLUMN_VOTE_AVG, 9.1);

        // Create a cursor with observer to make sure that the content provider is notifying
        // the observers as expected
        Cursor movieCursor = mContext.getContentResolver().query(MovieEntry.CONTENT_URI, null, null, null, null);

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        movieCursor.registerContentObserver(tco);

        int count = mContext.getContentResolver().update(
                MovieEntry.CONTENT_URI, updatedValues, MovieEntry._ID + "= ?",
                new String[] { Long.toString(movieRowId)});
        assertEquals(count, 1);

        // Test to make sure our observer is called.  If not, we throw an assertion.
        //
        // Students: If your code is failing here, it means that your content provider
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();

        movieCursor.unregisterContentObserver(tco);
        movieCursor.close();

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,   // projection
                MovieEntry._ID + " = " + movieRowId,
                null,   // Values for the "where" clause
                null    // sort order
        );

        TestUtilities.validateCursor("testUpdateMovie.  Error validating location entry update.",
                cursor, updatedValues);

        cursor.close();
    }


    // Make sure we can still delete after adding/updating stuff
    //
    // It relies on insertions with testInsertReadProvider, so insert and
    // query functionality must also be complete before this test can be used.
    public void testInsertReadProvider() {

        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();

        // Fantastic.  Now that we have a location, add some Movies!
        ContentValues MoviesValues = TestUtilities.createMovieValues();

        mContext.getContentResolver().registerContentObserver(MovieEntry.CONTENT_URI, true, tco);

        Uri MoviesInsertUri = mContext.getContentResolver()
                .insert(MovieEntry.CONTENT_URI, MoviesValues);
        assertTrue(MoviesInsertUri != null);

        // Did our content observer get called?  Students:  If this fails, your insert Movies
        // in your ContentProvider isn't calling
        // getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        // A cursor is your primary interface to the query results.
        Cursor MoviesCursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating MovieEntry insert.",
                MoviesCursor, MoviesValues);

        // Get the Movies data
        MoviesCursor = mContext.getContentResolver().query(
                MovieEntry.buildMovieWithMovieIdUri(TestUtilities.TEST_MOVIE_ID),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Movies and Location Data.",
                MoviesCursor, MoviesValues);
    }

    // Make sure we can still delete after adding/updating stuff
    //
    // Student: Uncomment this test after you have completed writing the delete functionality
    // in your provider.  It relies on insertions with testInsertReadProvider, so insert and
    // query functionality must also be complete before this test can be used.
    public void testDeleteRecords() {
        testInsertReadProvider();

        // Register a content observer for our Movies delete.
        TestUtilities.TestContentObserver MoviesObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieEntry.CONTENT_URI, true, MoviesObserver);

        deleteAllRecordsFromProvider();

        // If either of these fail,  most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in the ContentProvider
        // delete.  (only if the insertReadProvider is succeeding)
        MoviesObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(MoviesObserver);
    }


    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;
    static ContentValues[] createBulkInsertMoviesValues() {
        String currentTestID = TestUtilities.TEST_MOVIE_ID;
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, currentTestID+= "1" ) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MovieEntry.COLUMN_MOVIE_ID, currentTestID);
            contentValues.put(MovieEntry.COLUMN_OVERVIEW, "Once Upon a Time...");
            contentValues.put(MovieEntry.COLUMN_TITLE, "Movie " + i);
            contentValues.put(MovieEntry.COLUMN_VOTE_AVG, 1.1);
            contentValues.put(MovieEntry.COLUMN_GENRE_IDS, "1,2,3");
            contentValues.put(MovieEntry.COLUMN_BACKDROP_PATH, "back/path");
            contentValues.put(MovieEntry.COLUMN_POSTER, "poster/path");
            contentValues.put(MovieEntry.COLUMN_HAS_VIDEO, 0);
            contentValues.put(MovieEntry.COLUMN_RELEASE_DATE, TestUtilities.TEST_DATE);
            returnContentValues[i] = contentValues;
        }
        return returnContentValues;
    }

    // Note that this test will work with the built-in (default) provider
    // implementation, which just inserts records one-at-a-time, so really do implement the
    // BulkInsert ContentProvider function.
    public void testBulkInsert() {

        // Now we can bulkInsert some Movies.  In fact, we only implement BulkInsert for Movies
        // entries.  With ContentProviders, you really only have to implement the features you
        // use, after all.
        ContentValues[] bulkInsertContentValues = createBulkInsertMoviesValues();

        // Register a content observer for our bulk insert.
        TestUtilities.TestContentObserver MoviesObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieEntry.CONTENT_URI, true, MoviesObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(MovieEntry.CONTENT_URI, bulkInsertContentValues);

        // Students:  If this fails, it means that you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in your BulkInsert
        // ContentProvider method.
        MoviesObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(MoviesObserver);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                MovieEntry.COLUMN_MOVIE_ID + " ASC"  // sort order == by DATE ASCENDING
        );

        // we should have as many records in the database as we've inserted
        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        // and let's make sure they match the ones we created
        cursor.moveToFirst();
        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext() ) {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating MovieEntry " + i,
                    cursor, bulkInsertContentValues[i]);
        }
        cursor.close();
    }
}
