package com.mobimvp.privacybox.ui.guide;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobimvp.privacybox.Constants;
import com.mobimvp.privacybox.R;
import com.mobimvp.privacybox.ui.BaseActivity;
import com.mobimvp.privacybox.utility.SystemInfo;

public class NFCGuideActivity extends BaseActivity {

    public int currentLevel = 0;
    private Button btnBottom;
    private ImageView btnNFCSetting1;
    private ImageView btnNFCSetting2;
    private ImageView btnNFCSetted;
    private ImageView btnProgress1;
    private ImageView btnProgress2;
    private TextView tvNfcTips;
    private CheckBox ckHideLanucher;
    private String firstID = null;
    private String secondID = null;
    private SharedPreferences preference;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            currentLevel++;
            if (currentLevel == 1) {
                firstID = intent.getStringExtra(NFCBadgeActivity.NFC_ID);
                levelTwo();
            } else if (currentLevel == 2) {
                secondID = intent.getStringExtra(NFCBadgeActivity.NFC_ID);
                levelFinish();
            } else {
                LocalBroadcastManager.getInstance(NFCGuideActivity.this).unregisterReceiver(receiver);
            }
        }
    };
    private View.OnClickListener bottomListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (validateNFCID()) {
                preference.edit().putString(Constants.PRIVACY_NFC_ID, secondID).commit();
                preference.edit().putBoolean(Constants.PRIVACY_NFC_UNLOCK, true).commit();
                if (ckHideLanucher.isChecked()) {  //是否勾选隐藏桌面图标
                    SystemInfo.setComponentEnable(NFCGuideActivity.this, false);
                    preference.edit().putBoolean(Constants.PRIVACY_HIDE_LANUCHER, true).commit();
                    preference.edit().putString(Constants.PRIVACY_UNLOCKMODE, "1").commit();
                }
                toPatternLockScreen();
            } else {
                NFCGuideActivity.this.finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPrivacyActionBar().setTitle(getString(R.string.nfc_unlock));
        setContentView(R.layout.nfcguide);
        btnBottom = (Button) findViewById(R.id.btn_bottom);
        btnNFCSetting1 = (ImageView) findViewById(R.id.iv_nfcsetting1);
        btnNFCSetting2 = (ImageView) findViewById(R.id.iv_nfcsetting2);
        btnProgress1 = (ImageView) findViewById(R.id.iv_progress1);
        btnProgress2 = (ImageView) findViewById(R.id.iv_progress2);
        btnNFCSetted = (ImageView) findViewById(R.id.iv_nfcsetted);
        tvNfcTips = (TextView) findViewById(R.id.tv_nfcsetting_tips);
        ckHideLanucher = (CheckBox) findViewById(R.id.ck_hide_lanucher);
        btnBottom.setOnClickListener(bottomListener);
        initLevel();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.BROADCAST_NFC_ID);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
        preference = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(NFCGuideActivity.this).unregisterReceiver(receiver);
    }

    private void initLevel() {
        btnNFCSetting1.setVisibility(View.VISIBLE);
        tvNfcTips.setText(getString(R.string.nfc_unlock_tips1));
        btnBottom.setText(getString(R.string.cancel));
    }

    private void levelTwo() {
        btnNFCSetting1.setVisibility(View.VISIBLE);
        btnNFCSetting2.setVisibility(View.VISIBLE);
        btnProgress1.setVisibility(View.VISIBLE);
        tvNfcTips.setText(getString(R.string.nfc_unlock_tips2));
    }

    private void levelFinish() {
        btnProgress2.setVisibility(View.VISIBLE);
        btnNFCSetted.setVisibility(View.VISIBLE);
        if (validateNFCID()) {
            tvNfcTips.setText(getString(R.string.nfc_unlock_success));
            btnBottom.setBackgroundResource(R.drawable.btn_light_green);
        } else {
            tvNfcTips.setText(getString(R.string.nfc_unlock_fail));
            btnBottom.setBackgroundResource(R.drawable.btn_red);
        }
        btnBottom.setText(getString(R.string.ok));
        btnBottom.setTextColor(getResources().getColor(R.color.textcolor_white));
    }

    private void toPatternLockScreen() {
        Intent intent = new Intent();
        intent.setAction(Constants.BROADCAST_FIRST_TRAIN);
        LocalBroadcastManager.getInstance(NFCGuideActivity.this).sendBroadcast(intent);
        finish();
    }

    public boolean validateNFCID() {
        if ((firstID != null && secondID != null) && firstID.equals(secondID))
            return true;
        else
            return false;
    }

}
