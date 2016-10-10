package com.android.nanodegree.jrobbins.popularmovies.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.android.nanodegree.jrobbins.popularmovies.app.R;

public class Utility {

    public static String getPreferredMovieList(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sort_by_key),
                context.getString(R.string.pref_sort_by_popular));
    }
}
