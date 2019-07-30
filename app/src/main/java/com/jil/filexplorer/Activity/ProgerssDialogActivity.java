package com.jil.filexplorer.Activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.jil.filexplorer.ui.CopyProgressDialog;
import com.jil.filexplorer.utils.CopyFileUtils;

/**
 * 使用线程CopyFileUtils复制文件时显示进度
 */
public class ProgerssDialogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CopyProgressDialog copyProgressDialog= CopyFileUtils.getCopyWindow();
        copyProgressDialog.show();
        finish();
    }

}
