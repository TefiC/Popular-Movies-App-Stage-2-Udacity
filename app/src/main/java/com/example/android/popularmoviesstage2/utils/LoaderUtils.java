package com.example.android.popularmoviesstage2.utils;

import com.example.android.popularmoviesstage2.DataUtils.MoviesDBContract;

/**
 * Loader utilities
 */

public class LoaderUtils {

    //Loader IDs

    public static final int MAIN_SEARCH_LOADER = 20;
    public static final int DETAILS_SEARCH_LOADER = 58;
    public static final int CAST_SEARCH_LOADER = 90;
    public static final int TRAILERS_SEARCH_LOADER = 30;
    public static final int FAVORITE_MOVIES_LOADER = 60;
    public static final int FAVORITE_MOVIES_LOADER_BY_ID = 35;

    public static final String[] MAIN_FAVORITE_MOVIES_PROJECTION = {

            MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_MOVIEDB_ID,
            MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_TITLE,
            MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_RELEASE_DATE,
            MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_POSTER_PATH,
            MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_VOTE_AVERAGE,
            MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_PLOT,
    };

    public static final String[] INDIVIDUAL_MOVIE_DETAILS_PROJECTION = {
            MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_LANGUAGE,
            MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_RUNTIME,
            MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_CAST,
            MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_REVIEWS,
            MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_IS_FOR_ADULTS,
            MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_BACKDROP,
            MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_TRAILERS_THUMBNAILS,
    };
}
