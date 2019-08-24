package com.jil.filexplorer.adapter;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import com.jil.filexplorer.ui.CustomViewFragment;
import com.jil.filexplorer.ui.FileShowFragment;

import java.util.ArrayList;
import java.util.List;

public class FragmentAdapter extends FragmentStatePagerAdapter {
    private List<CustomViewFragment> fragments;

    public FragmentAdapter(FragmentManager fm, List<CustomViewFragment> fragments) {
        super(fm);
        this.fragments=fragments;
    }

    @Override
    public Fragment getItem(int position) {
       return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    public void removePager(int position){
        fragments.remove(position);
        notifyDataSetChanged();
    }


    public int findPositionByFilePath(String path){
        for(int i =0;i<getCount();i++){
            FileShowFragment fileShowFragment=(FileShowFragment) fragments.get(i);
            if(fileShowFragment.getFilePath().equals(path)){
                return i;
            }
        }
        return -1;
    }
}
