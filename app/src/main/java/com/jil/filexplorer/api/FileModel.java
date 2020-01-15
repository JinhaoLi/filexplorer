package com.jil.filexplorer.api;

import com.jil.filexplorer.R;
import com.jil.filexplorer.utils.FileUtils;
import com.jil.filexplorer.utils.LogUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;

import static com.jil.filexplorer.api.ExplorerApp.ApplicationContext;
import static com.jil.filexplorer.utils.FileUtils.getFileInfoFromFile;
import static com.jil.filexplorer.utils.FileUtils.getSelectedList;

public class FileModel implements FilePresenterCompl.IFileModel {
    public String path = "null";
    private ArrayList<FileInfo> aBunchOfData = new ArrayList<>();
    private static ArrayList<FileModel> fileModels;

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

    public int getSortType() {
        return comparator.getSortType();
    }

    public void remove(int adapterPosition) {
        aBunchOfData.remove(adapterPosition);
    }

    public void remove(ArrayList<FileInfo> deleteList) {
        aBunchOfData.removeAll(deleteList);
    }

    private void remove(int[] selectPosition) {
        for (int position :
                selectPosition) {
            remove(position);
        }
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
        LogUtils.d(getClass().getName(),"notifyChanged():{selectedSize:"+selectedSize+"}");
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
    }

    /**
     * 选中 or 取消选中 单项
     * @param position
     */
    public void selectPosition(int position,boolean select){
        aBunchOfData.get(position).setSelected(select);
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

    public boolean reName(String newName) {
        boolean reNameOk=false;
        int[] selectPosition =getSelectedPosition();
        if (getSelectedSize() == 1) {
            File oldFile =new File(aBunchOfData.get(selectPosition[0]).getFilePath());
            File newFile =new File(oldFile.getParentFile(),newName);
            if(oldFile.exists()){
                reNameOk=oldFile.renameTo(newFile);
            }
            if(reNameOk) {
                remove(selectPosition[0]);
                aBunchOfData.add(selectPosition[0], getFileInfoFromFile(newFile));
            }

        } else {
            ReNameList rnl=ReNameList.getInstance(newName);
            ArrayList<String> strings=rnl.getNameList(getSelectedSize());
            for (int h =0;h<getSelectedSize();h++) {
                FileInfo fi =aBunchOfData.get(selectPosition[h]);
                File f = new File(fi.getFilePath());
                File t = new File(f.getParent(), strings.get(h));
                fi.setName(strings.get(h));
                fi.setFilePath(t.getPath());
                if (t.exists()) {
                    t = new File(f.getParent(), strings.get(h) + h);
                }
                reNameOk=f.renameTo(t);

                if(reNameOk) {
                    remove(selectPosition[h]);
                    aBunchOfData.add(selectPosition[h], getFileInfoFromFile(t));
                }
            }
        }

        return reNameOk;
    }

    public void refreshMissionList() {
        FileOperation.inFiles=getSelectedList(aBunchOfData);
    }

    public FileInfo getFileInfoByPosition(int position) {
        return aBunchOfData.get(position);
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

            if(!check(file)){
                return;
            }

            if (file.isFile()) {
                FileUtils.viewFileWithPath(ApplicationContext,file.getPath());
                file=file.getParentFile();
                if(file==null||!check(file)){
                    return;
                }
            }

            aBunchOfData.clear();

            File[] files = file.listFiles(fileFilter);

            if (files == null) {
                listener.onError(ApplicationContext.getString(R.string.unable_to_access));
                return;
            }

            for (File f : files) {
                aBunchOfData.add(FileUtils.getFileInfoFromFile(f));
            }
            Collections.sort(aBunchOfData,comparator);
            listener.onComplete(aBunchOfData, file.getPath(), file.getName());
            notifyChanged();
        }

        private boolean check(File file){
            if (!file.exists()) {
                listener.onError(ApplicationContext.getString(R.string.Invalid_path));
                return false;
            }
            if (!file.canRead()) {
                listener.onError(ApplicationContext.getString(R.string.unable_to_access));
                return false;
            }
            return true;
        }
    }



}
