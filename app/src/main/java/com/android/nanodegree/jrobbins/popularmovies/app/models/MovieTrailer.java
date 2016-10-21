package com.android.nanodegree.jrobbins.popularmovies.app.models;

import com.android.nanodegree.jrobbins.popularmovies.app.utils.Utility;

import org.json.JSONObject;

/**
 * Created by jim on 10/17/16.
 */

public class MovieTrailer {
    public String id;
    public String key;
    public String name;
    public String site;

    public MovieTrailer(JSONObject jsonObject) {
        this.id = Utility.getJSONStringValue(jsonObject, "id");
        this.key = Utility.getJSONStringValue(jsonObject, "key");
        this.name = Utility.getJSONStringValue(jsonObject, "name");
        this.site = Utility.getJSONStringValue(jsonObject, "site");
    }

}
