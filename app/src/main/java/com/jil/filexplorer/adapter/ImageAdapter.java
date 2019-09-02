package com.jil.filexplorer.adapter;

import android.annotation.SuppressLint;
import android.graphics.Matrix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.jil.filexplorer.Api.OnScaleListener;
import com.jil.filexplorer.Activity.ImageDisplayActivity;
import com.jil.filexplorer.R;

import java.io.File;
import java.util.ArrayList;

public class ImageAdapter extends PagerAdapter {
    private ArrayList<File> images;
    private String path;
    private ImageDisplayActivity activity;
    public int width=720;
    public int height=1280;


    public ImageAdapter(String path,ImageDisplayActivity activity) {
        images=new ArrayList<>();
        this.activity=activity;
    }

    public ImageAdapter(ArrayList<File> images, ImageDisplayActivity activity) {
        this.images = images;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view==object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
    boolean sacle;
    @SuppressLint("ClickableViewAccessibility")
    @NonNull
    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        //sacle=false;
        View v = LayoutInflater.from(activity).inflate(R.layout.image_layout,container,false);
        final ImageView imageView =v.findViewById(R.id.imageView);
        RoundedCorners roundedCorners= new RoundedCorners(10);
        RequestOptions requestOptions =RequestOptions.bitmapTransform(roundedCorners)
                .skipMemoryCache(true)//跳过缓存
                .diskCacheStrategy(DiskCacheStrategy.NONE)//不缓存
                .override(width,height);
        Glide.with(activity).load(images.get(position)).apply(requestOptions).into(imageView);
        imageView.setOnTouchListener(new OnScaleListener(new OnScaleListener.OnScalceCallBack() {
            @Override
            public void scaleType(ImageView.ScaleType scaleType) {
                imageView.setScaleType( ImageView.ScaleType.MATRIX );
            }
            @SuppressLint("CheckResult")
            @Override
            public void scaleTouch(Matrix matrix) {
                imageView.setImageMatrix(matrix);
            }
            @Override
            public void onClick(View view) {
                activity.hideView();
            }

        }));
        container.addView(v);
        if(width!=720){
            width=720;
            height=1280;
        }
        return v;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public int findPositionByName(String namePath){
        for(int i =0;i<images.size();i++){
            if(images.get(i).getPath().equals(namePath)){
                return i;
            }
        }
        return 1;
    }

    public void remove(int position){
        images.remove(position);
        notifyDataSetChanged();
    }


}
