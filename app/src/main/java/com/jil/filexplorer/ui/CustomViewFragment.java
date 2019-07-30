package com.jil.filexplorer.ui;

import android.annotation.SuppressLint;
;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.jil.filexplorer.Api.FileInfo;
import com.jil.filexplorer.MainActivity;
import com.jil.filexplorer.R;
import com.jil.filexplorer.adapter.FileListAdapter;
import com.jil.filexplorer.Api.SortComparator;
import com.jil.filexplorer.utils.LogUtils;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;
import com.yanzhenjie.recyclerview.swipe.touch.OnItemStateChangedListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import static com.jil.filexplorer.Api.SortComparator.SORT_BY_DATE;
import static com.jil.filexplorer.Api.SortComparator.SORT_BY_DATE_REV;
import static com.jil.filexplorer.Api.SortComparator.SORT_BY_NAME;
import static com.jil.filexplorer.Api.SortComparator.SORT_BY_NAME_REV;
import static com.jil.filexplorer.Api.SortComparator.SORT_BY_SIZE;
import static com.jil.filexplorer.Api.SortComparator.SORT_BY_SIZE_REV;
import static com.jil.filexplorer.Api.SortComparator.SORT_BY_TYPE;
import static com.jil.filexplorer.Api.SortComparator.SORT_BY_TYPE_REV;
import static com.jil.filexplorer.utils.ConstantUtils.CAN_MOVE_COLOR;
import static com.jil.filexplorer.utils.ConstantUtils.GB;
import static com.jil.filexplorer.utils.ConstantUtils.GIRD_LINER_LAYOUT;
import static com.jil.filexplorer.utils.ConstantUtils.KB;
import static com.jil.filexplorer.utils.ConstantUtils.MB;
import static com.jil.filexplorer.utils.ConstantUtils.NORMAL_COLOR;
import static com.jil.filexplorer.utils.ConstantUtils.CANT_SELECTED_COLOR;
import static com.jil.filexplorer.utils.ConstantUtils.SELECTED_COLOR;
import static com.jil.filexplorer.utils.FileUtils.getDistance;
import static com.jil.filexplorer.utils.FileUtils.stayFrieNumber;

public abstract class CustomViewFragment extends Fragment implements View.OnClickListener {
    protected final static String TAG = "CustomViewFragment";
    protected String fragmentTitle;
    protected MainActivity mMainActivity;
    private View rootView;
    protected TextView underBarInfo;
    protected String filePath;
    protected ArrayList<FileInfo> fileInfos;
    protected FileListAdapter fileListAdapter;
    protected SwipeMenuRecyclerView fileList;
    //记录上次经过项
    protected int outOfFromPosition;
    //记录位置是否已经改变
    protected boolean selectPositionChange = true;
    protected LinearLayoutManager linearLayoutManager;
    protected SortComparator comparator = new SortComparator(SORT_BY_NAME);
    //顶部排序栏,底部操作栏
    private FrameLayout topBar;
    protected FrameLayout underBar;
    //列名
    private TextView sortName, sortDate, sortType, sortSize;
    protected int longClickPosition;
    //排列方式按钮
    protected ImageView liner, grid;
    //grid下一行多少个
    protected int spanCount = 4;

    public CustomViewFragment(String filePath) {
        super();
        this.filePath = filePath;
    }

    public CustomViewFragment() {
        super();
    }

    @SuppressLint({"ClickableViewAccessibility", "ResourceType"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = initView(inflater, container);
        initAction();
        return v;
    }

    protected View initView(LayoutInflater inflater, ViewGroup container) {
        mMainActivity = (MainActivity) getActivity();
        mMainActivity.setCustomViewFragment(this);
        rootView = inflater.inflate(R.layout.fragment_file_view_layout, container, false);
        topBar = rootView.findViewById(R.id.top_bar);

        fileList = rootView.findViewById(R.id.file_list_view);

        underBar = rootView.findViewById(R.id.under_bar);
        underBarInfo = underBar.findViewById(R.id.textView6);
        liner = underBar.findViewById(R.id.imageView5);
        grid = underBar.findViewById(R.id.imageView3);

        sortName = rootView.findViewById(R.id.textView2);
        sortDate = rootView.findViewById(R.id.textView3);
        sortSize = rootView.findViewById(R.id.textView5);
        sortType = rootView.findViewById(R.id.textView4);

        return rootView;
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void initAction() {
        sortName.setOnClickListener(this);
        sortDate.setOnClickListener(this);
        sortSize.setOnClickListener(this);
        sortType.setOnClickListener(this);
        liner.setOnClickListener(this);
        grid.setOnClickListener(this);
        fileList.setLongPressDragEnabled(true);// 开启长按拖拽
        setHasOptionsMenu(true);//onCreateOptionsMenu生效条件
        //item拖动状态改变时调用
        fileList.setOnItemStateChangedListener(new OnItemStateChangedListener() {
            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int i) {
                LogUtils.i(getClass().getName() + ":167", i + "");
                if (i == 2) { //i == 2选项被选中
                    fingerDownState(viewHolder.itemView);
                } else if (i == 0) { //i == 0 选中项被释放
                    fingerUpState(viewHolder.itemView);
                }
            }
        });
        sacleIcon();
    }

    protected void makeLinerLayout() {
        linearLayoutManager = new LinearLayoutManager(mMainActivity);
        fileListAdapter = new FileListAdapter(fileInfos, this, mMainActivity, R.layout.file_list_item_layout);
        fileList.setAdapter(fileListAdapter);
        fileList.setLayoutManager(linearLayoutManager);
    }

    protected void makeGridLayout(int spanCount, int layoutRes) {
        linearLayoutManager = new GridLayoutManager(mMainActivity, spanCount);
        fileListAdapter = new FileListAdapter(fileInfos, this, mMainActivity, layoutRes);
        fileList.setAdapter(fileListAdapter);
        fileList.setLayoutManager(linearLayoutManager);

    }

    protected abstract void deleteItem(int adapterPosition);

    protected abstract boolean getFileListFromDir(String filePath);

    public String getFilePath() {
        return filePath;
    }

    public String getFragmentTitle() {
        return fragmentTitle;
    }


    /**
     * 刷新
     *
     * @param filePath
     * @param isBack
     */
    public abstract void load(String filePath, boolean isBack);

    protected void sortReFresh(int sortType) {
        try {
            comparator.setSortType(sortType);
            Collections.sort(fileInfos, comparator);
        } catch (Exception e) {
            comparator.setSortType(SORT_BY_NAME);
            Collections.sort(fileInfos, comparator);
        }
        fileListAdapter.notifyItemRangeChanged(0, fileInfos.size());
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.textView2:
                if (comparator.getSortType() == SORT_BY_NAME)
                    sortReFresh(SORT_BY_NAME_REV);
                else
                    sortReFresh(SORT_BY_NAME);
                break;
            case R.id.textView3:
                if (comparator.getSortType() == SORT_BY_DATE)
                    sortReFresh(SORT_BY_DATE_REV);
                else
                    sortReFresh(SORT_BY_DATE);
                break;
            case R.id.textView5:
                if (comparator.getSortType() == SORT_BY_SIZE)
                    sortReFresh(SORT_BY_SIZE_REV);
                else
                    sortReFresh(SORT_BY_SIZE);
                break;
            case R.id.textView4:
                if (comparator.getSortType() == SORT_BY_TYPE)
                    sortReFresh(SORT_BY_TYPE_REV);
                else
                    sortReFresh(SORT_BY_TYPE);
                break;
            case R.id.imageView5:
                makeLinerLayout();
                view.setBackgroundColor(GIRD_LINER_LAYOUT);
                grid.setBackgroundColor(NORMAL_COLOR);
                break;
            case R.id.imageView3:
                if (spanCount < 7 && linearLayoutManager instanceof GridLayoutManager)
                    spanCount++;
                else if(spanCount>=7 && linearLayoutManager instanceof GridLayoutManager)
                    spanCount=2;
                if (spanCount == 7) {
                    makeGridLayout(spanCount, R.layout.flie_grid_item_layout_40dp);
                } else if (spanCount == 6) {
                    makeGridLayout(spanCount, R.layout.flie_grid_item_layout_60dp);
                } else if (spanCount == 5) {
                    makeGridLayout(spanCount, R.layout.flie_grid_item_layout_80dp);
                } else if (spanCount == 4) {
                    makeGridLayout(spanCount, R.layout.flie_grid_item_layout_100dp);
                } else if (spanCount == 3) {
                    makeGridLayout(spanCount, R.layout.flie_grid_item_layout_120dp);
                } else if (spanCount == 2) {
                    makeGridLayout(spanCount, R.layout.flie_grid_item_layout_140dp);
                } else {
                    makeGridLayout(spanCount, R.layout.flie_grid_item_layout_100dp);
                }
                view.setBackgroundColor(GIRD_LINER_LAYOUT);
                liner.setBackgroundColor(NORMAL_COLOR);
                break;
        }

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
                LogUtils.e(getClass().getName() + ":165", "list的item过多，屏幕无法显示所有的item，当outOfFromPosition还没修正时，尝试获取View时抛出错误");
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

    /**
     * 手指按下-拖动状态改变
     *
     * @param v
     */
    private void fingerDownState(View v) {
        selectPositionChange = false;
        int[] ints = getSelectStartAndEndPosition();
        selectSomePosition(ints[1], longClickPosition);
        //此项选中时将背景透明化
        v.setAlpha(0.7f);
        v.setBackgroundColor(NORMAL_COLOR);
    }

    public void setLongClickPosition(int longClickPosition) {
        this.longClickPosition = longClickPosition;
    }


    /**
     * 将一些区间选项选中
     *
     * @param start
     * @param end
     */
    private void selectSomePosition(int start, int end) {
        if (start != -1 && start != end) {
            int s = start > end ? end : start;
            int e = start > end ? start : end;
            if (fileInfos.get(end).isSelected()) {
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


    /**
     * 获取选中的列表
     *
     * @return
     */
    protected ArrayList<FileInfo> getSelectedList() {
        ArrayList<FileInfo> selectList = new ArrayList<>();//储存删除对象
        for (int i = 0; i < fileInfos.size(); i++) {
            FileInfo fileInfo = fileInfos.get(i);
            if (fileInfo.isSelected()) {
                selectList.add(fileInfo);
            }
        }
        return selectList;
    }

    /**
     * 获取所有选中的item position的开始位置和结束位置
     *
     * @return
     */
    private int[] getSelectStartAndEndPosition() {
        int[] SelectStartAndEndPosition = new int[2];//储存位置
        ArrayList<FileInfo> selectList = getSelectedList();
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
    protected ArrayList<Integer> getSelectPosition(ArrayList<FileInfo> deleteList) {
        ArrayList<Integer> selectPosition = new ArrayList<>();//储存位置
        for (int i = 0; i < deleteList.size(); i++) {
            FileInfo fileInfo = deleteList.get(i);
            selectPosition.add(fileInfos.indexOf(fileInfo));
        }
        return selectPosition;
    }

    @SuppressLint("SetTextI18n")
    protected void clearUnderBar() {
        underBarInfo.setText(fileInfos.size() + getString(R.string.how_many_item));
    }

    @SuppressLint("SetTextI18n")
    public void refreshUnderBar() {
        ArrayList<FileInfo> selectList = getSelectedList();
        String how = selectList.size() == 0 ? "" : getString(R.string.select) + selectList.size() + getString(R.string.how_many_item);
        long size = 0L;
        boolean haveDir = false;
        String big;
        for (FileInfo temp : selectList) {
            if (!temp.isDir()) {
                size += temp.getFileSize();
            } else {
                haveDir = true;
            }
        }
        if (haveDir || selectList.size() == 0) {
            big = "";
        } else {
            big = size > GB ? stayFrieNumber((float) size / GB) + "GB"
                    : size > MB ? stayFrieNumber((float) size / MB) + "MB"
                    : stayFrieNumber((float) size / KB) + "KB";
        }
        underBarInfo.setText(fileInfos.size() + getString(R.string.how_many_item) + "\t\t\t" + how + "\t\t\t" + big);
    }

    private static int makeItemLayoutRes(int spanCount) {
        if (spanCount == 7) {
            return R.layout.flie_grid_item_layout_40dp;
        } else if (spanCount == 6) {
            return R.layout.flie_grid_item_layout_60dp;
        } else if (spanCount == 5) {
            return R.layout.flie_grid_item_layout_80dp;
        } else if (spanCount == 4) {
            return R.layout.flie_grid_item_layout_100dp;
        } else if (spanCount == 3) {
            return R.layout.flie_grid_item_layout_120dp;
        } else if (spanCount == 2) {
            return R.layout.flie_grid_item_layout_140dp;
        } else {
            return R.layout.flie_grid_item_layout_100dp;
        }
    }

    private float distance = 50000;

    @SuppressLint("ClickableViewAccessibility")
    private void sacleIcon() {
        fileList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (event.getPointerCount() == 2 && linearLayoutManager instanceof GridLayoutManager) {
                            float newDistance = getDistance(event);
                            if (newDistance > 100f && newDistance >= distance * 1.3) {
                                if (spanCount > 2) {
                                    spanCount--;
                                    makeGridLayout(spanCount, makeItemLayoutRes(spanCount));
                                }
                                distance = newDistance;
                            } else if (newDistance > 100f && newDistance <= distance / 1.3) {
                                if (spanCount < 7) {
                                    spanCount++;
                                    makeGridLayout(spanCount, makeItemLayoutRes(spanCount));
                                }
                                distance = newDistance;
                            }
                            return true;
                        }
                        break;
                }
                return false;
            }
        });
    }
}
