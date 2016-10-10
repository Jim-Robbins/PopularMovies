package com.android.nanodegree.jrobbins.popularmovies.app.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by jim.robbins on 9/8/16.
 * <p>
 * Used http://www.parcelabler.com/ to generate Parcelable implementation
 */

public class Movie implements Parcelable {
    private int id;
    private String title;
    private String poster;
    private String overview;
    private String releaseDate;
    private Double voteAvg;
    private String backdropPath;
    private Boolean video;
    private List<String> genreIds;

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getPosterPathStr() {
        return poster;
    }

    public String getOverview() {
        return overview;
    }

    public String getReleaseYear() {
        return releaseDate.substring(0, 4);
    }

    public Double getMovieRating() {
        return voteAvg;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public Boolean getVideo() {
        return video;
    }

    public List<String> getGenreIds() {
        return genreIds;
    }

    // Decodes movide json into business model object
    public Movie(int id, String title, String poster, String overview, String releaseDate, Double voteAvg, String backdropPath, boolean video, List<String> genreIds) {
        this.id = id;
        this.title = title;
        this.poster = poster;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.voteAvg = voteAvg;
        this.backdropPath = backdropPath;
        this.video = video;
        this.genreIds = genreIds;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.title);
        dest.writeString(this.poster);
        dest.writeString(this.overview);
        dest.writeString(this.releaseDate);
        dest.writeValue(this.voteAvg);
        dest.writeString(this.backdropPath);
        dest.writeValue(this.video);
        dest.writeStringList(this.genreIds);
    }

    protected Movie(Parcel in) {
        this.id = in.readInt();
        this.title = in.readString();
        this.poster = in.readString();
        this.overview = in.readString();
        this.releaseDate = in.readString();
        this.voteAvg = (Double) in.readValue(Double.class.getClassLoader());
        this.backdropPath = in.readString();
        this.video = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.genreIds = in.createStringArrayList();
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}