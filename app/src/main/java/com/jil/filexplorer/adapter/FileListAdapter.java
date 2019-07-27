package com.jil.filexplorer.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jil.filexplorer.Api.FileInfo;
import com.jil.filexplorer.MainActivity;
import com.jil.filexplorer.R;
import com.jil.filexplorer.Api.OnDoubleClickListener;
import com.jil.filexplorer.ui.CustomViewFragment;
import com.jil.filexplorer.utils.FileUtils;
import com.jil.filexplorer.utils.LogUtils;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import static com.jil.filexplorer.utils.ConstantUtils.GB;
import static com.jil.filexplorer.utils.ConstantUtils.KB;
import static com.jil.filexplorer.utils.ConstantUtils.MB;
import static com.jil.filexplorer.utils.ConstantUtils.NORMAL_COLOR;
import static com.jil.filexplorer.utils.ConstantUtils.SELECTED_COLOR;
import static com.jil.filexplorer.utils.FileTypeFilter.imageIf;
import static com.jil.filexplorer.utils.FileUtils.stayFrieNumber;

public class FileListAdapter extends SwipeMenuAdapter<FileListAdapter.DefaultViewHolder> {
    private ArrayList<FileInfo> mData;
    private CustomViewFragment mCustomViewFragment;
    private MainActivity mMaintivity;
    private int itemLayoutRes;

    public FileListAdapter(ArrayList<FileInfo> mData, CustomViewFragment mFileListFragment, MainActivity m,int itemLayoutRes) {
        this.mData = mData;
        this.mCustomViewFragment = mFileListFragment;
        this.mMaintivity = m;
        this.itemLayoutRes=itemLayoutRes;
    }

    @Override
    public View onCreateContentView(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayoutRes, parent, false);
        return v;
    }

    @Override
    public DefaultViewHolder onCompatCreateViewHolder(View realContentView, int viewType) {
        return new DefaultViewHolder(realContentView);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final DefaultViewHolder holder, final int position) {
        final FileInfo fileInfo = mData.get(position);
        Date date = new Date(fileInfo.getModifiedDate());
        if (fileInfo.isSelected()) {
            holder.itemView.setBackgroundColor(SELECTED_COLOR);
        } else {
            holder.itemView.setBackgroundColor(NORMAL_COLOR);
        }
        holder.fileName.setText(fileInfo.getFileName());
        if(itemLayoutRes==R.layout.file_list_item_layout){
            holder.date.setText(FileUtils.getFormatData(date));
            setItemIco(fileInfo,holder);
            setItemTypeAndSize(fileInfo,holder);
        }else {
            setItemIco(fileInfo,holder);
        }

        holder.itemView.setOnTouchListener(new OnDoubleClickListener(new OnDoubleClickListener.DoubleClickCallback() {
            @Override
            public void onDoubleClick() {
                if (fileInfo.isDir()) {
                    String path = fileInfo.getFilePath();
                    mCustomViewFragment.load(path, false);
                } else {
                    FileUtils.viewFile(mMaintivity, fileInfo.getFilePath());
                }
            }

            @Override
            public boolean onLongClick() {
                //showAlerDialog(mMaintivity,fileInfo);
                return false;
            }
        }));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!fileInfo.isSelected()) {
                    fileInfo.setSelected(true);
                    view.setBackgroundColor(SELECTED_COLOR);
                } else {
                    fileInfo.setSelected(false);
                    view.setBackgroundColor(NORMAL_COLOR);
                }
                mCustomViewFragment.refreshUnderBar();
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mCustomViewFragment.setLongClickPosition(position);
                return true;
            }
        });
    }

    private void setItemIco(FileInfo fileInfo,DefaultViewHolder holder){
        File ico = new File(fileInfo.getFilePath(), "ico");
        if(fileInfo.isDir()){
            if (!ico.exists()) {
                Glide.with(mMaintivity).load(R.mipmap.list_ico_dir).into(holder.icon);
            } else {
                Glide.with(mMaintivity).load(ico)
                        .error(R.mipmap.list_ico_dir)
                        .placeholder(R.mipmap.list_ico_dir)
                        .into(holder.icon);
            }
        }else {
            if (imageIf(fileInfo.getFiletype())) {
                Glide.with(mMaintivity).load(new File(fileInfo.getFilePath()))
                        .error(fileInfo.getIcon())
                        .placeholder(fileInfo.getIcon())
                        .into(holder.icon);
            } else {
                Glide.with(mMaintivity).load(fileInfo.getIcon())
                        .error(R.mipmap.list_ico_unknow)
                        .placeholder(R.mipmap.list_ico_unknow)
                        .into(holder.icon);
            }
        }
    }

    private void setItemTypeAndSize(FileInfo fileInfo,DefaultViewHolder holder){
        if(fileInfo.isDir()){
            holder.size.setText("");

            try {
                holder.type.setText("共" + fileInfo.getCount() + "项");//文件种类
            } catch (Exception e) {
                LogUtils.e(getClass().getName(), "一个未知的错误，发生在计算文件夹内项目数量并显示出来时");
                e.printStackTrace();
            }
        }else {
            long size = fileInfo.getFileSize();
            holder.size.setText(size > GB ? stayFrieNumber((float) size / GB) + "GB" : size > MB ? stayFrieNumber((float) size / MB) + "MB" : stayFrieNumber((float) size / KB) + "KB");
            holder.type.setText(fileInfo.getFiletype());//文件种类
        }
    }

    public void refreshList() {
        notifyDataSetChanged();
    }

    public void setmData(ArrayList<FileInfo> mData) {
        this.mData = mData;
    }

    public ArrayList<FileInfo> getmData() {
        return mData;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class DefaultViewHolder extends RecyclerView.ViewHolder {
        private TextView fileName;
        private TextView date;
        private TextView type;
        private TextView size;
        private ImageView icon;

        public DefaultViewHolder(@NonNull View itemView) {
            super(itemView);
            if(itemLayoutRes==R.layout.file_list_item_layout){
                this.fileName = (TextView) itemView.findViewById(R.id.textView2);
                this.date = (TextView) itemView.findViewById(R.id.textView3);
                this.type = (TextView) itemView.findViewById(R.id.textView4);
                this.size = (TextView) itemView.findViewById(R.id.textView5);
                this.icon = (ImageView) itemView.findViewById(R.id.imageView2);
            }else {
                this.fileName = (TextView) itemView.findViewById(R.id.textView7);
                this.icon = (ImageView) itemView.findViewById(R.id.imageView6);
            }

        }



    }
}
