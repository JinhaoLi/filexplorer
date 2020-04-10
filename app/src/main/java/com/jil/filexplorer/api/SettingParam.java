package com.jil.filexplorer.api;

import android.content.Context;
import android.content.SharedPreferences;

import com.jil.filexplorer.R;
import com.jil.filexplorer.utils.ConstantUtils;

/**
 *配置参数，默认正数为是，负数为否
 */
public class SettingParam {
    public static final String CONFIRM_REJECTION ="queryRequestPermission";

    /**
     * 使用SharedPreferences保存一些表单数据，设置数据
     * @param mContext
     * @param key
     * @param value
     */
    public static void saveString(Context mContext , String key, String value) {
        SharedPreferences sp = mContext.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * 使用SharedPreferences保存一些表单数据，设置数据
     * @param mContext
     * @param key
     * @param value
     */
    public static void saveInt(Context mContext , String key, int value) {
        SharedPreferences sp = mContext.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    /**
     * 使用SharedPreferences保存一些表单数据，设置数据
     * @param mContext
     * @param key
     * @param value
     */
    public static void saveBoolean(Context mContext , String key, boolean value) {
        SharedPreferences sp = mContext.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    /**
     * 读取
     * @param mContext
     */
    public static String getString(Context mContext,String key){
        SharedPreferences sp = mContext.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        return sp.getString(key,"null");
    }

    /**
     * 读取
     * @param mContext
     */
    public static int getInt(Context mContext,String key,int def){
        SharedPreferences sp = mContext.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        return sp.getInt(key,def);
    }

    /**
     * 读取
     * @param mContext
     */
    public static boolean getBoolean(Context mContext,String key,boolean def){
        SharedPreferences sp = mContext.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        return sp.getBoolean(key,def);
    }

    /**
     * 读取
     * @param mContext
     */
    public static void readAllSharedPreferences(Context mContext){
        SharedPreferences sp = mContext.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        setTheme(sp.getInt("theme",R.style.AppTheme));
        setColor(sp.getInt("main_color",ConstantUtils.NORMAL_COLOR));
        setColumn(sp.getInt("Column",1));
        setRecycleBin(sp.getInt("RecycleBin",-1));
        setImageCacheSwitch(sp.getInt("ImageCacheSwitch",1));
        setSmallViewSwitch(sp.getInt("SmallViewSwitch",1));
        setTestModeSwitch(sp.getInt("TestModeSwitch",-1));
        setShowHide(sp.getInt("ShowHide",-1));
    }
    /**
     * 主题资源
     */
    public static int Theme=R.style.AppTheme;

    public static void setTheme(int resId){
        Theme=resId;
    }

    /**
     * 主题颜色
     */
    public static int MainColor = ConstantUtils.SELECTED_COLOR;

    public static void setColor(int color){
        MainColor =color;
    }

    /**
     * 布局列数
     */
    public static int Column;

    public static void setColumn(int i){
        if(i>0&&i<7){
            Column=i;
        }
    }

    /**
     * 是否开启回收站
     */
    public static int RecycleBin;

    public static void setRecycleBin(int i){
        RecycleBin=i;
    }

    /**
     * 是否开启图片缓存
     */
    public static int ImageCacheSwitch;

    public static void setImageCacheSwitch(int parm){
        ImageCacheSwitch=parm;
    }

    /**
     * 是否开启图片缓存
     */
    public static int SmallViewSwitch;

    public static void setSmallViewSwitch(int parm){
        SmallViewSwitch=parm;
    }

    /**
     * 测试模式
     */
    public static int TestModeSwitch;

    public static void setTestModeSwitch(int testModeSwitch) {
        TestModeSwitch = testModeSwitch;
    }

    /**
     * 测试模式
     */
    public static int ShowHide;

    public static void setShowHide(int showHide) {
        ShowHide = showHide;
    }
}
