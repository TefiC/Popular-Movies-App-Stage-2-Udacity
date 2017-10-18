package com.example.android.popularmoviesstage2.MovieData;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents a movie review with an author and the review itself
 */

public class MovieReview implements Parcelable {


    /*
     * Fields
     */


    private String reviewAuthor;
    private String reviewText;


    /*
     * Constructors
     */


    public MovieReview(String reviewAuthor, String reviewText) {
        this.reviewAuthor = reviewAuthor;
        this.reviewText = reviewText;
    }

    private MovieReview(Parcel in) {
        reviewAuthor = in.readString();
        reviewText = in.readString();
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
        parcel.writeString(reviewAuthor);
        parcel.writeString(reviewText);
    }

    public static Parcelable.Creator<MovieReview> CREATOR = new Parcelable.Creator<MovieReview>() {

        @Override
        public MovieReview createFromParcel(Parcel parcel) {
            return new MovieReview(parcel);
        }

        @Override
        public MovieReview[] newArray(int i) {
            return new MovieReview[i];
        }
    };


    /*
     * Getters
     */


    public String getReviewAuthor() { return reviewAuthor; }
    public String getReviewText() { return reviewText; }


    /*
     * Setters
     */


    public void setReviewAuthor(String reviewAuthor) { this.reviewAuthor = reviewAuthor; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }
}
