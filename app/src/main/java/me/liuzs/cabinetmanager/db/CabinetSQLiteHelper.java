package me.liuzs.cabinetmanager.db;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;

import me.liuzs.cabinetmanager.CabinetApplication;
import me.liuzs.cabinetmanager.Config;
import me.liuzs.cabinetmanager.util.StorageUtility;

public class CabinetSQLiteHelper extends SQLiteOpenHelper {

    public CabinetSQLiteHelper() {
        super(CabinetApplication.getInstance(), Config.DATABASE_NAME, null, Config.DATABASE_VERSION);
        setWriteAheadLoggingEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        createTable(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        createTable(sqLiteDatabase);
    }

    public void createTable(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(String.format("create table if not exists %s(%s integer PRIMARY KEY AUTOINCREMENT,%s TEXT,%s integer)", CabinetDatabase.LOG_TABLE_NAME, CabinetDatabase.COLUMN_ID, CabinetDatabase.COLUMN_VALUE, CabinetDatabase.COLUMN_IS_SENT));
        sqLiteDatabase.execSQL(String.format("create table if not exists %s(%s integer PRIMARY KEY AUTOINCREMENT,%s TEXT,%s integer)", CabinetDatabase.HWV_TABLE_NAME, CabinetDatabase.COLUMN_ID, CabinetDatabase.COLUMN_VALUE, CabinetDatabase.COLUMN_IS_SENT));
        sqLiteDatabase.execSQL(String.format("create table if not exists %s(%s integer PRIMARY KEY AUTOINCREMENT,%s VARCHAR(20),%s TEXT,%s integer)", CabinetDatabase.DEPOSIT_TABLE_NAME, CabinetDatabase.COLUMN_ID, CabinetDatabase.COLUMN_CONTAINER_NO, CabinetDatabase.COLUMN_VALUE, CabinetDatabase.COLUMN_IS_SENT));
        sqLiteDatabase.execSQL(String.format("create table if not exists %s(%s integer PRIMARY KEY AUTOINCREMENT,%s TEXT,%s integer)", CabinetDatabase.ALERT_TABLE_NAME, CabinetDatabase.COLUMN_ID, CabinetDatabase.COLUMN_VALUE, CabinetDatabase.COLUMN_IS_SENT));

    }

    //这个东西仅对当前SqliteConncetion有效，
    private boolean mainTmpDirSet = false;

    @Override
    public SQLiteDatabase getReadableDatabase() {
        if (!mainTmpDirSet) {
            String tempDir = StorageUtility.getExternalWorkDirPath() + "databasetemp/";
            File tempFile = new File(tempDir);
            boolean rs = tempFile.exists() || new File(tempDir).mkdir();
            Log.d("CabinetSQLiteHelper", rs + "");
            super.getReadableDatabase().execSQL("PRAGMA temp_store_directory = '" + tempDir + "'");
            mainTmpDirSet = true;
            return super.getReadableDatabase();
        }
        return super.getReadableDatabase();
    }
}
