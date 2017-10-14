package com.example.android.popularmoviesstage2.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.example.android.popularmoviesstage2.Adapters.ReviewsRecyclerViewAdapter;
import com.example.android.popularmoviesstage2.MovieData.Movie;
import com.example.android.popularmoviesstage2.MovieData.MovieReview;
import com.example.android.popularmoviesstage2.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ReviewsActivity extends AppCompatActivity {

    private static final int NUM_LIST_ITEMS = 5;

    private Movie mMovieSelected;
    private String mReviewsString;
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

            mMovieReviewsArray = mMovieSelected.getMovieReviews();

//            Log.v("HI", "REVIEWS: " + mMovieReviewsArray);

            setReviewsAdapter();
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


    /*
     * Helper methods
     */

    /**
     * Formats the reviews String fetched from the internet into JSON and
     * converts it into an ArrayList of movie reviews
     *
     * @param reviewsString A JSON in String format with the reviews data
     * @param movieSelected The movie selected by the user
     * @return An ArrayList of MovieReviews
     */
    public static ArrayList<MovieReview> formatJSONfromReviewsString(String reviewsString, Movie movieSelected) {

//        Log.v("HI", reviewsString);

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

        movieSelected.setMovieReviews(movieReviewsArray);

        return movieReviewsArray;
    }




    // SET ADAPTER ==========================================================================

    /**
     * Sets the ReviewsActivity RecyclerView adapter
     */
    private void setReviewsAdapter() {

        mReviewsRecyclerView = (RecyclerView) findViewById(R.id.reviews_recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mReviewsRecyclerView.setLayoutManager(layoutManager);


        mReviewsAdapter = new ReviewsRecyclerViewAdapter(mMovieReviewsArray, mMovieReviewsArray.size(), this);
        mReviewsRecyclerView.setAdapter(mReviewsAdapter);
    }
}
