package com.jil.filexplorer.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jil.filexplorer.Activity.MainActivity;
import com.jil.filexplorer.Api.Item;
import com.jil.filexplorer.Api.SettingParam;
import com.jil.filexplorer.R;
import com.jil.filexplorer.adapter.SettingItemAdapter;
import com.jil.filexplorer.utils.ConstantUtils;
import com.jil.filexplorer.utils.LogUtils;
import com.jil.filexplorer.utils.UiUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.jil.filexplorer.Api.SettingParam.saveSharedPreferences;
import static com.jil.filexplorer.utils.ConstantUtils.MAX_SPAN_COUNT;
import static com.jil.filexplorer.utils.ConstantUtils.MIN_SPAN_COUNT;
import static com.jil.filexplorer.utils.FileUtils.getDistance;

public abstract class CustomFragment<T extends Item> extends Fragment {
    private final static String TAG = "CustomFragment";
    /**
     * 页面标题
     */
    protected String fragmentTitle;
    /**
     * 主Activity
     */
    protected MainActivity mMainActivity;
    private View rootView;
    /**
     * 底部条
     */
    private String underBarMessage;
    /**
     * 路径
     */
    protected String path;
    /**
     * 数据集
     */
    protected ArrayList<T> ts;
    protected RecyclerView.Adapter tListAdapter;
    protected RecyclerView tList;
    protected String underBarMsg;

    /**
     * 菜单可见
     */
    public boolean menuVisible;
    public Bitmap smallView;
    protected Comparator comparator;
    //全选按钮可见状态
    public boolean allSelect = false;

    protected boolean isRecovery;

    protected LinearLayoutManager linearLayoutManager;
    //网格布局计数
    int spanCount = SettingParam.Column;

    protected abstract void initAction();
    protected abstract void deleteItem(int adapterPosition);
    protected abstract boolean initDates(String filePath, boolean isBack);
    public abstract int refreshUnderBar();
    public abstract int getFileInfosSize();
    public abstract void setUnderBarMsg(String underMsg);
    public abstract void makeGridLayout(int spanCount, int layoutRes);
    public abstract void makeLinerLayout();
    public abstract boolean isAllSelect();
    /**
     * 载入刷新
     *
     * @param filePath
     * @param isBack
     */
    public abstract void load(String filePath, boolean isBack);

    public void addDates(ArrayList<T> fileInfos) {
        this.ts.addAll(fileInfos);
        tListAdapter.notifyDataSetChanged();
    }

    public void addDate(T fileInfos) {
        this.ts.add(fileInfos);
        tListAdapter.notifyDataSetChanged();
    }

    public void removeDates(ArrayList<T> fileInfos) {
        this.ts.removeAll(fileInfos);
        tListAdapter.notifyDataSetChanged();
    }
    public CustomFragment() {
    }

    public CustomFragment(Activity activity, String filePath) {
        this.mMainActivity = (MainActivity) activity;
        this.path = filePath;
    }

    @SuppressLint({"ClickableViewAccessibility", "ResourceType"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = initView(inflater, container);
        if (!isRecovery) {
            initAction();
        }
        return v;
    }

    protected View initView(LayoutInflater inflater, ViewGroup container) {
        if (rootView == null) {
            isRecovery = false;
            rootView = inflater.inflate(R.layout.fragment_file_view_layout, container, false);
            tList = rootView.findViewById(R.id.file_list_view);

        } else {
            isRecovery = true;
        }
        return rootView;
    }

    private void refreshSmallView() {
        if (tList != null)
            smallView = UiUtils.createViewBitmap(smallView, tList);
    }

    public Bitmap getSmallView() {
        refreshSmallView();
        return smallView;
    }

    public String getPath() {
        return path;
    }

    public String getFragmentTitle() {
        return fragmentTitle;
    }

    public void initSort(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    public void sortReFresh(int sortType) {
        try {
            //comparator.setSortType(sortType);
            Collections.sort(ts, comparator);
        } catch (Exception e) {
            //comparator.setSortType(SORT_BY_NAME);
            Collections.sort(ts, comparator);
        }
        tListAdapter.notifyItemRangeChanged(0, ts.size());
    }

    public void load() {
        load(path, true);
    }

    protected void clearUnderBar() {
        mMainActivity.clearUnderBar();
    }

    public int getDatesSize() {
        if (ts != null)
            return ts.size();
        else
            return 0;
    }

    public void setUnderBarMessage(String underBarMessage) {
        this.underBarMessage = underBarMessage;
    }

    public static int makeItemLayoutRes(int spanCount) {
        if (spanCount == MAX_SPAN_COUNT) {
            return R.layout.flie_grid_item_layout_60dp;
        } else if (spanCount == MAX_SPAN_COUNT - 1) {
            return R.layout.flie_grid_item_layout_80dp;
        } else if (spanCount == MIN_SPAN_COUNT + 1) {
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

    public String getUnderBarMsg() {
        return underBarMessage;
    }

    private float distance = 50000;
    @SuppressLint("ClickableViewAccessibility")
    protected void enlargeIcon() {
        tList.setOnTouchListener(new View.OnTouchListener() {
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
                                if (spanCount > ConstantUtils.MIN_SPAN_COUNT) {
                                    spanCount--;
                                    makeGridLayout(spanCount, makeItemLayoutRes(spanCount));
                                    SettingParam.setColumn(spanCount);
                                }
                                distance = newDistance;
                            } else if (newDistance > 100f && newDistance <= distance / 1.3) {
                                if (spanCount < ConstantUtils.MAX_SPAN_COUNT) {
                                    spanCount++;
                                    makeGridLayout(spanCount, makeItemLayoutRes(spanCount));
                                    SettingParam.setColumn(spanCount);
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
