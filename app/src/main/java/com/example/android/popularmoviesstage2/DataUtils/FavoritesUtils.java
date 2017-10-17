package com.example.android.popularmoviesstage2.DataUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.example.android.popularmoviesstage2.Activities.MainActivity;
import com.example.android.popularmoviesstage2.GeneralUtils.FavoritesDataIntentService;
import com.example.android.popularmoviesstage2.MovieData.Movie;
import com.example.android.popularmoviesstage2.R;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility method to handle everything related to adding, maintaining and retrieving
 * the user's favorite movies
 */

public class FavoritesUtils {


    /*
     * Constants
     */


    // Image types
    public static final String IMAGE_TYPE_POSTER = "poster";
    public static final String IMAGE_TYPE_BACKDROP = "backdrop";
    public static final String IMAGE_TYPE_TRAILER_THUMBNAIL = "trailerThumbnail";

    // Characters to separate data
    public static final String CHARACTER_TO_SEPARATE_THUMBNAIL_TAG = ">";
    public static final String CHARACTER_TO_SEPARATE_THUMBNAILS = "==>";

    public static final String SHARED_PREFERENCES_FAVORITES_STRING = "favoriteMoviesPreferences";


    /*
     * Database methods
     */

    // Database methods ============================================================================

    /**
     * Checks if the movieDBID passed as argument is in the database
     *
     * @param context Context of the activity where this method was called
     * @param movieDBId The movie ID from The Movie Database API
     * @return True if the movieDBID is in the database and False otherwise.
     */
    public static boolean checkIfMovieIsFavorite(Context context, String movieDBId) {

        Uri uri = MoviesDBContract.FavoriteMoviesEntry.CONTENT_URI.buildUpon()
                .appendPath(movieDBId).build();

        Cursor cursor = context.getContentResolver().query(uri,
                null,
                "movieDBId=?",
                new String[]{movieDBId},
                null);

        if(cursor == null) {
            return false;
        } else if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }

        cursor.close();
        return true;
    }

    // Methods to add movie to favorites ====================

    /**
     * Adds movie poster to the database
     *
     * @param context Context of the activity that called this method
     * @param movieSelected Movie selected by the user as a favorite
     */
    public static void addFavoriteToDatabase(Context context, Movie movieSelected) {

        // Create intent
        Intent intent = new Intent(context, FavoritesDataIntentService.class);
        intent.setAction(DBServiceTasks.ACTION_INSERT_FAVORITE);
        intent.putExtra(MainActivity.INTENT_MOVIE_OBJECT_KEY, movieSelected);

        // Start service
        context.startService(intent);
    }

    // Methods to toggle movie from SharedPreference ===============================================

    /**
     * Removes the movie's MovieDB Id from Shared Preferences
     * @param context The context of the activity that called this method
     * @param movieSelected The movie selected by the user
     *
     * @return True if the movie Id was successfully removed from the String set
     */
    public static boolean removeFavoriteFromSharedPreferences(Context context, Movie movieSelected) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        return sharedPreferences.getStringSet(SHARED_PREFERENCES_FAVORITES_STRING, null)
                .remove(Integer.toString(movieSelected.getMovieId()));
    }

    /**
     * Adds a favorite movie's MovieDB Id to SharedPreferences by adding it to a
     * String Set that is stored under the key "favoriteMoviesPreferences".
     * If this key doesn't exist in SharedPreferences, it creates one and adds
     * the movie ID.
     *
     * @param context       The Context of the activity that called this method
     * @param movieSelected The Movie selected by the user
     * @return True if the movie was added correctly. False otherwise.
     */
    public static boolean addFavoriteToSharedPreferences(Context context, Movie movieSelected) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // If there is a favorites list in SharedPreferences, add the movie's MovieDB ID
        // Else, create a String Set under the key SHARED_PREFERENCES_FAVORITES_STRING
        // and add the movie's ID.
        if (sharedPreferences.contains(SHARED_PREFERENCES_FAVORITES_STRING)) {
            return sharedPreferences.getStringSet(SHARED_PREFERENCES_FAVORITES_STRING, null)
                    .add(Integer.toString(movieSelected.getMovieId()));

        } else {
            // Create a String Set and add the movie's ID
            SharedPreferences.Editor editor = sharedPreferences.edit();

            Set<String> stringSet = new HashSet<String>();
            stringSet.add(Integer.toString(movieSelected.getMovieId()));

            editor.putStringSet(SHARED_PREFERENCES_FAVORITES_STRING, stringSet);
            editor.apply();

            // Return if the String Set was correctly stored in SharedPreferences
            return sharedPreferences.contains(SHARED_PREFERENCES_FAVORITES_STRING);
        }
    }

    // User interaction methods ==================================================================

    /**
     * Creates and displays an alert dialog telling the user
     * he/she hasn't selected any favorite movies
     *
     * @param context Context of the Activity where the dialog is launched
     */
    public static void createNoFavoritesDialog(Context context) {

        //Create dialog builder with corresponding settings
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        // Set content
        builder.setTitle(context.getString(R.string.favorites_dialog_title))
                .setMessage(context.getString(R.string.favorites_dialog_message));
        // Set button
        builder.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        // Create dialog and display it to the user
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
