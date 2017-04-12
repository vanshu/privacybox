package com.mobimvp.privacybox.ui.filelock;

import android.app.ActionBar;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import com.mobimvp.privacybox.Constants;
import com.mobimvp.privacybox.R;
import com.mobimvp.privacybox.service.filelocker.FileLocker;
import com.mobimvp.privacybox.service.filelocker.internal.DBAction;
import com.mobimvp.privacybox.service.filelocker.internal.EncryptItem;
import com.mobimvp.privacybox.ui.SDCardActivity;
import com.mobimvp.privacybox.ui.browser.MediaBrowserActivity;
import com.mobimvp.privacybox.ui.browser.MediaBrowserAdapter.MediaType;
import com.mobimvp.privacybox.ui.widgets.AlertDialogLite;
import com.mobimvp.privacybox.ui.widgets.GenerateButtom;
import com.mobimvp.privacybox.ui.widgets.GenerateButtom.GenerateButtomButton;
import com.mobimvp.privacybox.ui.widgets.GridViewEx;
import com.mobimvp.privacybox.utility.AsyncLoader;
import com.mobimvp.privacybox.utility.ViewUtility;

import java.util.ArrayList;
import java.util.List;

public class MediaLockActivity extends SDCardActivity implements
        FileLockerListener, OnItemClickListener, OnItemLongClickListener {

    public static final String EXTRA_MEDIA_TYPE = "extra_mediatype";
    private FileLocker mLocker;
    private int mType;
    private GridViewEx mGridView;
    private ActionBar mActionBar;
    private MediaLockAdapter mAdapter;
    private LinearLayout parentView;
    private GenerateButtom mAddButtom;
    private GenerateButtom mEditButtom;
    private MediaLoader mLoader;
    private List<EncryptItem> mFileList;
    private String SdrootPath = Environment.getExternalStorageDirectory().toString();
    private View.OnClickListener decryptlistener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            List<EncryptItem> selectedFiles = mAdapter.getSelectedFiles();
            if (selectedFiles.size() > 0) {
                FileOperationUI.CreateDecryptUI(MediaLockActivity.this,
                        selectedFiles, MediaLockActivity.this).start();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.File_Lock_ChooseFileFirst),
                        Toast.LENGTH_LONG).show();
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
                new AlertDialogLite.Builder(MediaLockActivity.this)
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
                                        Toast.makeText(
                                                MediaLockActivity.this,
                                                String.format(getString(R.string.delete_select_file),
                                                        selectedFiles.size()),
                                                Toast.LENGTH_LONG).show();
                                        OnRefreshUI();
                                    }
                                })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create().show();
            } else {
                Toast.makeText(MediaLockActivity.this, getString(R.string.File_Lock_ChooseFileFirst),
                        Toast.LENGTH_LONG).show();
            }
        }
    };
    private View.OnClickListener mediaLockClicklistener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MediaType type;
            if (mType == Constants.TYPE_PHOTO) {
                type = MediaType.Image;
            } else if (mType == Constants.TYPE_VIDEO) {
                type = MediaType.Video;
            } else {
                type = MediaType.Image;
            }
            startActivityForResult(new Intent(MediaLockActivity.this,
                    MediaBrowserActivity.class).putExtra(
                    MediaBrowserActivity.EXTRA_MEDIA_TYPE,
                    type.toString()).putExtra(
                    MediaBrowserActivity.EXTRA_SDROOT, SdrootPath), 0);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mType = getIntent().getIntExtra(EXTRA_MEDIA_TYPE, Constants.TYPE_PHOTO);
        mActionBar = getPrivacyActionBar();
        mGridView = new GridViewEx(this);
        mGridView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.0f));
        mAddButtom = new GenerateButtom(this);
        if (mType == Constants.TYPE_PHOTO) {
            mActionBar.setTitle(getString(R.string.picture_encryption));
            mAddButtom.add(new GenerateButtomButton(mediaLockClicklistener, getString(R.string.File_Lock_AddPhoto)));
        } else if (mType == Constants.TYPE_VIDEO) {
            mActionBar.setTitle(getString(R.string.video_encryption));
            mAddButtom.add(new GenerateButtomButton(mediaLockClicklistener,
                    getString(R.string.File_Lock_AddVideo)));
        }
        mEditButtom = new GenerateButtom(this);
        mEditButtom.add(new GenerateButtomButton(decryptlistener, getString(R.string.decryption)),
                new GenerateButtomButton(reverseSelectlistener, getString(R.string.inverse)),
                new GenerateButtomButton(destroylistener, getString(R.string.delete)));
        parentView = (LinearLayout) new ViewUtility(this).add(mGridView,
                mAddButtom);
        setContentView(parentView);
        mLocker = FileLocker.getInstance();
        SdrootPath = DBAction.getSdcardRootPath();
        changeToContentView();
    }

    protected void onResume() {
        super.onResume();
        try {
            mLocker.gc();
        } catch (Exception e) {
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mLoader != null) {
            mLoader.cancel();
            mLoader = null;
        }
    }

    @Override
    protected void onDestroy() {
        if (mAdapter != null)
            mAdapter.recycle();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mAdapter != null && mAdapter.isEditMode()) {
            onActionClicked();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mediafilelock_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void onActionClicked() {
        mAdapter.toggleEditMode();
        if (mAdapter.isEditMode()) {
            parentView.removeView(mAddButtom);
            parentView.addView(mEditButtom);
        } else {
            parentView.removeView(mEditButtom);
            parentView.addView(mAddButtom);
        }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            List<String> fileList = data
                    .getStringArrayListExtra(MediaBrowserActivity.EXTRA_FILELIST);
            if (fileList != null) {
                List<EncryptItem> eList = new ArrayList<EncryptItem>();
                for (String file : fileList) {
                    EncryptItem ei = new EncryptItem(mType, SdrootPath);
                    ei.setOriginPath(file);
                    eList.add(ei);
                }
                FileOperationUI.CreateEncryptUI(this, eList, this).start();
            }
        } else if (resultCode == MediaGalleryActivity.RESULT_CHANGED) {
            OnRefreshUI();
        }
    }

    private void changeToContentView() {
        mGridView.setDefaultColumn();
        if (mType == Constants.TYPE_PHOTO) {
            mGridView.setEmptyText(getString(R.string.please_add_encryption_picture));
        } else if (mType == Constants.TYPE_VIDEO) {
            mGridView.setEmptyText(getString(R.string.please_add_encryption_video));
        }
        mFileList = new ArrayList<EncryptItem>();
        if (mType == Constants.TYPE_PHOTO) {
            mAdapter = new MediaLockAdapter(this, MediaType.Image, mFileList);
        } else if (mType == Constants.TYPE_VIDEO) {
            mAdapter = new MediaLockAdapter(this, MediaType.Video, mFileList);
        } else {
            finish();
            return;
        }
        mAdapter.setFileLockerListener(this);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);
        mGridView.setOnItemLongClickListener(this);
        OnRefreshUI();
    }

    @Override
    public void OnPreviewItem(int position) {
        if (mType == Constants.TYPE_VIDEO) {
            List<EncryptItem> previewList = new ArrayList<EncryptItem>();
            previewList.add(mFileList.get(position));
            FileOperationUI.CreatePreviewVideoUI(this, previewList, this)
                    .start();
        } else if (mType == Constants.TYPE_PHOTO) {
            startActivityForResult(
                    new Intent(this, MediaGalleryActivity.class).putExtra(
                            MediaGalleryActivity.EXTRA_POSITION, position),
                    MediaGalleryActivity.RESULT_UNCHANGED);
        }
    }

    @Override
    public void OnRefreshUI() {
        if (mAdapter != null && mAdapter.isEditMode()) {
            mAdapter.toggleEditMode();
            parentView.removeView(mEditButtom);
            parentView.addView(mAddButtom);
        }
        mLoader = new MediaLoader();
        mLoader.execute(this);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        mAdapter.select(arg2);
    }

    @Override
    public void onEnterEditMode(int position) {
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {
        if (!mAdapter.isEditMode()) {
            mAdapter.toggleEditMode();
            parentView.removeView(mAddButtom);
            parentView.addView(mEditButtom);
            mAdapter.select(arg2);
        }
        return true;
    }

    private static class MediaLoader extends
            AsyncLoader<MediaLockActivity, List<EncryptItem>, Integer> {
        @Override
        protected void onPreExecute() {
            MediaLockActivity context = getActivity();
            context.mGridView.showLoadingScreen(context.getString(R.string.generic_loading));
        }

        @Override
        protected List<EncryptItem> doInBackground() {
            MediaLockActivity context = getActivity();
            return context.mLocker.getEncryptedItems(context.mType);
        }

        @Override
        protected void onPostExecute(List<EncryptItem> loadResult) {
            MediaLockActivity context = getActivity();
            context.mGridView.hideLoadingScreen();
            context.mFileList.clear();
            context.mFileList.addAll(loadResult);
            context.mAdapter.notifyDataSetChanged();
        }

    }

}
