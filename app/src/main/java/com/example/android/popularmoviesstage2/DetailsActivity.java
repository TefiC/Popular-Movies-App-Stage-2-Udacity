package com.example.android.popularmoviesstage2;

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
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmoviesstage2.DataUtils.DataInsertionTasks;
import com.example.android.popularmoviesstage2.DataUtils.FavoritesUtils;
import com.example.android.popularmoviesstage2.DataUtils.MoviesDBContract;
import com.example.android.popularmoviesstage2.utils.FavoritesDataIntentService;
import com.example.android.popularmoviesstage2.utils.LoaderUtils;
import com.example.android.popularmoviesstage2.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import static com.example.android.popularmoviesstage2.R.id.favorite_floating_button;
import static com.example.android.popularmoviesstage2.utils.LoaderUtils.CAST_SEARCH_LOADER;
import static com.example.android.popularmoviesstage2.utils.LoaderUtils.FAVORITE_MOVIES_LOADER_BY_ID;
import static com.example.android.popularmoviesstage2.utils.LoaderUtils.REVIEWS_LOADER;
import static com.example.android.popularmoviesstage2.utils.LoaderUtils.TRAILERS_SEARCH_LOADER;
import static com.squareup.picasso.Picasso.with;

public class DetailsActivity extends AppCompatActivity {

    /*
     * Fields
     */

    public Movie movieSelected;

    private ImageView moviePosterView;
    private TextView movieVoteAverageView;
    private TextView movieReleaseView;
    private TextView moviePlotView;
    private TextView movieTitleView;
    private TextView movieLanguageView;
    private TextView movieRuntimeView;
    private TextView movieCastView;
    private ImageView movieBackdropView;
    private TextView reviewsReadMoreView;
    private LinearLayout movieDetailsTrailerLinearContainer;
    private FloatingActionButton floatingActionButtonFavorite;

    private RelativeLayout mDetailsLayout;
    private ProgressBar mDetailsProgressBar;

    private GradientDrawable mGradient;

    private Context mContext;

    /*
     * Constants
     */

    private static final String NOT_AVAILABLE = "Not available";

    // Constants to form the movie poster URL
    public static final String MOVIEDB_POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
    public static final String IMAGE_SIZE = "w185";

    // Backdrop parameters
    public static final String BACKDROP_SIZE = "w300";

    //Get movie trailer thumbnail
    private static final String TRAILER_THUMBNAIL_BASE_PATH = "https://img.youtube.com/vi/";

    //Launch trailer
    private static final String YOUTUBE_BASE_PATH = "https://www.youtube.com/watch?v=";

    private static final int NUMBER_OF_ACTORS_TO_INCLUDE = 5;


    private static final String TAG = DetailsActivity.class.getSimpleName();

    /*
     * Methods
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_second);

        mContext = this;

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

            movieSelected.setIsMovieFavorite(FavoritesUtils.checkIfMovieIsFavorite(this, Integer.toString(movie.getMovieId())));

            if (movieSelected.getIsMovieFavorite()) {
                movie.setIsMovieFavorite(true);
                loadDataFromDatabase(LoaderUtils.FAVORITE_MOVIES_LOADER_BY_ID);

            } else {
                //Start AsyncTaskLoader
                loadDataFromInternet(LoaderUtils.DETAILS_SEARCH_LOADER);
//                loadDataFromInternet(LoaderUtils.TRAILERS_SEARCH_LOADER);
            }
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

        movieCastView = (TextView) findViewById(R.id.details_cast_text);

        movieDetailsTrailerLinearContainer = (LinearLayout) findViewById(R.id.movie_details_trailers_container);

        floatingActionButtonFavorite = (FloatingActionButton) findViewById(R.id.favorite_floating_button);

        mDetailsLayout = (RelativeLayout) findViewById(R.id.details_relative_layout);

        mDetailsProgressBar = (ProgressBar) findViewById(R.id.details_progress_bar);

        reviewsReadMoreView = (TextView) findViewById(R.id.movie_details_reviews_read_more);
    }

    /**
     * Find the toolbar and sets a support action bar
     */
    public void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    // Network request methods ===================================================================

    // AsyncTaskLoader ==========================
    private String cachedDetails;
    private String cachedTrailers;
    private String cachedCast;
    private String cachedReviews;


    private class InternetLoader implements LoaderManager.LoaderCallbacks<String> {
        private Context mContext;

        public InternetLoader(Context context) {
            mContext = context;
        }

        @Override
        public Loader<String> onCreateLoader(final int id, final Bundle args) {
            return new AsyncTaskLoader<String>(mContext) {

                @Override
                protected void onStartLoading() {
                    super.onStartLoading();
                    if (args == null) {
                        return;
                    }

                    switch (id) {
                        case LoaderUtils.DETAILS_SEARCH_LOADER:
                            mDetailsProgressBar.setVisibility(View.VISIBLE);
                            if (cachedDetails == null) {
                                forceLoad();
                            } else {
                                deliverResult(cachedDetails);
                            }
                            break;
                        case LoaderUtils.TRAILERS_SEARCH_LOADER:
                            if (cachedTrailers == null) {
                                forceLoad();
                            } else {
                                deliverResult(cachedTrailers);
                            }
                            break;
                        case LoaderUtils.CAST_SEARCH_LOADER:
                            if (cachedCast == null) {
                                forceLoad();
                            } else {
                                deliverResult(cachedCast);
                            }
                            break;
                        case LoaderUtils.REVIEWS_LOADER:
                            if (cachedReviews == null) {
                                forceLoad();
                            } else {
                                deliverResult(cachedReviews);
                            }
                            break;
                    }
                }

                @Override
                public void deliverResult(String data) {
                    switch (id) {
                        case LoaderUtils.DETAILS_SEARCH_LOADER:
                            cachedDetails = data;
                            break;
                        case LoaderUtils.TRAILERS_SEARCH_LOADER:
                            cachedTrailers = data;
                            break;
                        case LoaderUtils.CAST_SEARCH_LOADER:
                            cachedCast = data;
                            break;
                        case LoaderUtils.REVIEWS_LOADER:
                            cachedReviews = data;
                            break;
                    }
                    super.deliverResult(data);
                }

                @Override
                public String loadInBackground() {
                    String searchResults = null;
                    URL searchQueryURL = null;
                    Movie movie = args.getParcelable("movieObject");

                    switch (id) {
                        case LoaderUtils.DETAILS_SEARCH_LOADER:
                            searchQueryURL = NetworkUtils.buildMovieDetailsUrl(movie.getMovieId());
                            break;
                        case LoaderUtils.TRAILERS_SEARCH_LOADER:
                            searchQueryURL = NetworkUtils.buildMovieTrailersUrl(movie.getMovieId());
                            break;
                        case LoaderUtils.CAST_SEARCH_LOADER:
                            searchQueryURL = NetworkUtils.buildMovieCastUrl(movie.getMovieId());
                            break;
                        case LoaderUtils.REVIEWS_LOADER:
                            searchQueryURL = NetworkUtils.buildMovieReviewsUrl(movie.getMovieId());
                            break;
                    }

                    if (searchQueryURL != null) {
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
//                    Log.v("DB", "LOADING DATA FROM INTERNET");
                    addMovieDetails(data, movieSelected);
                    loadDataFromInternet(CAST_SEARCH_LOADER);
                    break;
                case LoaderUtils.CAST_SEARCH_LOADER:
//                    Log.v("CAST", "LOADING CAST");
//                    Log.v("CAST", data);
                    loadDataFromInternet(TRAILERS_SEARCH_LOADER);
                    extractMovieCastArrayFromJSON(data);
                    fillMovieData(movieSelected);
                    mDetailsProgressBar.setVisibility(View.GONE);
                    mDetailsLayout.setVisibility(View.VISIBLE);
                    break;
                case LoaderUtils.TRAILERS_SEARCH_LOADER:
                    createMovieTrailers(data, movieSelected);

                    loadDataFromInternet(REVIEWS_LOADER);
                    break;
                case LoaderUtils.REVIEWS_LOADER:

                    movieSelected.setMovieReviews(ReviewsActivity.formatJSONfromReviewsString(data, movieSelected));

                    addOnClickListenerToFloatingActionButtons();
                    floatingActionButtonFavorite.setVisibility(View.VISIBLE);
                    break;
            }
        }

        @Override
        public void onLoaderReset(Loader<String> loader) {
            //
        }
    }

    /**
     * Triggers the appropriate loader to load the corresponding movie data
     *
     * @param loaderID The Loader's ID
     */
    private void loadDataFromInternet(int loaderID) {
//        Log.v("DB", "LOADING FROM INTERNET");
        Bundle detailsBundle = createDetailsBundle();

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> detailsLoader = loaderManager.getLoader(loaderID);

        if (detailsLoader == null) {
            loaderManager.initLoader(loaderID, detailsBundle, new InternetLoader(this));
        } else {
            loaderManager.restartLoader(loaderID, detailsBundle, new InternetLoader(this));
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

    // Methods to update UI ===================================================================

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
        if (movieSelected.getIsMovieFavorite() && movieSelected.getMoviePosterPath() != null) {
//            Log.v("DB", "LOADING POSTER IN DETAILS");
            fillMoviePosterDetailsFromDB(loadPosterFromDatabase(this, movieSelected));
        } else {
            loadMoviePoster(posterPath);
        }

        loadMovieBackdrop(movieBackdropPath);

        //Determine if it is favorite or not and update button
        movieSelected.setIsMovieFavorite(FavoritesUtils.checkIfMovieIsFavorite(this, Integer.toString(movieSelected.getMovieId())));
        setFloatingButtonImage();

        setViewData(movieVoteAverageView, voteAverage.toString());
        setViewData(movieReleaseView, releaseDate);

        setViewData(moviePlotView, moviePlot);
        setViewData(movieTitleView, movieTitle);

        setViewData(movieLanguageView, movieLanguage);
        setViewData(movieRuntimeView, movieRuntime);

        ArrayList<String> movieCast = movieSelected.getMovieCast();


        // Add cast
        int i;
        for (i = 0; i < NUMBER_OF_ACTORS_TO_INCLUDE; i++) {
            movieCastView.append(movieCast.get(i) + "\n");
        }

        reviewsReadMoreView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = mContext;
                Class destinationActivity = ReviewsActivity.class;

                // Intent
                Intent intent = new Intent(context, destinationActivity);
                intent.putExtra("movieObject", movieSelected);

                startActivity(intent);
            }
        });
    }

    /**
     * Sets the floating button image depending on whether
     * the movie is one of the user's favorite or not
     */
    private void setFloatingButtonImage() {
        if (movieSelected.getIsMovieFavorite()) {
            floatingActionButtonFavorite.setImageResource(R.drawable.heart_pressed_white);
        } else {
            floatingActionButtonFavorite.setImageResource(R.drawable.heart_not_pressed);
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
            with(this)
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

    /**
     * Creates ImageViews for each movie trailer, appends it to the corresponding ViewGroup
     * sets its properties and add an onClickListener
     *
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

    /**
     * Loads the movie trailer thumbnail from Youtube
     *
     * @param trailerKey The corresponding trailer's key
     */
    private void loadMovieTrailerThumbnail(ImageView trailerView, String trailerKey) {
        String searchURL = TRAILER_THUMBNAIL_BASE_PATH + trailerKey + "/0.jpg";

        Log.v("Service details", movieSelected.getMovieTrailersThumbnails().toString());

        // Update in reference
        if(searchURL != null) {
            movieSelected.getMovieTrailersThumbnails().add(searchURL);
        }


        Log.v(TAG, "MOVIE THUMBNAILS: " + movieSelected.getMovieTrailersThumbnails().toString());

        Picasso.with(this)
                .load(searchURL)
                .placeholder(generateGradientDrawable())
                .error(generateGradientDrawable())
                .into(trailerView);
    }

    // Methods for UI interaction ===================================================================

    /**
     * Launches a movie trailer on Youtube with an implicit intent
     *
     * @param trailerKey The trailer's key to add to Youtube's base path
     */
    private void launchTrailer(String trailerKey) {
        Uri youtubeLink = Uri.parse(YOUTUBE_BASE_PATH + trailerKey);
        Intent intent = new Intent(Intent.ACTION_VIEW, youtubeLink);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    /**
     * Sets an onClickListener for the trailer ImageView to launch an implicit
     * intent to the Youtube URL that corresponds to the trailer.
     *
     * @param trailerView The trailer's ImageView
     * @param trailerKey  The trailer's key to append to Youtube's URL
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
     * Adds onClickListeners for the floating buttons
     */
    private void addOnClickListenerToFloatingActionButtons() {


        FloatingActionButton favoriteButton = (FloatingActionButton) findViewById(favorite_floating_button);

        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                Log.v(TAG, "FAVORITE CLICKED");
//                Log.v(TAG, "IS FAVORITE: " + movieSelected.getIsMovieFavorite());
                if (movieSelected.getIsMovieFavorite()) {
                    removeMovieFromFavorites(mContext, movieSelected);
                    updateRemovedFromFavoritesUI();
                } else {
                    addMovieToFavorites(mContext, movieSelected);
                    updateAddedToFavoritesUI();
                }
            }
        });
    }

    public static void removeMovieFromFavorites(Context context, Movie movieSelected) {
//        Log.v("DB", "REMOVING POSTER FROM DATABASE");
        movieSelected.setIsMovieFavorite(false);

        Intent intent = new Intent(context, FavoritesDataIntentService.class);
        intent.setAction(DataInsertionTasks.ACTION_REMOVE_FAVORITE);
        intent.putExtra("movieObject", movieSelected);
        context.startService(intent);
    }

    public void updateRemovedFromFavoritesUI() {
        setFloatingButtonImage();
        Toast.makeText(mContext, '"' + movieSelected.getMovieTitle() + '"' + " removed from Favorites :-(", Toast.LENGTH_SHORT).show();
    }

    public static void addMovieToFavorites(Context context, Movie movieSelected) {
        movieSelected.setIsMovieFavorite(true);

        FavoritesUtils.addFavoriteToDatabase(context, movieSelected);
    }

    public void updateAddedToFavoritesUI() {
        setFloatingButtonImage();
        Toast.makeText(mContext, '"' + movieSelected.getMovieTitle() + '"' + " added to Favorites!", Toast.LENGTH_SHORT).show();
    }


    // Methods for UI properties ===================================================================

    /**
     * Sets the Trailer ImageView properties.
     *
     * @param trailerView The ImageView
     * @param trailerKey  The Trailer's key
     */
    private void setTrailerViewProperties(ImageView trailerView, String trailerKey) {

        // Set dimensions
        int height = convertDpToPixels(120);
        int width = convertDpToPixels(150);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);

        // Set margin
        int marginEnd = convertDpToPixels(10);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
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
     *
     * @param dimensionInDp The dimension to convert
     * @return The dimention passed as argument in pixels
     */
    private int convertDpToPixels(int dimensionInDp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dimensionInDp, getResources().getDisplayMetrics());
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


    // Movie data methods ===================================================================


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
     * Generate a full backdrop URL to load image from MovieDB API
     *
     * @param backdropPath The final piece of the path to the movie's backdrop image
     * @return A full URL to request the image
     */
    public static String createFullBackdropPath(String backdropPath) {
        return MOVIEDB_POSTER_BASE_URL + BACKDROP_SIZE + backdropPath;
    }


    //  Database methods ===============================================================

    /**
     * Triggers the appropriate loader to load the corresponding movie data
     *
     * @param loaderID The Loader's ID
     */
    private void loadDataFromDatabase(int loaderID) {
        Bundle detailsBundle = createDetailsBundle();

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> detailsLoader = loaderManager.getLoader(loaderID);

        if (detailsLoader == null) {
            loaderManager.initLoader(loaderID, detailsBundle, new DatabaseMovieDetailsLoader(this));
        } else {
            loaderManager.restartLoader(loaderID, detailsBundle, new DatabaseMovieDetailsLoader(this));
        }
    }

    /**
     * Loader cursor to load from the database
     */
    private class DatabaseMovieDetailsLoader implements LoaderManager.LoaderCallbacks<Cursor> {

        private Context mContext;

        public DatabaseMovieDetailsLoader(Context context) {
            mContext = context;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {

            switch (id) {
                case FAVORITE_MOVIES_LOADER_BY_ID:
                    return new CursorLoader(mContext,
                            MoviesDBContract.FavoriteMoviesEntry.CONTENT_URI
                                    .buildUpon()
                                    .appendPath(Integer.toString(movieSelected.getMovieId()))
                                    .build(),
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

                addOnClickListenerToFloatingActionButtons();
                mDetailsProgressBar.setVisibility(View.GONE);
                mDetailsLayout.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            //
        }
    }

    /**
     * Loads movie details from the database
     *
     * @param movieDetailsCursor A cursor containing the movie's data retrieved from the database
     */
    private void loadMovieDetailsFromDB(Cursor movieDetailsCursor) {

        while (movieDetailsCursor.moveToNext()) {

            String movieLanguage = MainActivity.getStringFromCursor(movieDetailsCursor,
                    MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_LANGUAGE);
            String movieRuntime = MainActivity.getStringFromCursor(movieDetailsCursor,
                    MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_RUNTIME);
            String movieCast = MainActivity.getStringFromCursor(movieDetailsCursor,
                    MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_CAST);
            String movieReviewsAuthor = MainActivity.getStringFromCursor(movieDetailsCursor,
                    MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_REVIEWS_AUTHOR);
            String movieReviewsText = MainActivity.getStringFromCursor(movieDetailsCursor,
                    MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_REVIEWS_TEXT);
            String movieIsForAdults = MainActivity.getStringFromCursor(movieDetailsCursor,
                    MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_IS_FOR_ADULTS);
            String movieBackdropPath = MainActivity.getStringFromCursor(movieDetailsCursor,
                    MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_BACKDROP);
            String movieTrailersThumbnails = MainActivity.getStringFromCursor(movieDetailsCursor,
                    MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_TRAILERS_THUMBNAILS);

            movieSelected.setMovieLanguage(movieLanguage);
            movieSelected.setMovieRuntime(Double.parseDouble(movieRuntime));

            // Cast
            fillMovieCastFromDB(movieCast);

            // Reviews
            fillMovieReviewsFromDB(movieReviewsAuthor, movieReviewsText);

            movieSelected.setIsMovieForAdults(Boolean.parseBoolean(movieIsForAdults));
            movieSelected.setMovieBackdropPath(movieBackdropPath);


        }
    }

    /**
     * Formats the movie reviews from the data retrieved from the database and assigns
     * this new ArrayList to the movie selected
     *
     * @param movieReviewsAuthor A String containing reviews authors
     * @param movieReviewsText A String containing reviews text
     */
    private void fillMovieReviewsFromDB(String movieReviewsAuthor, String movieReviewsText) {

        ArrayList<MovieReview> movieReviewsArrayList = new ArrayList<MovieReview>();

        String[] reviewAuthorsArray = movieReviewsAuthor.split(", ");
        String[] reviewTextArray = movieReviewsText.split("===>");

        int i;
        for(i =0; i<reviewAuthorsArray.length; i++) {
            movieReviewsArrayList.add(new MovieReview(reviewAuthorsArray[i], reviewTextArray[i]));
        }

        movieSelected.setMovieReviews(movieReviewsArrayList);
    }

    /**
     * Format the cast string retrieved from the database into
     * an ArrayList and set it as the cast attribute of the movie selected
     *
     * @param movieCast A String containing the cast names separated by ", "
     */
    private void fillMovieCastFromDB(String movieCast) {

        ArrayList<String> castArray = new ArrayList<>();

        for (String actor : movieCast.substring(1, movieCast.length() - 1).split(", ")) {
            castArray.add(actor);
        }

        movieSelected.setMovieCast(castArray);
    }

    /**
     * Loads a movie poster from the database
     *
     * @param context       The context of the activity that called this method
     * @param movieSelected The movie object selected
     * @return A Bitmap representing the corresponding poster
     */
    public static Bitmap loadPosterFromDatabase(Context context, Movie movieSelected) {

        Uri uri = MoviesDBContract.FavoriteMoviesEntry.CONTENT_URI.buildUpon()
                .appendPath(Integer.toString(movieSelected.getMovieId()))
                .build();

        String[] posterProjection = {
                MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_POSTER_PATH
        };

        Cursor posterPathCursor = context.getContentResolver().query(
                uri,
                posterProjection,
                null,
                null,
                MoviesDBContract.FavoriteMoviesEntry._ID);

        posterPathCursor.moveToFirst();

        String posterPath = posterPathCursor.getString(posterPathCursor
                .getColumnIndex(MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_POSTER_PATH));

        return FavoritesUtils.loadImageFromStorage(posterPath, Integer.toString(movieSelected.getMovieId()));
    }

    /**
     * Sets the ImageView resource in the Details Activity to be
     * the corresponding poster
     *
     * @param posterImage A Bitmap representing the poster
     */
    private void fillMoviePosterDetailsFromDB(Bitmap posterImage) {
        moviePosterView.setImageBitmap(posterImage);
        floatingActionButtonFavorite.setVisibility(View.VISIBLE);
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

            int i;
            for (i = 0; i < NUMBER_OF_ACTORS_TO_INCLUDE; i++) {
                castArray.add(arrayJSONCast.getJSONObject(i).getString("name"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        movieSelected.setMovieCast(castArray);

    }
}
