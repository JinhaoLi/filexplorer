package com.jil.filexplorer.api;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import com.jil.filexplorer.activity.ProgressActivity;
import com.jil.filexplorer.R;
import com.jil.filexplorer.utils.FileUtils;
import com.jil.filexplorer.utils.LogUtils;
import net.lingala.zip4j.model.ZipParameters;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;

import static com.jil.filexplorer.api.ExplorerApplication.ApplicationContext;

public class FileModel implements FilePresenterCompl.IFileModel {
    public String path = "null";
    private ArrayList<FileInfo> aBunchOfData = new ArrayList<>();

    private int selectedSize;
    private long allSelectedLength;
    private boolean haveSelectedDir;

    private FileComparator comparator = new FileComparator(FileComparator.SORT_BY_NAME);

    public void load(String path, FileFilter fileFilter, ResultListener<FileInfo> listener) {
        this.path = path;
        Runnable loadFile = new LoadFileThread(path, fileFilter, listener);
        new Thread(loadFile).start();
    }


    @Override
    public void load() {

    }

    @Override
    public boolean isNoneData() {
        return aBunchOfData==null||aBunchOfData.size()==0;
    }

    public void sort(int sortType) {
        comparator.setSortType(sortType);
        Collections.sort(aBunchOfData, comparator);
    }

    public void sort() {
        Collections.sort(aBunchOfData,comparator);
    }

    public int getSortType() {
        return comparator.getSortType();
    }

    public void remove(int adapterPosition) {
        aBunchOfData.remove(adapterPosition);
    }



    public int size() {
        return aBunchOfData.size();
    }


    public void notifyChanged(){
        selectedSize =0;
        allSelectedLength=0;
        haveSelectedDir=false;
        for (FileInfo f :
                aBunchOfData) {
            if(f.isSelected()){
                selectedSize++;

                if(f.isDir())
                    haveSelectedDir=true;
                else
                    allSelectedLength+=f.getFileSize();
            }
        }
    }

    public int indexOfFirstSelect() {
        for(int i=0;i<size();i++){
            if(aBunchOfData.get(i).isSelected())
                return i;
        }
        return -1;
    }

    public int indexOfLastSelect() {
        for(int i=size()-1;i>=0;i--){
            if(aBunchOfData.get(i).isSelected())
                return i;
        }
        return -1;
    }

    /**
     * 取消选中
     * @return
     */
    public int[] unSelectAll() {
        int[] ints =new int[selectedSize];
        int index=0;
        for(int i=0;i<size();i++){
            if(aBunchOfData.get(i).isSelected()){
                selectPosition(i,false);
                ints[index]=i;
                index++;
            }
        }
        notifyChanged();
        return ints;
    }

    /**
     * 获取选中的position
     * @return
     */
    public int[] getSelectedPosition() {
        int[] ints =new int[selectedSize];
        int index=0;
        for(int i=0;i<size();i++){
            if(aBunchOfData.get(i).isSelected()){
                ints[index]=i;
                index++;
            }
        }
        return ints;
    }

    /**
     * 全选中
     */
    public void selectAll() {
        for (FileInfo f : aBunchOfData) {
            f.setSelected(true);
        }
        notifyChanged();
    }

    /**
     * 选中 or 取消选中 单项
     * @param position
     */
    public void selectPosition(int position,boolean select){
        aBunchOfData.get(position).setSelected(select);
        notifyChanged();
    }

    public boolean isSelected(int position) {
        return aBunchOfData.get(position).isSelected();
    }


    public boolean haveSelectedDir() {
        return haveSelectedDir;
    }

    public int getSelectedSize() {
        return selectedSize;
    }

    public long getAllSelectedLength() {
        return allSelectedLength;
    }

    public void remove(ArrayList<FileInfo> deleteList) {
        aBunchOfData.remove(deleteList);
    }

    private class LoadFileThread implements Runnable {
        private String path;
        private FileFilter fileFilter;
        private ResultListener<FileInfo> listener;

        public LoadFileThread(String path, FileFilter fileFilter, ResultListener<FileInfo> OnResultListener) {
            this.path = path;
            this.fileFilter = fileFilter;
            this.listener = OnResultListener;
        }

        @Override
        public void run() {
            File file = new File(path);
            if (!file.exists()) {
                listener.onError(ApplicationContext.getString(R.string.Invalid_path));
                return;
            }
            if (!file.canRead()) {
                listener.onError(ApplicationContext.getString(R.string.unable_to_access));
                return;
            }
            aBunchOfData.clear();

            if (!file.isDirectory()) {
                return;
            }
            File[] files = file.listFiles(fileFilter);

            if (files == null) {
                listener.onError("无法读取！");
                return;
            }

            for (File f : files) {
                aBunchOfData.add(FileUtils.getFileInfoFromFile(f));
            }
            sort();
            listener.onComplete(aBunchOfData, path, file.getName());
            notifyChanged();
        }
    }

}
