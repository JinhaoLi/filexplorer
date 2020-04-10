package com.jil.filexplorer.model;

import com.jil.filexplorer.presenter.FilePresenter;
import com.jil.filexplorer.presenter.FragmentPresenterCompl;
import com.jil.filexplorer.ui.CustomFragment;
import com.jil.filexplorer.ui.FileShowFragment;


import java.util.ArrayList;
import java.util.List;

public class FragmentModel implements FragmentPresenterCompl.IFragmentModel {

    public List<CustomFragment> fragments;
    private static FragmentModel fragmentModel =null;

    public static FragmentModel getInstance(){
        fragmentModel=new FragmentModel();
        return fragmentModel;
    }

    private FragmentModel() {
        init();
    }

    @Override
    public void init() {
        fragments =new ArrayList<>();
    }

    @Override
    public void add(CustomFragment customFragment) {
        fragments.add(customFragment);
    }

    @Override
    public FilePresenter getFilePresent(int index) {
        FileShowFragment fragment= (FileShowFragment) fragments.get(index);
        return fragment.getFilePresent();
    }


    public CustomFragment getCurrentCustomFragment(int current){
        return fragments.get(current);
    }

}
