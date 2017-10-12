package com.example.android.popularmoviesstage2;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Represents a Movie which contains a title, release date,
 * a reference to a poster path, a user vote average and plot.
 */

public class Movie implements Parcelable {

    /*
     * Fields
     */

    private int movieId;

    private String movieTitle;
    private String movieReleaseDate;
    private String moviePosterPath;

    private double movieVoteAverage;
    private String moviePlot;
    private String movieLanguage;

    private double movieRuntime;
    private ArrayList<String> movieCast;

    private ArrayList<MovieReview> movieReviews;
    private boolean isMoviewForAdults;

    private String movieBackdropPath;
    private String movieTrailersThumbnails;

    private boolean isFavorite = false;


    /*
     * Constructors
     */

    public Movie(int movieId, String movieTitle, String movieReleaseDate, String moviePoster,
                 double movieVoteAverage, String moviePlot,String movieLanguage, double movieRuntime,
                 ArrayList<String> movieCast, ArrayList<MovieReview> movieReviews, boolean isMoviewForAdults,
                 String movieBackdropPath, String movieTrailersThumbnails) {
        this.movieId = movieId;
        this.movieTitle = movieTitle;

        this.movieReleaseDate = movieReleaseDate;
        this.moviePosterPath = moviePoster;

        this.movieVoteAverage = movieVoteAverage;
        this.moviePlot = moviePlot;

        this.movieLanguage = movieLanguage;
        this.movieRuntime = movieRuntime;

        this.movieCast = movieCast;
        this.movieReviews = movieReviews;

        this.isMoviewForAdults = isMoviewForAdults;
        this.movieBackdropPath = movieBackdropPath;
        this.movieTrailersThumbnails = movieTrailersThumbnails;
    }


    private Movie(Parcel in) {
        movieId = in.readInt();
        movieTitle = in.readString();
        movieReleaseDate = in.readString();

        moviePosterPath = in.readString();
        movieVoteAverage = in.readDouble();
        moviePlot = in.readString();

        movieLanguage = in.readString();
        movieRuntime = in.readDouble();
        movieCast = in.createStringArrayList();

        movieReviews = in.createTypedArrayList(MovieReview.CREATOR);

        isMoviewForAdults = in.readInt() == 1;
        movieBackdropPath = in.readString();
        movieTrailersThumbnails = in.readString();

        isFavorite = in.readInt() == 1;
    }

    /*
     * Implementing parcelable
     */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(movieId);
        parcel.writeString(movieTitle);
        parcel.writeString(movieReleaseDate);

        parcel.writeString(moviePosterPath);
        parcel.writeDouble(movieVoteAverage);
        parcel.writeString(moviePlot);

        parcel.writeString(movieLanguage);
        parcel.writeDouble(movieRuntime);
        parcel.writeStringList(movieCast);

        parcel.writeTypedList(movieReviews);

        parcel.writeValue(isMoviewForAdults ? 1 : 0);
        parcel.writeString(movieBackdropPath);
        parcel.writeString(movieTrailersThumbnails);

        parcel.writeValue(isFavorite ? 1 : 0);
    }

    public static Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {

        @Override
        public Movie createFromParcel(Parcel parcel) {
            return new Movie(parcel);
        }

        @Override
        public Movie[] newArray(int i) {
            return new Movie[i];
        }
    };

    /*
     * Getters
     */

    public int getMovieId() { return this.movieId; }

    public String getMovieTitle() {
        return this.movieTitle;
    }

    public String getMovieReleaseDate() {
        return this.movieReleaseDate;
    }

    public String getMoviePosterPath() {
        return this.moviePosterPath;
    }

    public double getMovieVoteAverage() {
        return this.movieVoteAverage;
    }

    public String getMoviePlot() {
        return this.moviePlot;
    }

    public String getMovieLanguage() { return this.movieLanguage; }

    public double getMovieRuntime() { return this.movieRuntime; }

    public ArrayList<String> getMovieCast() { return this.movieCast; }

    public ArrayList<MovieReview> getMovieReviews() { return this.movieReviews; }

    public boolean getIsMovieForAdults() { return this.isMoviewForAdults; }

    public String getMovieBackdropPath() { return this.movieBackdropPath; }

    public boolean getIsMovieFavorite() { return this.isFavorite; }

    public String getMovieTrailersThumbnails() { return this.movieTrailersThumbnails; }

    /*
     * Setters
     */

    public void setMovieId(int id) { this.movieId = movieId; }

    public void setMovieTitle(String title) {
        this.movieTitle = title;
    }

    public void setMovieReleaseDate(String releaseDate) {
        this.movieReleaseDate = releaseDate;
    }

    public void setMoviePosterPath(String posterPath) {
        this.moviePosterPath = posterPath;
    }

    public void setMovieVoteAverage(double voteAverage) {
        this.movieVoteAverage = voteAverage;
    }

    public void setMoviePlot(String plot) {
        this.moviePlot = plot;
    }

    public void setMovieLanguage(String language) { this.movieLanguage = language; }

    public void setMovieRuntime(double runtime) { this.movieRuntime = runtime; }

    public void setMovieCast(ArrayList<String> cast) { this.movieCast = cast; }

    public void setMovieReviews(ArrayList<MovieReview> movieReviews) { this.movieReviews = movieReviews; }

    public void setIsMovieForAdults(boolean isMovieForAdults) { this.isMoviewForAdults = isMovieForAdults; }

    public void setMovieBackdropPath(String movieBackdropPath) { this.movieBackdropPath = movieBackdropPath; }

    public void setIsMovieFavorite(boolean isFavorite) { this.isFavorite = isFavorite; }

    public void setMovieTrailersThumbnails(String trailersThumbnails) { this.movieTrailersThumbnails = trailersThumbnails; }

}
