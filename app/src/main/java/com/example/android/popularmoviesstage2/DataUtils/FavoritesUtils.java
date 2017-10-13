package com.example.android.popularmoviesstage2.DataUtils;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.example.android.popularmoviesstage2.Movie;
import com.example.android.popularmoviesstage2.R;
import com.example.android.popularmoviesstage2.utils.FavoritesDataIntentService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Utility method to handle everything related to adding, maintaining and retrieving
 * the user's favorite movies
 */

public class FavoritesUtils {

    public static final String IMAGE_TYPE_POSTER = "poster";
    public static final String IMAGE_TYPE_BACKDROP = "backdrop";
    public static final String IMAGE_TYPE_TRAILER_THUMBNAIL = "trailerThumbnail";

    // Database methods ==================================================================

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

        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }

        cursor.close();
        return true;
    }

    /**
     * Adds movie poster to the database
     *
     * @param context Context of the activity that called this method
     * @param movieSelected Movie selected by the user as a favorite
     */
    public static void addFavoriteToDatabase(Context context, Movie movieSelected) {
        Intent intent = new Intent(context, FavoritesDataIntentService.class);
        intent.setAction(DataInsertionTasks.ACTION_INSERT_FAVORITE);
        intent.putExtra("movieObject", movieSelected);
        context.startService(intent);
    }

    //    Based on https://stackoverflow.com/questions/17674634/saving-and-reading-bitmaps-images-from-internal-memory-in-android

    /**
     * Saves an image to the internal storage with a custom path that
     * depends on the imageType being saved. Calls a method that
     * saves the image path to the database.
     *
     * @param bitmapPoster A Bitmap of the poster image
     * @param movieDBId The MovieDB's ID
     * @param imageType The type of image resource to save, in order to determine
     *                  the correct directory. Either "poster", "backdrop" or "trailerThumbnail"
     * @param context The context of the activity that invoked this method
     *
     * @return The directory's absolute path
     */
    public static String saveImageToInternalStorage(Bitmap bitmapPoster, String movieDBId,
                                                     String imageType, Context context) {

        ContextWrapper cw = new ContextWrapper(context);

        File directory = null;

        switch (imageType) {
            case IMAGE_TYPE_POSTER:
                directory = cw.getDir("postersDir", Context.MODE_PRIVATE);
                break;
            case IMAGE_TYPE_BACKDROP:
                directory = cw.getDir("backdropsDir", Context.MODE_PRIVATE);
                break;
            case IMAGE_TYPE_TRAILER_THUMBNAIL:
                directory = cw.getDir("thumbnailDir", Context.MODE_PRIVATE);
                break;
            default:
                break;
        }

        if (directory != null) {
            // Create imageDir
            File posterPath = new File(directory, movieDBId + ".jpg");

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(posterPath);
                // Use the compress method on the BitMap object to write image to the OutputStream
                bitmapPoster.compress(Bitmap.CompressFormat.PNG, 100, fos);
                saveImagePathToDatabase(context, directory.getAbsolutePath(), movieDBId);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return directory.getAbsolutePath();
    }


    /**
     * Saves the image path to the database
     *
     * @param context The Context of the Activity that invoked this method
     * @param imageInternalPath Absolute path for the image without its ID
     * @param movieDBId The Movie's MovieDB Id
     */
    private static void saveImagePathToDatabase(Context context, String imageInternalPath, String movieDBId) {

        ContentValues cv = new ContentValues();

        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_POSTER_PATH, imageInternalPath);

        Uri uri = MoviesDBContract.FavoriteMoviesEntry.CONTENT_URI.buildUpon()
                .appendPath(movieDBId)
                .build();

        int updateResult = context.getContentResolver().update(uri, cv, null, null);

    }

    /**
     * Loads an image from local storage
     *
     * @param path Full path to the image
     * @param movieDBId MovieDBId of the movie selected
     *
     * @return A Bitmap of the corresponding image
     */
    public static Bitmap loadImageFromStorage(String path, String movieDBId) {

        Bitmap bitmap = null;

        try {
            File f = new File(path, movieDBId + ".jpg");
            bitmap = BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
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
