package com.example.android.popularmoviesstage2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.example.android.popularmoviesstage2.utils.LoaderUtils;
import com.example.android.popularmoviesstage2.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.atomic.LongAccumulator;

public class ReviewsActivity extends AppCompatActivity {

    private static final int NUM_LIST_ITEMS = 5;

    private Movie mMovieSelected;
    private ArrayList<MovieReview> mMovieReviewsArray;
    private RecyclerView mReviewsRecyclerView;
    private ReviewsRecyclerViewAdapter mReviewsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        ActionBar actionBar = this.getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
        }

        // Get movie data from intent
        Intent intentThatStartedThisActivity = getIntent();

        setTitle("Reviews");

        if(intentThatStartedThisActivity.hasExtra("movieObject")) {
            mMovieSelected = intentThatStartedThisActivity.getExtras().getParcelable("movieObject");

            LoaderManager loaderManager = getSupportLoaderManager();
            Loader<String> reviewsSearchLoader = loaderManager.getLoader(LoaderUtils.FAVORITE_REVIEWS_LOADER);

            Bundle queryBundle = new Bundle();
            queryBundle.putParcelable("movieObject", mMovieSelected);

            if(reviewsSearchLoader == null) {
                loaderManager.initLoader(LoaderUtils.FAVORITE_REVIEWS_LOADER, queryBundle, new ReviewsLoader(this));
            } else {
                loaderManager.restartLoader(LoaderUtils.FAVORITE_REVIEWS_LOADER, queryBundle, new ReviewsLoader(this));
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }

        return super.onOptionsItemSelected(item);
    }


    // LOADER METHODS ================================================================

    private String mCachedReviews;

    private class ReviewsLoader implements LoaderManager.LoaderCallbacks<String> {

        private Context mContext;

        public ReviewsLoader(Context context) {
            mContext = context;
        }

        @Override
        public Loader<String> onCreateLoader(final int id, final Bundle args) {
            return new AsyncTaskLoader<String>(mContext) {

                @Override
                protected void onStartLoading() {
                    super.onStartLoading();

                    if(args == null) {
                        return;
                    }

                    switch(id) {
                        case LoaderUtils.FAVORITE_REVIEWS_LOADER:
                            if(mCachedReviews != null) {
                                deliverResult(mCachedReviews);
                            } else {
                                forceLoad();
                            }
                    }
                }

                @Override
                public String loadInBackground() {
                    URL searchQueryUrl;
                    String searchResults = null;

                    searchQueryUrl = NetworkUtils.buildMovieReviewsUrl(mMovieSelected.getMovieId());

                    try {
                        searchResults = NetworkUtils.getResponseFromHttpUrl(searchQueryUrl);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return searchResults;
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<String> loader, String data) {
            formatJSONfromReviewsString(data);
            setReviewsAdapter();
        }

        @Override
        public void onLoaderReset(Loader<String> loader) {

        }
    }

    /*
     * Helper methods
     */

    private ArrayList<MovieReview> formatJSONfromReviewsString(String reviewsString) {

        ArrayList<MovieReview> movieReviewsArray = new ArrayList<>();

        try {
            JSONObject reviewsJSON = new JSONObject(reviewsString);
            JSONArray reviewsJSONArray = reviewsJSON.optJSONArray("results");

            int i;
            for(i = 0; i < reviewsJSONArray.length(); i++) {
                JSONObject reviewObject = reviewsJSONArray.getJSONObject(i);
                String author = reviewObject.getString("author");
                String review = reviewObject.getString("content");

                movieReviewsArray.add(new MovieReview(author, review));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        mMovieReviewsArray = movieReviewsArray;

        return movieReviewsArray;
    }

    // SET ADAPTER ==========================================================================

    private void setReviewsAdapter() {

        mReviewsRecyclerView = (RecyclerView) findViewById(R.id.reviews_recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mReviewsRecyclerView.setLayoutManager(layoutManager);


        mReviewsAdapter = new ReviewsRecyclerViewAdapter(mMovieReviewsArray, mMovieReviewsArray.size(), this);
        mReviewsRecyclerView.setAdapter(mReviewsAdapter);
    }
}
