package com.jil.filexplorer.utils;

import android.content.Context;
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import com.jil.filexplorer.R;
import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;

public class MenuUtils {

    public static boolean addMenu(Menu menu) {
        //单选
        /*SubMenu subMenu = menu.addSubMenu(0, 1, 0, R.string.menu_item_sort).setIcon(R.drawable.ic_sort);
        subMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        addMenuInGroup(subMenu, 11, 0, R.string.menu_item_sort_name);
        addMenuInGroup(subMenu, 12, 1, R.string.menu_item_sort_date);
        addMenuInGroup(subMenu, 13, 2, R.string.menu_item_sort_size);
        addMenuInGroup(subMenu, 14, 3, R.string.menu_item_sort_type);
        subMenu.setGroupCheckable(0, true, true);
        subMenu.getItem(0).setChecked(true);*/


        addMenu(menu, 1, 1, R.string.copy, R.drawable.ic_content_copy_black_24dp);
        addMenu(menu, 2, 2, R.string.paste, R.drawable.ic_content_paste_black_24dp);
        addMenu(menu, 3, 3, R.string.delete, R.drawable.ic_delete_black_24dp);
        addMenu(menu, 4, 4, R.string.add_new, R.drawable.ic_add);
        addMenu(menu, 5, 5, R.string.cut, R.drawable.ic_content_cut_black_24dp);
        addMenu(menu, 6, 6, R.string.refresh, R.drawable.ic_refresh);
        addMenu(menu, 7, 7, R.string.close, R.drawable.ic_close_black_24dp);

        /*addMenuInGroup(menu, 5, 4, R.string.setting);

        addMenuInGroup(menu, 6, 5, R.string.about);

        addMenuInGroup(menu, 7, 6, R.string.exit);*/
        return true;
    }

    private static void addMenuInGroup(Menu menu, int itemId, int order, int titleRes) {
        addMenu(menu, itemId, order, titleRes, -1);
    }

    private static void addMenu(Menu menu, int itemId, int order, int titleRes, int icon) {
        MenuItem menuItem = menu.add(0, itemId, order, titleRes);
        if (icon > 0) {
            LogUtils.i("main_icon", "" + icon);
            menuItem.setIcon(icon);
        }
        if (itemId != 10) {
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        } else {
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
    }

    public static SwipeMenuItem createSwipeMenu(Context context, int backgroundRes, int titleRes, int textColor) {
        return new SwipeMenuItem(context)
                .setText(context.getString(titleRes)) // 文字。
                .setTextColor(textColor) // 文字颜色。
                .setTextSize(15) // 文字大小。
                .setWidth(120)
                .setWeight(1)
                .setHeight(-1)
                .setBackgroundDrawable(backgroundRes);
    }

    public static void fileSwipeMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, Context context) {
        SwipeMenuItem deleteItem = createSwipeMenu(context, R.drawable.button_click_type_red, R.string.delete, Color.WHITE);
        SwipeMenuItem detailItem = createSwipeMenu(context, R.drawable.button_click_type, R.string.detail, Color.WHITE);
        SwipeMenuItem copyItem = createSwipeMenu(context, R.drawable.button_click_type, R.string.copy, Color.WHITE);
        SwipeMenuItem cutItem = createSwipeMenu(context, R.drawable.button_click_type, R.string.cut, Color.WHITE);

        swipeRightMenu.addMenuItem(deleteItem);// 添加一个按钮到右侧侧菜单。.
        swipeRightMenu.addMenuItem(detailItem);// 添加一个按钮到右侧侧菜单。.
        swipeLeftMenu.addMenuItem(copyItem);// 添加一个按钮到左侧侧菜单。.
        swipeLeftMenu.addMenuItem(cutItem);// 添加一个按钮到左侧侧菜单。.
    }


}
