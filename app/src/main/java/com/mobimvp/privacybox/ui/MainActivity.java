package com.mobimvp.privacybox.ui;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.mobimvp.privacybox.Constants;
import com.mobimvp.privacybox.R;
import com.mobimvp.privacybox.service.filelocker.FileLocker;
import com.mobimvp.privacybox.ui.applock.LockAppActivity;
import com.mobimvp.privacybox.ui.filelock.FileLockActivity;
import com.mobimvp.privacybox.ui.filelock.MediaLockActivity;
import com.mobimvp.privacybox.utility.LockAppListUtil;

public class MainActivity extends BaseActivity {

    private Button appLock;
    private Button photoLock;
    private Button videoLock;
    private Button fileLock;
    OnClickListener click = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == appLock) {
                startActivity(new Intent(MainActivity.this,
                        LockAppActivity.class));
            } else if (v == photoLock) {
                if (SDCardMounted()) {
                    startActivity(new Intent(MainActivity.this,
                            MediaLockActivity.class).putExtra(
                            MediaLockActivity.EXTRA_MEDIA_TYPE,
                            Constants.TYPE_PHOTO));
                }
            } else if (v == videoLock) {
                if (SDCardMounted()) {
                    startActivity(new Intent(MainActivity.this,
                            MediaLockActivity.class).putExtra(
                            MediaLockActivity.EXTRA_MEDIA_TYPE,
                            Constants.TYPE_VIDEO));
                }
            } else if (v == fileLock) {
                if (SDCardMounted()) {
                    startActivity(new Intent(MainActivity.this,
                            FileLockActivity.class));
                }
            }
        }
    };

    //	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		MenuInflater inflater = getMenuInflater();
//		inflater.inflate(R.menu.main_activity_actions, menu);
//		return super.onCreateOptionsMenu(menu);
//	}
//
//	@Override
//	public boolean onMenuItemSelected(int featureId, MenuItem item) {
//		switch (item.getItemId()) {
//		case R.id.action_setting:
//			Intent intent = new Intent(MainActivity.this,
//					LockSettingsActivity.class);
//			startActivity(intent);
//			return true;
//
//		default:
//			return super.onMenuItemSelected(featureId, item);
//		}
//	}
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            initData();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ActionBar actionBar = getPrivacyActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        appLock = (Button) findViewById(R.id.app_lock);
        photoLock = (Button) findViewById(R.id.photo_lock);
        videoLock = (Button) findViewById(R.id.video_lock);
        fileLock = (Button) findViewById(R.id.file_lock);
        appLock.setOnClickListener(click);
        photoLock.setOnClickListener(click);
        videoLock.setOnClickListener(click);
        fileLock.setOnClickListener(click);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addAction(Intent.ACTION_MEDIA_SHARED);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        filter.addDataScheme("file");
        registerReceiver(receiver, filter);

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private void initData() {
        int lockAppCount = LockAppListUtil.getInstance().getLockAppCount();
        appLock.setText(String.format(getString(R.string.app_protect_count), lockAppCount));
        long[] ret = FileLocker.getInstance().getAllSummary(Constants.TYPE_PHOTO);
        photoLock.setText(String.format(getString(R.string.photo_encryption_count), ret[0]));
        ret = FileLocker.getInstance().getAllSummary(Constants.TYPE_VIDEO);
        videoLock.setText(String.format(getString(R.string.video_encryption_count), ret[0]));
        ret = FileLocker.getInstance().getAllSummary(Constants.TYPE_FILE);
        fileLock.setText(String.format(getString(R.string.file_encryption_count), ret[0]));
    }

    private boolean SDCardMounted() {
        boolean mounted = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
        if (!mounted) {
            Toast.makeText(this, R.string.plug_sdcard_info, Toast.LENGTH_LONG).show();
        }
        return mounted;
    }
}