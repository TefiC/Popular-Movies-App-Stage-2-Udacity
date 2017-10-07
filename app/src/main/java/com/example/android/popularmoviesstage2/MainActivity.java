package com.example.android.popularmoviesstage2;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
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

import com.example.android.popularmoviesstage2.DataUtils.DatabaseUtils;
import com.example.android.popularmoviesstage2.DataUtils.MoviesDBHelper;
import com.example.android.popularmoviesstage2.utils.LoaderUtils;
import com.example.android.popularmoviesstage2.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import static com.example.android.popularmoviesstage2.utils.LoaderUtils.MAIN_SEARCH_LOADER;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
        LoaderManager.LoaderCallbacks<String>, MovieRecyclerViewAdapter.MovieAdapterOnClickHandler {

    /*
     * Fields
     */

    private String mSearchCriteria = "Most Popular"; // Default sort criteria
    private ArrayList<Movie> mMoviesArray = null;
    private ProgressBar mProgressBar;
    private GridLayoutManager mGridLayoutManager;

    MovieRecyclerViewAdapter mAdapter;
    RecyclerView mList;

    /*
     * Constants
     */

    // Tag for logging
    private static final String TAG = MainActivity.class.getSimpleName();

    // RecyclerView
    private static final int NUM_GRID_ITEMS = 12;




    /*
     * Methods
     */

    // Methods that request data and update ========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_recycler);

        MoviesDBHelper dbHelper = new MoviesDBHelper(this);
        DatabaseUtils dbUtils = new DatabaseUtils();
        dbUtils.insertData(dbHelper);
        dbUtils.testQuery(dbHelper);

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

        super.onSaveInstanceState(outState);
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
                loaderManager.initLoader(LoaderUtils.MAIN_SEARCH_LOADER, bundle, this);
            } else {
                loaderManager.restartLoader(LoaderUtils.MAIN_SEARCH_LOADER, bundle, this);
            }

        } else {
            NetworkUtils.createNoConnectionDialog(this);
            mMoviesArray = null;
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

    /**
     * An AsyncTask to handle network requests to MovieDB API
     * and updates the data received to update the UI
     */
    private class QueryTask extends AsyncTask<URL, Void, String> {

        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(URL... urls) {
            URL searchUrl = urls[0];
            String generalSearchResults = null;

            try {
                // Make query and store the results
                generalSearchResults = NetworkUtils.getResponseFromHttpUrl(searchUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return generalSearchResults;
        }

        @Override
        protected void onPostExecute(String s) {
            mProgressBar.setVisibility(View.INVISIBLE);
            createMovieObjects(s);
            setAdapter();
        }
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

            return new Movie(id, title, releaseDate, posterPath, voteAverage, plot, null, 0.0, null, null, false, null);

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

        mAdapter = new MovieRecyclerViewAdapter(mMoviesArray, NUM_GRID_ITEMS, this, this);
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
        Spinner spinnerView = (Spinner) spinner.getActionView();

        // Set listener
        spinnerView.setOnItemSelectedListener(this);

        // Create spinner adapter
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.sorting_options_array, R.layout.spinner_item);

        // Custom dropdown layout
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        spinnerView.setAdapter(spinnerAdapter);

        // To make sure that the previous selection is kept on device rotation
        spinnerView.setSelection(spinnerAdapter.getPosition(mSearchCriteria));
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
            makeSearchQuery(mSearchCriteria);
        }
    }


    // ASYNCTASK LOADER ===================================================================

    @Override
    public Loader<String> onCreateLoader(final int id, final Bundle args) {
        return new AsyncTaskLoader<String>(this) {

            @Override
            protected void onStartLoading() {
                super.onStartLoading();

                if (mProgressBar != null) {
                    mProgressBar.setVisibility(View.VISIBLE);
                }

                if (id == MAIN_SEARCH_LOADER) {
                    forceLoad();
                }
            }

            @Override
            public String loadInBackground() {
                String searchResults = null;

                if(id == MAIN_SEARCH_LOADER) {
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

        if(loader.getId() == MAIN_SEARCH_LOADER) {
            mProgressBar.setVisibility(View.INVISIBLE);
            createMovieObjects(data);
            setAdapter();
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        //
    }
}

