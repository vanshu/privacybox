package com.mobimvp.privacybox.ui.applock;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Button;
import android.widget.Toast;

import com.mobimvp.privacybox.Constants;
import com.mobimvp.privacybox.R;
import com.mobimvp.privacybox.ui.BaseActivity;
import com.mobimvp.privacybox.ui.guide.NFCGuideActivity;
import com.mobimvp.privacybox.utility.SystemInfo;

public class LockSettingsActivity extends BaseActivity {
    private Preference unlockType;
    private SharedPreferences preference;
    private ActionBar mActionBar;
    private ListPreference mUnlockmode;
    private Button mNFCMode;
    private Button mNormalMode;
    //unlockType.setSummary(R.string.Privacy_lock_Settings_LevelLow);
    private OnSharedPreferenceChangeListener listener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                              String key) {
            String currentMode = sharedPreferences.getString(Constants.PRIVACY_UNLOCKMODE, "-1");
            showUnlockModeSummary(currentMode);
            if (currentMode.equals("0")) {
                changeNormalMode();
            } else if (currentMode.equals("1")) {
                changeNFCMode();
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActionBar = getPrivacyActionBar();
        mActionBar.setTitle(getString(R.string.Privacy_lock_Settings));
        setContentView(R.layout.locksetting);
        mNFCMode = (Button) findViewById(R.id.btn_nfc);
        mNormalMode = (Button) findViewById(R.id.btn_nosetting);
        String currentMode = preference.getString(Constants.PRIVACY_UNLOCKMODE, "-1");
        if (currentMode.equals("0")) {
            //		mNFCMode.set

            //		mUnlockmode.setSummary("普通模式");
        } else if (currentMode.equals("1")) {
            mUnlockmode.setSummary("NFC解锁模式");
        }

    }

    private void showUnlockModeSummary(String currentMode) {

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void NFCUnlock() {
        switch (SystemInfo.getNFCStatus(LockSettingsActivity.this)) {
            case SystemInfo.DEVICE_NO_NFC:
                Toast.makeText(getApplicationContext(), "您的手机不支持NFC",
                        Toast.LENGTH_SHORT).show();
                preference.edit().putString(Constants.PRIVACY_UNLOCKMODE, "0").commit();
                break;
            case SystemInfo.DEVICE_NFC_DISABLE:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    Toast.makeText(getApplicationContext(), "请进入系统设置，打开NFC功能",
                            Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
                }
                preference.edit().putString(Constants.PRIVACY_UNLOCKMODE, "0").commit();
                break;
            case SystemInfo.DEVICE_NFC_ENABLE:
                Intent intent = new Intent();
                intent.setClass(LockSettingsActivity.this, NFCGuideActivity.class);
                startActivity(intent);
                break;
        }
    }

    public void changeNFCMode() {
        if (preference.getString(Constants.PRIVACY_NFC_ID, null) == null) {
            NFCUnlock();
        }
        ;
    }

    public void changeNormalMode() {
        SystemInfo.setComponentEnable(this, true);
    }


    protected void onResume() {
        super.onResume();
        preference.registerOnSharedPreferenceChangeListener(listener);
    }


    protected void onPause() {
        super.onPause();
        preference.unregisterOnSharedPreferenceChangeListener(listener);
    }


    public boolean onPreferenceClick(Preference preference) {
        if (Constants.PRIVACY_LOCKTYPE.equals(preference.getKey())) {
            Intent intent = new Intent();
            intent.setAction(Constants.BROADCAST_GRAPH_TRAIN);
            LocalBroadcastManager.getInstance(LockSettingsActivity.this)
                    .sendBroadcast(intent);
        }
        return false;
    }

}
