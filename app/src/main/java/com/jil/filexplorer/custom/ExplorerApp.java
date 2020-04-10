package com.jil.filexplorer.custom;

import android.app.Application;

import android.content.Context;
import android.os.Environment;
import com.jil.filexplorer.presenter.FragmentPresenter;

import java.io.File;

import static com.jil.filexplorer.api.SettingParam.readAllSharedPreferences;
import static com.jil.filexplorer.utils.NotificationUtils.registerNotifty;

public class ExplorerApp extends Application {
    public static Context ApplicationContext;
    public static FragmentPresenter fragmentPresenter;
    public static final String RECYCLE_PATH= Environment.getExternalStorageDirectory() + File.separator + "RecycleBin";

    @Override
    public void onCreate() {
        super.onCreate();
        ApplicationContext=getApplicationContext();
        readAllSharedPreferences(ApplicationContext);
        registerNotifty(ApplicationContext);//注册通知渠道

    }

    public static void setApplicationContext(Context applicationContext) {
        ApplicationContext = applicationContext;
    }
}
