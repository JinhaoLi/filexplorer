package com.jil.filexplorer.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jil.filexplorer.FileInfo;
import com.jil.filexplorer.MainActivity;
import com.jil.filexplorer.R;
import com.jil.filexplorer.interfaces.OnDoubleClickListener;
import com.jil.filexplorer.ui.FileViewFragment;
import com.jil.filexplorer.utils.FileUtils;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuAdapter;

import java.util.ArrayList;
import java.util.Date;

import static com.jil.filexplorer.utils.ColorUtils.NORMAL_COLOR;
import static com.jil.filexplorer.utils.ColorUtils.SELECTED_COLOR;

public class FileListAdapter extends SwipeMenuAdapter<FileListAdapter.DefaultViewHolder> {
    private ArrayList<FileInfo> data;
    private FileViewFragment mFileViewFragment;
    private MainActivity mMaintivity;

    public FileListAdapter(ArrayList<FileInfo> data, FileViewFragment mFileViewFragment,MainActivity m) {
        this.data = data;
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
        final FileInfo fileInfo =data.get(position);
        Date date=new Date(fileInfo.getModifiedDate());
        if(fileInfo.isSelected()){
            holder.itemView.setBackgroundColor(SELECTED_COLOR);
        }else{
            holder.itemView.setBackgroundColor(NORMAL_COLOR);
        }
        holder.fileName.setText(fileInfo.getFileName());
        holder.date.setText(FileUtils.getFormatData(date));
        holder.type.setText((fileInfo.isDir()) ? "文件夹":"文件");
        holder.size.setText(fileInfo.getFileSize()/1024+"Kb");
        holder.icon.setImageResource(fileInfo.getIcon()==0 ? R.mipmap.list_ico_dir:fileInfo.getIcon());
        holder.itemView.setOnTouchListener(new OnDoubleClickListener(new OnDoubleClickListener.DoubleClickCallback() {
            @Override
            public void onDoubleClick() {
                if(fileInfo.isDir()){
                    mFileViewFragment.load(data.get(position).getFilePath());
                }else {

                    FileUtils.viewFile(mMaintivity,fileInfo.getFilePath());

                }
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
                //ToastUtils.showToast(mMaintivity,fileInfo.getIcon()+"--"+fileInfo.getFileName(),1000);*/
            }
        });


    }

    @Override
    public int getItemCount() {
        return data.size();
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

    public void setData(ArrayList<FileInfo> data) {
        this.data = data;
    }

    public ArrayList<FileInfo> getData() {
        return data;
    }
}
