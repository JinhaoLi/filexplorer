package com.jil.filexplorer.custom;

import android.app.Activity;
import android.app.Application;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.jil.filexplorer.presenter.FragmentPresenter;

import java.io.File;
import java.util.ArrayList;

import static com.jil.filexplorer.api.SettingParam.readAllSharedPreferences;
import static com.jil.filexplorer.utils.NotificationUtils.registerNotifty;

public class ExplorerApp extends Application {
    public static Context ApplicationContext;
    public static FragmentPresenter fragmentPresenter;
    public static final String RECYCLE_PATH= Environment.getExternalStorageDirectory() + File.separator + "RecycleBin";
    private static ArrayList<Activity> activities =new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        ApplicationContext=getApplicationContext();
        readAllSharedPreferences(ApplicationContext);
        registerNotifty(ApplicationContext);//注册通知渠道
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                activities.add(activity);
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {

            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {

            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {

            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                activities.remove(activity);
            }
        });

    }

    public static void exitAllActivity(){
        for (Activity a:activities){
            a.finish();
        }
    }

    public static void setApplicationContext(Context applicationContext) {
        ApplicationContext = applicationContext;
    }
}
