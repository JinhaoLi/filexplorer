package com.jil.filexplorer.Activity;

import android.annotation.SuppressLint;
import android.content.Context;
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
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.navigation.NavigationView;
import com.jil.filexplorer.Api.ActivityManager;
import com.jil.filexplorer.Api.FileInfo;
import com.jil.filexplorer.Api.FileOperation;
import com.jil.filexplorer.Api.Item;
import com.jil.filexplorer.Api.ProgressMessage;
import com.jil.filexplorer.Api.SettingParam;
import com.jil.filexplorer.Api.MVPView;
import com.jil.filexplorer.Api.SettingFragment;
import com.jil.filexplorer.R;
import com.jil.filexplorer.adapter.FragmentAdapter;
import com.jil.filexplorer.adapter.SupperAdapter;
import com.jil.filexplorer.ui.CompressDialog;
import com.jil.filexplorer.ui.CustomFragment;
import com.jil.filexplorer.ui.FileShowFragment;
import com.jil.filexplorer.ui.MyItemDecoration;
import com.jil.filexplorer.ui.InputDialog;
import com.jil.filexplorer.utils.ConstantUtils;
import com.jil.filexplorer.utils.FileUtils;
import com.jil.filexplorer.utils.LogUtils;
import com.jil.filexplorer.utils.MenuUtils;
import com.jil.filexplorer.utils.ToastUtils;


import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.jil.filexplorer.Activity.ProgressActivity.setOnActionFinish;
import static com.jil.filexplorer.Api.FileOperation.MODE_COMPRESS;
import static com.jil.filexplorer.Api.FileOperation.MODE_COPY;
import static com.jil.filexplorer.Api.FileOperation.MODE_DELETE;
import static com.jil.filexplorer.Api.FileOperation.MODE_MOVE;
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
import static com.jil.filexplorer.utils.FileUtils.getFileInfoFromFile;
import static com.jil.filexplorer.utils.FileUtils.getFileInfoFromPath;
import static com.jil.filexplorer.utils.FileUtils.hideMax;
import static com.jil.filexplorer.utils.FileUtils.requestPermission;
import static com.jil.filexplorer.utils.FileUtils.stayFrieNumber;


public class MainActivity extends ClearActivity implements NavigationView.OnNavigationItemSelectedListener, ViewPager.OnPageChangeListener, View.OnClickListener, MVPView {
    private EditText pathEdit;                                  //路径输入框
    private CustomFragment customViewFragment;                //当前fragment
    private String mPath;                                       //当前fragment的路径
    private ArrayList<String> historyPath = new ArrayList<>();//历史路径
    private LinearLayoutManager smallFragmentView;              //预览图列表
    //private ImageButton upDir;
    private DrawerLayout drawerLayout;
    private RecyclerView smallFragmentList;
    private SupperAdapter<CustomFragment> smallFragmentViewAdapter;
    protected FrameLayout underBar;
    protected TextView underBarInfo;
    protected ImageView liner, grid;                        //排列方式按钮
    private TextView sortName, sortDate, sortType, sortSize; //列名
    private boolean pasteVisible = false;                    //粘贴按钮可见状态
    private boolean operationGroupVisible = false;            //操作按钮可见状态
    private FileOperation fileOperation;
    public int fileOperationType;
    private ViewPager fragmentPager;
    private FragmentAdapter fragmentAdapter;
    private FragmentManager fragmentManager;
    private List<CustomFragment> fragments;
    private int refuseCount;
    private Menu menu;
    private ArrayList<FileInfo> missionList;
    private int startPosition, endPsoition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setPasteVisible(boolean pasteVisible) {
        this.pasteVisible = pasteVisible;
        if (menu != null) {
            menu.getItem(3).setVisible(pasteVisible);
        }

    }

    public void setOperationGroupVisible(boolean operationGroupVisible) {
        this.operationGroupVisible = operationGroupVisible;
        if (menu != null) {
            menu.setGroupVisible(1, operationGroupVisible);
            menu.setGroupVisible(2, !operationGroupVisible);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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

    @Override
    protected void initView() {
        setContentView(R.layout.activity_main);
        fragments = new ArrayList<>();
        fragmentPager = findViewById(R.id.fragment_pager);
        fragmentManager = getSupportFragmentManager();
        //顶部排序栏,底部操作栏
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
                if(newState==1){
                    smallViewLoadOrRefresh();
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
        Intent shortCutIntent = getIntent();
        String path = shortCutIntent.getStringExtra("file_path");
        if (path != null && !path.equals("")) {
            customViewFragment = new FileShowFragment(this, path);
        } else {
            customViewFragment = new FileShowFragment(this, Environment.getExternalStorageDirectory().getPath());
        }

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
        if (customViewFragment != null) {
            refresh(customViewFragment.getPath());
        }
        hideInput();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuUtils.addMenu(menu);
        this.menu = menu;
        setOperationGroupVisible(operationGroupVisible);
        setSelectIntervalIco(false, -1, -1);
        setPasteVisible(pasteVisible);
        setAllSelectIco(customViewFragment.allSelect);
        return super.onCreateOptionsMenu(menu);
    }

    public void deleteFile() {
        //使用AsyncTask
        if (missionList.size() > 0) {
            ActionThread acthread = new ActionThread();
            acthread.execute(missionList);
        }
        clearUnderBar();
    }

    public void copySelectFile(){
        FileShowFragment s =(FileShowFragment)customViewFragment;
        missionList = s.getSelectedList();
        fileOperationType = MODE_COPY;
        setPasteVisible(true);
        s.unSelectAll();
    }

    public void delecteSelectFile(){
        FileShowFragment customViewFragment=(FileShowFragment) this.customViewFragment;
        ArrayList<FileInfo> deleteList = customViewFragment.getSelectedList();//储存删除对象
        ArrayList<Integer> deletePosition = customViewFragment.getSelectPosition(deleteList);//储存位置
        if (deleteList.size() != 0 && deletePosition.size() != 0) {
            customViewFragment.removeDates(deleteList);
        }
        fileOperationType = MODE_DELETE;
        missionList = deleteList;
        deleteFile();
    }

    public void createNewFile(){
        InputDialog newFileOrDir =new InputDialog(this,R.layout.dialog_new_file_or_dir_layout,"生成") {
            private Button dir;
            @Override
            public void queryButtonClick(View v, String nameInputStr) {

            }

            @Override
            public void customView() {
                super.customView();
                dir=findViewById(R.id.button4);
                dir.setText("文件夹");
                dir.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                String s = nameInput.getText().toString();
                String path = customViewFragment.getPath() + File.separator + s;
                String successful=null;
                switch (v.getId()){
                    case R.id.button3:
                        successful =FileUtils.newFile(path);
                        break;
                    case R.id.button4:
                        successful =FileUtils.newFolder(path);
                        break;
                }
                if(successful!=null) customViewFragment.addDate(getFileInfoFromPath(customViewFragment.getPath() + File.separator +successful));
                ToastUtils.showToast(MainActivity.this, successful!=null?"成功":"失败", 1000);
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

    public void moveSelectFile(){
        FileShowFragment customViewFragment=(FileShowFragment) this.customViewFragment;
        fileOperationType = MODE_MOVE;
        missionList = customViewFragment.getSelectedList();
        setPasteVisible(true);
        customViewFragment.unSelectAll();
    }


    public void load(){
        String s = pathEdit.getText().toString();
        if(s.startsWith(File.separator)){
            if(customViewFragment instanceof SettingFragment){
                customViewFragment= new FileShowFragment(this, s);
                customViewFragment.load();
            }else {
                if (s.startsWith("...")) {
                    customViewFragment.load();
                } else {
                    customViewFragment.load(s, false);
                }
            }

        }else {
            if(customViewFragment instanceof FileShowFragment){
                customViewFragment= new SettingFragment(this, s);
            }else {
                if (s.startsWith("...")) {
                    customViewFragment.load();
                } else {
                    customViewFragment.load(s, false);
                }
            }
        }

    }

    public void selectAllOrNot(){
        if(this.customViewFragment instanceof FileShowFragment){
            FileShowFragment customViewFragment=(FileShowFragment) this.customViewFragment;
            boolean isAll = customViewFragment.isAllSelect();
            customViewFragment.selectAllPositionOrNot(!isAll);
            setAllSelectIco(!isAll);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //path.setFocusable(false);
        switch (id) {
            case 1:
                copySelectFile();
                break;
            case 2:
                pasteFileHere();
                break;
            case 3:
                delecteSelectFile();
                break;
            case 4:
                //添加
                createNewFile();
                break;
            case 5:
                //剪切
                moveSelectFile();
                break;
            case 6:
                //刷新
                load();
                break;
            case 7:
                //close fragment
                if (fragments.size() > 1) {
                    removeFragmentPage();
                } else {
                    finish();
                }
                break;
            case 8:
                //全选or取消全选
                selectAllOrNot();
                break;
            case 9:
                //区间选择
                if (startPosition != -1 && endPsoition != -1) {
                    FileShowFragment customViewFragment=(FileShowFragment) this.customViewFragment;
                    customViewFragment.selectSomePosition(startPosition, endPsoition);
                }
                break;
            case 10:
                compressFiles();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void compressFiles(){
        FileShowFragment customViewFragment=(FileShowFragment) this.customViewFragment;
        missionList=customViewFragment.getSelectedList();
        fileOperationType=MODE_COMPRESS;
        CompressDialog compressDialog=new CompressDialog(this,R.layout.dialog_compression_layout,"压缩文件参数");
        compressDialog.showAndSetName(missionList.get(0).getFileName()+"等文件");
    }



    public void compressFileAction(String zipFile,String zipPass,int type){
        ZipParameters zipParameters =new ZipParameters();
        zipParameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // 压缩方式
        zipParameters.setCompressionLevel(type); // 压缩级别
        if(zipPass!=null&&!zipPass.equals("")){
            zipParameters.setEncryptFiles(true);
            zipParameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD); // 加密方式
            zipParameters.setPassword(zipPass);//密码
        }
        ArrayList<FileInfo> topath = new ArrayList<>();
        topath.add(getFileInfoFromPath(customViewFragment.getPath()+File.separator+zipFile));
        if (missionList != null && missionList.size() > 0) {
            ActionThread workThread = new ActionThread(zipParameters);
            workThread.execute(missionList, topath);
        } else {
            ToastUtils.showToast(MainActivity.this, "源路径不存在", 1000);
        }
    }

    public void setAllSelectIco(boolean isAllSelected) {
        if (isAllSelected) {
            menu.getItem(8).setIcon(R.drawable.ic_all_no_selecte_ico);
        } else {
            menu.getItem(8).setIcon(R.drawable.ic_all_selecte_ico);
        }
    }

    public void setSelectIntervalIco(boolean visible, int start, int end) {
        this.startPosition = start;
        this.endPsoition = end;
        menu.getItem(6).setVisible(visible);
    }

    private void pasteFileHere() {
        //使用AsyncTask
        setPasteVisible(false);
        ArrayList<FileInfo> topath = new ArrayList<>();
        topath.add(getFileInfoFromPath(customViewFragment.getPath()));
        if (missionList != null && missionList.size() > 0) {
            ActionThread workThread = new ActionThread();
            workThread.execute(missionList, topath);

        } else {
            ToastUtils.showToast(MainActivity.this, "源路径不存在", 1000);
        }
    }

    public void addFileInfoInMissionList(FileInfo fileInfo) {
        if (missionList != null)
            missionList.clear();
        else {
            missionList = new ArrayList<>();
        }
        missionList.add(fileInfo);
    }

    private FrameLayout right;


    public void slideToPager(String path) {
        int position = fragmentAdapter.findPositionByFilePath(path);
        if (position >= 0) {//已经有此页面
            fragmentPager.setCurrentItem(position);
        } else {//没有此页面
            createNewFragment(path);
        }
    }

    /**
     * 创建一个新页面并移动到此页面
     *
     * @param path
     */
    public void createNewFragment(String path) {
        CustomFragment customFragment;
        if(path.startsWith("system:")){
            customFragment =new SettingFragment(this,path);
        }else{
            customFragment = new FileShowFragment(this, path);
        }

        fragments.add(customFragment);
        fragmentAdapter.notifyDataSetChanged();
        fragmentPager.setCurrentItem(fragments.size());
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_phone:
                slideToPager(Environment.getExternalStorageDirectory().getPath());
                break;
            case R.id.nav_gallery:
                slideToPager(Environment.getExternalStorageDirectory().getPath() + File.separator + "Pictures");
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
                slideToPager(bin.getPath());
                break;
            case R.id.nav_setting:
                //setting in activity
                Intent i = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(i);
                //setting in fragment
                //slideToPager("system:"+File.separator+"setting");
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
        if (!mPath.equals("/") && !mPath.equals(Environment.getExternalStorageDirectory().getPath())) {
            File file1 = new File(mPath);
            String path = file1.getParent();
            customViewFragment.load(path, true);
            return true;
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

        if (fragments.size() > 1) {
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
        removeFragmentPage(p);
    }

    /**
     * 移除fragment
     */
    public void removeFragmentPage(int position) {
        if (position == fragmentPager.getCurrentItem()) {
            if (position == 0) {
                fragmentPager.setCurrentItem(position + 1);
            } else {
                fragmentPager.setCurrentItem(position - 1);
            }
        }
        fragmentAdapter.removePager(position);
        if (fragments.size() < 1) finish();
    }

    /**
     * 点击除editText外时，editText范围失去焦点
     *
     * @param ev
     *
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
     *
     * @param
     * @param
     *
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


    public void setCustomViewFragment(CustomFragment customViewFragment) {
        if (fragments == null) {
            fragments = new ArrayList<>();
        }
        fragments.add(customViewFragment);
        fragmentAdapter = new FragmentAdapter(fragmentManager, fragments);
        fragmentPager.setAdapter(fragmentAdapter);
        fragmentPager.setCurrentItem(0);
        fragmentPager.addOnPageChangeListener(this);
    }


    public void smallViewLoadOrRefresh() {
        if (smallFragmentViewAdapter == null) {
            smallFragmentViewAdapter = new SupperAdapter<CustomFragment>(fragments, this, new SupperAdapter.OnItemClickListener<CustomFragment>() {
                @Override
                public void onItemClick(SupperAdapter.VH holder, CustomFragment data, int position) {
                    holder.itemView.setBackgroundColor(ConstantUtils.SELECTED_COLOR);
                    if (position != fragmentPager.getCurrentItem()) {
                        if (smallFragmentView.findViewByPosition(fragmentPager.getCurrentItem()) != null)
                            Objects.requireNonNull(smallFragmentView.findViewByPosition(fragmentPager.getCurrentItem())).setBackgroundColor(NORMAL_COLOR);
                        fragmentPager.setCurrentItem(position);
                    }

                }
            }) {
                @Override
                public void onViewAttachedToWindow(@NonNull VH holder) {
                    super.onViewAttachedToWindow(holder);
                    if (holder.getAdapterPosition() == fragmentPager.getCurrentItem()) {
                        holder.itemView.setBackgroundColor(ConstantUtils.SELECTED_COLOR);
                    } else {
                        holder.itemView.setBackgroundColor(NORMAL_COLOR);
                    }
                }

                @Override
                public int getLayoutId(int viewType, CustomFragment data) {
                    return R.layout.small_fragment_view_item_layout;
                }

                @Override
                public void convert(VH holder, CustomFragment data, final int position, Context mContext) {
                    holder.setText(R.id.textView7, data.getFragmentTitle());
                    holder.setPic(R.id.imageView6, data.getSmallView(), mContext);
                    if (position == fragmentPager.getCurrentItem()) {
                        holder.itemView.setBackgroundColor(ConstantUtils.SELECTED_COLOR);
                    } else {
                        holder.itemView.setBackgroundColor(NORMAL_COLOR);
                    }
                    holder.getView(R.id.imageView9).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            removeFragmentPage(position);
                            smallFragmentViewAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };
            smallFragmentView = new LinearLayoutManager(this);
            smallFragmentList.setLayoutManager(smallFragmentView);
            smallFragmentList.addItemDecoration(new MyItemDecoration(this, 1));
            smallFragmentList.setAdapter(smallFragmentViewAdapter);
        } else {
            smallFragmentViewAdapter.notifyDataSetChanged();
        }
    }

    public void refresh(String path) {
        if (pathEdit != null && path != null && !path.equals("")) {
            mPath = path;
            String s = hideMax(path, 55);
            pathEdit.setText(s);
            setTitle(customViewFragment.getFragmentTitle());
        }
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
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        fragmentPager.setCurrentItem(position);
        customViewFragment = fragments.get(position);
        if (customViewFragment.getUnderBarMsg() == null || customViewFragment.getUnderBarMsg().equals("")) {
            clearUnderBar();
        } else {
            refreshUnderBar();
        }
        refresh(customViewFragment.getPath());

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View view) {
        if(!(this.customViewFragment instanceof FileShowFragment)){
            if(view.getId()==R.id.imageView5){
                customViewFragment.makeLinerLayout();
                SettingParam.setColumn(1);
                view.setBackgroundColor(GIRD_LINER_LAYOUT);
                grid.setBackgroundColor(NORMAL_COLOR);
            }else if(view.getId()==R.id.imageView3){
                int spanCount = SettingParam.Column;
                if (spanCount <= 2) spanCount += 2;
                SettingParam.setColumn(spanCount);
                customViewFragment.makeGridLayout(spanCount, CustomFragment.makeItemLayoutRes(spanCount));
                view.setBackgroundColor(GIRD_LINER_LAYOUT);
                liner.setBackgroundColor(NORMAL_COLOR);
            }
            return;
        }
        //FileShowFragment customViewFragment = (FileShowFragment) this.customViewFragment;
        switch (view.getId()) {
            case R.id.textView2://名字排序
                if (customViewFragment.getSortType() == SORT_BY_NAME)
                    customViewFragment.sortReFresh(SORT_BY_NAME_REV);
                else
                    customViewFragment.sortReFresh(SORT_BY_NAME);
                break;
            case R.id.textView3://日期排序
                if (customViewFragment.getSortType() == SORT_BY_DATE)
                    customViewFragment.sortReFresh(SORT_BY_DATE_REV);
                else
                    customViewFragment.sortReFresh(SORT_BY_DATE);
                break;
            case R.id.textView5://大小排序
                if (customViewFragment.getSortType() == SORT_BY_SIZE)
                    customViewFragment.sortReFresh(SORT_BY_SIZE_REV);
                else
                    customViewFragment.sortReFresh(SORT_BY_SIZE);
                break;
            case R.id.textView4://类型排序
                if (customViewFragment.getSortType() == SORT_BY_TYPE)
                    customViewFragment.sortReFresh(SORT_BY_TYPE_REV);
                else
                    customViewFragment.sortReFresh(SORT_BY_TYPE);
                break;
            case R.id.imageView5://列表布局
                customViewFragment.makeLinerLayout();
                SettingParam.setColumn(1);
                view.setBackgroundColor(GIRD_LINER_LAYOUT);
                grid.setBackgroundColor(NORMAL_COLOR);
                break;
            case R.id.imageView3://网格布局
                int spanCount = SettingParam.Column;
                if (spanCount <= 2) spanCount += 2;
                SettingParam.setColumn(spanCount);
                customViewFragment.makeGridLayout(spanCount, CustomFragment.makeItemLayoutRes(spanCount));
                view.setBackgroundColor(GIRD_LINER_LAYOUT);
                liner.setBackgroundColor(NORMAL_COLOR);
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    public void clearUnderBar() {
        if (menu != null) {
            setOperationGroupVisible(false);
            setAllSelectIco(false);
            setSelectIntervalIco(false, -1, -1);
        }
        underBarInfo.setText("\t" + customViewFragment.getFileInfosSize() + getString(R.string.how_many_item));
    }


    @SuppressLint("SetTextI18n")
    public void refreshUnderBar(int how, long size, boolean haveDir) {
        customViewFragment.menuVisible = how > 0;
        String howStr = how == 0 ? "" : "|\t" + getString(R.string.select) + how + "" + getString(R.string.how_many_item);
        String bigStr;
        if (haveDir || how == 0) {
            bigStr = "";
        } else {
            bigStr = size > GB ? stayFrieNumber((float) size / GB) + "GB" + "\t\t|"
                    : size > MB ? stayFrieNumber((float) size / MB) + "MB" + "\t\t|"
                    : stayFrieNumber((float) size / KB) + "KB" + "\t\t|";
        }
        if (how != 0) {
            setOperationGroupVisible(true);
        } else {
            setOperationGroupVisible(false);
        }
        String underMsg = "\t" + customViewFragment.getFileInfosSize() + getString(R.string.how_many_item) + "\t\t" + howStr + "\t\t" + bigStr;
        underBarInfo.setText(underMsg);
        customViewFragment.setUnderBarMsg(underMsg);
    }

    public void refreshUnderBar() {
        customViewFragment.refreshUnderBar();
    }

    @Override
    public String getFilePath() {
        return customViewFragment.getPath();
    }

    @Override
    public String getInputPath() {
        return pathEdit.getText().toString().trim();
    }

    @Override
    public void refresh(ArrayList<FileInfo> fileInfos) {

    }

    @Override
    public void showToast(String msg) {
        ToastUtils.showToast(this,msg,1000);
    }

    @SuppressLint("StaticFieldLeak")
    private class ActionThread extends AsyncTask<ArrayList<FileInfo>, ProgressMessage, Integer> {
        private ZipParameters zipParameters;

        public ActionThread(ZipParameters zipParameters) {
            this.zipParameters = zipParameters;
        }

        public ActionThread() {
        }

        @Override
        protected Integer doInBackground(ArrayList<FileInfo>... inParams) {
            switch (fileOperationType) {
                case MODE_COPY:
                    fileOperation = FileOperation.with(MainActivity.this).copy(inParams[0]).to(inParams[1].get(0));
                    break;
                case MODE_MOVE:
                    fileOperation = FileOperation.with(MainActivity.this).move(inParams[0]).to(inParams[1].get(0));
                    //missionList.clear();
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
                    fileOperation=FileOperation.with(MainActivity.this).compress(inParams[0]).applyZipParameters(zipParameters).to(inParams[1].get(0));
                    break;
            }
            if (fileOperationType != MODE_DELETE)
                publishProgress(fileOperation.getProgressMessage());
            fileOperation.run();
            return null;
        }

        @Override
        protected void onProgressUpdate(ProgressMessage... values) {
            super.onProgressUpdate(values);
            if (values[0] != null && values[0].getProgress() != 100) {
                Intent intent = new Intent(MainActivity.this, ProgressActivity.class);
                startActivity(intent);
                setOnActionFinish(new ProgressActivity.OnActionFinish() {
                    @Override
                    public void OnRefresh() {
                        customViewFragment.load();
                    }
                });
            } else if (values[0] != null && values[0].getProgress() == 100) {
                if (values[0].getmType() == MODE_COPY || values[0].getmType() == MODE_MOVE) {
                    customViewFragment.addDates(fileOperation.getInFiles());
                } else if(values[0].getmType() == MODE_COMPRESS ){
                    customViewFragment.addDate(fileOperation.getToDir());
                } else {
                    customViewFragment.removeDates(fileOperation.getInFiles());
                }
            } else {
                LogUtils.i(getClass().getName(), "获取的进度values为空");
            }
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String path = intent.getStringExtra("file_path");
        if (path != null && !path.equals("")) {
            slideToPager(path);
        }
    }
}
