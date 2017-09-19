package com.mobimvp.privacybox.ui.browser;

import java.io.File;
import java.util.ArrayList;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import com.mobimvp.privacybox.R;
import com.mobimvp.privacybox.ui.SDCardActivity;
import com.mobimvp.privacybox.ui.browser.MediaBrowserAdapter.BrowseMode;
import com.mobimvp.privacybox.ui.browser.MediaBrowserAdapter.MediaType;
import com.mobimvp.privacybox.ui.widgets.GenerateButtom;
import com.mobimvp.privacybox.ui.widgets.GenerateButtom.GenerateButtomButton;
import com.mobimvp.privacybox.ui.widgets.GridViewEx;
import com.mobimvp.privacybox.utility.ViewUtility;

public class MediaBrowserActivity extends SDCardActivity implements
		OnItemClickListener, MediaBrowserListener {

	public static final String EXTRA_FILELIST = "extra_filelist";
	public static final String EXTRA_MEDIA_TYPE = "extra_mediatype";
	public static final String EXTRA_SDROOT = "SDROOT";
	private GridViewEx mGridView;
	private MediaBrowserAdapter mAdapter;
	private ActionBar mActionBar;
	private GenerateButtom mStopScanButtom;
	private GenerateButtom mEncryptButtom;
	private MediaType mMediaType;
	private LinearLayout parentView;

	private boolean scanFinished;

	private File sdRoot = Environment.getExternalStorageDirectory();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar = getPrivacyActionBar();
		String r = getIntent().getStringExtra(EXTRA_SDROOT);
		if (r != null) {
			sdRoot = new File(r);
		}
		mGridView = new GridViewEx(this);
		mGridView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				0, 1.0f));

		try {
			mMediaType = MediaType.valueOf(getIntent().getStringExtra(
					EXTRA_MEDIA_TYPE));
		} catch (Exception e) {
			mMediaType = MediaType.Image;
		}

		if (mMediaType == MediaType.Video) {
			setTitle(getString(R.string.select_video));
			mGridView.setEmptyText(getString(R.string.no_video));
		} else if (mMediaType == MediaType.Image) {
			mActionBar.setTitle(getString(R.string.select_photo));
			mGridView.setEmptyText(getString(R.string.no_photo));
		} 
		mAdapter = new MediaBrowserAdapter(this, mMediaType, sdRoot);
		mAdapter.setBrowserListener(this);
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(this);
		mStopScanButtom = new GenerateButtom(this);
		mStopScanButtom
				.add(new GenerateButtomButton(mStopScanListener, getString(R.string.stop_scan)));
		mEncryptButtom = new GenerateButtom(this);
		mEncryptButtom.add(new GenerateButtomButton(mEncryptListener, getString(R.string.encryption)),
				new GenerateButtomButton(mSelectAllListener, getString(R.string.check_all)));
		parentView = (LinearLayout) new ViewUtility(this).add(mGridView,
				mStopScanButtom);
		setContentView(parentView);
		mAdapter.startScan();
	}

	private View.OnClickListener mSelectAllListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			mAdapter.selectAll();
		}
	};

	private View.OnClickListener mEncryptListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			ArrayList<String> result = mAdapter.getSelectedFiles();
			if (result.size() > 0) {
				Intent intent = new Intent();
				intent.putStringArrayListExtra(EXTRA_FILELIST, result);
				setResult(RESULT_OK, intent);
				finish();
			} else {
				Toast.makeText(getApplicationContext(),getString(R.string.File_Lock_ChooseFileFirst),
						Toast.LENGTH_LONG).show();
			}
		}
	};

	private View.OnClickListener mStopScanListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			mAdapter.stopScan();
		}
	};

	@Override
	protected void onDestroy() {
		mAdapter.recycle();
		super.onDestroy();
	}

	protected void onResume() {
		super.onResume();
		mGridView.setDefaultColumn();
	}

	@Override
	public void onBackPressed() {
		if (mAdapter.getMode() == BrowseMode.FileList) {
			mAdapter.exitFileListMode();
		} else {
			setResult(RESULT_CANCELED);
			finish();
		}
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			if (mAdapter.getMode() == BrowseMode.FileList) {
				mAdapter.exitFileListMode();
			} else {
				setResult(RESULT_CANCELED);
				finish();
			}
			return true;
		} else {
			return super.onMenuItemSelected(featureId, item);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		mAdapter.select(arg2);
	}

	@Override
	public void OnBrowse(BrowseMode mode, File folder) {
		if (mode == BrowseMode.FolderList) {
			String path;
			if (sdRoot.equals(Environment.getExternalStorageDirectory())) {
				path = mAdapter.getCurrentFolder().getAbsolutePath()
						.replace(sdRoot.getAbsolutePath(),getString(R.string.stardard_sdcard));
				mActionBar.setSubtitle(path);
			} 

			if (parentView.indexOfChild(mEncryptButtom) != -1) {
				parentView.removeView(mEncryptButtom);
			}
			if (parentView.indexOfChild(mStopScanButtom) != -1) {
				parentView.removeView(mStopScanButtom);
			}
			if (!scanFinished) {
				if (parentView.indexOfChild(mStopScanButtom) == -1) {
					parentView.addView(mStopScanButtom);
				}
			}
		} else if (folder != null) {
			String path = "";
			if (sdRoot.equals(Environment.getExternalStorageDirectory())) {
				path = folder.getAbsolutePath().replace(
						sdRoot.getAbsolutePath(), getString(R.string.stardard_sdcard));
			}
			mActionBar.setSubtitle(path);
			parentView.removeView(mStopScanButtom);
			parentView.addView(mEncryptButtom);
		}
	}

	@Override
	public void OnScanFinished(boolean stopped) {
		scanFinished = true;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mAdapter.getMode() == BrowseMode.FolderList) {
					parentView.removeView(mStopScanButtom);
				}
			}
		});
	}
}
