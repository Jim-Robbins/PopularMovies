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

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

public class TestUriMatcher extends AndroidTestCase {
    private static final String TEST_MOVIE_ID = "12345";

    // content://com.example.android.sunshine.app/Movies"
    private static final Uri TEST_MOVIES_DIR = MoviesContract.MovieEntry.CONTENT_URI;
    private static final Uri TEST_MOVIES_WITH_ID = MoviesContract.MovieEntry.buildMovieWithMovieIdUri(TEST_MOVIE_ID);

    public void testUriMatcher() {
        UriMatcher testMatcher = MoviesProvider.buildUriMatcher();

        assertEquals("Error: The MOVIES URI was matched incorrectly.",
                testMatcher.match(TEST_MOVIES_DIR), MoviesProvider.MOVIES);
        assertEquals("Error: The MOVIES WITH ID URI was matched incorrectly.",
                testMatcher.match(TEST_MOVIES_WITH_ID), MoviesProvider.MOVIES_WITH_ID);

    }
}
