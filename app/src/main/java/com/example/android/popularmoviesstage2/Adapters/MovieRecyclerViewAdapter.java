package com.example.android.popularmoviesstage2.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.popularmoviesstage2.DataUtils.FavoritesUtils;
import com.example.android.popularmoviesstage2.DataUtils.ImagesDBUtils;
import com.example.android.popularmoviesstage2.MovieData.Movie;
import com.example.android.popularmoviesstage2.MovieData.RectangularImageView;
import com.example.android.popularmoviesstage2.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * RecyclerView for a grid of movies
 */

public class MovieRecyclerViewAdapter extends RecyclerView.Adapter<MovieRecyclerViewAdapter.MovieViewHolder> {

    /*
     * Fields
     */

    // Views
    private int mNumberOfItems;
    private Context mContext;
    private final MovieAdapterOnClickHandler mClickHandler;
    private ArrayList<Movie> mMoviesArray;

    // Criteria
    private String mSearchCriteria;

    // View Id
    private int mMoviePosterViewId;


    /*
     * Constructor
     */

    public MovieRecyclerViewAdapter(ArrayList<Movie> moviesArray, int numberOfItems,
                                    MovieAdapterOnClickHandler movieAdapterOnClickHandler, Context context,
                                    String searchCriteria) {
        mNumberOfItems = numberOfItems;
        mClickHandler = movieAdapterOnClickHandler;
        mContext = context;
        mMoviesArray = moviesArray;
        mSearchCriteria = searchCriteria;
    }

    @Override
    public MovieRecyclerViewAdapter.MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();

        // Inflate layout
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        int layoutIdItem = R.layout.movie_item;
        boolean shouldAttachToParentImmediately = false;

        LinearLayout view = (LinearLayout) layoutInflater.inflate(layoutIdItem, parent, shouldAttachToParentImmediately);

        // Create and add poster image view
        RectangularImageView movieView = createRectangularImageView();
        view.addView(movieView, 0);

        return new MovieViewHolder(view);
    }

    private RectangularImageView createRectangularImageView() {

        RectangularImageView movieView = new RectangularImageView(mContext);
        movieView.setAdjustViewBounds(true);
        movieView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // Generate view id depending on the user's API version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mMoviePosterViewId = View.generateViewId();
            movieView.setId(mMoviePosterViewId);
        } else {
            mMoviePosterViewId = R.id.posterImageView;
            movieView.setId(R.id.posterImageView);
        }

        return movieView;
    }


    @Override
    public void onBindViewHolder(MovieRecyclerViewAdapter.MovieViewHolder holder, int position) {

        Movie movie = mMoviesArray.get(position);
        movie.setIsMovieFavorite(determineIfMovieIsFavorite(movie));

        // Load poster
        String posterPath = movie.getMoviePosterPath();

        if (!mSearchCriteria.equals("Favorites")) {
            // Set data on the corresponding view
            loadMoviePoster(posterPath, holder.mMoviePoster);
        } else {
            Bitmap poster = ImagesDBUtils.loadImageFromStorage(posterPath,
                    Integer.toString(movie.getMovieId()),
                    FavoritesUtils.IMAGE_TYPE_POSTER,
                    -1);
            holder.mMoviePoster.setImageBitmap(poster);
        }

        // Movie Poster Listener
        holder.setOnClickListener(holder.mMoviePoster, movie);

        // Poster data
        holder.mMovieTitleView.setText(movie.getMovieTitle());
        holder.mMovieRatingView.setText(Double.toString(movie.getMovieVoteAverage()));

        // Determine logos
        determineForAdultsLogo(holder, movie.getIsMovieForAdults());
        determineMainPosterFavoriteLogo(holder, movie, movie.getIsMovieFavorite());
    }

    /**
     * Determines of the movie is one of the user's favorites by first checking if
     * the movie's MovieDB Id is contained in Shared Preferences. Else, if there
     * are no favorite movies stores in Shared Preferences, it checks in the database
     * if the movie is saved in the "Favorites" table
     *
     * @param movie The movie data currently being populated by the adapter
     *
     * @return True if the movie is one of the user's favorites. Else, returns false
     */
    private boolean determineIfMovieIsFavorite(Movie movie) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        if (sharedPreferences.contains(FavoritesUtils.SHARED_PREFERENCES_FAVORITES_STRING)) {
            return sharedPreferences.getStringSet(FavoritesUtils.SHARED_PREFERENCES_FAVORITES_STRING, null).contains(Integer.toString(movie.getMovieId()));
        } else {
            return FavoritesUtils.checkIfMovieIsFavorite(mContext, Integer.toString(movie.getMovieId()));
        }
    }

    /**
     * Determines the image resource to be used for the logo
     * that display if the user has chosen the movie as one of
     * the "Favorites"
     *
     * @param holder The corresponding RecyclerView ViewHolder
     * @param movie  The selected Movie object
     */
    private void determineMainPosterFavoriteLogo(MovieRecyclerViewAdapter.MovieViewHolder holder, Movie movie, boolean isFavorite) {
        if (isFavorite) {
            holder.mMovieIsFavoriteView.setImageResource(R.drawable.heart_pressed_white);
            movie.setIsMovieFavorite(true);
        } else {
            holder.mMovieIsFavoriteView.setImageResource(R.drawable.heart_not_pressed_thin);
            movie.setIsMovieFavorite(false);
        }
    }

    /**
     * Determines the image resources to display if the movie is
     * appropriate for children or not.
     *
     * @param holder      The RecyclerView's ViewHolder
     * @param isForAdults A boolean. Whether or not the movie is appropriate for children
     */
    private void determineForAdultsLogo(MovieRecyclerViewAdapter.MovieViewHolder holder, boolean isForAdults) {
        if (isForAdults) {
            holder.mMovieIsForAdultsView.setImageResource(R.drawable.for_adults);
        } else {
            holder.mMovieIsForAdultsView.setImageResource(R.drawable.for_children);
        }
    }

    @Override
    public int getItemCount() {
        return mNumberOfItems;
    }

    /*
     * ViewHolder
     */

    public class MovieViewHolder extends RecyclerView.ViewHolder {

        // ViewHolder individual views
        private RectangularImageView mMoviePoster;

        private TextView mMovieTitleView;
        private TextView mMovieRatingView;
        private ImageView mMovieIsForAdultsView;
        private ImageView mMovieIsFavoriteView;

        private MovieViewHolder(View itemView) {
            super(itemView);

            // Assign views
            mMoviePoster = itemView.findViewById(mMoviePosterViewId);
            mMovieTitleView = itemView.findViewById(R.id.movie_item_title);
            mMovieRatingView = itemView.findViewById(R.id.movie_item_rating);
            mMovieIsForAdultsView = itemView.findViewById(R.id.movie_item_adults);
            mMovieIsFavoriteView = itemView.findViewById(R.id.movie_item_favorite);
        }

        /**
         * Sets an onClickListener to the movieView, passing the movie instance
         * to the onClick method of the MovieRecyclerViewAdapter interface to further customize
         * the actions to be performed on click
         *
         * @param movieView The view on which to set the listener
         * @param movie     The data that will be sent to the onClick method
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
    }

    // UI interaction ==============================================================================

    /**
     * Interface to implement onClick handler for each view
     */
    public interface MovieAdapterOnClickHandler {
        void onClick(Movie movie);
    }

    // Helper methods ==============================================================================

    /**
     * Loads a movie poster into the view provided using
     * the Picasso library
     *
     * @param posterPath URL path in String format to fetch the poster
     * @param movieView  The view that will hold the image
     */
    private void loadMoviePoster(String posterPath, RectangularImageView movieView) {

        if (posterPath != null) {
            Picasso.with(mContext)
                    .load(posterPath)
                    .placeholder(R.drawable.placeholder)
                    .fit()
                    .error(R.drawable.error)
                    .into(movieView);
        }
    }

    /**
     * Calculates the number of columns to autofit the movie poster layout
     *
     * @param context The Main Activity context
     * @return The number of columns to be displayed by a RecyclerView (spanSize)
     */
    public static int calculateColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int gridItemWidth = context.getResources().getInteger(R.integer.mainPosterLayoutWidthInt);
        int columns = (int) (dpWidth / gridItemWidth);
        return columns;
    }
}
