package com.jil.filexplorer.activity;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.util.Util;
import com.google.android.material.navigation.NavigationView;
import com.jil.filexplorer.api.*;
import com.jil.filexplorer.R;
import com.jil.filexplorer.ui.MyItemDecoration;
import com.jil.filexplorer.ui.InputDialog;
import com.jil.filexplorer.utils.*;

import java.io.File;

import static com.jil.filexplorer.api.FileComparator.SORT_BY_DATE;
import static com.jil.filexplorer.api.FileComparator.SORT_BY_DATE_REV;
import static com.jil.filexplorer.api.FileComparator.SORT_BY_NAME;
import static com.jil.filexplorer.api.FileComparator.SORT_BY_NAME_REV;
import static com.jil.filexplorer.api.FileComparator.SORT_BY_SIZE;
import static com.jil.filexplorer.api.FileComparator.SORT_BY_SIZE_REV;
import static com.jil.filexplorer.api.FileComparator.SORT_BY_TYPE;
import static com.jil.filexplorer.api.FileComparator.SORT_BY_TYPE_REV;
import static com.jil.filexplorer.api.SettingParam.readSharedPreferences;
import static com.jil.filexplorer.utils.ConstantUtils.GIRD_LINER_LAYOUT;
import static com.jil.filexplorer.utils.ConstantUtils.NORMAL_COLOR;
import static com.jil.filexplorer.utils.FileUtils.hideMax;
import static com.jil.filexplorer.utils.FileUtils.requestPermission;
import static com.jil.filexplorer.utils.NotificationUtils.registerNotifty;


/**
 * 主页面
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        ViewPager.OnPageChangeListener, View.OnClickListener, FragmentPresenterCompl.IFragmentView {


    private EditText pathEdit;//路径输入框
    private DrawerLayout drawerLayout;
    private RecyclerView smallFragmentList;
    protected FrameLayout underBar;
    protected TextView underBarInfo;
    protected ImageView liner, grid;                        //排列方式按钮
    private TextView sortName, sortDate, sortType, sortSize; //列名
    private ViewPager fragmentPager;

    private FragmentPresenter fragmentPresenter;

    public boolean pasteVisible = false;                   //粘贴按钮可见状态
    private boolean operationGroupVisible = false;           //操作按钮可见状态

    private int refuseCount;
    public Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readSharedPreferences(this);
        registerNotifty(this);//注册通知渠道
        requestPermission(this);//动态申请权限
        //ExplorerApplication.setApplicationContext(this);
        init();
    }


    @Override
    public void update() {
       refresh(fragmentPresenter.getPath());

    }

    /**
     * 操作图标可见
     *
     * @param operationGroupVisible
     */
    public void setOperationGroupVisible(boolean operationGroupVisible) {
        this.operationGroupVisible = operationGroupVisible;
        if (menu != null) {
            menu.setGroupVisible(1, operationGroupVisible);
            menu.setGroupVisible(2, !operationGroupVisible);
        }
    }

    @Override
    public FragmentManager linkFragmentManager() {
        return getSupportFragmentManager();
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fragmentPresenter.load(fragmentPager.getCurrentItem(), pathEdit.getText().toString(), true);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuUtils.addMenu(menu);
        this.menu = menu;
        setOperationGroupVisible(operationGroupVisible);
        setSelectIntervalIco(false, -1, -1);
        setPasteVisible(pasteVisible);
        allSelectIco(fragmentPresenter.isAllSelect());
        return super.onCreateOptionsMenu(menu);
    }

    public void createNewFile() {
        InputDialog newFileOrDir = new InputDialog(this, R.layout.dialog_new_file_or_dir_layout, "生成") {
            private Button dir;

            @Override
            public void queryButtonClick(View v, String nameInputStr) {

            }

            @Override
            public void customView() {
                super.customView();
                dir = findViewById(R.id.button4);
                dir.setText("文件夹");
                dir.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                String s = nameInput.getText().toString();
                boolean isFile = false;
                switch (v.getId()) {
                    case R.id.button3:
                        isFile = true;
                        break;
                    case R.id.button4:
                        isFile = false;
                        break;
                }

                boolean successful = fragmentPresenter.createNewFile(s, isFile);
                ToastUtils.showToast(MainActivity.this, successful ? "成功" : "失败", 1000);
                dismiss();
            }

            @Override
            public void queryButtonClick(View v) {

            }

            @Override
            public void setTitle(@Nullable CharSequence title) {
                super.setTitle(title);
                query.setText("文件");

            }
        };
        newFileOrDir.showAndSetName("新项目");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //path.setFocusable(false);
        switch (id) {
            case 1:
                fragmentPresenter.copySelectFile();
                break;
            case 2:
                fragmentPresenter.pasteFileHere(this);
                break;
            case 3:
                fragmentPresenter.delecteSelectFile();
                break;
            case 4:
                //添加
                createNewFile();
                break;
            case 5:
                //剪切
                fragmentPresenter.moveSelectFile();
                break;
            case 6:
                //刷新
                fragmentPresenter.load();
                break;
            case 7:
                //close fragment
                if (fragmentPresenter.size()> 1) {
                    removeFragmentPage();
                } else {
                    finish();
                }
                break;
            case 8:
                //全选or取消全选
                fragmentPresenter.selectAllOrNot();
                break;
            case 9:
                //区间选择
                fragmentPresenter.intervalSelection();
                break;
            case 10:
                fragmentPresenter.compressFiles(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 全选menu ico
     *
     * @param isAllSelected
     */
    public void allSelectIco(boolean isAllSelected) {
        if (menu != null)
            if (isAllSelected) {
                menu.getItem(8).setIcon(R.drawable.ic_all_no_selecte_ico);
            } else {
                menu.getItem(8).setIcon(R.drawable.ic_all_selecte_ico);
            }
    }

    @Override
    public void setCurrentItem(int position) {
        fragmentPager.setCurrentItem(position);
        fragmentPresenter.current=fragmentPager.getCurrentItem();
    }

    /**
     * 区间选择 ico
     *
     * @param visible
     * @param start
     * @param end
     */
    public void setSelectIntervalIco(boolean visible, int start, int end) {
        this.fragmentPresenter.startPosition = start;
        this.fragmentPresenter.endPsoition = end;
        menu.getItem(6).setVisible(visible);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_phone:
                fragmentPresenter.slideToPager(Environment.getExternalStorageDirectory().getPath());
                break;
            case R.id.nav_gallery:
                fragmentPresenter.slideToPager(Environment.getExternalStorageDirectory().getPath() + File.separator + "Pictures");
                break;
            case R.id.nav_fast_entry:
                break;
            case R.id.nav_ftp_net:
                break;
            case R.id.nav_recycle_station:
                File bin = new File(Environment.getExternalStorageDirectory() + File.separator + "RecycleBin");
                if (!bin.exists()) {
                    bin.mkdirs();
                }
                fragmentPresenter.slideToPager(bin.getPath());
                break;
            case R.id.nav_setting:
                //setting in activity
                Intent i = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(i);
                //setting in fragment
                //fragmentPresenter.slideToPager("system:" + File.separator + "setting");
                break;
            default:
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * 打开上层目录
     *
     * @return
     */
    public boolean openParentDir() {
        if (!fragmentPresenter.getPath().equals("/") && !fragmentPresenter.getPath().equals(Environment.getExternalStorageDirectory().getPath())) {
            File file1 = new File(fragmentPresenter.getPath());
            String path = file1.getParent();
            return fragmentPresenter.load(path);
        } else {
            return false;
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }

        if (openParentDir()) return;

        if (fragmentPresenter.size() > 1) {
            removeFragmentPage();
            return;
        }
        super.onBackPressed();
    }

    /**
     * 移除当前fragment
     */
    public void removeFragmentPage() {
        int p = fragmentPager.getCurrentItem();
        fragmentPresenter.removeFragmentPage(p);

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
        while (fragmentPresenter.historyPath.size() > 10) {
            fragmentPresenter.historyPath.remove(0);
        }
        final ListPopupWindow listPopupWindow;
        listPopupWindow = new ListPopupWindow(this);
        listPopupWindow.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fragmentPresenter.historyPath));//用android内置布局，或设计自己的样式
        listPopupWindow.setAnchorView(pathEdit);//以哪个控件为基准，在该处以mEditText为基准
        listPopupWindow.setModal(true);

        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {//设置项点击监听
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                fragmentPresenter.load(fragmentPresenter.historyPath.get(i));
                listPopupWindow.dismiss();//如果已经选择了，隐藏起来

            }
        });
        listPopupWindow.show();//把ListPopWindow展示出来
    }

    /**
     * 关闭输入法
     *
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

    public void refresh(String path) {
        if (menu != null) {
            menu.getItem(8).setVisible(true);
        }

        if (pathEdit != null && path != null && !path.equals("")) {
            String s = hideMax(path, 55);
            pathEdit.setText(s);
            setTitle(fragmentPresenter.getCurrentCustomFragment().getFragmentTitle());
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        setCurrentItem(position);
        if (fragmentPresenter.getCurrentCustomFragment().getUnderBarMsg() == null
                || fragmentPresenter.getCurrentCustomFragment().getUnderBarMsg().equals("")) {
            clearUnderBar();
        } else {
            refreshUnderBar(fragmentPresenter.getCurrentCustomFragment().getUnderBarMsg());
        }
        refresh(fragmentPresenter.getCurrentCustomFragment().getPath());

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View view) {
//        if (!(this.customViewFragment instanceof FileShowFragment)) {
//            if (view.getId() == R.id.imageView5) {
//                customViewFragment.makeLinerLayout();
//                SettingParam.setColumn(1);
//                view.setBackgroundColor(GIRD_LINER_LAYOUT);
//                grid.setBackgroundColor(NORMAL_COLOR);
//            } else if (view.getId() == R.id.imageView3) {
//                int spanCount = SettingParam.Column;
//                if (spanCount <= 2) spanCount += 2;
//                SettingParam.setColumn(spanCount);
//                customViewFragment.changeView(spanCount);
//                view.setBackgroundColor(GIRD_LINER_LAYOUT);
//                liner.setBackgroundColor(NORMAL_COLOR);
//            }
//            return;
//        }
        //FileShowFragment customViewFragment = (FileShowFragment) this.customViewFragment;
        switch (view.getId()) {
            case R.id.textView2://名字排序
                if (fragmentPresenter.getSortType() == SORT_BY_NAME)
                    fragmentPresenter.sortReFresh(SORT_BY_NAME_REV);
                else
                    fragmentPresenter.sortReFresh(SORT_BY_NAME);
                break;
            case R.id.textView3://日期排序
                if (fragmentPresenter.getSortType() == SORT_BY_DATE)
                    fragmentPresenter.sortReFresh(SORT_BY_DATE_REV);
                else
                    fragmentPresenter.sortReFresh(SORT_BY_DATE);
                break;
            case R.id.textView5://大小排序
                if (fragmentPresenter.getSortType() == SORT_BY_SIZE)
                    fragmentPresenter.sortReFresh(SORT_BY_SIZE_REV);
                else
                    fragmentPresenter.sortReFresh(SORT_BY_SIZE);
                break;
            case R.id.textView4://类型排序
                if (fragmentPresenter.getSortType() == SORT_BY_TYPE)
                    fragmentPresenter.sortReFresh(SORT_BY_TYPE_REV);
                else
                    fragmentPresenter.sortReFresh(SORT_BY_TYPE);
                break;
            case R.id.imageView5://列表布局
                fragmentPresenter.makeLinerLayout();
                view.setBackgroundColor(GIRD_LINER_LAYOUT);
                grid.setBackgroundColor(NORMAL_COLOR);
                break;
            case R.id.imageView3://网格布局
                int spanCount = SettingParam.Column;
                if (spanCount <= 2) spanCount += 2;
                fragmentPresenter.changeView(spanCount);
                view.setBackgroundColor(GIRD_LINER_LAYOUT);
                liner.setBackgroundColor(NORMAL_COLOR);
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    public void clearUnderBar() {
        if (menu != null) {
            setOperationGroupVisible(false);
            allSelectIco(false);
            setSelectIntervalIco(false, -1, -1);
        } else {
            allSelectIco(fragmentPresenter.isAllSelect());
        }
        underBarInfo.setText("\t" + fragmentPresenter.getFileInfosSize() + getString(R.string.how_many_item));
    }


    @Override
    public void showToast(String msg) {
        ToastUtils.showToast(this, msg, 1000);
    }

    @Override
    public void refreshUnderBar(String msg) {
        underBarInfo.setText(msg);
    }


    @Override
    public void setErr(String msg) {

    }


    @Override
    public void setPasteVisible(boolean pasteVisible) {
        this.pasteVisible = pasteVisible;
        if (menu != null) {
            menu.getItem(3).setVisible(pasteVisible);
        }
    }

    @Override
    public String getPathFromEdit() {
        return pathEdit.getText().toString().trim();
    }

    /*public static class ActionThread extends AsyncTask<ArrayList<FileInfo>, ProgressMessage, ProgressMessage> {
        private ZipParameters zipParameters;

        public ActionThread(ZipParameters zipParameters) {
            this.zipParameters = zipParameters;
        }

        public ActionThread() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Intent intent = new Intent(MainActivity.this, ProgressActivity.class);
            startActivity(intent);
            setOnActionFinish(new ProgressActivity.OnActionFinish() {
                @Override
                public void OnRefresh() {
                    customViewFragment.refresh();
                }
            });
        }

        @Override
        protected ProgressMessage doInBackground(ArrayList<FileInfo>... inParams) {
            switch (fileOperationType) {
                case MODE_COPY:
                    fileOperation = FileOperation.with(MainActivity.this).copy(inParams[0]).to(inParams[1].get(0));
                    break;
                case MODE_MOVE:
                    fileOperation = FileOperation.with(MainActivity.this).move(inParams[0]).to(inParams[1].get(0));
                    break;
                case MODE_DELETE:
                    File bin = new File(Environment.getExternalStorageDirectory() + File.separator + "RecycleBin");
                    if (!bin.exists()) {
                        bin.mkdirs();
                    }
                    if (customViewFragment.getPath().equals(bin.getPath()) || SettingParam.RecycleBin < 0) {
                        fileOperation = FileOperation.with(MainActivity.this).delete(inParams[0]);
                    } else {
                        fileOperation = FileOperation.with(MainActivity.this).move(inParams[0]).to(getFileInfoFromFile(bin));
                    }
                    break;
                case MODE_COMPRESS:
                    fileOperation = FileOperation.with(MainActivity.this).compress(inParams[0]).applyZipParameters(zipParameters).to(inParams[1].get(0));
                    break;
            }
            fileOperation.run();
            return fileOperation.getProgressMessage();
        }

        @Override
        protected void onProgressUpdate(ProgressMessage... values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(ProgressMessage progressMessage) {
            super.onPostExecute(progressMessage);
            if (progressMessage != null && progressMessage.getProgress() == 100) {
                if (progressMessage.getmType() == MODE_COPY || progressMessage.getmType() == MODE_MOVE) {
                    customViewFragment.addMoreData(fileOperation.getInFiles());
                } else if (progressMessage.getmType() == MODE_COMPRESS) {
                    customViewFragment.addData(fileOperation.getToDir());
                } else {
                    customViewFragment.removeData(fileOperation.getInFiles());
                }
            } else {
                LogUtils.e(getClass().getName(), "获取的进度values为空");
            }
        }
    }*/

    @SuppressLint("ClickableViewAccessibility")
    public void init() {
        setContentView(R.layout.activity_main);
        fragmentPager = findViewById(R.id.fragment_pager);
        FrameLayout topBar = findViewById(R.id.top_bar);
        underBar = findViewById(R.id.under_bar);
        underBarInfo = underBar.findViewById(R.id.textView6);
        liner = underBar.findViewById(R.id.imageView5);
        grid = underBar.findViewById(R.id.imageView3);
        sortName = topBar.findViewById(R.id.textView2);
        sortDate = topBar.findViewById(R.id.textView3);
        sortSize = topBar.findViewById(R.id.textView5);
        sortType = topBar.findViewById(R.id.textView4);
        pathEdit = findViewById(R.id.editText);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        smallFragmentList = findViewById(R.id.small_fragment_view_list_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerStateChanged(int newState) {//用户不可见的刷新
                super.onDrawerStateChanged(newState);
                if (newState == 1) {
                    fragmentPresenter.smallViewLoadOrRefresh();
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {//用户可见的刷新
                super.onDrawerOpened(drawerView);
//                if (drawerView.getId() == R.id.left_fragment_show) {
//                    smallViewLoadOrRefresh();
//                }
            }
        };
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        liner.setOnClickListener(this);
        grid.setOnClickListener(this);
        sortName.setOnClickListener(this);
        sortDate.setOnClickListener(this);
        sortSize.setOnClickListener(this);
        sortType.setOnClickListener(this);

        Intent shortCutIntent = getIntent();
        String path = shortCutIntent.getStringExtra("file_path");
        LogUtils.i(getClass().getName(),"path:"+path);
        fragmentPresenter=FragmentPresenter.getInstance(this,path);
        fragmentPager.setAdapter(fragmentPresenter.fragmentAdapter);
        fragmentPager.setCurrentItem(0);
        fragmentPager.addOnPageChangeListener(this);

        fragmentPresenter.initSmallView();
        smallFragmentList.setLayoutManager(fragmentPresenter.linearLayoutManager);
        smallFragmentList.addItemDecoration(new MyItemDecoration(this, 1));
        smallFragmentList.setAdapter(fragmentPresenter.smallFragmentViewAdapter);

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

        hideInput();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String path = intent.getStringExtra("file_path");
        if (path != null && !path.equals("")) {
            fragmentPresenter.slideToPager(path);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
