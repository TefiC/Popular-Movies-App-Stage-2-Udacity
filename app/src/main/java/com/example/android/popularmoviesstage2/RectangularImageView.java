package com.example.android.popularmoviesstage2;


import android.content.Context;

/**
 * A rectangular ImageView to hold the movie posters
 */

public class RectangularImageView extends android.support.v7.widget.AppCompatImageView {

    public RectangularImageView (Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int width, int height) {
        super.onMeasure(width, height);
        int measuredWidth = getMeasuredWidth();
        Double posterHeight = Math.floor(measuredWidth*1.5);
        setMeasuredDimension(measuredWidth, posterHeight.intValue());
    }
}
