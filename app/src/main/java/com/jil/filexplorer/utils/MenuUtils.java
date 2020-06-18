package com.jil.filexplorer.utils;

import android.content.Context;
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import androidx.appcompat.view.menu.MenuBuilder;

import com.jil.filexplorer.R;

import java.lang.reflect.Method;

public class MenuUtils {

    public static boolean addMenu(Menu menu) {
        //单选
        addMenu(4,menu, 2, 1, R.string.paste, R.drawable.ic_content_paste_black_24dp);
        addMenu(3,menu, 9, 2, R.string.interval, R.drawable.ic_all_selecte_in_qu);
        addMenu(3,menu, 8, 3, R.string.all_choose, R.drawable.ic_all_selecte_ico);
        addMenu(2,menu, 4, 4, R.string.add_new, R.drawable.ic_add);
        addMenu(2,menu, 6, 5, R.string.refresh, R.drawable.ic_refresh);
        addMenu(2,menu, 7, 6, R.string.close, R.drawable.ic_close_black_24dp);
        addMenu(1,menu, 10, 7, R.string.compress,R.drawable.ic_pages_black_24dp);

        return true;
    }

    private static void addMenu(int groupId,Menu menu, int itemId, int order, int titleRes, int icon) {
        MenuItem menuItem = menu.add(groupId, itemId, order, titleRes);
        if (icon > 0) {
            menuItem.setIcon(icon);
        }
        if (itemId == 8 || itemId == 9) {
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        } else {
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        /**
         * 反射显示图标
         */
//        if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
//            try{
//                Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
//                m.setAccessible(true);
//                m.invoke(menu, true);
//            } catch (Exception e) {
//                //Log.e(getClass().getSimpleName(), "onMenuOpened Exception", e);
//            }
//        }
    }

}
