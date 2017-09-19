package com.mobimvp.privacybox.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.mobimvp.privacybox.Constants;

public class LauncherActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Intent intent = new Intent();
		intent.setAction(Constants.BROADCAST_LOCKSCREEN_APP);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        finish();
	}
}
