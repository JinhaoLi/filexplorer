package com.jil.filexplorer.Api;

import android.view.MotionEvent;
import android.view.View;

import com.jil.filexplorer.utils.LogUtils;

/**
 * 双击
 */
public class OnDoubleClickListener implements View.OnTouchListener {
    private int count = 0;//点击次数
    private long firstClick = 0;//第一次点击时间
    /**
     * 两次点击时间间隔，单位毫秒
     */
    private final static int totalTime = 300;
    /**
     * 自定义回调接口
     */
    private DoubleClickCallback mCallback;
    private boolean moved;


    public interface DoubleClickCallback {
        /**
         * 双击
         */
        void onDoubleClick();

        /**
         * 按下手指300ms之后抬起手指就会触发
         * @return true,处理完毕。false,继续传递下去
         */
        boolean onLongClick();
    }

    public OnDoubleClickListener(DoubleClickCallback callback) {
        super();
        this.mCallback = callback;
    }


    /**
     * 触摸事件处理
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(final View v, MotionEvent event) {
        if (MotionEvent.ACTION_DOWN == event.getAction()) {//按下
            count++;
            if (1 == count) {
                firstClick = System.currentTimeMillis();//记录第一次点击时间

            } else if (2 == count) {
                //第二次点击时间
                long secondClick = System.currentTimeMillis();//记录第二次点击时间
                if (secondClick - firstClick < totalTime) {//判断二次点击时间间隔是否在设定的间隔时间之内
                    if (mCallback != null) {
                        mCallback.onDoubleClick();
                    }
                    count = 0;
                    firstClick = 0;
                    return true;
                } else {
                    firstClick = secondClick;
                    count = 1;
                }
            }
        }
        if(MotionEvent.ACTION_UP ==event.getAction()&&count==1&&!moved){
            long clickUp = System.currentTimeMillis();//记录放开手指的时间
            if(clickUp-firstClick>=totalTime){
                LogUtils.i(getClass().getName(),"long click");
                return mCallback.onLongClick();
            }
        }
        if(MotionEvent.ACTION_MOVE ==event.getAction()&&!moved){
            moved=true;
        }
        return false;
    }

}
