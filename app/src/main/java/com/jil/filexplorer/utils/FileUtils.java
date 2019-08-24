package com.jil.filexplorer.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.PopupWindow;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.jil.filexplorer.Api.FileInfo;
import com.jil.filexplorer.BuildConfig;
import com.jil.filexplorer.R;
import com.jil.filexplorer.adapter.BoxAdapter;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.jil.filexplorer.utils.DialogUtils.showAndMake;
import static com.jil.filexplorer.utils.DialogUtils.showListPopupWindow;
import static com.jil.filexplorer.utils.UiUtils.getScreenHeight;

public class FileUtils {

    /**
     * 请求授权
     */
    public static void requestPermission(Activity activity) {
        if (ContextCompat.checkSelfPermission( activity ,
                Manifest.permission.READ_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED) { //表示未授权时
            //进行授权
            ActivityCompat.requestPermissions( activity , new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE} , 1 );
        }
    }

    public static boolean RootCommand(String command){
        Process process =null;
        DataOutputStream os =null;

        try {
            process =Runtime.getRuntime().exec("su");
            //process = Runtime.getRuntime().exec("sudo"); //切换到root帐号
            os =new DataOutputStream(process.getOutputStream());
            os.writeBytes(command+"\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            LogUtils.d("*** DEBUG ***", "ROOT REE" + e.getMessage());
            return false;
        }finally {
            if(os!=null){
                try {
                    os.close();
                    process.destroy();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            LogUtils.d("*** DEBUG ***", "Root SUC ");
            return true;
        }

    }

    /**
     * 刷新RequestOptions，解決Glide图片缓存导致不刷新问题
     *
     * @param options
     * @param file       图片地址
     * @param modified  图片修改时间
     */
    public static void updateOptions(RequestOptions options, File file, long modified){
        if(!file.getPath().equals("")&&modified!=0){
            try{
                String tail = file.getName().toLowerCase();
                String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(tail);
                if(type!=null&&!type.equals(""))
                options.signature(new MediaStoreSignature(type, modified, 0));
            }catch (Exception e){}
        }
    }

    public static RequestOptions getOptions(int imageCache,int width,int height){
        //设置图片圆角角度
        RoundedCorners roundedCorners= new RoundedCorners(10);
        RequestOptions options;
        if(imageCache>0){
            options=RequestOptions.bitmapTransform(roundedCorners)
                    //中心加载
                    .centerCrop()
                    //指定宽高
                    .override(width, height);
        }else {
            options=RequestOptions.bitmapTransform(roundedCorners)
                    //中心加载
                    .centerCrop()
                    .skipMemoryCache(true)//跳过缓存
                    .diskCacheStrategy(DiskCacheStrategy.NONE)//不缓存
                    //指定宽高
                    .override(width, height);
        }
        return options;


    }


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
            String mimeType =getMimeType(file.getPath()); //image/jpeg
            fileInfo.setFiletype(mimeType);
            size =file.length();
            icon =FileTypeFilter.getIconRes(mimeType,size);
            fileInfo.setFileSize(size);
            fileInfo.setIcon(icon);
        }
        return fileInfo;
    }

    public static FileInfo getFileInfoFromPath(String path){
        SoftReference<File> softReference =new SoftReference<File>(new File(path));
        return getFileInfoFromFile(softReference.get());
    }

    public static boolean reNameFile(File file,String name){
        File file1 =new File(file.getParent(),name);
        if(file.isFile()){
            return file.renameTo(file1);
        }
        return false;
    }

    public static Drawable getApkIcon(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath,
                PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            appInfo.sourceDir = apkPath;
            appInfo.publicSourceDir = apkPath;
            try {
                return appInfo.loadIcon(pm);
            } catch (OutOfMemoryError e) {
                LogUtils.e("获取apkIco失败", e.toString());
            }
        }
        return null;
    }

    public static String getFormatData(Date date) {
        SimpleDateFormat df = new SimpleDateFormat( "yyyy/MM/dd" );//设置日期格式
        return df.format( date );
    }
    public static String getFormatData(long date) {
        SimpleDateFormat df = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" );//设置日期格式
        return df.format( date );
    }

    public static void viewFile(final Context context, final String filePath,String type){
        if(filePath.endsWith(".apk")){
            installApk(filePath,context);
            return;
        }
        Intent intent;
//        if(type.startsWith("image/")){
//            intent = new Intent(context, ImageDisplayActivity.class);
//            intent.putExtra("image_path",filePath);
//            context.startActivity(intent);
//            return;
//        }
        intent=new Intent();
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
        LogUtils.i("URI" ,uri.getPath());
        intent.setDataAndType(uri, type);
//        /storage/emulated/0/Pictures/64476669_p0.jpg
//            /external_storage_root/Pictures/00000001.jpg
        //Intent.createChooser(intent, "请选择对应的软件打开该附件！");
        context.startActivity(intent);
    }

    /**
     * 获取选中的列表
     *
     * @return
     */
    public static ArrayList<FileInfo> getSelectedList(ArrayList<FileInfo> fileInfos) {
        ArrayList<FileInfo> selectList = new ArrayList<>();//储存删除对象
        for (int i = 0; i < fileInfos.size(); i++) {
            FileInfo fileInfo = fileInfos.get(i);
            if (fileInfo.isSelected()) {
                selectList.add(fileInfo);
            }
        }
        return selectList;
    }

    /**
     * 历遍目录获取总大小
     *
     * @param f
     */
    public static long getLength(File f) {
        long size = 0;
        if (f.isDirectory()&&f.exists()) {
            File[] files = f.listFiles();
            if (files != null)
                for (File file:files) {
                    size += getLength(file);
                }
        }
        if (f.isFile()&&f.exists()) {
            return f.length();
        }
        return size;
    }

    /**
     * bitmap.options类为bitmap的裁剪类，通过他可以实现bitmap的裁剪；
     *         如果不设置裁剪后的宽高和裁剪比例，返回的bitmap对象将为空，
     *         但是这个对象存储了原bitmap的宽高信息。
     * @param imagePath
     * @return i[0] =width i[1]=height
     */
    public static int[]  getPicWidthAndHeight(String imagePath){
        int[] i =new int[2];
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//这个参数设置为true才有效，
        Bitmap bmp = BitmapFactory.decodeFile(imagePath, options);//这里的bitmap是个空
        if(bmp == null){
            LogUtils.e(FileUtils.class.getName(),"通过options获取到的bitmap为空 ===");
        }
        i[0]= options.outWidth;
        i[1]= options.outHeight;
        LogUtils.i(FileUtils.class.getName(),"通过Options获取到的图片大小"+ "width:"+ i[0] +" height: " + i[1]);
        return i;
    }

    public static void shareFile(Context context, String filePath, String type){
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
            LogUtils.i("FileUtils:",BuildConfig.APPLICATION_ID+".fileprovider");
            uri=FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID+".fileprovider",new File(filePath));//authority要和AndroidManifest中的定义authorities的一致
        }else {
            uri=Uri.fromFile(new File(filePath));
        }
        intent.setType(type);
        intent.putExtra(Intent.EXTRA_STREAM,uri);
        context.startActivity(intent);
    }

    public static void viewFileWithPath(final Context context, final String filePath) {
        String type = getMimeType(filePath);
        if (!TextUtils.isEmpty(type) && !TextUtils.equals(type, "*/*")) {
            viewFile(context,filePath,type);
        } else {
            chooseViewFile(context,filePath);
        }
    }

    public static void viewFile(final Context context, FileInfo fileInfo,View view) {
        String type = fileInfo.getFiletype();
        if (!TextUtils.isEmpty(type) && !TextUtils.equals(type, "*/*")) {
            viewFile(context,fileInfo.getFilePath(),type);
        } else {
            //chooseViewFile(context,fileInfo.getFilePath());
            chooseViewFile(context,view,fileInfo.getFilePath());
        }
    }

    public static void chooseViewFile(final Context context, final String filePath){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setTitle("选择文件类型");
        String[] menuItemArray = {"文本","音频","视频","图片","其他"};
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
                            case 4:

                                break;
                        }
                        viewFile(context,filePath,selectType);
                    }
                });
        showAndMake(dialogBuilder.create());
    }

    public static void chooseViewFile(final Context context, View view , final String filePath){
        ListPopupWindow listPopupWindow =showListPopupWindow(context,view);
        String[] menuItemArray = {"文本","音频","视频","图片","其他"};
        BoxAdapter<String> adapter =new BoxAdapter<String>(context,R.layout.menu_simple_list_item,menuItemArray) {
            @Override
            public long getItemId(int position) {
                return position;
            }
            @Override
            public void setData(ViewHolder holder, String data) {
                holder.txt_content.setText(data);
            }
        };
        listPopupWindow.setAdapter(adapter);
        listPopupWindow.setWidth(300);
        final ListPopupWindow finalListPopupWindow = listPopupWindow;
        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectType = "*/*";
                switch (position) {
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
                    case 4:
                        break;
                }
                viewFile(context,filePath,selectType);
                finalListPopupWindow.dismiss();
            }
        });
        ListView l =listPopupWindow.getListView();
        listPopupWindow.show();
        if(l!=null){
            l.setOverScrollMode(ListView.OVER_SCROLL_NEVER);
        }
    }

    public static void chooseViewFile(final Context context, View view , final String filePath, final ListPopupWindow upPopupWindow){
        ListPopupWindow listPopupWindow =showListPopupWindow(context,view);

        listPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                upPopupWindow.dismiss();
            }
        });
        String[] menuItemArray = {"文本","音频","视频","图片","其他"};
        BoxAdapter<String> adapter =new BoxAdapter<String>(context,R.layout.menu_simple_list_item,menuItemArray) {
            @Override
            public long getItemId(int position) {
                return position;
            }
            @Override
            public void setData(ViewHolder holder, String data) {
                holder.txt_content.setText(data);
            }
        };
        listPopupWindow.setAdapter(adapter);
        listPopupWindow.setWidth(300);
        final ListPopupWindow finalListPopupWindow = listPopupWindow;
        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectType = "*/*";
                switch (position) {
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
                    case 4:

                        break;
                }
                viewFile(context,filePath,selectType);
                finalListPopupWindow.dismiss();
            }
        });
        int[] location = new  int[2] ;
        view.getLocationOnScreen(location);
        listPopupWindow.getVerticalOffset();
        if(location[0]<540){
            listPopupWindow.setHorizontalOffset(listPopupWindow.getWidth()+3);
        }else {
            listPopupWindow.setHorizontalOffset(-(listPopupWindow.getWidth()+3));
        }
        ListView l =listPopupWindow.getListView();
        if(location[1]>=getScreenHeight(context)-(menuItemArray.length*115)){
            listPopupWindow.setVerticalOffset(115);
        }else {
            listPopupWindow.setVerticalOffset(-115);
        }
        try{
            listPopupWindow.show();
            if(l!=null){
                l.setOverScrollMode(ListView.OVER_SCROLL_NEVER);
            }
        }catch (Exception e){
            listPopupWindow=null;
            upPopupWindow.dismiss();
            chooseViewFile(context,filePath);
            LogUtils.i(e.getMessage(),"无法显示窗口");
        }

    }


    private static void grantUriPermission(Context context, Uri fileUri, Intent intent) {
        List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, fileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }

    public static String hideMax(String string,int i){
        if(string.length()>i){
            return "..."+string.substring(string.length()-(i-5));
        }else {
           return string;
        }
    }

    public static String getMimeType(String filePath) {
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
    public static boolean deleteSingleFile(String filePath$Name) {
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

    /**
     * 删除文件
     * @param fileInfo
     * @return
     */
    public static boolean deleteAFile(FileInfo fileInfo){

        String loaction =fileInfo.getFilePath();
        if(fileInfo.isDir()){
            return deleteDirectory(loaction);
        }else {
            return deleteSingleFile(loaction);
        }

    }

    /**
     * 复制文件夹及其路径下的所有文件
     * @param from
     * @param to
     * @throws IOException
     */
    public static void copyDirWithFile(File from,File to) {
        File targt =new File(to.getPath(),from.getName());
        if(!targt.exists()) targt.mkdir();
        File[] files =from.listFiles();
        if(files!=null){
            for(File temp:files){
                if(temp.isFile()){
                    nioBufferCopy(temp,new File(targt,temp.getName()));
                }else {
                    copyDirWithFile(temp,targt);
                }
            }
        }
    }

    public static boolean reNameFile(FileInfo inFile,String fileName){
        File in= new File(inFile.getFilePath());
        File out= new File(in.getParent(),fileName);
        if(!out.exists()){
            return in.renameTo(out);
        }else {
            return false;
        }
    }


    /**
     * 复制文件，效率最高
     * @param source
     * @param target
     */
    private static void nioTransferCopy(File source, File target) {
        FileChannel in = null;
        FileChannel out = null;
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        try {
            inStream = new FileInputStream(source);
            outStream = new FileOutputStream(target);
            in = inStream.getChannel();
            out = outStream.getChannel();
            in.transferTo(0, in.size(), out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeAnyThing(inStream,in,outStream,out);
        }
    }

    /**
     * 复制文件，可监测进度
     * @param source
     * @param target
     */
    private static void nioBufferCopy(File source, File target) {
        FileChannel in = null;
        FileChannel out = null;
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        try {
            inStream = new FileInputStream(source);
            outStream = new FileOutputStream(target);
            in = inStream.getChannel();
            out = outStream.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(4096);
            while (in.read(buffer) != -1) {
                buffer.flip();
                out.write(buffer);
                buffer.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeAnyThing(inStream,in,outStream,out);
        }
    }

    public static void closeAnyThing(Closeable... closeable){
        for(Closeable temp:closeable) {
            try {
                if(temp!=null) {
                    temp.close();
                }
            }catch(Exception e) {
                LogUtils.i("资源关闭失败","在FileUtils.closeAnyThing()");
            }
        }
    }

    /** 删除目录及目录下的文件
     * @param filePath 要删除的目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String filePath) {
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
