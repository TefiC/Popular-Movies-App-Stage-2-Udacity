package com.example.android.popularmoviesstage2.GeneralUtils;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

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


    /*
     * Methods
     */

    /**
     * Build the URL used to query MovieDB API
     *
     * @param sortBy The user's preference to display movies
     * @return The URL to fetch movies according to user's preferences
     */
    public static URL buildGeneralUrl(String sortBy) {

        URL url = null;

        String criteria = determineSearchCriteria(sortBy);

        Uri buildUri = Uri.parse(MOVIEDB_URL + criteria).buildUpon()
                .appendQueryParameter(PARAM_KEY, API_KEY)
                .build();
        try {
            url = new URL(buildUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * Builds a URL to query for the details of the movie passed as argument
     * @param movieId The ID of the movie
     * @return The URL to fetch the movie's details
     */
    public static URL buildMovieDetailsUrl(int movieId) {

        URL url = null;

        Uri buildUri = Uri.parse(MOVIEDB_URL + movieId).buildUpon()
                .appendQueryParameter(PARAM_KEY, API_KEY)
                .build();

        try {
            url = new URL(buildUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     *  Build a URL to fetch the review for the movie passed as argument
     * @param movieId The ID of the movie
     * @return The URL to fetch the movie's reviews
     */
    public static URL buildMovieReviewsUrl(int movieId) {

        URL url = null;

        Uri buildUri = Uri.parse(MOVIEDB_URL + movieId + "/reviews").buildUpon()
                .appendQueryParameter(PARAM_KEY, API_KEY)
                .build();

        try {
            url = new URL(buildUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * Build a URL to fetch the trailers for the movie passed as argument
     * @param movieId The movie ID
     * @return The URL to fetch movie's trailers
     */
    public static URL buildMovieTrailersUrl(int movieId) {

        URL url = null;

        Uri buildUri = Uri.parse(MOVIEDB_URL + movieId + "/videos").buildUpon()
                .appendQueryParameter(PARAM_KEY, API_KEY)
                .build();

        try {
            url = new URL(buildUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static URL buildMovieCastUrl(int movieId) {

        URL url = null;

        Uri buildUri = Uri.parse(MOVIEDB_URL + movieId + "/credits").buildUpon()
                .appendQueryParameter(PARAM_KEY, API_KEY)
                .build();

        try {
            url = new URL(buildUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * Fetches data from the URL passed as argument and returns it as a String
     *
     * @param url The url used to fetched the data
     * @return The data returned as String
     * @throws IOException
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
     * @return The formatted criteria for the API request.
     * Either "popular" or "top_rated"
     */
    private static String determineSearchCriteria(String sortBy) {

        String criteria;

        switch (sortBy) {
            case "Most Popular":
                criteria = "popular";
                break;
            case "Top Rated":
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
