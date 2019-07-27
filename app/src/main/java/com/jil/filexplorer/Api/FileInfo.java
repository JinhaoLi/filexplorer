package com.jil.filexplorer.Api;

public class FileInfo {
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
    private boolean Selected;
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

    public FileInfo(String fileName, String filePath,boolean isDir,long modifiedDate, boolean canRead, boolean canWrite, boolean isHidden) {
        this.fileName = fileName;
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
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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
        return Selected;
    }

    public void setSelected(boolean selected) {
        Selected = selected;
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
}
