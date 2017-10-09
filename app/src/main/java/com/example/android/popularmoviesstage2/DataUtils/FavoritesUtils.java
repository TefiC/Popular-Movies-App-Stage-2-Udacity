package com.example.android.popularmoviesstage2.DataUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;

import com.example.android.popularmoviesstage2.R;

/**
 * Utility method to handle everything related to adding, maintaining and retrieving
 * the user's favorite movies
 */

public class FavoritesUtils {

    public static boolean checkIfMovieIsFavorite(Context context, String movieDBId) {

        Uri uri = MoviesDBContract.FavoriteMoviesEntry.CONTENT_URI.buildUpon()
                .appendPath(movieDBId).build();

        Cursor cursor = context.getContentResolver().query(uri,
                null,
                "movieDBId=?",
                new String[]{movieDBId},
                null);

        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }

        cursor.close();
        return true;
    }

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
