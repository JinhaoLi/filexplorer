package com.jil.filexplorer.Api;

import android.app.Application;

import com.jil.filexplorer.utils.LogUtils;

public class ExplorerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        LogUtils.i("回收内存好时机(￣▽￣):","ExplorerApplication.onTrimMemory"+"("+level+")");
        if(level==20){
            //应用切到后台
        }

    }
}
