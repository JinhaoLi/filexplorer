package com.jil.filexplorer.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jil.filexplorer.api.*;
import com.jil.filexplorer.R;
import com.jil.filexplorer.utils.LogUtils;
import com.jil.filexplorer.utils.ToastUtils;
import net.lingala.zip4j.model.ZipParameters;

import java.io.File;
import java.io.FileFilter;

import static com.jil.filexplorer.utils.ConstantUtils.NORMAL_COLOR;
import static com.jil.filexplorer.utils.ConstantUtils.SELECTED_COLOR;

@SuppressLint("ValidFragment")
public class FileShowFragment extends CustomFragment implements FileChangeListener, FilePresenterCompl.IFileView {
    private static final String ARG_PARAM = "DirPath";
    private static final int ERR_MESSAGE = -1;
    private static final int UPDATE_MESSAGE = 1;

    private FilePresenter filePresenter;

    private FragmentPresenterCompl.IFragmentPresenter iFragmentPresenter;

    public FileShowFragment(FragmentPresenterCompl.IFragmentPresenter iFragmentPresenter) {
        super();
        this.iFragmentPresenter = iFragmentPresenter;
    }

    public static FileShowFragment newInstance(String param2, FragmentPresenterCompl.IFragmentPresenter iFragmentPresenter) {
        FileShowFragment fragment = new FileShowFragment(iFragmentPresenter);
        Bundle args = new Bundle();
        args.putString(ARG_PARAM, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public String getUnderBarMsg() {
        return filePresenter.getUnderBarMsg();
    }

    @Override
    public void refreshUnderBar() {
        filePresenter.refreshUnderBar();
    }

    @Override
    public void selectSomePosition(int startPosition, int endPosition) {
        filePresenter.selectSomePosition(startPosition, endPosition);
    }

    @Override
    public String getFragmentTitle() {
        return filePresenter.fragmentTitle;
    }

    @Override
    protected void initAction() {
        if (filePresenter == null)
            filePresenter = new FilePresenter(this, getContext());

        if (getArguments() != null) {
            if (filePresenter.path == null)
                filePresenter.path = getArguments().getString(ARG_PARAM);
        }
        init();
        load(filePresenter.path, false);

    }

    @Override
    public void makeLinerLayout() {
        if (linearLayoutManager != null) {
            filePresenter.saveSharedPreferences(1);
        }
        linearLayoutManager = new LinearLayoutManager(getContext());
        filePresenter.tListAdapter.setItemLayoutRes(R.layout.file_list_item_layout);
        try {
            tList.setAdapter(filePresenter.tListAdapter);
            tList.setLayoutManager(linearLayoutManager);
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e.getMessage() + getString(R.string.eat_err));
        }

    }

    @Override
    public boolean isAllSelect() {
        if (filePresenter.isNoneData()) {
            return false;
        }
        return filePresenter.isAllSelected();
    }

    @Override
    public void selectAllPositionOrNot(boolean selectAll) {
        filePresenter.selectAllPositionOrNot(selectAll);
        filePresenter.notifyChanged();
        filePresenter.refreshUnderBar();
    }

    @Override
    public void load(String filePath, boolean isBack) {
        filePresenter.input2Model(filePath,isBack);
    }


    @Override
    public void change() {
        load(filePresenter.path, false);
    }


    /**
     * 返回当前按什么方式排序
     *
     * @return
     */
    public int getSortType() {
        return filePresenter.getSortType();
    }


    @Override
    public void addData(FileInfo fileInfo) {
        filePresenter.addDate(fileInfo);
    }

    @Override
    public String getPath() {
        if (filePresenter == null)
            return "null";
        return filePresenter.path;
    }

    @Override
    public void refresh() {
        load(filePresenter.path, false);
    }

    @Override
    public void unSelectItem(int position) {
        selectOrNot_Item(position, false);
    }

    @Override
    public void selectItem(int position) {
        selectOrNot_Item(position, true);
    }

    private void selectOrNot_Item(int position, boolean isSelected) {
        View selectItem = linearLayoutManager.findViewByPosition(position);
        if (selectItem != null) {
            if (isSelected) {
                selectItem.setBackgroundColor(SELECTED_COLOR);
            } else {
                selectItem.setBackgroundColor(NORMAL_COLOR);
            }
        }

    }

    /**
     * 排序
     *
     * @param sortType 排序方式
     */
    @Override
    public void sortReFresh(int sortType) {
        filePresenter.sort(sortType);
    }


    @Override
    public void init() {
        if (SettingParam.Column > 2)
            linearLayoutManager = new GridLayoutManager(getContext(), SettingParam.Column);
        else
            linearLayoutManager = new LinearLayoutManager(getContext());
        tList.setAdapter(filePresenter.tListAdapter);
        tList.setLayoutManager(linearLayoutManager);

    }

    @Override
    public void update() {
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == ERR_MESSAGE) {
                ToastUtils.showToast(getContext(), (String) msg.obj, 1000);
            }
            if (msg.what == UPDATE_MESSAGE) {
                filePresenter.tListAdapter.notifyDataSetChanged();
                iFragmentPresenter.update();
                if (filePresenter.isAddHistory()) {
                    iFragmentPresenter.addHistory((String) msg.obj);
                }
                refreshUnderBar();
            }
        }
    };

    @Override
    public void upDatePath(String okPath) {
        Message message = Message.obtain();
        ;
        message.what = UPDATE_MESSAGE;
        message.obj = okPath;
        handler.sendMessage(message);
    }

    @Override
    public void setErr(String msg) {
        Message message = Message.obtain();
        message.what = ERR_MESSAGE;
        message.obj = msg;

        handler.sendMessage(message);
    }


    @Override
    public void changeView(int spanCount) {
        filePresenter.saveSharedPreferences(spanCount);
        filePresenter.changeItemLayout();
        tList.setAdapter(filePresenter.tListAdapter);
        linearLayoutManager = new GridLayoutManager(getContext(), spanCount);
        tList.setLayoutManager(linearLayoutManager);

    }

    @Nullable
    @Override
    public Context getContext() {
        return super.getContext();
    }

    @Override
    public void reNameDialog(String title, int layoutId, String oldName, final int type) {
        InputDialog dialog = new InputDialog(filePresenter.mContext, layoutId, title) {
            @Override
            public void queryButtonClick(View v) {

            }

            @Override
            public void queryButtonClick(View v, String name) {
                filePresenter.reNameSelectFile(name);
            }

        };
        dialog.showAndSetName(oldName);
    }

    @Override
    public void showCompressDialog(String parentName) {
        CompressDialog compressDialog = new CompressDialog(getContext(), R.layout.dialog_compression_layout, "配置压缩文件参数") {
            @Override
            public void doIt(ZipParameters zipParameters, String zipName) {
                filePresenter.compressFile(zipParameters, zipName);
            }
        };
        compressDialog.showAndSetName(parentName);
    }

    public FilePresenter getFilePresent() {
        return filePresenter;
    }
}
