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
 * Class specifically designed to parse the results from The Movie Database API
 *
 * Created by jim.robbins on 9/8/16.
 */

public class MovieDataService extends IntentService {
    private static final String LOG_TAG = MovieDataService.class.getSimpleName();

    private static String MOVIE_DB_API_URL = "http://api.themoviedb.org/3/movie";
    private static String MOVIE_DB_IMG_URL = "http://image.tmdb.org/t/p/";
    private static String MOVIE_DB_PARAM_API_KEY = "api_key";

    public static String MOVIE_DB_DETAIL_VIDEOS = "videos";
    public static String MOVIE_DB_DETAIL_REVIEWS = "reviews";

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

    static final public String API_RESULT_SUCCESS = "MovieDataService.REQUEST_PROCESSED";
    static final public String API_RESULT_FAIL = "MovieDataService.REQUEST_FAILED";

    public MovieDataService() {
        super(LOG_TAG);
    }

    private Uri mUri;

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
        String dataType = new MoviesProvider().getType(mUri);
        if(dataType.equalsIgnoreCase(MoviesContract.MovieEntry.CONTENT_TYPE)) {
            getMoviesListFromApi();
        } else {
            getMovieDetailsFromApi();
        }

    }

    private void getMoviesListFromApi()
    {
        String listQuery = MoviesContract.MovieEntry.getListTypeFromUri(mUri);
        Log.d(LOG_TAG, "getMoviesListFromApi:"+ listQuery);
        String baseUrl = MovieDataService.getTheMovieDBApiMovieListUri(listQuery);

        String movieDataJsonStr = getJSONDataFromApiCall(baseUrl);

        getMoviesListDataFromJson(movieDataJsonStr);
        sendLocalBroadcastResult();
    }

    private void getMovieDetailsFromApi()
    {
        ContentValues contentValues = new ContentValues();

        // Get movie id from the uri
        String movieId = MoviesContract.MovieEntry.getMovieIdFromUri(mUri);
        Log.d(LOG_TAG, "getMovieDetailsFromApi:"+ movieId);

        // Get list of movie trailers
        String baseUrl = MovieDataService.getTheMovieDBApiMovieDetailUri(movieId, MOVIE_DB_DETAIL_VIDEOS);
        String movieDataJsonStr = getJSONDataFromApiCall(baseUrl);
        if (!TextUtils.isEmpty(movieDataJsonStr)) {
            contentValues.put(MoviesContract.MovieEntry.COLUMN_TRAILERS, movieDataJsonStr);
        }

        // Get list of movie reviews
        baseUrl = MovieDataService.getTheMovieDBApiMovieDetailUri(movieId, MOVIE_DB_DETAIL_REVIEWS);
        movieDataJsonStr = getJSONDataFromApiCall(baseUrl);
        if (!TextUtils.isEmpty(movieDataJsonStr)) {
            contentValues.put(MoviesContract.MovieEntry.COLUMN_REVIEWS, movieDataJsonStr);
        }

        // Get movie details from API
        baseUrl = MovieDataService.getTheMovieDBApiMovieDetailUri(movieId, null);
        movieDataJsonStr = getJSONDataFromApiCall(baseUrl);
        getMovieDetailsDataFromJson(movieDataJsonStr, contentValues);

        sendLocalBroadcastResult();
    }

    private String getJSONDataFromApiCall(String baseUrl)
    {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String movieDataJsonStr = null;

        try {
            // Create the request to TheMovieDB API, and open the connection
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

    public void sendLocalBroadcastResult() {
        Intent intent = new Intent(API_RESULT_SUCCESS);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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

    public static String getTheMovieDBApiMovieDetailUri(String movieId, String detailPath)
    {
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
     * Parse out raw JSON data from The Movie DB api into usable Movie items
     * @param moviesListJsonStr The raw JSON string returned from the api
     * @return a List of type Movie
     */
    public void getMoviesListDataFromJson(String moviesListJsonStr) {

        // Convert raw json string to JSON Object
        JSONObject moviesListJSONObj;

        try {
            moviesListJSONObj = new JSONObject(moviesListJsonStr);

            // Check that we have a proper JSON Object and a results key
            if (!moviesListJSONObj.isNull(MOVIE_DB_KEY_RESULTS))
            {
                //Create our array list and populate it
                populateMoviesTable(moviesListJSONObj.getJSONArray(MOVIE_DB_KEY_RESULTS));
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
    private void populateMoviesTable(JSONArray jsonArray) {

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

            ContentValues movieValues = parseJSONIntoMoviesContentValues(moviesJson);
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
    private ContentValues parseJSONIntoMoviesContentValues(JSONObject movieJSONObj)
    {
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
     * Parse out raw JSON data from The Movie DB api into usable Movie detail items
     * @param moviesDetailJsonStr The raw JSON string returned from the api
     */
    public void getMovieDetailsDataFromJson(String moviesDetailJsonStr, ContentValues contentValues) {

        // Convert raw json string to JSON Object
        JSONObject moviesListJSONObj;

        try {
            moviesListJSONObj = new JSONObject(moviesDetailJsonStr);
            populateMovieDetails(moviesListJSONObj, contentValues);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update a movie entry populated with Movie details created from JSON object data
     * @param moviesJson The JSONObject containing the list of movie detail results
     * @return List of type Movie
     */
    private void populateMovieDetails(JSONObject moviesJson, ContentValues contentValues) {

        ContentValues movieValues = parseJSONIntoMovieDetailContentValue(moviesJson, contentValues);

        // update database
        Uri updateUri = MoviesContract.MovieEntry.buildMovieWithMovieIdUri(
                movieValues.getAsString(MoviesContract.MovieEntry.COLUMN_MOVIE_ID));

        String sMoviesByIdSelection = MoviesContract.MovieListsEntry.COLUMN_MOVIE_ID + " = ? ";

        int updated = this.getContentResolver().update(
                updateUri,
                movieValues,
                sMoviesByIdSelection,
                new String[]{MoviesContract.MovieEntry.getMovieIdFromUri(mUri)}
            );

        Log.d(LOG_TAG, "MovieDB Detail update Complete. " + updated + " Inserted");
    }

    /**
     * Parse the json result item into contentValues Object
     * @param movieJSONObj  JSONObject of a particular movie object from the results list
     * @return contentValues created from the JSON data
     */
    private ContentValues parseJSONIntoMovieDetailContentValue(JSONObject movieJSONObj, ContentValues contentValues)
    {
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
}
