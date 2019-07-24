package com.jil.filexplorer.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jil.filexplorer.FileInfo;
import com.jil.filexplorer.R;
import com.jil.filexplorer.utils.FileUtils;
import com.jil.filexplorer.utils.LogUtils;
import com.jil.filexplorer.utils.MenuUtils;
import com.jil.filexplorer.utils.ToastUtils;
import com.yanzhenjie.recyclerview.swipe.Closeable;
import com.yanzhenjie.recyclerview.swipe.OnSwipeMenuItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.touch.OnItemMoveListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import static com.jil.filexplorer.utils.FileUtils.deleteAFile;
import static com.jil.filexplorer.utils.MenuUtils.fileSwipeMenu;
import static com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView.LEFT_DIRECTION;
import static com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView.RIGHT_DIRECTION;

public class FileShowFragment extends CustomViewFragment {
    private TextView selectItemSize;

    public FileShowFragment(String filePath) {
        super(filePath);
    }

    public FileShowFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //item移动位置改变时调用

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container) {
        filePath = Environment.getExternalStorageDirectory().getPath();
        View v =super.initView(inflater, container);
        return v;
    }


    @Override
    protected void initAction() {
        super.initAction();
        load(filePath, false);
        fileList.setSwipeMenuCreator(new SwipeMenuCreator() {
            @Override
            public void onCreateMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, int viewType) {
                fileSwipeMenu(swipeLeftMenu, swipeRightMenu, mMainActivity);
            }
        });
        fileList.setSwipeMenuItemClickListener(new OnSwipeMenuItemClickListener() {
            @Override
            public void onItemClick(Closeable closeable, int adapterPosition, int menuPosition, int direction) {

                if (menuPosition == 0 && direction == RIGHT_DIRECTION) {
                    //删除逻辑
                    /*if(deleteAFile(fileInfos.get(adapterPosition))){
                        ToastUtils.showToast(mMainActivity,"已删除！",1000);
                    }*/
                    deleteItem(adapterPosition);
                    closeable.smoothCloseMenu();
                }

                if (menuPosition == 1 && direction == RIGHT_DIRECTION) {
                    //属性

                    closeable.smoothCloseMenu();
                }
                if (menuPosition == 0 && direction == LEFT_DIRECTION) {
                    //复制逻辑

                    ToastUtils.showToast(mMainActivity, getString(R.string.copy), 1000);
                    closeable.smoothCloseMenu();
                }
                if (menuPosition == 1 && direction == LEFT_DIRECTION) {
                    //剪切逻辑

                    ToastUtils.showToast(mMainActivity, getString(R.string.cut), 1000);
                    closeable.smoothCloseMenu();
                }

            }
        });
        fileList.setOnItemMoveListener(new OnItemMoveListener() {
            @Override
            public boolean onItemMove(int fromPosition, int toPosition) {
               return itemPositionDrag(toPosition);
            }

            @Override
            public void onItemDismiss(int position) {
                ToastUtils.showToast(mMainActivity, "onItemDismiss(int position)" + position, 1000);
            }
        });// 监听拖拽和侧滑删除，更新UI和数据。
    }

    @Override
    protected boolean itemPositionDrag(int toPosition) {
        return super.itemPositionDrag(toPosition);
    }

    @Override
    protected void deleteItem(int adapterPosition) {
        fileInfos.remove(adapterPosition);
        fileListAdapter.notifyItemRemoved(adapterPosition);
        fileListAdapter.notifyItemRangeChanged(adapterPosition, fileInfos.size());
        clearUnderBar();
    }

    @Override
    protected boolean getFileListFromDir(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            ToastUtils.showToast(mMainActivity, "无效路径", 1000);
            return false;
        }
        if (file.canRead()) {
            fragmentTitle = file.getName();
            File[] files = file.listFiles();
            LogUtils.i(getClass().getName() + "285", files.length + "");
            fileInfos = new ArrayList<>();
            for (File temp : files) {
                fileInfos.add(FileUtils.getFileInfoFromFile(temp));
            }
            try {
                Collections.sort(fileInfos, comparator);
            }catch (Exception e){
                LogUtils.e(getClass().getName(),"排序的时候产生一个错误，未知错误");
            }
            LogUtils.i(getClass().getName() + "290", fileInfos.size() + "");
            return true;//可以访问
        } else if (!file.canRead()) {
            ToastUtils.showToast(mMainActivity, "无法访问", 1000);
            return false;//无法访问
        } else {
            ToastUtils.showToast(mMainActivity, "无法访问", 1000);
            return false;//无效路径
        }
    }


    @Override
    public void load(String filePath, boolean isBack) {
        if (getFileListFromDir(filePath)) {
            if (fileListAdapter == null) {
                //第一次加载
                makeGridLayout(6,R.layout.flie_grid_item_layout_100dp);
                mMainActivity.getHistoryPath().add(filePath);
            } else {
                //刷新
                fileListAdapter.setmData(fileInfos);
                fileListAdapter.notifyDataSetChanged();
                if (!isBack) {
                    mMainActivity.getHistoryPath().add(filePath);
                }
            }
            this.filePath = filePath;
            mMainActivity.refresh(filePath);
            clearUnderBar();
        }
        outOfFromPosition = 0;
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
        switch (id) {
            case 1:
                //复制
                break;
            case 2:
                //剪切
                break;
            case 3:
                LinkedList<FileInfo> deleteList = getSelectedList();//储存删除对象
                ArrayList<Integer> deletePosition = getSelectPosition(deleteList);//储存位置
                if (deleteList.size() != 0 && deletePosition.size() != 0) {
                    fileInfos.removeAll(deleteList);
                    fileListAdapter.notifyDataSetChanged();
                    outOfFromPosition=0;
                }
                clearUnderBar();
                //删除逻辑
                //Code。。。
                ToastUtils.showToast(mMainActivity, "文件并未删除！" + "测试删除" + deletePosition.size() + "项", 1000);
                break;
            case 4:
                //添加
                break;
            case 5:
                //搜索
                break;
            case 6:
                //刷新
                load(mMainActivity.getEditText().getText().toString(), false);
                break;
            case 7:
                //退出
                //this.rootView.setVisibility(View.GONE);
                mMainActivity.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 拖动状态改变--手指抬起,移动文件夹
     * @param dir
     */
    protected void moveDir(FileInfo dir){
        String name = dir.getFileName();
        AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);
        AlertDialog alertDialog = builder.setTitle("文件操作").setMessage("确定移动到" + name + "吗？")
                .setNegativeButton("移动", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        fileInfos.remove(longClickPosition);  //移除选中项
                        fileListAdapter.notifyItemRemoved(longClickPosition);//通知刷新界面移除效果
                        //修正position
                        if (longClickPosition != fileInfos.size()) {
                            fileListAdapter.notifyItemRangeChanged(longClickPosition, fileInfos.size() - longClickPosition);
                        }
                        refreshUnderBar();


                    }
                })
                .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).create();
        alertDialog.show();
    }


}
