package com.jil.filexplorer.Api;

import java.util.ArrayList;
import java.util.List;

public class FilePersenter {
    MVPView mMVPView;
    MVPModel mvpModel =new FileLoadModel();

    public FilePersenter(MVPView mMVPView) {
        this.mMVPView = mMVPView;
    }

    public void start(){
        mvpModel.load(mMVPView.getFilePath(), null, new MVPModel.onResultListener() {
            @Override
            public void onComplete(List<FileInfo> list) {
                mMVPView.refresh((ArrayList<FileInfo>) list);
            }

            @Override
            public void onError(String msg) {
                mMVPView.showToast("加载失败");
            }
        });
    }
}
