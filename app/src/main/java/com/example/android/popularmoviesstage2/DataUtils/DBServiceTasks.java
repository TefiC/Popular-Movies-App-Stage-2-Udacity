package com.example.android.popularmoviesstage2.DataUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.example.android.popularmoviesstage2.MovieData.Movie;
import com.example.android.popularmoviesstage2.MovieData.MovieReview;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;
import static com.example.android.popularmoviesstage2.DataUtils.ImagesDBUtils.deleteImageFromStorage;
import static com.example.android.popularmoviesstage2.GeneralUtils.LoaderUtils.getStringFromCursor;

/**
 * Tasks to insert data into the database using a service
 */

public class DBServiceTasks {


    /*
     * Constants
     */


    // Service Actions
    public static final String ACTION_INSERT_FAVORITE = "insert-favorite-movie";
    public static final String ACTION_REMOVE_FAVORITE = "remove-favorite-movie";

    // Reviews characters
    public static final String CHARACTER_SEPARATING_REVIEWS_AUTHORS = ", ";
    public static final String CHARACTER_SEPARATING_REVIEWS_TEXT = "===>";

    // Cast characters
    public static final String CHARACTER_SEPARATING_CAST_MEMBERS = ", ";


    /*
     * Methods
     */


    // Methods to execute DB tasks  ================================================================

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
    private static void insertMovieToFavoritesDB(Context context, Movie movieSelected) {

        // Create parameters to insert data to DB
        ContentValues cv = createMovieContentValuesForDB(movieSelected);
        Uri uri = buildMovieSelectedDBUri(movieSelected);

        // Insert initial movie data to the DB
        Uri insertResult = context.getContentResolver().insert(uri, cv);

        // If the movie was successfully inserted, save the images
        // (poster, backdrop and trailer thumbnails)to internal storage
        // and update their path in the database
        if (insertResult != null) {
            ImagesDBUtils.saveAllMovieImages(context, movieSelected);
        } else {
            Log.v(TAG, "Movie couldn't be inserted successfully");
        }
    }

    /**
     * Removes the movie selected from the "Favorites" table in the database and from internal storage
     */
    private static void removeMovieFromFavoritesDB(Context context, Movie movieSelected) {

        Uri uri = MoviesDBContract.FavoriteMoviesEntry.CONTENT_URI.buildUpon()
                .appendPath(Integer.toString(movieSelected.getMovieId()))
                .build();

        // Poster
        boolean posterDeleted = deleteImageFromStorage(context,
                getImagePathFromDB(context, uri, MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_DATABASE_POSTER_PATH),
                Integer.toString(movieSelected.getMovieId()),
                FavoritesUtils.IMAGE_TYPE_POSTER,
                -1);

        // Backdrop
        boolean backdropDeleted = deleteImageFromStorage(context,
                getImagePathFromDB(context, uri, MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_DATABASE_BACKDROP_PATH),
                Integer.toString(movieSelected.getMovieId()),
                FavoritesUtils.IMAGE_TYPE_BACKDROP,
                -1);

        // Thumbnails
        boolean thumbnailsDeleted = ImagesDBUtils.deleteThumbnailsFromStorage(context, Integer.toString(movieSelected.getMovieId()));

        int numDeleted = context.getContentResolver().delete(uri, "movieDBId=?", new String[]{"id"});

        if (numDeleted == 1) {
            movieSelected.setIsMovieFavorite(false);
        }
    }

    /**
     * Gets the path in the database to internal storage for an image resource
     *
     * @param context The context of the activity that called this method
     * @param uri The Uri to find an individual movie
     * @param columnName The name of the image resource column in the database
     *
     * @return The image path to internal storage
     */
    public static String getImagePathFromDB(Context context, Uri uri, String columnName) {
        Cursor imagePathCursor = context.getContentResolver().query(uri,
                new String[]{columnName},
                null,
                null,
                MoviesDBContract.FavoriteMoviesEntry._ID);

        imagePathCursor.moveToFirst();
        return getStringFromCursor(imagePathCursor, columnName);
    }

    /**
     * Creates a ContentValues instance that contains the data for the movie selected
     *
     * @param movieSelected The movie selected by the user
     *
     * @return ContentValues with the movie's data
     */
    private static ContentValues createMovieContentValuesForDB(Movie movieSelected) {

        ContentValues cv = new ContentValues();

        /*
        Add movie data to the content values
         */

        // Details
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

        // Images path placeholders
        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_POSTER_PATH, movieSelected.getMoviePosterPath());
        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_DATABASE_BACKDROP_PATH, "");

        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_BACKDROP, movieSelected.getMovieBackdropPath());
        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_DATABASE_BACKDROP_PATH, "");

        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_TRAILERS_THUMBNAILS, "");
        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_DATABASE_TRAILERS_THUMBNAILS, "");

        return cv;
    }

    /**
     * Build the Uri for database operations for the movie selected
     *
     * @param movieSelected The movie selected by the user
     *
     * @return The Uri to operate with for the movie selected
     */
    public static Uri buildMovieSelectedDBUri(Movie movieSelected) {
        return MoviesDBContract.FavoriteMoviesEntry.CONTENT_URI.buildUpon()
                .appendPath(Integer.toString(movieSelected.getMovieId()))
                .build();
    }

    // Methods to format data for DB  ==============================================================

    /**
     * Format reviews to insert them to the database by converting them into
     * Strings and separating each one of the elements with a special character
     * to distinguish where they must be split.
     *
     * @param movieReviews An ArrayList of MovieReview objects
     *
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
}
