package com.jil.filexplorer.Api;

import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public interface MVPModel {
    List<FileInfo> load(String path, FilenameFilter filenameFilter,onResultListener onResultListener);

    interface onResultListener{//数据加载完成后的监听回调
        void onComplete(List<FileInfo> list);
        void onError(String msg);
    }
}
