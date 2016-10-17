package com.android.nanodegree.jrobbins.popularmovies.app.models;

/**
 * Created by jim on 10/17/16.
 */

public class MovieReview {
    String id;
    String author;
    String content;

    public MovieReview(String id, String author, String content)
    {
        this.id = id;
        this.author = author;
        this.content = content;
    }

}
