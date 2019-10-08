package com.jil.filexplorer.utils;


import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public final class LogUtils {
    private static final boolean LOGV = true;
    private static final boolean LOGD = true;
    private static final boolean LOGI = true;
    private static final boolean LOGW = true;
    private static final boolean LOGE = true;
    private static final String LOG_FILE_PATH = Environment.getExternalStorageDirectory() + File.separator + "fileExplorer.log";

    public static void v(String tag, String mess) {
        if (LOGV) {
            Log.v(tag, mess);
        }
    }

    public static void d(String tag, String mess) {
        if (LOGD) {
            Log.d(tag, mess);
        }
    }

    public static void i(String tag, String mess) {
        if (LOGI) {
            Log.i(tag, mess);
        }
        String date = "[" + FileUtils.getFormatData(System.currentTimeMillis()) + "] ";
        writeInFile(date + tag + mess);
    }

    public static void e(String tag, String mess) {
        if (LOGE) {
            Log.e(tag, mess);
        }
        String date = "[" + FileUtils.getFormatData(System.currentTimeMillis()) + "] ";
        writeInFile(date + tag + mess);
    }

    public static void w(String tag, String mess) {
        if (LOGW) {
            Log.w(tag, mess);
        }
    }

    public static void eNoOut(String tag, String mess) {
        if (LOGE) {
            Log.e(tag, mess);
        }
    }

    private static void getLogFile() throws IOException {
        File f = new File(LOG_FILE_PATH);
        if (!f.exists()) {
            f.createNewFile();
            StringBuilder mobileInfo = new StringBuilder();
            mobileInfo.append("======Mobile phone info======").append("\n");
            mobileInfo.append("Mobile phone models：").append(Build.MODEL).append("\n");
            mobileInfo.append("The android version：").append(Build.VERSION.SDK_INT).append("\n");
            mobileInfo.append("======Mobile phone info======").append("\n");
            writeInFile(mobileInfo.toString());
        }
    }

    private static void writeInFile(String log) {
        try {
            getLogFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //创建源
        File logFile =new File(LOG_FILE_PATH);
        //选择流
        Writer writer = null;
        try {
            writer = new FileWriter(logFile, true);
            writer.write(log + "\n");
            writer.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            FileUtils.closeAnyThing(writer);
        }
    }
}
