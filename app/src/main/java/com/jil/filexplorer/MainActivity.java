package com.jil.filexplorer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListPopupWindow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.jil.filexplorer.Activity.ClearActivity;
import com.jil.filexplorer.Activity.ProgressActivity;
import com.jil.filexplorer.ui.CustomViewFragment;
import com.jil.filexplorer.utils.CopyFileUtils;
import com.jil.filexplorer.utils.LogUtils;
import com.jil.filexplorer.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;

import static com.jil.filexplorer.Activity.ProgressActivity.setOnActionFinish;
import static com.jil.filexplorer.utils.FileUtils.requestPermission;
import static com.jil.filexplorer.utils.NotificationUtils.registerNotifty;


public class MainActivity extends ClearActivity implements NavigationView.OnNavigationItemSelectedListener, ProgressActivity.OnActionFinish, CopyFileUtils.CopyOverListener {
    //路径输入
    private EditText pathEdit;
    private boolean activityIsRunning;
    //actionBar 图标资源
    private int mIconRes;
    private FragmentManager fragmentManager;
    private FrameLayout pageRound;
    private CustomViewFragment customViewFragment;
    //private FragmentManager fragmentManager;
    private String mPath;
    private ArrayList<String> historyPath = new ArrayList<>();
    private ImageButton upDir;
    NavigationView navigationView;
    DrawerLayout drawer;
    FragmentTransaction fragmentTransaction;
    private ArrayList<CustomViewFragment> fragments;
    private int refuseCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerNotifty(this);//注册通知渠道
        requestPermission(this);//动态申请权限
        activityIsRunning=true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                customViewFragment.load(pathEdit.getText().toString(), true);
            } else { //拒绝权限申请
                if (refuseCount >= 2) {
                    finish();
                    return;
                }
                refuseCount++;
                requestPermission(this);//动态申请权限
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initAction() {
        ImageButton viewHistory = findViewById(R.id.imageButton3);
        viewHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showListPopulWindow();
                pathEdit.setFocusable(true);
                pathEdit.selectAll();
                pathEdit.setFocusableInTouchMode(true);
                pathEdit.requestFocus();
                pathEdit.setTextColor(Color.BLACK);
            }
        });
        pathEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                pathEdit.setFocusable(true);
                pathEdit.selectAll();
                pathEdit.setFocusableInTouchMode(true);
                pathEdit.requestFocus();
                pathEdit.setTextColor(Color.BLACK);
                return false;
            }
        });
        upDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(mPath);
                String path = file.getParent();
                if (path != null && !path.equals(""))
                    customViewFragment.load(path, false);
                else ToastUtils.showToast(MainActivity.this, "空路径无法访问", 1000);
            }
        });
        if (customViewFragment != null) {
            refresh(customViewFragment.getFilePath());
        }
        hideInput();
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_main);
        fragments = new ArrayList<>();
        fragmentManager = getSupportFragmentManager();
        pageRound = findViewById(R.id.fragment_page);
        pathEdit = (EditText) findViewById(R.id.editText);
        upDir = findViewById(R.id.imageButton2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);


    }

    private void naViewCreateMenu(int groupId, int itemId, int order, String title) {
        navigationView.getMenu().add(groupId, itemId, order, title);

    }

    private void naViewRemoveMenu(int itemId) {
        navigationView.getMenu().removeItem(itemId);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        fragmentTransaction = fragmentManager.beginTransaction();
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_phone:
                break;
            case R.id.nav_gallery:
                break;
            case R.id.nav_fast_entry:
                break;
            case R.id.nav_ftp_net:
                break;
            case R.id.nav_recycle_station:
                break;
            case R.id.nav_setting:
                Intent i =new Intent(MainActivity.this, SettingActivity.class);
                startActivity(i);
                break;
            default:

        }

        fragmentTransaction.commit();
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (!mPath.equals("/") && !mPath.equals(Environment.getExternalStorageDirectory().getPath())) {
            File file1 = new File(mPath);
            String path = file1.getParent();
            customViewFragment.load(path, true);
        } else {
            activityIsRunning=false;
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuUtils.addMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    /**
     * 点击除editText外时，editText范围失去焦点
     *
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
                    if (pathEdit != null) {
                        pathEdit.clearFocus();
                    }
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        return getWindow().superDispatchTouchEvent(ev) || onTouchEvent(ev);
    }

    private void hideInput() {
        pathEdit.setFocusable(false);
        pathEdit.setTextColor(Color.WHITE);
    }

    private void showListPopulWindow() {
        while (historyPath.size() > 10) {
            historyPath.remove(0);
        }
        final ListPopupWindow listPopupWindow;
        listPopupWindow = new ListPopupWindow(this);
        listPopupWindow.setAdapter(new ArrayAdapter<>(this, R.layout.listpopup_window_item_layout, historyPath));//用android内置布局，或设计自己的样式
        listPopupWindow.setAnchorView(pathEdit);//以哪个控件为基准，在该处以mEditText为基准
        listPopupWindow.setModal(true);

        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {//设置项点击监听
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                customViewFragment.load(historyPath.get(i), true);
                listPopupWindow.dismiss();//如果已经选择了，隐藏起来
            }
        });
        listPopupWindow.show();//把ListPopWindow展示出来
    }

    /**
     * 关闭输入法
     * @param
     * @param
     * @return
     */
    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            //pathEdit = (EditText) v;
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


    public void setCustomViewFragment(CustomViewFragment customViewFragment) {
        this.customViewFragment = customViewFragment;
    }

    public void refresh(String path) {
        if (pathEdit != null && path != null && !path.equals("")) {
            setOnActionFinish(this);
            mPath = path;
            String s;
            if(path.length()>50){
                s ="..."+path.substring(path.length()-45);
            }else {
                s=path;
            }
            pathEdit.setText(s);
            setTitle(customViewFragment.getFragmentTitle());
        }

    }

    public EditText getPathEdit() {
        return pathEdit;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public ArrayList<String> getHistoryPath() {
        return historyPath;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void OnRefresh() {
        if(activityIsRunning){
            try {
                customViewFragment.load(mPath,true);
            }catch (Exception e){
                LogUtils.e("复制任务进行中退出activity","activity已退出，无法刷新");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void updateUi() {
        if(activityIsRunning){
            try {
                customViewFragment.load(mPath,true);
            }catch (Exception e){
                LogUtils.e("复制任务进行中退出activity","activity已退出，无法刷新");
                e.printStackTrace();
            }
        }
    }
}
