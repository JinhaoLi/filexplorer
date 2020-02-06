package com.jil.filexplorer.api;

import android.annotation.SuppressLint;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

@SuppressLint("AppCompatCustomView")
public class OnScaleListener implements View.OnTouchListener{

    public OnScaleListener(OnScalceCallBack onScalceCallBack) {
        this.onScalceCallBack = onScalceCallBack;
    }

    private OnScalceCallBack onScalceCallBack;
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    private boolean over ;



    //用于显示图像的Matrix
    private Matrix matrix=new Matrix();
    private Matrix saveMatrix =new Matrix();
    //点
    private PointF startPoint =new PointF(  );
    private PointF midPoint =new PointF(  );
    //两点距离
    private float oriDis = 1f;
    float oldRotation = 0;

    public interface OnScalceCallBack {
        //开始触摸就调用
        //void scaleType(ImageView.ScaleType scaleType);
        //动作结束调用
        //void scaleTouch(Matrix matrix);
        //点击调用
        void onClick(View view);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ImageView imageView= (ImageView) v;
        imageView.setScaleType( ImageView.ScaleType.MATRIX );;
        ImageView view = (ImageView) v;
        switch (event.getAction()& MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:
                matrix.set(view.getImageMatrix());
                saveMatrix.set( matrix );
                startPoint.set( event.getX(),event.getY() );
                mode=DRAG;
                over=true;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                over=false;
                oldRotation = rotation(event);
                oriDis=getDistance(event);
                if(oriDis>10){
                    saveMatrix.set( matrix );
                    midPoint=getMiddle( event );
                    mode = ZOOM;
                }
                break;
            case MotionEvent.ACTION_UP:
                if(over)
                onScalceCallBack.onClick(v);
                break;
            case MotionEvent.ACTION_POINTER_UP:

                mode = NONE;
                break;
            // 单指滑动事件
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    // 是一个手指拖动

                    matrix.set(saveMatrix);
                    matrix.postTranslate(event.getX() - startPoint.x, event.getY() - startPoint.y);
                } else if (mode == ZOOM) {
                    // 两个手指滑动
                    float rotation = rotation(event) - oldRotation;
                    float newDist = getDistance(event);
                    if (newDist > 10f) {
                        matrix.set(saveMatrix);
                        float scale = newDist / oriDis;
                        matrix.postScale(scale, scale, midPoint.x, midPoint.y);
                        matrix.postRotate(rotation, midPoint.x, midPoint.y);// 旋轉
                    }
                }
                break;
        }
        imageView.setImageMatrix(matrix);
        return true;
    }

    // 计算两个触摸点之间的距离
    private static float getDistance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }

    // 计算两个触摸点的中点
    private static PointF getMiddle(MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        return new PointF(x / 2, y / 2);
    }

    // 取旋转角度
    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    public void setOnScalceCallBack(OnScalceCallBack onScalceCallBack){
        this.onScalceCallBack = onScalceCallBack;
    }
}
