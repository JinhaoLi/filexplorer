package com.jil.filexplorer.utils;

import android.graphics.Color;

public class ConstantUtils {
    public final static int NORMAL_COLOR = Color.argb(0,255,255,255);//透明色
    public final static int SELECTED_COLOR =Color.argb(50,21,143,197);//item被选中时的颜色
    public final static int CANT_SELECTED_COLOR =Color.argb(45,255,77,77);//item拖动到文件上面时，无法移动的文件背景颜色
    public final static int CAN_MOVE_COLOR = Color.argb(76,252,215,8);//item拖动到文件夹上面时，能够移动的背景颜色
    public final static long GB =1024*1024*1024;
    public final static long MB =1024*1024;
    public final static long KB =1024;
    public final static int GIRD_LINER_LAYOUT =Color.argb(81,0,196,255);//item被选中时的颜色
    public final static String CHANNEL_ID = "com.jil.filexplorer.copy_action";
    public final static int BULE_COLOR =Color.argb(255,21,143,197);//item被选中时的颜色
    public final static int DARK_COLOR=Color.argb(255,53,53,53);
    public final static int HALF_DARK_COLOR=Color.argb(125,66,66,66);
    public final static int IMAGE_SELECTED_COLOR =Color.argb(255,21,143,197);//image被选中时的颜色
    public final static int MAX_SPAN_COUNT=6;
    public final static int MIN_SPAN_COUNT=3;
}
