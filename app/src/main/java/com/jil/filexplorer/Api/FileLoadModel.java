package com.jil.filexplorer.Api;

import android.content.Context;
import android.os.Message;

import com.jil.filexplorer.R;
import com.jil.filexplorer.utils.FileUtils;
import com.jil.filexplorer.utils.LogUtils;
import com.jil.filexplorer.utils.ToastUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.jil.filexplorer.Api.SortComparator.SORT_BY_NAME;

public class FileLoadModel implements MVPModel {
    ArrayList<FileInfo> fileInfos;
    SortComparator comparator = new SortComparator(SORT_BY_NAME);
    @Override
    public List<FileInfo> load(String path, FilenameFilter filenameFilter, final onResultListener onResultListener) {
        final File file = new File(path);
        String fragmentTitle = file.getName();
        Runnable loadFile =new Runnable() {
            @Override
            public void run() {
                File[] files = file.listFiles();
                SoftReference<File[]> softReference =new SoftReference<>(files);
                fileInfos = new ArrayList<>();
                for (File temp : softReference.get()) {
                    fileInfos.add(FileUtils.getFileInfoFromFile(temp));
                }
                try {
                    Collections.sort(fileInfos, comparator);
                }catch (Exception e){
                    LogUtils.e(getClass().getName(),e.getMessage()+"排序的时候产生一个错误~%?…#*☆&℃$︿★?未知错误");
                }

                onResultListener.onComplete(fileInfos);
//                Message message =new Message();
//                message.obj =path;
//                if(isBack){
//                    message.what=18;
//                }else {
//                    message.what=83;
//                }
//                handler.sendMessage(message);

            }
        };
        if (!file.exists()) {
           // ToastUtils.showToast(mMainActivity, mMainActivity.getString(R.string.Invalid_path), 1000);
            onResultListener.onError("无效路径");
            return null;
        }
        if(!file.canRead()){
            //ToastUtils.showToast(mMainActivity, mMainActivity.getString(R.string.unable_to_access), 1000);
            onResultListener.onError("无法访问");
            return null;
        }
        new Thread(loadFile).start();
        return null;
    }
}
