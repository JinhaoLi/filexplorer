package com.jil.filexplorer;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.navigation.NavigationView;
import com.jil.filexplorer.Activity.ClearActivity;
import com.jil.filexplorer.Activity.ProgressActivity;
import com.jil.filexplorer.Api.ActivityManager;
import com.jil.filexplorer.Api.FileInfo;
import com.jil.filexplorer.Api.FileOperation;
import com.jil.filexplorer.Api.ProgressMessage;
import com.jil.filexplorer.Api.SettingParam;
import com.jil.filexplorer.adapter.FragmentAdapter;
import com.jil.filexplorer.ui.CustomViewFragment;
import com.jil.filexplorer.ui.FileShowFragment;
import com.jil.filexplorer.utils.LogUtils;
import com.jil.filexplorer.utils.MenuUtils;
import com.jil.filexplorer.utils.ToastUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.jil.filexplorer.Activity.ProgressActivity.setOnActionFinish;
import static com.jil.filexplorer.Api.FileOperation.MODE_COPY;
import static com.jil.filexplorer.Api.FileOperation.MODE_DELETE;
import static com.jil.filexplorer.Api.FileOperation.MODE_MOVE;
import static com.jil.filexplorer.Api.SettingParam.readSharedPreferences;
import static com.jil.filexplorer.Api.SortComparator.SORT_BY_DATE;
import static com.jil.filexplorer.Api.SortComparator.SORT_BY_DATE_REV;
import static com.jil.filexplorer.Api.SortComparator.SORT_BY_NAME;
import static com.jil.filexplorer.Api.SortComparator.SORT_BY_NAME_REV;
import static com.jil.filexplorer.Api.SortComparator.SORT_BY_SIZE;
import static com.jil.filexplorer.Api.SortComparator.SORT_BY_SIZE_REV;
import static com.jil.filexplorer.Api.SortComparator.SORT_BY_TYPE;
import static com.jil.filexplorer.Api.SortComparator.SORT_BY_TYPE_REV;
import static com.jil.filexplorer.utils.ConstantUtils.GB;
import static com.jil.filexplorer.utils.ConstantUtils.GIRD_LINER_LAYOUT;
import static com.jil.filexplorer.utils.ConstantUtils.KB;
import static com.jil.filexplorer.utils.ConstantUtils.MB;
import static com.jil.filexplorer.utils.ConstantUtils.NORMAL_COLOR;
import static com.jil.filexplorer.utils.DialogUtils.showAlertDialog;
import static com.jil.filexplorer.utils.FileUtils.getFileInfoFromFile;
import static com.jil.filexplorer.utils.FileUtils.getFileInfoFromPath;
import static com.jil.filexplorer.utils.FileUtils.requestPermission;
import static com.jil.filexplorer.utils.FileUtils.stayFrieNumber;
import static com.jil.filexplorer.utils.NotificationUtils.registerNotifty;


public class MainActivity extends ClearActivity implements NavigationView.OnNavigationItemSelectedListener,ViewPager.OnPageChangeListener,View.OnClickListener {
    //路径输入
    private EditText pathEdit;
    private LinearLayout pageRound;
    /**
     * 当前页面fragment
     */
    private FileShowFragment customViewFragment;
    private String mPath;
    private ArrayList<String> historyPath = new ArrayList<>();
    private ImageButton upDir;
    NavigationView navigationView;
    DrawerLayout drawer;
    //顶部排序栏,底部操作栏
    private FrameLayout topBar;
    protected FrameLayout underBar;
    protected TextView underBarInfo;
    //排列方式按钮
    protected ImageView liner, grid;
    //列名
    private TextView sortName, sortDate, sortType, sortSize;
    //粘贴按钮可见状态
    private boolean pasteVisible =false;
    //操作按钮可见状态
    private boolean operationGroupVisible=false;


    private ViewPager fragmentPager;
    private FragmentAdapter fragmentAdapter;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private List<CustomViewFragment> fragments;
    private int refuseCount;
    private Bundle savedInstanceState;
    private Menu menu;
    private ArrayList<FileInfo> missionList;
    public int fileOperationType;
    private FileOperation fileOperation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.savedInstanceState=savedInstanceState;
        readSharedPreferences(this);
        super.onCreate(savedInstanceState);
        //if(customViewFragment!=null)
        registerNotifty(this);//注册通知渠道
        requestPermission(this);//动态申请权限
    }

    public void setPasteVisible(boolean pasteVisible) {
        this.pasteVisible = pasteVisible;
        if(menu!=null){
            menu.getItem(3).setVisible(pasteVisible);
        }

    }

    public void setOperationGroupVisible(boolean operationGroupVisible) {
        this.operationGroupVisible = operationGroupVisible;
        if(menu!=null){
            menu.setGroupVisible(1,operationGroupVisible);
            menu.setGroupVisible(2,!operationGroupVisible);
        }
    }

    public Menu getMenu() {
        return menu;
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
        liner.setOnClickListener(this);
        grid.setOnClickListener(this);
        sortName.setOnClickListener(this);
        sortDate.setOnClickListener(this);
        sortSize.setOnClickListener(this);
        sortType.setOnClickListener(this);
        customViewFragment=new FileShowFragment(this,Environment.getExternalStorageDirectory().getPath());
        setCustomViewFragment(customViewFragment);
        ActivityManager activityManager = ActivityManager.getInstance();
        activityManager.addActivity(this);
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
//        upDir.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                File file = new File(mPath);
//                String path = file.getParent();
//                if (path != null && !path.equals(""))
//                    customViewFragment.load(path, false);
//                else ToastUtils.showToast(MainActivity.this, "空路径无法访问", 1000);
//            }
//        });
        if (customViewFragment != null) {
            refresh(customViewFragment.getFilePath());
        }
        hideInput();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuUtils.addMenu(menu);
        this.menu=menu;
        setOperationGroupVisible(operationGroupVisible);
        setPasteVisible(pasteVisible);
        setAllSelectIco(customViewFragment.allSelect);
        return super.onCreateOptionsMenu(menu);
    }

    public void deleteFile() {
        //使用AsyncTask
        if(missionList.size()>0){
            final ActionThread acthread=new ActionThread();
            acthread.execute(missionList);
        }
        clearUnderBar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //path.setFocusable(false);
        switch (id) {
            case 1:
                missionList =customViewFragment.getSelectedList();
                fileOperationType =MODE_COPY;
                setPasteVisible(true);
                customViewFragment.unSelectAll();
                break;
            case 2:
                pasteMenuClick();
                break;
            case 3:
                ArrayList<FileInfo> deleteList = customViewFragment.getSelectedList();//储存删除对象
                ArrayList<Integer> deletePosition = customViewFragment.getSelectPosition(deleteList);//储存位置
                if (deleteList.size() != 0 && deletePosition.size() != 0) {
                    customViewFragment.removeFileInfos(deleteList);
                }
                fileOperationType=MODE_DELETE;
                missionList=deleteList;
                deleteFile();
                break;
            case 4:
                //添加
                final EditText et = new EditText(this);
                AlertDialog.Builder builder =showAlertDialog(this,"请输入名字").setView(et)
                        .setPositiveButton("生成文件", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String s =et.getText().toString();
                                File f =new File(customViewFragment.getFilePath()+File.separator+s);
                                if(!f.exists()){
                                    try {
                                        f.createNewFile();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        ToastUtils.showToast(MainActivity.this,"生成文件失败",1000);
                                        return;
                                    }
                                    customViewFragment.addFileInfo(getFileInfoFromFile(f));
                                }else {
                                    ToastUtils.showToast(MainActivity.this,"生成文件失败",1000);
                                }

                            }
                        })
                        .setNegativeButton("生成文件夹", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String s =et.getText().toString();
                                File f =new File(customViewFragment.getFilePath()+File.separator+s);
                                if(!f.exists()){
                                    f.mkdir();
                                    customViewFragment.addFileInfo(getFileInfoFromFile(f));
                                }else {
                                    ToastUtils.showToast(MainActivity.this,"生成文件夹失败",1000);
                                }

                            }
                        })
                        .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog alertDialog=builder.create();
                alertDialog.show();
                //CopyProgressDialog c1 = new CopyProgressDialog(mMainActivity);
                //c1.show();
                break;
            case 5:
                //剪切
                fileOperationType =MODE_MOVE;
                missionList=customViewFragment.getSelectedList();
                setPasteVisible(true);
                customViewFragment.unSelectAll();
                break;
            case 6:
                //刷新
                String s =pathEdit.getText().toString();
                if(s.startsWith("...")){
                    customViewFragment.load();
                }else {
                    customViewFragment.load(s, false);
                }
                break;
            case 7:
                //退出
                if(fragments.size()>1){
                    removeFragmentPage();
                }else {
                    finish();
                }

                break;
            case 8:
                //全选/取消全选
                boolean isAll=customViewFragment.selectAllPosition();
                setAllSelectIco(isAll);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setAllSelectIco(boolean isAllSelected){
        if(isAllSelected){
            menu.getItem(7).setIcon(R.drawable.ic_check_box_black_24dp);
        }else {
            menu.getItem(7).setIcon(R.drawable.ic_check_box_outline_blank_black_24dp);
        }

    }

    private void pasteMenuClick() {
        //使用AsyncTask
        setPasteVisible(false);
        ArrayList<FileInfo> topath =new ArrayList<>();
        topath.add(getFileInfoFromPath(customViewFragment.getFilePath()));
        if(missionList!=null&&missionList.size()>0){
            final ActionThread workThread=new ActionThread();
            workThread.execute(missionList,topath);

        }else {
            ToastUtils.showToast(MainActivity.this,"源路径不存在",1000);
        }
    }

    public ArrayList<FileInfo> getMissionList() {
        return missionList;
    }

    public void setMissionList(ArrayList<FileInfo> missionList) {
        this.missionList = missionList;
    }

    public void addFileInfoInMissionList(FileInfo fileInfo){
        if(missionList!=null)
            missionList.clear();
        else{
           missionList=new ArrayList<>();
        }
        missionList.add(fileInfo);
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_main);
        fragments = new ArrayList<>();
        fragmentPager=findViewById(R.id.fragment_pager);
        fragmentManager = getSupportFragmentManager();

        topBar = findViewById(R.id.top_bar);
        underBar = findViewById(R.id.under_bar);
        underBarInfo = underBar.findViewById(R.id.textView6);
        liner = underBar.findViewById(R.id.imageView5);
        grid = underBar.findViewById(R.id.imageView3);
        sortName = topBar.findViewById(R.id.textView2);
        sortDate = topBar.findViewById(R.id.textView3);
        sortSize = topBar.findViewById(R.id.textView5);
        sortType = topBar.findViewById(R.id.textView4);

        pageRound = findViewById(R.id.fragment_page);
        pathEdit = (EditText) findViewById(R.id.editText);
        //upDir = findViewById(R.id.imageButton2);
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

    public void slideToPager(String path){
        int position=fragmentAdapter.findPositionByFilePath(path);
        if(position>=0){
            fragmentPager.setCurrentItem(position);
        }else {
            FileShowFragment fileShow1=new FileShowFragment(this,path);
            fragments.add(fileShow1);
            fragmentAdapter.notifyDataSetChanged();
            fragmentPager.setCurrentItem(fragments.size());
        }
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        fragmentTransaction = fragmentManager.beginTransaction();
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_phone:
                slideToPager(Environment.getExternalStorageDirectory().getPath());
                break;
            case R.id.nav_gallery:
                slideToPager(Environment.getExternalStorageDirectory().getPath()+File.separator+"Pictures");
                //FileShowFragment fileShowFragment=(FileShowFragment) fragments.get(fragmentPager.getCurrentItem());
                //fileShowFragment.load(Environment.getExternalStorageDirectory().getPath()+File.separator+"Pictures",false);
                break;
            case R.id.nav_fast_entry:
                break;
            case R.id.nav_ftp_net:
                break;
            case R.id.nav_recycle_station:
                File bin =new File(Environment.getExternalStorageDirectory()+File.separator+"RecycleBin");
                if(!bin.exists()){
                    bin.mkdirs();
                }
                customViewFragment.load(bin.getPath(), true);
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
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (!mPath.equals("/") && !mPath.equals(Environment.getExternalStorageDirectory().getPath())) {
            File file1 = new File(mPath);
            String path = file1.getParent();
            customViewFragment.load(path, true);
        } else if(fragments.size()>1){
            for(Fragment fragment:fragments){
                FileShowFragment f=(FileShowFragment) fragment;
                LogUtils.i("当前的界面有",f.getFilePath());
            }
            removeFragmentPage();
        }else {
            super.onBackPressed();
        }
    }

    /**
     * 移除当前fragment
     */
    public void removeFragmentPage(){
        int p=fragmentPager.getCurrentItem();

        if(p==0){
            fragmentPager.setCurrentItem(p+1);
        }else {
            fragmentPager.setCurrentItem(p-1);
        }
        LogUtils.i("销毁第"+(p+1)+"个",fragments.get(p)+"");
        fragmentAdapter.removePager(p);
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
        listPopupWindow.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, historyPath));//用android内置布局，或设计自己的样式
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
        //this.customViewFragment = customViewFragment;
        //fragmentManager.putFragment(savedInstanceState,customViewFragment.getFragmentTitle(),customViewFragment);
        if(fragments==null){
            //fragments=fragmentManager.getFragments();
            fragments=new ArrayList<>();
        }
        fragments.add(customViewFragment);
        fragmentAdapter =new FragmentAdapter(fragmentManager,fragments,this,savedInstanceState);
        fragmentPager.setAdapter(fragmentAdapter);
        fragmentPager.setCurrentItem(0);
        fragmentPager.addOnPageChangeListener(this);

    }

    public void refresh(String path) {
        if (pathEdit != null && path != null && !path.equals("")) {
            mPath = path;
            String s;
            if(path.length()>55){
                s ="..."+path.substring(path.length()-50);
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
    protected void onResume() {
        super.onResume();
        //customViewFragment.load(mPath,true);
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        fragmentPager.setCurrentItem(position);
        customViewFragment=(FileShowFragment)fragments.get(position);
        if(customViewFragment.getUnderBarInfos()==null||customViewFragment.getUnderBarInfos().equals("")){
            clearUnderBar();
        }else {
            refreshUnderBar();
        }
        refresh(customViewFragment.getFilePath());

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.textView2:
                if (customViewFragment.getSortType() == SORT_BY_NAME)
                    customViewFragment.sortReFresh(SORT_BY_NAME_REV);
                else
                    customViewFragment.sortReFresh(SORT_BY_NAME);
                break;
            case R.id.textView3:
                if (customViewFragment.getSortType() == SORT_BY_DATE)
                    customViewFragment.sortReFresh(SORT_BY_DATE_REV);
                else
                    customViewFragment.sortReFresh(SORT_BY_DATE);
                break;
            case R.id.textView5:
                if (customViewFragment.getSortType() == SORT_BY_SIZE)
                    customViewFragment.sortReFresh(SORT_BY_SIZE_REV);
                else
                    customViewFragment.sortReFresh(SORT_BY_SIZE);
                break;
            case R.id.textView4:
                if (customViewFragment.getSortType() == SORT_BY_TYPE)
                    customViewFragment.sortReFresh(SORT_BY_TYPE_REV);
                else
                    customViewFragment.sortReFresh(SORT_BY_TYPE);
                break;
            case R.id.imageView5:
                customViewFragment.makeLinerLayout();
                view.setBackgroundColor(GIRD_LINER_LAYOUT);
                grid.setBackgroundColor(NORMAL_COLOR);
                break;
            case R.id.imageView3:
                int spanCount=SettingParam.Column;
                if(spanCount<2)spanCount++;
                customViewFragment.makeGridLayout(spanCount, CustomViewFragment.makeItemLayoutRes(spanCount));
                view.setBackgroundColor(GIRD_LINER_LAYOUT);
                liner.setBackgroundColor(NORMAL_COLOR);
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    public void clearUnderBar() {
        if(menu!=null){
            setOperationGroupVisible(false);
            setAllSelectIco(false);
        }
        underBarInfo.setText("\t"+customViewFragment.getFileInfosSize() + getString(R.string.how_many_item));
    }

    @SuppressLint("SetTextI18n")
    public void refreshUnderBar(int how, long size,boolean haveDir){
        customViewFragment.menuVisible=how>0;
        String howStr = how == 0 ? "" : "|\t"+getString(R.string.select) + how +"" + getString(R.string.how_many_item);
        String bigStr;
        if (haveDir || how == 0) {
            bigStr = "";
        } else {
            bigStr = size > GB ? stayFrieNumber((float) size / GB) + "GB"+"\t\t|"
                    : size > MB ? stayFrieNumber((float) size / MB) + "MB"+"\t\t|"
                    : stayFrieNumber((float) size / KB) + "KB"+"\t\t|";
        }
        if(how!=0){
            setOperationGroupVisible(true);
        }else {
            setOperationGroupVisible(false);
        }
        String underMsg="\t"+customViewFragment.getFileInfosSize() + getString(R.string.how_many_item) + "\t\t" + howStr + "\t\t" + bigStr;
        underBarInfo.setText(underMsg);
        customViewFragment.setUnderBarInfos(underMsg);
    }

    public void refreshUnderBar(){
        customViewFragment.refreshUnderBar();
    }

    @SuppressLint("StaticFieldLeak")
    private class ActionThread extends AsyncTask<ArrayList<FileInfo>, ProgressMessage,Integer> {
        @Override
        protected Integer doInBackground(ArrayList<FileInfo>... inParams) {
            switch (fileOperationType){
                case MODE_COPY:
                    fileOperation= FileOperation.with(MainActivity.this).copy(inParams[0]).to(inParams[1].get(0));
                    break;
                case MODE_MOVE:
                    fileOperation=FileOperation.with(MainActivity.this).move(inParams[0]).to(inParams[1].get(0));
                    //missionList.clear();
                    break;
                case MODE_DELETE:
                    File bin =new File(Environment.getExternalStorageDirectory()+File.separator+"RecycleBin");
                    if(!bin.exists()){
                        bin.mkdirs();
                    }
                    if(customViewFragment.getFilePath().equals(bin.getPath())||SettingParam.RecycleBin<0) {
                        fileOperation = FileOperation.with(MainActivity.this).delete(inParams[0]);
                    }else {
                        fileOperation = FileOperation.with(MainActivity.this).move(inParams[0]).to(getFileInfoFromFile(bin));
                    }
                    break;
            }
            if(fileOperationType!=MODE_DELETE)
                publishProgress(fileOperation.getProgressMessage());
            fileOperation.start();
            return null;
        }

        @Override
        protected void onProgressUpdate(ProgressMessage... values) {
            super.onProgressUpdate(values);
            Intent intent =new Intent(MainActivity.this, ProgressActivity.class);
            if(values[0]!=null&&values[0].getProgress()!=100) {
                startActivity(intent);
                setOnActionFinish(new ProgressActivity.OnActionFinish() {
                    @Override
                    public void OnRefresh() {
                        customViewFragment.load();
                    }
                });
            } else if(values[0]!=null&&values[0].getProgress()==100){
                //load(filePath,true);
                if(values[0].getmType()== MODE_COPY||values[0].getmType()== MODE_MOVE){
                    customViewFragment.addFileInfos(fileOperation.getInFiles());
                }else {
                    customViewFragment.removeFileInfos(fileOperation.getInFiles());
                }
            }else {

            }
        }

    }
}
