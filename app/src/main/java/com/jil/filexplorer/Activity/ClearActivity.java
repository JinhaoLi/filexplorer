package com.jil.filexplorer.Activity;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.jil.filexplorer.Api.SettingParam;
import com.jil.filexplorer.R;
import static com.jil.filexplorer.Api.SettingParam.readSharedPreferences;
import static com.jil.filexplorer.utils.FileUtils.RootCommand;
import static com.jil.filexplorer.utils.FileUtils.requestPermission;
import static com.jil.filexplorer.utils.NotificationUtils.registerNotifty;


public abstract class ClearActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initTheme();
        readSharedPreferences(this);
        registerNotifty(this);//注册通知渠道
        requestPermission(this);//动态申请权限
        initView();
        initAction();
    }


    protected abstract void initView();
    protected abstract void initAction();

    private void initTheme() {
        if(SettingParam.Theme!=R.style.AppTheme){
            if (Build.VERSION.SDK_INT >= 23) {
                onApplyThemeResource(getTheme(), SettingParam.Theme, false);
            } else {
                setTheme(SettingParam.Theme);
            }
        }
    }

    private boolean getRoot(){
        String apkRoot = "chmod 777 " + getPackageCodePath();//getPackageCodePath()来获得当前应用程序对应的 apk 文件的路径
        boolean b = RootCommand(apkRoot);
        return b;
    }
}
