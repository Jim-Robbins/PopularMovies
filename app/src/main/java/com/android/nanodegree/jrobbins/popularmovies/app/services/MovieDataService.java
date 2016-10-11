package com.android.nanodegree.jrobbins.popularmovies.app.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.android.nanodegree.jrobbins.popularmovies.app.BuildConfig;
import com.android.nanodegree.jrobbins.popularmovies.app.data.MoviesContract;
import com.android.nanodegree.jrobbins.popularmovies.app.models.Movie;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Class specifically designed to parse the results from The Movie Database API
 *
 * Created by jim.robbins on 9/8/16.
 */

public class MovieDataService extends IntentService {
    private static final String LOG_TAG = MovieDataService.class.getSimpleName();
    public static final String LIST_QUERY_EXTRA = "lqe";

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

    private String MOVIE_DB_KEY_RESULTS = "results";
    private String MOVIE_DB_KEY_ID = "id";
    private String MOVIE_DB_KEY_TITLE = "title";
    private String MOVIE_DB_KEY_POSTER_PATH = "poster_path";
    private String MOVIE_DB_KEY_OVERVIEW = "overview";
    private String MOVIE_DB_KEY_RELEASE_DATE = "release_date";
    private String MOVIE_DB_KEY_VOTE_AVG = "vote_average";
    private String MOVIE_DB_KEY_BACKDROP_PATH = "backdrop_path";
    private String MOVIE_DB_KEY_VIDEO = "video";
    private String MOVIE_DB_KEY_GENRE_IDS = "genre_ids";

    public MovieDataService() {
        super(LOG_TAG);
    }

    private List<Integer> mMovieIds;

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "onHandleIntent");
        String listQuery = intent.getStringExtra(LIST_QUERY_EXTRA);

        Log.d(LOG_TAG, "onHandleIntent:"+ listQuery);

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String movieDataJsonStr = null;

        try {
            // Create the request to TheMovieDB API, and open the connection
            String baseUrl = MovieDataService.getTheMovieDBApiMovieListUri(listQuery);
            Log.d(LOG_TAG,baseUrl);

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
                return;
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
                return;
            }
            movieDataJsonStr = buffer.toString();
            getMovieDataFromJson(movieDataJsonStr);
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
        return;
    }

    /**
     *  Construct the URL for TheMoiveDB query
     *  Possible parameters are avaiable at TMDB's API page, at
     *  https://www.themoviedb.org/documentation/api
     * @param sortByPath
     * @return
     */
    public static String getTheMovieDBApiMovieListUri(String sortByPath)
    {
        Uri builtUri = Uri.parse(MOVIE_DB_API_URL).buildUpon()
                .appendPath(sortByPath)
                .appendQueryParameter(MOVIE_DB_PARAM_API_KEY, BuildConfig.THE_MOVIE_DB_API_KEY)
                .build();
        return builtUri.toString();
    }

    public static String getTheMovieDBApiPosterUri(String size, String posterPath) {

        Uri builtUri = Uri.parse(MOVIE_DB_IMG_URL).buildUpon()
                .appendPath(size)
                .build();
        return  builtUri.toString() + posterPath;
    }

    public Cursor getAllEntries() {
        Uri allMoviesUri = MoviesContract.MovieEntry.buildMoviesUri();
        String[] MOVIES_COLUMNS = {
                MoviesContract.MovieEntry.COLUMN_MOVIE_ID
        };

        Cursor cursor = this.getContentResolver().query(
                allMoviesUri,
                MOVIES_COLUMNS,
                null,
                null,
                null
        );

        return cursor;
    }

    /**
     * Parse out raw JSON data from The Movie DB api into usable Movie items
     * @param moviesListJsonStr The raw JSON string returned from the api
     * @return a List of type Movie
     */
    public void getMovieDataFromJson(String moviesListJsonStr) {

        // Convert raw json string to JSON Object
        JSONObject moviesListJSONObj;
        mMovieIds = new ArrayList<Integer>();

        try {
            moviesListJSONObj = new JSONObject(moviesListJsonStr);

            // Check that we have a proper JSON Object and a results key
            if (!moviesListJSONObj.isNull(MOVIE_DB_KEY_RESULTS))
            {
                //Create our array list and populate it
                populateMovieDB(moviesListJSONObj.getJSONArray(MOVIE_DB_KEY_RESULTS));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a List populated with Movie types created from JSON object data
     * @param jsonArray The JSON array containing the list of movie results
     * @return List of type Movie
     */
    private void populateMovieDB(JSONArray jsonArray) {

        JSONObject moviesJson;
        //ArrayList<Movie> movieArrayList = new ArrayList<>(jsonArray.length());

        // Insert the new weather information into the database
        Vector<ContentValues> cVVector = new Vector<ContentValues>(jsonArray.length());

        // Process each result in json array, decode and convert to business object
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                moviesJson = jsonArray.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }

            ContentValues movieValues = parseJSONIntoObject(moviesJson);
            cVVector.add(movieValues);
        }

        int inserted = 0;
        // add to database
        if ( cVVector.size() > 0 ) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            Uri insertUri = MoviesContract.MovieEntry.buildMoviesWithListTypeUri(Utility.getPreferredMovieList(this));
            this.getContentResolver().bulkInsert(insertUri, cvArray);
        }

        Log.d(LOG_TAG, "MovieDB Service Complete. " + cVVector.size() + " Inserted");
    }

    /**
     * Parse the json result item into contentValues Object
     * @param movieJSONObj  JSONObject of a particular movie object from the results list
     * @return contentValues created from the JSON data
     */
    private ContentValues parseJSONIntoObject(JSONObject movieJSONObj)
    {
        int mId = getJSONIntValue(movieJSONObj, MOVIE_DB_KEY_ID);
        mMovieIds.add(mId);
        String mTitle = getJSONStringValue(movieJSONObj, MOVIE_DB_KEY_TITLE);
        String mPoster = getJSONStringValue(movieJSONObj, MOVIE_DB_KEY_POSTER_PATH);
        String mOverview = getJSONStringValue(movieJSONObj, MOVIE_DB_KEY_OVERVIEW);
        String mReleaseDate = getJSONStringValue(movieJSONObj, MOVIE_DB_KEY_RELEASE_DATE);
        Double mVoteAvg = getJSONDoubleValue(movieJSONObj, MOVIE_DB_KEY_VOTE_AVG);
        String mBackdropPath = getJSONStringValue(movieJSONObj, MOVIE_DB_KEY_BACKDROP_PATH);
        List<String> mGenreIds = getJSONListValue(movieJSONObj, MOVIE_DB_KEY_GENRE_IDS);
        String sGenreIds = TextUtils.join(",", mGenreIds);

        ContentValues contentValues = new ContentValues();
        contentValues.put(MoviesContract.MovieEntry.COLUMN_MOVIE_ID, mId);
        contentValues.put(MoviesContract.MovieEntry.COLUMN_TITLE, mTitle);
        contentValues.put(MoviesContract.MovieEntry.COLUMN_POSTER, mPoster);
        contentValues.put(MoviesContract.MovieEntry.COLUMN_OVERVIEW, mOverview);
        contentValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE, mReleaseDate);
        contentValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_AVG, mVoteAvg);
        contentValues.put(MoviesContract.MovieEntry.COLUMN_BACKDROP_PATH, mBackdropPath);
        contentValues.put(MoviesContract.MovieEntry.COLUMN_GENRE_IDS, sGenreIds);

        return contentValues;
    }

    /**
     * Helper class to safely grab a string value from JSON data, even if the key is invalid
     * @param jsonObject The data object we want to look for the key in
     * @param key The key string to look for in the object
     * @return String value from JSON key
     */
    private String getJSONStringValue(JSONObject jsonObject, String key) {
        String value = "";
        try {
            if (!jsonObject.isNull(key)) {
                value = jsonObject.getString(key);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return value;
    }

    /**
     * Helper class to safely grab a boolean value from JSON data, even if the key is invalid
     * @param jsonObject The data object we want to look for the key in
     * @param key The key string to look for in the object
     * @return boolean value from JSON key
     */
    private boolean getJSONBoolValue(JSONObject jsonObject, String key) {
        boolean value = false;
        try {
            if (!jsonObject.isNull(key)) {
                value = jsonObject.getBoolean(key);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return value;
    }

    /**
     * Helper class to safely grab an int value from JSON data, even if the key is invalid
     * @param jsonObject The data object we want to look for the key in
     * @param key The key string to look for in the object
     * @return int value from JSON key
     */
    private int getJSONIntValue(JSONObject jsonObject, String key) {
        int value = 0;
        try {
            if (!jsonObject.isNull(key)) {
                value = jsonObject.getInt(key);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return value;
    }

    /**
     * Helper class to safely grab a double value from JSON data, even if the key is invalid
     * @param jsonObject The data object we want to look for the key in
     * @param key The key string to look for in the object
     * @return Double value from JSON key
     */
    private Double getJSONDoubleValue(JSONObject jsonObject, String key) {
        Double value = 0.0;
        try {
            if (!jsonObject.isNull(key)) {
                value = jsonObject.getDouble(key);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return value;
    }

    /**
     * Helper class to safely grab JSON Array data, and covert it to a List\<String\>
     * @param jsonObject The data object we want to look for the key in
     * @param key The key string to look for in the object
     * @return List\<String\> value from JSONArray
     */
    private List<String> getJSONListValue(JSONObject jsonObject, String key) {
        List<String> listdata = new ArrayList<>();

        try {
            if (!jsonObject.isNull(key)) {
                JSONArray jArray = jsonObject.getJSONArray(key);
                for (int i = 0; i < jArray.length(); i++) {
                    try {
                        listdata.add(jArray.getString(i));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return listdata;
    }
}
