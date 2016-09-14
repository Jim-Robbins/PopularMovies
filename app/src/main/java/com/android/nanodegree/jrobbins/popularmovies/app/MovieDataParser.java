package com.android.nanodegree.jrobbins.popularmovies.app;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Class specifically designed to parse the results from The Movie Database API
 *
 * Created by jim.robbins on 9/8/16.
 */

class MovieDataParser {
    private static final String LOG_TAG = MovieDataParser.class.getSimpleName();

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

    public static String REQUEST_METHOD_GET = "GET";

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

    public static String getMovieListRequestUrl(String sortByPath)
    {
        Uri builtUri = Uri.parse(MOVIE_DB_API_URL).buildUpon()
                .appendPath(sortByPath)
                .appendQueryParameter(MOVIE_DB_PARAM_API_KEY, BuildConfig.THE_MOVIE_DB_API_KEY)
                .build();
        return builtUri.toString();
    }

    public static String getMoviePosterUrl(String size, String posterPath) {

        Uri builtUri = Uri.parse(MOVIE_DB_IMG_URL).buildUpon()
                .appendPath(size)
                .build();
        return  builtUri.toString() + posterPath;
    }
    /**
     * Parse out raw JSON data from The Movie DB api into usable Movie items
     * @param moviesListJsonStr The raw JSON string returned from the api
     * @return a List of type Movie
     */
    public List<Movie> getMovieDataFromJson(String moviesListJsonStr) {

        // Convert raw json string to JSON Object
        JSONObject moviesListJSONObj;

        try {
            moviesListJSONObj = new JSONObject(moviesListJsonStr);

            // Check that we have a proper JSON Object and a results key
            if (!moviesListJSONObj.isNull(MOVIE_DB_KEY_RESULTS))
            {
                //Create our array list and populate it
                return populateMovieList(moviesListJSONObj.getJSONArray(MOVIE_DB_KEY_RESULTS));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Create a List populated with Movie types created from JSON object data
     * @param jsonArray The JSON array containing the list of movie results
     * @return List of type Movie
     */
    private ArrayList<Movie> populateMovieList(JSONArray jsonArray) {

        JSONObject moviesJson;
        ArrayList<Movie> movieArrayList = new ArrayList<>(jsonArray.length());

        // Process each result in json array, decode and convert to business object
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                moviesJson = jsonArray.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }

            Movie movie = parseJSONIntoObject(moviesJson);
            if (movie != null) {
                Log.v(LOG_TAG, movie.getTitle());
                movieArrayList.add(movie);
            }
        }

        return movieArrayList;
    }

    /**
     * Parse the json result item into a Movie
     * @param movieJSONObj  JSONObject of a particular movie object from the results list
     * @return Movie object created from the JSON data
     */
    private Movie parseJSONIntoObject(JSONObject movieJSONObj)
    {
        int mId = getJSONIntValue(movieJSONObj, MOVIE_DB_KEY_ID);
        String mTitle = getJSONStringValue(movieJSONObj, MOVIE_DB_KEY_TITLE);
        String mPoster = getJSONStringValue(movieJSONObj, MOVIE_DB_KEY_POSTER_PATH);
        String mOverview = getJSONStringValue(movieJSONObj, MOVIE_DB_KEY_OVERVIEW);
        String mReleaseDate = getJSONStringValue(movieJSONObj, MOVIE_DB_KEY_RELEASE_DATE);
        Double mVoteAvg = getJSONDoubleValue(movieJSONObj, MOVIE_DB_KEY_VOTE_AVG);
        String mBackdropPath = getJSONStringValue(movieJSONObj, MOVIE_DB_KEY_BACKDROP_PATH);
        boolean mVideo = getJSONBoolValue(movieJSONObj, MOVIE_DB_KEY_VIDEO);
        List<String> mGenreIds = getJSONListValue(movieJSONObj, MOVIE_DB_KEY_GENRE_IDS);

        return new Movie(mId, mTitle, mPoster, mOverview, mReleaseDate, mVoteAvg, mBackdropPath, mVideo, mGenreIds);
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
