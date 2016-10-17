package com.android.nanodegree.jrobbins.popularmovies.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.android.nanodegree.jrobbins.popularmovies.app.R;
import com.android.nanodegree.jrobbins.popularmovies.app.services.MovieDataService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Utility {

    public static String getPreferredMovieList(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sort_by_key),
                context.getString(R.string.pref_sort_by_popular));
    }

    /**
     * Checking Network is Connected - make sure to setup the android.permission.ACCESS_NETWORK_STATE
     * permission, to verify network availability: https://guides.codepath.com/android/Sending-and-Managing-Network-Requests
     * @return true if we have a connection
     */
    public static Boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    /**
     * Checking the Internet is Connected -To verify if the device is actually connected to the internet,
     * we can use the following method of pinging the Google DNS servers to check for the expected exit value.
     * @return true if we get a response
     */
    public static Boolean isOnline() {
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
     * Helper class to safely grab a string value from JSON data, even if the key is invalid
     * @param jsonObject The data object we want to look for the key in
     * @param key The key string to look for in the object
     * @return String value from JSON key
     */
    public static String getJSONStringValue(JSONObject jsonObject, String key) {
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
     * Helper class to safely grab a double value from JSON data, even if the key is invalid
     * @param jsonObject The data object we want to look for the key in
     * @param key The key string to look for in the object
     * @return Double value from JSON key
     */
    public static Double getJSONDoubleValue(JSONObject jsonObject, String key) {
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
    public static List<String> getJSONListValue(JSONObject jsonObject, String key, String listKey) {
        List<String> listdata = new ArrayList<>();

        try {
            if (!jsonObject.isNull(key)) {
                JSONArray jArray = jsonObject.getJSONArray(key);
                for (int i = 0; i < jArray.length(); i++) {
                    try {
                        JSONObject listObject = jArray.getJSONObject(i);
                        listdata.add(getJSONStringValue(listObject, listKey));
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


    /**
     * Helper class to safely grab an int value from JSON data, even if the key is invalid
     * @param jsonObject The data object we want to look for the key in
     * @param key The key string to look for in the object
     * @return int value from JSON key
     */
    public static int getJSONIntValue(JSONObject jsonObject, String key) {
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
     * Helper class to safely grab a boolean value from JSON data, even if the key is invalid
     * @param jsonObject The data object we want to look for the key in
     * @param key The key string to look for in the object
     * @return boolean value from JSON key
     */
    private static boolean getJSONBoolValue(JSONObject jsonObject, String key) {
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
}
