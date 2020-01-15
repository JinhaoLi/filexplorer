package com.jil.filexplorer.utils;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.View;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import com.jil.filexplorer.activity.SettingActivity;
import com.jil.filexplorer.api.FileInfo;
import com.jil.filexplorer.api.Item;
import com.jil.filexplorer.R;
import com.jil.filexplorer.adapter.ListPopupWindowAdapter;
import com.jil.filexplorer.ui.SimpleDialog;

import java.lang.reflect.Field;

import static com.jil.filexplorer.utils.FileUtils.getFileInfoFromPath;
import static com.jil.filexplorer.utils.FileUtils.getPicWidthAndHeight;
import static com.jil.filexplorer.utils.FileUtils.hideMax;

public class DialogUtils {


    public static void showFileInfoMsg(Context context, final FileInfo fileInfo){

//        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
//        dialogBuilder.setTitle("文件详细信息");
        int icoRes;
        String path =hideMax(fileInfo.getFilePath(),35);
        final StringBuilder msg =new StringBuilder();
        msg.     append("名称：").append(fileInfo.getFileName()).append("\n")
                .append("路径：").append(path).append("\n")
                .append("最后修改日期：").append(FileUtils.getFormatData(fileInfo.getModifiedDate())).append("\n")
                .append("大小：").append(fileInfo.getFileSize() / 1024).append("kb").append("\n")
                .append("可读：").append(fileInfo.isCanRead()).append("\n")
                .append("可写：").append(fileInfo.isCanWrite());

        if(fileInfo.isDir()){
            icoRes=R.mipmap.list_ico_dir;
        }else {
            if(fileInfo.getFiletype().startsWith("image")){
                int[] wh =getPicWidthAndHeight(fileInfo.getFilePath());
                msg.append("\n宽度：").append(wh[0]).append("\n")
                        .append("高度：").append(wh[1]);
            }
            icoRes=fileInfo.getIcon();
        }
//        dialogBuilder.setMessage(msg.toString());
//        dialogBuilder.setInverseBackgroundForced(true);
//        AlertDialog dialog = dialogBuilder.create();
//        showAndMake(dialog);

        SimpleDialog updateDialog =new SimpleDialog(context,R.layout.dialog_detail_info_layout,fileInfo.getFileName()) {
            TextView info;
            @Override
            public void queryButtonClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(null, fileInfo.getFilePath());
                assert clipboard != null;
                clipboard.setPrimaryClip(clip);
                ToastUtils.showToast(getContext(), "密码已复制到粘贴板", 1000);
                dismiss();
            }

            @Override
            public void customView() {
                super.customView();
                info=findViewById(R.id.textView13);
                info.setText(msg.toString());
                query.setText("复制路径");
            }
        };

        updateDialog.showAndSet(icoRes);
    }

    public static void showFileInfoMsg(Context context,String path){
        showFileInfoMsg(context,getFileInfoFromPath(path));
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
        listPopupWindow = showListPopupWindow(context,view);
        ListPopupWindowAdapter adapter =new ListPopupWindowAdapter(context, itemRes, list);
        listPopupWindow.setAdapter(adapter);//用android内置布局，或设计自己的样式
        return listPopupWindow;
    }

    public static ListPopupWindow showListPopupWindow(Context context, View view) {
        ListPopupWindow listPopupWindow;
        listPopupWindow = new ListPopupWindow(context);
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
