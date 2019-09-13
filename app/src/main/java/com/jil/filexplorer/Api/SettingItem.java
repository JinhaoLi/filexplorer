package com.jil.filexplorer.Api;

import android.view.View;

import com.jil.filexplorer.R;

public abstract class SettingItem extends Item{
    private String description;
    private int itemLayout;
    private boolean switchOpen;

    public SettingItem(String name, int id) {
        super(name,id);
    }

    public SettingItem(String name, String description, int id) {
        super(name,id);
        this.description = description;
    }

    public SettingItem(String name, String description, boolean switchOpen, int id) {
        super(name,id);
        this.description = description;
        this.itemLayout = R.layout.setting_item_switch_layout;
        this.switchOpen = switchOpen;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public abstract void click(View v);

    public int getItemLayout() {
        return itemLayout;
    }

    public void setItemLayout(int itemLayout) {
        this.itemLayout = itemLayout;
    }


    public boolean isSwitchOpen() {
        return switchOpen;
    }

    public void setSwitchOpen(boolean switchOpen) {
        this.switchOpen = switchOpen;
    }
}
