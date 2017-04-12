package com.mobimvp.privacybox.ui.guide;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import com.mobimvp.privacybox.Constants;

public class OutcallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String currentNumber = getResultData();
        if (currentNumber == null) {
            currentNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        }
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
        String number = preference.getString(Constants.PRIVACY_DIAL_PHONE_NUMBER, "");
        if (currentNumber.equals(number)) {
            lockscreenApp(context);
            setResultData(null);
            abortBroadcast();
        }
    }

    private void lockscreenApp(Context context) {
        Intent intent = new Intent();
        intent.setAction(Constants.BROADCAST_LOCKSCREEN_APP);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
