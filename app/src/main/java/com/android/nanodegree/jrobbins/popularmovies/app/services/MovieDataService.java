package com.android.nanodegree.jrobbins.popularmovies.app.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.nanodegree.jrobbins.popularmovies.app.BuildConfig;
import com.android.nanodegree.jrobbins.popularmovies.app.data.MoviesContract;
import com.android.nanodegree.jrobbins.popularmovies.app.data.MoviesProvider;
import com.android.nanodegree.jrobbins.popularmovies.app.utils.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Vector;

/**
 * MovieDataService will handle all the calls to TMDb API on a background thread
 * Then it will store the results in a local Sqlite db.
 * Finally it will send a notification letting the ui know the data is ready.
 */

public class MovieDataService extends IntentService {
    private static final String LOG_TAG = MovieDataService.class.getSimpleName();

    private static String MOVIE_DB_API_URL = "http://api.themoviedb.org/3/movie";
    private static String MOVIE_DB_IMG_URL = "http://image.tmdb.org/t/p/";
    private static String MOVIE_DB_PARAM_API_KEY = "api_key";

    public static String MOVIE_DB_IMG_SIZE_92 = "w92";
    public static String MOVIE_DB_IMG_SIZE_154 = "w154";
    public static String MOVIE_DB_IMG_SIZE_185 = "w185";
    public static String MOVIE_DB_IMG_SIZE_342 = "w342";
    public static String MOVIE_DB_IMG_SIZE_500 = "w500";
    public static String MOVIE_DB_IMG_SIZE_780 = "w780";
    public static String MOVIE_DB_IMG_SIZE_FULL = "original";
    public static String MOVIE_DB_KEY_RESULTS = "results";

    private String MOVIE_DB_KEY_ID = "id";
    private String MOVIE_DB_KEY_TITLE = "title";
    private String MOVIE_DB_KEY_POSTER_PATH = "poster_path";
    private String MOVIE_DB_KEY_OVERVIEW = "overview";
    private String MOVIE_DB_KEY_RELEASE_DATE = "release_date";
    private String MOVIE_DB_KEY_VOTE_AVG = "vote_average";
    private String MOVIE_DB_KEY_BACKDROP_PATH = "backdrop_path";
    private String MOVIE_DB_KEY_NAME = "name";

    private String MOVIE_DB_DETAIL_VIDEOS = "videos";
    private String MOVIE_DB_DETAIL_REVIEWS = "reviews";

    public static final String API_RESULT_LIST_SUCCESS = "MovieDataService.LIST_REQUEST_PROCESSED";
    public static final String API_RESULT_DETAIL_SUCCESS = "MovieDataService.DETAIL_REQUEST_PROCESSED";

    private Uri mUri;

    public MovieDataService() {
        super(LOG_TAG);
    }

    /**
     * Construct the URI to retrieve the poster image from TMDb
     *
     * @param size       Image size we want to load
     * @param posterPath The poster path from the movie details
     * @return uri to load the poster image
     */
    public static String getTheMovieDBApiPosterUri(String size, String posterPath) {

        Uri builtUri = Uri.parse(MOVIE_DB_IMG_URL).buildUpon()
                .appendPath(size)
                .build();
        return builtUri.toString() + posterPath;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        mUri = intent.getData();
        if (mUri == null) {
            return;
        }

        // Determine if we are looking up a list of movies or loading movie details
        String dataType = new MoviesProvider().getType(mUri);
        if (dataType != null && dataType.equalsIgnoreCase(MoviesContract.MovieEntry.CONTENT_TYPE)) {
            getMoviesListFromApi();
        } else {
            getMovieDetailsFromApi();
        }

    }

    /**
     * Make call to TMDb API and collect JSON response
     *
     * @param baseUrl The formatted api call
     * @return JSON result string
     */
    private String getJSONDataFromApiCall(String baseUrl) {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String movieDataJsonStr = null;

        try {
            // Create the request to TheMovieDB API, and open the connection
            Log.d(LOG_TAG, baseUrl);

            URL url = new URL(baseUrl);

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            movieDataJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return movieDataJsonStr;
    }

    /**
     * Send local broadcast to notify listeners the background task is complete
     */
    private void sendLocalBroadcastResult(String resultKey) {
        Intent intent = new Intent(resultKey);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Get list of movies from TMDb API and store results in local db
     */
    private void getMoviesListFromApi() {
        String listQuery = MoviesContract.MovieEntry.getListTypeFromUri(mUri);
        Log.d(LOG_TAG, "getMoviesListFromApi:" + listQuery);

        // Format the API uri
        String baseUrl = getTheMovieDBApiMovieListUri(listQuery);

        // Make the call to the API
        String movieDataJsonStr = getJSONDataFromApiCall(baseUrl);

        // Store Movie List in db
        parseMovieListResultsJSONintoDB(movieDataJsonStr);

        // Notify the UI data is ready
        sendLocalBroadcastResult(API_RESULT_LIST_SUCCESS);
    }

    /**
     * Construct the URI for TMDb query for movie lists
     * Possible parameters are avaiable at TMDb's API page, at
     * https://www.themoviedb.org/documentation/api
     *
     * @param filterBy
     * @return
     */
    private String getTheMovieDBApiMovieListUri(String filterBy) {
        Uri builtUri = Uri.parse(MOVIE_DB_API_URL).buildUpon()
                .appendPath(filterBy)
                .appendQueryParameter(MOVIE_DB_PARAM_API_KEY, BuildConfig.THE_MOVIE_DB_API_KEY)
                .build();
        return builtUri.toString();
    }

    /**
     * Parse out raw JSON data from The Movie DB api into usable Movie items
     *
     * @param moviesListJsonStr The raw JSON string returned from the api
     * @return a List of type Movie
     */
    private void parseMovieListResultsJSONintoDB(String moviesListJsonStr) {

        // Convert raw json string to JSON Object
        JSONObject moviesListJSONObj;

        try {
            moviesListJSONObj = new JSONObject(moviesListJsonStr);

            // Check that we have a proper JSON Object and a results key
            if (!moviesListJSONObj.isNull(MOVIE_DB_KEY_RESULTS)) {
                //Create our array list and populate it
                populateMoviesTable(moviesListJSONObj.getJSONArray(MOVIE_DB_KEY_RESULTS));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a List populated with Movie types created from JSON object data
     *
     * @param jsonArray The JSON array containing the list of movie results
     * @return List of type Movie
     */
    private void populateMoviesTable(JSONArray jsonArray) {

        JSONObject moviesJson;

        // Insert/replace the new movie list into the database
        Vector<ContentValues> cVVector = new Vector<ContentValues>(jsonArray.length());

        // Process each result in json array, decode and add to content values
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                moviesJson = jsonArray.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }

            ContentValues movieValues = parseJSONIntoMoviesContentValues(moviesJson);
            cVVector.add(movieValues);
        }

        // add to database
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            Uri insertUri = MoviesContract.MovieEntry.buildMoviesWithListTypeUri(Utility.getPreferredMovieList(this));
            this.getContentResolver().bulkInsert(insertUri, cvArray);
        }

        Log.d(LOG_TAG, "MovieDB Service Complete. " + cVVector.size() + " Inserted");
    }

    /**
     * Parse the json result item into contentValues Object
     *
     * @param movieJSONObj JSONObject of a particular movie object from the results list
     * @return contentValues created from the JSON data
     */
    private ContentValues parseJSONIntoMoviesContentValues(JSONObject movieJSONObj) {
        int movieId = Utility.getJSONIntValue(movieJSONObj, MOVIE_DB_KEY_ID);
        String movieTitle = Utility.getJSONStringValue(movieJSONObj, MOVIE_DB_KEY_TITLE);
        String moviePoster = Utility.getJSONStringValue(movieJSONObj, MOVIE_DB_KEY_POSTER_PATH);
        String movieOverview = Utility.getJSONStringValue(movieJSONObj, MOVIE_DB_KEY_OVERVIEW);
        String movieReleaseDate = Utility.getJSONStringValue(movieJSONObj, MOVIE_DB_KEY_RELEASE_DATE);
        Double movieVoteAvg = Utility.getJSONDoubleValue(movieJSONObj, MOVIE_DB_KEY_VOTE_AVG);
        String movieBackdropPath = Utility.getJSONStringValue(movieJSONObj, MOVIE_DB_KEY_BACKDROP_PATH);

        ContentValues contentValues = new ContentValues();
        contentValues.put(MoviesContract.MovieEntry.COLUMN_MOVIE_ID, movieId);
        contentValues.put(MoviesContract.MovieEntry.COLUMN_TITLE, movieTitle);
        contentValues.put(MoviesContract.MovieEntry.COLUMN_POSTER, moviePoster);
        contentValues.put(MoviesContract.MovieEntry.COLUMN_OVERVIEW, movieOverview);
        contentValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE, movieReleaseDate);
        contentValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_AVG, movieVoteAvg);
        contentValues.put(MoviesContract.MovieEntry.COLUMN_BACKDROP_PATH, movieBackdropPath);

        return contentValues;
    }

    /**
     * Get specific movie detail from TMDb API and store results in local db
     */
    private void getMovieDetailsFromApi() {
        ContentValues contentValues = new ContentValues();

        // Get movie id from the uri
        String movieId = MoviesContract.MovieEntry.getMovieIdFromUri(mUri);
        Log.d(LOG_TAG, "getMovieDetailsFromApi:" + movieId);

        // Get list of movie trailers
        String movieTrailerDataJsonStr = getMovieDetailJSONStringFromAPI(MOVIE_DB_DETAIL_VIDEOS);
        if (!TextUtils.isEmpty(movieTrailerDataJsonStr)) {
            contentValues.put(MoviesContract.MovieEntry.COLUMN_TRAILERS, movieTrailerDataJsonStr);
        }

        // Get list of movie reviews
        String movieReviewDataJsonStr = getMovieDetailJSONStringFromAPI(MOVIE_DB_DETAIL_REVIEWS);
        if (!TextUtils.isEmpty(movieReviewDataJsonStr)) {
            contentValues.put(MoviesContract.MovieEntry.COLUMN_REVIEWS, movieReviewDataJsonStr);
        }

        // Get movie details from API
        String movieDetailsDataJsonStr = getMovieDetailJSONStringFromAPI(null);
        contentValues = getMovieDetailsContentValuesFromJson(movieDetailsDataJsonStr, contentValues);

        // Update movie record with content details
        populateMovieDetails(contentValues);

        // Notify the UI data is ready
        sendLocalBroadcastResult(API_RESULT_DETAIL_SUCCESS);
    }

    /**
     * Helper method to grab movieId and get TMDb API uri to make API call and return data string
     *
     * @param detailPath
     * @return
     */
    private String getMovieDetailJSONStringFromAPI(String detailPath) {
        String movieId = MoviesContract.MovieEntry.getMovieIdFromUri(mUri);
        String baseUrl = getTheMovieDBApiMovieDetailUri(movieId, detailPath);

        return getJSONDataFromApiCall(baseUrl);
    }

    /**
     * Construct the URI to retrieve the movie details
     *
     * @param movieId    The movie id to load details for
     * @param detailPath Parameter to tag on if we want to load trailers/reviews
     * @return uri to load the movie detail
     */
    private String getTheMovieDBApiMovieDetailUri(String movieId, String detailPath) {
        Uri builtUri;
        if (detailPath == null) {
            builtUri = Uri.parse(MOVIE_DB_API_URL).buildUpon()
                    .appendPath(movieId)
                    .appendQueryParameter(MOVIE_DB_PARAM_API_KEY, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .build();
        } else {
            builtUri = Uri.parse(MOVIE_DB_API_URL).buildUpon()
                    .appendPath(movieId)
                    .appendPath(detailPath)
                    .appendQueryParameter(MOVIE_DB_PARAM_API_KEY, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .build();
        }

        return builtUri.toString();
    }

    /**
     * Parse out raw JSON data from The Movie DB api into usable Movie detail items
     *
     * @param moviesDetailJsonStr The raw JSON string returned from the api
     */
    private ContentValues getMovieDetailsContentValuesFromJson(String moviesDetailJsonStr,
                                                               ContentValues contentValues) {

        // Convert raw json string to JSON Object
        JSONObject moviesListJSONObj;

        try {
            moviesListJSONObj = new JSONObject(moviesDetailJsonStr);
            contentValues = parseJSONIntoMovieDetailContentValue(moviesListJSONObj, contentValues);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return contentValues;
    }

    /**
     * Parse the json result item into contentValues Object
     *
     * @param movieJSONObj JSONObject of a particular movie object from the results list
     * @return contentValues created from the JSON data
     */
    private ContentValues parseJSONIntoMovieDetailContentValue(JSONObject movieJSONObj,
                                                               ContentValues contentValues) {
        int mId = Utility.getJSONIntValue(movieJSONObj, MOVIE_DB_KEY_ID);
        String movieHomepage = Utility.getJSONStringValue(movieJSONObj, "homepage");
        String movieImdbId = Utility.getJSONStringValue(movieJSONObj, "imdb_id");
        String moviePopularity = Utility.getJSONStringValue(movieJSONObj, "popularity");
        List<String> movieProductionCompanies = Utility.getJSONListValue(movieJSONObj, "production_companies", MOVIE_DB_KEY_NAME);
        String sMovieProductionCompanies = TextUtils.join(", ", movieProductionCompanies);
        Double movieRuntime = Utility.getJSONDoubleValue(movieJSONObj, "runtime");
        int movieVoteCount = Utility.getJSONIntValue(movieJSONObj, "vote_count");
        List<String> movieGenres = Utility.getJSONListValue(movieJSONObj, "genres", MOVIE_DB_KEY_NAME);
        String sGenres = TextUtils.join(", ", movieGenres);

        contentValues.put(MoviesContract.MovieEntry.COLUMN_MOVIE_ID, mId);
        contentValues.put(MoviesContract.MovieEntry.COLUMN_HOMEPAGE, movieHomepage);
        contentValues.put(MoviesContract.MovieEntry.COLUMN_IMDB_ID, movieImdbId);
        contentValues.put(MoviesContract.MovieEntry.COLUMN_POPULARITY, moviePopularity);
        contentValues.put(MoviesContract.MovieEntry.COLUMN_PRODUCTION_COMPANIES, sMovieProductionCompanies);
        contentValues.put(MoviesContract.MovieEntry.COLUMN_RUNTIME, movieRuntime);
        contentValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_COUNT, movieVoteCount);
        contentValues.put(MoviesContract.MovieEntry.COLUMN_GENRES, sGenres);

        return contentValues;
    }

    /**
     * Update the movie record with the additional details, trailers and review data
     *
     * @param contentValues The content values populated with the movie detail updates
     */
    private void populateMovieDetails(ContentValues contentValues) {
        // update database
        Uri updateUri = MoviesContract.MovieEntry.buildMovieWithMovieIdUri(
                contentValues.getAsString(MoviesContract.MovieEntry.COLUMN_MOVIE_ID));

        String sMoviesByIdSelection = MoviesContract.MovieListsEntry.COLUMN_MOVIE_ID + " = ? ";

        int updated = this.getContentResolver().update(
                updateUri,
                contentValues,
                sMoviesByIdSelection,
                new String[]{MoviesContract.MovieEntry.getMovieIdFromUri(mUri)}
        );

        Log.d(LOG_TAG, "MovieDB Detail update Complete. " + updated + " updated");

    }
}
