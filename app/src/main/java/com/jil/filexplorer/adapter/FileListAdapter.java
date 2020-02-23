package com.jil.filexplorer.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.jil.filexplorer.BuildConfig;
import com.jil.filexplorer.activity.ImageDisplayActivity;
import com.jil.filexplorer.activity.MainActivity;
import com.jil.filexplorer.api.*;
import com.jil.filexplorer.R;
import com.jil.filexplorer.utils.ConstantUtils;
import com.jil.filexplorer.utils.FileUtils;
import com.jil.filexplorer.utils.LogUtils;

import java.io.File;
import java.util.ArrayList;

import static com.jil.filexplorer.utils.ConstantUtils.GB;
import static com.jil.filexplorer.utils.ConstantUtils.KB;
import static com.jil.filexplorer.utils.ConstantUtils.MB;
import static com.jil.filexplorer.utils.ConstantUtils.NORMAL_COLOR;
import static com.jil.filexplorer.utils.ConstantUtils.SELECTED_COLOR;
import static com.jil.filexplorer.utils.DialogUtils.showFileInfoMsg;
import static com.jil.filexplorer.utils.DialogUtils.showListPopupWindow;
import static com.jil.filexplorer.utils.FileTypeFilter.imageIf;
import static com.jil.filexplorer.utils.FileTypeFilter.videoIf;
import static com.jil.filexplorer.utils.FileUtils.addFastToDesk;
import static com.jil.filexplorer.utils.FileUtils.chooseViewFile;
import static com.jil.filexplorer.utils.FileUtils.getOptions;
import static com.jil.filexplorer.utils.FileUtils.shareFile;
import static com.jil.filexplorer.utils.FileUtils.stayFireNumber;
import static com.jil.filexplorer.utils.FileUtils.viewFile;

/**
 * 文件列表设配器
 */
public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.DefaultViewHolder> {
    private ArrayList<FileInfo> mData;
    private FilePresenter filePresenter;
    private int itemLayoutRes;
    static Item[] dirMenu = {new Item("新页面打开",142),/*new Item("压缩",854),*/new Item("剪切",321)
            ,new Item("复制",44),new Item("发送到桌面",52)
            ,new Item("重命名",623),new Item("删除",732)
            ,new Item("属性",813)};//要填充的数据
    static Item[] fileMenu = {new Item("打开方式",143),/*new Item("压缩",854),*/new Item("分享",128),new Item("剪切",321)
            ,new Item("复制",44),new Item("发送到桌面",52)
            ,new Item("重命名",623),new Item("删除",732)
            ,new Item("属性",813)};//要填充的数据

    public FileListAdapter(ArrayList<FileInfo> mData, FilePresenter filePresenter,int itemLayoutRes) {
        this.mData = mData;
        this.filePresenter = filePresenter;
        this.itemLayoutRes = itemLayoutRes;
    }

    public FileListAdapter(FilePresenter filePresenter,int itemLayoutRes) {
        this.filePresenter = filePresenter;
        this.itemLayoutRes = itemLayoutRes;
    }

    public void setItemLayoutRes(int itemLayoutRes) {
        this.itemLayoutRes = itemLayoutRes;
    }

    /**
     * 生成布局 ViewHolder(View v)
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public DefaultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayoutRes, parent, false);
        return new DefaultViewHolder(v);
    }

    //item进入视图
    @Override
    public void onViewAttachedToWindow(@NonNull DefaultViewHolder holder) {
//        if (mData.get(holder.getAdapterPosition()).isSelected()) {
//            holder.itemView.setBackgroundColor(ConstantUtils.SELECTED_COLOR);
//        } else {
//            holder.itemView.setBackgroundColor(NORMAL_COLOR);
//        }
        super.onViewAttachedToWindow(holder);
    }

    @Override
    public void onBindViewHolder(final DefaultViewHolder holder, final int position) {
        final FileInfo fileInfo = mData.get(position);
        if (fileInfo.isSelected()) {
            holder.itemView.setBackgroundColor(SELECTED_COLOR);
        } else {
            holder.itemView.setBackgroundColor(NORMAL_COLOR);
        }
        holder.fileName.setText(fileInfo.getFileName());
        if(itemLayoutRes==R.layout.file_list_item_layout){
            holder.date.setText(FileUtils.getFormatData(fileInfo.getModifiedDate()));
            setItemTypeAndSize(fileInfo,holder);
        }else {
            //holder.type.setVisibility(View.GONE);
            if(fileInfo.isDir()){
                holder.type.setText(String.valueOf(fileInfo.getCount()));
            }else{
                holder.type.setText("");
            }
        }
        setItemIco(fileInfo,holder);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!fileInfo.isSelected()) {
                    filePresenter.selectItem(position);
                } else {
                    filePresenter.unSelectItem(position);
                }
                filePresenter.notifyChanged();
                filePresenter.refreshUnderBar();

            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if(!fileInfo.isSelected()){
                    filePresenter.selectAllPositionOrNot(false);
                    filePresenter.selectItem(position);
                    filePresenter.notifyChanged();
                    filePresenter.refreshUnderBar();
                }

                Item[] menu ;
                if(fileInfo.isDir()){
                    menu=dirMenu;
                }else {
                    menu=fileMenu;
                }
                chooseOperation(v,menu,fileInfo,position);
                return true;
            }

        });

        holder.itemView.setOnTouchListener(new OnDoubleClickListener(new OnDoubleClickListener.DoubleClickCallback() {
            @Override
            public void onDoubleClick(View view) {
                if (fileInfo.isDir()) {
                    String path = fileInfo.getFilePath();
                    filePresenter.input2Model(path,true);
                } else {
                    if(fileInfo.getIcon()==R.mipmap.list_ico_image){
//                        Activity activity=ActivityManager.getInstance().getActivity(MainActivity.class);
//                        ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,holder.icon, "shareElement");
//                        Intent intent =new Intent(activity, ImageDisplayActivity.class);
//                        Uri uri = FileProvider.getUriForFile(ExplorerApp.ApplicationContext, BuildConfig.APPLICATION_ID + ".fileprovider", new File(fileInfo.getFilePath()));
//                        intent.setData(uri);
//                        ActivityCompat.startActivity(activity, intent, activityOptionsCompat.toBundle());
                        viewFile(filePresenter.mContext, fileInfo,view);

                    }else {
                        viewFile(filePresenter.mContext, fileInfo,view);
                    }


                }
            }

            @Override
            public boolean onLongClick() {
                return false;
            }
        }));

    }

    private void chooseOperation(final View v, final Item[] menu, final FileInfo fileInfo, final int position) {
        final ListPopupWindow listPopupWindow=showListPopupWindow(filePresenter.mContext,v,R.layout.menu_simple_list_item,menu);
        v.setBackgroundColor(ConstantUtils.CAN_MOVE_COLOR);
        listPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
//                filePresenter.refreshUnderBar();
                if(fileInfo.isSelected()){
                    v.setBackgroundColor(SELECTED_COLOR);
                }else {
                    v.setBackgroundColor(NORMAL_COLOR);
                }
            }
        });
        listPopupWindow.setWidth(300);
        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {//设置项点击监听
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int p, long i) {
                int id=(int)i;
                int clickId = 0;
                switch (id){
                    case 142://新页面打开
                        filePresenter.slideToPager(fileInfo.getFilePath());
                        break;
                    case 143://打开方式
                        clickId=143;
                        chooseViewFile(filePresenter.mContext,view,fileInfo.getFilePath(),listPopupWindow);
                        break;
                    case 128:
                        shareFile(filePresenter.mContext,fileInfo.getFilePath(),fileInfo.getFiletype());
                        break;
                    case 732://删除
                        filePresenter.deleteSelectFile();
                        break;
                    case 44://复制
                        filePresenter.copySelectFile();
                        break;
                    case 321://剪切
                          filePresenter.moveSelectFile();
                        break;
                    case 623://重命名
                        filePresenter.showReNameDialog(fileInfo.getFileName());
                        break;
                    case 813:
                        showFileInfoMsg(filePresenter.mContext,fileInfo);
                        break;
                    case 52:
                        int ico;
                        if(fileInfo.isDir())
                            ico=R.mipmap.list_ico_dir;
                        else
                            ico=fileInfo.getIcon();
                        addFastToDesk(filePresenter.mContext,fileInfo.getFileName(),fileInfo.getFilePath(), ico);
                        break;
                    case 854:
                        filePresenter.showCompressDialog();
                        break;

                }
                if(clickId!=143)
                listPopupWindow.dismiss();//如果已经选择了，隐藏起来
            }

        });
        listPopupWindow.show();
        ListView l =listPopupWindow.getListView();
        if(l!=null)
            l.setOverScrollMode(ListView.OVER_SCROLL_NEVER);
    }

    private void setItemIco(FileInfo fileInfo,DefaultViewHolder holder){
        File ico = new File(fileInfo.getFilePath(), "ico");
        RequestOptions options=getOptions(SettingParam.ImageCacheSwitch,260,283);
        if(fileInfo.isDir()){
            if (ico.exists()) {//自定义文件夹照片
                Glide.with(filePresenter.mContext).load(ico).apply(options)
                        .error(R.mipmap.list_ico_dir)
                        .signature(new MediaStoreSignature("image/*", ico.length(), 0))
                        .placeholder(R.mipmap.list_ico_dir)
                        .into(holder.icon);

            }else {
                Glide.with(filePresenter.mContext).load(R.mipmap.list_ico_dir).placeholder(R.mipmap.list_ico_dir).into(holder.icon);
            }
        }else {
            File f=new File(fileInfo.getFilePath());
            if (imageIf(fileInfo.getFiletype())&&SettingParam.SmallViewSwitch>0) {//加载图片缩略图
                Glide.with(filePresenter.mContext).load(f).apply(options)
                        .error(fileInfo.getIcon())
                        .signature(new MediaStoreSignature("image/*", f.length(), 2))
                        .placeholder(fileInfo.getIcon())
                        .into(holder.icon);
            } else if(fileInfo.getFileName().endsWith(".apk")){//加载apk缩略图
                Glide.with(filePresenter.mContext).load(FileUtils.getApkIcon(filePresenter.mContext,f.getPath()))
                        .error(R.drawable.ic_android_black_24dp)
                        .placeholder(R.mipmap.list_ico_unknow)
                        .into(holder.icon);
            }else if(videoIf(fileInfo.getFiletype())&&SettingParam.SmallViewSwitch>0){//加载视频缩略图
                Glide.with(filePresenter.mContext).load(fileInfo.getFilePath())
                        .error(fileInfo.getIcon())
                        .placeholder(fileInfo.getIcon())
                        .into(holder.icon);
            }else {//不加载缩略图
                Glide.with(filePresenter.mContext).load(fileInfo.getIcon())
                        .error(R.mipmap.list_ico_unknow)
                        .placeholder(fileInfo.getIcon())
                        .into(holder.icon);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void setItemTypeAndSize(FileInfo fileInfo, DefaultViewHolder holder){
        if(fileInfo.isDir()){
            holder.type.setText("文件夹");
            try {
                holder.size.setText("共" + fileInfo.getCount() + "项");//文件种类
            } catch (Exception e) {
                LogUtils.e(getClass().getName(), e.getMessage()+"一个未知的错误，发生在计算文件夹内项目数量并显示出来时");
                e.printStackTrace();
            }
        }else {
            long size = fileInfo.getFileSize();
            String mineType =fileInfo.getName();
            holder.size.setText(size > GB ? stayFireNumber((float) size / GB) + "GB" : size > MB ? stayFireNumber((float) size / MB) + "MB" : stayFireNumber((float) size / KB) + "KB");
            if(mineType.contains("."))
                holder.type.setText(mineType.substring(mineType.lastIndexOf(".")+1).toUpperCase()+"文件");//文件种类
            else
                holder.type.setText("文件");//文件种类
        }
    }

    public void setmData(ArrayList<FileInfo> mData) {
        this.mData = mData;
    }

    @Override
    public int getItemCount(){
        if(mData==null)
            return 0;
        return mData.size();
    }

    public void addData(FileInfo fileInfo) {
        mData.add(fileInfo);
    }

    public void addMoreData(ArrayList<FileInfo> inFiles) {
        mData.addAll(inFiles);
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
                this.fileName = itemView.findViewById(R.id.textView2);
                this.date = itemView.findViewById(R.id.textView3);
                this.type = itemView.findViewById(R.id.textView4);
                this.size = itemView.findViewById(R.id.textView5);
                this.icon = itemView.findViewById(R.id.imageView2);
            }else {
                this.type = itemView.findViewById(R.id.textView4);
                this.fileName = itemView.findViewById(R.id.textView7);
                this.icon = itemView.findViewById(R.id.imageView6);
            }

        }
    }
}
