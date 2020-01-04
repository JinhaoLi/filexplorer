package com.jil.filexplorer.api;

import android.content.Context;
import androidx.fragment.app.FragmentManager;
import com.jil.filexplorer.ui.CustomFragment;

public class FragmentPresenterCompl {

    public interface IFragmentModel extends MVPFramework.IModel{

        void init();


        void add(CustomFragment customFragment);
    }

    public interface IFragmentView extends MVPFramework.IView{
        void setPasteVisible(boolean pasteVisible);

        String getPathFromEdit();

        void clearUnderBar();

        void allSelectIco(boolean b);

        void setCurrentItem(int position);

        void showToast(String msg);

        void refreshUnderBar(String msg);

        void setOperationGroupVisible(boolean b);

        FragmentManager linkFragmentManager();

        Context getContext();
    }

    public interface IFragmentPresenter extends MVPFramework.IPresenter{

        int getSortType();

        void sortReFresh(int sort);

        void update();

        void addHistory(String okPath);
    }
}
