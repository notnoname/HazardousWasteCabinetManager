package me.liuzs.cabinetmanager;

import android.content.Context;
import android.content.SharedPreferences;

import com.arcsoft.face.enums.DetectFaceOrientPriority;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Liuzs
 */
public class Config {
    public static final String DEVICE_NAME_PRE = "Cabinet";
    public static final String DATABASE_NAME = "CabinetDatabase";
    public static final int DATABASE_VERSION = 1;
    public static final String EZ_APP_KEY = "15e1da4c183a470c89ebb68c3a26a349";

    public static final String SYSTEM_PREFERENCE_NAME = "SystemPreference";
    public static final String SYSPRE_Cabinet_User = "SYSPRE_Cabinet_User";
    public static final String SYSPRE_CABINET_INFO = "CABINET_INFO_FULL_VERSION_3";
    public static final String UN_SUBMIT_DEPOSIT_INFO = "UN_SUBMIT_DEPOSIT_INFO";
    public static final String UN_SUBMIT_USAGE_INFO = "UN_SUBMIT_USAGE_INFO";
    public static final String UN_SUBMIT_TAKE_OUT_INFO = "UN_SUBMIT_TAKE_OUT_INFO";
    public static final String PRINTER_BLUETOOTH_INFO = "PRINTER_BLUETOOTH_INFO";
    public static final String SYSPRE_CAMERA_VERIFY_CODE = "SYSPRE_CAMERA_VERIFY_CODE";
    public static final String MODBUS_ADDRESS = "MODBUS_ADDRESS";

    public static final String ARC_APP_ID = "5UQUZXBwq4RknCfrk4zs7nBew3BQrNh7ZQH1ALQybdNR";
    public static final String ARC_SDK_KEY = "AzFyDx3GmvuvVWDSGZW9ZCe8uerNp1zoVcYfj45eXcNd";

    //    public static final String SUB_BOARD_CONFIG = "SUB_BOARD_CONFIG";
    public static final String TDA09C485_CONFIG = "TDA09C485_CONFIG";
    public static final String SCALES_DEVICE = "SCALES_DEVICE";
    //    public static final int DEFAULT_SUB_BOARD_ADDRESS = 1;
    public static final int DEFAULT_TDA09C485_ADDRESS = 31;
    /**
     * IR预览数据相对于RGB预览数据的横向偏移量，注意：是预览数据，一般的摄像头的预览数据都是 width > height
     */
    public static final int HORIZONTAL_OFFSET = 0;
    /**
     * IR预览数据相对于RGB预览数据的纵向偏移量，注意：是预览数据，一般的摄像头的预览数据都是 width > height
     */
    public static final int VERTICAL_OFFSET = 0;
    public static final String ACTION_HARDWARE_VALUE_SEND = "ACTION_HARDWARE_VALUE_SEND";
    public static final String KEY_HARDWARE_VALUE = "KEY_HARDWARE_VALUE";
    private static final String[] ARC_LIBRARIES = new String[]{
            "libarcsoft_face_engine.so",
            "libarcsoft_face.so",
            "libarcsoft_image_util.so",
    };
    private static final String ARC_APP_NAME = "CabinetManager";
    private static final String TRACKED_FACE_COUNT = "trackedFaceCount";
    private static final String FT_ORIENT = "ftOrientPriority";
    private static final String MAC_PRIORITY = "macPriority";
    private static final String[] LIBRARIES = new String[]{
            // 人脸相关
            "libarcsoft_face_engine.so",
            "libarcsoft_face.so",
            // 图像库相关
            "libarcsoft_image_util.so",
    };
    public static String[] ScalesDeviceName = {"欧路达(100kg/10g)"};

    public static boolean setTrackedFaceCount(Context context, int trackedFaceCount) {
        if (context == null) {
            return false;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(ARC_APP_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.edit()
                .putInt(TRACKED_FACE_COUNT, trackedFaceCount)
                .commit();
    }

    public static int getTrackedFaceCount(Context context) {
        if (context == null) {
            return 0;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(ARC_APP_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(TRACKED_FACE_COUNT, 0);
    }

    public static boolean setFtOrient(Context context, DetectFaceOrientPriority ftOrient) {
        if (context == null) {
            return false;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(ARC_APP_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.edit()
                .putString(FT_ORIENT, ftOrient.name())
                .commit();
    }

    public static DetectFaceOrientPriority getFtOrient(Context context) {
        if (context == null) {
            return DetectFaceOrientPriority.ASF_OP_ALL_OUT;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(ARC_APP_NAME, Context.MODE_PRIVATE);
        return DetectFaceOrientPriority.valueOf(sharedPreferences.getString(FT_ORIENT, DetectFaceOrientPriority.ASF_OP_ALL_OUT.name()));
    }

    /**
     * 检查能否找到动态链接库，如果找不到，请修改工程配置
     *
     * @return 动态库是否存在
     */
    public static boolean isLibraryExists(Context context) {
        File dir = new File(context.getApplicationInfo().nativeLibraryDir);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return false;
        }
        List<String> libraryNameList = new ArrayList<>();
        for (File file : files) {
            libraryNameList.add(file.getName());
        }
        boolean exists = true;
        for (String library : LIBRARIES) {
            exists &= libraryNameList.contains(library);
        }
        return exists;
    }
}