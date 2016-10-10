package com.android.nanodegree.jrobbins.popularmovies.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.android.nanodegree.jrobbins.popularmovies.app.utils.PollingCheck;

import java.util.Map;
import java.util.Set;

/*
    Students: These are functions and some test data to make it easier to test your database and
    Content Provider.  Note that you'll want your MoviesContract class to exactly match the one
    in our solution to use these as-given.
 */
public class TestUtilities extends AndroidTestCase {
    static final String TEST_MOVIE_ID = "12345";
    static final long TEST_DATE = 1419033600L;  // December 20th, 2014

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    /*
        Use this to create some default movie values for your database tests.
     */
    static ContentValues createMovieValues() {
        ContentValues movieValues = new ContentValues();
        movieValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE, TEST_DATE);
        movieValues.put(MoviesContract.MovieEntry.COLUMN_MOVIE_ID, TEST_MOVIE_ID);
        movieValues.put(MoviesContract.MovieEntry.COLUMN_TITLE, "Toy Story");
        movieValues.put(MoviesContract.MovieEntry.COLUMN_POSTER, "poster_123");
        movieValues.put(MoviesContract.MovieEntry.COLUMN_OVERVIEW, "Loruem ipsum blah blah blah");
        movieValues.put(MoviesContract.MovieEntry.COLUMN_BACKDROP_PATH, "path/");
        movieValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_AVG, 1.5);
        movieValues.put(MoviesContract.MovieEntry.COLUMN_GENRE_IDS, "1,2,4");
        movieValues.put(MoviesContract.MovieEntry.COLUMN_HAS_VIDEO, 0);

        return movieValues;
    }

//    static ContentValues createFavoritesValues() {
//        // Create a new map of values, where column names are the keys
//        ContentValues testValues = new ContentValues();
//        testValues.put(MoviesContract.FavoriteEntry.COLUMN_MOVIE_ID, "12345");
//
//        return testValues;
//    }
//
//    static ContentValues createMovieDetailsValues() {
//        // Create a new map of values, where column names are the keys
//        ContentValues testValues = new ContentValues();
//        testValues.put(MoviesContract.MovieDetailsEntry.COLUMN_MOVIE_ID, "12345");
//        testValues.put(MoviesContract.MovieDetailsEntry.COLUMN_DETAIL_TYPE, "123");
//        testValues.put(MoviesContract.MovieDetailsEntry.COLUMN_DETAIL, "http://google.com");
//
//        return testValues;
//    }

//    static long insertTestFavoriteValues(Context context) {
//        // insert our test records into the database
//        MoviesDbHelper dbHelper = new MoviesDbHelper(context);
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//        ContentValues testValues = TestUtilities.createFavoritesValues();
//
//        long favoritesRowId;
//        favoritesRowId = db.insert(MoviesContract.FavoriteEntry.TABLE_NAME, null, testValues);
//
//        // Verify we got a row back.
//        assertTrue("Error: Failure to insert favorites Values", favoritesRowId != -1);
//
//        return favoritesRowId;
//    }

    /*
        Test the ContentObserver callbacks using the PollingCheck class that we grabbed from the Android
        CTS tests.

        Note that this only tests that the onChange function is called; it does not test that the
        correct Uri is returned.
     */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
