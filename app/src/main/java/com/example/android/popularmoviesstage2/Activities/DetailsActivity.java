package com.example.android.popularmoviesstage2.Activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmoviesstage2.DataUtils.DBServiceTasks;
import com.example.android.popularmoviesstage2.DataUtils.FavoritesUtils;
import com.example.android.popularmoviesstage2.DataUtils.ImagesDBUtils;
import com.example.android.popularmoviesstage2.DataUtils.MoviesDBContract;
import com.example.android.popularmoviesstage2.GeneralUtils.FavoritesDataIntentService;
import com.example.android.popularmoviesstage2.GeneralUtils.LoaderUtils;
import com.example.android.popularmoviesstage2.GeneralUtils.NetworkUtils;
import com.example.android.popularmoviesstage2.MovieData.Movie;
import com.example.android.popularmoviesstage2.MovieData.MovieReview;
import com.example.android.popularmoviesstage2.MovieData.MovieTrailerThumbnail;
import com.example.android.popularmoviesstage2.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import static com.example.android.popularmoviesstage2.GeneralUtils.LoaderUtils.CAST_SEARCH_LOADER;
import static com.example.android.popularmoviesstage2.GeneralUtils.LoaderUtils.FAVORITE_MOVIES_LOADER_BY_ID;
import static com.example.android.popularmoviesstage2.GeneralUtils.LoaderUtils.REVIEWS_LOADER;
import static com.example.android.popularmoviesstage2.GeneralUtils.LoaderUtils.TRAILERS_SEARCH_LOADER;
import static com.example.android.popularmoviesstage2.R.id.favorite_floating_button;
import static com.squareup.picasso.Picasso.with;

public class DetailsActivity extends AppCompatActivity {

     /*
     * Constants ============================================================
     */

    // Tag for logging
    private static final String TAG = DetailsActivity.class.getSimpleName();

    // Default value for any attribute if the value retrieved is null
    private static final String NOT_AVAILABLE = "Not available";

    // Constants to form the movie poster URL
    public static final String MOVIEDB_POSTER_BASE_URL = "http://image.tmdb.org/t/p/";

    // Constants to form the movie trailer thumbails URL
    public static final String TRAILER_THUMBNAIL_BASE_PATH = "https://img.youtube.com/vi/";

    //Launch trailer
    private static final String YOUTUBE_BASE_PATH = "https://www.youtube.com/watch?v=";

    // Determines the number of cast members to be displayed
    private static final int NUMBER_OF_ACTORS_TO_INCLUDE = 5;

    /*
     * Fields ============================================================
     */

    // Activity context
    private static Context mContext;

    // Movie selected by the user
    public Movie movieSelected;
    public boolean mIsMovieSelectedFavorite;

    // Individual views
    private ImageView mMoviePosterView;
    private TextView mMovieVoteAverageView;
    private TextView mMovieReleaseView;

    private TextView mMoviePlotView;
    private TextView mMovieTitleView;
    private TextView mMovieLanguageView;

    private TextView mMovieRuntimeView;
    private TextView mMovieCastView;
    private ImageView mMovieBackdropView;

    private TextView mReviewsReadMoreView;
    private LinearLayout mMovieDetailsTrailerLinearContainer;
    private FloatingActionButton mFloatingActionButtonFavorite;

    private RelativeLayout mDetailsLayout;
    private ProgressBar mDetailsProgressBar;
    private FloatingActionButton mFavoriteButton;

    // Gradient to fill background color with app's theme color
    private GradientDrawable mGradient;

    // Cached loaders data
    private String mCachedDetails;
    private String mCachedTrailers;
    private String mCachedCast;
    private String mCachedReviews;

    // Uri
    private static Uri mMovieSelectedUri;

    /*
     * Methods ============================================================
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_second);

        // Assign the context
        mContext = this;

        // Prepare the layout
        setupToolbar();
        getViewsReference();
        // Make plot view scrollable
        mMoviePlotView.setMovementMethod(new ScrollingMovementMethod());

        // Get movie data from intent and populate views with data
        Intent intentThatStartedThisActivity = getIntent();

        if (intentThatStartedThisActivity.hasExtra("movieObject")) {

            movieSelected = intentThatStartedThisActivity.getExtras().getParcelable("movieObject");

            // Create Uri for movie in database
            createMovieUri();

            // Set activity title
            setTitle(movieSelected.getMovieTitle());
            // Define if the movie selected is one of the user's favorites
            mIsMovieSelectedFavorite = movieSelected.getIsMovieFavorite();

            // If the movie is one of the user's favorite, load its data from the database
            // Else, if it isn't one of the user's favorite, fetch its data from the internet
            if (mIsMovieSelectedFavorite) {
                loadDataFromDatabase(LoaderUtils.FAVORITE_MOVIES_LOADER_BY_ID);
            } else {
                loadDataFromInternet(LoaderUtils.DETAILS_SEARCH_LOADER);
            }
        }
    }

    // Methods to prepare the UI before network request ============================================

    /**
     * Assigns all the necessary views in the details activity to member variables
     */
    public void getViewsReference() {

        mMoviePosterView = (ImageView) findViewById(R.id.movie_details_poster_view);
        mMovieTitleView = (TextView) findViewById(R.id.movie_details_title_view);
        mMovieVoteAverageView = (TextView) findViewById(R.id.movie_details_vote_view);

        mMovieReleaseView = (TextView) findViewById(R.id.movie_details_release_view);
        mMoviePlotView = (TextView) findViewById(R.id.movie_details_plot_view);
        mMovieLanguageView = (TextView) findViewById(R.id.movie_details_language);

        mMovieRuntimeView = (TextView) findViewById(R.id.movie_details_runtime);
        mMovieBackdropView = (ImageView) findViewById(R.id.movie_details_backdrop);
        mMovieCastView = (TextView) findViewById(R.id.details_cast_text);

        mMovieDetailsTrailerLinearContainer = (LinearLayout) findViewById(R.id.movie_details_trailers_container);
        mFloatingActionButtonFavorite = (FloatingActionButton) findViewById(R.id.favorite_floating_button);
        mDetailsLayout = (RelativeLayout) findViewById(R.id.details_relative_layout);

        mDetailsProgressBar = (ProgressBar) findViewById(R.id.details_progress_bar);
        mReviewsReadMoreView = (TextView) findViewById(R.id.movie_details_reviews_read_more);
        mFavoriteButton = (FloatingActionButton) findViewById(favorite_floating_button);
    }

    /**
     * Finds the toolbar and sets a support action bar
     */
    public void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    /**
     * Create the movie selected Uri for database operations
     * and assigns it to a member variable
     */
    private void createMovieUri() {
        mMovieSelectedUri = MoviesDBContract.FavoriteMoviesEntry.CONTENT_URI.buildUpon()
                .appendPath(Integer.toString(movieSelected.getMovieId()))
                .build();
    }

    // Network request methods and Loaders =========================================================

    /**
     * Triggers the appropriate loader to load the corresponding movie data
     *
     * @param loaderID The Loader's ID
     */
    private void loadDataFromInternet(int loaderID) {

        Bundle detailsBundle = createDetailsBundle();

        // Setting up the Loader Manager
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> detailsLoader = loaderManager.getLoader(loaderID);

        // Start the loader
        if (detailsLoader == null) {
            loaderManager.initLoader(loaderID, detailsBundle, new InternetLoader(this));
        } else {
            loaderManager.restartLoader(loaderID, detailsBundle, new InternetLoader(this));
        }
    }

    /**
     * Creates a bundle with the parcelable movie object
     *
     * @return A Bundle with a movie object
     */
    private Bundle createDetailsBundle() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("movieObject", movieSelected);

        return bundle;
    }

    /**
     * Loader to handle network requests
     */
    private class InternetLoader implements LoaderManager.LoaderCallbacks<String> {

        private Context mContext;

        // Constructor
        private InternetLoader(Context context) {
            mContext = context;
        }

        @Override
        public Loader<String> onCreateLoader(final int id, final Bundle args) {
            return new AsyncTaskLoader<String>(mContext) {

                @Override
                protected void onStartLoading() {
                    super.onStartLoading();

                    // If there are no arguments Bundle, end Loader
                    if (args == null) {
                        return;
                    }

                    // Determine if loader should load data or return cached data
                    // for the corresponding loader
                    switch (id) {
                        case LoaderUtils.DETAILS_SEARCH_LOADER:
                            mDetailsProgressBar.setVisibility(View.VISIBLE);
                            determineLoaderAction(mCachedDetails);
                            break;
                        case LoaderUtils.TRAILERS_SEARCH_LOADER:
                            determineLoaderAction(mCachedTrailers);
                            break;
                        case LoaderUtils.CAST_SEARCH_LOADER:
                            determineLoaderAction(mCachedCast);
                            break;
                        case LoaderUtils.REVIEWS_LOADER:
                            determineLoaderAction(mCachedReviews);
                            break;
                    }
                }

                @Override
                public void deliverResult(String data) {

                    // Return cached data
                    switch (id) {
                        case LoaderUtils.DETAILS_SEARCH_LOADER:
                            mCachedDetails = data;
                            break;
                        case LoaderUtils.TRAILERS_SEARCH_LOADER:
                            mCachedTrailers = data;
                            break;
                        case LoaderUtils.CAST_SEARCH_LOADER:
                            mCachedCast = data;
                            break;
                        case LoaderUtils.REVIEWS_LOADER:
                            mCachedReviews = data;
                            break;
                    }

                    mDetailsProgressBar.setVisibility(View.GONE);
                    super.deliverResult(data);
                }

                @Override
                public String loadInBackground() {

                    String searchResults = null;
                    URL searchQueryURL = null;

                    Movie movie = args.getParcelable("movieObject");

                    switch (id) {
                        case LoaderUtils.DETAILS_SEARCH_LOADER:
                            searchQueryURL = NetworkUtils.buildSearchUrl(NetworkUtils.SEARCH_TYPE_DETAILS,
                                                                         null,
                                                                         movie.getMovieId());
                            break;
                        case LoaderUtils.TRAILERS_SEARCH_LOADER:
                            searchQueryURL = NetworkUtils.buildSearchUrl(NetworkUtils.SEARCH_TYPE_TRAILERS,
                                                                         null,
                                                                         movie.getMovieId());
                            break;
                        case LoaderUtils.CAST_SEARCH_LOADER:
                            searchQueryURL = NetworkUtils.buildSearchUrl(NetworkUtils.SEARCH_TYPE_CAST,
                                                                         null,
                                                                         movie.getMovieId());
                            break;
                        case LoaderUtils.REVIEWS_LOADER:
                            searchQueryURL = NetworkUtils.buildSearchUrl(NetworkUtils.SEARCH_TYPE_REVIEWS,
                                                                         null,
                                                                         movie.getMovieId());
                            break;
                    }

                    if (searchQueryURL != null) {
                        try {
                            // Request data and save the results
                            searchResults = NetworkUtils.getResponseFromHttpUrl(searchQueryURL);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    return searchResults;
                }

                /**
                 * Determines if the loader should load new data or if there
                 * are cached results to deliver instead
                 * @param cachedVariableName The name of the member variable
                 *                           that stores the cached data
                 */
                private void determineLoaderAction(String cachedVariableName) {
                    if (cachedVariableName == null) {
                        forceLoad();
                    } else {
                        deliverResult(cachedVariableName);
                    }
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<String> loader, String data) {

            switch (loader.getId()) {

                case LoaderUtils.DETAILS_SEARCH_LOADER:
                    /* Add movie details to the movie object,
                     hide the progress bar and trigger
                     a loader to request the cast */
                    loadDataFromInternet(CAST_SEARCH_LOADER);
                    addMovieDetails(data, movieSelected);
                    mDetailsProgressBar.setVisibility(View.GONE);
                    break;

                case LoaderUtils.CAST_SEARCH_LOADER:
                    /* Trigger a loader to request movie trailers,
                       extract the cast data, fill the UI with the
                       corresponding data and display it to the user */
                    loadDataFromInternet(TRAILERS_SEARCH_LOADER);
                    extractMovieCastArrayFromJSON(data);
                    fillMovieData(movieSelected);
                    mDetailsLayout.setVisibility(View.VISIBLE);
                    break;

                case LoaderUtils.TRAILERS_SEARCH_LOADER:
                    /* Create the movie trailer objects from data
                       received, and trigger a loader to load reviews*/
                    createMovieTrailers(data);
                    loadDataFromInternet(REVIEWS_LOADER);
                    break;

                case LoaderUtils.REVIEWS_LOADER:
                    /* Assign reviews to the movie object in memory,
                       display and add onClickListener to the button that adds
                       the movie to favorites to make sure that all the data has
                       loaded correctly before inserting it to the database */
                    movieSelected.setMovieReviews(ReviewsActivity.formatJSONfromReviewsString(data, movieSelected));
                    addOnClickListenerToFloatingActionButton();
                    mFloatingActionButtonFavorite.setVisibility(View.VISIBLE);
                    break;
            }
        }

        @Override
        public void onLoaderReset(Loader<String> loader) {
            //
        }
    }

    // Methods to format data and update UI after network request ==================================

    /**
     * Updates the Details Activity UI by setting the text
     * and resources for the movie selected by the user
     *
     * @param movie The movie selected by the user
     */
    private void fillMovieData(Movie movie) {

        // Extract data from the Movie object
        String movieTitle = movie.getMovieTitle();
        String posterPath = movie.getMoviePosterPath();
        Double voteAverage = movie.getMovieVoteAverage();

        String releaseDate = extractReleaseYear(movie.getMovieReleaseDate());
        String moviePlot = movie.getMoviePlot();
        String movieLanguage = movie.getMovieLanguage();

        String movieRuntime = Integer.toString((int) movie.getMovieRuntime());
        boolean isMovieForAdults = movie.getIsMovieForAdults();
        String movieBackdropPath = createFullBackdropPath(movie.getMovieBackdropPath());

        // Update views with the data

        addMovieImagesToUI(posterPath, movieBackdropPath);

        setFloatingButtonImage();

        setViewData(mMovieVoteAverageView, voteAverage.toString());
        setViewData(mMovieReleaseView, releaseDate);
        setViewData(mMoviePlotView, moviePlot);

        setViewData(mMovieTitleView, movieTitle);
        setViewData(mMovieLanguageView, movieLanguage);
        setViewData(mMovieRuntimeView, movieRuntime);

        // Append cast to UI
        appendCastToUI(movieSelected.getMovieCast());

        addReadReviewsOnClickListener();

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

    /**
     * Appends the number of cast members to be included to the UI
     * according to the constant NUMBER_OF_ACTORS_TO_INCLUDE
     *
     * @param movieCast The ArrayList of Strings with the cast data
     */
    private void appendCastToUI(ArrayList<String> movieCast) {
        // Add cast
        for (int i = 0; i < NUMBER_OF_ACTORS_TO_INCLUDE; i++) {
            mMovieCastView.append(movieCast.get(i) + "\n");
        }
    }

    /**
     * Adds the movie images to the UI (poster, backdrop and trailer thumbnails.
     * The source of the images is determined by whether the movie is one of the
     * user's favorites, in which case they are retrieved from the database.
     * Else, the images are retrieved from the internet.
     *
     * @param posterPath        The movie's poster path
     * @param movieBackdropPath The movie's backdrop path
     */
    private void addMovieImagesToUI(String posterPath, String movieBackdropPath) {

        if (mIsMovieSelectedFavorite) {

            // Poster
            fillMoviePosterDetailsFromDB(loadImageFromDatabase(
                    this,
                    movieSelected,
                    MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_POSTER_PATH,
                    FavoritesUtils.IMAGE_TYPE_POSTER));

            // Backdrop
            fillMovieBackdropDetailsFromDB(loadImageFromDatabase(
                    this,
                    movieSelected,
                    MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_BACKDROP,
                    FavoritesUtils.IMAGE_TYPE_BACKDROP));

            // Trailers
            displayTrailersBitmapsOnUI(loadMovieTrailersFromDatabase(this, movieSelected),
                    loadTrailerKeysFromDatabase(this));

        } else {
            // Poster
            loadMoviePoster(posterPath);
            // Backdrop
            loadMovieBackdrop(movieBackdropPath);
        }
    }

    /**
     * Sets the poster ImageView to be the corresponding poster image
     *
     * @param posterImage A Bitmap representing the poster
     */
    private void fillMoviePosterDetailsFromDB(Bitmap posterImage) {
        mMoviePosterView.setImageBitmap(posterImage);
        mFloatingActionButtonFavorite.setVisibility(View.VISIBLE);
    }

    /**
     * Sets the Backdrop ImageView to be the corresponding backdrop image
     *
     * @param backdropImage A Bitmap representing the movie Backdrop
     */
    private void fillMovieBackdropDetailsFromDB(Bitmap backdropImage) {
        mMovieBackdropView.setImageBitmap(backdropImage);
    }

    /**
     * Sets the floating button image depending on whether
     * the movie is one of the user's favorite or not
     */
    private void setFloatingButtonImage() {
        if (mIsMovieSelectedFavorite) {
            mFloatingActionButtonFavorite.setImageResource(R.drawable.heart_pressed_white);
        } else {
            mFloatingActionButtonFavorite.setImageResource(R.drawable.heart_not_pressed);
        }
    }

    /**
     * Load Movie poster into the corresponding view
     * using the Picasso library.
     *
     * @param posterPath URL to fetch the movie poster
     */
    private void loadMoviePoster(String posterPath) {
        if (posterPath != null) {
            with(this)
                    .load(posterPath)
                    .placeholder(R.drawable.placeholder)
                    .resize(200, 300)
                    .error(R.drawable.movie_details_error)
                    .into(mMoviePosterView);
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
                    .into(mMovieBackdropView);
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
     * Creates ImageViews for each movie trailer, appends it to the corresponding ViewGroup
     * sets its properties and add an onClickListener
     *
     * @param data
     */
    private void createMovieTrailers(String data) {
        try {
            JSONObject trailersJSON = new JSONObject(data);
            JSONArray trailersArray = trailersJSON.getJSONArray("results");

            // Iterate over the trailers array
            for (int i = 0; i < trailersArray.length(); i++) {

                JSONObject trailer = trailersArray.getJSONObject(i);
                final String trailerKey = trailer.getString("key");

                //Create ImageView, set its properties and add it to the layout
                ImageView trailerView = createTrailerView(this, mMovieDetailsTrailerLinearContainer, i, trailerKey);

                // Load the thumbnail
                loadMovieTrailerThumbnail(trailerView, trailerKey);

                // Set onClick listener
                setTrailerOnClickListener(this, trailerView, trailerKey);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates an ImageView and sets its properties for a movie trailer thumbnail
     *
     * @param context    Context of the activity that called this method
     * @param container  The Layout that will contain the trailer's ImageView
     * @param index      The index in which the ImageView must by inserted to the container view
     * @param trailerKey The trailer's key
     * @return The trailer's ImageView with custom properties
     */
    private static ImageView createTrailerView(Context context, LinearLayout container, int index, String trailerKey) {
        ImageView trailerView = new ImageView(context);
        setTrailerViewProperties(context, trailerView, trailerKey);
        container.addView(trailerView, index);

        return trailerView;
    }

    /**
     * Loads the movie trailer thumbnail from Youtube
     *
     * @param trailerKey The corresponding trailer's key
     */
    private void loadMovieTrailerThumbnail(ImageView trailerView, String trailerKey) {

        // Create Trailer URL and assign add to the movie object
        String searchURL = TRAILER_THUMBNAIL_BASE_PATH + trailerKey + "/0.jpg";

        if(movieSelected.getMovieTrailersThumbnails() != null) {
            movieSelected.getMovieTrailersThumbnails().add(new MovieTrailerThumbnail(searchURL, trailerKey));
        }

        // Load the thumbnail with Picasso
        Picasso.with(this)
                .load(searchURL)
                .placeholder(generateGradientDrawable())
                .error(generateGradientDrawable())
                .into(trailerView);
    }

    // Methods for UI interaction ===================================================================

    /**
     * Sets an onClickListener for the trailer ImageView to launch an implicit
     * intent to the Youtube URL that corresponds to the trailer.
     *
     * @param trailerView The trailer's ImageView
     * @param trailerKey  The trailer's key to append to Youtube's URL
     */
    private static void setTrailerOnClickListener(final Context context, ImageView trailerView, final String trailerKey) {
        trailerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchTrailer(context, trailerKey);
            }
        });
    }

    /**
     * Launches a movie trailer on Youtube with an implicit intent
     *
     * @param trailerKey The trailer's key to add to Youtube's base path
     */
    private static void launchTrailer(Context context, String trailerKey) {
        Uri youtubeLink = Uri.parse(YOUTUBE_BASE_PATH + trailerKey);
        Intent intent = new Intent(Intent.ACTION_VIEW, youtubeLink);

        // If there is an app to handle the intent
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

    /**
     * Adds an onClickListener to the "Read Reviews" TextView
     * to launch an activity that displays the reviews
     */
    private void addReadReviewsOnClickListener() {
        mReviewsReadMoreView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = mContext;
                Class destinationActivity = ReviewsActivity.class;

                // Intent
                Intent intent = new Intent(context, destinationActivity);
                intent.putExtra(MainActivity.INTENT_MOVIE_OBJECT_KEY, movieSelected);

                startActivity(intent);
            }
        });
    }

    /**
     * Adds onClickListeners to the floating button
     */
    private void addOnClickListenerToFloatingActionButton() {

        mFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (movieSelected.getIsMovieFavorite()) {
                    mIsMovieSelectedFavorite = false;
                    // Remove it from the database and update UI
                    removeMovieFromFavorites(mContext, movieSelected);
                    updateRemovedFromFavoritesUI();
                } else {
                    mIsMovieSelectedFavorite = true;
                    // Add it to the database and update UI
                    addMovieToFavorites(mContext, movieSelected);
                    updateAddedToFavoritesUI();
                }
            }
        });
    }

    private static void removeMovieFromFavorites(Context context, Movie movieSelected) {

        movieSelected.setIsMovieFavorite(false);

        // Remove the MovieDB ID from shared preferences to update UI immediately
        // in case the user returns to the Main Activity before the deletion
        // operation is completed
        FavoritesUtils.removeFavoriteFromSharedPreferences(context, movieSelected);

        // Start a service that will remove the movie from the database
        Intent intent = new Intent(context, FavoritesDataIntentService.class);
        intent.setAction(DBServiceTasks.ACTION_REMOVE_FAVORITE);
        intent.putExtra("movieObject", movieSelected);
        context.startService(intent);
    }

    /**
     * Updates the UI by changing the favorites floating action button logo
     * and displays a message saying that the movie has been removed from "Favorites"
     */
    private void updateRemovedFromFavoritesUI() {
        setFloatingButtonImage();
        Toast.makeText(mContext,
                '"' + movieSelected.getMovieTitle() + '"' + " removed from Favorites :-(", // Message
                Toast.LENGTH_SHORT)
                .show();
    }

    /**
     * Adds the movie to SharedPreferences and to the "Favorites" table in the database
     *
     * @param context       The Context of the Activity that called this method
     * @param movieSelected Movie selected by the user
     */
    private static void addMovieToFavorites(Context context, Movie movieSelected) {

        movieSelected.setIsMovieFavorite(true);

        // Add MovieDBId to Shared Preferences to update UI
        // immediately in case the user returns to the Main Activity
        // before the insertion operation is completed
        FavoritesUtils.addFavoriteToSharedPreferences(context, movieSelected);

        // Add Movie to the database
        FavoritesUtils.addFavoriteToDatabase(context, movieSelected);
    }

    /**
     * Updates the UI by setting the correct favorites floating action button logo
     * and displaying a message saying that the movie has been added to "Favorites"
     */
    private void updateAddedToFavoritesUI() {
        setFloatingButtonImage();
        Toast.makeText(mContext,
                '"' + movieSelected.getMovieTitle() + '"' + " added to Favorites!",
                Toast.LENGTH_SHORT)
                .show();
    }

    // Methods to set UI properties ===================================================================

    /**
     * Sets the Trailer ImageView properties.
     *
     * @param context     The context of the activity that called this method
     * @param trailerView The ImageView
     * @param trailerKey  The Trailer's key
     */
    private static void setTrailerViewProperties(Context context, ImageView trailerView, String trailerKey) {

        // Set dimensions
        int width = convertDpToPixels(context.getResources().getInteger(R.integer.trailerWidth), context);
        int height = convertDpToPixels(context.getResources().getInteger(R.integer.trailerHeight), context);

        // Create Layout Parameters
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);

        // Set margin
        int marginEnd = convertDpToPixels(10, context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            params.setMarginEnd(marginEnd);
        } else {
            params.setMargins(marginEnd, marginEnd, marginEnd, marginEnd);
        }

        if(trailerKey != null) {
            //Set tag
            trailerView.setTag(trailerKey);
        }

        // Include parameters
        trailerView.setLayoutParams(params);

        trailerView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        //Image scale type
        trailerView.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }

    /**
     * Converts Dp to Pixels to use when setting LayoutParams
     *
     * @param dimensionInDp The dimension to convert
     * @return The dimension passed as argument in pixels
     */
    private static int convertDpToPixels(int dimensionInDp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dimensionInDp,
                context.getResources().getDisplayMetrics());
    }

    /**
     * Generates a gradient drawable from the app's primary color
     * to act as I as a placeholder or to display in case of error
     * loading movie backdrop
     *
     * @return A GradientDrawable of the app's primary color
     */
    private GradientDrawable generateGradientDrawable() {
        // Check if there isn't a gradient already defined.
        // Else, create a gradient that matches the primary
        // color of the app
        if (mGradient != null) {
            return mGradient;
        } else {
            GradientDrawable gradient = new GradientDrawable();
            gradient.setShape(GradientDrawable.RECTANGLE);
            gradient.setColor(ContextCompat.getColor(this, R.color.colorPrimary));

            // Assign the gradient to a member variable
            // for reuse
            mGradient = gradient;

            return gradient;
        }
    }

    // Movie details methods ===================================================================

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

            // Set the selected movie's properties from the JSON object
            movieSelected.setMovieLanguage(movieDetailsJSON.getString("original_language"));
            movieSelected.setMovieRuntime(movieDetailsJSON.getDouble("runtime"));
            movieSelected.setIsMovieForAdults(movieDetailsJSON.getBoolean("adult"));
            movieSelected.setMovieBackdropPath(movieDetailsJSON.getString("backdrop_path"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generate a full backdrop URL to load image from MovieDB API
     *
     * @param backdropPath The final piece of the path to the movie's backdrop image
     * @return A full URL to request the image
     */
    public static String createFullBackdropPath(String backdropPath) {
        return MOVIEDB_POSTER_BASE_URL + mContext.getResources().getString(R.string.backdrop_size) + backdropPath;
    }

    /**
     * Creates an ArrayList of strings from a JSON in string format
     *
     * @param stringCast JSON in String format containing the results
     *                   of requesting the movie's cast to MovieDB API
     */
    private void extractMovieCastArrayFromJSON(String stringCast) {

        JSONObject jsonCast = null;
        ArrayList<String> castArray = new ArrayList<String>();

        try {
            jsonCast = new JSONObject(stringCast);
            JSONArray arrayJSONCast = jsonCast.getJSONArray("cast");

            for (int i = 0; i < NUMBER_OF_ACTORS_TO_INCLUDE; i++) {
                castArray.add(arrayJSONCast.getJSONObject(i).getString("name"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        movieSelected.setMovieCast(castArray);
    }


    //  Database methods ===============================================================


    /**
     * Triggers the appropriate loader to load the corresponding movie data
     *
     * @param loaderID The Loader's ID
     */
    private void loadDataFromDatabase(int loaderID) {

        Bundle detailsBundle = createDetailsBundle();

        // Loader Manager
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> detailsLoader = loaderManager.getLoader(loaderID);

        if (detailsLoader == null) {
            loaderManager.initLoader(loaderID, detailsBundle, new DatabaseMovieDetailsLoader(this));
        } else {
            loaderManager.restartLoader(loaderID, detailsBundle, new DatabaseMovieDetailsLoader(this));
        }
    }

    /**
     * CursorLoader to load data from the database
     */
    private class DatabaseMovieDetailsLoader implements LoaderManager.LoaderCallbacks<Cursor> {

        private Context mContext;

        private DatabaseMovieDetailsLoader(Context context) {
            mContext = context;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {

            Uri uri = MoviesDBContract.FavoriteMoviesEntry.CONTENT_URI
                    .buildUpon()
                    .appendPath(Integer.toString(movieSelected.getMovieId()))
                    .build();

            switch (id) {
                case FAVORITE_MOVIES_LOADER_BY_ID:
                    return new CursorLoader(mContext,
                            uri,
                            null,
                            null,
                            null,
                            MoviesDBContract.FavoriteMoviesEntry._ID);
                default:
                    throw new RuntimeException("Loader not implemented: " + id);
            }
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data.getCount() > 0) {

                loadMovieDetailsFromDB(data);
                fillMovieData(movieSelected);

                addOnClickListenerToFloatingActionButton();

                // Make UI visible to the user
                mDetailsProgressBar.setVisibility(View.GONE);
                mDetailsLayout.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mDetailsProgressBar.setVisibility(View.GONE);
        }
    }

    /**
     * Loads movie details from the database
     *
     * @param movieDetailsCursor A cursor containing the movie's data retrieved from the database
     */
    private void loadMovieDetailsFromDB(Cursor movieDetailsCursor) {

        /*
         * Get data for each movie from the Cursor and assign it to the Movie object
         */
        while (movieDetailsCursor.moveToNext()) {

            /*
             Retrieve data
             */

            // Language
            String movieLanguage = LoaderUtils.getStringFromCursor(movieDetailsCursor,
                    MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_LANGUAGE);
            // Runtime
            String movieRuntime = LoaderUtils.getStringFromCursor(movieDetailsCursor,
                    MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_RUNTIME);
            // Cast
            String movieCast = LoaderUtils.getStringFromCursor(movieDetailsCursor,
                    MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_CAST);
            // Reviews / Author
            String movieReviewsAuthor = LoaderUtils.getStringFromCursor(movieDetailsCursor,
                    MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_REVIEWS_AUTHOR);
            // Reviews / Text
            String movieReviewsText = LoaderUtils.getStringFromCursor(movieDetailsCursor,
                    MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_REVIEWS_TEXT);
            // Is the movie for Adults?
            String movieIsForAdults = LoaderUtils.getStringFromCursor(movieDetailsCursor,
                    MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_IS_FOR_ADULTS);
            // Backdrop internal storage path
            String movieBackdropPath = LoaderUtils.getStringFromCursor(movieDetailsCursor,
                    MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_BACKDROP);
            // Trailer thumbnails internal storage paths
            String movieTrailersThumbnails = LoaderUtils.getStringFromCursor(movieDetailsCursor,
                    MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_TRAILERS_THUMBNAILS);

             /*
              Assign data
              */

            movieSelected.setMovieLanguage(movieLanguage);
            movieSelected.setMovieRuntime(Double.parseDouble(movieRuntime));
            movieSelected.setIsMovieForAdults(Boolean.parseBoolean(movieIsForAdults));
            movieSelected.setMovieBackdropPath(movieBackdropPath);

            fillMovieReviewsFromDB(movieReviewsAuthor, movieReviewsText);
            fillMovieCastFromDB(movieCast);
        }
    }

    /**
     * Formats the movie reviews from the data retrieved from the database and assigns
     * this new ArrayList to the movie selected
     *
     * @param movieReviewsAuthor A String containing reviews authors
     * @param movieReviewsText   A String containing reviews text
     */
    private void fillMovieReviewsFromDB(String movieReviewsAuthor, String movieReviewsText) {

        ArrayList<MovieReview> movieReviewsArrayList = new ArrayList<MovieReview>();

        // Extract movie reviews authors and text
        String[] reviewAuthorsArray = movieReviewsAuthor.split(DBServiceTasks.CHARACTER_SEPARATING_REVIEWS_AUTHORS);
        String[] reviewTextArray = movieReviewsText.split(DBServiceTasks.CHARACTER_SEPARATING_REVIEWS_TEXT);

        // Add MovieReview objects with their corresponding author and text
        for (int i = 0; i < reviewAuthorsArray.length; i++) {
            movieReviewsArrayList.add(new MovieReview(reviewAuthorsArray[i], reviewTextArray[i]));
        }

        // Assign the MovieReviews ArrayList to the movie object
        movieSelected.setMovieReviews(movieReviewsArrayList);
    }

    /**
     * Format the cast string retrieved from the database into
     * an ArrayList and set it as the cast attribute of the movie selected
     *
     * @param movieCast A String containing the cast names separated by DBServiceTasks.CHARACTER_SEPARATING_CAST_MEMBERS
     */
    private void fillMovieCastFromDB(String movieCast) {

        ArrayList<String> castArray = new ArrayList<>();

        // Add a member of the cast
        for (String castMember : movieCast.substring(1, movieCast.length() - 1).split(DBServiceTasks.CHARACTER_SEPARATING_CAST_MEMBERS)) {
            castArray.add(castMember);
        }

        // Assign the ArrayList with cast members strings to the movie object
        movieSelected.setMovieCast(castArray);
    }

    /**
     * Loads a movie image (poster or backdrop) from internal storage
     *
     * @param context Context of the activity that called this method
     * @param movieSelected Movie object selected by the user
     * @param databaseColumnName Database column name with the corresponding image category
     * @param imageType The corresponding image type
     *
     * @return The image as a Bitmap
     */
    public static Bitmap loadImageFromDatabase(Context context, Movie movieSelected, String databaseColumnName, String imageType) {

        String[] projection = {
                databaseColumnName
        };

        Cursor pathCursor = context.getContentResolver().query(
                mMovieSelectedUri,
                projection,
                null,
                null,
                MoviesDBContract.FavoriteMoviesEntry._ID);

        pathCursor.moveToFirst();

        String imagePath = pathCursor.getString(pathCursor.getColumnIndex(databaseColumnName));

        pathCursor.close();

        return ImagesDBUtils.loadImageFromStorage(
                imagePath,
                Integer.toString(movieSelected.getMovieId()),
                imageType,
                -1);
    }

    /**
     * Creates an ArrayList of Bitmaps representing the movie's trailers thumbnails from
     * the trailer thumbnails data retrieved from the database
     *
     * @param context Context of the activity
     * @param movieSelected Movie selected by the user
     *
     * @return An ArrayList of Bitmaps of the trailers thumbnails
     */
    private static ArrayList<Bitmap> loadMovieTrailersFromDatabase(Context context, Movie movieSelected) {

        ArrayList<Bitmap> trailersBitmapArray = new ArrayList<>();

        String[] trailersArray = queryTrailersArray(context);

        // Get individual trailer elements from the string by splitting the at the corresponding character
        // and split each element again to retrieve the trailer path
        int i = 0;
        for (String trailer : trailersArray) {

            String trailerPath = trailer.split(FavoritesUtils.CHARACTER_TO_SEPARATE_THUMBNAIL_TAG)[0];

            // Add the Bitmap resource to the array
            trailersBitmapArray.add(ImagesDBUtils.loadImageFromStorage(
                    trailerPath,
                    Integer.toString(movieSelected.getMovieId()),
                    FavoritesUtils.IMAGE_TYPE_TRAILER_THUMBNAIL,
                    i));

            // Increase counter
            i++;
        }

        return trailersBitmapArray;
    }

    /**
     * Creates an ArrayList of Strings with the trailers keys retrieved from the database
     *
     * @param context The activity context
     *
     * @return an ArrayList of Strings with the trailers keys
     */
    private static ArrayList<String> loadTrailerKeysFromDatabase(Context context) {

        ArrayList<String> trailerTagArray = new ArrayList<String>();

        String[] trailersArray = queryTrailersArray(context);

        // For each individual trailer, extract its tag
        for (String trailer : trailersArray) {

            String[] trailerData = trailer.split(FavoritesUtils.CHARACTER_TO_SEPARATE_THUMBNAIL_TAG);

            if(trailerData.length > 1) {
                String trailerKey = trailerData[1];
                trailerTagArray.add(trailerKey);
            } else {
                trailerTagArray.add(null);
            }
        }

        return trailerTagArray;
    }

    /**
     * Queries the database for movie trailer thumbnails data (internal storage path
     * and trailer keys)
     *
     * @param context Context of the activity that called this method
     *
     * @return An array of Strings that represent the movie thumbnails with their corresponding data
     */
    private static String[] queryTrailersArray(Context context) {
        String[] posterProjection = {
                MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_TRAILERS_THUMBNAILS
        };

        Cursor trailersPathsCursor = context.getContentResolver().query(
                mMovieSelectedUri,
                posterProjection,
                null,
                null,
                MoviesDBContract.FavoriteMoviesEntry._ID);

        trailersPathsCursor.moveToFirst();

        String trailersDataString = LoaderUtils.getStringFromCursor(trailersPathsCursor,
                MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_TRAILERS_THUMBNAILS);

        trailersPathsCursor.close();

        // Get an array of individual movie trailer thumbnails data
        // The string structure stored in the database is:
        // "<thumbnailInternalStoragePath> CHARACTER_TO_SEPARATE_THUMBNAIL_TAG <thumbnailKey> CHARACTER_TO_SEPARATE_THUMBNAIL ... another thumbnail>"
        String[] trailersArray = trailersDataString.split(FavoritesUtils.CHARACTER_TO_SEPARATE_THUMBNAILS);

        return trailersArray;
    }


    /**
     * Creates an ImageView for each trailer, sets its properties and adds it to the layout
     *
     * @param trailersThumbnails ArrayList of Bitmaps representing the trailers thumbnails
     * @param trailersKeys ArrayList of Strings with the trailers keys
     */
    private void displayTrailersBitmapsOnUI(ArrayList<Bitmap> trailersThumbnails, ArrayList<String> trailersKeys) {

        for (int i = 0; i < trailersThumbnails.size(); i++) {
            //Create ImageView, set its properties and add it to the layout
            ImageView trailerView = new ImageView(this);
            setTrailerViewProperties(this, trailerView, trailersKeys.get(i));

            if(trailersKeys.get(i) != null) {
                setTrailerOnClickListener(this, trailerView, trailersKeys.get(i));
            }

            mMovieDetailsTrailerLinearContainer.addView(trailerView, i);
            trailerView.setImageBitmap(trailersThumbnails.get(i));
        }
    }
}