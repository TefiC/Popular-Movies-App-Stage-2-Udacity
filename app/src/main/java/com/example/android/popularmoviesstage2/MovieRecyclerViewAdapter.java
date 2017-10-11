package com.example.android.popularmoviesstage2;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.popularmoviesstage2.DataUtils.FavoritesUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * RecyclerView for a grid of movies
 */

public class MovieRecyclerViewAdapter extends RecyclerView.Adapter<MovieRecyclerViewAdapter.MovieViewHolder> {

    private int mNumberOfItems;
    private Context mContext;
    private final MovieAdapterOnClickHandler mClickHandler;
    private ArrayList<Movie> mMoviesArray;
    private String mSearchCriteria;

    private int mMoviePosterViewId;

    // Determine item width
    private static int gridItemWidth = 170;

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
        // INFLATE LAYOUT
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        int layoutIdItem = R.layout.movie_item;
        boolean shouldAttachToParentImmediately = false;

        LinearLayout view = (LinearLayout) layoutInflater.inflate(layoutIdItem, parent, shouldAttachToParentImmediately);


        RectangularImageView movieView = new RectangularImageView(mContext);
        movieView.setAdjustViewBounds(true);
        movieView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        // TODO: CHECK COMPATIBILITY
        mMoviePosterViewId = View.generateViewId();
        movieView.setId(mMoviePosterViewId);

        view.addView(movieView, 0);

        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieRecyclerViewAdapter.MovieViewHolder holder, int position) {

        Movie movie = mMoviesArray.get(position);

        if (movie != null) {
            String posterPath = movie.getMoviePosterPath();

            if(!mSearchCriteria.equals("Favorites")) {
                // Set data on the corresponding view
                loadMoviePoster(posterPath, holder.mMoviePoster);
            } else {
                Bitmap poster = FavoritesUtils.loadImageFromStorage(posterPath, Integer.toString(movie.getMovieId()));
                holder.mMoviePoster.setImageBitmap(poster);
            }

            // Listener
            holder.setOnClickListener(holder.mMoviePoster, movie);

            // Poster data
            holder.mMovieTitleView.setText(movie.getMovieTitle());
            holder.mMovieRatingView.setText(Double.toString(movie.getMovieVoteAverage()));

            // Determine logos
            determineForAdultsLogo(holder, movie.getIsMovieForAdults());
            determineMainPosterFavoriteLogo(holder, movie, FavoritesUtils.checkIfMovieIsFavorite(mContext, Integer.toString(movie.getMovieId())));
        }
    }

    /**
     * Determines the image resource to be used for the logo
     * that display if the user has chosen the movie as one of
     * the "Favorites"
     *
     * @param holder The corresponding RecyclerView ViewHolder
     * @param movie The selected Movie object
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
     * @param holder The RecyclerView's ViewHolder
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

    public class MovieViewHolder extends RecyclerView.ViewHolder {

        public RectangularImageView mMoviePoster;

        private TextView mMovieTitleView;
        private ImageView mMovieRatingStarView;
        private TextView mMovieRatingView;
        private ImageView mMovieIsForAdultsView;
        private ImageView mMovieIsFavoriteView;

        private MovieViewHolder(View itemView) {
            super(itemView);

            mMoviePoster = itemView.findViewById(mMoviePosterViewId);

            mMovieTitleView = itemView.findViewById(R.id.movie_item_title);
            mMovieRatingStarView = itemView.findViewById(R.id.movie_item_star);
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

    /**
     * Interface to implement onClick handler for each view
     */
    public interface MovieAdapterOnClickHandler {
        void onClick(Movie movie);
    }

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
        int columns = (int) (dpWidth / gridItemWidth);
        return columns;
    }
}
