package me.liuzs.cabinetmanager.db;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import me.liuzs.cabinetmanager.CabinetApplication;
import me.liuzs.cabinetmanager.Config;

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
        sqLiteDatabase.execSQL(String.format("create table if not exists %s(%s integer PRIMARY KEY AUTOINCREMENT,%s TEXT,%s integer)", CDatabase.LOG_TABLE_NAME, CDatabase.COLUMN_ID, CDatabase.COLUMN_VALUE, CDatabase.COLUMN_IS_SENT));
        sqLiteDatabase.execSQL(String.format("create table if not exists %s(%s integer PRIMARY KEY AUTOINCREMENT,%s TEXT,%s integer)", CDatabase.HWV_TABLE_NAME, CDatabase.COLUMN_ID, CDatabase.COLUMN_VALUE, CDatabase.COLUMN_IS_SENT));
        sqLiteDatabase.execSQL(String.format("create table if not exists %s(%s integer PRIMARY KEY AUTOINCREMENT,%s TEXT,%s integer)", CDatabase.OFFLINE_TABLE_NAME, CDatabase.COLUMN_ID, CDatabase.COLUMN_VALUE, CDatabase.COLUMN_IS_SENT));
        sqLiteDatabase.execSQL(String.format("create table if not exists %s(%s integer PRIMARY KEY AUTOINCREMENT,%s TEXT,%s integer)", CDatabase.ALERT_TABLE_NAME, CDatabase.COLUMN_ID, CDatabase.COLUMN_VALUE, CDatabase.COLUMN_IS_SENT));

    }
}
