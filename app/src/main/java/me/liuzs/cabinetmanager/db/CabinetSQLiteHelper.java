package me.liuzs.cabinetmanager.db;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import me.liuzs.cabinetmanager.BuildConfig;
import me.liuzs.cabinetmanager.CabinetApplication;
import me.liuzs.cabinetmanager.Config;
import me.liuzs.cabinetmanager.util.StorageUtility;

public class CabinetSQLiteHelper extends SQLiteOpenHelper {

    private static CabinetSQLiteHelper INSTANCE = new CabinetSQLiteHelper();

    private CabinetSQLiteHelper() {
        super(CabinetApplication.getInstance(), BuildConfig.DEBUG ? StorageUtility.getApplicationExternalWorkDirPath() + Config.DATABASE_NAME : Config.DATABASE_NAME, null, Config.DATABASE_VERSION);
        setWriteAheadLoggingEnabled(false);
    }

    public static CabinetSQLiteHelper getInstance() {
        return INSTANCE;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table if not exists " + CabinetDatabase.TVOC_VALUE_TABLE_NAME + "("
                + CabinetDatabase.COLUMN_ID + " integer PRIMARY KEY AUTOINCREMENT,"
                + CabinetDatabase.COLUMN_VALUE + " integer,"
                + CabinetDatabase.COLUMN_RECORD_TYPE + " integer,"
                + CabinetDatabase.COLUMN_RECORD_TIME + " long,"
                + CabinetDatabase.COLUMN_CREATE_TIME + " long,"
                + CabinetDatabase.COLUMN_IS_SENT + " integer,"
                + "UNIQUE (" + CabinetDatabase.COLUMN_CREATE_TIME + "))");
        sqLiteDatabase.execSQL("create table if not exists " + CabinetDatabase.USER_TABLE_NAME + "("
                + CabinetDatabase.COLUMN_ID + " integer PRIMARY KEY AUTOINCREMENT,"
                + CabinetDatabase.COLUMN_USER_ID + " CHAR(50),"
                + CabinetDatabase.COLUMN_USER_NAME + " CHAR(100),"
                + CabinetDatabase.COLUMN_USER_PASSWORD_MD5 + " CHAR(32),"
                + CabinetDatabase.COLUMN_CREATE_TIME + " long,"
                + CabinetDatabase.COLUMN_USER_STATE + " integer,"
                + "UNIQUE (" + CabinetDatabase.COLUMN_USER_ID + "))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
