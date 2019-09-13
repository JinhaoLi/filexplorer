package com.jil.filexplorer.ui;

import android.annotation.SuppressLint;
;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jil.filexplorer.Api.FileInfo;
import com.jil.filexplorer.Activity.MainActivity;
import com.jil.filexplorer.R;
import com.jil.filexplorer.adapter.FileListAdapter;
import com.jil.filexplorer.Api.SortComparator;
import com.jil.filexplorer.utils.FileUtils;
import com.jil.filexplorer.utils.LogUtils;
import com.jil.filexplorer.utils.UiUtils;

import java.util.ArrayList;
import java.util.Collections;

import static com.jil.filexplorer.Api.SettingParam.saveSharedPreferences;
import static com.jil.filexplorer.Api.SortComparator.SORT_BY_NAME;
import static com.jil.filexplorer.utils.ConstantUtils.CAN_MOVE_COLOR;
import static com.jil.filexplorer.utils.ConstantUtils.MAX_SPAN_COUNT;
import static com.jil.filexplorer.utils.ConstantUtils.MIN_SPAN_COUNT;
import static com.jil.filexplorer.utils.ConstantUtils.NORMAL_COLOR;
import static com.jil.filexplorer.utils.ConstantUtils.CANT_SELECTED_COLOR;
import static com.jil.filexplorer.utils.ConstantUtils.SELECTED_COLOR;

public abstract class CustomViewFragment extends Fragment {
    protected final static String TAG = "CustomViewFragment";
    protected String fragmentTitle;
    protected MainActivity mMainActivity;
    private View rootView;
    private String underBarInfos;
    protected String filePath;
    protected ArrayList<FileInfo> fileInfos;
    protected FileListAdapter fileListAdapter;
    protected RecyclerView fileList;
    public boolean menuVisible;
    public Bitmap smallView;
    //全选按钮可见状态
    public boolean allSelect=false;

    private boolean isRecovery;

    public void addFileInfos(ArrayList<FileInfo> fileInfos){
        this.fileInfos.addAll(fileInfos);
        fileListAdapter.notifyDataSetChanged();
    }
    public void addFileInfo(FileInfo fileInfos){
        this.fileInfos.add(fileInfos);
        fileListAdapter.notifyDataSetChanged();
    }

    public void removeFileInfos(ArrayList<FileInfo> fileInfos){
        this.fileInfos.removeAll(fileInfos);
        fileListAdapter.notifyDataSetChanged();
    }

    //记录上次经过项
    protected int outOfFromPosition;
    //记录位置是否已经改变
    protected boolean selectPositionChange = true;
    protected LinearLayoutManager linearLayoutManager;
    protected SortComparator comparator = new SortComparator(SORT_BY_NAME);
    protected int longClickPosition;
    //网格布局计数
    int spanCount = 4;

    public CustomViewFragment() {
    }

    public CustomViewFragment(Activity activity, String filePath) {
        this.mMainActivity =(MainActivity)activity;
        this.filePath = filePath;
    }

    @SuppressLint({"ClickableViewAccessibility", "ResourceType"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = initView(inflater, container);
        if(!isRecovery){
            initAction();
        }

        return v;
    }

    protected View initView(LayoutInflater inflater, ViewGroup container) {
        if(rootView==null){
            isRecovery=false;
            rootView = inflater.inflate(R.layout.fragment_file_view_layout, container, false);
            fileList = rootView.findViewById(R.id.file_list_view);

        }else {
            isRecovery=true;
        }
        return rootView;
    }

    @SuppressLint("ClickableViewAccessibility")
    protected abstract void initAction();

    private void refreshSmallView(){
        if(fileList!=null)
        smallView= UiUtils.createViewBitmap(smallView,fileList);
    }

    public Bitmap getSmallView(){
        refreshSmallView();
        return smallView;
    }

    protected abstract void deleteItem(int adapterPosition);

    protected abstract boolean getFileListFromDir(String filePath,boolean isBack);

    public String getFilePath() {
        return filePath;
    }

    public String getFragmentTitle() {
        return fragmentTitle;
    }


    /**
     * 载入刷新
     * @param filePath
     * @param isBack
     */
    public abstract void load(String filePath, boolean isBack);

    public int getSortType(){
        return comparator.getSortType();
    }

    public void sortReFresh(int sortType) {
        try {
            comparator.setSortType(sortType);
            Collections.sort(fileInfos, comparator);
        } catch (Exception e) {
            comparator.setSortType(SORT_BY_NAME);
            Collections.sort(fileInfos, comparator);
        }
        fileListAdapter.notifyItemRangeChanged(0, fileInfos.size());
    }
    /**
     * 手指抬起-拖动状态改变
     *
     * @param view
     */
    private void fingerUpState(View view) {
        FileInfo temp = fileInfos.get(outOfFromPosition);
        //ToastUtils.showToast(mMainActivity,temp.getFileName(),1000);
        //选中项次层级
        View v = linearLayoutManager.findViewByPosition(outOfFromPosition);
        if (v != null && selectPositionChange) {
            if (temp.isSelected()) {
                v.setBackgroundColor(SELECTED_COLOR);  //恢复次层级颜色
            } else {
                v.setBackgroundColor(NORMAL_COLOR);  //恢复次层级颜色
            }
        }
        if (selectPositionChange) {    //移动范围超item范围
            if (temp.isDir()) {
                moveDir(temp);
            }
        }
        //被选中的项目恢复原来的颜色
        View selectItem = linearLayoutManager.findViewByPosition(longClickPosition);
        if (selectItem != null) {
            selectItem.setAlpha(1.0f);
            if (fileInfos.get(longClickPosition).isSelected())
                selectItem.setBackgroundColor(SELECTED_COLOR);
        }
    }

    /**
     * 项目拖动超范围
     *
     * @param toPosition
     * @return
     */
    protected boolean itemPositionDrag(int toPosition) {
        FileInfo temp = fileInfos.get(outOfFromPosition);//从此项离开
        FileInfo inputDir = fileInfos.get(toPosition);//进入此项
        selectPositionChange = true;//代表位置已经改变
        //记录此项原来的的位置
        if (outOfFromPosition != toPosition) {
            //此项位置发生改变时，将上一次染色的item背景改变为透明
            try {
                if (!temp.isSelected())
                    linearLayoutManager.findViewByPosition(outOfFromPosition).setBackgroundColor(NORMAL_COLOR);
                else
                    linearLayoutManager.findViewByPosition(outOfFromPosition).setBackgroundColor(SELECTED_COLOR);
            } catch (Exception e) {
                LogUtils.e(getClass().getName(), e.getMessage()+"list的item过多，屏幕无法显示所有的item，当outOfFromPosition还没修正时，尝试获取View时抛出错误");
            }
            //刷新此项当前的的位置
            outOfFromPosition = toPosition;
        }
        //将此项的次层级item背景改变为浅蓝色
        if (inputDir.isDir()/*&&!temp.isSelected()*/) {
            linearLayoutManager.findViewByPosition(toPosition).setBackgroundColor(CAN_MOVE_COLOR);
        } else if (!inputDir.isDir()) {
            linearLayoutManager.findViewByPosition(toPosition).setBackgroundColor(CANT_SELECTED_COLOR);
        }
        return true;
    }

    protected abstract void moveDir(FileInfo temp);

//    /**
//     * 手指按下-拖动状态改变
//     *
//     * @param v
//     */
//    protected void fingerDownState(View v) {
//        selectPositionChange = false;
//        int[] ints = getSelectStartAndEndPosition();
//        selectSomePosition(ints[1], longClickPosition);
//        //此项选中时将背景透明化
//        v.setAlpha(0.7f);
//        v.setBackgroundColor(NORMAL_COLOR);
//    }

    public void setLongClickPosition(int longClickPosition) {
        this.longClickPosition = longClickPosition;
    }


    /**
     * 将一些区间选项选中
     *
     * @param start
     * @param end
     */
    public void selectSomePosition(int start, int end) {
        if (start != -1 && start != end) {
            int s = start > end ? end : start;
            int e = start > end ? start : end;
            if (!fileInfos.get(end).isSelected()) {
                for (int i = s; i <= e; i++) {
                    fileInfos.get(i).setSelected(false);
                    View selectItem = linearLayoutManager.findViewByPosition(i);
                    if (selectItem != null)
                        selectItem.setBackgroundColor(NORMAL_COLOR);
                }
            } else {
                for (int i = s; i <= e; i++) {
                    fileInfos.get(i).setSelected(true);
                    View selectItem = linearLayoutManager.findViewByPosition(i);
                    if (selectItem != null)
                        selectItem.setBackgroundColor(SELECTED_COLOR);
                }
            }
        }
        refreshUnderBar();
    }

    public void unSelectAll(){
        for(FileInfo fileInfo:fileInfos){
            if(fileInfo.isSelected()){
                fileInfo.setSelected(false);
                int i =fileInfos.indexOf(fileInfo);
                View selectItem = linearLayoutManager.findViewByPosition(i);
                if (selectItem != null)
                    selectItem.setBackgroundColor(NORMAL_COLOR);
            }
        }
        mMainActivity.setOperationGroupVisible(false);
        mMainActivity.setAllSelectIco(false);
    }

    public boolean isAllSelect(){
        return FileUtils.getSelectedList(fileInfos).size()==fileInfos.size();
    }

    /**
     * 全选or不全选
     * @return 是全选为true
     */
    public void selectAllPositionOrNot(boolean selectAll) {
        if (selectAll) {
            for (int i = 0; i < fileInfos.size(); i++) {
                fileInfos.get(i).setSelected(true);
                View selectItem = linearLayoutManager.findViewByPosition(i);
                if (selectItem != null){
                    selectItem.setBackgroundColor(SELECTED_COLOR);
                }

            }
        } else {
            for (int i = 0; i < fileInfos.size(); i++) {
                fileInfos.get(i).setSelected(false);
                View selectItem = fileList.getChildAt(i);
                if (selectItem != null){
                    selectItem.setBackgroundColor(NORMAL_COLOR);
                }

            }

        }
        refreshUnderBar();
    }


    public void load(){
        load(filePath,true);
    }


    public ArrayList<FileInfo> getSelectedList() {
        return FileUtils.getSelectedList(fileInfos);
    }

    /**
     * 获取所有选中的item position的开始位置和结束位置
     *
     * @return
     */
    private int[] getSelectStartAndEndPosition(ArrayList<FileInfo> selectList) {
        int[] SelectStartAndEndPosition = new int[2];//储存位置
        if(selectList==null||selectList.size()==0) selectList = FileUtils.getSelectedList(fileInfos);
        if (selectList.size() != 0) {
            SelectStartAndEndPosition[0] = fileInfos.indexOf(selectList.get(0));
            SelectStartAndEndPosition[1] = fileInfos.indexOf(selectList.get(selectList.size() - 1));
        } else {
            SelectStartAndEndPosition[0] = -1;
            SelectStartAndEndPosition[1] = -1;
        }
        return SelectStartAndEndPosition;
    }

    /**
     * 获取所有选中的item position
     *
     * @param deleteList 选中的列表
     * @return
     */
    public ArrayList<Integer> getSelectPosition(ArrayList<FileInfo> deleteList) {
        ArrayList<Integer> selectPosition = new ArrayList<>();//储存位置
        for (int i = 0; i < deleteList.size(); i++) {
            FileInfo fileInfo = deleteList.get(i);
            selectPosition.add(fileInfos.indexOf(fileInfo));
        }
        return selectPosition;
    }

    @SuppressLint("SetTextI18n")
    protected void clearUnderBar() {
        //underBarInfo.setText("\t"+fileInfos.size() + getString(R.string.how_many_item));
        mMainActivity.clearUnderBar();
    }

    public int getFileInfosSize(){
        if(fileInfos!=null)
        return fileInfos.size();
        else
        return 0;
    }


    @SuppressLint("SetTextI18n")
    public int refreshUnderBar() {
        ArrayList<FileInfo> selectList = FileUtils.getSelectedList(fileInfos);

        long size = 0L;
        boolean haveDir = false;

        for (FileInfo temp : selectList) {
            if (!temp.isDir()) {
                size += temp.getFileSize();
            } else {
                haveDir = true;
            }
        }
        if(selectList.size()==fileInfos.size()&&fileInfos.size()!=0){
            allSelect=true;
        }else {
            allSelect=false;
        }
        mMainActivity.setAllSelectIco(allSelect);
        int[] interval =getSelectStartAndEndPosition(selectList);
        if(interval[0]!=-1&&interval[1]!=-1){
            if(interval[1]-interval[0]>=selectList.size())
                mMainActivity.setSelectIntervalIco(true,interval[0],interval[1]);
            else
                mMainActivity.setSelectIntervalIco(false, interval[0], interval[1]);
        }

        mMainActivity.refreshUnderBar(selectList.size(),size,haveDir);
        return selectList.size();
    }

    public void setUnderBarInfos(String underBarInfos) {
        this.underBarInfos = underBarInfos;
    }

    public static int makeItemLayoutRes(int spanCount) {
        if (spanCount == MAX_SPAN_COUNT) {
            return R.layout.flie_grid_item_layout_60dp;
        } else if (spanCount == MAX_SPAN_COUNT-1) {
            return R.layout.flie_grid_item_layout_80dp;
        } else if (spanCount == MIN_SPAN_COUNT+1) {
            return R.layout.flie_grid_item_layout_100dp;
        } else if (spanCount == MIN_SPAN_COUNT) {
            return R.layout.flie_grid_item_layout_120dp;
        } else {
            return R.layout.flie_grid_item_layout_100dp;
        }
    }





    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != rootView) {
            ((ViewGroup) rootView.getParent()).removeView(rootView);
        }
    }

    public String getUnderBarInfos() {
        return underBarInfos;
    }
}
