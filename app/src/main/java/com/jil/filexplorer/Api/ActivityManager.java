package com.jil.filexplorer.Api;

import android.app.Activity;

import com.jil.filexplorer.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class ActivityManager {


    private static ActivityManager mActivityManager;
    /**
     * 存放Activity的map
     */
    private List<Activity> mActivities = new ArrayList<Activity>();

    //将构造方法私有化，所以不能通构造方法来初始化ActivityManager
    private ActivityManager() {
    }

    //采用单例模式初始化ActivityManager，使只初始化一次
    public static ActivityManager getInstance() {
        if (mActivityManager == null) {
            mActivityManager = new ActivityManager();
        }
        return mActivityManager;
    }

    //添加activity
    public void addActivity(Activity activity) {
        if (!mActivities.contains(activity)) {
            mActivities.add(activity);
        }
    }

    //关闭指定的Activity
    public void removeActivity(Activity activity) {
        if (activity != null) {
            mActivities.remove(activity);
            activity.finish();
            activity = null;
        }
    }

    public void removeActivity(Class<?> cls) {
        String name =cls.getSimpleName();
        for (Activity activity : mActivities) {
            LogUtils.e("未销毁activity", activity.getClass().getSimpleName());
            if (activity.getClass().getSimpleName().equals(name)) {
                removeActivity(activity);
                break;
            }
        }
    }

    public void reCreatActivity(Class<?> cls) {
        String name =cls.getSimpleName();
        for (Activity activity : mActivities) {
            LogUtils.e("未销毁activity", activity.getClass().getSimpleName());
            if (activity.getClass().getSimpleName().equals(name)) {
                reCreatActivity(activity);
                break;
            }
        }
    }

    public void reCreatActivity(Activity activity) {
        activity.recreate();
    }

    public boolean isLive(Class<?> cls){
        boolean islive=false;
        String name =cls.getSimpleName();
        for (Activity activity : mActivities) {
            LogUtils.e("未销毁activity", activity.getClass().getSimpleName());
            if (activity.getClass().getSimpleName().equals(name)) {
                islive=true;
            }
        }
        return islive;
    }

    //将activity全部关闭掉
    public void clearAll() {
        for (Activity activity : mActivities) {
            activity.finish();
        }
    }


    //将activity全部关闭掉,除掉MainAcitiy
    public void clearOther() {

        for (Activity activity : mActivities) {
            LogUtils.e("eerrree", activity.getClass().getSimpleName());

            if (activity.getClass().getSimpleName().equals("MainActivity")) {

                continue;
            }
            activity.finish();
        }
    }
}
