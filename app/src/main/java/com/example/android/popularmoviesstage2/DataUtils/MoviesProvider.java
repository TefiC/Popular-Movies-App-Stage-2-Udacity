package com.example.android.popularmoviesstage2.DataUtils;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * A Content Provider for Favorite movies and Watchlist movies
 */

public class MoviesProvider extends ContentProvider {

    private static final String TAG = MoviesProvider.class.getSimpleName();

    // Codes for the URI Matcher
    private static final int CODE_FAVORITE_MOVIES = 100;
    private static final int CODE_FAVORITE_MOVIE_WITH_ID = 101;

    //UriMatcher and Database helper
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoviesDBHelper mMoviesDBHelper;


    /**
     * Matches the different paths to their corresponding integers
     * @return A UriMatcher
     */
    public static UriMatcher buildUriMatcher() {

        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        //Match favorite movies
        uriMatcher.addURI(MoviesDBContract.CONTENT_AUTHORITY,
                MoviesDBContract.PATH_FAVORITE_MOVIES,
                CODE_FAVORITE_MOVIES);

        uriMatcher.addURI(MoviesDBContract.CONTENT_AUTHORITY,
                MoviesDBContract.PATH_FAVORITE_MOVIES + "/#",
                CODE_FAVORITE_MOVIE_WITH_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mMoviesDBHelper = new MoviesDBHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        final SQLiteDatabase db = mMoviesDBHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);

        Cursor returnCursor = null;

        switch (match) {

            case CODE_FAVORITE_MOVIES:
                returnCursor = db.query(MoviesDBContract.FavoriteMoviesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case CODE_FAVORITE_MOVIE_WITH_ID:
                String id = uri.getPathSegments().get(1);
                String mSelection = "movieDBId=?";
                String[] mSelectionArgs = new String[]{id};

                returnCursor = db.query(MoviesDBContract.FavoriteMoviesEntry.TABLE_NAME,
                        projection,
                        mSelection,
                        mSelectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }

        returnCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return returnCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = sUriMatcher.match(uri);

        switch (match) {
            case CODE_FAVORITE_MOVIES:
                return "vnd.android.cursor.dir"               + "/" +
                        MoviesDBContract.CONTENT_AUTHORITY    + "/" +
                        MoviesDBContract.PATH_FAVORITE_MOVIES;
            case CODE_FAVORITE_MOVIE_WITH_ID:
                return "vnd.android.cursor.item"               + "/" +
                        MoviesDBContract.CONTENT_AUTHORITY    + "/" +
                        MoviesDBContract.PATH_FAVORITE_MOVIES;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final SQLiteDatabase db = mMoviesDBHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        Uri returnUri;
        long _id;

        switch (match) {
            case CODE_FAVORITE_MOVIES:
                _id = db.insert(MoviesDBContract.FavoriteMoviesEntry.TABLE_NAME,
                        null,
                        contentValues);
                if (_id > 0 ) {
                    returnUri = ContentUris.withAppendedId(MoviesDBContract.FavoriteMoviesEntry.CONTENT_URI, _id);
                } else {
                    throw new android.database.SQLException("Failed to insert row: " + _id);
                }
                break;
            case CODE_FAVORITE_MOVIE_WITH_ID:

                _id = db.insert(MoviesDBContract.FavoriteMoviesEntry.TABLE_NAME,
                        null,
                        contentValues);

                if (_id > 0 ) {
                    returnUri = ContentUris.withAppendedId(MoviesDBContract.FavoriteMoviesEntry.CONTENT_URI, _id);
                } else {
                    throw new android.database.SQLException("Failed to insert row: " + _id);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mMoviesDBHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);

        int numOfMoviesDeleted;

        // To delete entire table
        if (null == selection) selection = "1";

        switch (match) {
            case CODE_FAVORITE_MOVIES:
                numOfMoviesDeleted = db.delete(MoviesDBContract.FavoriteMoviesEntry.TABLE_NAME,
                        selection,
                        selectionArgs);

            case CODE_FAVORITE_MOVIE_WITH_ID:
                String id = uri.getPathSegments().get(1);
                numOfMoviesDeleted = db.delete(MoviesDBContract.FavoriteMoviesEntry.TABLE_NAME,
                        "movieDBId" + " = ? ",
                        new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }

        if(numOfMoviesDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numOfMoviesDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        final SQLiteDatabase db = mMoviesDBHelper.getWritableDatabase();

        int moviesUpdated;
        int match = sUriMatcher.match(uri);

        switch (match) {
            case CODE_FAVORITE_MOVIE_WITH_ID:
                Log.v("DB", "FAVORITE MOVIE WITH ID ");
                String id = uri.getPathSegments().get(1);
                Log.v("DB", "FAVORITE MOVIE WITH ID " + id);
                moviesUpdated = db.update(MoviesDBContract.FavoriteMoviesEntry.TABLE_NAME,
                        contentValues,
                        "movieDBId=?",
                        new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if(moviesUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        Log.v("DB", "PATH UPDATED: " + moviesUpdated);

        return moviesUpdated;
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        mMoviesDBHelper.close();
        super.shutdown();
    }
}
