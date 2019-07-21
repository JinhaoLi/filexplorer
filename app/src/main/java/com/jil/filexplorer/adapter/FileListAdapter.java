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
import com.jil.filexplorer.FileInfo;
import com.jil.filexplorer.MainActivity;
import com.jil.filexplorer.R;
import com.jil.filexplorer.interfaces.OnDoubleClickListener;
import com.jil.filexplorer.ui.FileViewFragment;
import com.jil.filexplorer.utils.FileUtils;
import com.jil.filexplorer.utils.LogUtils;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuAdapter;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

import static com.jil.filexplorer.utils.ConstantUtils.GB;
import static com.jil.filexplorer.utils.ConstantUtils.KB;
import static com.jil.filexplorer.utils.ConstantUtils.MB;
import static com.jil.filexplorer.utils.ConstantUtils.NORMAL_COLOR;
import static com.jil.filexplorer.utils.ConstantUtils.SELECTED_COLOR;
import static com.jil.filexplorer.utils.FileTypeFilter.imageIf;
import static com.jil.filexplorer.utils.FileUtils.stayDoubleNumber;

public class FileListAdapter extends SwipeMenuAdapter<FileListAdapter.DefaultViewHolder> {
    private ArrayList<FileInfo> mData;
    private FileViewFragment mFileViewFragment;
    private MainActivity mMaintivity;
    private int itemHeigth;

    public FileListAdapter(ArrayList<FileInfo> mData, FileViewFragment mFileViewFragment, MainActivity m) {
        this.mData = mData;
        this.mFileViewFragment=mFileViewFragment;
        this.mMaintivity = m;
    }

    @Override
    public View onCreateContentView(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate( R.layout.file_list_item_layout, parent, false);

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
        Date date=new Date(fileInfo.getModifiedDate());
        if(fileInfo.isSelected()){
            holder.itemView.setBackgroundColor(SELECTED_COLOR);
        }else{
            holder.itemView.setBackgroundColor(NORMAL_COLOR);
        }
        holder.fileName.setText(fileInfo.getFileName());
        holder.date.setText(FileUtils.getFormatData(date));
        //图标
        if(fileInfo.isDir()){
            holder.size.setText("");
            File ico=new File(fileInfo.getFilePath(),"ico");
            if(!ico.exists()){
                Glide.with(mMaintivity).load(R.mipmap.list_ico_dir).into(holder.icon);
            }else {
                Glide.with(mMaintivity).load(ico).into(holder.icon);
            }
            try {
                File file =new File(fileInfo.getFilePath());
                int count =file.listFiles().length;
                holder.type.setText("共"+count+"项");//文件种类
                //holder.type.setText(count<10? count+"\t\t项":count<100 ? count+"\t项":count+"项");//文件种类
            }catch (Exception e){
                LogUtils.e(getClass().getName(),"一个未知的错误，发生在计算文件夹内项目数量并显示出来时");
                e.printStackTrace();
            }
        }else{
            long size =fileInfo.getFileSize();
            holder.size.setText(size>GB ? stayDoubleNumber((float) size/GB)+"G":size>MB ? stayDoubleNumber((float) size/MB)+"M":stayDoubleNumber((float) size/KB)+"K");
            holder.type.setText(fileInfo.getFiletype());//文件种类
            if(imageIf(fileInfo.getFiletype())){
                Glide.with(mMaintivity).load(new File(fileInfo.getFilePath())).into(holder.icon);
            }else {
                Glide.with(mMaintivity).load(fileInfo.getIcon()).into(holder.icon);
            }
        }

        holder.itemView.setOnTouchListener(new OnDoubleClickListener(new OnDoubleClickListener.DoubleClickCallback() {
            @Override
            public void onDoubleClick() {
                if(fileInfo.isDir()){
                    String path =fileInfo.getFilePath();
                    mFileViewFragment.load(path,false);
                    //mMaintivity.getHistoryPath().add(path);
                    //int positionHis =mMaintivity.getPositionInHistory()+1;
                    //mMaintivity.setPositionInHistory(positionHis);
                }else {
                    FileUtils.viewFile(mMaintivity,fileInfo.getFilePath());
                }
            }

            @Override
            public boolean onLongClick() {
                //showAlerDialog(mMaintivity,fileInfo);
                return false;
                //ToastUtils.showToast(mMaintivity,fileInfo.getIcon()+"--"+fileInfo.getFilePath(),1000);
            }
        }));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!fileInfo.isSelected()){
                    fileInfo.setSelected(true);
                    view.setBackgroundColor(SELECTED_COLOR);
                }else {
                    fileInfo.setSelected(false);
                    view.setBackgroundColor(NORMAL_COLOR);
                }

            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //showAlerDialog(mMaintivity,fileInfo);


                return true;
            }
        });



    }

    public void refreshList(){
        notifyDataSetChanged();
    }

    public void setmData(ArrayList<FileInfo> mData) {
        this.mData = mData;
    }

    public ArrayList<FileInfo> getmData() {
        return mData;
    }

    public void setItemHeigth(int itemHeigth) {
        this.itemHeigth = itemHeigth;
    }

    public int getItemHeigth() {
        return itemHeigth;
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
            this.fileName = (TextView) itemView.findViewById(R.id.textView2);
            this.date = (TextView) itemView.findViewById(R.id.textView3);
            this.type = (TextView) itemView.findViewById(R.id.textView4);
            this.size = (TextView) itemView.findViewById(R.id.textView5);
            this.icon= (ImageView) itemView.findViewById(R.id.imageView2);
        }

    }
}
