package com.example.android.popularmoviesstage2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmoviesstage2.utils.LoaderUtils;
import com.example.android.popularmoviesstage2.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {

    /*
     * Fields
     */

    private Movie movieSelected;
    private String movieStringData;

    private ImageView moviePosterView;
    private TextView movieVoteAverageView;
    private TextView movieReleaseView;
    private TextView moviePlotView;
    private TextView movieTitleView;
    private TextView movieLanguageView;
    private TextView movieRuntimeView;
    private TextView movieForAdultsView;
    private TextView movieBackdropView;

    private ProgressBar mProgressBarDetails;

    /*
     * Constants
     */

    private static final String NOT_AVAILABLE = "Not available";

    private static final String TAG = DetailsActivity.class.getSimpleName();

    /*
     * Methods
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        mProgressBarDetails = (ProgressBar) findViewById(R.id.progress_bar_details);


        setupToolbar();

        // Get movie data from intent
        Intent intentThatStartedThisActivity = getIntent();

        getViewsReference();

        // Make plot view scrollable
        moviePlotView.setMovementMethod(new ScrollingMovementMethod());

        // Get movie from intent and populate views with data
        if (intentThatStartedThisActivity.hasExtra("movieObject")) {
            Movie movie = intentThatStartedThisActivity.getExtras().getParcelable("movieObject");
            movieSelected = movie;

            // Set activity title
            setTitle(movie.getMovieTitle());

            //Start AsyncTaskLoader
            loadMovieDetails();
        }
    }

    /**
     * Assigns all the necessary views in the details activity to variables
     */
    public void getViewsReference() {

        //Assign the views that will be populated with the movie's data
        moviePosterView = (ImageView) findViewById(R.id.movie_details_poster_view);
        movieTitleView = (TextView) findViewById(R.id.movie_title_view);

        movieVoteAverageView = (TextView) findViewById(R.id.movie_details_vote_view);
        movieReleaseView = (TextView) findViewById(R.id.movie_details_release_view);

        moviePlotView = (TextView) findViewById(R.id.movie_details_plot_view);
        movieLanguageView = (TextView) findViewById(R.id.language);

        movieRuntimeView = (TextView) findViewById(R.id.runtime);
        movieForAdultsView = (TextView) findViewById(R.id.forAdults);
        movieBackdropView = (TextView) findViewById(R.id.backdrop);
    }

    /**
     * Find the toolbar and sets a support action bar
     */
    public void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    /**
     * Loads the movie's details and data asynchronously
     */
    private void loadMovieDetails() {

        Bundle detailsBundle = createDetailsBundle();

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> detailsLoader = loaderManager.getLoader(LoaderUtils.DETAILS_SEARCH_LOADER);
        if (detailsLoader == null) {
            loaderManager.initLoader(LoaderUtils.DETAILS_SEARCH_LOADER, detailsBundle, this);
        } else {
            loaderManager.restartLoader(LoaderUtils.DETAILS_SEARCH_LOADER, detailsBundle, this);
        }
    }

    /**
     * Creates a bundle with the movie object
     *
     * @return A Bundle with a movie object
     */
    private Bundle createDetailsBundle() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("movieObject", movieSelected);

        return bundle;
    }

    /**
     * Updates the Details Activity UI by setting the text
     * and resources for the movie selected by the user
     *
     * @param movie The movie selected by the user
     */
    private void fillMovieData(Movie movie) {

        // Movie data
        String movieTitle = movie.getMovieTitle();
        String posterPath = movie.getMoviePosterPath();

        Double voteAverage = movie.getMovieVoteAverage();
        String releaseDate = extractReleaseYear(movie.getMovieReleaseDate());

        String moviePlot = movie.getMoviePlot();
        String movieLanguage = movie.getMovieLanguage();

        String movieRuntime = Integer.toString((int) movie.getMovieRuntime());
        boolean isMovieForAdults = movie.getIsMovieForAdults();
        String movieBackdropPath = movie.getMovieBackdropPath();

        // Update views
        loadMoviePoster(posterPath);
        setViewData(movieVoteAverageView, voteAverage.toString());
        setViewData(movieReleaseView, releaseDate);

        setViewData(moviePlotView, moviePlot);
        setViewData(movieTitleView, movieTitle);

        setViewData(movieLanguageView, movieLanguage);
        setViewData(movieRuntimeView, movieRuntime);

        setViewData(movieForAdultsView, Boolean.toString(isMovieForAdults));
        setViewData(movieBackdropView, movieBackdropPath);

    }

    /**
     * Load Movie poster into the corresponding view
     * using the Picasso library.
     *
     * @param posterPath URL to fetch the movie poster
     */
    private void loadMoviePoster(String posterPath) {
        if (posterPath != null) {
            Picasso.with(this)
                    .load(posterPath)
                    .placeholder(R.drawable.placeholder)
                    .resize(200, 300)
                    .error(R.drawable.movie_details_error)
                    .into(moviePosterView);
        }
    }

    /**
     * Extract the year the movie was released.
     *
     * @param releaseDate A String that represents
     *                    a date with the format YYYY-MM-DD
     * @return A four-digit year in String format
     */
    private String extractReleaseYear(String releaseDate) {
        return releaseDate.split("-")[0];
    }

    /**
     * Sets the view's text to be the value provided
     *
     * @param view  A TextView
     * @param value A String that will be the text
     *              for the view provided
     */
    private void setViewData(TextView view, String value) {
        if (value != null) {
            view.setText(value);
        } else {
            view.setText(NOT_AVAILABLE);
        }
    }

    // AsyncTaskLoader ============================================================================

    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String>(this) {

            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                if (args == null) {
                    return;
                }
                mProgressBarDetails.setVisibility(View.VISIBLE);
                forceLoad();
            }

            @Override
            public void deliverResult(String data) {
                super.deliverResult(data);
            }

            @Override
            public String loadInBackground() {
                String searchResults = null;

                Movie movie = args.getParcelable("movieObject");

                URL searchQueryURL = NetworkUtils.buildMovieDetailsUrl(movie.getMovieId());

                try {
                    searchResults = NetworkUtils.getResponseFromHttpUrl(searchQueryURL);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return searchResults;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        addMovieDetails(data, movieSelected);
        fillMovieData(movieSelected);
        mProgressBarDetails.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        //
    }

    /*
     * Helper methods
     */

    /**
     * Adds movie details (language, runtime, isForAdults, backdrop path)
     * to the movie object reference stored in memory.
     *
     * @param movieDetailsString JSON as string with the movie's details
     * @param movieSelected      A reference to the movie instance
     */
    public static void addMovieDetails(String movieDetailsString, Movie movieSelected) {

        JSONObject movieDetailsJSON;

        try {
            movieDetailsJSON = new JSONObject(movieDetailsString);

            movieSelected.setMovieLanguage(movieDetailsJSON.getString("original_language"));
            movieSelected.setMovieRuntime(movieDetailsJSON.getDouble("runtime"));
            movieSelected.setIsMovieForAdults(movieDetailsJSON.getBoolean("adult"));
            movieSelected.setMovieBackdropPath(movieDetailsJSON.getString("backdrop_path"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
