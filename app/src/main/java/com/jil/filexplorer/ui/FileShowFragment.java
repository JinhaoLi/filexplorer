package com.jil.filexplorer.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jil.filexplorer.Api.FileChangeListenter;
import com.jil.filexplorer.Api.FileInfo;
import com.jil.filexplorer.Api.SettingParam;
import com.jil.filexplorer.R;
import com.jil.filexplorer.adapter.FileListAdapter;
import com.jil.filexplorer.utils.FileUtils;
import com.jil.filexplorer.utils.LogUtils;
import com.jil.filexplorer.utils.ToastUtils;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;

import static com.jil.filexplorer.Api.SettingParam.saveSharedPreferences;
import static com.jil.filexplorer.utils.DialogUtils.showAndMake;

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
        //path = Environment.getExternalStorageDirectory().getPath();
        View v =super.initView(inflater, container);
        return v;
    }


    @Override
    protected void initAction() {
        enlargeIcon();
        load(path, false);

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
        ts.remove(adapterPosition);
        tListAdapter.notifyDataSetChanged();
        refreshUnderBar();
    }

    @Override
    protected boolean initDates(String filePath, boolean isBack) {
        return false;
    }

    public void makeGridLayout(int spanCount, int layoutRes) {
        if(linearLayoutManager!=null){
            saveSharedPreferences(mMainActivity,"Column",spanCount);
        }
        linearLayoutManager = new GridLayoutManager(mMainActivity, spanCount);
        tListAdapter = new FileListAdapter(ts, this, mMainActivity, layoutRes);
        tList.setAdapter(tListAdapter);
        tList.setLayoutManager(linearLayoutManager);

    }

    public void makeLinerLayout() {
        if(linearLayoutManager!=null){
            saveSharedPreferences(mMainActivity,"Column",1);
        }
        linearLayoutManager = new LinearLayoutManager(mMainActivity);
        tListAdapter = new FileListAdapter(ts, this, mMainActivity, R.layout.file_list_item_layout);
        try{
            tList.setAdapter(tListAdapter);
            tList.setLayoutManager(linearLayoutManager);
        }catch (Exception e){
            LogUtils.e(e.getMessage(),e.getMessage()+getString(R.string.eat_err));
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
                SoftReference<File[]> softReference =new SoftReference<>(files);
                ts = new ArrayList<>();
                for (File temp : softReference.get()) {
                    ts.add(FileUtils.getFileInfoFromFile(temp));
                }
                try {
                    Collections.sort(ts, comparator);
                }catch (Exception e){
                    LogUtils.e(getClass().getName(),e.getMessage()+getString(R.string.sort_err));
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
            ToastUtils.showToast(mMainActivity, mMainActivity.getString(R.string.Invalid_path), 1000);
            return false;
        }
        if(!file.canRead()){
            ToastUtils.showToast(mMainActivity, mMainActivity.getString(R.string.unable_to_access), 1000);
            return false;
        }
        new Thread(loadFile).start();
        return true;
    }

    private void screenToView(String filePath,boolean isBack){
        if (tListAdapter == null) {
            //第一次加载
            int column =SettingParam.Column;
            if(column<2){
                makeLinerLayout();
            }else {
                makeGridLayout(column,makeItemLayoutRes(column));
            }
            mMainActivity.getHistoryPath().add(filePath);
        } else {
            //刷新
            FileListAdapter fla= (FileListAdapter) tListAdapter;
            fla.setmData(ts);
            fla.notifyDataSetChanged();
            if (!isBack) {
                mMainActivity.getHistoryPath().add(filePath);
            }
        }
        this.path = filePath;

    }


    @Override
    public void load(String filePath, boolean isBack) {
        boolean result=getFileListFromDir(filePath,isBack);
        if(!result)return;
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
        AlertDialog alertDialog = builder.setTitle(getString(R.string.File_operations)).setMessage(getString(R.string.Determine_to_move_to) + name + "？")
                .setNegativeButton(getString(R.string.move), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ts.remove(longClickPosition);  //移除选中项
                        tListAdapter.notifyItemRemoved(longClickPosition);//通知刷新界面移除效果
                        //修正position
                        if (longClickPosition != ts.size()) {
                            tListAdapter.notifyItemRangeChanged(longClickPosition, ts.size() - longClickPosition);
                        }
                        refreshUnderBar();
                    }
                })
                .setPositiveButton(getString(R.string.cancle), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).create();
        showAndMake(alertDialog);
    }

    @Override
    public void change() {
        load(path,true);
    }
}
