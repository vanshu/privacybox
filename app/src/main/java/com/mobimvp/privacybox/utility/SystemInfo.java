package com.mobimvp.privacybox.utility;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;

public class SystemInfo {

	public static float dip2px(Context context, float dip) {
		Resources r = context.getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());
		return px;
	}

	public static float sp2px(Context context, float sp) {
		Resources r = context.getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, r.getDisplayMetrics());
		return px;
	}

	public static CharSequence getLabel(Context context, String packageName, String className, String defaultValue) {
		try {
			final PackageManager pm = context.getPackageManager();
			CharSequence label = null;
			ComponentName componentName = new ComponentName(packageName, className);
			label = pm.getActivityInfo(componentName, 0).loadLabel(pm);
			if (label == null) {
				label = packageName;
			}
			return label;
		} catch (Exception e) {
		}
		return defaultValue;
	}

	public static CharSequence getLabel(Context context, String packageName, String defaultValue) {
		try {
			APKFile apkfile = APKFile.CreateFromPackage(context, packageName);
			return apkfile.getLabel();
		} catch (Exception e) {
		}
		return defaultValue;
	}

	public static int getIntField(String className, String fieldName, int defaultValue) {
		try {
			Class<?> cls = Class.forName(className);
			Field field = cls.getDeclaredField(fieldName);
			field.setAccessible(true);
			Integer code = field.getInt(null);
			return code;
		} catch (Exception e) {
			return defaultValue;
		}
	}
	
	public static final int DEVICE_NO_NFC=0x000001;
	public static final int DEVICE_NFC_DISABLE=0x000002;
	public static final int DEVICE_NFC_ENABLE=0x000003;
	
	public static int getNFCStatus(Context context) {
		NfcManager manager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
		NfcAdapter adapter = manager.getDefaultAdapter();
		if (adapter == null) {
			return DEVICE_NO_NFC;
		} else {
			if(adapter.isEnabled()){
				return DEVICE_NFC_ENABLE;
			}else{
				return DEVICE_NFC_DISABLE;
			}
		}
	}
	
	public static void setComponentEnable(Context context,boolean flag){  
		ComponentName componentToDisable = new ComponentName(context.getPackageName(),context.getPackageName()+".ui.LauncherActivity");
		if(flag){
			context.getPackageManager().setComponentEnabledSetting(componentToDisable, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);
		}else{
			context.getPackageManager().setComponentEnabledSetting(componentToDisable, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
		}
	}
	
	
	
	public static int getComponentEnabledState(Context context){  
		ComponentName componentToDisable = new ComponentName(context.getPackageName(),context.getPackageName()+".ui.LauncherActivity");
		int flag=context.getPackageManager().getComponentEnabledSetting(componentToDisable);
		return flag;
	}
	
	public static void rmdir(String path){
		try {
			Runtime.getRuntime().exec("rm -r " + path);
		} catch (Exception e) {
		}
	}
	
	public static String [] [] listFiles(String path){
		File [] files=new File(path).listFiles();
		ArrayList<String> array1=new ArrayList<String>();
		ArrayList<String> array2=new ArrayList<String>();
		for(int i=0;i<files.length;i++){
			if(files[i].isDirectory()){
				array1.add(files[i].getAbsolutePath());
			}else if(files[i].isFile()){
				array2.add(files[i].getAbsolutePath());
			}
		}
		String [] result1=new String[array1.size()];
		String [] result2=new String[array2.size()];
		
		for(int i=0;i<result1.length;i++){
			result1[i]=array1.get(i);
		}
		for(int i=0;i<result2.length;i++){
			result2[i]= array2.get(i);
		}
		String [] [] result={result1,result2};
		return result;
	}
	
	public static void addView(LinearLayout contentView, View...childrenViews){
		for(int i=0;i<childrenViews.length;i++)
			contentView.addView(childrenViews[i]);
	}
}
