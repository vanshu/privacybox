package com.mobimvp.privacybox.ui.widgets;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobimvp.privacybox.R;
import com.mobimvp.privacybox.ui.widgets.LockPatternView.Cell;

import java.util.List;

public class FloatView extends LinearLayout {


    public TextView tip;
    public LockPatternView pattern;
    public BackPressListener listener;
    public LinearLayout cancel;
    public LinearLayout homeAsUp;

    public FloatView(Context context) {
        super(context);
        LayoutInflater.from(new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light)).inflate(R.layout.keyguard_pattern, this);
        this.tip = (TextView) findViewById(R.id.tip);
        this.pattern = (LockPatternView) findViewById(R.id.pattern);
        cancel = (LinearLayout) findViewById(R.id.cancel);
        homeAsUp = (LinearLayout) findViewById(R.id.actionbar_homeasup_layout);
        homeAsUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                userQuit();
            }
        });
    }

    public void userQuit() {
        if (listener != null)
            listener.onBackPressed();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP && listener != null) {
            return listener.onBackPressed();
        }
        return super.dispatchKeyEvent(event);
    }

    public void setBackPressListener(BackPressListener listener) {
        this.listener = listener;
    }

    public String encodePattern(List<Cell> patterns) {
        return pattern.patternToSha1(patterns);
    }

    public interface BackPressListener {
        boolean onBackPressed();
    }
}
