package com.mobimvp.privacybox.ui.guide;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import com.mobimvp.privacybox.Constants;

public class NFCBadgeActivity extends Activity {
    public static final String NFC_ID = "NFCId";

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences preferencs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean settingNfcUnlock = preferencs.getBoolean(Constants.PRIVACY_NFC_UNLOCK, false);
        if (settingNfcUnlock) { // 已经设置NFC标签
            String defaultNfcID = preferencs.getString(Constants.PRIVACY_NFC_ID, null);
            if (defaultNfcID != null && defaultNfcID.equals(getNfcId())) {
                Intent intent = new Intent();
                intent.setAction(Constants.BROADCAST_LOCKSCREEN_APP);
                LocalBroadcastManager.getInstance(NFCBadgeActivity.this).sendBroadcast(intent);
                finish();
            }
        } else {
            // 没有设置NFC标签
            String NFCId = getNfcId();
            Intent intent = new Intent();
            intent.putExtra(NFC_ID, NFCId);
            intent.setAction(Constants.BROADCAST_NFC_ID);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            finish();
        }
    }

    public String getNfcId() {
        byte[] bytesID = getIntent().getByteArrayExtra(NfcAdapter.EXTRA_ID);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytesID.length; i++) {
            sb.append(bytesID[i]);
        }
        return sb.toString();
    }
}
