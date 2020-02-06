package com.jil.filexplorer.adapter;

import android.annotation.SuppressLint;
import android.graphics.Matrix;
import android.net.Uri;
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
import com.jil.filexplorer.api.OnScaleListener;
import com.jil.filexplorer.activity.ImageDisplayActivity;
import com.jil.filexplorer.R;
import java.util.ArrayList;

/**
 * 当获取到的是图片的uri的时候使用这个适配器
 */
public class UriAdapter  extends PagerAdapter {
    private ArrayList<Uri> images;
    private ImageDisplayActivity activity;
    public int width=720;
    public int height=1280;

    public UriAdapter(ArrayList<Uri> images, ImageDisplayActivity activity) {
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

    @SuppressLint("ClickableViewAccessibility")
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View v = LayoutInflater.from(activity).inflate(R.layout.image_layout,container,false);
        ImageView imageView =v.findViewById(R.id.imageView);
        Uri uri =images.get(position);
        RoundedCorners roundedCorners= new RoundedCorners(10);
        RequestOptions requestOptions =RequestOptions.bitmapTransform(roundedCorners)
                .skipMemoryCache(true)//跳过缓存
                .diskCacheStrategy(DiskCacheStrategy.NONE)//不缓存
                .override(width,height);
        Glide.with(activity).load(uri).apply(requestOptions)
                .into(imageView);
        imageView.setOnTouchListener(new OnScaleListener(new OnScaleListener.OnScalceCallBack() {
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

    public int findPositionByName(Uri uri){
        for(int i =0;i<images.size();i++){
            if(images.get(i).equals(uri)){
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
