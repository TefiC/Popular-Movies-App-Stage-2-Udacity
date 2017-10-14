package com.example.android.popularmoviesstage2.DataUtils;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.example.android.popularmoviesstage2.Activities.DetailsActivity;
import com.example.android.popularmoviesstage2.MovieData.Movie;
import com.example.android.popularmoviesstage2.MovieData.MovieReview;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Tasks to insert data into the database using a service
 */

public class DataTasks {

    public static final String ACTION_INSERT_FAVORITE = "insert-favorite-movie";
    public static final String ACTION_REMOVE_FAVORITE = "remove-favorite-movie";

    // Reviews
    public static final String CHARACTER_SEPARATING_REVIEWS_AUTHORS = ", ";
    public static final String CHARACTER_SEPARATING_REVIEWS_TEXT = "===>";

    // Cast
    public static final String CHARACTER_SEPARATING_CAST_MEMBERS = ", ";

    /**
     * Executes the Service's corresponding task
     *
     * @param context       The context of the activity that called this method
     * @param action        The action that the Service was called to perform
     * @param movieSelected The movie selected by the user
     */
    public static void executeTask(Context context, String action, Movie movieSelected) {

        switch (action) {
            case ACTION_INSERT_FAVORITE:
                insertMovieToFavoritesDB(context, movieSelected);
                break;
            case ACTION_REMOVE_FAVORITE:
                removeMovieFromFavoritesDB(context, movieSelected);
                break;
        }
    }

    /**
     * Insert the movie selected to the "Favorites" table in the database
     */
    public static void insertMovieToFavoritesDB(Context context, Movie movieSelected) {

        ContentValues cv = new ContentValues();

        // Set movie data in the database
        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_MOVIEDB_ID, Integer.toString(movieSelected.getMovieId()));
        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_TITLE, movieSelected.getMovieTitle());

        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_RELEASE_DATE, movieSelected.getMovieReleaseDate());
        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_VOTE_AVERAGE, movieSelected.getMovieVoteAverage());

        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_PLOT, movieSelected.getMoviePlot());
        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_LANGUAGE, movieSelected.getMovieLanguage());
        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_RUNTIME, movieSelected.getMovieRuntime());

        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_CAST, movieSelected.getMovieCast().toString());
        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_IS_FOR_ADULTS, movieSelected.getIsMovieFavorite());

        // Reviews
        String[] formattedReviews = formatReviewsForDB(movieSelected.getMovieReviews());
        String formattedAuthors = formattedReviews[0];
        String formattedReviewsText = formattedReviews[1];

        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_REVIEWS_AUTHOR, formattedAuthors);
        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_REVIEWS_TEXT, formattedReviewsText);

        //Images
        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_BACKDROP, movieSelected.getMovieBackdropPath());
        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_TRAILERS_THUMBNAILS, "");


        Uri uri = MoviesDBContract.FavoriteMoviesEntry.CONTENT_URI.buildUpon()
                .appendPath(Integer.toString(movieSelected.getMovieId()))
                .build();

        Uri insertResult = context.getContentResolver().insert(uri, cv);

        if (insertResult != null) {


            try {

                //Save poster
                Bitmap bitmapPoster = Picasso.with(context)
                        .load(movieSelected.getMoviePosterPath())
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .get();

                FavoritesUtils.saveImageToInternalStorage(bitmapPoster, Integer.toString(movieSelected.getMovieId()),
                        FavoritesUtils.IMAGE_TYPE_POSTER, context, movieSelected, 0);

                //Save backdrop
                Bitmap bitmapBackdrop = Picasso.with(context)
                        .load(DetailsActivity.MOVIEDB_POSTER_BASE_URL + DetailsActivity.BACKDROP_SIZE + movieSelected.getMovieBackdropPath())
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .get();

                FavoritesUtils.saveImageToInternalStorage(bitmapBackdrop, Integer.toString(movieSelected.getMovieId()),
                        FavoritesUtils.IMAGE_TYPE_BACKDROP, context, movieSelected, 0);

                //Save trailer thumbnails
                int i;
                for(i = 0; i < movieSelected.getMovieTrailersThumbnails().size(); i++) {
                    Log.v("INSERTING ", "INSERTING THUMBNAIL");
                    Bitmap bitmapTrailer = Picasso.with(context)
                            .load(movieSelected.getMovieTrailersThumbnails().get(i).getThumbnailPath())
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .get();
                    //Save thumbnails
                    FavoritesUtils.saveImageToInternalStorage(bitmapTrailer, Integer.toString(movieSelected.getMovieId()),
                            FavoritesUtils.IMAGE_TYPE_TRAILER_THUMBNAIL, context, movieSelected, i);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            Log.v(TAG, "Movie couldn't be inserted");
        }
    }

    /**
     * Format reviews to insert them to the database by converting them into
     * Strings and separating each one of the elements with a special character
     * to distinguish where they must be split.
     *
     * @param movieReviews An ArrayList of MovieReview objects
     * @return An array of two elements. The first one is the String that
     * contains the authors and the second elements in the String that
     * contains the reviews text.
     */
    private static String[] formatReviewsForDB(ArrayList<MovieReview> movieReviews) {

        String reviewsAuthor = "";
        String reviewsText = "";

        for (MovieReview review : movieReviews) {
            reviewsAuthor += review.getReviewAuthor() + CHARACTER_SEPARATING_REVIEWS_AUTHORS;
            reviewsText += review.getReviewText() + CHARACTER_SEPARATING_REVIEWS_TEXT;
        }

        String[] reviewsData = new String[2];
        reviewsData[0] = reviewsAuthor;
        reviewsData[1] = reviewsText;

        return reviewsData;
    }
    /**
     * Removes the movie selected from the "Favorites" table in the database
     */
    public static void removeMovieFromFavoritesDB(Context context, Movie movieSelected) {

        Uri uri = MoviesDBContract.FavoriteMoviesEntry.CONTENT_URI.buildUpon()
                .appendPath(Integer.toString(movieSelected.getMovieId())).build();

        int numDeleted = context.getContentResolver().delete(uri, "movieDBId=?", new String[]{"id"});

        if (numDeleted == 1) {
            movieSelected.setIsMovieFavorite(false);
        }
    }
}
