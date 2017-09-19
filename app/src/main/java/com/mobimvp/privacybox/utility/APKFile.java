package com.mobimvp.privacybox.utility;

import java.io.File;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import com.mobimvp.privacybox.PBApplication;

public class APKFile extends File {

	public static class APKFileWithLockStatus extends APKFile{
		private static final long serialVersionUID = 2987092715572600051L;
		private int lockstatus;//0-unlock,1-lock
		public static final int APK_LOCKSTATUS_LOCK = 0;
		public static final int APK_LOCKSTATUS_UNLOCK = 1;
		public APKFileWithLockStatus(PackageInfo info, int lockstatus) {
			super(PBApplication.getApplication(), info);
			this.lockstatus = lockstatus;
		}
		public int getLockstatus(){
			return lockstatus;
		}
		public boolean isLock(){
			return lockstatus==APK_LOCKSTATUS_LOCK;
		}
		public boolean isUnLock(){
			return lockstatus==APK_LOCKSTATUS_UNLOCK;
		}
		public void setLock(boolean lock){
			if(lock){
				lockstatus=APK_LOCKSTATUS_LOCK;
			}else{
				lockstatus=APK_LOCKSTATUS_UNLOCK;
			}
		}
	}

		
	private static final long serialVersionUID = -6798139977976928135L;
	protected PackageInfo packageInfo;
	protected Drawable icon;
	protected CharSequence label;
	private Object tag;
	private boolean selected = false;
	private Context mContext;


	public APKFile(Context context, PackageInfo info) {
		super(info.applicationInfo.sourceDir);
		this.mContext = context;
		this.packageInfo = info;
	}

	public static APKFile CreateFromPackage(Context context, String packageName) throws Exception {
		PackageManager pm = context.getPackageManager();
		return new APKFile(context, pm.getPackageInfo(packageName, 0));
	}
	

	public Object getTag() {
		return tag;
	}

	public void setTag(Object tag) {
		this.tag = tag;
	}

	public PackageInfo getPackageInfo() {
		return packageInfo;
	}

	public String getPackageName() {
		return packageInfo.packageName;
	}

	public synchronized Drawable getIcon() {
		if (icon == null) {
			try {
				icon = packageInfo.applicationInfo.loadIcon(mContext.getPackageManager());
			} catch (Throwable e) {
				//抛出的可能是OutOfMemoryError或者OutOfMemoryException
			}
			if (icon == null) {
				icon = mContext.getResources().getDrawable(android.R.drawable.sym_def_app_icon);
			}
		}
		return icon;
	}

	public synchronized CharSequence getLabel() {

		if (label == null) {
			try {
				label = packageInfo.applicationInfo.loadLabel(mContext.getPackageManager());
			} catch (Exception e) {
			}
			try {
				if (label == null && packageInfo != null) {
					label = packageInfo.packageName;
				}
			} catch (Exception e) {
				//已发现过空指针异常
			}
			
			if (label == null) {
				label = getName();
			}
			
		}
		return label;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isSelected() {
		return this.selected;
	}
}
