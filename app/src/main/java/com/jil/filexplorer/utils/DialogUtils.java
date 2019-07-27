package com.jil.filexplorer.utils;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListPopupWindow;

import com.jil.filexplorer.Api.FileInfo;

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
        dialogBuilder.show();
    }

    public static void showListPopulWindow(Context context,View view) {
        final String[] list = {"1"};//要填充的数据
        final ListPopupWindow listPopupWindow;
        listPopupWindow = new ListPopupWindow(context);
        listPopupWindow.setAdapter(new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1, list));//用android内置布局，或设计自己的样式
        listPopupWindow.setAnchorView(view);//以哪个控件为基准，在该处以mEditText为基准
        listPopupWindow.setModal(true);

        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {//设置项点击监听
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //pageFragment.load(historyPath.get(i),true);
                listPopupWindow.dismiss();//如果已经选择了，隐藏起来
            }
        });
        listPopupWindow.show();//把ListPopWindow展示出来
    }
}
