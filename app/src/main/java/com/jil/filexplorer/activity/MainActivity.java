package com.jil.filexplorer.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.*;
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
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
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
import static com.jil.filexplorer.utils.ConstantUtils.GIRD_LINER_LAYOUT;
import static com.jil.filexplorer.utils.ConstantUtils.NORMAL_COLOR;
import static com.jil.filexplorer.utils.FileUtils.hideMax;
import static com.jil.filexplorer.utils.FileUtils.requestPermission;


/**
 * 主页面
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        ViewPager.OnPageChangeListener, View.OnClickListener, FragmentPresenterCompl.IFragmentView {

    public static final String INTENT_INPUT_PATH = "file_path";
    private EditText pathEdit;                              //路径输入框
    private DrawerLayout drawerLayout;
    private TextView underInfoBar;
    private ImageView liner, grid;                        //排列方式按钮
    private ViewPager fragmentPager;

    private FragmentPresenter fragmentPresenter;

    public boolean pasteVisible = false;                   //粘贴按钮可见状态
    private boolean operationGroupVisible = false;           //操作按钮可见状态

    private int refuseCount;
    public Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ExplorerApp.setApplicationContext(this);
        requestPermission(this);//动态申请权限
        init();
    }


    @Override
    public void update() {
        if (pathEdit != null && fragmentPresenter.getPath() != null && !fragmentPresenter.getPath().equals("")) {
            String s = hideMax(fragmentPresenter.getPath(), 55);
            pathEdit.setText(s);
            setTitle(fragmentPresenter.getCurrentCustomFragment().getFragmentTitle());
        }
    }

    /**
     * 操作图标可见
     *
     * @param operationGroupVisible
     */
    public void setOperationGroupVisible(boolean operationGroupVisible) {
        this.operationGroupVisible = operationGroupVisible;
        if (menu != null) {
            menu.setGroupVisible(1, false);
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
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fragmentPresenter.refresh(fragmentPager.getCurrentItem(), pathEdit.getText().toString(), true);
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
        fragmentPresenter.setSelectIntervalIco(false, -1, -1);
        setPasteVisible(pasteVisible);
        changeAllSelectIco(false);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case 2:
                fragmentPresenter.pasteFileHere(this);
                break;
            case 4:
                //添加
                createNewFile();
                break;
            case 6:
                //刷新
                fragmentPresenter.refresh();
                break;
            case 7:
                //close fragment
                removeFragmentPage();
                break;
            case 8:
                //全选or取消全选
                fragmentPresenter.selectAllOrNot();
                break;
            case 9:
                //区间选择
                fragmentPresenter.intervalSelection();
                break;
        }
        return super.onOptionsItemSelected(item);
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
                String name = nameInput.getText().toString();
                boolean successful;
                if(v.getId()==R.id.button3){
                    successful = fragmentPresenter.createNewFile(name, true);
                }else if(v.getId()==R.id.button4){
                    successful = fragmentPresenter.createNewFile(name, false);
                }else {
                    dismiss();
                    return;
                }
                if(!successful){
                    ToastUtils.showToast(MainActivity.this, successful ? "成功" : "失败", 1000);
                }else
                    dismiss();
            }

            @Override
            public void queryButtonClick(View v) { }

            @Override
            public void setTitle(@Nullable CharSequence title) {
                super.setTitle(title);
                query.setText("文件");

            }
        };
        newFileOrDir.showAndSetName("新建项目");
    }

    /**
     * 全选menu ico
     *
     * @param isAllSelected
     */
    public void changeAllSelectIco(boolean isAllSelected) {
        if (menu != null)
            if (isAllSelected) {
                menu.getItem(2).setIcon(R.drawable.ic_all_no_selecte_ico);
            } else {
                menu.getItem(2).setIcon(R.drawable.ic_all_selecte_ico);
            }
    }

    @Override
    public void setCurrentItem(int position) {
        fragmentPager.setCurrentItem(position);
        fragmentPresenter.current = fragmentPager.getCurrentItem();
    }

    /**
     * 区间选择 ico
     *
     * @param visible
     */
    public void setSelectIntervalIco(boolean visible) {
        menu.getItem(1).setVisible(visible);
    }

    @Override
    public void exit() {
        finish();
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
                File bin = new File(ExplorerApp.RECYCLE_PATH);
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
        if(fragmentPresenter.size()==1){
            finish();
            return;
        }
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

    private void showListPopupWindow() {
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
    private boolean isShouldHideInput(View v, MotionEvent event) {
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

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

    @Override
    public void onPageSelected(int position) {
        setCurrentItem(position);
        refreshUnderBar(fragmentPresenter.getCurrentCustomFragment().getUnderBarMsg());
        update();


    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onClick(View view) {
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
                if (spanCount < 3)
                    spanCount = 6;
                fragmentPresenter.changeView(spanCount);
                view.setBackgroundColor(GIRD_LINER_LAYOUT);
                liner.setBackgroundColor(NORMAL_COLOR);
                break;
        }
    }


    @Override
    public void showToast(String msg) {
        ToastUtils.showToast(this, msg, 1000);
    }

    @Override
    public void refreshUnderBar(String msg) {
        underInfoBar.setText(msg);
    }

    @Override
    public void setErr(String msg) {

    }

    @Override
    public void setPasteVisible(boolean pasteVisible) {
        this.pasteVisible = pasteVisible;
        if (menu != null) {
            menu.getItem(0).setVisible(pasteVisible);
        }
    }

    @Override
    public String getPathFromEdit() {
        return pathEdit.getText().toString().trim();
    }

    public void init() {
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setupWindowAnimations();
        }
        fragmentPager = findViewById(R.id.fragment_pager);

        FrameLayout underBar = findViewById(R.id.under_bar);
        underInfoBar = underBar.findViewById(R.id.textView6);

        //布局切换按钮
        liner = underBar.findViewById(R.id.imageView5);
        grid = underBar.findViewById(R.id.imageView3);

        //排序按钮
        FrameLayout topBar = findViewById(R.id.top_bar);
        TextView sortName = topBar.findViewById(R.id.textView2);
        TextView sortDate = topBar.findViewById(R.id.textView3);
        TextView sortSize = topBar.findViewById(R.id.textView5);
        TextView sortType = topBar.findViewById(R.id.textView4);

        pathEdit = findViewById(R.id.editText);
        pathEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                fragmentPresenter.load(pathEdit.getText().toString().trim());
                hideInput();
                return true;
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //fragment缩略图
        drawerLayout = findViewById(R.id.drawer_layout);
        RecyclerView smallFragmentList = findViewById(R.id.small_fragment_view_list_layout);

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
            }
        };
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        liner.setOnClickListener(this);
        grid.setOnClickListener(this);
        sortName.setOnClickListener(this);
        sortDate.setOnClickListener(this);
        sortSize.setOnClickListener(this);
        sortType.setOnClickListener(this);

        Intent shortCutIntent = getIntent();
        String path = shortCutIntent.getStringExtra(INTENT_INPUT_PATH);

        fragmentPresenter = FragmentPresenter.getInstance(this, path);
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
                showListPopupWindow();
                pathEdit.setFocusable(true);
                pathEdit.selectAll();
                pathEdit.setFocusableInTouchMode(true);
                pathEdit.requestFocus();
                pathEdit.setTextColor(Color.BLACK);
            }
        });

        setOnTouchListener(new View.OnTouchListener() {
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setupWindowAnimations() {

        //爆炸效果进入进出
//        Explode explodeTransition = new Explode();
//        explodeTransition.setDuration(300);
//        //排除状态栏
//        explodeTransition.excludeTarget(android.R.id.statusBarBackground, true);
//        //是否同时执行
//        getWindow().setAllowEnterTransitionOverlap(false);
//        getWindow().setAllowReturnTransitionOverlap(false);
//        //进入
//        getWindow().setEnterTransition(explodeTransition);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setOnTouchListener(View.OnTouchListener onTouchListener) {
        pathEdit.setOnTouchListener(onTouchListener);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        LogUtils.i(getClass().getName(), fragmentPresenter.toString());
        super.onNewIntent(intent);
        String path = intent.getStringExtra(INTENT_INPUT_PATH);

        if (path != null && !path.equals("")) {
//            if (fragmentPresenter == null) {
//                FragmentPresenter.getInstance(this, path);
//            }
            fragmentPresenter.slideToPager(path);
        }
    }
}
