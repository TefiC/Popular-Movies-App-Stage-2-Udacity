package com.example.android.popularmoviesstage2.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.android.popularmoviesstage2.Adapters.ReviewsRecyclerViewAdapter;
import com.example.android.popularmoviesstage2.MovieData.Movie;
import com.example.android.popularmoviesstage2.MovieData.MovieReview;
import com.example.android.popularmoviesstage2.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ReviewsActivity extends AppCompatActivity {

    /*
     * Constants
     */

    // Tag for logging
    private static final String TAG = ReviewsActivity.class.getSimpleName();

    // Activity title
    private static final String REVIEWS_ACTIVITY_TITLE = "Reviews";

    /*
     * Fields
     */

    private Movie mMovieSelected;
    private ArrayList<MovieReview> mMovieReviewsArray;
    private RecyclerView mReviewsRecyclerView;
    private ReviewsRecyclerViewAdapter mReviewsAdapter;

    /*
     * Methods
     */

    // Methods to initialize activity ==============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        // Set activity title
        setTitle(REVIEWS_ACTIVITY_TITLE);

        // Get movie data from intent
        Intent intentThatStartedThisActivity = getIntent();

        if(intentThatStartedThisActivity.hasExtra(MainActivity.INTENT_MOVIE_OBJECT_KEY)) {

            mMovieSelected = intentThatStartedThisActivity.getExtras().getParcelable(MainActivity.INTENT_MOVIE_OBJECT_KEY);
            mMovieReviewsArray = mMovieSelected.getMovieReviews();

            // Set adapter
            setReviewsAdapter();
        }
    }


    // Methods to process data =====================================================================

    /**
     * Formats the reviews String fetched from the internet into JSON and
     * converts it into an ArrayList of movie reviews
     *
     * @param reviewsString A JSON in String format with the reviews data
     * @param movieSelected The movie selected by the user
     * @return An ArrayList of MovieReviews
     */
    public static ArrayList<MovieReview> formatJSONfromReviewsString(String reviewsString, Movie movieSelected) {

        ArrayList<MovieReview> movieReviewsArray = new ArrayList<>();

        try {
            // Create JSON object from the String received
            JSONObject reviewsJSON = new JSONObject(reviewsString);
            JSONArray reviewsJSONArray = reviewsJSON.optJSONArray("results");

            // Extract data
            for(int i = 0; i < reviewsJSONArray.length(); i++) {
                JSONObject reviewObject = reviewsJSONArray.getJSONObject(i);
                String author = reviewObject.getString("author");
                String review = reviewObject.getString("content");

                // Create a new review
                movieReviewsArray.add(new MovieReview(author, review));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return movieReviewsArray;
    }


    // Methods for Reviews UI ======================================================================

    /**
     * Sets the ReviewsActivity RecyclerView adapter
     */
    private void setReviewsAdapter() {

        mReviewsRecyclerView = (RecyclerView) findViewById(R.id.reviews_recycler_view);

        // Layout Manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mReviewsRecyclerView.setLayoutManager(layoutManager);

        // Adapter
        mReviewsAdapter = new ReviewsRecyclerViewAdapter(mMovieReviewsArray, mMovieReviewsArray.size(), this);
        mReviewsRecyclerView.setAdapter(mReviewsAdapter);
    }
}
