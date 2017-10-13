package com.example.android.popularmoviesstage2.utils;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.popularmoviesstage2.DataUtils.DataInsertionTasks;
import com.example.android.popularmoviesstage2.Movie;

/**
 * Service to either add or remove a movie from the "Favorites" database
 */

public class FavoritesDataIntentService extends IntentService {


    public FavoritesDataIntentService() {
        super("FavoritesDataIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String action = intent.getAction();

        Movie movieObject = null;

        if(intent.hasExtra("movieObject")) {
            movieObject = intent.getParcelableExtra("movieObject");

            Log.v("Service", "BACKDROP " + movieObject.getMovieBackdropPath());
            Log.v("Service", "TITLE " + movieObject.getMovieTitle());
            Log.v("Service", "CAST " + movieObject.getMovieCast());
            Log.v("Service", "ID " + movieObject.getMovieId());
            Log.v("Service", "TRAILERS " + movieObject.getMovieTrailersThumbnails());
        }

        if(action.equals(DataInsertionTasks.ACTION_INSERT_FAVORITE)) {

            DataInsertionTasks.executeTask(this, action, movieObject);

        } else if (action.equals(DataInsertionTasks.ACTION_REMOVE_FAVORITE)) {

            DataInsertionTasks.executeTask(this, action, movieObject);
        }
    }

}
