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

import com.jil.filexplorer.activity.MainActivity;
import com.jil.filexplorer.api.FileInfo;
import com.jil.filexplorer.api.Item;
import com.jil.filexplorer.api.SettingParam;
import com.jil.filexplorer.R;
import com.jil.filexplorer.utils.ConstantUtils;
import com.jil.filexplorer.utils.UiUtils;

import java.util.ArrayList;

import static com.jil.filexplorer.api.SettingParam.saveSharedPreferences;
import static com.jil.filexplorer.utils.ConstantUtils.MAX_SPAN_COUNT;
import static com.jil.filexplorer.utils.ConstantUtils.MIN_SPAN_COUNT;
import static com.jil.filexplorer.utils.FileUtils.getDistance;

public abstract class CustomFragment extends Fragment {
    private final static String TAG = "CustomFragment";

    private View rootView;

    protected RecyclerView tList;

    public Bitmap smallView;

    //全选按钮可见状态
    public boolean allSelect = false;

    protected boolean isRecovery;

    protected LinearLayoutManager linearLayoutManager;
    //网格布局计数
    int spanCount = SettingParam.Column;

    protected abstract void initAction();

    public abstract void refreshUnderBar();

    public abstract void makeGridLayout(int spanCount);

    public abstract void makeLinerLayout();

    public abstract boolean isAllSelect();

    public abstract void selectAllPositionOrNot(boolean selectAll);

    public abstract void changeView(int spanCount);

    public abstract String getFragmentTitle();

    public abstract void sortReFresh(int sortType);

    public abstract int getSortType();

    public abstract void addData(FileInfo fileInfoFromPath);

    public abstract String getPath();

    public abstract void refresh();

    public abstract void selectSomePosition(int startPosition, int endPosition);

    public abstract void load(String filePath, boolean isBack);

    public CustomFragment() { }

    @SuppressLint({"ClickableViewAccessibility", "ResourceType"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initView(inflater, container);
        if (!isRecovery) {
            initAction();
        }
        return rootView;
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

    public static int makeItemLayoutRes(int spanCount) {
        if (spanCount == 6) {
            return R.layout.flie_grid_item_layout_60dp;
        } else if (spanCount == 5) {
            return R.layout.flie_grid_item_layout_80dp;
        } else if (spanCount == 4) {
            return R.layout.flie_grid_item_layout_100dp;
        } else if (spanCount == 3) {
            return R.layout.flie_grid_item_layout_120dp;
        } else {
            return R.layout.file_list_item_layout;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != rootView) {
            ((ViewGroup) rootView.getParent()).removeView(rootView);
        }
    }

    public abstract String getUnderBarMsg();

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
                                    SettingParam.setColumn(spanCount);
                                    changeView(spanCount);

                                }
                                distance = newDistance;
                            } else if (newDistance > 100f && newDistance <= distance / 1.3) {
                                if (spanCount < ConstantUtils.MAX_SPAN_COUNT) {
                                    spanCount++;
                                    SettingParam.setColumn(spanCount);
                                    changeView(spanCount);

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
