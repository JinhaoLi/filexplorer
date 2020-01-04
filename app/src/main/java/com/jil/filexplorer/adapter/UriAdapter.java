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
        final ImageView imageView =v.findViewById(R.id.imageView);
        Glide.with(activity).load(images.get(position))
                .skipMemoryCache(true)//跳过缓存
                .diskCacheStrategy(DiskCacheStrategy.NONE)//不缓存
                .into(imageView);
        imageView.setOnTouchListener(new OnScaleListener(new OnScaleListener.OnScalceCallBack() {
            @Override
            public void scaleTouch(Matrix matrix) {
                imageView.setImageMatrix(matrix);
            }

            @Override
            public void scaleType(ImageView.ScaleType scaleType) {
                imageView.setScaleType( ImageView.ScaleType.MATRIX );
            }

            @Override
            public void onClick(View view) {
                activity.hideView();
            }
        }));
        container.addView(v);
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
