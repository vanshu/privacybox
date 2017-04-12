package com.mobimvp.privacybox.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.mobimvp.privacybox.Constants;
import com.mobimvp.privacybox.PBApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Iterator;

public class LockAppListUtil {
    private static LockAppListUtil lockAppListUtils;
    private HashSet<String> lockApp;
    private HashSet<String> temporaryUnlockApp;
    private SharedPreferences preference;

    private LockAppListUtil(Context context) {
        preference = PreferenceManager.getDefaultSharedPreferences(context);
        lockApp = getLockAppSet(preference.getString(Constants.PRIVACY_DB_LOCKPKGNAME, null));
        temporaryUnlockApp = new HashSet<String>();
    }

    public static LockAppListUtil getInstance() {
        if (lockAppListUtils == null) {
            lockAppListUtils = new LockAppListUtil(PBApplication.getApplication());
        }
        return lockAppListUtils;
    }

    private HashSet<String> getLockAppSet(String params) {
        HashSet<String> resultSet = new HashSet<String>();
        try {
            JSONObject object = new JSONObject(params);
            JSONArray process = object.getJSONArray("packagename");
            for (int i = 0; i < process.length(); i++) {
                String name = process.getString(i);
                resultSet.add(name);
            }
        } catch (Exception e) {
        }
        return resultSet;
    }

    private String getInsertJSONString(HashSet<String> paramSet) {
        try {
            JSONObject object = new JSONObject();
            JSONArray array = new JSONArray();
            for (Iterator<String> iterator = paramSet.iterator(); iterator.hasNext(); ) {
                String pkgName = iterator.next();
                array.put(pkgName);
            }
            object.put("packagename", array);
            return object.toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public int getLockAppCount() {
        return lockApp.size();
    }

    public boolean isInLockAppSet(String pkgname) {
        return lockApp.contains(pkgname);
    }

    public boolean isInTemporaryUnlockAppSet(String pkgname) {
        return temporaryUnlockApp.contains(pkgname);
    }

    public void clearTemporaryUnlockAppSet() {
        temporaryUnlockApp.clear();
    }

    public void addItemInTemporaryUnlockAppSet(String pkgname) {
        temporaryUnlockApp.add(pkgname);
    }

    public void lockPackage(String pkg, boolean lock) {
        if (lock) {
            lockApp.add(pkg);
        } else {
            lockApp.remove(pkg);
        }
    }

    public void InsertLockApp() {
        String jsonString = getInsertJSONString(lockApp);
        preference.edit().putString(Constants.PRIVACY_DB_LOCKPKGNAME, jsonString).commit();
    }

}
