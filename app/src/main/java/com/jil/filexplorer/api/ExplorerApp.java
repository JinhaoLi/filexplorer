package com.jil.filexplorer.api;

import android.app.Application;

import android.content.Context;
import com.jil.filexplorer.utils.LogUtils;

public class ExplorerApp extends Application {
    public static Context ApplicationContext;
    public static FragmentPresenter fragmentPresenter;

    @Override
    public void onCreate() {
        super.onCreate();
        ApplicationContext=getApplicationContext();
    }

    public static void setApplicationContext(Context applicationContext) {
        ApplicationContext = applicationContext;
    }
}
