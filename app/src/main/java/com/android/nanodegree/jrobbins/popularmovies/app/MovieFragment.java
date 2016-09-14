package com.android.nanodegree.jrobbins.popularmovies.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Copied structure from Sunshine App example, Fragment to create Grid list of movie titles
 */
public class MovieFragment extends Fragment {

    private MovieAdapter mMovieAdapter;
    private ArrayList<Movie> movieList = new ArrayList<>();
    private String mSortBy = "";
    private static final String MOVIE_LIST_KEY = "movieList";
    private static final String MOVIE_LIST_INTENT = "movieVOIntent";

    private static final String LOG_TAG = MovieFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if(savedInstanceState != null && !savedInstanceState.containsKey(MOVIE_LIST_KEY)) {
            // Retrieve previous movie list
            movieList = savedInstanceState.getParcelableArrayList(MOVIE_LIST_KEY);
            // Store users sort preference as the default sort order
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            mSortBy = sharedPref.getString(getString(R.string.pref_sort_by_key), getString(R.string.pref_sort_by_popular));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Create instance of our custom ArrayAdapter
        mMovieAdapter = new MovieAdapter(getActivity(), movieList);

        // Get reference to our grid list and apply our adapter
        GridView movieGridList = (GridView) rootView.findViewById(R.id.gridview_movies);
        movieGridList.setAdapter(mMovieAdapter);

        // Set the item click listener to pass an intent to the detail view
        movieGridList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie selectedMovie = mMovieAdapter.getItem(position);
                //Setup explicit intent and pass our Movie parcelable to our detail activity
                Intent intentDetail = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(MOVIE_LIST_INTENT, selectedMovie);
                startActivity(intentDetail);
            }
        });
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //Store our current movie list
        outState.putParcelableArrayList(MOVIE_LIST_KEY, movieList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check to see if our shared preference sort order has changed from what the list was previously sorted on
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortBy = sharedPref.getString(getString(R.string.pref_sort_by_key), getString(R.string.pref_sort_by_popular));
        if (!sortBy.equalsIgnoreCase(mSortBy)) {
            mSortBy = sortBy;
            // Our sort has changed, so do a fresh look up from the api
            updateMovies();
        }
    }

    /**
     *
     */
    private void updateMovies()
    {
        // If we don't have a conneciton, show message to user.
        if (isOnline() && isNetworkAvailable()) {
            FetchMovieDataTask movieDataTask = new FetchMovieDataTask();
            movieDataTask.execute(mSortBy);
        }
        else
        {
            Toast.makeText(getContext(), R.string.no_wifi_message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Checking Network is Connected - make sure to setup the android.permission.ACCESS_NETWORK_STATE
     * permission, to verify network availability: https://guides.codepath.com/android/Sending-and-Managing-Network-Requests
     * @return true if we have a connection
     */
    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    /**
     * Checking the Internet is Connected -To verify if the device is actually connected to the internet,
     * we can use the following method of pinging the Google DNS servers to check for the expected exit value.
     * @return true if we get a response
     */
    private Boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Custom Async Task used to retrieve data from movie db api
     */
    public class FetchMovieDataTask extends AsyncTask<String, Void, List<Movie>> {
        private final String LOG_TAG = FetchMovieDataTask.class.getSimpleName();

        ProgressDialog mProgress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mProgress = new ProgressDialog(getActivity());
            mProgress.setTitle(getString(R.string.loading));
            mProgress.setMessage(getString(R.string.loading_movie_list));
            mProgress.setIndeterminate(false);
            mProgress.setCancelable(true);
            mProgress.show();
        }

        /**
         * Establish background thread to load data from movie db
         * @param params In this case, only single parameter passed, used to modify api list.
         * @return List of Movie objects
         */
        @Override
        protected List<Movie> doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;


            // Will contain the raw JSON response as a string.
            String movieDataJsonStr = null;

            try {
                // Create the request to TheMovieDB API, and open the connection
                String baseUrl = MovieDataParser.getMovieListRequestUrl(params[0]);
                Log.d(LOG_TAG,baseUrl);

                URL url = new URL(baseUrl);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod(MovieDataParser.REQUEST_METHOD_GET);
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Add newline to assist with easier debugging
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                movieDataJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the data, no point to parse it.
                return null;
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

            Log.d(LOG_TAG, movieDataJsonStr);
            // Send JSON data to the parser function and return movie list
            MovieDataParser movieDataParser = new MovieDataParser();
            return movieDataParser.getMovieDataFromJson(movieDataJsonStr);
        }

        @Override
        protected void onPostExecute(List<Movie> movieData) {
            super.onPostExecute(movieData);

            mProgress.dismiss();

            if (movieData != null) {
                //Reset our adapter with the new movie data
                mMovieAdapter.clear();
                mMovieAdapter.addAll(movieData);

                // Convert the List<Movie> to an ArrayList<Movie> so we can save an instance of the list
                Movie[] aMovies = movieData.toArray(new Movie[movieData.size()]);
                movieList = new ArrayList<>(Arrays.asList(aMovies));
            }
        }
    }
}
