package com.example.android.popularmoviesstage2.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
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

            if(mMovieReviewsArray.size() > 0) {
                // Set adapter
                setReviewsAdapter();
            } else {
                createNoReviewsDialog(this);
            }
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

    // Methods for User interaction ======================================================================

    /**
     * Creates and displays an alert dialog telling the user
     * the movie he/she selected has no reviews available
     *
     * @param context Context of the Activity where the dialog is launched
     */
    public static void createNoReviewsDialog(final Context context) {

        //Create dialog builder with corresponding settings
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        // Set content
        builder.setTitle(context.getString(R.string.no_reviews_dialog_title))
                .setMessage(context.getString(R.string.no_reviews_dialog_message));
        // Set button
        builder.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                NavUtils.navigateUpFromSameTask((Activity) context);
            }
        });

        // Create dialog and display it to the user
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
