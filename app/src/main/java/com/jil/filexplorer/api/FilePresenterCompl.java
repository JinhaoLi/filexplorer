package com.jil.filexplorer.api;

import android.content.Context;

import java.io.FileFilter;

public class FilePresenterCompl {

    public interface IFileModel extends MVPFramework.IModel {

        /**
         * 载入
         */
        void load();

        boolean isNoneData();
    }


    public interface IFileView extends MVPFramework.IView {
        /**
         *路径更新
         * @param okPath 路径
         */
        void upDatePath(String okPath);


        void unSelectItem(int position);

        void selectItem(int i);

        Context getContext();
    }

    public interface IFilePresenter extends MVPFramework.IPresenter {
        void input2Model(String path, FileFilter fileFilter, boolean addHistory);

        boolean isNoneData();
    }

}
