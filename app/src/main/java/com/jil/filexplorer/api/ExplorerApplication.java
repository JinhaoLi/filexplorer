package com.jil.filexplorer.api;

import android.app.Application;

import android.content.Context;
import com.jil.filexplorer.utils.LogUtils;

public class ExplorerApplication extends Application {
    public static Context ApplicationContext;
    public static FragmentPresenter fragmentPresenter;
    public static FilePresenter filePresenter;

    @Override
    public void onCreate() {
        super.onCreate();
        ApplicationContext=getApplicationContext();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        LogUtils.i("回收内存好时机(￣▽￣):","ExplorerApplication.onTrimMemory"+"("+level+")");
        if(level==20){
            //应用切到后台
        }

    }

    public static void setApplicationContext(Context applicationContext) {
        ApplicationContext = applicationContext;
    }
}
