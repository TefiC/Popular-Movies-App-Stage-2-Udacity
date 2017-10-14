package com.example.android.popularmoviesstage2.GeneralUtils;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.example.android.popularmoviesstage2.DataUtils.DataTasks;
import com.example.android.popularmoviesstage2.MovieData.Movie;

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
        }

        if(action.equals(DataTasks.ACTION_INSERT_FAVORITE)) {

            DataTasks.executeTask(this, action, movieObject);

        } else if (action.equals(DataTasks.ACTION_REMOVE_FAVORITE)) {

            DataTasks.executeTask(this, action, movieObject);
        }
    }

}
