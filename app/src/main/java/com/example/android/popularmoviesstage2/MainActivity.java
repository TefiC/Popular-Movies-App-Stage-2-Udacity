package com.example.android.popularmoviesstage2;

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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.example.android.popularmoviesstage2.DataUtils.FavoritesUtils;
import com.example.android.popularmoviesstage2.DataUtils.MoviesDBContract;
import com.example.android.popularmoviesstage2.utils.LoaderUtils;
import com.example.android.popularmoviesstage2.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import static com.example.android.popularmoviesstage2.utils.LoaderUtils.FAVORITE_MOVIES_LOADER;
import static com.example.android.popularmoviesstage2.utils.LoaderUtils.MAIN_SEARCH_LOADER;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener
        , MovieRecyclerViewAdapter.MovieAdapterOnClickHandler {

    /*
     * Fields
     */

    private String mSearchCriteria = "Most Popular"; // Default sort criteria
    private ArrayList<Movie> mMoviesArray = null;

    private String mMoviesArrayString = null;

    private ProgressBar mProgressBar;
    private GridLayoutManager mGridLayoutManager;

    MovieRecyclerViewAdapter mAdapter;
    RecyclerView mList;
    Parcelable mListState;

    private Spinner mSpinnerView;

    /*
     * Constants
     */

    // Tag for logging
    private static final String TAG = MainActivity.class.getSimpleName();


    /*
     * Methods
     */

    // Methods that request data and update data ========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_recycler);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        // Check if there is a previous state to be restored
        if (savedInstanceState == null
                || !savedInstanceState.containsKey("movies")
                || !savedInstanceState.containsKey("criteria")
                || !savedInstanceState.containsKey("gridScroll")) {

            makeSearchQuery(mSearchCriteria);

        } else {

            //Retrieve data
            mMoviesArray = savedInstanceState.getParcelableArrayList("movies");
            mSearchCriteria = savedInstanceState.getString("criteria");

            // Prevent cases where there was no internet connection,
            // no data was loaded previously but the user rotates device
            if (mMoviesArray != null) {
                setAdapter();
                restoreScrollPosition(savedInstanceState);
            }
        }
    }

    /*
     * Getters
     */

    public ArrayList<Movie> getMovieArray() {
        return mMoviesArray;
    }

    /*
     * Setters
     */

    public void setMovieArray(ArrayList<Movie> moviesArray) {
        mMoviesArray = moviesArray;
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
        if (NetworkUtils.isNetworkAvailable(this)) {
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
            mSpinnerView.setSelection(2);
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
        mList.smoothScrollToPosition(position);
    }


    // Methods that process data after API request ============================================


    /**
     * Set the mMoviesArray to be an array of Movie objects
     * created from the data received from the API request
     *
     * @param JSONString JSON response in String format
     *                   that contains data to make
     *                   the Movie objects
     */
    public void createMovieObjects(String JSONString) {

        JSONObject JSONObject = null;
        try {
            JSONObject = new JSONObject(JSONString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONArray resultsArray = JSONObject.optJSONArray("results");

        ArrayList<Movie> movieArray = createMoviesArrayFromJSONArray(resultsArray);

        if (movieArray.size() > 0) {
            setMovieArray(movieArray);
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

        int i;
        for (i = 0; i < resultsArray.length(); i++) {

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
            // Movie data
            int id = movie.getInt("id");
            String title = movie.getString("title");
            String posterPath = DetailsActivity.MOVIEDB_POSTER_BASE_URL + DetailsActivity.IMAGE_SIZE + movie.getString("poster_path");
            String plot = movie.getString("overview");
            String releaseDate = movie.getString("release_date");
            Double voteAverage = movie.getDouble("vote_average");

            return new Movie(id, title, releaseDate, posterPath, voteAverage, plot, null, 0.0, null, null, false, null, null);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Methods for UI =======================================================================

    /**
     * Sets the Movie Adapter for the main layout that will contain movie posters
     */
    public void setAdapter() {

        mList = (RecyclerView) findViewById(R.id.root_recycler_view);

        int numberOfColumns = MovieRecyclerViewAdapter.calculateColumns(this);

        mGridLayoutManager = new GridLayoutManager(this, numberOfColumns);
        mList.setLayoutManager(mGridLayoutManager);

        mAdapter = new MovieRecyclerViewAdapter(mMoviesArray, mMoviesArray.size(), this, this, mSearchCriteria);
        mList.setAdapter(mAdapter);
    }

    // Listeners =======================================================================

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
        intent.putExtra("movieObject", movie);

        startActivity(intent);
    }

    // Menu  =======================================================================

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

        String searchCriteria = parent.getItemAtPosition(pos).toString();

        /*
         If the query is not searching the same criteria already selected,
         set this criteria as the new selection and make a new API request
          */
        if (!mSearchCriteria.equals(searchCriteria)) {

            mSearchCriteria = searchCriteria;

            switch (searchCriteria) {
                case "Top Rated":
                    makeSearchQuery(searchCriteria);
                    break;
                case "Most Popular":
                    makeSearchQuery(searchCriteria);
                    break;
                case "Favorites":
                    makeDatabaseQuery();
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

        // To make sure that the previous selection is kept on device rotation
        mSpinnerView.setSelection(spinnerAdapter.getPosition(mSearchCriteria));
    }

    // Activity lifecycle methods ========================================================

    /**
     * Lifecycle method to handle cases where the user was initially
     * offline and no data was fetched and then the user reconnects
     * and restarts the app. To handle fetching automatically without
     * user intervention.
     */
    @Override
    public void onRestart() {
        super.onRestart();

        if (mMoviesArray == null) {

            if(mSearchCriteria.equals("Favorites")) {
                makeDatabaseQuery();
            } else {
                makeSearchQuery(mSearchCriteria);
            }
        } else {
            setAdapter();
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
        outState.putParcelableArrayList("movies", mMoviesArray);
        outState.putString("criteria", mSearchCriteria);

        // If the view was loaded correctly
        if (mList != null) {
            outState.putInt("gridScroll", mGridLayoutManager.findFirstVisibleItemPosition());
        }

        // To restore scroll position of recycler view on back button pressed
        //TODO: RESTORE SCROLL STATE AFTER PRESSING BACK BUTTON
//        outState.putParcelable("recyclerViewScroll", mGridLayoutManager.onSaveInstanceState());

        super.onSaveInstanceState(outState);
    }

    //TODO: RESTORE SCROLL STATE AFTER PRESSING BACK BUTTON
//
//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//
//        Log.v(TAG, "RESTORING STATE");
//
//        mListState = savedInstanceState.getParcelable("recyclerViewScroll");
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        if (mListState != null) {
//            Log.v(TAG, "UPDATING LAYOUT MANAGER");
//            mGridLayoutManager.onRestoreInstanceState(mListState);
//        } else {
//            Log.v(TAG, "STATE EMPTY");
//        }
//    }

    // AsyncTaskLoaders ===================================================================

    private class InternetMoviesLoader implements LoaderManager.LoaderCallbacks<String> {

        private Context mContext;

        public InternetMoviesLoader(Context context) {
            mContext = context;
        }

        @Override
        public Loader<String> onCreateLoader(final int id, final Bundle args) {
            return new AsyncTaskLoader<String>(mContext) {

                @Override
                protected void onStartLoading() {
                    super.onStartLoading();

                    if (id == MAIN_SEARCH_LOADER) {
                        if (mProgressBar != null) {
                            mProgressBar.setVisibility(View.VISIBLE);
                        }
                        forceLoad();
                    } else {
                        deliverResult(mMoviesArrayString);
                    }
                }

                @Override
                public void deliverResult(String data) {
                    mMoviesArrayString = data;
                    mProgressBar.setVisibility(View.INVISIBLE);

                    super.deliverResult(data);
                }

                @Override
                public String loadInBackground() {
                    String searchResults = null;

                    if (id == MAIN_SEARCH_LOADER && !mSearchCriteria.equals("Favorites")) {
                        URL searchURL = NetworkUtils.buildGeneralUrl(mSearchCriteria);

                        try {
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
                mProgressBar.setVisibility(View.INVISIBLE);
                createMovieObjects(data);
                setAdapter();
            } else {
                // Handle if "Favorites" was selected
                setAdapter();
            }
        }

        @Override
        public void onLoaderReset(Loader<String> loader) {
            mProgressBar.setVisibility(View.INVISIBLE);
            setAdapter();
        }

    }

    // Database methods ===================================================================

    /**
     * Initiates an AsyncTaskLoader to load favorite movies data from the database
     */
    private void makeDatabaseQuery() {
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

    // Database loader ============

    private class DatabaseMoviesLoader implements LoaderManager.LoaderCallbacks<Cursor> {

        private Context mContext;

        public DatabaseMoviesLoader(Context context) {
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
                setAdapter();
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            //
        }
    }

    /**
     * Replaces the existing movies array with an array of favorite Movies
     * @param cursor The data received from the query to the database
     */
    private void convertCursorIntoMoviesArray(Cursor cursor) {

        ArrayList<Movie> moviesDBArray = new ArrayList<Movie>();

        if(cursor.getCount() == 0) {
            mMoviesArray = new ArrayList<>();
            FavoritesUtils.createNoFavoritesDialog(this);
            mSpinnerView.setSelection(0);
        }

        while(cursor.moveToNext()) {

            // Get movie data from cursor
            String movieDBId = getStringFromCursor(cursor, MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_MOVIEDB_ID);
            String movieTitle = getStringFromCursor(cursor, MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_TITLE);
            String movieReleaseDate = getStringFromCursor(cursor, MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_RELEASE_DATE);
            String moviePosterPath = getStringFromCursor(cursor, MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_POSTER_PATH);
            String movieVoteAverage = getStringFromCursor(cursor, MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_VOTE_AVERAGE);
            String moviePlot = getStringFromCursor(cursor, MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_PLOT);
            String movieIsForAdults = getStringFromCursor(cursor, MoviesDBContract.FavoriteMoviesEntry.COLUMN_NAME_IS_FOR_ADULTS);

            Log.v(TAG, moviePosterPath);


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
                    null,
                    null);

            movie.setIsMovieFavorite(true);

            // Add the movie to an ArrayList
            moviesDBArray.add(movie);

        }

        mMoviesArray = moviesDBArray;
    }

    public static String getStringFromCursor(Cursor cursor, String colName) {
        return cursor.getString(cursor.getColumnIndex(colName));
    }

}

