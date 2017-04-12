package com.mobimvp.privacybox.utility;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class ViewUtility {
    private Context mContext;
    private LinearLayout parentView;

    public ViewUtility(Context context) {
        this.mContext = context;
        parentView = new LinearLayout(mContext);
        parentView.setOrientation(LinearLayout.VERTICAL);
    }

    public ViewGroup add(View... views) {
        for (int i = 0; i < views.length; i++)
            parentView.addView(views[i]);
        return parentView;
    }

    public ViewGroup remove(View... views) {
        for (int i = 0; i < views.length; i++)
            parentView.removeView(views[i]);
        return parentView;
    }
}
