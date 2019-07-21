package com.jil.filexplorer.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jil.filexplorer.FileInfo;
import com.jil.filexplorer.MainActivity;
import com.jil.filexplorer.R;
import com.jil.filexplorer.adapter.FileListAdapter;
import com.jil.filexplorer.interfaces.SortComparator;
import com.jil.filexplorer.utils.FileUtils;
import com.jil.filexplorer.utils.LogUtils;
import com.jil.filexplorer.utils.MenuUtils;
import com.jil.filexplorer.utils.ToastUtils;
import com.yanzhenjie.recyclerview.swipe.Closeable;
import com.yanzhenjie.recyclerview.swipe.OnSwipeMenuItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;
import com.yanzhenjie.recyclerview.swipe.touch.OnItemMoveListener;
import com.yanzhenjie.recyclerview.swipe.touch.OnItemStateChangedListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import static com.jil.filexplorer.interfaces.SortComparator.SORT_BY_DATE;
import static com.jil.filexplorer.interfaces.SortComparator.SORT_BY_NAME;
import static com.jil.filexplorer.interfaces.SortComparator.SORT_BY_SIZE;
import static com.jil.filexplorer.interfaces.SortComparator.SORT_BY_TYPE;
import static com.jil.filexplorer.utils.ConstantUtils.CAN_MOVE_COLOR;
import static com.jil.filexplorer.utils.ConstantUtils.NORMAL_COLOR;
import static com.jil.filexplorer.utils.ConstantUtils.CANT_SELECTED_COLOR;
import static com.jil.filexplorer.utils.ConstantUtils.SELECTED_COLOR;
import static com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView.LEFT_DIRECTION;
import static com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView.RIGHT_DIRECTION;

public class FileViewFragment extends Fragment implements View.OnClickListener {
    private final static String TAG = "FileViewFragment";
    private String fragmentTitle;
    private MainActivity mMainActivity;
    private View rootView;
    private String filePath;
    private ArrayList<FileInfo> fileInfos;
    private FileListAdapter fileListAdapter;
    private SwipeMenuRecyclerView fileList;
    //记录上次经过项
    private int outOfFromPosition;
    //记录选中项
    private int selectPosition =-1;
    private LinearLayoutManager linearLayoutManager;
    private SortComparator comparator =new SortComparator(SORT_BY_NAME);
    //顶部排序栏,底部操作栏
    private FrameLayout topBar,underBar;
    //列名
    private TextView sortName,sortDate,sortType,sortSize;


    @SuppressLint({"ClickableViewAccessibility", "ResourceType"})
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        filePath = Environment.getExternalStorageDirectory().getPath();
        mMainActivity = (MainActivity) getActivity();
        mMainActivity.setFileViewFragment(this);
        rootView = inflater.inflate(R.layout.fragment_file_view_layout, container, false);
        topBar=rootView.findViewById(R.id.top_bar);
        underBar=rootView.findViewById(R.id.under_bar);
        sortName=rootView.findViewById(R.id.textView2);
        sortDate=rootView.findViewById(R.id.textView3);
        sortSize=rootView.findViewById(R.id.textView5);
        sortType =rootView.findViewById(R.id.textView4);
        sortName.setOnClickListener(this);
        sortDate.setOnClickListener(this);
        sortSize.setOnClickListener(this);
        sortType.setOnClickListener(this);
        fileList = (SwipeMenuRecyclerView) rootView.findViewById(R.id.file_list_view);
        linearLayoutManager = new LinearLayoutManager(mMainActivity);
        fileList.setLayoutManager(linearLayoutManager);
        fileList.setLongPressDragEnabled(true);// 开启长按拖拽
        setHasOptionsMenu(true);//onCreateOptionsMenu生效条件
        load(filePath,false);
        fileList.setSwipeMenuCreator(new SwipeMenuCreator() {
            @Override
            public void onCreateMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, int viewType) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(mMainActivity)
                        .setBackgroundColor(Color.RED)
                        //.setImage(R.mipmap.ic_launcher) // 图标。
                        .setText(mMainActivity.getString(R.string.delete)) // 文字。
                        .setTextColor(Color.WHITE) // 文字颜色。
                        .setTextSize(15) // 文字大小。
                        .setWidth(120).setWeight(1)
                        .setHeight(-1);//-1指的时父布局的高度
                swipeRightMenu.addMenuItem(deleteItem);// 添加一个按钮到右侧侧菜单。.
                SwipeMenuItem copyItem = new SwipeMenuItem(mMainActivity)
                        .setBackgroundColor(Color.BLUE)
                        //.setImage(R.mipmap.ic_launcher) // 图标。
                        .setText(mMainActivity.getString(R.string.copy)) // 文字。
                        .setTextColor(Color.WHITE) // 文字颜色。
                        .setTextSize(15) // 文字大小。
                        .setWidth(120)
                        .setHeight(-1);//-1指的时父布局的高度
                SwipeMenuItem cutItem = new SwipeMenuItem(mMainActivity)
                        .setBackgroundColor(Color.DKGRAY)
                        //.setImage(R.mipmap.ic_launcher) // 图标。
                        .setText(mMainActivity.getString(R.string.cut)) // 文字。
                        .setTextColor(Color.WHITE) // 文字颜色。
                        .setTextSize(15) // 文字大小。
                        .setWidth(120)
                        .setHeight(-1);//-1指的时父布局的高度
                swipeLeftMenu.addMenuItem(copyItem);
                swipeLeftMenu.addMenuItem(cutItem);
            }
        });

        fileList.setSwipeMenuItemClickListener(new OnSwipeMenuItemClickListener() {
            @Override
            public void onItemClick(Closeable closeable, int adapterPosition, int menuPosition, int direction) {
                ArrayList<FileInfo> fileInfos = fileListAdapter.getmData();
                if (menuPosition == 0 && direction==RIGHT_DIRECTION) {
                    //删除逻辑
                    fileListAdapter.notifyItemRemoved(adapterPosition);
                    fileInfos.remove(adapterPosition);
                    fileListAdapter.notifyItemRangeChanged(adapterPosition, fileInfos.size());
                    closeable.smoothCloseMenu();
                }
                if(menuPosition==0&&direction==LEFT_DIRECTION){
                    //复制逻辑

                    ToastUtils.showToast(mMainActivity,getString(R.string.copy),1000);
                    closeable.smoothCloseMenu();
                }
                if(menuPosition==1&&direction==LEFT_DIRECTION){
                    //剪切逻辑

                    ToastUtils.showToast(mMainActivity,getString(R.string.cut),1000);
                    closeable.smoothCloseMenu();
                }
            }
        });

        //item拖动状态改变时调用
        fileList.setOnItemStateChangedListener(new OnItemStateChangedListener() {
            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int i) {
                LogUtils.i(getClass().getName() + ":167", i + "");
                if (i == 2) { //i == 2选项被选中
                    fingerDownState(viewHolder.itemView);
                }else if(i == 0){ //i == 0 选中项被释放
                    fingerUpState(viewHolder.itemView);
                }
            }
        });


        //item移动位置改变时调用
        fileList.setOnItemMoveListener(new OnItemMoveListener() {
            @Override
            public boolean onItemMove(int fromPosition, int toPosition) {
                FileInfo temp =fileInfos.get(outOfFromPosition);
                //记录此项原来的的位置
                selectPosition = fromPosition;
                if (outOfFromPosition != toPosition) {
                    //此项位置发生改变时，将上一次染色的item背景改变为透明
                    //if(outOfFromPosition-fromPosition<15&&outOfFromPosition-fromPosition>-15){
                    try {
                        if(!temp.isSelected())
                            linearLayoutManager.findViewByPosition(outOfFromPosition).setBackgroundColor(NORMAL_COLOR);
                        else
                            linearLayoutManager.findViewByPosition(outOfFromPosition).setBackgroundColor(SELECTED_COLOR);
                    }catch (Exception e){
                        LogUtils.e(loge(191),"list的item过多，屏幕无法显示所有的item，当outOfFromPosition还没修正时，尝试获取View时抛出错误");
                    }

                    //}

                    //刷新此项当前的的位置
                    outOfFromPosition = toPosition;
                }
                //将此项的次层级item背景改变为浅蓝色
                FileInfo inputDir=fileInfos.get(toPosition);
                if(inputDir.isDir()&&!temp.isSelected()){
                    linearLayoutManager.findViewByPosition(toPosition).setBackgroundColor(CAN_MOVE_COLOR);
                }else if(!inputDir.isDir()){
                    linearLayoutManager.findViewByPosition(toPosition).setBackgroundColor(CANT_SELECTED_COLOR);
                }

                return true;
            }

            @Override
            public void onItemDismiss(int position) {
                LogUtils.i(getClass().getName() + ":117", position + "");
            }
        });// 监听拖拽和侧滑删除，更新UI和数据。
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        MenuUtils.addMenu(menu);
        super.onCreateOptionsMenu(menu, mMainActivity.getMenuInflater());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        //path.setFocusable(false);
        switch (id){
            case 1:
                //复制
                //LogUtils.d("SortPara",""+sortWithDoule(SortPara.SORT_BY_SIZE,fileInfos.get(1),fileInfos.get(2)));
                break;
            case 2:
                //剪切
                break;
            case 3:
                ArrayList<FileInfo> deleteList =new ArrayList<FileInfo>();//储存删除对象
                ArrayList<Integer> deletePosition=new ArrayList<Integer>();//储存位置
                int start=0;//第一个删除的position
                for(int i =0;i<fileInfos.size();i++) {
                    FileInfo fileInfo =fileInfos.get(i);
                    if(fileInfo.isSelected()){
                        deleteList.add(fileInfo);
                        deletePosition.add(i);
                        if(deletePosition.size()==1){
                            start=i;
                        }
                    }
                }
                if(deleteList.size()!=0){
                    for(int i =0;i<deletePosition.size();i++){
                        fileListAdapter.notifyItemRemoved(deletePosition.get(i));
                    }
                    fileInfos.removeAll(deleteList);
                    fileListAdapter.notifyItemRangeChanged(start,fileInfos.size());
                }
                //删除逻辑
                //Code。。。
                ToastUtils.showToast(mMainActivity,"文件并未删除！"+"测试删除"+deletePosition.size()+"项",1000);
                break;
            case 4:
                //添加
                break;
            case 5:
                //搜索
                break;
            case 6:
                //刷新
                load(mMainActivity.getEditText().getText().toString(),false);
                break;
            case 7:
                //退出
                //this.rootView.setVisibility(View.GONE);
                mMainActivity.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    private boolean getFileFromDir(String filePath) {
        File file=new File(filePath);
        if(!file.exists()){
            ToastUtils.showToast(mMainActivity,"无效路径",1000);
            return false;
        }
        if (file.canRead()) {
            fragmentTitle = file.getName();
            File[] files = file.listFiles();
            LogUtils.i(getClass().getName()+"285",files.length+"");
            fileInfos = new ArrayList<FileInfo>();
            for (File temp : files) {
                fileInfos.add(FileUtils.getFileInfoFromFile(temp));
            }
            Collections.sort(fileInfos,comparator);
            LogUtils.i(getClass().getName()+"290",fileInfos.size()+"");
            return true;//可以访问
        }else if(!file.canRead()){
            ToastUtils.showToast(mMainActivity,"无法访问",1000);
            return false;//无法访问
        }else {
            ToastUtils.showToast(mMainActivity,"无法访问",1000);
            return false;//无效路径
        }
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFragmentTitle() {
        return fragmentTitle;
    }

    public void load(String filePath,boolean isBack) {
        if(getFileFromDir(filePath)) {
            if (fileListAdapter == null) {
                //第一次加载
                fileListAdapter = new FileListAdapter(fileInfos, this, mMainActivity);
                fileList.setAdapter(fileListAdapter);
                mMainActivity.getHistoryPath().add(filePath);
            } else {
                //刷新
                fileListAdapter.setmData(fileInfos);
                fileListAdapter.notifyDataSetChanged();
                if(!isBack){
                    mMainActivity.getHistoryPath().add(filePath);
                }


            }
            this.filePath=filePath;

            mMainActivity.refresh(filePath);

        }
        outOfFromPosition=0;
        selectPosition=-1;
    }

    public void sort(int type){
        switch (type){
            case R.id.textView2:
                comparator.setSortType(SORT_BY_NAME);
                break;
            case R.id.textView3:
                comparator.setSortType(SORT_BY_DATE);
                break;
            case R.id.textView5:
                comparator.setSortType(SORT_BY_SIZE);
                break;
            case R.id.textView4:
                comparator.setSortType(SORT_BY_TYPE);
        }
        load(filePath,true);

    }

    //public void

    public ArrayList<FileInfo> getFileInfos() {
        return fileInfos;
    }

    @Override
    public void onClick(View view) {
        sort(view.getId());
    }

    //手指抬起-拖动状态改变
    private void fingerUpState(View view){

        FileInfo temp =fileInfos.get(outOfFromPosition);
        //ToastUtils.showToast(mMainActivity,temp.getFileName(),1000);
        //选中项次层级
        View v = linearLayoutManager.findViewByPosition(outOfFromPosition);
        //if (!fileList.isComputingLayout() && v != null) {
        if(v!=null){
            if(temp.isSelected()){
                v.setBackgroundColor(SELECTED_COLOR);  //恢复次层级颜色
            }else {
                v.setBackgroundColor(NORMAL_COLOR);  //恢复次层级颜色
            }
        }
        if(selectPosition !=-1 ){    //移动范围超item范围
            if(temp.isDir()){
                moveDir(temp);
            }
            //被选中的项目恢复原来的颜色
            View selectItem = linearLayoutManager.findViewByPosition(selectPosition);
            if(fileInfos.get(selectPosition).isSelected()&&selectItem!=null)
                selectItem.setBackgroundColor(SELECTED_COLOR);
            //selectPosition =-1;//修正范围
        }



    }

    //手指按下-拖动状态改变
    private void fingerDownState(View v){
        //此项选中时将背景透明化
        //v.setBackgroundColor(NORMAL_COLOR);
    }

    //手指按下-拖动状态改变,移动文件夹
    private void moveDir(FileInfo dir){
        String name =dir.getFileName();
        AlertDialog.Builder builder = new AlertDialog.Builder( mMainActivity );
        AlertDialog alertDialog=builder.setTitle("文件操作").setMessage("确定移动到"+name+"吗？")
                .setNegativeButton("移动" , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        fileInfos.remove(selectPosition);  //移除选中项
                        fileListAdapter.notifyItemRemoved(selectPosition);//通知刷新界面移除效果
                        //修正position
                        if (selectPosition != fileInfos.size()) {
                            fileListAdapter.notifyItemRangeChanged(selectPosition, fileInfos.size() - selectPosition);
                        }
                        selectPosition =-1;//修正范围

                    }
                })
                .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        selectPosition =-1;//修正范围
                    }
                }).create();
        alertDialog.show();
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                selectPosition =-1;//修正范围
            }
        });
    }

    private String loge(int line){
        return getClass().getCanonicalName()+"--"+line;

    }
}
