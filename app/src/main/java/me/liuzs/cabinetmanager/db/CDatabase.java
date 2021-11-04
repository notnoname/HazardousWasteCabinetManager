package me.liuzs.cabinetmanager.db;

import static me.liuzs.cabinetmanager.db.CDatabase.Table.Alert;
import static me.liuzs.cabinetmanager.db.CDatabase.Table.HardwareValue;
import static me.liuzs.cabinetmanager.db.CDatabase.Table.Log;
import static me.liuzs.cabinetmanager.db.CDatabase.Table.Offline;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import me.liuzs.cabinetmanager.model.AlertLog;
import me.liuzs.cabinetmanager.model.DepositRecord;
import me.liuzs.cabinetmanager.model.HardwareValue;
import me.liuzs.cabinetmanager.model.OptLog;
import me.liuzs.cabinetmanager.model.modbus.EnvironmentStatus;

public class CDatabase {
    public static final String TAG = "CabinetDatabase";
    public static final String LOG_TABLE_NAME = "opt";
    public static final String HWV_TABLE_NAME = "hwv";
    public static final String OFFLINE_TABLE_NAME = "offline";
    public static final String ALERT_TABLE_NAME = "alert";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_VALUE = "value";
    public static final String COLUMN_IS_SENT = "sent";

    private final static CDatabase INSTANCE = new CDatabase();

    private final CabinetSQLiteHelper mHelper;

    private final Gson mGson = new Gson();

    private CDatabase() {
        mHelper = new CabinetSQLiteHelper();
    }

    public static CDatabase getInstance() {
        return INSTANCE;
    }

    public synchronized List<DepositRecord> getDepositRecordList(Filter filter) {
        LinkedHashMap<Long, String> strList = getDataList(Offline, filter);
        List<DepositRecord> result = new LinkedList<>();
        for (Map.Entry<Long, String> entry : strList.entrySet()) {
            DepositRecord depositRecord = mGson.fromJson(entry.getValue(), DepositRecord.class);
            depositRecord.localId = entry.getKey();
            result.add(depositRecord);
        }
        return result;
    }

    public synchronized List<OptLog> getOptLogList(Filter filter) {
        LinkedHashMap<Long, String> strList = getDataList(Log, filter);
        List<OptLog> result = new LinkedList<>();
        for (Map.Entry<Long, String> entry : strList.entrySet()) {
            OptLog optLog = mGson.fromJson(entry.getValue(), OptLog.class);
            optLog.id = entry.getKey();
            result.add(optLog);
        }
        return result;
    }

    public synchronized List<HardwareValue> getHardwareValueList(Filter filter) {
        LinkedHashMap<Long, String> strList = getDataList(HardwareValue, filter);
        List<HardwareValue> result = new LinkedList<>();
        for (Map.Entry<Long, String> entry : strList.entrySet()) {
            HardwareValue env = mGson.fromJson(entry.getValue(), HardwareValue.class);
            env.id = entry.getKey();
            result.add(env);
        }
        return result;
    }

    public synchronized List<AlertLog> getAlertLogList(Filter filter) {
        LinkedHashMap<Long, String> strList = getDataList(Alert, filter);
        List<AlertLog> result = new LinkedList<>();
        for (Map.Entry<Long, String> entry : strList.entrySet()) {
            AlertLog env = mGson.fromJson(entry.getValue(), AlertLog.class);
            env.id = entry.getKey();
            result.add(env);
        }
        return result;
    }

    public synchronized boolean addHardwareValue(@NotNull HardwareValue hardwareValue) {
        return addData(HardwareValue, mGson.toJson(hardwareValue)) != -1;
    }

    public synchronized boolean addAlertLog(@NotNull AlertLog alertLog) {
        return addData(Alert, mGson.toJson(alertLog)) != -1;
    }

    public synchronized boolean addOptLog(@NotNull OptLog optLog) {
        return addData(Log, mGson.toJson(optLog)) != -1;
    }

    public synchronized boolean addOfflineDepositRecord(@NotNull DepositRecord depositRecord) {
        return addData(Offline, mGson.toJson(depositRecord)) != -1;
    }

    private long addData(Table table, String data) {
        try (SQLiteDatabase database = mHelper.getReadableDatabase()) {
            String tableName = null;
            switch (table) {
                case Log:
                    tableName = LOG_TABLE_NAME;
                    break;
                case HardwareValue:
                    tableName = HWV_TABLE_NAME;
                    break;
                case Offline:
                    tableName = OFFLINE_TABLE_NAME;
                    break;
                case Alert:
                    tableName = ALERT_TABLE_NAME;
                    break;
            }
            ContentValues values = new ContentValues();
            values.put(COLUMN_VALUE, data);
            values.put(COLUMN_IS_SENT, "0");
            return database.insert(tableName, null, values);

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private LinkedHashMap<Long, String> getDataList(Table table, Filter filter) {
        LinkedHashMap<Long, String> result = new LinkedHashMap<>();
        try (SQLiteDatabase database = mHelper.getReadableDatabase()) {
            String[] columns = new String[]{COLUMN_ID, COLUMN_VALUE};
            String selection = null;
            String[] selectArgs = null;
            switch (filter) {
                case All:
                    selection = COLUMN_IS_SENT + " != ? ";
                    selectArgs = new String[]{"2"};
                    break;
                case NoUpload:
                    selection = COLUMN_IS_SENT + " = ? ";
                    selectArgs = new String[]{"0"};
                    break;
                case Uploaded:
                    selection = COLUMN_IS_SENT + " == ? ";
                    selectArgs = new String[]{"1"};
                    break;
            }
            String tableName = null;
            switch (table) {
                case Log:
                    tableName = LOG_TABLE_NAME;
                    break;
                case HardwareValue:
                    tableName = HWV_TABLE_NAME;
                    break;
                case Offline:
                    tableName = OFFLINE_TABLE_NAME;
                    break;
                case Alert:
                    tableName = ALERT_TABLE_NAME;
                    break;
            }
            try (Cursor cursor = database.query(tableName, columns, selection,
                    selectArgs, null, null, COLUMN_ID)) {
                while (cursor != null && cursor.moveToNext()) {
                    result.put(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)), cursor.getString(cursor.getColumnIndex(COLUMN_VALUE)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public enum Filter {All, NoUpload, Uploaded}

    public enum Table {Log, HardwareValue, Offline, Alert}

}