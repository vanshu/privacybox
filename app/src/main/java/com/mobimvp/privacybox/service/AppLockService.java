package com.mobimvp.privacybox.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;

import com.mobimvp.privacybox.Constants;
import com.mobimvp.privacybox.monitor.MonitorFactory;
import com.mobimvp.privacybox.monitor.MonitorInterface.MonitorListener;
import com.mobimvp.privacybox.monitor.MonitorInterface.PackageMonitor;
import com.mobimvp.privacybox.service.LockScreenService.OnLockScreenFinish;
import com.mobimvp.privacybox.utility.LockAppListUtil;
import com.mobimvp.privacybox.utility.SystemInfo;

public class AppLockService extends Service {

	private final int NativeStatus_Effective = 0;
	private final int NativeStatus_Invalid = 1;
	private int NativeStatus = NativeStatus_Invalid;
	// MSG
	private static final int UIMSG_SHOW_LOCKSCREEN = 0;
	// 上锁状态
	public int lockAppStatus = 0;
	public static final int LOCK_APPTYPE_SELF = 0;
	public static final int LOCK_APPTYPE_OTHER = 1;

	private Handler UIHandler;
	private PackageMonitor amMonitor;
	private PackageMonitor nativeMonitor;
	private Context mContext;
	private LockAppListUtil lockAppListUtils;
	private SharedPreferences preference;
	private AppLockerPreferenceListener preferecelisten;
	private String lastTopPackage = "";
	private String lockedPackage;
	// 解锁之后是否要跳转到LockAppActivity页
	private boolean isShowAppList;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private OnLockScreenFinish screenFinish = new OnLockScreenFinish() {
		@Override
		public void onUnlockSuccess() {
			if (isShowAppList) {
				isShowAppList = false;
			}
			if (preference.getInt(Constants.PRIVACY_LOCKEDTYPE, Constants.PRIVACY_LOCKEDTYPE_DEFAULT) == Constants.PRIVACY_LOCKEDTYPE_SCREENOFF && lastTopPackage != null) {
				LockAppListUtil.getInstance().addItemInTemporaryUnlockAppSet(lastTopPackage);
			}
		}
		
		@Override
		public void onUnlockFailed(int reason) {
			ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
			am.killBackgroundProcesses(lockedPackage);
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addCategory(Intent.CATEGORY_HOME);
			startActivity(intent);
		}

		@Override
		public boolean onUnlockPassword(String password) {
			return false;
		}
	};
	@Override
	public void onCreate() {
		mContext = getApplication();
		lockAppListUtils = LockAppListUtil.getInstance();
		preference = PreferenceManager.getDefaultSharedPreferences(mContext);
		preferecelisten = new AppLockerPreferenceListener();
		amMonitor = MonitorFactory.createPackageMonitor(getApplicationContext(), MonitorFactory.MONITOR_AMSERVICE, new AMServiceListener());
		nativeMonitor = MonitorFactory.createPackageMonitor(getApplicationContext(), MonitorFactory.MONITOR_NATIVE, new NativeListener());
		UIHandler = new Handler(mContext.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case UIMSG_SHOW_LOCKSCREEN:
					String name = (String) msg.obj;
					if(name != null){
						if (name.equals(mContext.getPackageName())) {
							isShowAppList = true;
						} else {
							isShowAppList = false;
						}
					}
					lockedPackage = name;
					LockScreenService.getInstance().lockScreen(screenFinish);
					break;
				}
			}
		};

		if (preference.getBoolean(Constants.PRIVACY_LOCK_ENABLE, Constants.PRIVACY_LOCK_ENABLE_DEFAULT)) {
			amMonitor.start();
			nativeMonitor.start();
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		preference.registerOnSharedPreferenceChangeListener(preferecelisten);
		if (preference.getInt(Constants.PRIVACY_LOCKEDTYPE, Constants.PRIVACY_LOCKEDTYPE_DEFAULT) == Constants.PRIVACY_LOCKEDTYPE_SCREENOFF) {
			registerScreenOffReceiver();
		}
		registerScreenOffLockSelfReceiver();
		
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterScreenOffLockSelfReceiver();
		SystemInfo.setComponentEnable(this,true);
	}

	private class AMServiceListener implements MonitorListener {
		private int countChange = 1;
		public void onTopPackageChange(String packageName) {
			if (NativeStatus == NativeStatus_Invalid) {
				if (countChange > 0)
					countChange++;
				if (countChange > 5) {
					countChange = -1;
					nativeMonitor.stop();
				}
			} else if (NativeStatus == NativeStatus_Effective && amMonitor != null) {
				amMonitor.stop();
				return;
			}
			doLockLogic(packageName);
		}
	}

	private class NativeListener implements MonitorListener {
		public void onTopPackageChange(String packageName) {
			//4.1机器首次运行的时候一定会触发
			if(!packageName.equals(getPackageName())){
				NativeStatus = NativeStatus_Effective;
				doLockLogic(packageName);
			}
		}
	}

	private void doLockLogic(String name) {
		int lockedtype = preference.getInt(Constants.PRIVACY_LOCKEDTYPE, Constants.PRIVACY_LOCKEDTYPE_DEFAULT);
		if (!lockAppListUtils.isInTemporaryUnlockAppSet(name)) {
			if (lockAppListUtils.isInLockAppSet(name) && UIHandler != null && (!name.equals(lastTopPackage) || lockedtype == Constants.PRIVACY_LOCKEDTYPE_SCREENOFF)) {
				UIHandler.obtainMessage(UIMSG_SHOW_LOCKSCREEN, name).sendToTarget();
			}
		}

		if (!mContext.getPackageName().equals(name)) {
			lastTopPackage = name;
		}
	}
	

	private class AppLockerPreferenceListener implements SharedPreferences.OnSharedPreferenceChangeListener {
		
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if (Constants.PRIVACY_LOCK_ENABLE.equals(key)) {
				if (sharedPreferences.getBoolean(Constants.PRIVACY_LOCK_ENABLE, Constants.PRIVACY_LOCK_ENABLE_DEFAULT)) {
					if (sharedPreferences.getInt(Constants.PRIVACY_LOCKEDTYPE, Constants.PRIVACY_LOCKEDTYPE_DEFAULT) == Constants.PRIVACY_LOCKEDTYPE_SCREENOFF) {
						registerScreenOffReceiver();
					}
					if(amMonitor == null){
						amMonitor = MonitorFactory.createPackageMonitor(getApplicationContext(), MonitorFactory.MONITOR_AMSERVICE, new AMServiceListener());
					}
					amMonitor.start();
					if(nativeMonitor == null){
						nativeMonitor = MonitorFactory.createPackageMonitor(getApplicationContext(), MonitorFactory.MONITOR_NATIVE, new NativeListener());
					}
					nativeMonitor.start();
				} else {
					unregisterScreenOffReceiver();
					if(amMonitor != null){
						amMonitor.stop();
						amMonitor = null;
					}
					if(nativeMonitor != null){
						nativeMonitor.stop();
						nativeMonitor = null;
					}
				}
			} else if (Constants.PRIVACY_LOCKEDTYPE.equals(key)) {
				if (sharedPreferences.getInt(Constants.PRIVACY_LOCKEDTYPE, Constants.PRIVACY_LOCKEDTYPE_DEFAULT) == Constants.PRIVACY_LOCKEDTYPE_EXITAPP) {
					unregisterScreenOffReceiver();
				} else {
					registerScreenOffReceiver();
				}
			}
		}

	}

	private boolean isRegisterScreenReceiver = false;
	private BroadcastReceiver screenOffReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
				LockAppListUtil.getInstance().clearTemporaryUnlockAppSet();
				ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
				String name = am.getRunningTasks(1).get(0).topActivity.getPackageName();
				doLockLogic(name);
			}
		}
	};

	private void unregisterScreenOffReceiver() {
		if (isRegisterScreenReceiver) {
			mContext.unregisterReceiver(screenOffReceiver);
			isRegisterScreenReceiver = false;
			LockAppListUtil.getInstance().clearTemporaryUnlockAppSet();
		}
	}

	private void registerScreenOffReceiver() {
		if (isRegisterScreenReceiver == false) {
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Intent.ACTION_SCREEN_ON);
			isRegisterScreenReceiver = true;
			mContext.registerReceiver(screenOffReceiver, intentFilter);
		}
	}
	
	private boolean isRegisterScreenLockSelfReceiver = false;
	private BroadcastReceiver screenOffReceiverLockSelf = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
				ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
				String name = am.getRunningTasks(1).get(0).topActivity.getPackageName();
				if(getPackageName().equals(name)){
					UIHandler.obtainMessage(UIMSG_SHOW_LOCKSCREEN, name).sendToTarget();
				}
			}
		}
	};
	private void unregisterScreenOffLockSelfReceiver(){
		if(isRegisterScreenLockSelfReceiver){
			mContext.unregisterReceiver(screenOffReceiverLockSelf);
			isRegisterScreenLockSelfReceiver = false;
		}
	}
	private void registerScreenOffLockSelfReceiver(){
		if(isRegisterScreenLockSelfReceiver == false){
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Intent.ACTION_SCREEN_ON);
			isRegisterScreenLockSelfReceiver = true;
			mContext.registerReceiver(screenOffReceiverLockSelf, intentFilter);
		}
	}

}
