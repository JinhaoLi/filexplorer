package com.jil.filexplorer.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.jil.filexplorer.R;

public abstract class ClearActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.MyTheme);//设置主题
        //getColor(R.color.alp1);
        initView();
        initAction();
    }

    protected abstract void initView();
    protected abstract void initAction();
}
