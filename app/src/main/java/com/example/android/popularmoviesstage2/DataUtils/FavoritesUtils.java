package com.example.android.popularmoviesstage2.DataUtils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

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
}
