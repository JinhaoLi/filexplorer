package com.jil.filexplorer.Api;

import java.util.ArrayList;

public interface MVPView {
    String getFilePath();
    String getInputPath();
    void refresh(ArrayList<FileInfo> fileInfos);
    void showToast(String msg);
}
