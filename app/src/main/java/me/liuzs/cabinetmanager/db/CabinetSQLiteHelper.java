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
        sqLiteDatabase.execSQL(String.format("create table if not exists %s(%s integer PRIMARY KEY AUTOINCREMENT,%s TEXT,%s integer)", CabinetDatabase.LOG_TABLE_NAME, CabinetDatabase.COLUMN_ID, CabinetDatabase.COLUMN_VALUE, CabinetDatabase.COLUMN_IS_SENT));
        sqLiteDatabase.execSQL(String.format("create table if not exists %s(%s integer PRIMARY KEY AUTOINCREMENT,%s TEXT,%s integer)", CabinetDatabase.HWV_TABLE_NAME, CabinetDatabase.COLUMN_ID, CabinetDatabase.COLUMN_VALUE, CabinetDatabase.COLUMN_IS_SENT));
        sqLiteDatabase.execSQL(String.format("create table if not exists %s(%s integer PRIMARY KEY AUTOINCREMENT,%s VARCHAR(20),%s TEXT,%s integer)", CabinetDatabase.DEPOSIT_TABLE_NAME, CabinetDatabase.COLUMN_ID, CabinetDatabase.COLUMN_CONTAINER_NO, CabinetDatabase.COLUMN_VALUE, CabinetDatabase.COLUMN_IS_SENT));
        sqLiteDatabase.execSQL(String.format("create table if not exists %s(%s integer PRIMARY KEY AUTOINCREMENT,%s TEXT,%s integer)", CabinetDatabase.ALERT_TABLE_NAME, CabinetDatabase.COLUMN_ID, CabinetDatabase.COLUMN_VALUE, CabinetDatabase.COLUMN_IS_SENT));

    }
}
