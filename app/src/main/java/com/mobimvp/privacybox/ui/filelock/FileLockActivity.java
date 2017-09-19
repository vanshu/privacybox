package com.mobimvp.privacybox.ui.filelock;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.app.ActionBar;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import com.mobimvp.privacybox.Constants;
import com.mobimvp.privacybox.R;
import com.mobimvp.privacybox.service.filelocker.FileLocker;
import com.mobimvp.privacybox.service.filelocker.internal.DBAction;
import com.mobimvp.privacybox.service.filelocker.internal.EncryptItem;
import com.mobimvp.privacybox.ui.SDCardActivity;
import com.mobimvp.privacybox.ui.browser.FileBrowserActivity;
import com.mobimvp.privacybox.ui.widgets.AlertDialogLite;
import com.mobimvp.privacybox.ui.widgets.GenerateButtom;
import com.mobimvp.privacybox.ui.widgets.GenerateButtom.GenerateButtomButton;
import com.mobimvp.privacybox.ui.widgets.ListViewEx;
import com.mobimvp.privacybox.ui.widgets.RevealColorView;
import com.mobimvp.privacybox.utility.AsyncLoader;
import com.mobimvp.privacybox.utility.SystemInfo;

public class FileLockActivity extends SDCardActivity implements
		FileLockerListener {

	private static class FileLoader extends AsyncLoader<FileLockActivity, List<EncryptItem>, Integer> {

		@Override
		protected List<EncryptItem> doInBackground() {
			FileLockActivity context = getActivity();
			return context.mLocker.getEncryptedItems(Constants.TYPE_FILE);
		}

		@Override
		protected void onPostExecute(List<EncryptItem> loadResult) {
			FileLockActivity context = getActivity();
			context.mListView.hideLoadingScreen();
			context.mFileList.clear();
			context.mFileList.addAll(loadResult);
			context.mAdapter.notifyDataSetChanged();
		}

		@Override
		protected void onPreExecute() {
			FileLockActivity context = getActivity();
			context.mListView.showLoadingScreen(context.getString(R.string.generic_loading));
		}
	}

	private FileLocker mLocker;

	private ListViewEx mListView;
	private ActionBar mActionBar;
	private FileLockAdapter mAdapter;

	private GenerateButtom mAddButtom;
	private GenerateButtom mEditButtom;
	private LinearLayout mContentView;

	private FileLoader mLoader;

	private List<EncryptItem> mFileList;
	private String SdrootPath ;
	private RevealColorView mRevealColorView;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar = getPrivacyActionBar();
		mActionBar.setTitle(getString(R.string.file_encryption));
		mListView = new ListViewEx(this);
		mListView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,0, 1.0f)); // 动态设置权重,宽为全屏，高度根据其他控件自动缩放
		mFileList = new ArrayList<EncryptItem>();
		mAdapter = new FileLockAdapter(this, mFileList);
		mAdapter.setFileLockerListener(this);
		mEditButtom = new GenerateButtom(this);
		mAddButtom = new GenerateButtom(this);
		mEditButtom.add(new GenerateButtomButton(decryptlistener, getString(R.string.decryption)),new GenerateButtomButton(reverseSelectlistener, getString(R.string.inverse)),
				new GenerateButtomButton(destroylistener, getString(R.string.delete)));
		mAddButtom.add(new GenerateButtomButton(addlistener, getString(R.string.File_Lock_AddFile)));  
		mLocker = FileLocker.getInstance();
		View parentView=LayoutInflater.from(this).inflate(R.layout.parent, null);
		mRevealColorView=(RevealColorView)parentView.findViewById(R.id.reveal_view);
		mContentView=(LinearLayout) parentView.findViewById(R.id.content);
		SystemInfo.addView(mContentView,mListView,mAddButtom);
		setContentView(parentView);
		SdrootPath = DBAction.getSdcardRootPath();
		mListView.setEmptyText(getString(R.string.please_add_encryption_file));
		mListView.setAdapter(mAdapter);
		OnRefreshUI();
	}
	


	private View.OnClickListener decryptlistener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			List<EncryptItem> selectedFiles = mAdapter.getSelectedFiles();
			if (selectedFiles.size() > 0) {
				FileOperationUI.CreateDecryptUI(FileLockActivity.this,
						selectedFiles, FileLockActivity.this).start();
			} else {
				Toast.makeText(getApplicationContext(), getString(R.string.File_Lock_ChooseFileFirst),Toast.LENGTH_LONG).show();
			}
		}
	};

	private View.OnClickListener reverseSelectlistener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			mAdapter.reverseSelect();
		}
	};

	private View.OnClickListener destroylistener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mAdapter.getSelectedFiles().size() > 0) {
				new AlertDialogLite.Builder(FileLockActivity.this)
						.setTitle(R.string.app_name)
						.setMessage(R.string.File_Lock_Destroy)
						.setPositiveButton(android.R.string.ok,
								new OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										List<EncryptItem> selectedFiles = mAdapter
												.getSelectedFiles();
										mLocker.deleteEncryptItem(selectedFiles);
										Toast.makeText(FileLockActivity.this,String.format(getString(R.string.delete_select_file),selectedFiles.size()),
												Toast.LENGTH_LONG).show();
										OnRefreshUI();
									}
								})
						.setNegativeButton(android.R.string.cancel, null)
						.create().show();
			} else {
				Toast.makeText(FileLockActivity.this, getString(R.string.File_Lock_ChooseFileFirst),
						Toast.LENGTH_LONG).show();
			}
		}
	};

	private View.OnClickListener addlistener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			startActivityForResult(new Intent(FileLockActivity.this,
					FileBrowserActivity.class).putExtra(
					FileBrowserActivity.EXTRA_SDROOT, SdrootPath), 0);
//			final Point p = getLocationInView(mRevealColorView, v);
//			mRevealColorView.reveal(p.x, p.y,Color.parseColor("#6666cc"), mAnimatorListner);
		}
	};
	
	private AnimatorListener mAnimatorListner=new AnimatorListener() {
		
		@Override
		public void onAnimationStart(Animator animation) {
			
		}
		
		@Override
		public void onAnimationRepeat(Animator animation) {
			
		}
		
		@Override
		public void onAnimationEnd(Animator animation) {
		
		}
		
		@Override
		public void onAnimationCancel(Animator animation) {
			
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && data != null) {
			List<String> fileList = data
					.getStringArrayListExtra(FileBrowserActivity.EXTRA_FILELIST);
			if (fileList != null) {
				List<EncryptItem> eList = new ArrayList<EncryptItem>();
				for (String file : fileList) {
					EncryptItem ei = new EncryptItem(Constants.TYPE_FILE,SdrootPath);
					ei.setOriginPath(file);
					eList.add(ei);
				}
				FileOperationUI.CreateEncryptUI(this, eList, this).start();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			mLocker.gc();
		} catch (Exception e) {
		}
	}

	@Override
	public void onBackPressed() {
		if (mAdapter.isEditMode()) {
			onActionClicked();
		} else {
			super.onBackPressed();
		}
	}

	private void onActionClicked() {
		mAdapter.toggleEditMode();
		if (mAdapter.isEditMode()) {
			mContentView.removeView(mAddButtom);
			mContentView.addView(mEditButtom);
		} else {
			mContentView.removeView(mEditButtom);
			mContentView.addView(mAddButtom);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mediafilelock_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_edit:
			onActionClicked();
			return true;
		case android.R.id.home:
			if (mAdapter.isEditMode()) {
				onActionClicked();
			} else {
				super.onBackPressed();
			}
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	@Override
	public void OnPreviewItem(int position) {
		List<EncryptItem> previewList = new ArrayList<EncryptItem>();
		previewList.add(mFileList.get(position));
		FileOperationUI.CreateTempDecryptUI(this, previewList, this).start();
	}

	@Override
	public void OnRefreshUI() {
		if (mAdapter.isEditMode()) {
			mAdapter.toggleEditMode();
			mContentView.removeView(mEditButtom);
			mContentView.addView(mAddButtom);
		}
		mLoader = new FileLoader();
		mLoader.execute(this);
	}

	@Override
	public void onEnterEditMode(int position) {
		mContentView.removeView(mAddButtom);
		mContentView.addView(mEditButtom);
	}
	
	
	private Point getLocationInView(View src, View target) {
		final int[] l0 = new int[2];
		src.getLocationOnScreen(l0);
 
		final int[] l1 = new int[2];
		target.getLocationOnScreen(l1);
 
		l1[0] = l1[0] - l0[0] + target.getWidth() / 2;
		l1[1] = l1[1] - l0[1] + target.getHeight() / 2;
 
		return new Point(l1[0], l1[1]);
	}
}

