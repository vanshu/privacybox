package com.mobimvp.privacybox.ui.filelock;

import java.util.ArrayList;
import java.util.List;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mobimvp.privacybox.Constants;
import com.mobimvp.privacybox.R;
import com.mobimvp.privacybox.service.filelocker.FileLocker;
import com.mobimvp.privacybox.service.filelocker.internal.EncryptItem;
import com.mobimvp.privacybox.ui.SDCardActivity;
import com.mobimvp.privacybox.ui.widgets.AlertDialogLite;
import com.mobimvp.privacybox.ui.widgets.ru.truba.touchgallery.GalleryWidget.EncryptedPagerAdapter;
import com.mobimvp.privacybox.ui.widgets.ru.truba.touchgallery.GalleryWidget.GalleryViewPager;

public class MediaGalleryActivity extends SDCardActivity implements OnPageChangeListener {
	
	public static final String EXTRA_POSITION = "extra_position";
	public static final int RESULT_CHANGED           = 10000;
	public static final int RESULT_UNCHANGED           = 10001;
	private GalleryViewPager mGallery;
	private TextView mImageName;
	private TextView mGalleryPosition;
	private Button mDecrypt;
	private Button mDelete;

	private EncryptedPagerAdapter mAdapter;
	private List<EncryptItem> mList;
	
	private FileLocker mLocker;
	
	private class DecryptionListener implements FileLockerListener {
		
		public int mPosition;
		
		public DecryptionListener(int position) {
			mPosition = position;
		}

		@Override
		public void onEnterEditMode(int position) {
		}

		@Override
		public void OnPreviewItem(int position) {
		}

		@Override
		public void OnRefreshUI() {
			if (mList.size() <= 1) {
				finish();
			} else {
				mAdapter.removeItem(mPosition);
				if (mPosition > mList.size() - 1) {
					mPosition = mList.size() - 1;
				}
				onPageSelected(mPosition);
			}
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setResult(RESULT_UNCHANGED);
		mLocker = FileLocker.getInstance();
		mLocker.gc();
		
		int position = getIntent().getIntExtra(EXTRA_POSITION, 0);
		
		getPrivacyActionBar().hide();
		setContentView(R.layout.preview);
		
		mGallery = (GalleryViewPager) findViewById(R.id.gallery);
		mImageName = (TextView) findViewById(R.id.name);
		mGalleryPosition = (TextView) findViewById(R.id.position);
		mDecrypt = (Button) findViewById(R.id.decrypt);
		mDelete = (Button) findViewById(R.id.delete);
		
		mList = new ArrayList<EncryptItem>(mLocker.getEncryptedItems(Constants.TYPE_PHOTO));
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		mAdapter = new EncryptedPagerAdapter(this, dm, mList);
		mGallery.setAdapter(mAdapter);
		mGallery.setOnPageChangeListener(this);
		mGallery.setCurrentItem(position);
		onPageSelected(position);
		
		mDecrypt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				List<EncryptItem> list = new ArrayList<EncryptItem>();
				list.add(mList.get(mGallery.getCurrentItem()));

				FileOperationUI.CreateDecryptUI(MediaGalleryActivity.this, list, new DecryptionListener(mGallery.getCurrentItem())).start();
				setResult(RESULT_CHANGED);
			}
		});
		
		mDelete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialogLite.Builder(MediaGalleryActivity.this).setTitle(R.string.app_name).setMessage(R.string.File_Lock_Destroy)
				.setPositiveButton(android.R.string.ok, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						int position = mGallery.getCurrentItem();
						List<EncryptItem> list = new ArrayList<EncryptItem>();
						list.add(mList.get(position));
						
						mLocker.deleteEncryptItem(list);
						setResult(RESULT_CHANGED);
						if (mList.size() <= 1) {
							finish();
						} else {
							mAdapter.removeItem(position);
							if (position > mList.size() - 1) {
								position = mList.size() - 1;
							}
							onPageSelected(position);
						}
						Toast.makeText(MediaGalleryActivity.this, getString(R.string.file_delete_already), Toast.LENGTH_LONG).show();
					}
				}).setNegativeButton(android.R.string.cancel, null).create().show();
				
				
			}
		});
	}

	@Override
	protected void onDestroy() {
		mAdapter.recycle();
		super.onDestroy();
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(int position) {
		try {
			mImageName.setText(mList.get(position).getName());
			mGalleryPosition.setText(String.format("%1$d/%2$d", position+1, mList.size()));
		} catch (Exception e) {
		}
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}
}
