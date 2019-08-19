package com.jil.filexplorer.utils;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import com.jil.filexplorer.Api.FileInfo;
import com.jil.filexplorer.Api.Item;
import com.jil.filexplorer.R;
import com.jil.filexplorer.adapter.ListPopupWindowAdapter;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static com.jil.filexplorer.utils.ConstantUtils.SELECTED_COLOR;

public class DialogUtils {

    /**
     * 获取density
     * @param context
     * @return
     */
    public static float getDensity(Context context){
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Service.WINDOW_SERVICE);
        if (manager != null) {
            manager.getDefaultDisplay().getMetrics(metrics);
        }
        return metrics.density;
    }

    public static void showAlerDialog(Context context, FileInfo fileInfo){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setTitle("文件详细信息");
        String string = ("文件名：" + fileInfo.getFileName() + "\n"
                + "路径：" + fileInfo.getFilePath() + "\n"
                + "最后修改日期：" + FileUtils.getFormatData(fileInfo.getModifiedDate()) + "\n"
                + "文件大小：" + fileInfo.getFileSize() / 1024 + "kb" + "\n"
                + "是否可读：" + fileInfo.isCanRead() + "\n"
                + "是否可写：" + fileInfo.isCanWrite());
        dialogBuilder.setMessage(string);
        dialogBuilder.setInverseBackgroundForced(true);
        AlertDialog dialog = dialogBuilder.create();
        showAndMake(dialog);
    }

    /**
     * 反射机制更改颜色
     * @param dialog
     */
    public static void showAndMake(AlertDialog dialog){
        dialog.show();
        try {
            Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
            mAlert.setAccessible(true);
            Object alertController = mAlert.get(dialog);

            Field mTitleView = alertController.getClass().getDeclaredField("mTitleView");
            mTitleView.setAccessible(true);

            TextView title = (TextView) mTitleView.get(alertController);
            title.setTextColor(Color.argb(225,21,143,197));

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    public static ListPopupWindow showListPopupWindow(Context context, View view,int itemRes,Item[] list) {
        ListPopupWindow listPopupWindow;
        listPopupWindow = new ListPopupWindow(context);
        ListPopupWindowAdapter adapter =new ListPopupWindowAdapter(context, itemRes, list);
        listPopupWindow.setAdapter(adapter);//用android内置布局，或设计自己的样式
        listPopupWindow.setAnchorView(view);//以哪个控件为基准，在该处以mEditText为基准
        listPopupWindow.setModal(true);
        return listPopupWindow;
    }


    public static AlertDialog.Builder showAlertDialog(Context context,String title){
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setIcon( R.mipmap.ic_launcher ).setTitle( title )
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        return builder;
    }
}
