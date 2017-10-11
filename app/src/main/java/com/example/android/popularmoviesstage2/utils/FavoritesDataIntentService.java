package com.example.android.popularmoviesstage2.utils;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;

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

//        Log.v("DB", "HANDLING INTENT");

        Movie movieObject = null;

        if(intent.hasExtra("movieObject")) {
            movieObject = intent.getExtras().getParcelable("movieObject");
        }

        if(action.equals(DataInsertionTasks.ACTION_INSERT_FAVORITE)) {
//            Log.v("DB", "INSERT FAVORITE");
            Bitmap bitmap = (Bitmap) intent.getExtras().getParcelable("bitmap");
            DataInsertionTasks.executeTask(this, action, movieObject, bitmap);
        } else if (action.equals(DataInsertionTasks.ACTION_REMOVE_FAVORITE)) {
            DataInsertionTasks.executeTask(this, action, movieObject, null);
        }
    }

}
