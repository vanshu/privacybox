package com.mobimvp.privacybox.service.filelocker.internal;

import android.content.ContentValues;
import android.database.Cursor;

import com.mobimvp.privacybox.Constants;

public class EncryptItem {

	private int id;
	private String name;

	private String originPath;
	private String encryptFilePath;
	private String tempPath;
	private String encryptThumbPath;
	private String groupname;
	private String sdrootPath;
	private long date;
	private long size;
	private boolean fullEncrypt;
	private boolean tempDecipher = false;
	private int fileType = 1;
	private int groupid = -1;
	public Object tag;
	private boolean selected = false;

	public static final String Table_Name_Video = "video";
	public static final String Table_Name_Photo = "photo";
	public static final String Table_Name_File = "file";
	public static final String Table_Name_Group = "encgroup";

	public static final String Column_Id = "id";
	public static final String Column_Name = "name";
	public static final String Column_OriPath = "oriPath";
	public static final String Column_EncPath = "encpath";
	public static final String Column_Date = "date";
	public static final String Column_Size = "filesize";
	public static final String Column_Mode = "mode";
	public static final String Column_Thumb = "thumb";
	public static final String Column_Temp = "temp";
	public static final String Column_Groupid = "groupid";

	public static final String CREATE_GROUP_TABLE_SQL = "CREATE TABLE %s (" + Column_Id + " integer PRIMARY KEY autoincrement, " + Column_Name + " VARCHAR)";

	public static final String CREATE_TABLE_SQL = "CREATE TABLE %s (" + Column_Id + " integer PRIMARY KEY autoincrement, " + Column_Name + " VARCHAR, " + Column_Mode + " INT, " + Column_Temp + " INT, " + Column_Groupid + " INT, "
			+ Column_OriPath + " VARCHAR, " + Column_EncPath + " VARCHAR, " + Column_Thumb + " VARCHAR, " + Column_Date + " INT64, " + Column_Size + " INT64)";
	public static final String DROP_TABLE_SQL = "DROP TABLE IF EXISTS `%s`";
	public static final String SELECT_BY_ID = String.format("%1$s=?", Column_Id);

	public ContentValues toContentValues() {
		ContentValues values = new ContentValues();
		values.put(Column_Name, getName());
		values.put(Column_OriPath, getOriginPath().replaceFirst(sdrootPath, ""));
		values.put(Column_EncPath, (encryptFilePath == null) ? "" : encryptFilePath.replaceFirst(sdrootPath, ""));
		values.put(Column_Thumb, (encryptThumbPath == null) ? "" : encryptThumbPath.replaceFirst(sdrootPath, ""));
		values.put(Column_Date, getDate());
		values.put(Column_Size, getSize());
		values.put(Column_Mode, fullEncrypt ? 1 : 0);
		values.put(Column_Temp, tempDecipher ? 1 : 0);
		values.put(Column_Groupid, groupid);
		return values;
	}

	public EncryptItem(Cursor cursor, int type, String root) {
		setSdrootPath(root);
		setFileType(type);
		id = cursor.getInt(cursor.getColumnIndex(Column_Id));
		name = cursor.getString(cursor.getColumnIndex(Column_Name));
		originPath = sdrootPath + cursor.getString(cursor.getColumnIndex(Column_OriPath));
		encryptFilePath = sdrootPath + cursor.getString(cursor.getColumnIndex(Column_EncPath));
		encryptThumbPath = sdrootPath + cursor.getString(cursor.getColumnIndex(Column_Thumb));
		date = cursor.getLong(cursor.getColumnIndex(Column_Date));
		size = cursor.getLong(cursor.getColumnIndex(Column_Size));
		fullEncrypt = cursor.getInt(cursor.getColumnIndex(Column_Mode)) == 1 ? true : false;
		tempDecipher = cursor.getInt(cursor.getColumnIndex(Column_Temp)) == 1 ? true : false;
		groupid = cursor.getInt(cursor.getColumnIndex(Column_Groupid));
	}

	public EncryptItem(int type, String root) {
		setFileType(type);
		setSdrootPath(root);
	}

	public String getTableName() {
		if (fileType == Constants.TYPE_PHOTO)
			return Table_Name_Photo;
		if (fileType == Constants.TYPE_VIDEO)
			return Table_Name_Video;
		return Table_Name_File;
	}

	public String getGroupName() {
		if (groupname == null && groupid >= 0) {

		}
		return groupname;
	}

	public int getGroupId() {
		return groupid;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		if (name == null || name.equals("")) {
			name = originPath.substring(originPath.lastIndexOf("/") + 1);
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOriginPath() {
		return originPath;
	}

	public void setSdrootPath(String root){
		sdrootPath = root;
	}
	public void setOriginPath(String originalFilePath) {
		this.originPath = originalFilePath;
	}

	public String getFilePath() {
		return encryptFilePath;
	}

	public void setFilePath(String filePath) {
		this.encryptFilePath = filePath;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public int getFileType() {
		return fileType;
	}

	public void setFileType(int fileType) {
		this.fileType = fileType;
	}

	public boolean isFullEncrypt() {
		return fullEncrypt;
	}

	public void setFullEncrypt(boolean full) {
		this.fullEncrypt = full;
	}

	public String getThumbPath() {
		return encryptThumbPath;
	}

	public void setThumbPath(String smallPhotoPath) {
		this.encryptThumbPath = smallPhotoPath;
	}

	public String getTempPath() {
		try {
			if (tempPath == null) {
				String suffix = getOriginPath().substring(getOriginPath().lastIndexOf('.') + 1);
				String encName = getFilePath().substring(getFilePath().lastIndexOf('/') + 1);
				tempPath = String.format("%s/%s.%s", DBAction.getSdcardRootPath() + Constants.FILE_TEMPORARY_PATH, encName, suffix);
			}
		} catch (Exception e) {
		}
		return tempPath;
	}

	public boolean getTemp() {
		return tempDecipher;
	}

	public void setTemp(boolean flag) {
		tempDecipher = flag;
	}

	public void setSelected(boolean flag) {
		selected = flag;
	}

	public boolean getSelected() {
		return selected;
	}
}
