package com.jil.filexplorer.Api;

import android.view.View;

import com.jil.filexplorer.R;

public abstract class SettingItem {
    private String name;
    private String description;
    private int itemLayout;
    private boolean switchOpen;
    private int id;

    public SettingItem(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public SettingItem(String name, String description, int id) {
        this.name = name;
        this.description = description;
        this.id = id;
    }

    public SettingItem(String name, String description, boolean switchOpen, int id) {
        this.name = name;
        this.description = description;
        this.itemLayout = R.layout.setting_item_switch_layout;
        this.switchOpen = switchOpen;
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public abstract void click(View v);

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getItemLayout() {
        return itemLayout;
    }

    public void setItemLayout(int itemLayout) {
        this.itemLayout = itemLayout;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isSwitchOpen() {
        return switchOpen;
    }

    public void setSwitchOpen(boolean switchOpen) {
        this.switchOpen = switchOpen;
    }
}
