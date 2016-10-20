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

import android.net.Uri;
import android.test.AndroidTestCase;

public class TestMoviesContract extends AndroidTestCase {

    // intentionally includes a slash to make sure Uri is getting quoted correctly
    private static final String TEST_MOVIE_ID = TestUtilities.TEST_MOVIE_ID;
    private static final String TEST_MOVIE_FILTER = TestUtilities.TEST_MOVIE_FILTER;

    public void testBuildMovieWithMovieIdUri() {
        Uri movieUri = MoviesContract.MovieEntry.buildMovieWithMovieIdUri(TEST_MOVIE_ID);
        assertNotNull("Error: Null Uri returned.  You must fill-in buildMovieWithMovieIdUri in " +
                        "MoviesContract.",
                movieUri);
        assertEquals("Error: Movies id not properly appended to the end of the Uri",
                TEST_MOVIE_ID,MoviesContract.MovieEntry.getMovieIdFromUri(movieUri));
        assertEquals("Error: Movies Uri doesn't match our expected result",
                movieUri.toString(),
                MoviesContract.MovieEntry.CONTENT_URI.toString() + "/detail/" + TEST_MOVIE_ID);
    }

    public void testBuildMovieListUri() {
        Uri movieUri = MoviesContract.MovieEntry.buildMoviesWithListTypeUri(TEST_MOVIE_FILTER);
        assertNotNull("Error: Null Uri returned.  You must fill-in buildMoviesWithListTypeUri in " +
                        "MoviesContract.",
                movieUri);
        assertEquals("Error: Movies id not properly appended to the end of the Uri",
                TEST_MOVIE_FILTER, MoviesContract.MovieEntry.getListTypeFromUri(movieUri));
        assertEquals("Error: Movies Uri doesn't match our expected result",
                movieUri.toString(),
                MoviesContract.MovieEntry.CONTENT_URI.toString() + "/list/" +TEST_MOVIE_FILTER);
    }

    public void testBuildFavoriteMovieWithMovieIdUri() {
        Uri movieUri = MoviesContract.FavoritesEntry.buildFavoriteWithIdUri(TEST_MOVIE_ID);
        assertNotNull("Error: Null Uri returned.  You must fill-in buildFavoriteWithIdUri in " +
                        "MoviesContract.",
                movieUri);
        assertEquals("Error: Movies Uri doesn't match our expected result",
                movieUri.toString(),
                MoviesContract.FavoritesEntry.CONTENT_URI.toString() + "/" + TEST_MOVIE_ID);
    }

    public void testBuildFavoritesMovieListUri() {
        Uri movieUri = MoviesContract.FavoritesEntry.buildFavoritesUri();
        assertNotNull("Error: Null Uri returned.  You must fill-in buildFavoritesUri in " +
                        "MoviesContract.",
                movieUri);

        assertEquals("Error: Favorites Uri doesn't match our expected result",
                movieUri.toString(),
                MoviesContract.FavoritesEntry.CONTENT_URI.toString());
    }

}
