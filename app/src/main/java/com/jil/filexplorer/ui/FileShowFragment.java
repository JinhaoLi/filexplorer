package com.jil.filexplorer.ui;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

import com.jil.filexplorer.Activity.ProgressActivity;
import com.jil.filexplorer.Api.FileInfo;
import com.jil.filexplorer.MainActivity;
import com.jil.filexplorer.R;
import com.jil.filexplorer.SettingActivity;
import com.jil.filexplorer.utils.FileUtils;
import com.jil.filexplorer.utils.LogUtils;
import com.jil.filexplorer.utils.MenuUtils;
import com.jil.filexplorer.utils.NotificationUtils;
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

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.jil.filexplorer.Activity.AfterIntentService.startActionCopy;
import static com.jil.filexplorer.Activity.AfterIntentService.startActionMove;
import static com.jil.filexplorer.Activity.ProgressActivity.setOnActionFinish;
import static com.jil.filexplorer.utils.ConstantUtils.CHANNEL_ID;
import static com.jil.filexplorer.utils.FileUtils.deleteAFile;
import static com.jil.filexplorer.utils.FileUtils.reNameFile;
import static com.jil.filexplorer.utils.MenuUtils.fileSwipeMenu;
import static com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView.LEFT_DIRECTION;
import static com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView.RIGHT_DIRECTION;

public class FileShowFragment extends CustomViewFragment {
    private TextView selectItemSize;
    //要复制的文件路径;
    private FileInfo copyPath;
    //粘贴到的路径；
    private FileInfo pastePath;

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
                    if(deleteAFile(fileInfos.get(adapterPosition))){
                        ToastUtils.showToast(mMainActivity,"已删除！",1000);
                    }
                    deleteItem(adapterPosition);
                    closeable.smoothCloseMenu();
                }

                if (menuPosition == 1 && direction == RIGHT_DIRECTION) {
                    //属性

                    closeable.smoothCloseMenu();
                }
                if (menuPosition == 0 && direction == LEFT_DIRECTION) {
                    //复制逻辑
                    copyPath=fileInfos.get(adapterPosition);
//                    if(reNameFile(fileInfos.get(adapterPosition),fragmentTitle+"新文件名")){
//                        ToastUtils.showToast(mMainActivity, "重命名成功", 1000);
//                    }
                    //ToastUtils.showToast(mMainActivity, getString(R.string.copy), 1000);

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
            //LogUtils.i(getClass().getName() + "285", files.length + "");
            fileInfos = new ArrayList<>();
            for (File temp : files) {
                fileInfos.add(FileUtils.getFileInfoFromFile(temp));
            }
            try {
                Collections.sort(fileInfos, comparator);
            }catch (Exception e){
                LogUtils.e(getClass().getName(),"排序的时候产生一个错误，未知错误");
            }
            //LogUtils.i(getClass().getName() + "290", fileInfos.size() + "");
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
                //NotificationCompat.Builder builder = new NotificationCompat.Builder(mMainActivity,CHANNEL_ID );
                //NotificationUtils.setNotification(mMainActivity, builder);
//                startActionCopy(mMainActivity,"复制任务已启动","move");
//                CopyProgressDialog c1 = new CopyProgressDialog(mMainActivity,R.layout.progress_dialog_layout);
//                c1.setProgress(50);
//                c1.show();

                break;
            case 2:
                //粘贴

                File in =new File(copyPath.getFilePath());
                File to =new File(filePath,copyPath.getFileName());
                if(to.exists()){
                    ToastUtils.showToast(mMainActivity,"文件或目录已存在",1000);
                    return super.onOptionsItemSelected(item);
                }
                if(in.exists()){
                    //Intent i =new Intent(mMainActivity,ProgressActivity.class);
                    startActionCopy(mMainActivity,copyPath.getFilePath(),filePath);
                    //startActivity(i);
                }else {
                    ToastUtils.showToast(mMainActivity,"源文件不存在！",1000);
                    return super.onOptionsItemSelected(item);
                }



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
                CopyProgressDialog c1 = new CopyProgressDialog(mMainActivity,R.layout.progress_dialog_layout);
                c1.show();
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
