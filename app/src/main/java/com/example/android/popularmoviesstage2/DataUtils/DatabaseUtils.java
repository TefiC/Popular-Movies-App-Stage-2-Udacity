package com.example.android.popularmoviesstage2.DataUtils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Methods to test proper functionality of the database
 */

public class DatabaseUtils {

    private SQLiteDatabase mDB;

    public void testQuery(MoviesDBHelper dbHelper) {
        mDB = dbHelper.getReadableDatabase();
        Cursor cursor = mDB.query(MoviesDBContract.FavoriteMoviesEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);
        while(cursor.moveToNext()) {
            Log.v("DATABASE", cursor.getString(cursor.getColumnIndex(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_TITLE)));
        }
    }

    public long insertData(MoviesDBHelper dbHelper) {
        mDB = dbHelper.getReadableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_MOVIEDB_ID, "FIRST MOVIE");
        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_TITLE, "FIRST MOVIE");
        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_RELEASE_DATE, "FIRST MOVIE");
        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_POSTER_PATH, "FIRST MOVIE");
        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_VOTE_AVERAGE, "FIRST MOVIE");
        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_PLOT, "FIRST MOVIE");
        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_LANGUAGE, "FIRST MOVIE");
        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_RUNTIME, "FIRST MOVIE");
        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_CAST, "FIRST MOVIE");
        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_REVIEWS, "FIRST MOVIE");
        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_IS_FOR_ADULTS, "FIRST MOVIE");
        cv.put(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_BACKDROP, "FIRST MOVIE");

        return mDB.insert(MoviesDBContract.FavoriteMoviesEntry.TABLE_NAME, null, cv);

    }


}
