package com.jil.filexplorer.adapter;

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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.jil.filexplorer.activity.ImageDisplayActivity;
import com.jil.filexplorer.api.*;
import com.jil.filexplorer.R;
import com.jil.filexplorer.ui.InputDialog;
import com.jil.filexplorer.utils.ConstantUtils;
import com.jil.filexplorer.utils.FileUtils;
import com.jil.filexplorer.utils.LogUtils;
import com.jil.filexplorer.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

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
import static com.jil.filexplorer.utils.FileUtils.getFileInfoFromFile;
import static com.jil.filexplorer.utils.FileUtils.getOptions;
import static com.jil.filexplorer.utils.FileUtils.getSelectedList;
import static com.jil.filexplorer.utils.FileUtils.shareFile;
import static com.jil.filexplorer.utils.FileUtils.stayFrieNumber;
import static com.jil.filexplorer.utils.FileUtils.viewFile;

/**
 * 文件列表设配器
 */
public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.DefaultViewHolder> {
    private ArrayList<FileInfo> mData;
    private FilePresenter filePresenter;
    private int itemLayoutRes;
    static Item[] dirMenu = {new Item("新页面打开",142),new Item("分享",128),new Item("剪切",321)
            ,new Item("复制",44),new Item("发送到桌面",52)
            ,new Item("重命名",623),new Item("删除",732)
            ,new Item("属性",813)};//要填充的数据
    static Item[] fileMenu = {new Item("打开方式",143),new Item("分享",128),new Item("剪切",321)
            ,new Item("复制",44),new Item("发送到桌面",52)
            ,new Item("重命名",623),new Item("删除",732)
            ,new Item("属性",813)};//要填充的数据

    public FileListAdapter(ArrayList<FileInfo> mData, FilePresenter filePresenter,int itemLayoutRes) {
        this.mData = mData;
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
        if (mData.get(holder.getAdapterPosition()).isSelected()) {
            holder.itemView.setBackgroundColor(ConstantUtils.SELECTED_COLOR);
        } else {
            holder.itemView.setBackgroundColor(NORMAL_COLOR);
        }

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
            holder.date.setText(FileUtils.getFormatData(new Date(fileInfo.getModifiedDate())));
            setItemTypeAndSize(fileInfo,holder);
        }else {
            //holder.type.setVisibility(View.GONE);
            if(fileInfo.isDir()){
                holder.type.setText(fileInfo.getCount()+"");
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
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(!fileInfo.isSelected())
                    filePresenter.selectAllPositionOrNot(false);
                filePresenter.selectItem(position);

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
                    filePresenter.input2Model(path, null,true);
                    //filePresenter.refreshUnderBar();
                } else {
                    viewFile(filePresenter.mContext, fileInfo,view);
                    if(fileInfo.isSelected()){
                        filePresenter.unSelectItem(position);
//                        fileInfo.setSelected(false);
//                        view.setBackgroundColor(NORMAL_COLOR);
                    }
                    if(fileInfo.getFiletype().startsWith("image")){
                        ImageDisplayActivity.setFileChangeListenter(filePresenter);
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
                filePresenter.refreshUnderBar();
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
                final File temp =new File(fileInfo.getFilePath());
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
                        filePresenter.delecteSelectFile();
                        break;
                    case 44://复制
                        filePresenter.copySelectFile();
                        break;
                    case 321://剪切
                          filePresenter.moveSelectFile();
                        break;
                    case 623://重命名
                        final ArrayList<FileInfo> fileInfos =getSelectedList(mData);
                        InputDialog dialog;
                        if (fileInfos.size() == 1) {
                            dialog =new InputDialog(filePresenter.mContext,R.layout.dialog_rename_layout,"重命名") {
                                @Override
                                public void queryButtonClick(View v) {

                                }

                                @Override
                                public void queryButtonClick(View v,String name) {
                                    boolean reNameOk=false;
                                    File f =new File(filePresenter.getPath()+File.separator+name);
                                    if(temp.exists()){
                                        reNameOk=temp.renameTo(f);
                                    }
                                    if(reNameOk){
                                        mData.remove(position);
                                        mData.add(position,getFileInfoFromFile(f));
                                        notifyDataSetChanged();
                                    }else {
                                        ToastUtils.showToast(filePresenter.mContext,"重命名失败",1000);
                                    }
                                }
                            };
                        } else {
                            dialog =new InputDialog(filePresenter.mContext,R.layout.dialog_renames_layout,"批量重命名"){
                                @Override
                                public void queryButtonClick(View v) {

                                }

                                @Override
                                public void queryButtonClick(View v, String nameInputStr) {
                                    ReNameList rnl=ReNameList.getInstance(nameInputStr);
                                    ArrayList<String> strings=rnl.getNameList(fileInfos.size());
                                    for (int h =0;h<fileInfos.size();h++) {
                                        FileInfo fi =fileInfos.get(h);
                                        File f = new File(fi.getFilePath());
                                        File t = new File(f.getParent(), strings.get(h));
                                        fi.setName(strings.get(h));
                                        fi.setFilePath(t.getPath());
                                        if(!t.exists()){
                                            f.renameTo(t);
                                        }else {
                                            t=new File(f.getParent(), strings.get(h)+h);
                                            f.renameTo(t);
                                        }


                                    }
                                    notifyDataSetChanged();

                                }

                            };
                        }
                    dialog.showAndSetName(temp.getName());
                        break;
                    case 813:
                        showFileInfoMsg(filePresenter.mContext,fileInfo);
                        break;
                    case 52:
                        addFastToDesk(filePresenter.mContext,fileInfo.getFileName(),fileInfo.getFilePath(), android.os.Build.VERSION.SDK_INT );
                        break;
                    case 418:
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

    private void setItemTypeAndSize(FileInfo fileInfo,DefaultViewHolder holder){
        if(fileInfo.isDir()){
            holder.size.setText("");
            try {
                holder.type.setText("共" + fileInfo.getCount() + "项");//文件种类
            } catch (Exception e) {
                LogUtils.e(getClass().getName(), e.getMessage()+"一个未知的错误，发生在计算文件夹内项目数量并显示出来时");
                e.printStackTrace();
            }
        }else {
            long size = fileInfo.getFileSize();
            String mineType =fileInfo.getName();
            holder.size.setText(size > GB ? stayFrieNumber((float) size / GB) + "GB" : size > MB ? stayFrieNumber((float) size / MB) + "MB" : stayFrieNumber((float) size / KB) + "KB");
            holder.type.setText(mineType.substring(mineType.lastIndexOf(".")+1));//文件种类
        }
    }

    public void setmData(ArrayList<FileInfo> mData) {
        this.mData = mData;
    }

    @Override
    public int getItemCount(){
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
