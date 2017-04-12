package com.mobimvp.privacybox.ui.guide;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.mobimvp.privacybox.Constants;
import com.mobimvp.privacybox.R;
import com.mobimvp.privacybox.ui.BaseActivity;
import com.mobimvp.privacybox.utility.SystemInfo;

public class GuideActivity extends BaseActivity implements OnClickListener {
    private Button btnNfc;
    private Button btnWearable;
    private Button btnDialphone;
    private Button btnNosetting;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//		setContentView(R.layout.guide);
//		getPrivacyActionBar().setLogo(R.drawable.logo);
//		getPrivacyActionBar().setDisplayShowTitleEnabled(false);
//		getPrivacyActionBar().setDisplayShowHomeEnabled(true);
//		btnNfc = (Button) findViewById(R.id.btn_nfc);
//		btnWearable = (Button) findViewById(R.id.btn_wearable);
//		btnDialphone = (Button) findViewById(R.id.btn_dialphone);
//		btnNosetting = (Button) findViewById(R.id.btn_nosetting);
//		btnNfc.setOnClickListener(this);
//		btnWearable.setOnClickListener(this);
//		btnDialphone.setOnClickListener(this);
//		btnNosetting.setOnClickListener(this);
//		registerfinishActivity();
        noSetting();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_nfc:
                NFCUnlock();
                break;
            case R.id.btn_wearable:
                wearableUnlock();
                break;
            case R.id.btn_dialphone:
                dialPhoneUnlock();
                break;
            case R.id.btn_nosetting:
                noSetting();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterfinishActivity();
    }

    public void dialPhoneUnlock() {
        Intent intent = new Intent();
        intent.setClass(GuideActivity.this, DialPhoneGuideActivity.class);
        startActivity(intent);
    }

    public void wearableUnlock() {
//		Intent intent = new Intent();
//		intent.setClass(GuideActivity.this, WearableGuideActivity.class);
//		startActivity(intent);
    }

    public void noSetting() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.edit().putString(Constants.PRIVACY_UNLOCKMODE, "0").commit();
        Intent intent = new Intent();
        intent.setAction(Constants.BROADCAST_FIRST_TRAIN);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        finish();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void NFCUnlock() {
        switch (SystemInfo.getNFCStatus(GuideActivity.this)) {
            case SystemInfo.DEVICE_NO_NFC:
                Toast.makeText(getApplicationContext(), getString(R.string.cellphone_unsupport_nfc),
                        Toast.LENGTH_SHORT).show();
                break;
            case SystemInfo.DEVICE_NFC_DISABLE:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    Toast.makeText(getApplicationContext(), getString(R.string.open_nfc_function_tips),
                            Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
                }
                break;
            case SystemInfo.DEVICE_NFC_ENABLE:
                Intent intent = new Intent();
                intent.setClass(GuideActivity.this, NFCGuideActivity.class);
                startActivity(intent);
                break;
        }
    }

    public void registerfinishActivity() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.BROADCAST_CLOSE_GUIDEACTIVITY);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,
                filter);
    }

    private void unregisterfinishActivity() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }
}
