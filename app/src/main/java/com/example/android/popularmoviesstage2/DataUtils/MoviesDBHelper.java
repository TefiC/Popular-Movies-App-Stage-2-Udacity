package com.example.android.popularmoviesstage2.DataUtils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Creates and upgrades the Movies SQLite database
 */

public class MoviesDBHelper extends SQLiteOpenHelper {

    /*
     * Constants
     */

    private static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 1;

    /*
     * Constructor
     */

    public MoviesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /*
     * Methods
     */

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_FAVORITE_MOVIES_TABLE =
                "CREATE TABLE " + MoviesDBContract.FavoriteMoviesEntry.TABLE_NAME + " (" +
                              MoviesDBContract.FavoriteMoviesEntry._ID                              + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                              MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_MOVIEDB_ID           + " INT NOT NULL, "  +
                              MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_TITLE                + " TEXT NOT NULL, " +
                              MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_RELEASE_DATE         + " TEXT NOT NULL, " +
                              MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_POSTER_PATH          + " TEXT, "          +
                              MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_VOTE_AVERAGE         + " TEXT NOT NULL, " +
                              MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_PLOT                 + " TEXT NOT NULL, " +
                              MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_LANGUAGE             + " TEXT NOT NULL, " +
                              MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_RUNTIME              + " TEXT NOT NULL, " +
                              MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_CAST                 + " TEXT NOT NULL, " +
                              MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_REVIEWS_AUTHOR       + " TEXT, "          +
                              MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_REVIEWS_TEXT         + " TEXT, "          +
                              MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_IS_FOR_ADULTS        + " TEXT NOT NULL, " +
                              MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_BACKDROP             + " TEXT NOT NULL, " +
                              MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_TRAILERS_THUMBNAILS  + " TEXT NOT NULL, " +
                              " UNIQUE (" + MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_MOVIEDB_ID + ") ON CONFLICT REPLACE" +
                              ");";
        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITE_MOVIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " +
                MoviesDBContract.FavoriteMoviesEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
