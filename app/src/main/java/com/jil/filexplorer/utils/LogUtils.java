package com.jil.filexplorer.utils;



import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.logging.Logger;

public final class LogUtils {

	private static final boolean LOGV = true;
	private static final boolean LOGD = true;
	private static final boolean LOGI = true;
	private static final boolean LOGW = true;
	private static final boolean LOGE = true;
	 
	public static void v(String tag, String mess) {
	    if (LOGV) { Log.v(tag, mess); }
	}
	public static void d(String tag, String mess) {
	    if (LOGD) { Log.d(tag, mess); }
	}
	public static void i(String tag, String mess) {
	    if (LOGI) { Log.i(tag, mess); }
		writeInFile(tag+mess);
	}
	public static void w(String tag, String mess) {
	    if (LOGW) { Log.w(tag, mess); }

	}
	public static void e(String tag, String mess) {
	    if (LOGE) { Log.e(tag, mess); }
		writeInFile(tag+mess);
	}

	public static void writeInFile(String log){
		String date = "["+FileUtils.getFormatData(System.currentTimeMillis())+"]";
		//创建源
		File f = new File(Environment.getExternalStorageDirectory()+File.separator+"fileExplorer.log");
		//选择流
		Writer writer = null;
		if(!f.exists()){
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			writer =new FileWriter(f,true);
			writer.write(date+log);
			writer.append("\n");
			writer.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
	}


}
