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
    private FragmentManager fm;
    private Bundle savedInstanceState;
    private Activity activity;

    public FragmentAdapter(FragmentManager fm, List<CustomViewFragment> fragments, Activity activity, Bundle savedInstanceState) {
        super(fm);
        this.fm=fm;
        this.activity=activity;
        this.savedInstanceState=savedInstanceState;
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

//    @Override
//    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
//        Fragment fragment = (Fragment) object;
//        //如果getItemPosition中的值为PagerAdapter.POSITION_NONE，就执行该方法。
//        if (fragments.contains(fragment)) {
//            super.destroyItem(container, position, fragment);
//            return;
//        }
//        //自己执行移除。因为mFragments在删除的时候就把某个fragment对象移除了，所以一般都得自己移除在fragmentManager中的该对象。
//        fm.beginTransaction().remove(fragment).commitNowAllowingStateLoss();
//    }

    @Override
    public int getItemPosition(Object object) {
//        if (!((Fragment) object).isAdded() || !fragments.contains(object)) {
//            return PagerAdapter.POSITION_NONE;
//        }
//        return fragments.indexOf(object);
        return PagerAdapter.POSITION_NONE;
    }

//    @Override
//    public int getItemPosition(Object object) {
//        return POSITION_NONE;
//    }

    public void removePager(int position){
        fragments.remove(position);
        //notifyAll();
        notifyDataSetChanged();
    }

//    @Override
//    public Object instantiateItem(ViewGroup container, int position) {
//        Fragment instantiateItem = ((Fragment) super.instantiateItem(container, position));
//        Fragment item = fragments.get(position);
//        if (instantiateItem == item) {
//            return instantiateItem;
//        } else {
//            //如果集合中对应下标的fragment和fragmentManager中的对应下标的fragment对象不一致，那么就是新添加的，所以自己add进入；这里为什么不直接调用super方法呢，因为fragment的mIndex搞的鬼，以后有机会再补一补。
//            //fm.beginTransaction().add(container.getId(), item).commitNowAllowingStateLoss();
//            return item;
//        }
//    }


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
