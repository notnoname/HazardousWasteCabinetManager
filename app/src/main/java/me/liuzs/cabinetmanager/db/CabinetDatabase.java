package me.liuzs.cabinetmanager.db;

public class CabinetDatabase {
    public static final String TAG = "CabinetDatabase";
    public static final String LOG_TABLE_NAME = "log";
    public static final String TVOC_VALUE_TABLE_NAME = "tvocs";
    public static final String USER_TABLE_NAME = "cab_user";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_VALUE = "value";
    public static final String COLUMN_RECORD_TIME = "record_time";
    public static final String COLUMN_RECORD_TYPE = "record_type";
    public static final String COLUMN_CREATE_TIME = "create_time";
    public static final String COLUMN_IS_SENT = "is_send";
    public static final String COLUMN_USER_NAME = "user_name";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USER_PASSWORD_MD5 = "user_pass_md5";
    public static final String COLUMN_USER_STATE = "user_state";

    private final static CabinetDatabase INSTANCE = new CabinetDatabase();

    private CabinetDatabase() {
    }

    public static CabinetDatabase getInstance() {
        return INSTANCE;
    }

//    public synchronized List<User> getHrvVOs(String date) {
//        List<User> result = new ArrayList<>();
//        try (SQLiteDatabase database = mDatabaseOpenHelper.getReadableDatabase()) {
//            String[] columns = new String[]{COLUMN_VALUE, COLUMN_AVERAGE,
//                    COLUMN_STATE1, COLUMN_STATE2, COLUMN_STATE3, COLUMN_RECORD_TIME};
//            String selection = "date(" + COLUMN_RECORD_TIME + "/1000,'unixepoch','localtime') = ? ";
//            String[] selectArgs = new String[]{date};
//            String orderBy = COLUMN_RECORD_TIME;
//            try (Cursor cursor = database.query(HRV_TABLE_NAME, columns, selection,
//                    selectArgs, null, null, orderBy)) {
//                while (cursor != null && cursor.moveToNext()) {
//                    HrvVO hrv = new HrvVO();
//                    hrv.setValue(cursor.getInt(cursor.getColumnIndex(COLUMN_VALUE)));
//                    hrv.setAverage(cursor.getInt(cursor
//                            .getColumnIndex(COLUMN_AVERAGE)));
//                    hrv.setState1(cursor.getInt(cursor
//                            .getColumnIndex(COLUMN_STATE1)));
//                    hrv.setState2(cursor.getInt(cursor
//                            .getColumnIndex(COLUMN_STATE2)));
//                    hrv.setState3(cursor.getInt(cursor
//                            .getColumnIndex(COLUMN_STATE3)));
//                    hrv.setRecordTime(cursor.getLong(cursor
//                            .getColumnIndex(COLUMN_RECORD_TIME)));
//                    result.add(hrv);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return result;
//    }

}