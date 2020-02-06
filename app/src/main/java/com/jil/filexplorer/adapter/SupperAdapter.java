package com.jil.filexplorer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.util.Util;
import com.jil.filexplorer.R;
import com.jil.filexplorer.api.SettingParam;
import com.jil.filexplorer.utils.FileUtils;

import java.io.File;
import java.util.List;

/**
 * 万能适配器
 *
 * @param <T>
 */
public abstract class SupperAdapter<T> extends RecyclerView.Adapter<SupperAdapter.VH> {

    private List<T> mDatas;
    private Context mContext;
    protected OnItemClickListener<T> listener;

    public interface OnItemClickListener<T> {
        void onItemClick(VH holder, T data, int position);
    }

    public SupperAdapter(List<T> mDatas, Context context) {
        this.mDatas = mDatas;
        this.mContext = context;
    }

    public SupperAdapter(List<T> mDatas, Context mContext, OnItemClickListener<T> listener) {
        this.mDatas = mDatas;
        this.mContext = mContext;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public abstract int getLayoutId(int viewType, T data);

    @Override
    public VH onCreateViewHolder(ViewGroup viewGroup, int i) {
        return VH.get(viewGroup, getLayoutId(i, mDatas.get(i)));
    }

    @Override
    public void onViewDetachedFromWindow(VH holder) {
        //item退出视图
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull VH holder) {
//        RoundedCorners roundedCorners= new RoundedCorners(10);
//        RequestOptions requestOptions =RequestOptions.bitmapTransform(roundedCorners)
//                .skipMemoryCache(true)//跳过缓存
//                .diskCacheStrategy(DiskCacheStrategy.NONE)//不缓存
//                .override(width,height);
        //item进入视图
        super.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewRecycled(VH holder) {
        //Log.e("SupperAdapter--", "onViewRecycled" + holder.getAdapterPosition());
        super.onViewRecycled(holder);

    }

    @Override
    public void onBindViewHolder(final SupperAdapter.VH vh, final int i) {
        if (listener != null) {
            vh.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(vh, mDatas.get(vh.getLayoutPosition()), i);
                }
            });

        }
        convert(vh, mDatas.get(i), i, mContext);
    }


    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public abstract void convert(VH holder, T data, int position, Context mContext);

    public static class VH extends RecyclerView.ViewHolder {
        private SparseArray<View> mViews;
        private View mConvertView;

        private VH(View v) {
            super(v);
            mConvertView = v;
            mViews = new SparseArray<>();
        }

        public static VH get(ViewGroup parent, int layoutId) {
            View convertView = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            return new VH(convertView);
        }

        public <T extends View> T getView(int id) {
            View v = mViews.get(id);
            if (v == null) {
                v = mConvertView.findViewById(id);
                mViews.put(id, v);
            }
            return (T) v;
        }

        public void setText(int id, String value) {
            TextView view = getView(id);
            view.setText(value);
        }

        public void setPic(int id, File pic, Context mContext) {
            ImageView view = getView(id);
            RequestOptions options = FileUtils.getOptions(SettingParam.ImageCacheSwitch, 100, 140);
            Glide.with(mContext).load(pic).apply(options).into(view);
        }

        public void setPic(int id, Uri pic, Context mContext) {
            ImageView view = getView(id);
            RequestOptions options = FileUtils.getOptions(SettingParam.ImageCacheSwitch, 100, 140);
            Glide.with(mContext).load(pic).apply(options).into(view);
        }

        public void setPic(int id, Drawable pic, Context mContext) {
            ImageView view = getView(id);
            //RequestOptions options= FileUtils.getOptions(SettingParam.ImageCacheSwitch,100,140);
            Glide.with(mContext).load(pic).override(400, 400).centerInside().into(view);
        }

        public void setPic(int id, Bitmap pic, Context mContext) {
            ImageView view = getView(id);
            //RequestOptions options= FileUtils.getOptions(SettingParam.ImageCacheSwitch,250,400);
            Glide.with(mContext).load(pic).override(250, 400).into(view);

        }
    }

    //带动画效果的添加
    public void addDate(T account, int position, int type) {
        mDatas.add(position, account);
        if (type == 1) {
            notifyItemInserted(position);
            if (position != mDatas.size()) {
                notifyItemRangeChanged(position, mDatas.size() - position);
            }
        } else {
            notifyDataSetChanged();
        }
    }

    //带动画效果的移除
    public void remove(int position, int type) {
        mDatas.remove(position);
        if (type == 1) {
            notifyItemRemoved(position);
            if (position != mDatas.size()) {
                notifyItemRangeChanged(position, mDatas.size() - position);
            }
        }
    }
}
