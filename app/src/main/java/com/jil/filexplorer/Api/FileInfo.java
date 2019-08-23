package com.jil.filexplorer.Api;

import android.widget.Checkable;

import static com.jil.filexplorer.utils.ConstantUtils.MB;

public class FileInfo extends Item implements Checkable {
    //文件名
    private String fileName;
    //文件路径
    private String filePath;
    //文件大小
    private long fileSize=0;
    //是否文件夹
    private boolean isDir;
    //包含数量
    private int Count;
    //最后编辑时间
    private long ModifiedDate;
    //是否选中
    private boolean selected;
    //可读
    private boolean canRead;
    //可写
    private boolean canWrite;
    //隐藏
    private boolean isHidden;
    //图标
    private int icon;
    //文件后缀
    private String filetype;

    public FileInfo() {
    }

    public FileInfo(String fileName, String filePath, boolean isDir, long modifiedDate, boolean canRead, boolean canWrite, boolean isHidden) {
        super(fileName,(int)(modifiedDate/ MB));
        //this.fileName = fileName;
        this.filePath = filePath;
        this.isDir = isDir;
        ModifiedDate = modifiedDate;
        this.canRead = canRead;
        this.canWrite = canWrite;
        this.isHidden = isHidden;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getFileName() {
        //return fileName;
        return getName();
    }

    public void setFileName(String fileName) {
        //this.fileName = fileName;
        setName(fileName);
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public boolean isDir() {
        return isDir;
    }

    public void setDir(boolean dir) {
        isDir = dir;
    }

    public int getCount() {
        return Count;
    }

    public void setCount(int count) {
        Count = count;
    }

    public long getModifiedDate() {
        return ModifiedDate;
    }

    public void setModifiedDate(long modifiedDate) {
        ModifiedDate = modifiedDate;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isCanRead() {
        return canRead;
    }

    public void setCanRead(boolean canRead) {
        this.canRead = canRead;
    }

    public boolean isCanWrite() {
        return canWrite;
    }

    public void setCanWrite(boolean canWrite) {
        this.canWrite = canWrite;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean hidden) {
        isHidden = hidden;
    }

    public String getFiletype() {
            return filetype;
    }

    public void setFiletype(String filetype) {
            this.filetype = filetype;
    }

    @Override
    public void setChecked(boolean checked) {
        this.selected =checked;
    }

    @Override
    public boolean isChecked() {
        return selected;
    }

    @Override
    public void toggle() {
        this.selected =!selected;
    }
}
