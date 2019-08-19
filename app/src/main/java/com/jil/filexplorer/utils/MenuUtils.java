package com.jil.filexplorer.utils;

import android.content.Context;
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import androidx.appcompat.view.menu.MenuBuilder;

import com.jil.filexplorer.R;
import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;

import java.lang.reflect.Method;

public class MenuUtils {

    public static boolean addMenu(Menu menu) {
        //单选
        addMenu(1,menu, 1, 1, R.string.copy, R.drawable.ic_content_copy_black_24dp);
        addMenu(1,menu, 3, 2, R.string.delete, R.drawable.ic_delete_black_24dp);
        addMenu(1,menu, 5, 3, R.string.cut, R.drawable.ic_content_cut_black_24dp);
        //addMenu(3,menu, 8, 4, R.string.all_choose, R.drawable.ic_check_box_outline_blank_black_24dp);
        addMenu(2,menu, 2, 4, R.string.paste, R.drawable.ic_content_paste_black_24dp);

        addMenu(2,menu, 4, 5, R.string.add_new, R.drawable.ic_add);
        addMenu(2,menu, 6, 6, R.string.refresh, R.drawable.ic_refresh);
        addMenu(2,menu, 7, 7, R.string.close, R.drawable.ic_close_black_24dp);
        addMenu(3,menu, 8, 8, R.string.all_choose, R.drawable.ic_check_box_outline_blank_black_24dp);
        return true;
    }

    private static void addMenu(int groupId,Menu menu, int itemId, int order, int titleRes, int icon) {
        MenuItem menuItem = menu.add(groupId, itemId, order, titleRes);
        if (icon > 0) {
            menuItem.setIcon(icon);
        }
        if (itemId != 10) {
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        } else {
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        }
        /**
         * 反射
         */
        if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
            try{
                Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                m.setAccessible(true);
                m.invoke(menu, true);
            } catch (Exception e) {
                //Log.e(getClass().getSimpleName(), "onMenuOpened Exception", e);
            }
        }
    }

    public static SwipeMenuItem createSwipeMenu(Context context, int backgroundRes, int titleRes, int textColor) {
        return new SwipeMenuItem(context)
                .setText(context.getString(titleRes)) // 文字。
                .setTextColor(textColor) // 文字颜色。
                .setTextSize(15) // 文字大小。
                .setWidth(60)
                .setWeight(1)
                .setHeight(-1)
                .setBackgroundDrawable(backgroundRes);
    }

    public static void fileSwipeMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, Context context) {
//        SwipeMenuItem deleteItem = createSwipeMenu(context, R.drawable.button_click_type_red, R.string.delete, Color.WHITE);
//        SwipeMenuItem detailItem = createSwipeMenu(context, R.drawable.button_click_type_bule_bule, R.string.detail, Color.WHITE);
//        SwipeMenuItem actionItem = createSwipeMenu(context, R.drawable.button_click_type_bule_bule, R.string.action, Color.WHITE);
//        SwipeMenuItem cutItem = createSwipeMenu(context, R.drawable.button_click_type_bule_bule, R.string.cut, Color.WHITE);
//
//        swipeRightMenu.addMenuItem(deleteItem);// 添加一个按钮到右侧侧菜单。.
//        swipeRightMenu.addMenuItem(detailItem);// 添加一个按钮到右侧侧菜单。.
//        swipeLeftMenu.addMenuItem(actionItem);// 添加一个按钮到左侧侧菜单。.
//        swipeLeftMenu.addMenuItem(cutItem);// 添加一个按钮到左侧侧菜单。.
    }


}
