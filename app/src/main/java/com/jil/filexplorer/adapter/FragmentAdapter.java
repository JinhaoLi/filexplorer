package com.jil.filexplorer.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import com.jil.filexplorer.ui.CustomFragment;
import com.jil.filexplorer.ui.FileShowFragment;

import java.util.List;

public class FragmentAdapter extends FragmentStatePagerAdapter {
    private List<CustomFragment> fragments;

    public FragmentAdapter(FragmentManager fm, List<CustomFragment> fragments) {
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
            CustomFragment fragment=fragments.get(i);
            if(fragment.getPath().equals(path)){
                return i;
            }
        }
        return -1;
    }
}
