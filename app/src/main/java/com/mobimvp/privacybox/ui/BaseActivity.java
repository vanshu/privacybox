package com.mobimvp.privacybox.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;

public class BaseActivity extends Activity {
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
    }

    public ActionBar getPrivacyActionBar() {
        return actionBar;
    }


}
