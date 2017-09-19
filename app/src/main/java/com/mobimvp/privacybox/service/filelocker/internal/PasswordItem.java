package com.mobimvp.privacybox.service.filelocker.internal;

import android.content.ContentValues;
import android.database.Cursor;

public class PasswordItem {

	public static final String Password_Table = "PSWD";
	public static final String Column_Key = "key";
	public static final String Column_Level = "level";
	
	private int level;
	private byte[] seckey;
	
	public static final String CREATE_PASSWORD_TABLE_SQL = 
			"CREATE TABLE IF NOT EXISTS PSWD (" 
			+ Column_Key + " BLOB PRIMARY KEY, "
			+ Column_Level + " int)";
	public static final String DROP_TABLE_SQL = "DROP TABLE IF EXISTS `PSWD`";
	
	public ContentValues toContentValues(){
		ContentValues values = new ContentValues();
		values.put(Column_Key, seckey);
		values.put(Column_Level, level);
		return values;
	}
	
	public PasswordItem(Cursor cursor){
		level = cursor.getInt(cursor.getColumnIndex(Column_Level));
		seckey = cursor.getBlob(cursor.getColumnIndex(Column_Key));
	}
	
	public PasswordItem(byte[] key, int level){
		this.level = level;
		this.seckey = key;
	}
	
	public int getLevel(){
		return level;
	}
	public byte[] getSeckey(){
		return seckey;
	}
}
