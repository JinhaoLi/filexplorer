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
import com.jil.filexplorer.Api.SortComparator;
import com.jil.filexplorer.R;
import com.jil.filexplorer.adapter.FileListAdapter;
import com.jil.filexplorer.utils.FileUtils;
import com.jil.filexplorer.utils.LogUtils;
import com.jil.filexplorer.utils.ToastUtils;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.jil.filexplorer.Api.SettingParam.saveSharedPreferences;
import static com.jil.filexplorer.Api.SortComparator.SORT_BY_NAME;
import static com.jil.filexplorer.utils.ConstantUtils.NORMAL_COLOR;
import static com.jil.filexplorer.utils.ConstantUtils.SELECTED_COLOR;
import static com.jil.filexplorer.utils.DialogUtils.showAndMake;

public class FileShowFragment extends CustomFragment<FileInfo> implements FileChangeListenter {
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

    @SuppressLint("ValidFragment")
    public FileShowFragment(Activity activity, String filePath) {
        super(activity, filePath);
        initSort(new SortComparator(SORT_BY_NAME));
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


    @Override
    public int getFileInfosSize() {
        return 0;
    }

    @Override
    public void setUnderBarMsg(String underMsg) {

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
    public boolean isAllSelect() {
        return false;
    }

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

        mMainActivity.refresh(filePath);
        clearUnderBar();
    }


    @Override
    public void change() {
        load(path,true);
    }

    @SuppressLint("SetTextI18n")
    public int refreshUnderBar() {
        ArrayList<FileInfo> selectList = FileUtils.getSelectedList(ts);

        long size = 0L;
        boolean haveDir = false;

        for (FileInfo temp : selectList) {
            if (!temp.isDir()) {
                size += temp.getFileSize();
            } else {
                haveDir = true;
            }
        }
        if(selectList.size()==ts.size()&&ts.size()!=0){
            allSelect=true;
        }else {
            allSelect=false;
        }
        mMainActivity.setAllSelectIco(allSelect);
        int[] interval =getSelectStartAndEndPosition(selectList);
        if(interval[0]!=-1&&interval[1]!=-1){
            if(interval[1]-interval[0]>=selectList.size())
                mMainActivity.setSelectIntervalIco(true,interval[0],interval[1]);
            else
                mMainActivity.setSelectIntervalIco(false, interval[0], interval[1]);
        }

        mMainActivity.refreshUnderBar(selectList.size(),size,haveDir);
        return selectList.size();
    }

    /**
     * 获取所有选中的item position的开始位置和结束位置
     *
     * @return
     */
    private int[] getSelectStartAndEndPosition(ArrayList<FileInfo> selectList) {
        int[] SelectStartAndEndPosition = new int[2];//储存位置
        if(selectList==null||selectList.size()==0) selectList = FileUtils.getSelectedList(ts);
        if (selectList.size() != 0) {
            SelectStartAndEndPosition[0] = ts.indexOf(selectList.get(0));
            SelectStartAndEndPosition[1] = ts.indexOf(selectList.get(selectList.size() - 1));
        } else {
            SelectStartAndEndPosition[0] = -1;
            SelectStartAndEndPosition[1] = -1;
        }
        return SelectStartAndEndPosition;
    }

    public ArrayList<FileInfo> getSelectedList() {
        return FileUtils.getSelectedList(ts);
    }

    public void unSelectAll(){
        for(FileInfo fileInfo:ts){
            if(fileInfo.isSelected()){
                fileInfo.setSelected(false);
                int i =ts.indexOf(fileInfo);
                View selectItem = linearLayoutManager.findViewByPosition(i);
                if (selectItem != null)
                    selectItem.setBackgroundColor(NORMAL_COLOR);
            }
        }
        mMainActivity.setOperationGroupVisible(false);
        mMainActivity.setAllSelectIco(false);
    }

    /**
     * 获取所有选中的item position
     *
     * @param deleteList 选中的列表
     * @return
     */
    public ArrayList<Integer> getSelectPosition(ArrayList<FileInfo> deleteList) {
        ArrayList<Integer> selectPosition = new ArrayList<>();//储存位置
        for (int i = 0; i < deleteList.size(); i++) {
            FileInfo fileInfo = deleteList.get(i);
            selectPosition.add(ts.indexOf(fileInfo));
        }
        return selectPosition;
    }

    /**
     * 全选or不全选
     * @return 是全选为true
     */
    public void selectAllPositionOrNot(boolean selectAll) {
        if (selectAll) {
            for (int i = 0; i < ts.size(); i++) {
                ts.get(i).setSelected(true);
                View selectItem = linearLayoutManager.findViewByPosition(i);
                if (selectItem != null){
                    selectItem.setBackgroundColor(SELECTED_COLOR);
                }

            }
        } else {
            for (int i = 0; i < ts.size(); i++) {
                ts.get(i).setSelected(false);
                View selectItem = tList.getChildAt(i);
                if (selectItem != null){
                    selectItem.setBackgroundColor(NORMAL_COLOR);
                }

            }

        }
        refreshUnderBar();
    }

    /**
     * 将一些区间选项选中
     *
     * @param start
     * @param end
     */
    public void selectSomePosition(int start, int end) {
        if (start != -1 && start != end) {
            int s = start > end ? end : start;
            int e = start > end ? start : end;
            if (!ts.get(end).isSelected()) {
                for (int i = s; i <= e; i++) {
                    ts.get(i).setSelected(false);
                    View selectItem = linearLayoutManager.findViewByPosition(i);
                    if (selectItem != null)
                        selectItem.setBackgroundColor(NORMAL_COLOR);
                }
            } else {
                for (int i = s; i <= e; i++) {
                    ts.get(i).setSelected(true);
                    View selectItem = linearLayoutManager.findViewByPosition(i);
                    if (selectItem != null)
                        selectItem.setBackgroundColor(SELECTED_COLOR);
                }
            }
        }
        refreshUnderBar();
    }

    /**
     * 返回当前按什么方式排序
     * @return
     */
    public int getSortType(){
        SortComparator sortComparator = (SortComparator) comparator;
        return sortComparator.getSortType();
    }

//    @Override
//    public void initSort(Comparator<FileInfo> comparator) {
//        super.initSort(comparator);
//    }

    /**
     * 排序
     * @param sortType 排序方式
     */
    @Override
    public void sortReFresh(int sortType) {
        SortComparator sortComparator = (SortComparator) comparator;
        try {
            sortComparator.setSortType(sortType);
            Collections.sort(ts, sortComparator);
        } catch (Exception e) {
            sortComparator.setSortType(SORT_BY_NAME);
            Collections.sort(ts, sortComparator);
        }
        tListAdapter.notifyItemRangeChanged(0, ts.size());
    }
}
