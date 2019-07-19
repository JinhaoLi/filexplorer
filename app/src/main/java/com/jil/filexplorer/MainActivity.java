package com.jil.filexplorer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import androidx.appcompat.app.ActionBar;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import com.google.android.material.navigation.NavigationView;
import com.jil.filexplorer.customView.ClearActivity;
import com.jil.filexplorer.ui.FileViewFragment;
import com.jil.filexplorer.utils.ToastUtils;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import java.io.File;
import java.util.Objects;


public class MainActivity extends ClearActivity implements NavigationView.OnNavigationItemSelectedListener {
    //路径输入
    private EditText editText;
    //actionBar 图标资源
    private int mIconRes;
    private FileViewFragment pageFragment;
    //private FragmentManager fragmentManager;
    private String mPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void initAction() {
        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                editText.setFocusable(true);
                editText.setFocusableInTouchMode(true);
                editText.requestFocus();
                editText.setTextColor(Color.BLACK);
                return false;
            }
        });
        if(pageFragment!=null){
            refresh(pageFragment.getFilePath());
        }
       hideInput();
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_main);
        editText = (EditText) findViewById(R.id.editText);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        //assert actionBar != null;
        //actionBar.setIcon(R.mipmap.ic_launcher);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            File file =new File(editText.getText().toString());
            mPath=file.getParent();

            if(mPath!=null&& !Objects.equals(mPath, ""))
                pageFragment.load(mPath);
            else
                ToastUtils.showToast(MainActivity.this,"没有访问权限！",1100);
            //super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuUtils.addMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        /*int id = item.getItemId();
        //path.setFocusable(false);
        switch (id){
            case 1:
                ArrayList<FileInfo> deleteList =pageFragment.getFileInfos();
                for(FileInfo fileInfo:pageFragment.getFileInfos()){
                    if(fileInfo.isSelected()) deleteList.add(fileInfo);
                }

                ToastUtils.showToast(MainActivity.this,deleteList.size()+"",1000);
                break;
            case 11:
                LogUtils.i("main","sort by name");
                break;
            case 7:
                System.exit(0);
                break;
            case 4:
                pageFragment.load(editText.getText().toString());
                break;
            case 5:
                finish();
                break;

        }*/

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_ftp_net) {

        } else if (id == R.id.nav_setting) {

        } /*else if (id == R.id.nav_send) {

        }*/

        //DrawerLayout drawer = findViewById(R.id.drawer_layout);
        //drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * 点击除editText外时，editText范围失去焦点
     * @param ev
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {//点击editText控件外部
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    assert v != null;
                    hideInput();
                    if (editText != null) {
                        editText.clearFocus();
                    }
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        return getWindow().superDispatchTouchEvent(ev) || onTouchEvent(ev);
    }

    private void hideInput() {
        editText.setFocusable(false);
        editText.setTextColor(Color.WHITE);
    }

    /**
     * 关闭输入法
     * @param
     * @param
     * @return
     */
    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            editText = (EditText) v;
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            //判断点击的范围
            return !(event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom);
        }
        return false;
    }


    public void setFileViewFragment(FileViewFragment fileViewFragment) {
        this.pageFragment =fileViewFragment;
    }

    public void refresh(String path){
        if(editText !=null && path!=null && !path.equals("")){
            mPath=path;
            editText.setText(path);
            setTitle(pageFragment.getFragmentTitle());
        }

    }

    public EditText getEditText() {
        return editText;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


}
