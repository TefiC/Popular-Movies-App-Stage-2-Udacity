package com.example.android.popularmoviesstage2.MovieData;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents a movie trailer thumbnail
 */

public class MovieTrailerThumbnail implements Parcelable {

    private String thumbnailPath;
    private String thumbnailTag;

    public MovieTrailerThumbnail(String thumbnailPath, String thumbnailTag) {
        this.thumbnailPath = thumbnailPath;
        this.thumbnailTag = thumbnailTag;
    }

    private MovieTrailerThumbnail(Parcel in) {
        thumbnailPath = in.readString();
        thumbnailTag = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(thumbnailPath);
        parcel.writeString(thumbnailTag);
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

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public String getThumbnailTag() {
        return thumbnailTag;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public void setThumbnailTag(String thumbnailTag) {
        this.thumbnailTag = thumbnailTag;
    }
}
