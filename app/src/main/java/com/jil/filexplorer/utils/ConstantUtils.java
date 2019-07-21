package com.jil.filexplorer.utils;

import android.graphics.Color;

public class ConstantUtils {
    public final static int NORMAL_COLOR = Color.argb(0,255,255,255);//透明色
    public final static int SELECTED_COLOR =Color.argb(49,69,198,255);//item被选中时的颜色
    public final static int CANT_SELECTED_COLOR =Color.argb(45,255,77,77);//item拖动到文件上面时，无法移动的文件背景颜色
    public final static int CAN_MOVE_COLOR = Color.argb(76,252,215,8);//item拖动到文件夹上面时，能够移动的背景颜色
    public final static long GB =1024*1024*1024;
    public final static long MB =1024*1024;
    public final static long KB =1024;
}
