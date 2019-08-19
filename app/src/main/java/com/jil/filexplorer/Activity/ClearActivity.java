package com.jil.filexplorer.Activity;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import com.jil.filexplorer.Api.SettingParam;
import com.jil.filexplorer.R;


public abstract class ClearActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(SettingParam.Theme!=R.style.AppTheme){
            setTheme(SettingParam.Theme);//设置主题
        }
        initView();
        initAction();
    }


    protected abstract void initView();
    protected abstract void initAction();


}
