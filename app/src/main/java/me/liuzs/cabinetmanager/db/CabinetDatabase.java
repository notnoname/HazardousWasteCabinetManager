package me.liuzs.cabinetmanager.db;

import static me.liuzs.cabinetmanager.db.CabinetDatabase.Table.Alert;
import static me.liuzs.cabinetmanager.db.CabinetDatabase.Table.HardwareValue;
import static me.liuzs.cabinetmanager.db.CabinetDatabase.Table.Log;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import me.liuzs.cabinetmanager.CabinetCore;
import me.liuzs.cabinetmanager.model.AlertLog;
import me.liuzs.cabinetmanager.model.DepositRecord;
import me.liuzs.cabinetmanager.model.HardwareValue;
import me.liuzs.cabinetmanager.model.OptLog;

@SuppressWarnings("unused")
public class CabinetDatabase {
    public static final String TAG = "CabinetDatabase";
    public static final String LOG_TABLE_NAME = "opt";
    public static final String HWV_TABLE_NAME = "hwv";
    public static final String DEPOSIT_TABLE_NAME = "deposit";
    public static final String ALERT_TABLE_NAME = "alert";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CONTAINER_NO = "no";
    public static final String COLUMN_VALUE = "value";
    public static final String COLUMN_IS_SENT = "sent";

    private final static CabinetDatabase INSTANCE = new CabinetDatabase();

    private final CabinetSQLiteHelper mDefaultHelper;

    private final Gson mGson = new Gson();

    private CabinetDatabase() {
        mDefaultHelper = new CabinetSQLiteHelper();
    }

    public static CabinetDatabase getInstance() {
        return INSTANCE;
    }

    public void close() {
        try {
            mDefaultHelper.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized List<OptLog> getOptLogList(Filter filter, boolean orderByDESC, int pageSize, int offSet) {
        LinkedHashMap<Long, String> strList = getDataList(Log, filter, orderByDESC, pageSize, offSet);
        List<OptLog> result = new LinkedList<>();
        for (Map.Entry<Long, String> entry : strList.entrySet()) {
            OptLog optLog = mGson.fromJson(entry.getValue(), OptLog.class);
            optLog.id = entry.getKey();
            result.add(optLog);
        }
        return result;
    }

    public synchronized List<HardwareValue> getHardwareValueList(Filter filter, boolean orderByDESC, int pageSize, int offSet) {
        LinkedHashMap<Long, String> strList = getDataList(HardwareValue, filter, orderByDESC, pageSize, offSet);
        List<HardwareValue> result = new LinkedList<>();
        for (Map.Entry<Long, String> entry : strList.entrySet()) {
            HardwareValue env = mGson.fromJson(entry.getValue(), HardwareValue.class);
            env.id = entry.getKey();
            result.add(env);
        }
        return result;
    }

    public synchronized List<DepositRecord> getDepositRecordList(Filter filter, @Nullable String containerNo, boolean orderByDESC, int pageSize, int offSet) {
        List<DepositRecord> result = new LinkedList<>();
        try (SQLiteDatabase database = mDefaultHelper.getReadableDatabase()) {
            String[] columns = new String[]{COLUMN_ID, COLUMN_VALUE, COLUMN_IS_SENT};
            String selection = null;
            String[] selectArgs = containerNo == null ? new String[1] : new String[2];
            if (containerNo != null) {
                selectArgs[1] = containerNo;
            }
            switch (filter) {
                case All:
                    selection = COLUMN_IS_SENT + " != ? ";
                    selectArgs[0] = "2";
                    break;
                case NoUpload:
                    selection = COLUMN_IS_SENT + " = ? ";
                    selectArgs[0] = "0";
                    break;
                case Uploaded:
                    selection = COLUMN_IS_SENT + " == ? ";
                    selectArgs[0] = "1";
                    break;
            }
            selection = containerNo == null ? selection : selection + " AND " + COLUMN_CONTAINER_NO + " = ? ";

            try (Cursor cursor = database.query(DEPOSIT_TABLE_NAME, columns, selection,
                    selectArgs, null, null, COLUMN_ID + (orderByDESC ? " DESC" : " ASC"), offSet + "," + pageSize)) {
                while (cursor != null && cursor.moveToNext()) {
                    DepositRecord depositRecord = mGson.fromJson(cursor.getString(cursor.getColumnIndex(COLUMN_VALUE)), DepositRecord.class);
                    depositRecord.localId = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
                    depositRecord.isSent = cursor.getInt(cursor.getColumnIndex(COLUMN_IS_SENT)) == 1;
                    result.add(depositRecord);
                }
                if (cursor != null) {
                    cursor.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public synchronized List<AlertLog> getAlertLogList(Filter filter, boolean orderByDESC, int pageSize, int offSet) {
        LinkedHashMap<Long, String> strList = getDataList(Alert, filter, orderByDESC, pageSize, offSet);
        List<AlertLog> result = new LinkedList<>();
        for (Map.Entry<Long, String> entry : strList.entrySet()) {
            AlertLog env = mGson.fromJson(entry.getValue(), AlertLog.class);
            env.id = entry.getKey();
            result.add(env);
        }
        return result;
    }

    public synchronized void addHardwareValue(@NotNull HardwareValue hardwareValue) {
        addData(HardwareValue, mGson.toJson(hardwareValue));
    }

    public synchronized void addAlertLog(@NotNull AlertLog alertLog) {
        addData(Alert, mGson.toJson(alertLog));
    }

    public synchronized void addOptLog(@NotNull OptLog optLog) {
        addData(Log, mGson.toJson(optLog));
    }

    public synchronized void addDepositRecord(@NotNull DepositRecord depositRecord) {
        try (SQLiteDatabase database = mDefaultHelper.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_VALUE, CabinetCore.GSON.toJson(depositRecord));
            values.put(COLUMN_CONTAINER_NO, depositRecord.storage_no);
            values.put(COLUMN_IS_SENT, depositRecord.isSent ? "1":"0");
            database.insert(DEPOSIT_TABLE_NAME, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void deleteDepositRecord(long id) {
        try (SQLiteDatabase database = mDefaultHelper.getWritableDatabase()) {
            database.delete(DEPOSIT_TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void setDataSent(Table table, List<String> ids) {
        try (SQLiteDatabase database = mDefaultHelper.getWritableDatabase()) {
            String tableName = null;
            switch (table) {
                case Log:
                    tableName = LOG_TABLE_NAME;
                    break;
                case HardwareValue:
                    tableName = HWV_TABLE_NAME;
                    break;
                case Alert:
                    tableName = ALERT_TABLE_NAME;
                    break;
            }
            ContentValues values = new ContentValues();
            values.put(COLUMN_IS_SENT, "1");
            for (String id : ids) {
                database.update(tableName, values, COLUMN_ID + " in (?)", new String[]{id});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long deleteData(Table table, long id) {
        try (SQLiteDatabase database = mDefaultHelper.getWritableDatabase()) {
            String tableName = null;
            switch (table) {
                case Log:
                    tableName = LOG_TABLE_NAME;
                    break;
                case HardwareValue:
                    tableName = HWV_TABLE_NAME;
                    break;
                case Alert:
                    tableName = ALERT_TABLE_NAME;
                    break;
            }
            return database.delete(tableName, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private long addData(Table table, String data) {
        try (SQLiteDatabase database = mDefaultHelper.getWritableDatabase()) {
            String tableName = null;
            switch (table) {
                case Log:
                    tableName = LOG_TABLE_NAME;
                    break;
                case HardwareValue:
                    tableName = HWV_TABLE_NAME;
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
            return 0;
        }
    }

    private LinkedHashMap<Long, String> getDataList(Table table, Filter filter, boolean orderByDESC, int pageSize, int offSet) {
        LinkedHashMap<Long, String> result = new LinkedHashMap<>();
        try (SQLiteDatabase database = mDefaultHelper.getReadableDatabase()) {
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
                case Alert:
                    tableName = ALERT_TABLE_NAME;
                    break;
            }
            try (Cursor cursor = database.query(tableName, columns, selection,
                    selectArgs, null, null, COLUMN_ID + (orderByDESC ? " DESC" : " ASC"), offSet + "," + pageSize)) {
                while (cursor != null && cursor.moveToNext()) {
                    result.put(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)), cursor.getString(cursor.getColumnIndex(COLUMN_VALUE)));
                }
                if (cursor != null) {
                    cursor.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public enum Filter {All, NoUpload, Uploaded}

    public enum Table {Log, HardwareValue, Alert}

}