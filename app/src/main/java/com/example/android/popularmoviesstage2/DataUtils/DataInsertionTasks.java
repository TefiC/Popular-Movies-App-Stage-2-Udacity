package com.example.android.popularmoviesstage2.DataUtils;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.example.android.popularmoviesstage2.Movie;

/**
 * Tasks to insert data into the database using a service
 */

public class DataInsertionTasks {

    public static final String ACTION_INSERT_FAVORITE = "insert-favorite-movie";
    public static final String ACTION_REMOVE_FAVORITE = "remove-favorite-movie";

    public static void executeTask(Context context, String action, Movie movieSelected, Bitmap bitmap) {

//        Log.v("DB", "INSIDE TASK");

        switch (action) {
            case ACTION_INSERT_FAVORITE:
                insertMovieToFavoritesDB(context, movieSelected, bitmap);
                break;
            case ACTION_REMOVE_FAVORITE:
                removeMovieFromFavoritesDB(context, movieSelected);
                break;
        }
    }

    /**
     * Insert the movie selected to the "Favorites" table in the database
     */
    public static void insertMovieToFavoritesDB(Context context, Movie movieSelected, Bitmap bitmap) {

        ContentValues cv = new ContentValues();

        //TODO: FAKE VALUES, PLACEHOLDERS WHILE IN THE PROCESS OF BUILDING METHODS TO FETCH DATA FROM DIFFERENT URLs
        String trailers = "wjfiwoejfwofj";
        String reviews = "sodkoadkasp";

        // Set movie data in the database
        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_MOVIEDB_ID, Integer.toString(movieSelected.getMovieId()));
        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_TITLE, movieSelected.getMovieTitle());

        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_RELEASE_DATE, movieSelected.getMovieReleaseDate());
        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_VOTE_AVERAGE, movieSelected.getMovieVoteAverage());

        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_PLOT, movieSelected.getMoviePlot());
        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_LANGUAGE, movieSelected.getMovieLanguage());
        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_RUNTIME, movieSelected.getMovieRuntime());

        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_CAST, movieSelected.getMovieCast().toString());
        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_REVIEWS, reviews);
        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_IS_FOR_ADULTS, movieSelected.getIsMovieFavorite());
        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_BACKDROP, movieSelected.getMovieBackdropPath());

        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_TRAILERS_THUMBNAILS, trailers);

        Uri uri = MoviesDBContract.FavoriteMoviesEntry.CONTENT_URI.buildUpon()
                .appendPath(Integer.toString(movieSelected.getMovieId()))
                .build();

        Uri insertResult = context.getContentResolver().insert(uri, cv);

        if (insertResult != null) {
            movieSelected.setIsMovieFavorite(true);
            //TODO: HANDLE USER INTERFACE IN DETAILS ACTIVITY
//            Log.v("DB", "MOVIE INSERTED");
            // Save image to internal storage and insert the poster to the database
            FavoritesUtils.saveImageToInternalStorage(bitmap, Integer.toString(movieSelected.getMovieId()),
                    FavoritesUtils.IMAGE_TYPE_POSTER, context);
        } else {
//            Log.v("DB", "MOVIE NOT INSERTED");
        }


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
            //TODO: HANDLE USER INTERFACE IN DETAILS ACTIVITY
//            Log.v("DB", "MOVIE REMOVED");}
        }
    }
}
