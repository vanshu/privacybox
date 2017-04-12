package com.mobimvp.privacybox.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

public class RotateView extends View {

    public RotateView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public RotateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RotateView(Context context) {
        super(context);
        init();
    }

    private void init() {
        RotateAnimation anim = new RotateAnimation(0f, -360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(getContext(), android.R.anim.linear_interpolator);
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(500);
        this.startAnimation(anim);
    }
}
