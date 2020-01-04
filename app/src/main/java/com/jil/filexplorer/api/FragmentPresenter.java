package com.jil.filexplorer.api;

import android.content.Context;
import android.os.Environment;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.jil.filexplorer.R;
import com.jil.filexplorer.adapter.FragmentAdapter;
import com.jil.filexplorer.adapter.SupperAdapter;
import com.jil.filexplorer.ui.*;
import com.jil.filexplorer.utils.ConstantUtils;
import com.jil.filexplorer.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import static com.jil.filexplorer.api.FileOperation.*;
import static com.jil.filexplorer.utils.ConstantUtils.*;
import static com.jil.filexplorer.utils.FileUtils.getFileInfoFromPath;
import static com.jil.filexplorer.utils.FileUtils.stayFrieNumber;

public class FragmentPresenter implements FragmentPresenterCompl.IFragmentPresenter {

    FragmentPresenterCompl.IFragmentView fragmentView;
    private FragmentModel fragmentModel;

    public int current;
    public String mPath;                                       //当前fragment的路径
    public ArrayList<String> historyPath = new ArrayList<>();//历史路径
    public LinearLayoutManager linearLayoutManager;              //预览图列表
    public SupperAdapter<CustomFragment> smallFragmentViewAdapter;

    public FileOperation fileOperation;
    public int fileOperationType;

    public FragmentAdapter fragmentAdapter;
    public FragmentManager fragmentManager;


    public ArrayList<FileInfo> missionList;
    public int startPosition, endPsoition;

    public static FragmentPresenter getInstance(FragmentPresenterCompl.IFragmentView iFragmentView,String startPath){
        if(startPath==null||startPath.trim().equals("")){
            ExplorerApplication.fragmentPresenter=new FragmentPresenter(iFragmentView,Environment.getExternalStorageDirectory().getPath());
        }else
            ExplorerApplication.fragmentPresenter=new FragmentPresenter(iFragmentView,startPath);
        return ExplorerApplication.fragmentPresenter;
    }


    private FragmentPresenter(FragmentPresenterCompl.IFragmentView fragmentView, String startPath) {
        this.fragmentView = fragmentView;
        this.fragmentModel =FragmentModel.getInstance();
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
                        smallFragmentViewAdapter.notifyDataSetChanged();
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

    public void load(int current,String path,boolean isBack){
        getCurrentCustomFragment(current).load(path,isBack);
    }

    public boolean isAllSelect(){
        return getCurrentCustomFragment(current).allSelect;
    }

    public void copySelectFile(){
//        FileShowFragment s =(FileShowFragment)getCurrentCustomFragment();
//        missionList = s.getSelectedList();
//        fileOperationType = MODE_COPY;
//        fragmentView.setPasteVisible(true);
//        s.unSelectAll();
//        fragmentView.setOperationGroupVisible(false);
//        fragmentView.allSelectIco(false);
    }

    public void delecteSelectFile(){
//        fileOperationType = MODE_DELETE;
//        FileShowFragment customViewFragment=(FileShowFragment)getCurrentCustomFragment();
//        missionList = customViewFragment.getSelectedList();//储存删除对象
//        ArrayList<Integer> deletePosition = customViewFragment.getSelectPosition(missionList);//储存位置
//        if (missionList.size() != 0 && deletePosition.size() != 0) {
//            customViewFragment.removeData(missionList);
//        }
//
//        deleteFile();
    }

    private void deleteFile() {
//        if(fileOperationType != MODE_DELETE)
//            return;
//        if (missionList.size() > 0) {
//            new Thread(FileOperation.with(getCurrentCustomFragment().getContext()).delete(missionList)).start();
//        }
//        fragmentView.clearUnderBar();
    }

    public void moveSelectFile(){
//        FileShowFragment customViewFragment=(FileShowFragment) getCurrentCustomFragment();
//        fileOperationType = MODE_MOVE;
//        missionList = customViewFragment.getSelectedList();
//        fragmentView.setPasteVisible(true);
//        customViewFragment.unSelectAll();
    }

    public void load(){
        String s = fragmentView.getPathFromEdit();
        if (s.startsWith("...")) {
            getCurrentCustomFragment().refresh();
        } else {
            getCurrentCustomFragment().load(s, false);
        }
    }

    public boolean load(String path) {
        if (path != null && !path.trim().equals("")){
            getCurrentCustomFragment().load(path, true);
            return true;
        } else {
            fragmentView.showToast("errPath:" + path);
            return false;
        }
    }

    public void selectAllOrNot(){
        boolean isAll = getCurrentCustomFragment().isAllSelect();
        getCurrentCustomFragment().selectAllPositionOrNot(!isAll);
        fragmentView.allSelectIco(!isAll);
    }

    public boolean createNewFile(String fileName,boolean isFile){
        String path = getCurrentCustomFragment().getPath() + File.separator + fileName;
        String successful=null;
        if(isFile)
            successful=FileUtils.newFile(path);
        else
            successful=FileUtils.newFolder(path);
        if(successful!=null) {
            getCurrentCustomFragment().addData(getFileInfoFromPath(getCurrentCustomFragment().getPath() + File.separator + successful));
            return true;
        }else {
            return false;
        }
    }

    public void compressFiles(Context context){
//        FileShowFragment customViewFragment=(FileShowFragment) getCurrentCustomFragment();
//        missionList=customViewFragment.getSelectedList();
//        fileOperationType=MODE_COMPRESS;
//        CompressDialog compressDialog=new CompressDialog(context,R.layout.dialog_compression_layout,"压缩文件参数");
//        compressDialog.showAndSetName(missionList.get(0).getFileName()+"等文件");
    }

    public void intervalSelection() {
        if (startPosition != -1 && endPsoition != -1) {
            FileShowFragment customViewFragment=(FileShowFragment)getCurrentCustomFragment();
            customViewFragment.selectSomePosition(startPosition, endPsoition);
            fragmentView.refreshUnderBar(customViewFragment.getUnderBarMsg());
        }
    }

    public String getPath() {
        return getCurrentCustomFragment().getPath();
    }

    public void pasteFileHere(Context context){
        if(missionList == null ||missionList.size() == 0)
            return;
        ArrayList<FileInfo> topath = new ArrayList<>();
        topath.add(getFileInfoFromPath(getPath()));
        if(fileOperationType== MODE_COPY){
            new Thread(FileOperation.with(context).copy(missionList).to(getPath())).start();
        }else {
            new Thread(FileOperation.with(context).move(missionList).to(getPath())).start();
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
        if (position == current) {
            if (position == 0) {
                fragmentView.setCurrentItem(position + 1);
            } else {
                fragmentView.setCurrentItem(position - 1);
            }
        }
        fragmentAdapter.removePager(position);
    }

    public void smallViewLoadOrRefresh() {
        smallFragmentViewAdapter.notifyDataSetChanged();
//        if (smallFragmentViewAdapter == null) {
//
//
//        } else {
//
//        }
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

    }

    public void makeLinerLayout() {
        getCurrentCustomFragment().makeLinerLayout();
        SettingParam.setColumn(1);
    }

    public void changeView(int spanCount) {
        SettingParam.setColumn(spanCount);
        getCurrentCustomFragment().changeView(spanCount);
    }

    public int getFileInfosSize() {
        return getCurrentCustomFragment().getFileInfosSize();
    }

    public void setUnderBarMsg(String underMsg) {
        fragmentView.refreshUnderBar(underMsg);
    }

    public void setOperationGroupVisible(boolean b) {
        fragmentView.setOperationGroupVisible(b);
    }
}
