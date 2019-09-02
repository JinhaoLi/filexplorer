package com.jil.filexplorer.Api;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.jil.filexplorer.R;
import com.jil.filexplorer.utils.ConstantUtils;

/**
 *配置参数，默认正数为是，负数为否
 */
public class SettingParam {

    /**
     * 使用SharedPreferences保存一些表单数据，设置数据
     * @param mContext
     * @param key
     * @param value
     */
    public static void saveSharedPreferences(Context mContext ,String key, String value) {
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
    public static void saveSharedPreferences(Context mContext ,String key, int value) {
        SharedPreferences sp = mContext.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    /**
     * 读取
     * @param mContext
     */
    public static void readSharedPreferences(Context mContext){
        SharedPreferences sp = mContext.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        setTheme(sp.getInt("theme",R.style.AppTheme));
        setColor(sp.getInt("main_color",ConstantUtils.NORMAL_COLOR));
        setColumn(sp.getInt("Column",1));
        setRecycleBin(sp.getInt("RecycleBin",-1));
        setImageCacheSwitch(sp.getInt("ImageCacheSwitch",1));
        setSmallViewSwitch(sp.getInt("SmallViewSwitch",1));
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
        if(i>0&&i<8){
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
}
