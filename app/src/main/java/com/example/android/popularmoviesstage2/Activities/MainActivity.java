package com.example.android.popularmoviesstage2.Activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.example.android.popularmoviesstage2.Adapters.MovieRecyclerViewAdapter;
import com.example.android.popularmoviesstage2.DataUtils.FavoritesUtils;
import com.example.android.popularmoviesstage2.DataUtils.MoviesDBContract;
import com.example.android.popularmoviesstage2.GeneralUtils.LoaderUtils;
import com.example.android.popularmoviesstage2.GeneralUtils.NetworkUtils;
import com.example.android.popularmoviesstage2.MovieData.Movie;
import com.example.android.popularmoviesstage2.MovieData.MovieTrailerThumbnail;
import com.example.android.popularmoviesstage2.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import static com.example.android.popularmoviesstage2.GeneralUtils.LoaderUtils.FAVORITE_MOVIES_LOADER;
import static com.example.android.popularmoviesstage2.GeneralUtils.LoaderUtils.MAIN_SEARCH_LOADER;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
                    MovieRecyclerViewAdapter.MovieAdapterOnClickHandler {

    /*
     * Constants
     */

    // Tag for logging
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int SPINNER_FAVORITES_POSITION = 2;
    private static final int SPINNER_MOST_POPULAR_POSITION = 0;

    public static final String INTENT_MOVIE_OBJECT_KEY = "movieObject";


    /*
     * Fields
     */

    // Default search criteria
    private String mSearchCriteria = "Most Popular";
    // Array to store movie objects
    private ArrayList<Movie> mMoviesArray = null;

    // Array to store cached movies data as a String
    private String mMoviesArrayString = null;

    // Views references
    private ProgressBar mProgressBar;
    private GridLayoutManager mGridLayoutManager;
    private MovieRecyclerViewAdapter mAdapter;
    private RecyclerView mMainRecyclerView;
    private Spinner mSpinnerView;

    //Scroll state
    private Parcelable mState;


    /*
     * Methods
     */

    // Methods that request and update data ========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_recycler);

        // Get reference to the progress bar
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        // Check if there is a previous state to be restored
        if (savedInstanceState == null
                || !savedInstanceState.containsKey("movies")
                || !savedInstanceState.containsKey("criteria")
                || !savedInstanceState.containsKey("gridScroll")) {

            makeSearchQuery(mSearchCriteria);

        } else {

            //Retrieve data from the previous state
            mMoviesArray = savedInstanceState.getParcelableArrayList("movies");
            mSearchCriteria = savedInstanceState.getString("criteria");

            // Prevent cases where there was no internet connection,
            // no data was loaded previously but the user rotates device
            if (mMoviesArray != null) {
                setMainActivityAdapter();
                restoreScrollPosition(savedInstanceState);
            }
        }
    }

    /**
     * Makes a query to the MoviesDB API if there is internet connection.
     * Otherwise, it shows an dialog to alert the user and sets
     * the movie array to null
     *
     * @param searchCriteria The criteria the user chose to fetch movies data.
     *                       Either "Most Popular" or "Top Rated"
     */
    private void makeSearchQuery(String searchCriteria) {
        /*
        Check if there is an internet connection. If so, request movies data
        with the current search criteria. Else, display a "No Connection" dialog
         */
        if (NetworkUtils.isNetworkAvailable(this)) {
            // Loader Manager
            LoaderManager loaderManager = getSupportLoaderManager();
            Loader<String> searchLoader = loaderManager.getLoader(LoaderUtils.MAIN_SEARCH_LOADER);

            Bundle bundle = new Bundle();
            bundle.putString("searchCriteria", searchCriteria);

            if (searchLoader == null) {
                loaderManager.initLoader(LoaderUtils.MAIN_SEARCH_LOADER, bundle, new InternetMoviesLoader(this));
            } else {
                loaderManager.restartLoader(LoaderUtils.MAIN_SEARCH_LOADER, bundle, new InternetMoviesLoader(this));
            }
        } else {
            NetworkUtils.createNoConnectionDialog(this);
        }
    }

    /**
     * Restores scroll for the main GridView when the device is rotated
     *
     * @param savedInstanceState The previous state to be restored that contains
     *                           a "gridScroll" key with the previous scroll position
     */
    private void restoreScrollPosition(Bundle savedInstanceState) {
        int position = savedInstanceState.getInt("gridScroll");
        mMainRecyclerView.smoothScrollToPosition(position);
    }


    // Methods that process data after API request =================================================

    /**
     * Set the mMoviesArray to be an array of Movie objects
     * created from the data received from the API request
     *
     * @param JSONString JSON response in String format
     *                   that contains data to make
     *                   the Movie objects
     */
    public void createMovieObjects(String JSONString) {

        JSONObject JSONObject;

        try {
            // Get movies JSON array
            JSONObject = new JSONObject(JSONString);
            JSONArray resultsArray = JSONObject.optJSONArray("results");

            // Create movie objects from this array
            ArrayList<Movie> movieArray = createMoviesArrayFromJSONArray(resultsArray);

            // If there is at least one movie, set it as the member variable
            if (movieArray.size() > 0) {
                mMoviesArray = movieArray;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates an ArrayList of Movie objects from a JSONArray
     *
     * @param resultsArray JSONArray that contains JSON objects for each movie
     *                     fetched from the API request to movieDB
     * @return an ArrayList of Movie objects
     */
    private ArrayList<Movie> createMoviesArrayFromJSONArray(JSONArray resultsArray) {

        ArrayList<Movie> movieArray = new ArrayList<Movie>();

        // For each movie in the movies array, create a movie
        // object and add the object to the ArrayList
        for (int i = 0; i < resultsArray.length(); i++) {

            try {
                JSONObject movie = resultsArray.getJSONObject(i);
                Movie movieObject = createMovie(movie);

                if (movieObject != null) {
                    movieArray.add(createMovie(movie));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return movieArray;
    }

    /**
     * Creates a Movie object from a JSON Object fetched from the API request
     * by filling its title, poster URL (adding a base path), plot, release date
     * and vote average.
     *
     * @param movie a JSONObject containing the data for a movie
     * @return Movie object
     */
    private Movie createMovie(JSONObject movie) {
        try {
            // Get movie data
            int id = movie.getInt("id");
            String title = movie.getString("title");
            String posterPath = DetailsActivity.MOVIEDB_POSTER_BASE_URL +
                                getString(R.string.poster_size) +
                                movie.getString("poster_path");
            String plot = movie.getString("overview");
            String releaseDate = movie.getString("release_date");
            Double voteAverage = movie.getDouble("vote_average");

            // Create a movie with placeholder values for details attributes that will be fetched
            // and assigned to the movie object if the user clicks on the movie
            return new Movie(id, title, releaseDate, posterPath, voteAverage, plot,
                    null, 0.0, null, null, false, null, new ArrayList<MovieTrailerThumbnail>());

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Methods for UI ==============================================================================

    /**
     * Sets the Movie Adapter for the main layout that will contain movie posters
     */
    private void setMainActivityAdapter() {

        mMainRecyclerView = (RecyclerView) findViewById(R.id.root_recycler_view);

        // Layout Manager
        setMainActivityLayoutManager();

        // Create and set the adapter
        mAdapter = new MovieRecyclerViewAdapter(mMoviesArray, mMoviesArray.size(), this, this, mSearchCriteria);
        mMainRecyclerView.setAdapter(mAdapter);

        // Restore RecyclerView scroll
        if(mState != null) {
            mGridLayoutManager.onRestoreInstanceState(mState);
            mState = null;
        }
    }

    /**
     * Sets a grid layout manager with a dynamically calculated number of columns
     * depending on the screen size
     */
    private void setMainActivityLayoutManager() {
        // Dynamically calculate the number of columns the GridManager should create
        // depending on the screen size
        int numberOfColumns = MovieRecyclerViewAdapter.calculateColumns(this);

        // Create and apply the layout manager
        mGridLayoutManager = new GridLayoutManager(this, numberOfColumns);
        mMainRecyclerView.setLayoutManager(mGridLayoutManager);
    }

    // UI interaction methods ======================================================================

    /**
     * Implementation of the onClick method in the MovieRecylerViewAdapter class.
     * It launches an activity passing the corresponding Movie object
     * through an intent
     *
     * @param movie A Movie instance that corresponds to the item clicked
     */
    @Override
    public void onClick(Movie movie) {
        Context context = this;
        Class destinationActivity = DetailsActivity.class;

        // Intent
        Intent intent = new Intent(context, destinationActivity);
        intent.putExtra(INTENT_MOVIE_OBJECT_KEY, movie);

        startActivity(intent);
    }

    // Methods for Activity Options Menu ===========================================================

    /**
     * Creates the options menu and spinner
     *
     * @param menu menu to be created
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        createSpinner(menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

        // Extract the search criteria
        String searchCriteria = parent.getItemAtPosition(pos).toString();

        /*
         If the query is not searching the same criteria already selected,
         set this criteria as the new selection and make a new API request.
         Else, do not request data again since the criteria selected is the same
          */
        if (!mSearchCriteria.equals(searchCriteria)) {

            mSearchCriteria = searchCriteria;

            switch (searchCriteria) {
                case "Top Rated":
                case "Most Popular":
                    // Query data from the internet
                    makeSearchQuery(searchCriteria);
                    break;
                case "Favorites":
                    // Query data from the database
                    makeDatabaseQuery();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        //
    }

    /**
     * Creates a Spinner feature in the menu bar with custom layout
     * and format that displays the corresponding selection
     *
     * @param menu The menu being created
     */
    private void createSpinner(Menu menu) {

        // Get spinner and spinner view
        MenuItem spinner = menu.findItem(R.id.sort_spinner);
        mSpinnerView = (Spinner) spinner.getActionView();

        // Set listener
        mSpinnerView.setOnItemSelectedListener(this);

        // Create spinner adapter
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.sorting_options_array, R.layout.spinner_item);

        // Custom dropdown layout
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        mSpinnerView.setAdapter(spinnerAdapter);

        // If there is internet connection, restore the previously selected criteria
        // Else, select the "Favorites" option automatically
        if (NetworkUtils.isNetworkAvailable(this)) {
            // To make sure that the previous selection is kept on device rotation
            mSpinnerView.setSelection(spinnerAdapter.getPosition(mSearchCriteria));
        } else {
            mSpinnerView.setSelection(spinnerAdapter.getPosition("Favorites"));
        }
    }

    // Activity lifecycle methods ==================================================================

    /**
     * Lifecycle method to handle cases where the user was initially
     * offline and no data was fetched and then the user reconnects
     * and restarts the app. To handle fetching automatically without
     * user intervention.
     */
    @Override
    public void onRestart() {
        super.onRestart();

        /*
        If there aren't movie objects to restore, check the criteria selected.
        If the criteria is "Favorites", retrieve data from the database.
        Else, if the criteria is not "Favorites", retrieve data from the internet.
        If there are no movie objects to restore, set the adapter
         */
        if (mMoviesArray == null) {

            if (mSearchCriteria.equals("Favorites")) {
                makeDatabaseQuery();
            } else {
                makeSearchQuery(mSearchCriteria);
            }
        } else {
            setMainActivityAdapter();
        }
    }

    /**
     * Saves the current moviesArray, searchCriteria and scroll position
     * to avoid fetching data from API when the device is rotated
     *
     * @param outState The state that will be passed to onCreate
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {

        // Insert data into the Bundle
        outState.putParcelableArrayList("movies", mMoviesArray);
        outState.putString("criteria", mSearchCriteria);

        // If the view was loaded correctly
        if (mMainRecyclerView != null) {
            outState.putInt("gridScroll", mGridLayoutManager.findFirstVisibleItemPosition());
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // To restore RecyclerView scroll
        mState = mGridLayoutManager.onSaveInstanceState();
    }

    // Methods and classes to load data  from the internet =========================================

    /**
     * Loads movies data from the internet
     */
    private class InternetMoviesLoader implements LoaderManager.LoaderCallbacks<String> {

        private Context mContext;

        private InternetMoviesLoader(Context context) {
            mContext = context;
        }

        @Override
        public Loader<String> onCreateLoader(final int id, final Bundle args) {
            return new AsyncTaskLoader<String>(mContext) {

                @Override
                protected void onStartLoading() {
                    super.onStartLoading();

                    /*
                    If there is a network connection available, display the progress bar.
                    else, if there is no connection, display a dialog to the user and set
                    the spinner selection to "Favorites"
                     */
                    if (NetworkUtils.isNetworkAvailable(mContext)) {

                        if (id == MAIN_SEARCH_LOADER) {
                            if (mProgressBar != null && mMoviesArray == null) {
                                mProgressBar.setVisibility(View.VISIBLE);
                            }
                            forceLoad();
                        } else {
                            deliverResult(mMoviesArrayString);
                        }
                    } else if (!mSearchCriteria.equals("Favorites")) {
                        NetworkUtils.createNoConnectionDialog(mContext);
                        mSpinnerView.setSelection(SPINNER_FAVORITES_POSITION);
                    }
                }

                @Override
                public void deliverResult(String data) {
                    mMoviesArrayString = data;
                    mProgressBar.setVisibility(View.GONE);

                    super.deliverResult(data);
                }

                @Override
                public String loadInBackground() {
                    String searchResults = null;

                    if (id == MAIN_SEARCH_LOADER && !mSearchCriteria.equals("Favorites")) {

                        try {
                            URL searchURL = NetworkUtils.buildSearchUrl(NetworkUtils.SEARCH_TYPE_GENERAL_DATA, mSearchCriteria, 0);
                            searchResults = NetworkUtils.getResponseFromHttpUrl(searchURL);

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

            if (loader.getId() == MAIN_SEARCH_LOADER && data != null) {
                mProgressBar.setVisibility(View.GONE);
                createMovieObjects(data);
                setMainActivityAdapter();
            } else {
                // Handle if "Favorites" was selected
                setMainActivityAdapter();
            }
        }

        @Override
        public void onLoaderReset(Loader<String> loader) {
            mProgressBar.setVisibility(View.GONE);
            setMainActivityAdapter();
        }
    }

    // Methods and classes to load data  from the database =========================================

    /**
     * Initiates an AsyncTaskLoader to load favorite movies data from the database
     */
    private void makeDatabaseQuery() {

        // Loader Manager
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> searchLoader = loaderManager.getLoader(LoaderUtils.FAVORITE_MOVIES_LOADER);

        Bundle bundle = new Bundle();
        bundle.putString("searchCriteria", "Favorites");

        if (searchLoader == null) {
            loaderManager.initLoader(LoaderUtils.FAVORITE_MOVIES_LOADER, bundle, new DatabaseMoviesLoader(this));
        } else {
            loaderManager.restartLoader(LoaderUtils.FAVORITE_MOVIES_LOADER, bundle, new DatabaseMoviesLoader(this));
        }
    }

    /**
     * Retrieves movies data from the database
     */
    private class DatabaseMoviesLoader implements LoaderManager.LoaderCallbacks<Cursor> {

        private Context mContext;

        private DatabaseMoviesLoader(Context context) {
            mContext = context;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {

            switch (id) {
                case FAVORITE_MOVIES_LOADER:
                    return new CursorLoader(mContext,
                            MoviesDBContract.FavoriteMoviesEntry.CONTENT_URI,
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
            if (data.getCount() >= 0) {
                convertCursorIntoMoviesArray(data);
                setMainActivityAdapter();
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            //
        }
    }

    /**
     * Replaces the existing movies array with an array of favorite Movies
     * from data contained in a Cursor
     *
     * @param cursor The data received from the query to the database
     */
    private void convertCursorIntoMoviesArray(Cursor cursor) {

        ArrayList<Movie> moviesDBArray = new ArrayList<Movie>();

        if (cursor.getCount() == 0) {
            handleNoFavoriteMoviesSelected();
        }

        while (cursor.moveToNext()) {

            // Get movie data from cursor
            String movieDBId = LoaderUtils.getStringFromCursor(cursor,
                    MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_MOVIEDB_ID);
            String movieTitle = LoaderUtils.getStringFromCursor(cursor,
                    MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_TITLE);
            String movieReleaseDate = LoaderUtils.getStringFromCursor(cursor,
                    MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_RELEASE_DATE);
            String moviePosterPath = LoaderUtils.getStringFromCursor(cursor,
                    MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_POSTER_PATH);
            String movieVoteAverage = LoaderUtils.getStringFromCursor(cursor,
                    MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_VOTE_AVERAGE);
            String moviePlot = LoaderUtils.getStringFromCursor(cursor,
                    MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_PLOT);
            String movieIsForAdults = LoaderUtils.getStringFromCursor(cursor,
                    MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_IS_FOR_ADULTS);
            String backdropPath = LoaderUtils.getStringFromCursor(cursor,
                    MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_BACKDROP);

            Movie movie = new Movie(
                    Integer.parseInt(movieDBId),
                    movieTitle,
                    movieReleaseDate,
                    moviePosterPath,
                    Double.parseDouble(movieVoteAverage),
                    moviePlot,
                    null,
                    0.0,
                    null,
                    null,
                    Boolean.parseBoolean(movieIsForAdults),
                    backdropPath,
                    null);

            movie.setIsMovieFavorite(true);

            // Add the movie to an ArrayList
            moviesDBArray.add(movie);
        }

        mMoviesArray = moviesDBArray;
    }

    /**
     * Handle a scenario where the user has no favorite movies selected
     * in which case, it sets the spinner to the first option to load new movies
     * from the internet with the default search criteria to avoid displaying an
     * empty screen to the user
     */
    private void handleNoFavoriteMoviesSelected() {
        // Set the variable to an empty ArrayList
        mMoviesArray = new ArrayList<>();

        FavoritesUtils.createNoFavoritesDialog(this);

        if (NetworkUtils.isNetworkAvailable(this)) {
            mSpinnerView.setSelection(SPINNER_MOST_POPULAR_POSITION);
        }
    }
}

