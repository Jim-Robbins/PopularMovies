package com.android.nanodegree.jrobbins.popularmovies.app.models;

import com.android.nanodegree.jrobbins.popularmovies.app.utils.Utility;

import org.json.JSONObject;

/**
 * Created by jim on 10/17/16.
 */

public class MovieReview {
    public String id;
    public String author;
    public String content;

    public MovieReview(JSONObject jsonObject) {
        this.id = Utility.getJSONStringValue(jsonObject, "id");
        this.author = Utility.getJSONStringValue(jsonObject, "author");
        this.content = Utility.getJSONStringValue(jsonObject, "content");
    }

}
