package com.example.android.popularmoviesstage2.MovieData;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents a movie trailer thumbnail with a Path and a Key
 */

public class MovieTrailerThumbnail implements Parcelable {


    /*
     * Fields
     */


    private String thumbnailPath;
    private String thumbnailKey;


    /*
     * Constructors
     */


    public MovieTrailerThumbnail(String thumbnailPath, String thumbnailKey) {
        this.thumbnailPath = thumbnailPath;
        this.thumbnailKey = thumbnailKey;
    }

    private MovieTrailerThumbnail(Parcel in) {
        thumbnailPath = in.readString();
        thumbnailKey = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }


    /*
     * Implementing Parcelable
     */


    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(thumbnailPath);
        parcel.writeString(thumbnailKey);
    }

    public static Parcelable.Creator<MovieTrailerThumbnail> CREATOR = new Parcelable.Creator<MovieTrailerThumbnail>() {

        @Override
        public MovieTrailerThumbnail createFromParcel(Parcel parcel) {
            return new MovieTrailerThumbnail(parcel);
        }

        @Override
        public MovieTrailerThumbnail[] newArray(int i) {
            return new MovieTrailerThumbnail[i];
        }
    };


    /*
     * Getters
     */


    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public String getThumbnailTag() {
        return thumbnailKey;
    }


    /*
     * Setters
     */


    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public void setThumbnailTag(String thumbnailTag) {
        this.thumbnailKey = thumbnailTag;
    }
}
