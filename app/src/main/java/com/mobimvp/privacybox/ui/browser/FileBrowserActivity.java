package com.mobimvp.privacybox.ui.browser;

import java.io.File;
import java.util.ArrayList;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import com.mobimvp.privacybox.R;
import com.mobimvp.privacybox.ui.SDCardActivity;
import com.mobimvp.privacybox.ui.widgets.GenerateButtom;
import com.mobimvp.privacybox.ui.widgets.GenerateButtom.GenerateButtomButton;
import com.mobimvp.privacybox.ui.widgets.ListViewEx;

public class FileBrowserActivity extends SDCardActivity implements
		FileBrowserListener {

	public static final String EXTRA_FILELIST = "extra_filelist";

	public static final String EXTRA_SDROOT = "SDROOT";
	private ListViewEx mListView;
	private FileBrowserAdapter mAdapter;
	private ActionBar mActionBar;
	private LinearLayout parentView;
	private GenerateButtom mButtomBar;
	private Handler mHandler = new Handler(Looper.getMainLooper());

	private File sdRoot = Environment.getExternalStorageDirectory();

	private long LOADING_DELAY = 500;
	private Runnable showLoadingAction = new Runnable() {

		@Override
		public void run() {
			if (!mListView.isLoading()) {
				mListView
						.showLoadingScreen(getString(R.string.generic_loading));
			}
		}
	};

	private Runnable hideLoadingAction = new Runnable() {

		@Override
		public void run() {
			if (mListView.isLoading()) {
				mListView.hideLoadingScreen();
			}
			String path = "";
			if (sdRoot.equals(Environment.getExternalStorageDirectory())) {
				path = mAdapter.getCurrentFolder().getAbsolutePath()
						.replace(sdRoot.getAbsolutePath(), getString(R.string.stardard_sdcard));
			}
			mActionBar.setSubtitle(path);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String r = getIntent().getStringExtra(EXTRA_SDROOT);
		if (r != null) {
			sdRoot = new File(r);
		}
		mActionBar = getPrivacyActionBar();
		mActionBar.setTitle(getString(R.string.select_file));
		mListView = new ListViewEx(this);
		mListView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				0, 1.0f));
		mButtomBar = new GenerateButtom(this);
		mButtomBar.add(new GenerateButtomButton(mEncryptListener,
				getString(R.string.encryption)),
				new GenerateButtomButton(mSelectAllListener,
						getString(R.string.check_all)));
		mAdapter = new FileBrowserAdapter(this, sdRoot);
		mAdapter.setBrowserListener(this);
		mListView.setAdapter(mAdapter);
		mAdapter.browseFolder(sdRoot);
		parentView = new LinearLayout(this);
		parentView.setOrientation(LinearLayout.VERTICAL);
		parentView.addView(mListView);
		parentView.addView(mButtomBar);
		setContentView(parentView);
	}

	@Override
	public void onBackPressed() {
		if (!mAdapter.isBusy()) {
			if (sdRoot.equals(mAdapter.getCurrentFolder())) {
				setResult(RESULT_CANCELED);
				finish();
			} else {
				mAdapter.browseParent();
			}
		}
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			if (!mAdapter.isBusy()) {
				if (sdRoot.equals(mAdapter.getCurrentFolder())) {
					setResult(RESULT_CANCELED);
					finish();
				} else {
					mAdapter.browseParent();
				}
			}
			return true;
		} else {
			return super.onMenuItemSelected(featureId, item);
		}
	}

	@Override
	public void OnStartLoadFolder(File folder) {
		mHandler.removeCallbacksAndMessages(null);
		mHandler.postDelayed(showLoadingAction, LOADING_DELAY);
	}

	@Override
	public void OnFinishLoadFolder(File folder) {
		mHandler.removeCallbacksAndMessages(null);
		mHandler.post(hideLoadingAction);
	}

	private View.OnClickListener mEncryptListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			ArrayList<String> selectedFiles = mAdapter.getSelectedFiles();
			if (selectedFiles.size() > 0) {
				Intent result = new Intent();
				result.putStringArrayListExtra(EXTRA_FILELIST, selectedFiles);
				setResult(RESULT_OK, result);
				finish();
			} else {
				Toast.makeText(getApplicationContext(),
						getString(R.string.File_Lock_ChooseFileFirst),
						Toast.LENGTH_LONG).show();
			}
		}
	};

	private View.OnClickListener mSelectAllListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			mAdapter.selectAll();
		}
	};
}
