package com.example.android.popularmoviesstage2;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MovieAdapter extends ArrayAdapter<Movie> {

    /*
     * Fields
     */

    private final Context context;
    private ArrayList<Movie> mMoviesArray;
    private final MovieAdapterOnClickHandler mClickHandler;

    /*
     * Constants
     */

    // Tag for logging
    private static final String TAG = MovieAdapter.class.getSimpleName();

    /*
     * Constructor
     */

    public MovieAdapter(Activity context, ArrayList<Movie> movies, MovieAdapterOnClickHandler clickHandler) {
        super(context, 0, movies);
        this.context = context;
        mMoviesArray = movies;
        mClickHandler = clickHandler;
    }

    /**
     * Interface to implement onClick handler for each view
     */
    public interface MovieAdapterOnClickHandler {
        void onClick(Movie movie);
    }

    /**
     * Methods
     */

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        RectangularImageView movieView;

        if (convertView == null) {
            movieView = createAndFormatMovieView();
        } else {
            movieView = (RectangularImageView) convertView;
        }

        // Get movie data associated with this view
        Movie movie = getItem(position);

        if(movie != null) {
            String posterPath = movie.getMoviePosterPath();

            // Set data on the corresponding view
            loadMoviePoster(posterPath, movieView);

            // Listener
            setOnClickListener(movieView, movie);
        }

        return movieView;
    }

    @Override
    public int getCount() {
        return mMoviesArray.size();
    }

    /**
     * Method that finds the Movie data at the current
     * adapter position in mMoviesArray.
     *
     * @param position Index of the Movie we need to find
     * @return the Movie instance at index position in mMoviesArray
     */
    @Nullable
    @Override
    public Movie getItem(int position) {
        return mMoviesArray.get(position);
    }

    /**
     * Creates a movieView instance and formats it
     *
     * @return A formatted RectangularImageView
     */
    private RectangularImageView createAndFormatMovieView() {
        RectangularImageView movieView = new RectangularImageView(context);
        movieView.setAdjustViewBounds(true);
        movieView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        return movieView;
    }

    /**
     * Sets an onClickListener to the movieView, passing the movie instance
     * to the onClick method of the MovieAdapter interface to further customize
     * the actions to be performed on click
     *
     * @param movieView The view on which to set the listener
     * @param movie The data that will be sent to the onClick method
     */
    private void setOnClickListener(RectangularImageView movieView, Movie movie) {
        final Movie movieFinal = movie;
        movieView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickHandler.onClick(movieFinal);
            }
        });
    }

    /**
     * Loads a movie poster into the view provided using
     * the Picasso library
     *
     * @param posterPath URL path in String format to fetch the poster
     * @param movieView  The view that will hold the image
     */
    private void loadMoviePoster(String posterPath, RectangularImageView movieView) {
        if(posterPath != null) {
            Picasso.with(context)
                    .load(posterPath)
                    .placeholder(R.drawable.placeholder)
                    .fit()
                    .error(R.drawable.error)
                    .into(movieView);
        }
    }
}
