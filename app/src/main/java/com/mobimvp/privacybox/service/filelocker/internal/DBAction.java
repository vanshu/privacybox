package com.mobimvp.privacybox.service.filelocker.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.mobimvp.privacybox.Constants;
import com.mobimvp.privacybox.PBApplication;

public class DBAction {

	static {
		initDatabases();
	}

	private static String currentSdcard = Environment
			.getExternalStorageDirectory().getPath();

	public static String getSdcardRootPath() {
		return currentSdcard;  // /sdcard/
	}
	
	public static String getSdcardDBPath(){
		currentSdcard = Environment.getExternalStorageDirectory().toString();
		File dbPath = new File(currentSdcard + Constants.FILE_PATH);
		return dbPath.getAbsolutePath();
	}

	public static void initDatabases() {
		currentSdcard = Environment.getExternalStorageDirectory().toString();
		File dbPath = new File(currentSdcard + Constants.FILE_PATH);
		if (!dbPath.exists()) {
			dbPath.mkdirs();
		}
	}

	private static String getTableName(int type) {
		if (type == Constants.TYPE_VIDEO) {
			return EncryptItem.Table_Name_Video;
		}
		if (type == Constants.TYPE_PHOTO) {
			return EncryptItem.Table_Name_Photo;
		}
		return EncryptItem.Table_Name_File;
	}

	/**
	 * 取保存的文件集合的大小
	 * 
	 * @param type
	 * @return
	 */
	public static long[] getTableInfo(int type) {
		long[] ret = new long[2];
		String sql = String.format("select count(*),sum(%s) from %s",
				EncryptItem.Column_Size, getTableName(type));
		Cursor cursor = null;
		DBHelper helper = null;
		try {
			helper = new DBHelper(PBApplication.getApplication(), currentSdcard);
			SQLiteDatabase mDatabase = helper.getWritableDatabase();
			cursor = mDatabase.rawQuery(sql, null);
			if (cursor.moveToFirst()) {
				ret[0] = cursor.getLong(0);
				ret[1] = cursor.getLong(1);
			}
		} catch (Throwable e) {
			ret[0] = ret[1] = 0;
		} finally {
			if (cursor != null)
				cursor.close();
			if (helper != null) {
				helper.close();
			}
		}
		return ret;
	}

	public static long[] getAllTableInfo() {
		long[] ret1 = getTableInfo(Constants.TYPE_FILE);
		long[] ret2 = getTableInfo(Constants.TYPE_PHOTO);
		long[] ret3 = getTableInfo(Constants.TYPE_VIDEO);
		ret3[0] += ret1[0] + ret2[0];
		ret3[1] += ret1[1] + ret2[1];
		return ret3;
	}

	public static List<EncryptItem> selectAll(int type) {
		String sql = String.format("select * from %s ", getTableName(type));
		List<EncryptItem> list = new ArrayList<EncryptItem>();
		Cursor cursor = null;
		DBHelper helper = null;
		Log.w("DBAction", "selectAll:" + currentSdcard);
		try {
			helper = new DBHelper(PBApplication.getApplication(), currentSdcard);
			SQLiteDatabase mDatabase = helper.getWritableDatabase();
			cursor = mDatabase.rawQuery(sql, null);
			while (cursor.moveToNext()) {
				list.add(new EncryptItem(cursor, type, currentSdcard));
			}
		} catch (Exception e) {
		} finally {
			if (cursor != null)
				cursor.close();
			if (helper != null) {
				helper.close();
			}
		}
		return list;
	}

	public static List<EncryptItem> getTempList() {
		List<Integer> types = new ArrayList<Integer>();
		types.add(Constants.TYPE_FILE);
		types.add(Constants.TYPE_PHOTO);
		types.add(Constants.TYPE_VIDEO);
		List<EncryptItem> list = new ArrayList<EncryptItem>();
		Cursor cursor = null;
		DBHelper helper = null;
		helper = new DBHelper(PBApplication.getApplication(), currentSdcard);
		SQLiteDatabase mDatabase = helper.getWritableDatabase();
		for (Integer type : types) {
			try {
				String sql = String.format("select * from %s where %s = ?",
						getTableName(type), EncryptItem.Column_Temp);
				cursor = mDatabase.rawQuery(sql, new String[] { "" + 1 });
				while (cursor.moveToNext()) {
					list.add(new EncryptItem(cursor, type, getSdcardRootPath()));
				}
			} catch (Exception e) {
			}
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
		if (helper != null)
			helper.close();
		return list;
	}

	public static boolean insert(EncryptItem fi) {
		DBHelper helper = null;
		try {
			helper = new DBHelper(PBApplication.getApplication(), currentSdcard);
			SQLiteDatabase mDatabase = helper.getWritableDatabase();
			long rowid = mDatabase.replace(fi.getTableName(), null,
					fi.toContentValues());
			if (rowid >= 0) {
				fi.setId((int) rowid);
				// Log.w("", "insert ok: rowid= " + rowid + "  --  id= " +
				// fi.getId());
				return true;
			}
		} catch (Exception e) {
		} finally {
			if (helper != null) {
				helper.close();
			}
		}
		return false;
	}

	public static boolean delete(EncryptItem fi) {
		DBHelper helper = null;
		try {
			helper = new DBHelper(PBApplication.getApplication(), currentSdcard);
			SQLiteDatabase mDatabase = helper.getWritableDatabase();
			String sql = String.format("delete from %s where %s = ?",
					fi.getTableName(), EncryptItem.Column_Id);
			mDatabase.execSQL(sql, new String[] { "" + fi.getId() });
		} catch (Exception e) {
			return false;
		} finally {
			if (helper != null)
				helper.close();
		}
		return true;
	}

	public String getGroupname(EncryptItem fi) {
		String name = null;
		Cursor cursor = null;
		DBHelper helper = null;
		try {
			helper = new DBHelper(PBApplication.getApplication(), currentSdcard);
			SQLiteDatabase mDatabase = helper.getWritableDatabase();
			String sql = String.format("select %s from %s where %s = ?",
					EncryptItem.Column_Name, EncryptItem.Table_Name_Group,
					EncryptItem.Column_Groupid);
			cursor = mDatabase.rawQuery(sql,
					new String[] { "" + fi.getGroupId() });
			if (cursor.moveToFirst()) {
				name = cursor.getString(0);
			}
		} catch (Exception e) {
		} finally {
			if (cursor != null)
				cursor.close();
			if (helper != null)
				helper.close();
		}
		return name;
	}

	public static void updateTempMode(EncryptItem fi, boolean flag) {
		fi.setTemp(flag);
		DBHelper helper = null;
		try {
			helper = new DBHelper(PBApplication.getApplication(), currentSdcard);
			SQLiteDatabase mDatabase = helper.getWritableDatabase();
			mDatabase.update(fi.getTableName(), fi.toContentValues(),
					EncryptItem.SELECT_BY_ID, new String[] { "" + fi.getId() });
		} catch (Exception e) {
		} finally {
			if (helper != null)
				helper.close();
		}
	}

	public static void updatePassword(PasswordItem pi) {
		DBHelper helper = null;
		try {
			helper = new DBHelper(PBApplication.getApplication(), currentSdcard);
			SQLiteDatabase mDatabase = helper.getWritableDatabase();
			mDatabase.delete(PasswordItem.Password_Table, null, null);
			mDatabase.execSQL(PasswordItem.CREATE_PASSWORD_TABLE_SQL);
			mDatabase.insert(PasswordItem.Password_Table, null,
					pi.toContentValues());
		} catch (Exception e) {
		} finally {
			if (helper != null)
				helper.close();
		}
	}

	public static void clearPassword() {
		DBHelper helper = null;
		try {
			helper = new DBHelper(PBApplication.getApplication(), currentSdcard);
			SQLiteDatabase mDatabase = helper.getWritableDatabase();
			mDatabase.delete(PasswordItem.Password_Table, null, null);
		} catch (Exception e) {
		} finally {
			if (helper != null)
				helper.close();
		}
	}

	public static void clearAllData() {
		DBHelper helper = null;
		try {
			helper = new DBHelper(PBApplication.getApplication(), currentSdcard);
			SQLiteDatabase mDatabase = helper.getWritableDatabase();
			mDatabase.delete(EncryptItem.Table_Name_Group, null, null);
			mDatabase.delete(EncryptItem.Table_Name_File, null, null);
			mDatabase.delete(EncryptItem.Table_Name_Photo, null, null);
			mDatabase.delete(EncryptItem.Table_Name_Video, null, null);
			mDatabase.delete(PasswordItem.Password_Table, null, null);
		} catch (Exception e) {
		} finally {
			if (helper != null)
				helper.close();
		}
	}

	public static PasswordItem queryPassword() {
		Cursor cursor = null;
		PasswordItem pi = null;
		DBHelper helper = null;
		try {
			helper = new DBHelper(PBApplication.getApplication(), currentSdcard);
			SQLiteDatabase mDatabase = helper.getWritableDatabase();
			mDatabase.execSQL(PasswordItem.CREATE_PASSWORD_TABLE_SQL);
			cursor = mDatabase.query(PasswordItem.Password_Table, null, null,
					null, null, null, null);
			if (cursor.moveToLast()) {
				pi = new PasswordItem(cursor);
			}
		} catch (Exception e) {
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
			if (helper != null)
				helper.close();
		}
		return pi;
	}
}
