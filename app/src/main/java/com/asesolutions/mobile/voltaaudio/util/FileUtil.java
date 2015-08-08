package com.asesolutions.mobile.voltaaudio.util;

import android.os.Environment;

import com.asesolutions.mobile.voltaaudio.MainApplication;

import java.io.File;
import java.io.FileNotFoundException;

public class FileUtil {
    public static String getStorageDir() throws FileNotFoundException {
        if (!isExternalStorageWritable()) {
            throw new FileNotFoundException("External storage not mounted");
        }

        File file = MainApplication.getContext().getExternalFilesDir(null);

        if (file == null) {
            throw new FileNotFoundException();
        }

        if (!file.exists()) {
        }

        return "";
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}
