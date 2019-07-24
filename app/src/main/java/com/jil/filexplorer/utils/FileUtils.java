package com.jil.filexplorer.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.MotionEvent;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.jil.filexplorer.BuildConfig;
import com.jil.filexplorer.FileInfo;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class FileUtils {

    public static FileInfo getFileInfoFromFile(File file){
        boolean isDir =file.isDirectory();
        String name =file.getName();

        FileInfo fileInfo=new FileInfo(name,file.getPath(),
                isDir,file.lastModified(),
                file.canRead(),file.canWrite(),
                file.isHidden());

        long size;
        int icon;
        if (isDir) {
            int lCount;
            File[] files = file.listFiles();
            // files==null意味着无法访问此目录
            if (files == null) {
                //return null;
            }else {
                lCount=files.length;
                fileInfo.setCount(lCount);
            }
        } else {
            fileInfo.setFiletype(getFileType(file.getName()));
            size =file.length();
            icon =FileTypeFilter.getIconRes(fileInfo.getFiletype(),size);
            fileInfo.setFileSize(size);
            fileInfo.setIcon(icon);
        }
        return fileInfo;
    }

    public static FileInfo getFileInfoFromPath(String path){
        File file =new File(path);
        return getFileInfoFromFile(file);
    }

    public static boolean reNameFile(File file,String name){
        File file1 =new File(file.getParent(),name);
        if(file.isFile()){
            return file.renameTo(file1);
        }
        return false;
    }

    public static String getFormatData(Date date) {
        SimpleDateFormat df = new SimpleDateFormat( "yyyy/MM/dd" );//设置日期格式
        return df.format( date );
    }
    public static String getFormatData(long date) {
        SimpleDateFormat df = new SimpleDateFormat( "yyyy/MM/dd" );//设置日期格式
        return df.format( date );
    }

    public static void viewFile(final Context context, final String filePath,String type){
        if(filePath.endsWith(".apk")){
            installApk(filePath,context);
            return;
        }
        Intent intent = new Intent();
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        Uri uri;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
            LogUtils.i("FileUtils:",BuildConfig.APPLICATION_ID+".fileprovider");
            uri=FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID+".fileprovider",new File(filePath));//authority要和AndroidManifest中的定义authorities的一致
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            //grantUriPermission(context, uri, intent);
        }else {
            uri=Uri.fromFile(new File(filePath));
        }
        intent.setDataAndType(uri, type);
        //Intent.createChooser(intent, "请选择对应的软件打开该附件！");
        context.startActivity(intent);
    }

    public static void viewFile(final Context context, final String filePath) {
        String type = getMimeType(filePath);
        if (!TextUtils.isEmpty(type) && !TextUtils.equals(type, "*/*")) {
            viewFile(context,filePath,type);
        } else {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
            dialogBuilder.setTitle("选择文件类型");
            String[] menuItemArray = {"文本","音频","视频","图片"};
            dialogBuilder.setItems(menuItemArray,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String selectType = "*/*";
                            switch (which) {
                                case 0:
                                    selectType = "text/plain";
                                    break;
                                case 1:
                                    selectType = "audio/*";
                                    break;
                                case 2:
                                    selectType = "video/*";
                                    break;
                                case 3:
                                    selectType = "image/*";
                                    break;
                            }
                            viewFile(context,filePath,selectType);
                        }
                    });
            dialogBuilder.show();
        }
    }

    private static void grantUriPermission(Context context, Uri fileUri, Intent intent) {
        List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, fileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }

    private static String getMimeType(String filePath) {
        int dotPosition = filePath.lastIndexOf('.');
        if (dotPosition == -1)
            return "*/*";

        String ext = filePath.substring(dotPosition + 1)
                .toLowerCase();
        String mimeType = MimeUtils.guessMimeTypeFromExtension(ext);
        if (ext.equals("mtz")) {
            mimeType = "application/miui-mtz";
        }

        return mimeType != null ? mimeType : "*/*";
    }

    private static String getFileType(String filePath) {
        int dotPosition = filePath.lastIndexOf('.');
        if (dotPosition == -1)
            return "未知";

        String ext = filePath.substring(dotPosition + 1)
                .toLowerCase();
        return ext;
    }

    private static void  installApk(String filePath,Context context) {
        File apkFile = new File(filePath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //版本高于6.0，权限不一样
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID+".fileprovider", apkFile);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            //兼容8.0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                boolean hasInstallPermission = context.getPackageManager().canRequestPackageInstalls();
                if (!hasInstallPermission) {
                    startInstallPermissionSettingActivity(context);
                }
            }
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        context.startActivity(intent);
    }

    /**
     * 跳转到设置-允许安装未知来源-页面
     * @param context
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void startInstallPermissionSettingActivity(Context context) {
        //注意这个是8.0新API
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 保留小数点后2位
     * @param priceCar
     * @return
     */
    public static float stayFrieNumber(float priceCar){
        // 设置位数
        int scale = getScale(priceCar);
        // 表示四舍五入，可以选择其他舍值方式，例如去尾，等等.
        int roundingMode = 4;
        BigDecimal bd = new BigDecimal((float) priceCar);
        bd = bd.setScale(scale, roundingMode);
        priceCar = bd.floatValue();
        return priceCar;
    }

    /** 删除单个文件
     * @param filePath$Name 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    private static boolean deleteSingleFile(String filePath$Name) {
        File file = new File(filePath$Name);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                LogUtils.i("--Method--", "Copy_Delete.deleteSingleFile: 删除单个文件" + filePath$Name + "成功！");
                return true;
            } else {
                LogUtils.i("删除进程：", "删除单个文件" + filePath$Name + "失败！");
                return false;
            }
        } else {
            LogUtils.i("删除进程：", "删除单个文件失败：" + filePath$Name + "不存在！");
            return false;
        }
    }


    public static boolean deleteAFile(FileInfo fileInfo){
        String loaction =fileInfo.getFilePath();
        if(fileInfo.isDir()){
            return deleteDirectory(loaction);
        }else {
            return deleteSingleFile(loaction);
        }
    }

    /** 删除目录及目录下的文件
     * @param filePath 要删除的目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    private static boolean deleteDirectory(String filePath) {
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator))
            filePath = filePath + File.separator;
        File dirFile = new File(filePath);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            LogUtils.i("删除进程：", "删除目录失败：" + filePath + "不存在！");
            return false;
        }
        boolean flag = true;
        // 删除文件夹中的所有文件包括子目录
        File[] files = dirFile.listFiles();
        for (File file : files) {
            // 删除子文件
            if (file.isFile()) {
                flag = deleteSingleFile(file.getAbsolutePath());
                if (!flag)
                    break;
            }
            // 删除子目录
            else if (file.isDirectory()) {
                flag = deleteDirectory(file
                        .getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag) {
            LogUtils.i("删除进程：", "删除目录失败！");
            return false;
        }
        // 删除当前目录
        if (dirFile.delete()) {
            LogUtils.i("--Method--", "Copy_Delete.deleteDirectory: 删除目录" + filePath + "成功！");
            return true;
        } else {
            LogUtils.i("删除进程：", "删除目录：" + filePath + "失败！");
            return false;
        }
    }

    public static int getScale(float f){
        if(f<10){
            return 3;
        }else if(f<100){
            return 2;
        } else if(f<1000){
            return 1;
        } else {
            return 0;
        }
    }

    // 计算两个触摸点之间的距离
    public static float getDistance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }

    // 计算两个触摸点的中点
    private static PointF getMiddle(MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        return new PointF(x / 2, y / 2);
    }


}
