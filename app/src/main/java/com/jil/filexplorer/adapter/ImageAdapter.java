package com.jil.filexplorer.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Matrix;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.jil.filexplorer.Api.ImageFilter;
import com.jil.filexplorer.Api.OnScaleListener;
import com.jil.filexplorer.ImageDisplayActivity;
import com.jil.filexplorer.R;

import java.io.File;
import java.util.ArrayList;

public class ImageAdapter extends PagerAdapter {
    private ArrayList<File> images;
    private ArrayList<Uri>  datas;
    private String path;
    private ImageDisplayActivity activity;


    public ImageAdapter(String path,ImageDisplayActivity activity) {
        images=new ArrayList<>();
        File file =new File(path);
        ImageFilter imageFilter =new ImageFilter();
        File image[] = file.getParentFile().listFiles(imageFilter);
        for(int i =0;i<image.length;i++){
            images.add(image[i]);
        }
        this.activity=activity;
    }

    public ImageAdapter(ArrayList<Uri> datas, ImageDisplayActivity activity) {
        this.datas = datas;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        if(images==null){
            return datas.size();
        }else {
            return images.size();
        }

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

    public ArrayList<File> getImages() {
        return images;
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
