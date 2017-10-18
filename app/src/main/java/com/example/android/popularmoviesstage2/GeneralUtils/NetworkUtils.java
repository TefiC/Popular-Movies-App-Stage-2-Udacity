package com.example.android.popularmoviesstage2.GeneralUtils;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.widget.Button;

import com.example.android.popularmoviesstage2.Activities.MainActivity;
import com.example.android.popularmoviesstage2.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Utilities that will be used to connect to the network, to perform API requests
 * and to respond to the user for issues related to the network
 */

public class NetworkUtils {


    /**
     * Constants
     */


    // Tag for logging
    private static final String TAG = NetworkUtils.class.getSimpleName();

    // Base URL to fetch data
    private static final String MOVIEDB_URL = "https://api.themoviedb.org/3/movie/";

    // Parameters to build the URL
    private static final String PARAM_KEY = "api_key";

    // Values to build the URL
    // TODO: Uncomment this variable and initialize it by adding your "The Movie Database" API key
    // private final static String API_KEY = "YOUR API KEY"


    // Constants to build URLs
    public static final String SEARCH_TYPE_GENERAL_DATA = "general";
    public static final String SEARCH_TYPE_DETAILS = "details";
    public static final String SEARCH_TYPE_REVIEWS = "reviews";
    public static final String SEARCH_TYPE_TRAILERS = "trailers";
    public static final String SEARCH_TYPE_CAST = "cast";


    /*
     * Methods
     */


    // Methods to build URLs =======================================================================

    /**
     * Build the search URL that corresponds to the search type passed as argument
     *
     * @param searchType One of the search types defined in the class constants. Either
     *                   "general", "details", "reviews", "trailers" or "cast".
     * @param sortBy If the search type if "general", a sort type to determine which movies to fetch.
     *               Either "Most Popular" or "Top Rated". Pass null us the type is not "general"
     * @param movieId The MovieDB Id of the movie selected by the user. Pass null if
     *                the type is not "general"
     *
     * @return The URL to fetch the corresponding data
     */
    public static URL buildSearchUrl(String searchType, String sortBy, int movieId) {

        URL url = null;

        try {
            Uri buildUri = buildUri(searchType, sortBy, Integer.toString(movieId));
            url = new URL(buildUri.toString());

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * Build a Uri to fetch data from the internet according to the criteria passed as argument
     *
     * @param searchType One of the search types defined in the class constants. Either
     *                   "general", "details", "reviews", "trailers" or "cast".
     * @param sortBy If the search type if "general", a sort type to determine which movies to fetch.
     *               Either "Most Popular" or "Top Rated". Pass null us the type is not "general"
     * @param movieId The MovieDB Id of the movie selected by the user. Pass null if
     *                the type is not "general"
     *
     * @return A Uri to fetch the corresponding data
     */
    private static Uri buildUri(String searchType, String sortBy, String movieId) {

        Uri buildUri;

        String baseUri = buildBaseUri(searchType, sortBy, movieId);

        // Build Uri
        buildUri = Uri.parse(baseUri).buildUpon()
                .appendQueryParameter(PARAM_KEY, API_KEY)
                .build();

        return buildUri;
    }

    /**
     * Build the base Uri for a Network Request according to the criteria passed as argument
     *
     * @param searchType One of the search types defined in the class constants. Either
     *                   "general", "details", "reviews", "trailers" or "cast".
     * @param sortBy If the search type if "general", a sort type to determine which movies to fetch.
     *               Either "Most Popular" or "Top Rated". Pass null us the type is not "general"
     * @param movieId The MovieDB Id of the movie selected by the user. Pass null if
     *                the type is not "general"
     *
     * @return A String with the corresponding base path to be parsed as a Uri
     */
    private static String buildBaseUri(String searchType, String sortBy, String movieId) {
        String baseUri;

        // Determine base path of the Uri
        switch (searchType) {
            case SEARCH_TYPE_GENERAL_DATA:
                String criteria = determineSearchCriteria(sortBy);
                baseUri = MOVIEDB_URL + criteria;
                break;
            case SEARCH_TYPE_DETAILS:
                baseUri = MOVIEDB_URL + movieId;
                break;
            case SEARCH_TYPE_REVIEWS:
                baseUri = MOVIEDB_URL + movieId + "/reviews";
                break;
            case SEARCH_TYPE_TRAILERS:
                baseUri = MOVIEDB_URL + movieId + "/videos";
                break;
            case SEARCH_TYPE_CAST:
                baseUri = MOVIEDB_URL + movieId + "/credits";
                break;
            default:
                throw new UnsupportedOperationException("Unknown search type: " + searchType);
        }

        return baseUri;
    }

    /**
     * Fetches data from the URL passed as argument and returns it as a String
     *
     * @param url The url used to fetched the data
     * @return The data returned as String
     *
     * @throws IOException An exception thrown if the URL connection fails
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();

            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    /**
     * Creates and displays an alert dialog telling the user
     * there is no internet connection
     *
     * @param context Context of the Activity where the dialog is launched
     */
    public static void createNoConnectionDialog(Context context) {

        //Create dialog builder with corresponding settings
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        // Set content
        builder.setTitle(context.getString(R.string.connection_dialog_title))
                .setMessage(context.getString(R.string.connection_dialog_message));
        // Set button
        builder.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        // Create dialog and display it to the user
        AlertDialog dialog = builder.create();

        dialog.show();

        Button okButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        okButton.setTextColor(Color.RED);
    }

    /**
     * Determine if there is an internet connection available.
     *
     * @return true if there is, false if there isn't.
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Determines the right formatting for the search parameter
     * according to the user's preference
     *
     * @param sortBy User's preference in String format.
     *               Either "Most Popular" or "Top Rated"
     *
     * @return The formatted criteria for the API request.
     *         Either "popular" or "top_rated"
     */
    private static String determineSearchCriteria(String sortBy) {

        String criteria;

        switch (sortBy) {
            case MainActivity.MOST_POPULAR_CRITERIA_STRING:
                criteria = "popular";
                break;
            case MainActivity.TOP_RATED_CRITERIA_STRING:
                criteria = "top_rated";
                break;
            // Handle any other cases
            default:
                criteria = "popular";
                break;
        }

        return criteria;
    }
}
