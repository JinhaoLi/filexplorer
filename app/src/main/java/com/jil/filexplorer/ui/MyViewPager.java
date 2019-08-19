package com.jil.filexplorer.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.jil.filexplorer.utils.LogUtils;

public class MyViewPager extends ViewPager {
    public MyViewPager(@NonNull Context context) {
        super(context);
    }
    public MyViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(ev.getPointerCount()==2){
            return false;
        }else if(ev.getPointerCount()==1){
            try {
                return super.onInterceptTouchEvent(ev);
            }catch (Exception e){
                LogUtils.e("未知错误","放大手势中出现的bug");
            }
        }
        return false;
    }
}
