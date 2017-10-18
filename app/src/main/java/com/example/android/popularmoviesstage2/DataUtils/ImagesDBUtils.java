package com.example.android.popularmoviesstage2.DataUtils;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.example.android.popularmoviesstage2.Activities.DetailsActivity;
import com.example.android.popularmoviesstage2.GeneralUtils.LoaderUtils;
import com.example.android.popularmoviesstage2.MovieData.Movie;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.example.android.popularmoviesstage2.DataUtils.FavoritesUtils.CHARACTER_TO_SEPARATE_THUMBNAILS;
import static com.example.android.popularmoviesstage2.DataUtils.FavoritesUtils.CHARACTER_TO_SEPARATE_THUMBNAIL_TAG;
import static com.example.android.popularmoviesstage2.DataUtils.FavoritesUtils.IMAGE_TYPE_BACKDROP;
import static com.example.android.popularmoviesstage2.DataUtils.FavoritesUtils.IMAGE_TYPE_POSTER;
import static com.example.android.popularmoviesstage2.DataUtils.FavoritesUtils.IMAGE_TYPE_TRAILER_THUMBNAIL;

/**
 * Methods to insert, remove and update images in the database
 */

public class ImagesDBUtils {

    /*
     * Constants
     */

    // Directory constants
    private static final String DIRECTORY_POSTERS = "postersDir";
    private static final String DIRECTORY_BACKDROPS = "backdropsDir";
    private static final String DIRECTORY_THUMBNAILS = "thumbnailDir";

    /*
     * Methods
     */

    // Methods to save images to internal storage ==================================================

    /**
     * Saves all movie images to internal storage and updates its path in the database
     *
     * @param context       The context of the activity that called this method
     * @param movieSelected The movie selected by the user
     */
    public static void saveAllMovieImages(Context context, Movie movieSelected) {

        Log.v("DB SAVE POSTER",  movieSelected.getMoviePosterPath());
        Log.v("DB SAVE bACKDROP",  movieSelected.getMovieBackdropPath());

        // Save poster to internal storage and update path in database
        saveMovieImage(FavoritesUtils.IMAGE_TYPE_POSTER,
                context,
                movieSelected,
                movieSelected.getMoviePosterPath());

        // Save backdrop to internal storage and update path in database
        saveMovieImage(FavoritesUtils.IMAGE_TYPE_BACKDROP,
                context,
                movieSelected,
                DetailsActivity.createFullBackdropPath(context, movieSelected.getMovieBackdropPath()));

        //Save trailer thumbnails
        saveMovieThumbnails(context, movieSelected);
    }

    /**
     * Saves an individual movie image to internal storage and updates its path in the database
     *
     * @param imageType     The type of image to be saved.
     *                      Either FavoritesUtils.IMAGE_TYPE_POSTER or FavoritesUtils.IMAGE_TYPE_BACKDROP
     * @param context       The context of the activity that called this method
     * @param movieSelected The movie selected by the user
     * @param loadPath      The path used to load the image with Picasso
     */
    private static void saveMovieImage(String imageType, Context context, Movie movieSelected, String loadPath) {

        Log.v("DB", loadPath);

        Bitmap bitmap = getImageBitmapFromPicasso(context, loadPath);

        ImagesDBUtils.saveImageToInternalStorage(bitmap,
                Integer.toString(movieSelected.getMovieId()),
                imageType,
                context,
                movieSelected,
                -1);
    }

    /**
     * Saves all the movie thumbnails to the database and updates their corresponding path
     *
     * @param context       The context of the activity that called this method
     * @param movieSelected The movie selected by the user
     */
    private static void saveMovieThumbnails(Context context, Movie movieSelected) {

        Log.v("DB THUMBNAILS ",  "THUMBNAILS" + movieSelected.getMovieTrailersThumbnails());

        Log.v("DB THUMBNAILS", movieSelected.getMovieTrailersThumbnails().toString());

        // Load and Save each thumbnail to internal storage
        for (int i = 0; i < movieSelected.getMovieTrailersThumbnails().size(); i++) {

            Bitmap bitmapTrailer = getImageBitmapFromPicasso(context,
                    movieSelected.getMovieTrailersThumbnails().get(i).getThumbnailPath());

            ImagesDBUtils.saveImageToInternalStorage(bitmapTrailer,
                    Integer.toString(movieSelected.getMovieId()),
                    FavoritesUtils.IMAGE_TYPE_TRAILER_THUMBNAIL,
                    context,
                    movieSelected,
                    i);
        }
    }

    /**
     * Loads a bitmap using the Picasso library, retrieving images from local storage
     *
     * @param context The context of the activity that called this method
     * @param path    The URL used to load the image with Picasso
     * @return A Bitmap representing the image retrieved
     */
    private static Bitmap getImageBitmapFromPicasso(Context context, String path) {

        Bitmap bitmap = null;

        try {
            bitmap = Picasso.with(context)
                    .load(path)
                    .get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * Saves an image to the internal storage with a custom path that
     * depends on the imageType being saved. Calls a method that
     * saves the image path to the database.
     *
     * @param bitmapImage    A Bitmap of the poster image
     * @param movieDBId      The MovieDB's ID
     * @param imageType      The type of image resource to save, in order to determine
     *                       the correct directory. Either "poster", "backdrop" or "trailerThumbnail"
     * @param context        The context of the activity that invoked this method
     * @param thumbnailIndex If the imageType is FavoritesUtils.IMAGE_TYPE_TRAILER_THUMBNAIL, an integer
     *                       describing the thumbnail's position in the thumbnails list stored in
     *                       the movie object. Else, -1
     * @return The directory's absolute path
     */
    public static String saveImageToInternalStorage(Bitmap bitmapImage, String movieDBId,
                                                    String imageType, Context context, Movie movieSelected,
                                                    int thumbnailIndex) {

        File directory = createFileDirectory(context, imageType);

        if (directory != null) {

            // Save image to internal storage
            File bitmapPath = createBitmapPath(directory, imageType, movieDBId, thumbnailIndex);
            createBitmapOutputStream(bitmapImage, bitmapPath);

            // Update image path in database
            saveImagePathToDatabase(context, directory.getAbsolutePath(), imageType, movieSelected, thumbnailIndex);
        }

        return directory.getAbsolutePath();
    }

    /**
     * Create a file directory for the corresponding image type
     *
     * @param context   The context of the activity that called this method
     * @param imageType The type of image being saved
     * @return A File directory
     */
    private static File createFileDirectory(Context context, String imageType) {

        ContextWrapper cw = new ContextWrapper(context);

        File directory = null;

        switch (imageType) {
            case IMAGE_TYPE_POSTER:
                directory = cw.getDir(DIRECTORY_POSTERS, Context.MODE_PRIVATE);
                break;
            case IMAGE_TYPE_BACKDROP:
                directory = cw.getDir(DIRECTORY_BACKDROPS, Context.MODE_PRIVATE);
                break;
            case IMAGE_TYPE_TRAILER_THUMBNAIL:
                directory = cw.getDir(DIRECTORY_THUMBNAILS, Context.MODE_PRIVATE);
                break;
            default:
                break;
        }

        return directory;
    }

    /**
     * Create a bitmap path for the corresponding image
     *
     * @param directory      Directory for the file
     * @param imageType      Type of image to be saved
     * @param movieDBId      MovieDB Id of the movie selected
     * @param thumbnailIndex If the imageType is a thumbnail, the thumbnail position in the movie
     *                       object ArrayList
     * @return The path where the image will be saved to in internal storage
     */
    private static File createBitmapPath(File directory, String imageType, String movieDBId, int thumbnailIndex) {
        File bitmapPath;

        if (imageType.equals(IMAGE_TYPE_TRAILER_THUMBNAIL)) {
            bitmapPath = new File(directory, movieDBId + thumbnailIndex + ".jpg");
        } else {
            // Create imageDir
            bitmapPath = new File(directory, movieDBId + ".jpg");
        }

        return bitmapPath;
    }

    /**
     * Saves the bitmap passed as argument to the path passed as argument in internal storage
     *
     * @param bitmapImage A Bitmap representing the image being saved
     * @param bitmapPath  The path where the bitmap must be saved
     */
    private static void createBitmapOutputStream(Bitmap bitmapImage, File bitmapPath) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(bitmapPath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);

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

    // Methods to update image path in database ====================================================

    /**
     * Saves the image path to the database
     *
     * @param context           The context of the activity that called this method
     * @param imageInternalPath Path where the image was stored in internal storage
     * @param imageType         Type of image to be stored
     * @param movieSelected     Movie Selected by the user
     * @param thumbnailIndex    If the imageType is thumbnail, the thumbnail's position in the
     *                          movie object's trailer thumbnails ArrayList
     */
    public static void saveImagePathToDatabase(Context context, String imageInternalPath,
                                               String imageType, Movie movieSelected,
                                               int thumbnailIndex) {

        // Generate Uri to query
        Uri uri = DBServiceTasks.buildMovieSelectedDBUri(movieSelected);

        // Generate content values
        ContentValues cv = generateContentValuesForImagePath(context,
                imageInternalPath, imageType, movieSelected, thumbnailIndex, uri);

        // Update the image path in database
        context.getContentResolver().update(uri, cv, null, null);
    }

    /**
     * Generates the content values with the image internal storage path
     *
     * @param context           The context of the activity that called this method
     * @param imageInternalPath The image path in internal storage
     * @param imageType         The type of image being updated
     * @param movieSelected     The movie selected by the user
     * @param thumbnailIndex    If the image type is thumbnail, the position of the thumbnail in the
     *                          movie object's trailer thumbnail ArrayList
     * @param uri               The Uri to find the movie in the database
     * @return Content values with the database column that corresponds to the image type
     * and the path that will be updated
     */
    private static ContentValues generateContentValuesForImagePath(Context context, String imageInternalPath,
                                                                   String imageType, Movie movieSelected,
                                                                   int thumbnailIndex, Uri uri) {
        ContentValues cv = new ContentValues();

        switch (imageType) {
            case IMAGE_TYPE_POSTER:
                cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_DATABASE_POSTER_PATH, imageInternalPath);
                break;
            case IMAGE_TYPE_BACKDROP:
                cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_DATABASE_BACKDROP_PATH, imageInternalPath);
                break;
            case IMAGE_TYPE_TRAILER_THUMBNAIL:

                String newInternetThumbnails = formatInternetTrailersForDB(context, movieSelected, uri, thumbnailIndex);

                String newDatabaseThumbnailsString = formatThumbnailsForDB(context,
                        imageInternalPath,
                        movieSelected,
                        uri,
                        thumbnailIndex);

                cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_DATABASE_TRAILERS_THUMBNAILS,
                        newDatabaseThumbnailsString);

                cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_TRAILERS_THUMBNAILS,
                        newInternetThumbnails);

                break;
        }

        return cv;
    }

    private static String formatInternetTrailersForDB(Context context, Movie movieSelected,
                                                     Uri uri, int thumbnailIndex) {
        Cursor previousInternetThumbnails = context.getContentResolver().query(uri,
                new String[]{MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_TRAILERS_THUMBNAILS},
                null,
                null,
                null);

        if (previousInternetThumbnails.moveToFirst()) {
            String previousString = LoaderUtils.getStringFromCursor(previousInternetThumbnails,
                    MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_TRAILERS_THUMBNAILS);

            previousInternetThumbnails.close();

            return createNewInternetThumbnails(previousString,
                    movieSelected.getMovieTrailersThumbnails().get(thumbnailIndex).getThumbnailPath(),
                    movieSelected.getMovieTrailersThumbnails().get(thumbnailIndex).getThumbnailTag());
        } else {
            return null;
        }
    }

    private static String createNewInternetThumbnails(String previousThumbnails, String trailerPath, String trailerKey) {
        return previousThumbnails +
                trailerPath +
                CHARACTER_TO_SEPARATE_THUMBNAIL_TAG +
                trailerKey +
                CHARACTER_TO_SEPARATE_THUMBNAILS;
    }

    /**
     * Creates a new thumbnails string by querying the previous one from the database,
     * and appending the new thumbnail path to the existing string in the database
     *
     * @param context           The context of the activity that called this method
     * @param imageInternalPath The path to the image stored in internal storage
     * @param movieSelected     The movie selected by the user
     * @param uri               The Uri to access the movie in the database
     * @param thumbnailIndex    The thumbnail's position in the movie object's thumbnails ArrayList
     * @return A updated thumbnails String with the new thumbnail data
     */
    private static String formatThumbnailsForDB(Context context, String imageInternalPath, Movie movieSelected,
                                                Uri uri, int thumbnailIndex) {

        Cursor previousThumbnails = context.getContentResolver().query(uri,
                new String[]{MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_DATABASE_TRAILERS_THUMBNAILS},
                null,
                null,
                null);

        if (previousThumbnails.moveToFirst()) {
            String previousString = LoaderUtils.getStringFromCursor(previousThumbnails,
                    MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_DATABASE_TRAILERS_THUMBNAILS);

            previousThumbnails.close();

            return createNewThumbnails(previousString, imageInternalPath, movieSelected, thumbnailIndex);
        } else {
            return null;
        }
    }

    /**
     * Creates a new thumbnails String from the previous thumbnails String by adding the new
     * thumbnail's data to the end of the existing String
     *
     * @param previousString    Previous thumbnails String
     * @param imageInternalPath Path to internal storage where the image is saved
     * @param movieSelected     Movie selected by the user
     * @param thumbnailIndex    The thumbnail's position in the movie object's thumbnails ArrayList
     * @return An updated String that includes the new thumbnail data
     */
    private static String createNewThumbnails(String previousString, String imageInternalPath,
                                              Movie movieSelected, int thumbnailIndex) {
        String newThumbnails = "";

        newThumbnails += previousString +
                imageInternalPath +
                CHARACTER_TO_SEPARATE_THUMBNAIL_TAG +
                movieSelected.getMovieTrailersThumbnails().get(thumbnailIndex).getThumbnailTag() +
                CHARACTER_TO_SEPARATE_THUMBNAILS;

        return newThumbnails;
    }

    // Methods to load images from internal storage ================================================

    /**
     * Loads an image from local storage
     *
     * @param path           Full path to the image
     * @param movieDBId      MovieDBId of the movie selected
     * @param imageType      The type of image that will be retrieved from local storage.
     *                       Either IMAGE_TYPE_TRAILER_THUMBNAIL, IMAGE_TYPE_TRAILER_BACKDROP,
     *                       IMAGE_TYPE_TRAILER_POSTER.
     * @param thumbnailIndex If the image type is thumbnail, the thumbnail's position
     *                       in the movie object's thumbnails ArrayList. Else -1.
     * @return A Bitmap of the corresponding image
     */
    public static Bitmap loadImageFromStorage(String path, String movieDBId, String imageType, int thumbnailIndex) {

        Bitmap bitmap = null;

        try {
            File f;

            if (imageType.equals(FavoritesUtils.IMAGE_TYPE_TRAILER_THUMBNAIL)) {
                f = new File(path, movieDBId + thumbnailIndex + ".jpg");
            } else {
                f = new File(path, movieDBId + ".jpg");
            }

            bitmap = BitmapFactory.decodeStream(new FileInputStream(f));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    /**
     * Removes an image from internal storage
     *
     * @param context        The context of the activity that called this method
     * @param path           The image path (directory)
     * @param movieDBId      The ID of the movie selected by the user
     * @param imageType      The type of image to be deleted (poster, backdrop, trailer thumbnail)
     * @param thumbnailIndex The position of the trailer thumbnail in the movie object's ArrayList
     * @return true if the file is deleted, false otherwise.
     */
    public static boolean deleteImageFromStorage(Context context, String path, String movieDBId, String imageType, int thumbnailIndex) {

        File file;

        switch (imageType) {
            case FavoritesUtils.IMAGE_TYPE_BACKDROP:
            case FavoritesUtils.IMAGE_TYPE_POSTER:
                file = new File(path, movieDBId + ".jpg");
                break;
            case FavoritesUtils.IMAGE_TYPE_TRAILER_THUMBNAIL:
                file = new File(path, movieDBId + thumbnailIndex + ".jpg");
                break;
            default:
                throw new UnsupportedOperationException("Unknown image type: " + imageType);
        }

        return file.delete();
    }

    /**
     * Removes trailer thumbnails from internal storage
     *
     * @param context   The context of the activity that called this method
     * @param movieDBId The MovieDB Id of the movie selected by the user
     * @return true if all the thumbnails were removed correctly. False otherwise
     */
    public static boolean deleteThumbnailsFromStorage(Context context, String movieDBId) {

        boolean allRemoved = true;

        String[] trailersArray = DetailsActivity.queryTrailersArray(context);

        for (int i = 0; i < trailersArray.length; i++) {

            String trailerPath = trailersArray[i].split(FavoritesUtils.CHARACTER_TO_SEPARATE_THUMBNAIL_TAG)[0];

            boolean removed = ImagesDBUtils.deleteImageFromStorage(context,
                    trailerPath,
                    movieDBId,
                    FavoritesUtils.IMAGE_TYPE_TRAILER_THUMBNAIL,
                    i);

            if (!removed) {
                allRemoved = false;
            }
        }
        return allRemoved;
    }

    /**
     * Loads a movie image (poster or backdrop) from internal storage
     *
     * @param context            Context of the activity that called this method
     * @param movieSelected      Movie object selected by the user
     * @param databaseColumnName Database column name with the corresponding image category
     * @param imageType          The corresponding image type
     * @return The image as a Bitmap
     */
    public static Bitmap loadImageFromDatabase(Context context, Movie movieSelected, String databaseColumnName, String imageType) {

        String[] projection = {
                databaseColumnName
        };

        Cursor pathCursor = context.getContentResolver().query(
                DetailsActivity.mMovieSelectedUri,
                projection,
                null,
                null,
                MoviesDBContract.FavoriteMoviesEntry._ID);

        pathCursor.moveToFirst();

        String imagePath = pathCursor.getString(pathCursor.getColumnIndex(databaseColumnName));

        pathCursor.close();

        return ImagesDBUtils.loadImageFromStorage(
                imagePath,
                Integer.toString(movieSelected.getMovieId()),
                imageType,
                -1);
    }
}
