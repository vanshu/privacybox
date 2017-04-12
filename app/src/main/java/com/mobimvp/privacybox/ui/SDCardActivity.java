package com.mobimvp.privacybox.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;

public class SDCardActivity extends BaseActivity {

    private static final IntentFilter sdFilter = new IntentFilter();

    static {
        sdFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        sdFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        sdFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        sdFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        sdFilter.addDataScheme("file");
    }

    private BroadcastReceiver sdReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            finish();
        } else {
            registerReceiver(sdReceiver, sdFilter);
        }
    }

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(sdReceiver);
        } catch (Exception e) {
        }
        super.onDestroy();
    }


}
