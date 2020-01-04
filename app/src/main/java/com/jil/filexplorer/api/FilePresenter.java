package com.jil.filexplorer.api;


import android.content.Context;
import android.view.View;
import com.jil.filexplorer.R;
import com.jil.filexplorer.adapter.FileListAdapter;
import com.jil.filexplorer.ui.CustomFragment;
import com.jil.filexplorer.utils.FileUtils;
import org.json.JSONObject;

import java.io.FileFilter;
import java.util.*;

import static com.jil.filexplorer.utils.ConstantUtils.*;
import static com.jil.filexplorer.utils.FileUtils.stayFrieNumber;

public class  FilePresenter implements FilePresenterCompl.IFilePresenter, MVPFramework.IModel.ResultListener<FileInfo>, FileChangeListenter {
    /**
     * 页面标题
     */
    public String fragmentTitle;
    /**
     * 路径
     */
    public String path;
    /**
     * 底部条
     */
    private String underBarMessage;
    //网格布局计数
    public FileListAdapter tListAdapter;
    private FilePresenterCompl.IFileView fileView;
    private FileModel fileModel;
    private boolean addHistory;
    public Context mContext;

    public FilePresenter(FilePresenterCompl.IFileView fileView, Context context) {
        this.mContext=context;
        this.fileView = fileView;
        this.fileModel = new FileModel();

    }

    public void deleteFileInfoItem(int position) {
        fileModel.remove(position);
    }

    public int getFileInfosSize() {
        if(fileModel.isNoneData()){
            return 0;
        }else
            return fileModel.size();
    }

    @Override
    public void input2Model(String path, FileFilter fileFilter,boolean addHistory) {
        this.addHistory =addHistory;
        fileModel.load(path,fileFilter,this);
    }

    @Override
    public boolean isNoneData() {
        return fileModel.isNoneData();
    }

    @Override
    public void onComplete(List<FileInfo> list,String resultPath,String name) {
        if(tListAdapter ==null){
            tListAdapter=new FileListAdapter((ArrayList<FileInfo>) list,this, CustomFragment.makeItemLayoutRes(SettingParam.Column));
            fileView.init();
        }else {
            tListAdapter.setmData((ArrayList<FileInfo>) list);
        }

        this.path=resultPath;
        this.fragmentTitle=name;
        //刷新视图
        fileView.upDatePath(path);
    }

    public void changeItemLayout(){
        tListAdapter.setItemLayoutRes(CustomFragment.makeItemLayoutRes(SettingParam.Column));
    }



    @Override
    public void onError(String msg) {
        fileView.setErr(msg);
    }

//    public ArrayList<FileInfo> getData(){
//        return fileModel.aBunchOfData;
//    }

    public int getSortType(){
        return fileModel.getSortType();
    }

    public void sort(int sortType){
        fileModel.sort(sortType);
        tListAdapter.notifyItemRangeChanged(0,fileModel.size());
    }

    public String getPath() {
        return path;
    }



    public void saveSharedPreferences(int spanCount) {
        SettingParam.saveSharedPreferences(mContext,"Column",spanCount);
    }

    public boolean isAddHistory() {
        return addHistory;
    }

    public void removeDates(ArrayList<FileInfo> deleteList) {
        fileModel.remove(deleteList);
        tListAdapter.notifyDataSetChanged();
    }

    public void addDate(FileInfo fileInfo) {
        tListAdapter.addData(fileInfo);
        tListAdapter.notifyDataSetChanged();
    }

    public void addMoreData(ArrayList<FileInfo> inFiles) {
        tListAdapter.addMoreData(inFiles);
        tListAdapter.notifyDataSetChanged();
    }

    public void copySelectFile() {
        JSONObject miss =new JSONObject();
       // miss.put("missionType",FileOperation.MODE_COPY);
       // fileModel.mission();
    }

    public void refreshUnderBar() {
        ExplorerApplication.fragmentPresenter.setUnderBarMsg(getUnderBarMsg());
    }

    public void delecteSelectFile() {

    }

    public void moveSelectFile() {

    }

    public void slideToPager(String filePath) {

    }

    @Override
    public void change() {
        tListAdapter.notifyDataSetChanged();
    }


    public boolean isAllSelected() {
        return fileModel.getSelectedSize()==fileModel.size();
    }

    /**
     * 获取所有选中的item position的开始位置和结束位置
     *
     * @return
     */
    private int[] getSelectStartAndEndPosition() {

        int[] SelectStartAndEndPosition = new int[2];//储存位置

        if (fileModel.getSelectedSize() != 0) {
            SelectStartAndEndPosition[0] = fileModel.indexOfFirstSelect();
            SelectStartAndEndPosition[1] = fileModel.indexOfLastSelect();
        } else {
            SelectStartAndEndPosition[0] = -1;
            SelectStartAndEndPosition[1] = -1;
        }
        return SelectStartAndEndPosition;
    }



    public void unSelectAll(){
        int[] ints=fileModel.unSelectAll();

        for (int i =0;i<ints.length;i++){
                fileView.unSelectItem(ints[i]);
        }
    }

    /**
     * 获取所有选中的item position
     *
     * @return
     */
    public int[] getSelectedPosition() {
        return fileModel.getSelectedPosition();
    }

    /**
     * 全选or不全选
     * @return 是全选为true
     */
    public void selectAllPositionOrNot(boolean selectAll) {
        if (selectAll) {
            fileModel.selectAll();
            for(int i =0;i<fileModel.size();i++)
                fileView.selectItem(i);
        } else {
            unSelectAll();
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
            if (!fileModel.isSelected(end)) {
                for (int i = s; i <= e; i++) {
                    unSelectItem(i);
                }
            } else {
                for (int i = s; i <= e; i++) {
                    selectItem(i);
                }
            }
        }

    }


    public void selectItem(int position) {
        fileModel.selectPosition(position,true);
        fileView.selectItem(position);
        refreshUnderBar();
    }

    public void unSelectItem(int position){
        fileModel.selectPosition(position,false);
        fileView.unSelectItem(position);
        refreshUnderBar();
    }

    public String getUnderBarMsg(){
        int selectSize =fileModel.getSelectedSize();

        long size =fileModel.getAllSelectedLength();
        boolean haveDir =fileModel.haveSelectedDir();
        String howStr = selectSize == 0 ? "" : "|\t" +getContext().getString(R.string.select) + selectSize + "" + getContext().getString(R.string.how_many_item);
        String bigStr;
        if (haveDir || selectSize == 0) {
            bigStr = "";
        } else {
            bigStr = size > GB ? stayFrieNumber((float) size / GB) + "GB" + "\t\t|"
                    : size > MB ? stayFrieNumber((float) size / MB) + "MB" + "\t\t|"
                    : stayFrieNumber((float) size / KB) + "KB" + "\t\t|";
        }
        if (selectSize != 0) {
            ExplorerApplication.fragmentPresenter.setOperationGroupVisible(true);
        } else {
            ExplorerApplication.fragmentPresenter.setOperationGroupVisible(false);
        }
        return "\t" + fileModel.size() + getContext().getString(R.string.how_many_item) + "\t\t" + howStr + "\t\t" + bigStr;
    }

    private Context getContext() {
        return fileView.getContext();
    }
}
