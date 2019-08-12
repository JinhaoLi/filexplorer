package com.jil.filexplorer.Activity;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.jil.filexplorer.MainActivity;
import com.jil.filexplorer.R;

import static com.jil.filexplorer.utils.FileUtils.requestPermission;
import static com.jil.filexplorer.utils.NotificationUtils.registerNotifty;

public abstract class ClearActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.MyThemeGray);//设置主题
        //getColor(R.color.alp1);
        initView();
        initAction();
    }


    protected abstract void initView();
    protected abstract void initAction();
}
