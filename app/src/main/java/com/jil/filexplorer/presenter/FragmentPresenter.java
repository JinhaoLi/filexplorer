package com.jil.filexplorer.presenter;

import android.content.Context;
import android.os.Environment;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.jil.filexplorer.R;
import com.jil.filexplorer.activity.ProgressActivity;
import com.jil.filexplorer.adapter.FragmentAdapter;
import com.jil.filexplorer.adapter.SupperAdapter;
import com.jil.filexplorer.api.FileChangeListener;
import com.jil.filexplorer.api.FileOperation;
import com.jil.filexplorer.api.SettingParam;
import com.jil.filexplorer.model.FragmentModel;
import com.jil.filexplorer.ui.*;
import com.jil.filexplorer.utils.ConstantUtils;
import com.jil.filexplorer.utils.FileUtils;
import com.jil.filexplorer.utils.LogUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import static com.jil.filexplorer.custom.ExplorerApp.fragmentPresenter;
import static com.jil.filexplorer.api.FileOperation.*;
import static com.jil.filexplorer.utils.ConstantUtils.*;
import static com.jil.filexplorer.utils.FileUtils.getFileInfoFromPath;

public class FragmentPresenter implements FragmentPresenterCompl.IFragmentPresenter, FileChangeListener {

    private FragmentPresenterCompl.IFragmentView fragmentView;
    private FragmentModel fragmentModel;
    public int current;
    public ArrayList<String> historyPath;
    public LinearLayoutManager linearLayoutManager;
    public SupperAdapter<CustomFragment> smallFragmentViewAdapter;
    public int fileOperationType;
    public FragmentAdapter fragmentAdapter;
    public int startPosition, endPosition;

    public static FragmentPresenter getInstance(FragmentPresenterCompl.IFragmentView iFragmentView,String startPath){
        if(startPath==null||startPath.trim().equals("")){
            fragmentPresenter=new FragmentPresenter(iFragmentView,Environment.getExternalStorageDirectory().getPath());
        }else
            fragmentPresenter=new FragmentPresenter(iFragmentView,startPath);
        return fragmentPresenter;
    }


    private FragmentPresenter(FragmentPresenterCompl.IFragmentView fragmentView, String startPath) {
        this.fragmentView = fragmentView;
        this.fragmentModel =FragmentModel.getInstance();
        this.historyPath=new ArrayList<>();
        fragmentAdapter = new FragmentAdapter(fragmentView.linkFragmentManager(), fragmentModel.fragments);
        fragmentModel.add(FileShowFragment.newInstance(startPath,this));
    }

    public void initSmallView(){
        smallFragmentViewAdapter = new SupperAdapter<CustomFragment>(fragmentModel.fragments,getContext(), new SupperAdapter.OnItemClickListener<CustomFragment>() {
            @Override
            public void onItemClick(SupperAdapter.VH holder, CustomFragment data, int position) {
                holder.itemView.setBackgroundColor(ConstantUtils.SELECTED_COLOR);
                if (position != current) {
                    if (linearLayoutManager.findViewByPosition(current) != null)
                        Objects.requireNonNull(linearLayoutManager.findViewByPosition(current)).setBackgroundColor(NORMAL_COLOR);
                    fragmentView.setCurrentItem(position);
                }

            }
        }) {
            @Override
            public void onViewAttachedToWindow(@NonNull VH holder) {
                super.onViewAttachedToWindow(holder);
                if (holder.getAdapterPosition() == current) {
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
                if (position == current) {
                    holder.itemView.setBackgroundColor(ConstantUtils.SELECTED_COLOR);
                } else {
                    holder.itemView.setBackgroundColor(NORMAL_COLOR);
                }
                holder.getView(R.id.imageView9).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeFragmentPage(position);
                    }
                });
            }
        };
        linearLayoutManager = new LinearLayoutManager(getContext());
    }

    private Context getContext() {
        return fragmentView.getContext();
    }


    public CustomFragment getCurrentCustomFragment(){
        return getCurrentCustomFragment(current);
    }

    private CustomFragment getCurrentCustomFragment(int current){
        return fragmentModel.getCurrentCustomFragment(current);
    }

    public void refresh(int current, String path, boolean isBack){
        getCurrentCustomFragment(current).load(path,isBack);
    }

    public boolean isAllSelect(){
        return getCurrentCustomFragment(current).isAllSelect();
    }

    public void refresh(){
        String s = fragmentView.getPathFromEdit();
        if (s.startsWith("...")) {
            getCurrentCustomFragment().refresh();
        } else {
            getCurrentCustomFragment().load(s, false);
        }
    }

    public boolean load(String path) {
        if (path != null && !path.trim().equals("")){
            getCurrentCustomFragment().load(path, false);
            return true;
        } else {
            fragmentView.showToast("errPath:" + path);
            return false;
        }
    }

    public void selectAllOrNot(){
        boolean isAll = isAllSelect();
        getCurrentCustomFragment().selectAllPositionOrNot(!isAll);
        changeAllSelectIco(!isAll);
    }

    public void changeAllSelectIco(boolean isAll){
        fragmentView.changeAllSelectIco(isAll);
    }

    public boolean createNewFile(String fileName,boolean isFile){
        String path = getCurrentCustomFragment().getPath() + File.separator + fileName;
        String successful;
        if(isFile)
            successful=FileUtils.newFile(path);
        else
            successful=FileUtils.newFolder(path);
        if(successful!=null) {
            getCurrentCustomFragment().addData(getFileInfoFromPath(getCurrentCustomFragment().getPath() + File.separator + successful));
            getCurrentCustomFragment().refreshUnderBar();
            return true;
        }else {
            return false;
        }
    }

    public void intervalSelection() {
        if (startPosition != -1 && endPosition != -1) {
            FileShowFragment customViewFragment=(FileShowFragment)getCurrentCustomFragment();
            customViewFragment.selectSomePosition(startPosition, endPosition);
            fragmentView.refreshUnderBar(customViewFragment.getUnderBarMsg());
        }
    }

    public String getPath() {
        return getCurrentCustomFragment().getPath();
    }

    public void pasteFileHere(Context context){
        FileOperation fileOperation;
        if(inFiles == null ||inFiles.size() == 0)
            return;

        if(fileOperationType== MODE_COPY)
            fileOperation=FileOperation.with(context).copy().to(getPath());
        else if(fileOperationType==MODE_MOVE)
            fileOperation=FileOperation.with(context).move().to(getPath());
        else
            return;
        fileOperation.setFileChangeListener(this);
        ProgressActivity.start(context,fileOperation.getId());
        new Thread(fileOperation).start();
    }

    public void slideToPager(String path) {
        int position = findPositionByPath(path);
        if (position >= 0) {//已经有此页面
            fragmentView.setCurrentItem(position);
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
        fragmentModel.add(FileShowFragment.newInstance(path,this));
        fragmentAdapter.notifyDataSetChanged();
        fragmentView.setCurrentItem(fragmentModel.fragments.size());
    }


     public int findPositionByPath(String path){
        return fragmentAdapter.findPositionByFilePath(path);
     }

    public int size() {
        return fragmentModel.fragments.size();
    }

    /**
     * 移除fragment
     */
    public void removeFragmentPage(int position) {
        if(size()==1){
            fragmentView.exit();
            return;
        }
        if (position == current) {
            if (position == 0) {
                fragmentView.setCurrentItem(position + 1);
            } else {
                fragmentView.setCurrentItem(position - 1);
            }
        }
        fragmentAdapter.removePager(position);
        smallViewLoadOrRefresh();
        LogUtils.d(getClass().getName(),"removeFragmentPage():size()=="+size()+")");

    }

    /**
     * 区间选择 ico
     *
     * @param visible
     * @param start
     * @param end
     */
    public void setSelectIntervalIco(boolean visible, int start, int end) {
        this.startPosition = start;
        this.endPosition = end;
        fragmentView.setSelectIntervalIco(visible);

    }

    public void smallViewLoadOrRefresh() {
        smallFragmentViewAdapter.notifyDataSetChanged();
    }

    @Override
    public int getSortType() {
        return getCurrentCustomFragment().getSortType();
    }

    @Override
    public void sortReFresh(int sort) {
        getCurrentCustomFragment().sortReFresh(sort);
    }

    @Override
    public void update() {
        fragmentView.update();
    }

    @Override
    public void addHistory(String okPath) {
        while (historyPath.size() > 10) {
            historyPath.remove(0);
        }
        historyPath.add(0,okPath);
    }

    @Override
    public void showToast(String msg) {
        fragmentView.showToast(msg);
    }

    public void makeLinerLayout() {
        getCurrentCustomFragment().makeLinerLayout();
        SettingParam.setColumn(1);
    }

    public void changeView(int spanCount) {
        SettingParam.setColumn(spanCount);
        getCurrentCustomFragment().changeView(spanCount);
    }

    public void setUnderBarMsg(String underMsg) {
        fragmentView.refreshUnderBar(underMsg);
    }

    public void setPasteVisible(boolean visible) {
        fragmentView.setPasteVisible(visible);
    }

    @Override
    public void change() {
        refresh();
    }
}
