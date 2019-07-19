package com.jil.filexplorer.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
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
        load(filePath);
        fileList.setSwipeMenuCreator(new SwipeMenuCreator() {
            @Override
            public void onCreateMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, int viewType) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(mMainActivity)
                        .setBackgroundColor(Color.RED)
                        //.setImage(R.mipmap.ic_launcher) // 图标。
                        .setText("删除") // 文字。
                        .setTextColor(Color.WHITE) // 文字颜色。
                        .setTextSize(12) // 文字大小。
                        .setWidth(60)
                        .setHeight(48);
                swipeRightMenu.addMenuItem(deleteItem);// 添加一个按钮到右侧侧菜单。.
            }
        });

        fileList.setSwipeMenuItemClickListener(new OnSwipeMenuItemClickListener() {
            @Override
            public void onItemClick(Closeable closeable, int adapterPosition, int menuPosition, int direction) {
                ArrayList<FileInfo> fileInfos = fileListAdapter.getData();
                if (menuPosition == 0) {
                    fileListAdapter.notifyItemRemoved(adapterPosition);
                    fileInfos.remove(adapterPosition);
                    fileListAdapter.notifyItemRangeChanged(adapterPosition, fileInfos.size());
                    /*if (adapterPosition != fileInfos.size()) {
                        fileListAdapter.notifyItemRangeChanged(adapterPosition, fileInfos.size() - adapterPosition);
                    }*/
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
                final ArrayList<FileInfo> fileInfos = fileListAdapter.getData();
                if (i == 0 && fileInfos.size() > 1 && !fileList.isComputingLayout() && v != null) {
                    //i == 0 选中项被释放
                    v.setBackgroundColor(NORMAL_COLOR);  //恢复次层级颜色
                    FileInfo inputDir=fileListAdapter.getData().get(outOfFromPosition);

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
                FileInfo inputDir=fileListAdapter.getData().get(toPosition);
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
                ArrayList<FileInfo> deleteList =new ArrayList<FileInfo>();
                /*for(FileInfo fileInfo:getFileInfos()){
                    if(fileInfo.isSelected()) deleteList.add(fileInfo);
                }*/
                //final int[] position=new int[2];
                ArrayList<Integer> delete=new ArrayList<Integer>();
                //int[] delete =new int[fileInfos.size()];
                int start=1;
                for(int i =0;i<fileInfos.size();i++) {
                    if(fileInfos.get(i).isSelected()){
                        deleteList.add(fileInfos.get(i));
                        delete.add(i);
                        if(delete.size()==1){
                            start=i;
                        }
                    }
                }
                if(start!=-1){
                    for(int i =0;i<delete.size();i++){
                        fileListAdapter.notifyItemRemoved(delete.get(i));
                    }
                    fileInfos.removeAll(deleteList);
                    //fileListAdapter.notifyItemRangeRemoved(start,delete.size());
                    fileListAdapter.notifyItemRangeChanged(start,fileInfos.size());
                    //fileListAdapter.getData().removeAll(deleteList);

                    //fileListAdapter.notifyDataSetChanged();
                }



                //删除逻辑
                //Code。。。
                ToastUtils.showToast(mMainActivity,deleteList.size()+"",1000);
                break;
            case 11:
                LogUtils.i("main","sort by name");
                break;
            case 7:
                System.exit(0);
                break;
            case 4:
                load(mMainActivity.getEditText().getText().toString());
                break;
            case 5:
                mMainActivity.finish();
                break;

        }
        return super.onOptionsItemSelected(item);
    }



    private void initAction(String filePath) {
        File file = new File(filePath);
        fragmentTitle = file.getName();
        File[] files = file.listFiles();
        for (File file1 : files) {
            fileInfos.add(FileUtils.getFileInfoFromFile(file1));
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

    public void load(String filePath) {
        fileInfos = new ArrayList<FileInfo>();
        initAction(filePath);
        if (fileListAdapter == null) {
            fileListAdapter = new FileListAdapter(fileInfos, this, mMainActivity);
            fileList.setAdapter(fileListAdapter);
        } else {
            fileListAdapter.setData(fileInfos);
            fileListAdapter.notifyDataSetChanged();
        }
        mMainActivity.refresh(filePath);
    }

    //public void

    public ArrayList<FileInfo> getFileInfos() {
        return fileInfos;
    }
}
