package me.liuzs.cabinetmanager.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class StorageUtility {

    private static String BaseDir = null;
    private static final StorageUtility INSTANCE = new StorageUtility();
    private Context mContext;

    public static synchronized void init(Context context) {
        INSTANCE.mContext = context;
        BaseDir = context.getExternalFilesDir(null).getAbsolutePath() + File.separator;
        File file = new File(BaseDir);
        if (file.exists() && !file.isDirectory()) {
            file.delete();
        }
        if (!file.exists()) {
            file.mkdir();
        }
    }

    /**
     * 本项目外部存储空间路径。
     *
     * @return 外部存储空间路径。
     */
    public static String getExternalWorkDirPath() {
        return BaseDir;
    }

    /**
     * 外部存储空间是否可用
     *
     * @return true可用，false不可用
     */
    public static boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED)
                || state.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
    }

    /**
     * 外部存储空间是否可写
     *
     * @return true可写，false不可写
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * @param type for example Environment.DIRECTORY_PICTURES
     * @return 制定类型文件存放路径
     */
    public static File getApplicationFilesDirectory(String type) {
        return INSTANCE.mContext.getExternalFilesDir(type);
    }

    public static ArrayList<File> getFiles(File directory, boolean recursive) {
        ArrayList<File> fileList = new ArrayList<File>();
        if (directory != null && directory.exists() && directory.isDirectory()) {
            walkFiles(directory, recursive, null, null, fileList);
        }
        return fileList;
    }

    // pattern can be for example "(.+(\\.(?i)(jpg|jpeg))$)"
    public static ArrayList<File> getFiles(File directory, boolean recursive,
                                           Pattern fileNameFilter, Pattern directoryNameFilter) {
        ArrayList<File> fileList = new ArrayList<File>();
        if (directory != null && directory.exists() && directory.isDirectory()) {
            walkFiles(directory, recursive, fileNameFilter, directoryNameFilter, fileList);
        }
        return fileList;
    }

    private static void walkFiles(File directory, boolean recursive, Pattern fileNameFilter,
                                  Pattern directoryNameFilter, ArrayList<File> fileList) {
        File[] list = directory.listFiles();
        for (int i = 0; i < Objects.requireNonNull(list).length; i++) {
            File f = list[i];
            if (f.isDirectory()) {
                if (recursive) {
                    if (validateName(f.getName(), directoryNameFilter)) {
                        walkFiles(f, recursive, fileNameFilter, directoryNameFilter, fileList);
                    }
                }
            } else if (validateName(f.getName(), fileNameFilter)) {
                fileList.add(f);
            }
        }
    }

    private static boolean validateName(String name, Pattern pattern) {
        if (pattern == null)
            return true;
        else {
            Matcher matcher = pattern.matcher(name);
            return matcher.matches();
        }
    }

    public static HashSet<String> getExternalMounts() {
        final HashSet<String> externalMounts = new HashSet<>();
        String regex = "(?i).*vold.*(vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*";
        StringBuilder mountOutput = new StringBuilder();
        // run mount process
        try {
            final Process process = new ProcessBuilder().command("mount").redirectErrorStream(true)
                    .start();
            process.waitFor();
            final InputStream is = process.getInputStream();
            final byte[] buffer = new byte[1024];
            while (is.read(buffer) != -1) {
                mountOutput.append(new String(buffer));
            }
            is.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        // parse mount output
        final String[] lines = mountOutput.toString().split("\n");
        for (String line : lines) {
            if (!line.toLowerCase(Locale.US).contains("asec")) // skip lines
            {
                if (line.matches(regex)) {
                    String[] parts = line.split(" ");
                    for (String part : parts) {
                        if (part.startsWith("/")) // starts with slash
                            if (!part.toLowerCase(Locale.US).contains("vold")) // not
                                // contains
                                // "vold"
                                externalMounts.add(part);
                    }
                }
            }
        }
        return externalMounts;
    }
}
