package com.jil.filexplorer.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jil.filexplorer.FileInfo;
import com.jil.filexplorer.MainActivity;
import com.jil.filexplorer.R;
import com.jil.filexplorer.adapter.FileListAdapter;
import com.jil.filexplorer.utils.FileUtils;
import com.jil.filexplorer.utils.LogUtils;
import com.jil.filexplorer.utils.MenuUtils;
import com.jil.filexplorer.utils.ToastUtils;
import com.yanzhenjie.recyclerview.swipe.Closeable;
import com.yanzhenjie.recyclerview.swipe.OnSwipeMenuItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;
import com.yanzhenjie.recyclerview.swipe.touch.OnItemMoveListener;
import com.yanzhenjie.recyclerview.swipe.touch.OnItemStateChangedListener;

import java.io.File;
import java.util.ArrayList;

import static com.jil.filexplorer.utils.ColorUtils.NORMAL_COLOR;
import static com.jil.filexplorer.utils.ColorUtils.CANT_SELECTED_COLOR;
import static com.jil.filexplorer.utils.ColorUtils.SELECTED_COLOR;
import static com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView.LEFT_DIRECTION;
import static com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView.RIGHT_DIRECTION;

public class FileViewFragment extends Fragment {
    private final static String TAG = "FileViewFragment";
    private String fragmentTitle;
    private MainActivity mMainActivity;
    private View rootView;
    private String filePath;
    private ArrayList<FileInfo> fileInfos;
    private FileListAdapter fileListAdapter;
    private SwipeMenuRecyclerView fileList;
    //记录上次经过项
    private int outOfFromPosition;
    //记录选中项
    private int selectPosition =-1;
    private LinearLayoutManager linearLayoutManager;
    private int fileListScrollState;
    //路径输入

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        filePath = Environment.getExternalStorageDirectory().getPath();
        mMainActivity = (MainActivity) getActivity();
        mMainActivity.setFileViewFragment(this);
        rootView = inflater.inflate(R.layout.file_view_fragment_layout, container, false);
        fileList = (SwipeMenuRecyclerView) rootView.findViewById(R.id.file_list_view);
        linearLayoutManager = new LinearLayoutManager(mMainActivity);
        fileList.setLayoutManager(linearLayoutManager);
        fileList.setLongPressDragEnabled(true);// 开启长按拖拽
        setHasOptionsMenu(true);//onCreateOptionsMenu生效条件
        load(filePath,false);
        fileList.setSwipeMenuCreator(new SwipeMenuCreator() {
            @Override
            public void onCreateMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, int viewType) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(mMainActivity)
                        .setBackgroundColor(Color.RED)
                        //.setImage(R.mipmap.ic_launcher) // 图标。
                        .setText(mMainActivity.getString(R.string.delete)) // 文字。
                        .setTextColor(Color.WHITE) // 文字颜色。
                        .setTextSize(15) // 文字大小。
                        .setWidth(120).setWeight(1)
                        .setHeight(-1);//-1指的时父布局的高度
                swipeRightMenu.addMenuItem(deleteItem);// 添加一个按钮到右侧侧菜单。.
                SwipeMenuItem copyItem = new SwipeMenuItem(mMainActivity)
                        .setBackgroundColor(Color.BLUE)
                        //.setImage(R.mipmap.ic_launcher) // 图标。
                        .setText(mMainActivity.getString(R.string.copy)) // 文字。
                        .setTextColor(Color.WHITE) // 文字颜色。
                        .setTextSize(15) // 文字大小。
                        .setWidth(120)
                        .setHeight(-1);//-1指的时父布局的高度
                SwipeMenuItem cutItem = new SwipeMenuItem(mMainActivity)
                        .setBackgroundColor(Color.DKGRAY)
                        //.setImage(R.mipmap.ic_launcher) // 图标。
                        .setText(mMainActivity.getString(R.string.cut)) // 文字。
                        .setTextColor(Color.WHITE) // 文字颜色。
                        .setTextSize(15) // 文字大小。
                        .setWidth(120)
                        .setHeight(-1);//-1指的时父布局的高度
                swipeLeftMenu.addMenuItem(copyItem);
                swipeLeftMenu.addMenuItem(cutItem);
            }
        });

        fileList.setSwipeMenuItemClickListener(new OnSwipeMenuItemClickListener() {
            @Override
            public void onItemClick(Closeable closeable, int adapterPosition, int menuPosition, int direction) {
                ArrayList<FileInfo> fileInfos = fileListAdapter.getmData();
                if (menuPosition == 0 && direction==RIGHT_DIRECTION) {
                    //删除逻辑
                    fileListAdapter.notifyItemRemoved(adapterPosition);
                    fileInfos.remove(adapterPosition);
                    fileListAdapter.notifyItemRangeChanged(adapterPosition, fileInfos.size());
                    closeable.smoothCloseMenu();
                }
                if(menuPosition==0&&direction==LEFT_DIRECTION){
                    //复制逻辑

                    ToastUtils.showToast(mMainActivity,getString(R.string.copy),1000);
                    closeable.smoothCloseMenu();
                }
                if(menuPosition==1&&direction==LEFT_DIRECTION){
                    //剪切逻辑

                    ToastUtils.showToast(mMainActivity,getString(R.string.cut),1000);
                    closeable.smoothCloseMenu();
                }
            }
        });

        fileList.setOnItemStateChangedListener(new OnItemStateChangedListener() {
            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int i) {
                LogUtils.i(getClass().getName() + ":95", i + "");
                //选中项次层级
                View v = linearLayoutManager.findViewByPosition(outOfFromPosition);
                if (i == 2) {
                    //此项选中时将背景透明化
                    viewHolder.itemView.setBackgroundColor(NORMAL_COLOR);
                }
                final ArrayList<FileInfo> fileInfos = fileListAdapter.getmData();
                if (i == 0 && fileInfos.size() > 1 && !fileList.isComputingLayout() && v != null) {
                    //i == 0 选中项被释放
                    v.setBackgroundColor(NORMAL_COLOR);  //恢复次层级颜色
                    FileInfo inputDir=fileListAdapter.getmData().get(outOfFromPosition);

                    if(selectPosition !=-1 && inputDir.isDir()){    //移动范围超item范围
                        String name =inputDir.getFileName();
                        AlertDialog.Builder builder = new AlertDialog.Builder( mMainActivity );
                        AlertDialog alertDialog=builder.setTitle("文件操作").setMessage("确定移动到"+name+"吗？")
                                .setNegativeButton("移动" , new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        fileInfos.remove(selectPosition);  //移除选中项
                                        fileListAdapter.notifyItemRemoved(selectPosition);//通知刷新界面移除效果
                                        //修正position
                                        if (selectPosition != fileInfos.size()) {
                                            fileListAdapter.notifyItemRangeChanged(selectPosition, fileInfos.size() - selectPosition);
                                        }
                                        selectPosition =-1;//修正范围

                                    }
                                })
                                .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        selectPosition =-1;//修正范围
                                    }
                                }).create();
                        alertDialog.show();
                    }
                }

            }
        });


        fileList.setOnItemMoveListener(new OnItemMoveListener() {
            @Override
            public boolean onItemMove(int fromPosition, int toPosition) {
                LogUtils.i(getClass().getName() + "157", fromPosition + "--"+toPosition);
                if (outOfFromPosition != toPosition) {
                    //此项位置发生改变时，将上一次染色的item背景改变为透明
                    if (linearLayoutManager.findViewByPosition(outOfFromPosition) != null)
                        linearLayoutManager.findViewByPosition(outOfFromPosition).setBackgroundColor(NORMAL_COLOR);
                    //刷新此项当前的的位置
                    outOfFromPosition = toPosition;
                }
                //记录此项原来的的位置
                selectPosition = fromPosition;
                //将此项的次层级item背景改变为浅蓝色
                FileInfo inputDir=fileListAdapter.getmData().get(toPosition);
                if(inputDir.isDir()){
                    linearLayoutManager.findViewByPosition(toPosition).setBackgroundColor(SELECTED_COLOR);
                }else {
                    linearLayoutManager.findViewByPosition(toPosition).setBackgroundColor(CANT_SELECTED_COLOR);
                }

                return true;
            }

            @Override
            public void onItemDismiss(int position) {
                LogUtils.i(getClass().getName() + ":117", position + "");
            }
        });// 监听拖拽和侧滑删除，更新UI和数据。
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        MenuUtils.addMenu(menu);
        super.onCreateOptionsMenu(menu, mMainActivity.getMenuInflater());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        //path.setFocusable(false);
        switch (id){
            case 1:
                //复制
                break;
            case 2:
                //剪切
                break;
            case 3:
                ArrayList<FileInfo> deleteList =new ArrayList<FileInfo>();//储存删除对象
                ArrayList<Integer> deletePosition=new ArrayList<Integer>();//储存位置
                int start=0;//第一个删除的position
                for(int i =0;i<fileInfos.size();i++) {
                    FileInfo fileInfo =fileInfos.get(i);
                    if(fileInfo.isSelected()){
                        deleteList.add(fileInfo);
                        deletePosition.add(i);
                        if(deletePosition.size()==1){
                            start=i;
                        }
                    }
                }
                if(deleteList.size()!=0){
                    for(int i =0;i<deletePosition.size();i++){
                        fileListAdapter.notifyItemRemoved(deletePosition.get(i));
                    }
                    fileInfos.removeAll(deleteList);
                    fileListAdapter.notifyItemRangeChanged(start,fileInfos.size());
                }
                //删除逻辑
                //Code。。。
                ToastUtils.showToast(mMainActivity,deleteList.size()+"",1000);
                break;
            case 4:
                //添加
                break;
            case 5:
                //搜索
                break;
            case 6:
                //刷新
                load(mMainActivity.getEditText().getText().toString(),false);
                break;
            case 7:
                //退出
                this.rootView.setVisibility(View.GONE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    private boolean getFileFromDir(String filePath) {
        File file=new File(filePath);
        if(!file.exists()){
            ToastUtils.showToast(mMainActivity,"无效路径",1000);
            return false;
        }
        if (file.canRead()) {
            fragmentTitle = file.getName();
            File[] files = file.listFiles();
            LogUtils.i(getClass().getName()+"285",files.length+"");
            fileInfos = new ArrayList<FileInfo>();
            for (File temp : files) {
                fileInfos.add(FileUtils.getFileInfoFromFile(temp));
            }
            LogUtils.i(getClass().getName()+"290",fileInfos.size()+"");
            return true;//可以访问
        }else if(!file.canRead()){
            ToastUtils.showToast(mMainActivity,"无法访问",1000);
            return false;//无法访问
        }else {
            ToastUtils.showToast(mMainActivity,"无法访问",1000);
            return false;//无效路径
        }
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFragmentTitle() {
        return fragmentTitle;
    }

    public void load(String filePath,boolean isBack) {
        if(getFileFromDir(filePath)){
            if (fileListAdapter == null) {
                //第一次加载
                fileListAdapter = new FileListAdapter(fileInfos, this, mMainActivity);
                fileList.setAdapter(fileListAdapter);
                mMainActivity.getHistoryPath().add(filePath);
                mMainActivity.setPositionInHistory(0);
            } else {
                //刷新
                fileListAdapter.setmData(fileInfos);
                fileListAdapter.notifyDataSetChanged();
                if(!filePath.equals(this.filePath)&&!isBack){
                    mMainActivity.getHistoryPath().add(filePath);
                    int positionHis =mMainActivity.getPositionInHistory()+1;
                    mMainActivity.setPositionInHistory(positionHis);
                    this.filePath=filePath;
                }

            }
            mMainActivity.refresh(filePath);
        }/*else{
            ToastUtils.showToast(mMainActivity,"没有找到目录",1000);
        }*/
    }

    //public void

    public ArrayList<FileInfo> getFileInfos() {
        return fileInfos;
    }
}
