package com.example.android.popularmoviesstage2;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmoviesstage2.utils.LoaderUtils;
import com.example.android.popularmoviesstage2.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {

    /*
     * Fields
     */

    private Movie movieSelected;

    private ImageView moviePosterView;
    private TextView movieVoteAverageView;
    private TextView movieReleaseView;
    private TextView moviePlotView;
    private TextView movieTitleView;
    private TextView movieLanguageView;
    private TextView movieRuntimeView;
    private ImageView movieBackdropView;
    private LinearLayout movieDetailsTrailerLinearContainer;

    private ProgressBar mProgressBarDetails;

    private GradientDrawable mGradient;
    /*
     * Constants
     */

    private static final String NOT_AVAILABLE = "Not available";

    // Constants to form the movie poster URL
    public static final String MOVIEDB_POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
    public static final String IMAGE_SIZE = "w185";

    // Backdrop parameters
    private static final String BACKDROP_SIZE = "w300";

    //Get movie trailer thumbnail
    private static final String TRAILER_THUMBNAIL_BASE_PATH = "https://img.youtube.com/vi/";


    private static final String TAG = DetailsActivity.class.getSimpleName();

    /*
     * Methods
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_second);

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
            loadData(LoaderUtils.DETAILS_SEARCH_LOADER);
            loadData(LoaderUtils.TRAILERS_SEARCH_LOADER);
        }
    }

    /**
     * Assigns all the necessary views in the details activity to variables
     */
    public void getViewsReference() {

        //Assign the views that will be populated with the movie's data
        moviePosterView = (ImageView) findViewById(R.id.movie_details_poster_view);
        movieTitleView = (TextView) findViewById(R.id.movie_details_title_view);

        movieVoteAverageView = (TextView) findViewById(R.id.movie_details_vote_view);
        movieReleaseView = (TextView) findViewById(R.id.movie_details_release_view);

        moviePlotView = (TextView) findViewById(R.id.movie_details_plot_view);
        movieLanguageView = (TextView) findViewById(R.id.movie_details_language);

        movieRuntimeView = (TextView) findViewById(R.id.movie_details_runtime);
        movieBackdropView = (ImageView) findViewById(R.id.movie_details_backdrop);

        movieDetailsTrailerLinearContainer = (LinearLayout) findViewById(R.id.movie_details_trailers_container);
    }

    /**
     * Find the toolbar and sets a support action bar
     */
    public void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    /**
     * Triggers the appropriate loader to load the corresponding movie data
     *
     * @param loaderID The Loader's ID
     */
    private void loadData(int loaderID) {
        Bundle detailsBundle = createDetailsBundle();

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> detailsLoader = loaderManager.getLoader(loaderID);
        if (detailsLoader == null) {
            loaderManager.initLoader(loaderID, detailsBundle, this);
        } else {
            loaderManager.restartLoader(loaderID, detailsBundle, this);
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
        String movieBackdropPath = createFullBackdropPath(movie.getMovieBackdropPath());

        // Update views
        loadMoviePoster(posterPath);
        loadMovieBackdrop(movieBackdropPath);

        setViewData(movieVoteAverageView, voteAverage.toString());
        setViewData(movieReleaseView, releaseDate);

        setViewData(moviePlotView, moviePlot);
        setViewData(movieTitleView, movieTitle);

        setViewData(movieLanguageView, movieLanguage);
        setViewData(movieRuntimeView, movieRuntime);

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
     * Load Movie backdrop to the corresponding View using
     * the Picasso library
     *
     * @param backdropPath URL to fetch the movie backdrop
     */
    private void loadMovieBackdrop(String backdropPath) {
        if (backdropPath != null) {
            Picasso.with(this)
                    .load(backdropPath)
                    .placeholder(generateGradientDrawable())
                    .error(generateGradientDrawable())
                    .into(movieBackdropView);
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
    public Loader<String> onCreateLoader(final int id, final Bundle args) {
        return new AsyncTaskLoader<String>(this) {

            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                if (args == null) {
                    return;
                }

                forceLoad();
            }

            @Override
            public void deliverResult(String data) {
                super.deliverResult(data);
            }

            @Override
            public String loadInBackground() {
                String searchResults = null;
                URL searchQueryURL;
                Movie movie = args.getParcelable("movieObject");

                switch (id) {
                    case LoaderUtils.DETAILS_SEARCH_LOADER:
                        searchQueryURL = NetworkUtils.buildMovieDetailsUrl(movie.getMovieId());

                        try {
                            searchResults = NetworkUtils.getResponseFromHttpUrl(searchQueryURL);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case LoaderUtils.TRAILERS_SEARCH_LOADER:
                        searchQueryURL = NetworkUtils.buildMovieTrailersUrl(movie.getMovieId());

                        try {
                            searchResults = NetworkUtils.getResponseFromHttpUrl(searchQueryURL);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }

                return searchResults;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        switch (loader.getId()) {
            case LoaderUtils.DETAILS_SEARCH_LOADER:
                addMovieDetails(data, movieSelected);
                fillMovieData(movieSelected);
                break;
            case LoaderUtils.TRAILERS_SEARCH_LOADER:
                createMovieTrailers(data, movieSelected);
        }
    }

    /**
     * Creates ImageViews for each movie trailer, appends it to the corresponding ViewGroup
     * sets its properties and add an onClickListener
     * @param data
     * @param movieSelected
     */
    private void createMovieTrailers(String data, final Movie movieSelected) {
        try {
            JSONObject trailersJSON = new JSONObject(data);
            JSONArray trailersArray = trailersJSON.getJSONArray("results");

            // Iterate over the trailers array
            int i;
            for (i = 0; i < trailersArray.length(); i++) {

                try {
                    JSONObject trailer = trailersArray.getJSONObject(i);
                    final String trailerKey = trailer.getString("key");

                    //Create ImageView, set its properties and add it to the layout
                    ImageView trailerView = new ImageView(this);
                    setTrailerViewProperties(trailerView, trailerKey);
                    movieDetailsTrailerLinearContainer.addView(trailerView, i);

                    // Load the thumbnail
                    loadMovieTrailerThumbnail(trailerView, trailerKey);
                    // Set onClick listener
                    setTrailerOnClickListener(trailerView, trailerKey);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void launchTrailer(String trailerKey) {
        // LAUNCH IMPLICIT INTENT TO WATCH TRAILER
        Log.v(TAG, trailerKey);
    }

    /**
     * Sets an onClickListener for the trailer ImageView to launch an implicit
     * intent to the Youtube URL that corresponds to the trailer.
     * @param trailerView The trailer's ImageView
     * @param trailerKey The trailer's key to append to Youtube's URL
     */
    private void setTrailerOnClickListener(ImageView trailerView, final String trailerKey) {
        trailerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchTrailer(trailerKey);
            }
        });
    }

    /**
     * Sets the Trailer ImageView properties.
     * @param trailerView The ImageView
     * @param trailerKey The Trailer's key
     */
    private void setTrailerViewProperties(ImageView trailerView, String trailerKey) {

        // Set dimensions
        int height = convertDpToPixels(120);
        int width = convertDpToPixels(150);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);

        // Set margin
        int marginEnd = convertDpToPixels(10);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            params.setMarginEnd(marginEnd);
        } else {
            params.setMargins(marginEnd, marginEnd, marginEnd, marginEnd);
        }

        //Set tag
        trailerView.setTag(trailerKey);

        // Include parameters
        trailerView.setLayoutParams(params);

        //Image scale type
        trailerView.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }

    /**
     * Converts Dp to Pixels to use when setting LayoutParams
     * @param dimensionInDp The dimension to convert
     * @return The dimention passed as argument in pixels
     */
    private int convertDpToPixels(int dimensionInDp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dimensionInDp, getResources().getDisplayMetrics());
    }

    /**
     * Loads the movie trailer thumbnail from Youtube
     * @param trailerKey The corresponding trailer's key
     */
    private void loadMovieTrailerThumbnail(ImageView trailerView, String trailerKey) {
        String searchURL = TRAILER_THUMBNAIL_BASE_PATH + trailerKey + "/0.jpg";

        Picasso.with(this)
                .load(searchURL)
                .placeholder(generateGradientDrawable())
                .error(generateGradientDrawable())
                .into(trailerView);
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


    /**
     * Generate a full backgrop URL to load image from MovieDB API
     *
     * @param backdropPath The final piece of the path to the movie's backdrop image
     * @return A full URL to request the image
     */
    public String createFullBackdropPath(String backdropPath) {
        return MOVIEDB_POSTER_BASE_URL + BACKDROP_SIZE + backdropPath;
    }

    /**
     * Generates a gradient drawable from the app's primary color
     * to act as I as a placeholder or to display in case of error
     * loading movie backdrop
     *
     * @return A GradientDrawable of the app's primary color
     */
    private GradientDrawable generateGradientDrawable() {
        if (mGradient != null) {
            return mGradient;
        } else {
            GradientDrawable gradient = new GradientDrawable();
            gradient.setShape(GradientDrawable.RECTANGLE);
            gradient.setColor(ContextCompat.getColor(this, R.color.colorPrimary));
            mGradient = gradient;

            return gradient;
        }
    }
}
