package com.jil.filexplorer.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jil.filexplorer.api.*;
import com.jil.filexplorer.R;
import com.jil.filexplorer.utils.FileUtils;
import com.jil.filexplorer.utils.LogUtils;
import com.jil.filexplorer.utils.ToastUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

import static com.jil.filexplorer.utils.ConstantUtils.NORMAL_COLOR;
import static com.jil.filexplorer.utils.ConstantUtils.SELECTED_COLOR;

@SuppressLint("ValidFragment")
public class FileShowFragment extends CustomFragment<FileInfo> implements FileChangeListenter, FilePresenterCompl.IFileView {
    private static final String ARG_PARAM = "DirPath";

    private FilePresenter filePresenter;

    private FragmentPresenterCompl.IFragmentPresenter iFragmentPresenter;

    public FileShowFragment(FragmentPresenterCompl.IFragmentPresenter iFragmentPresenter) {
        super();
        this.iFragmentPresenter=iFragmentPresenter;
    }

    public static FileShowFragment newInstance(String param2,FragmentPresenterCompl.IFragmentPresenter iFragmentPresenter) {
        FileShowFragment fragment = new FileShowFragment(iFragmentPresenter);
        Bundle args = new Bundle();
        args.putString(ARG_PARAM, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(filePresenter==null)
            filePresenter =new FilePresenter(this,getContext());

        if (getArguments() != null) {
            filePresenter.path = getArguments().getString(ARG_PARAM);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container) {
        return super.initView(inflater, container);
    }

    @Override
    public String getUnderBarMsg() {
        return filePresenter.getUnderBarMsg();
    }

    @Override
    public void unSelectAll() {
        filePresenter.unSelectAll();
    }

    @Override
    public void selectSomePosition(int startPosition, int endPosition) {
        filePresenter.selectSomePosition(startPosition,endPosition);
    }

    @Override
    public String getFragmentTitle() {
        return filePresenter.fragmentTitle;
    }


    @Override
    protected void initAction() {
        enlargeIcon();
        load(filePresenter.path, false);

    }

    @Override
    public void deleteItem(int adapterPosition) {
        filePresenter.deleteFileInfoItem(adapterPosition);
        filePresenter.tListAdapter.notifyDataSetChanged();
        refreshUnderBar();
    }

    @Override
    protected boolean initDates(String filePath, boolean isBack) {
        return false;
    }


    @Override
    public int getFileInfosSize() {
        return filePresenter.getFileInfosSize();
    }

    @Override
    public void makeGridLayout(int spanCount) {
        if(linearLayoutManager!=null){
            filePresenter.saveSharedPreferences(spanCount);
        }
        linearLayoutManager = new GridLayoutManager(getContext(), spanCount);
        tList.setAdapter(filePresenter.tListAdapter);
        tList.setLayoutManager(linearLayoutManager);
    }

    public void makeLinerLayout() {
        if(linearLayoutManager!=null){
            filePresenter.saveSharedPreferences(1);
        }
        linearLayoutManager = new LinearLayoutManager(getContext());
        filePresenter.tListAdapter.setItemLayoutRes(R.layout.file_list_item_layout);
        try{
            tList.setAdapter(filePresenter.tListAdapter);
            tList.setLayoutManager(linearLayoutManager);
        }catch (Exception e){
            LogUtils.e(e.getMessage(),e.getMessage()+getString(R.string.eat_err));
        }

    }

    @Override
    public boolean isAllSelect() {
        if(filePresenter.isNoneData()){
            return false;
        }
        return filePresenter.isAllSelected();
    }

    @Override
    public void selectAllPositionOrNot(boolean selectAll) {
        filePresenter.selectAllPositionOrNot(selectAll);
    }

    @Override
    public void load(String filePath, boolean isBack) {
        filePresenter.input2Model(filePath, new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return true;
            }
        },isBack);
    }


    @Override
    public void change() {
        load(filePresenter.path,true);
    }

    @Override
    public void refreshUnderBar() {
        filePresenter.refreshUnderBar();
    }


    /**
     * 返回当前按什么方式排序
     * @return
     */
    public int getSortType(){
        return filePresenter.getSortType();
    }

    @Override
    public void removeData(ArrayList<FileInfo> deleteList) {
        filePresenter.removeDates(deleteList);
    }

    @Override
    public void addData(FileInfo fileInfo) {
        filePresenter.addDate(fileInfo);
    }

    @Override
    public void addMoreData(ArrayList<FileInfo> inFiles) {
        filePresenter.addMoreData(inFiles);
    }

    @Override
    public String getPath() {
        return filePresenter.path;
    }

    @Override
    public void refresh() {
        load(filePresenter.path,false);
    }


    /**
     * 排序
     * @param sortType 排序方式
     */
    @Override
    public void sortReFresh(int sortType) {
        filePresenter.sort(sortType);
//        FileComparator fileComparator = (FileComparator) comparator;
//        try {
//            fileComparator.setSortType(sortType);
//            Collections.sort(filePresenter.getData(), fileComparator);
//        } catch (Exception e) {
//            fileComparator.setSortType(SORT_BY_NAME);
//            Collections.sort(filePresenter.getData(), fileComparator);
//        }
//        filePresenter.tListAdapter.notifyItemRangeChanged(0, filePresenter.getData().size());
    }


    @Override
    public void init() {
        changeView(SettingParam.Column);
    }

    @Override
    public void update() {

    }

    @Override
    public void upDatePath(final String okPath) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                filePresenter.tListAdapter.notifyDataSetChanged();

                iFragmentPresenter.update();
                if(filePresenter.isAddHistory()){
                    iFragmentPresenter.addHistory(okPath);
                }
                refreshUnderBar();
            }
        });
    }

    @Override
    public void unSelectItem(int position) {
        selectOrNot_Item(position,false);
    }

    @Override
    public void selectItem(int position) {
        selectOrNot_Item(position,true);
    }

    private void selectOrNot_Item(int position,boolean isSelected){
        View selectItem = linearLayoutManager.findViewByPosition(position);
        if (selectItem != null){
            if(isSelected){
                selectItem.setBackgroundColor(SELECTED_COLOR);
            }else {
                selectItem.setBackgroundColor(NORMAL_COLOR);
            }
        }

    }


    public void missionSuccess(final String okPath) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                filePresenter.tListAdapter.notifyDataSetChanged();

            }
        });
    }

    @Override
    public void changeView(int spanCount) {
        filePresenter.changeItemLayout();
        if(spanCount<2)
            makeLinerLayout();
        else
            makeGridLayout(spanCount);

    }


    @Override
    public void setErr(String msg) {
        ToastUtils.showToast(getContext(), msg, 1000);
    }


    @Nullable
    @Override
    public Context getContext() {
        return super.getContext();
    }
}
