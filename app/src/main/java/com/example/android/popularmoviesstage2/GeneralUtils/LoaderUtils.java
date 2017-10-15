package com.example.android.popularmoviesstage2.GeneralUtils;

import android.database.Cursor;

/**
 * Loader utilities. Constants and Methods
 */

public final class LoaderUtils {

    // Loader IDs
    public static final int MAIN_SEARCH_LOADER = 20;
    public static final int DETAILS_SEARCH_LOADER = 58;
    public static final int CAST_SEARCH_LOADER = 90;
    public static final int TRAILERS_SEARCH_LOADER = 30;
    public static final int FAVORITE_MOVIES_LOADER = 60;
    public static final int FAVORITE_MOVIES_LOADER_BY_ID = 35;
    public static final int REVIEWS_LOADER = 100;

    /**
     * Retrieves a String in the given column from a cursor
     * @param cursor The Cursor with data
     * @param colName The name of the column that contains the String
     *
     * @return The String in the given cursor column
     */
    public static String getStringFromCursor(Cursor cursor, String colName) {
        return cursor.getString(cursor.getColumnIndex(colName));
    }

}
