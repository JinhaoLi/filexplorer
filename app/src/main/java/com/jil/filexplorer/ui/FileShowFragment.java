package com.jil.filexplorer.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jil.filexplorer.Activity.ImageDisplayActivity;
import com.jil.filexplorer.Api.FileChangeListenter;
import com.jil.filexplorer.Api.FileInfo;
import com.jil.filexplorer.Api.SettingParam;
import com.jil.filexplorer.R;
import com.jil.filexplorer.adapter.FileListAdapter;
import com.jil.filexplorer.utils.FileUtils;
import com.jil.filexplorer.utils.LogUtils;
import com.jil.filexplorer.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import static com.jil.filexplorer.Api.SettingParam.saveSharedPreferences;
import static com.jil.filexplorer.utils.DialogUtils.showAndMake;
import static com.jil.filexplorer.utils.FileUtils.getDistance;

public class FileShowFragment extends CustomViewFragment implements FileChangeListenter {
    @SuppressLint("HandlerLeak")
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String path =(String) msg.obj;
            if(msg.what==18){
                screenToView(path,true);
            }else {
                screenToView(path,false);
            }
        }
    };


    public FileShowFragment() {

    }

    public FileShowFragment(Activity activity, String filePath) {
        super(activity,filePath);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container) {
        //filePath = Environment.getExternalStorageDirectory().getPath();
        View v =super.initView(inflater, container);
        return v;
    }


    @Override
    protected void initAction() {
        sacleIcon();
        load(filePath, false);

    }

    /**
     * 单独任务
     * @param fileInfo
     */
    public void refreshMissionList(FileInfo fileInfo){
        mMainActivity.addFileInfoInMissionList(fileInfo);
    }



    @Override
    public void deleteItem(int adapterPosition) {
        fileInfos.remove(adapterPosition);
        fileListAdapter.notifyDataSetChanged();
        refreshUnderBar();
    }

    public void makeGridLayout(int spanCount, int layoutRes) {
        if(linearLayoutManager!=null){
            saveSharedPreferences(mMainActivity,"Column",spanCount);
        }
        linearLayoutManager = new GridLayoutManager(mMainActivity, spanCount);
        fileListAdapter = new FileListAdapter(fileInfos, this, mMainActivity, layoutRes);
        fileList.setAdapter(fileListAdapter);
        fileList.setLayoutManager(linearLayoutManager);

    }

    public void makeLinerLayout() {
        if(linearLayoutManager!=null){
            saveSharedPreferences(mMainActivity,"Column",1);
        }
        linearLayoutManager = new LinearLayoutManager(mMainActivity);
        fileListAdapter = new FileListAdapter(fileInfos, this, mMainActivity, R.layout.file_list_item_layout);
        try{
            fileList.setAdapter(fileListAdapter);
            fileList.setLayoutManager(linearLayoutManager);
        }catch (Exception e){
            LogUtils.e("页面被吃了,发生错误",e.getMessage());
        }

    }

    @Override
    protected boolean getFileListFromDir(final String filePath, final boolean isBack) {
        final File file = new File(filePath);
        Runnable loadFile =new Runnable() {
            @Override
            public void run() {
                fragmentTitle = file.getName();
                File[] files = file.listFiles();
                fileInfos = new ArrayList<>();
                for (File temp : files) {
                    fileInfos.add(FileUtils.getFileInfoFromFile(temp));
                }
                try {
                    Collections.sort(fileInfos, comparator);
                }catch (Exception e){
                    LogUtils.e(getClass().getName(),"排序的时候产生一个错误，未知错误");
                }
                Message message =new Message();
                message.obj =filePath;
                if(isBack){
                    message.what=18;
                }else {
                    message.what=83;
                }
                handler.sendMessage(message);

            }
        };
        if (!file.exists()) {
            ToastUtils.showToast(mMainActivity, "无效路径", 1000);
            return false;
        }
        if(!file.canRead()){
            ToastUtils.showToast(mMainActivity, "无法访问", 1000);
            return false;
        }
        loadFile.run();
        return false;
    }

    public void screenToView(String filePath,boolean isBack){
        if (fileListAdapter == null) {
            //第一次加载
            int i =SettingParam.Column;
            if(i<2){
                makeLinerLayout();
            }else {
                makeGridLayout(i,makeItemLayoutRes(i));
            }
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

    }


    @Override
    public void load(String filePath, boolean isBack) {
        getFileListFromDir(filePath,isBack);
        outOfFromPosition = 0;
        mMainActivity.refresh(filePath);
        clearUnderBar();
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
        showAndMake(alertDialog);
    }
    private float distance = 50000;
    @SuppressLint("ClickableViewAccessibility")
    private void sacleIcon() {
        fileList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (event.getPointerCount() == 2 && linearLayoutManager instanceof GridLayoutManager) {
                            float newDistance = getDistance(event);
                            if (newDistance > 100f && newDistance >= distance * 1.3) {
                                if (spanCount > 2) {
                                    spanCount--;
                                    makeGridLayout(spanCount, makeItemLayoutRes(spanCount));
                                }
                                distance = newDistance;
                            } else if (newDistance > 100f && newDistance <= distance / 1.3) {
                                if (spanCount < 7) {
                                    spanCount++;
                                    makeGridLayout(spanCount, makeItemLayoutRes(spanCount));
                                }
                                distance = newDistance;
                            }
                            return true;
                        }
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public void change() {
        load(filePath,true);
    }
}
