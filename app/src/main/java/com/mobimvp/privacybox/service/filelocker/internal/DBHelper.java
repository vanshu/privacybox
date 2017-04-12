package com.mobimvp.privacybox.service.filelocker.internal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mobimvp.privacybox.Constants;

public class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context, String mnt) {
        super(context, DBAction.getSdcardRootPath() + Constants.FILE_DB_PATH, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        try {
            database.execSQL(String.format(EncryptItem.CREATE_TABLE_SQL, EncryptItem.Table_Name_File));
            database.execSQL(String.format(EncryptItem.CREATE_TABLE_SQL, EncryptItem.Table_Name_Photo));
            database.execSQL(String.format(EncryptItem.CREATE_TABLE_SQL, EncryptItem.Table_Name_Video));
            database.execSQL(String.format(EncryptItem.CREATE_GROUP_TABLE_SQL, EncryptItem.Table_Name_Group));
            database.execSQL(PasswordItem.CREATE_PASSWORD_TABLE_SQL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onDestroy(SQLiteDatabase database) {
        try {
            database.execSQL(String.format(EncryptItem.DROP_TABLE_SQL, EncryptItem.Table_Name_File));
            database.execSQL(String.format(EncryptItem.DROP_TABLE_SQL, EncryptItem.Table_Name_Photo));
            database.execSQL(String.format(EncryptItem.DROP_TABLE_SQL, EncryptItem.Table_Name_Video));
            database.execSQL(String.format(EncryptItem.DROP_TABLE_SQL, EncryptItem.Table_Name_Group));
            database.execSQL(PasswordItem.DROP_TABLE_SQL);
        } catch (Exception e) {
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}
