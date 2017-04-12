package com.mobimvp.privacybox.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.LinearLayout;

import com.mobimvp.privacybox.R;

public class WeightedLinearLayout extends LinearLayout {
    private float mMajorWeight;
    private float mMinorWeight;

    public WeightedLinearLayout(Context context) {
        super(context);
    }

    public WeightedLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WeightedLinearLayout);

        mMajorWeight = a.getFloat(R.styleable.WeightedLinearLayout_majorWeight, 0.0f);
        mMinorWeight = a.getFloat(R.styleable.WeightedLinearLayout_minorWeight, 0.0f);

        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        final int screenWidth = metrics.widthPixels;
        final boolean isPortrait = screenWidth < metrics.heightPixels;

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        boolean measure = false;

        widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

        final float widthWeight = isPortrait ? mMinorWeight : mMajorWeight;
        if (widthMode == MeasureSpec.AT_MOST && widthWeight > 0.0f) {
            if (width < (screenWidth * widthWeight)) {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec((int) (screenWidth * widthWeight), MeasureSpec.EXACTLY);
                measure = true;
            }
        }

        // TODO: Support height?

        if (measure) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
