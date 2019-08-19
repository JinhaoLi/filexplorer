package com.jil.filexplorer.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MyItemDecoration extends RecyclerView.ItemDecoration {

    private final Drawable mLine;
    private final int orientation;

    public MyItemDecoration(Context context, int orientation) {
        this.orientation = orientation;
        int[] attrs = new int[]{android.R.attr.listDivider};
        TypedArray a = context.obtainStyledAttributes(attrs);
        mLine = a.getDrawable(0);
        a.recycle();
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        if (parent.getLayoutManager() instanceof GridLayoutManager){
            drawHorizontal(c, parent, state);
            drawVertical(c, parent, state);
        }else {
            if (orientation == RecyclerView.HORIZONTAL) {
                drawVertical(c, parent, state);
            } else if (orientation == RecyclerView.VERTICAL) {
                drawHorizontal(c, parent, state);
            }
        }

    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {

        super.onDrawOver(c, parent, state);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (orientation == RecyclerView.HORIZONTAL) {
            //画垂直线
            outRect.set(0, 0, mLine.getIntrinsicWidth(), 0);
        } else if (orientation == RecyclerView.VERTICAL) {
            //画水平线
            outRect.set(0, 0, 0, mLine.getIntrinsicHeight());
        }
    }

    /**
     * 画垂直分割线
     */
    private void drawVertical(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            int left = child.getRight();
            int top = child.getTop();
            int right = left + mLine.getIntrinsicWidth();
            int bottom = child.getBottom();
            mLine.setBounds(left, top, right, bottom);
            mLine.draw(c);
        }
    }


    /**
     * 画水平分割线
     */
    private void drawHorizontal(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            int left = child.getLeft();
            int top = child.getBottom();
            int right = child.getRight();
            int bottom = top + mLine.getIntrinsicHeight();
            mLine.setBounds(left, top, right, bottom);
            mLine.draw(c);
        }
    }

}
