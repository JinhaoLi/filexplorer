package com.jil.filexplorer.api;


import android.content.Context;
import android.os.Environment;
import com.jil.filexplorer.R;
import com.jil.filexplorer.activity.ProgressActivity;
import com.jil.filexplorer.adapter.FileListAdapter;
import com.jil.filexplorer.ui.CustomFragment;
import com.jil.filexplorer.utils.LogUtils;
import net.lingala.zip4j.model.ZipParameters;

import java.io.File;
import java.io.FileFilter;
import java.util.*;

import static com.jil.filexplorer.utils.ConstantUtils.*;
import static com.jil.filexplorer.utils.FileUtils.*;

public class FilePresenter implements FilePresenterCompl.IFilePresenter, MVPFramework.IModel.ResultListener<FileInfo>, FileChangeListener {
    /**
     * 页面标题
     */
    public String fragmentTitle;
    /**
     * 路径
     */
    public String path;

    public FileListAdapter tListAdapter;

    private FilePresenterCompl.IFileView fileView;

    private FileModel fileModel;

    private boolean addHistory;

    public Context mContext;

    public FilePresenter(FilePresenterCompl.IFileView fileView, Context context) {
        this.mContext = context;
        this.fileView = fileView;
        this.fileModel = new FileModel();
        tListAdapter = new FileListAdapter(this,CustomFragment.makeItemLayoutRes(SettingParam.Column));


    }

    @Override
    public void onComplete(List<FileInfo> list, String resultPath, String name) {
        tListAdapter.setmData((ArrayList<FileInfo>) list);
        this.path = resultPath;
        this.fragmentTitle = name;
        //刷新视图
        fileView.upDatePath(path);
    }

    public void deleteFileInfoItem(int position) {
        fileModel.remove(position);
    }

    public int getModelSize() {
        if (fileModel.isNoneData()) {
            return 0;
        } else
            return fileModel.size();
    }

    @Override
    public void input2Model(String path, FileFilter fileFilter, boolean addHistory) {
        this.addHistory = addHistory;
        fileModel.load(path, fileFilter, this);

    }

    @Override
    public boolean isNoneData() {
        return fileModel.isNoneData();
    }



    public void changeItemLayout() {
        tListAdapter.setItemLayoutRes(CustomFragment.makeItemLayoutRes(SettingParam.Column));
    }


    @Override
    public void onError(String msg) {
        fileView.setErr(msg);
    }

    public int getSortType() {
        return fileModel.getSortType();
    }

    public void sort(int sortType) {
        fileModel.sort(sortType);
        tListAdapter.notifyItemRangeChanged(0, fileModel.size());
    }

    public String getPath() {
        return path;
    }


    public void saveSharedPreferences(int spanCount) {
        SettingParam.saveSharedPreferences(mContext, "Column", spanCount);
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



    public void refreshUnderBar() {
        ExplorerApp.fragmentPresenter.setUnderBarMsg(getUnderBarMsg());
    }

    public void copySelectFile() {
        ExplorerApp.fragmentPresenter.fileOperationType=FileOperation.MODE_COPY;
        int[] select =fileModel.getSelectedPosition();
        ArrayList<FileInfo> fileInfos =new ArrayList<>();
        for(int i =0;i<select.length;i++){
            fileInfos.add(fileModel.getFileInfoByPosition(select[i]));
            fileModel.selectPosition(select[i],false);
            fileView.unSelectItem(select[i]);
        }
        ExplorerApp.fragmentPresenter.setPasteVisible(true);
        FileOperation.inFiles=fileInfos;
        notifyChanged();
        refreshUnderBar();



    }

    public void deleteSelectFile() {
        ArrayList<FileInfo> deleteList=fileModel.refreshMissionList(FileOperation.MODE_DELETE);
        FileOperation fileOperation;
        if(SettingParam.RecycleBin>0 && !getPath().equals(ExplorerApp.RECYCLE_PATH)){
            fileOperation=FileOperation.with(getContext()).moveToRecycleBin(deleteList).to(ExplorerApp.RECYCLE_PATH);
        }else {
            fileOperation=FileOperation.with(getContext()).delete(deleteList);
        }

        fileOperation.setFileChangeListener(this);
        ProgressActivity.start(getContext(),fileOperation.getId());
        new Thread(fileOperation).start();

    }

    public void moveSelectFile() {
        ExplorerApp.fragmentPresenter.fileOperationType=FileOperation.MODE_MOVE;
        int[] select =fileModel.getSelectedPosition();
        ArrayList<FileInfo> fileInfos =new ArrayList<>();
        for(int i =0;i<select.length;i++){
            fileInfos.add(fileModel.getFileInfoByPosition(select[i]));
            //fileModel.selectPosition(select[i],false);
            //fileView.unSelectItem(select[i]);
        }
        ExplorerApp.fragmentPresenter.setPasteVisible(true);
        FileOperation.inFiles=fileInfos;
        notifyChanged();
        refreshUnderBar();
    }

    public void reNameSelectFile(String newName){
        if(fileModel.reName(newName)){
            ExplorerApp.fragmentPresenter.showToast("success!");
        }else {
            ExplorerApp.fragmentPresenter.showToast("failure!");
        }
        tListAdapter.notifyDataSetChanged();
        unSelectAll();
        notifyChanged();
        refreshUnderBar();

    }

    public void showReNameDialog(String oldName) {
        if(fileModel.getSelectedSize()==1){
            fileView.reNameDialog("重命名",R.layout.dialog_rename_layout,oldName,FileOperation.MODE_RENAME);
        }else {
            fileView.reNameDialog("批量重命名",R.layout.dialog_renames_layout,oldName+"...",FileOperation.MODE_RENAME);
        }
    }

    public void slideToPager(String filePath) {
        ExplorerApp.fragmentPresenter.slideToPager(filePath);
    }

    @Override
    public void change() {
        fileModel.load(path, null, this);
    }


    public boolean isAllSelected() {
        return fileModel.getSelectedSize() == fileModel.size();
    }

    /**
     * 获取所有选中的item position的开始位置和结束位置
     * SelectStartAndEndPosition==SSAEP
     *
     * @return
     */
    private int[] getSelectStartAndEndPosition() {

        int[] SSAEP = new int[2];//储存位置

        if (fileModel.getSelectedSize() != 0 && fileModel.getSelectedSize() != fileModel.size()) {
            SSAEP[0] = fileModel.indexOfFirstSelect();
            SSAEP[1] = fileModel.indexOfLastSelect();
            if (SSAEP[1] - SSAEP[0] == fileModel.getSelectedSize() - 1) {
                SSAEP[0] = -1;
                SSAEP[1] = -1;
            }
        } else {
            SSAEP[0] = -1;
            SSAEP[1] = -1;
        }
        return SSAEP;
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
     *
     * @return 是全选为true
     */
    public void selectAllPositionOrNot(boolean selectAll) {
        if (selectAll) {
            selectAll();
        } else {
            unSelectAll();
        }
    }

    private void selectAll(){
        fileModel.selectAll();
        for (int i = 0; i < fileModel.size(); i++)
            fileView.selectItem(i);
    }

    private void unSelectAll() {
        int[] ints = fileModel.unSelectAll();

        for (int i = 0; i < ints.length; i++) {
            fileView.unSelectItem(ints[i]);
        }
    }

    /**
     * 将一些区间选项选中
     *
     * @param start
     * @param end
     */
    public void selectSomePosition(int start, int end) {
        if (start != -1 && start != end) {
            int s = Math.min(start, end);
            int e = Math.max(start, end);
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
        notifyChanged();
        refreshUnderBar();
    }

    public void invertSelection() {
        for (int i = 0; i < fileModel.size(); i++) {
            if (fileModel.isSelected(i))
                unSelectItem(i);
            else
                selectItem(i);
        }
        notifyChanged();
        refreshUnderBar();
    }

    public void notifyChanged() {
        fileModel.notifyChanged();
    }


    public void selectItem(int position) {
        fileModel.selectPosition(position, true);
        fileView.selectItem(position);
    }

    public void unSelectItem(int position) {
        fileModel.selectPosition(position, false);
        fileView.unSelectItem(position);

    }

    public String getUnderBarMsg() {

        int selectSize = fileModel.getSelectedSize();
        setMenuVisible(selectSize);
        long size = fileModel.getAllSelectedLength();
        boolean haveDir = fileModel.haveSelectedDir();
        String howStr = selectSize == 0 ? "" : "|\t" + getContext().getString(R.string.select) + selectSize + "" + getContext().getString(R.string.how_many_item);
        String bigStr;
        if (haveDir || selectSize == 0) {
            bigStr = "";
        } else {
            bigStr = size > GB ? stayFireNumber((float) size / GB) + "GB" + "\t\t|"
                    : size > MB ? stayFireNumber((float) size / MB) + "MB" + "\t\t|"
                    : stayFireNumber((float) size / KB) + "KB" + "\t\t|";
        }
        LogUtils.d(getClass().getName(),"getUnderBarMsg():{fileModel.size():"+fileModel.size()+"}"+"{selectSize:"+selectSize+"}");
        return "\t" + fileModel.size() + getContext().getString(R.string.how_many_item) + "\t\t" + howStr + "\t\t" + bigStr;
    }

    private void setMenuVisible(int selectSize) {
        //区间选择menu
        int[] se = getSelectStartAndEndPosition();
        if (se[1] - se[0] > 1)
            ExplorerApp.fragmentPresenter.setSelectIntervalIco(true, se[0], se[1]);
        else
            ExplorerApp.fragmentPresenter.setSelectIntervalIco(false, -1, -1);

        //文件操作menus
//        if (selectSize != 0) {
//            ExplorerApp.fragmentPresenter.setOperationGroupVisible(true);
//        } else {
//            ExplorerApp.fragmentPresenter.setOperationGroupVisible(false);
//        }
        if(isAllSelected()){
            ExplorerApp.fragmentPresenter.changeAllSelectIco(true);
        }else {
            ExplorerApp.fragmentPresenter.changeAllSelectIco(false);
        }


    }

    private Context getContext() {
        return fileView.getContext();
    }

    private String getParentName(){
        File f =new File(getPath()).getParentFile();
        if(f!=null)
            return f.getName();
        else return "null";
    }

    public void showCompressDialog() {
        fileView.showCompressDialog(getParentName());
    }

    public void compressFile(ZipParameters zipParameters, String zipName) {
        fileModel.refreshMissionList(FileOperation.MODE_COMPRESS);
        FileOperation fileOperation =FileOperation.with(getContext()).compress().to(getPath()+File.separator+zipName);
        fileOperation.setFileChangeListener(this);
        fileOperation.applyZipParameters(zipParameters);
        ProgressActivity.start(getContext(),fileOperation.getId());
        new Thread(fileOperation).start();
    }
}
