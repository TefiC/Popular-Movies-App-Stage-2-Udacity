package com.example.android.popularmoviesstage2.DataUtils;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Database contract for the Movies Database
 */

public final class MoviesDBContract {

    /*
     * Constants
     */

    // Path constants
    public static final String CONTENT_AUTHORITY = "com.example.android.popularmoviesstage2";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Path to the favorite movies table
    public static final String PATH_FAVORITE_MOVIES = "favoriteMovies";

    /*
     * Constructor
     */

    private MoviesDBContract() {}

    /*
     * Tables
     */

    // Favorite movies table
    public static class FavoriteMoviesEntry implements BaseColumns {

        // Path to entire table
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_FAVORITE_MOVIES)
                .build();

        // Column names
        public static final String COLUMN_NAME_MOVIEDB_ID = "movieDBId";

        public static final String TABLE_NAME = "favoriteMovies";

        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_RELEASE_DATE = "releaseDate";
        public static final String COLUMN_NAME_POSTER_PATH = "posterPath";
        public static final String COLUMN_NAME_DATABASE_POSTER_PATH = "databasePosterPath";

        public static final String COLUMN_NAME_VOTE_AVERAGE = "voteAverage";
        public static final String COLUMN_NAME_PLOT = "plot";
        public static final String COLUMN_NAME_LANGUAGE = "language";

        public static final String COLUMN_NAME_RUNTIME = "runtime";
        public static final String COLUMN_NAME_CAST = "cast";
        public static final String COLUMN_NAME_REVIEWS_AUTHOR = "reviewsAuthor";
        public static final String COLUMN_NAME_REVIEWS_TEXT = "reviewsText";

        public static final String COLUMN_NAME_IS_FOR_ADULTS = "isForAdults";
        public static final String COLUMN_NAME_BACKDROP = "backdrop";
        public static final String COLUMN_NAME_DATABASE_BACKDROP_PATH = "databaseBackdropPath";

        public static final String COLUMN_NAME_TRAILERS_THUMBNAILS = "trailersThumbnails";
        public static final String COLUMN_NAME_DATABASE_TRAILERS_THUMBNAILS = "databaseTrailersThumbnails";
    }
}
