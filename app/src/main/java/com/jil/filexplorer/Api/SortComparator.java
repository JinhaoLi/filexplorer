package com.jil.filexplorer.Api;

import com.jil.filexplorer.utils.LogUtils;

import java.util.Comparator;


/**
 * 排序
 */
public class SortComparator implements Comparator<FileInfo> {

    @Override
    public int compare(FileInfo fileInfo, FileInfo t1) {
        return sortWithDoule(sortType,fileInfo,t1);

    }

    public static final int SORT_BY_NAME = 12;
    public static final int SORT_BY_SIZE = 34;
    public static final int SORT_BY_DATE = 56;
    public static final int SORT_BY_TYPE = 78;
    public static final int SORT_BY_NAME_REV = 120;
    public static final int SORT_BY_SIZE_REV = 340;
    public static final int SORT_BY_DATE_REV = 560;
    public static final int SORT_BY_TYPE_REV = 780;

    private int sortType;

    public SortComparator(int sortType) {
        this.sortType = sortType;
    }

    private static int sortTypeMenth(int sortType, FileInfo f1, FileInfo f2){
        String s1 =f1.getFileName().toLowerCase();
        String s2 =f2.getFileName().toLowerCase();
        switch (sortType){
            case SORT_BY_NAME:
                return s1.compareTo(s2);
            case SORT_BY_SIZE:
                return (int) (f1.getFileSize()-f2.getFileSize());
            case SORT_BY_DATE:
                if(f1.getModifiedDate()==f2.getModifiedDate()) return 0;
                return (int) (f1.getModifiedDate()-f2.getModifiedDate());
            case SORT_BY_TYPE:
                if (f1.getFiletype()==null||f2.getFiletype()==null){
                    return 1;
                }
                return f1.getFiletype().compareTo(f2.getFiletype());
            case SORT_BY_NAME_REV:
                return s2.compareTo(s1);
            case SORT_BY_SIZE_REV:
                return (int) (f2.getFileSize()-f1.getFileSize());
            case SORT_BY_DATE_REV:
                if(f1.getModifiedDate()==f2.getModifiedDate()) return 0;
                return (int) (f2.getModifiedDate()-f1.getModifiedDate());
            case SORT_BY_TYPE_REV:
                if (f1.getFiletype()==null||f2.getFiletype()==null){
                    return 1;
                }
                return f2.getFiletype().compareTo(f1.getFiletype());
            default:
                return 1;

        }
    }

    private static int sortWithDoule(int sortType,FileInfo f1,FileInfo f2){
        int f1_isDir = f1.isDir() ? 10:1;
        int f2_isDir = f2.isDir() ? 10:1;
        if(f1_isDir!=f2_isDir){
            return f2_isDir-f1_isDir;
        }else{
            return sortTypeMenth(sortType,f1,f2);
        }
    }


    public void setSortType(int sortType) {
        LogUtils.d("setSortTypeIN",this.sortType+"___"+sortType);
        if(this.sortType==sortType/10){
            this.sortType = sortType;
        }else if (this.sortType==sortType*10){
            this.sortType = sortType;
        }else if(this.sortType==sortType){
            if(this.sortType>100){
                this.sortType=sortType/10;
            }else {
                this.sortType=sortType*10;
            }
        }else {
            this.sortType=sortType;
        }
        LogUtils.d("setSortTypeOUT",this.sortType+"___"+sortType);
    }

    public int getSortType() {
        return sortType;
    }
}
