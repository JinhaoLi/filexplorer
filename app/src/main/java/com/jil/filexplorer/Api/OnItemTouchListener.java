package com.jil.filexplorer.Api;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.RecyclerView;

public abstract class OnItemTouchListener implements RecyclerView.OnItemTouchListener {

    private final RecyclerView recyclerView;
    private final GestureDetectorCompat mGestureDetector;

    public OnItemTouchListener(RecyclerView recyclerView){
        this.recyclerView=recyclerView;
        mGestureDetector = new GestureDetectorCompat(recyclerView.getContext(),new ItemTouchHelperGestureListener());
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        mGestureDetector.onTouchEvent(e);
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        mGestureDetector.onTouchEvent(e);
        return false;
    }

    public abstract void onItemClick(RecyclerView.ViewHolder viewHolder,int position);

    public abstract void onItemLongClick(RecyclerView.ViewHolder viewHolder, int position);

    private class ItemTouchHelperGestureListener extends GestureDetector.SimpleOnGestureListener {

        public  boolean onSingleTapUp(MotionEvent event){
            View child = recyclerView.findChildViewUnder(event.getX(), event.getY());
            if (child != null){
                int i =recyclerView.getChildPosition(child);
                RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(child);
                onItemClick(viewHolder,i);
            }
            return true;
        }

        public  void onLongPress(MotionEvent event){
            View child = recyclerView.findChildViewUnder(event.getX(), event.getY());
            if (child != null){
                int i =recyclerView.getChildPosition(child);
                RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(child);
                onItemLongClick(viewHolder,i);
            }
        }



    }

}
