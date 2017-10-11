package com.example.android.popularmoviesstage2;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.util.ArrayList;

/**
 * Adapter for reviews recyclerview
 */

public class ReviewsRecyclerViewAdapter extends RecyclerView.Adapter<ReviewsRecyclerViewAdapter.ReviewViewHolder> {


    private Context mContext;
    private ArrayList<MovieReview> mMovieReviewsArray;
    private int mNumberOfItems;


    private final SparseBooleanArray mCollapsedStatus;

    public ReviewsRecyclerViewAdapter(ArrayList<MovieReview> reviewsArray, int numberOfItems, Context context) {
        mMovieReviewsArray = reviewsArray;
        mNumberOfItems = numberOfItems;
        mContext = context;
        mCollapsedStatus = new SparseBooleanArray();
    }

    public class ReviewViewHolder extends RecyclerView.ViewHolder {

        private TextView reviewAuthorView;
        private ExpandableTextView expandTextView;
        private TextView reviewTextView;

        public ReviewViewHolder(View itemView) {
            super(itemView);

            reviewAuthorView = itemView.findViewById(R.id.review_author);
            expandTextView = itemView.findViewById(R.id.review_author_expandable_view);
            reviewTextView = itemView.findViewById(R.id.expandable_text);

        }
    }


    @Override
    public ReviewsRecyclerViewAdapter.ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        int layoutIdItem = R.layout.review_item;
        boolean shouldAttachToParentImmediately = false;

        LinearLayout view = (LinearLayout) layoutInflater.inflate(layoutIdItem, parent, shouldAttachToParentImmediately);

        ReviewViewHolder viewHolder = new ReviewViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ReviewsRecyclerViewAdapter.ReviewViewHolder holder, int position) {

        Log.v("DB", "POSISTION " + position);

        MovieReview review = mMovieReviewsArray.get(position);

        if(review != null) {
            holder.reviewAuthorView.setText(review.getReviewAuthor());

            Log.v("ADAPTER", "SETTING TEXT IN EXPANDABLE");
            holder.expandTextView.setText(review.getReviewText(), mCollapsedStatus, position);
        }

    }

    @Override
    public int getItemCount() {
        return mNumberOfItems;
    }
}
