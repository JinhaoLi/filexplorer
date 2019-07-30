package com.jil.filexplorer.utils;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.jil.filexplorer.Activity.ProgerssDialogActivity;
import com.jil.filexplorer.Api.ProgressMessage;
import com.jil.filexplorer.R;
import com.jil.filexplorer.ui.CopyProgressDialog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.jil.filexplorer.utils.ConstantUtils.CHANNEL_ID;

public class CopyFileUtils {
    private static long dirSize = 0;// 文件夹总体积
    private static long hasReadSize = 0;// 已复制的部分，体积
    private static CopyProgressDialog progressDialog;// 进度提示框
    private static Thread copyFileThread;
    private static Runnable run = null;
    private static FileInputStream fileInputStream = null;
    private static FileOutputStream fileOutputStream = null;
    private static FileChannel fileChannelOutput = null;
    private static FileChannel fileChannelInput = null;
    private static Thread copyDirThread;
    private static BufferedInputStream inbuff = null;
    private static BufferedOutputStream outbuff = null;
    private static ProgressMessage progressMessage;
    private static boolean showNotifiticaon;
    private static CopyOverListener copyOverListener;
    private static NotificationManager notificationManager;
    private static NotificationCompat.Builder builder;
    private static Notification notification;

    public static void setCopyOverListener(CopyOverListener listener){
        copyOverListener=listener;
    }

    private static void createNotification(Context context, String title, int progress,boolean first){
        if(first){
            Intent intent = new Intent(context, ProgerssDialogActivity.class);// ========= 重点2============
            PendingIntent pendingIntent = null;
            //if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                pendingIntent = PendingIntent.getActivity(context, 0, intent,PendingIntent.FLAG_IMMUTABLE);
           // }
            notification = builder
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .setContentTitle(title)//标题
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.copy_move_progress_ico))
                    .setLocalOnly(true)
                    .setContentIntent(pendingIntent)
                    .setPriority(Notification.DEFAULT_ALL)
                    .build();
        }else {
            builder.setProgress(100,progress,false).setContentTitle(title);
            notification=builder.build();
            notificationManager.notify(1810,notification);
        }

    }

    public interface CopyOverListener{
        void updateUi();
    }


    /**
     * handler用于在主线程刷新ui
     */
    @SuppressLint("HandlerLeak")
    private final static Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 0&&progressDialog != null) {
                int progress = msg.getData().getInt("progress");
                long fileVolume = msg.getData().getLong("fileVolume");
                progressDialog.setProgress(progress);
                progressDialog.setTitle("已完成"+progress + "%");
                if(progress==100)
                    copyOverListener.updateUi();
                progressDialog.setFileVolumeText(fileVolume * progress / 100 + " MB/" + fileVolume + " MB");
            }else if(msg.what==1){
                progressDialog.setNameText(msg.getData().getString("file_name"));
//                if(progressDialog != null) {
//                    int fileVolume = msg.getData().getInt("fileVolume");
//                    progressDialog.setFileVolumeText(0 + " MB/" + fileVolume + " MB");
//                }
            }
        }
    };

    public static void stopCopyFile(){
        if(progressDialog.isShow()){
            progressDialog.dismiss();
        }
        run = null;
        copyFileThread.interrupt();
        copyFileThread = null;
        try {
            fileInputStream.close();
            fileOutputStream.close();
            fileChannelOutput.close();
            fileChannelInput.close();
        } catch (IOException e) {
            Log.e("CopyPasteUtil", "CopyPasteUtil copyFile error:" + e.getMessage());
        }
    }

    public static void stopCopyDir(){
        run = null;
        copyDirThread.interrupt();
        copyDirThread = null;
        try {
            if(fileInputStream != null) fileInputStream.close();
            if(fileOutputStream != null) fileOutputStream.close();
            if(inbuff != null) inbuff.close();
            if(outbuff != null) outbuff.close();
            if(fileChannelOutput != null) fileChannelOutput.close();
            if(fileChannelInput != null) fileChannelInput.close();
        } catch (IOException e) {
            Log.e("CopyPasteUtil", "CopyPasteUtil copyDirectiory error:" + e.getMessage());
        }
    }

    public static CopyProgressDialog getCopyWindow(){
        return progressDialog;
    }

    /**
     * 复制单个文件
     * @param oldPathName  G:\个人\All Users\Registry.db
     * @param newPathName  F:\Oracle VM VirtualBox\doc\Registry.db
     * @param context
     * @return
     */
    public static boolean copyFile(final String oldPathName, final String newPathName, final Context context) {
        builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        createNotification(context,"复制任务进行中...",0,true);
        progressDialog = new CopyProgressDialog(context);
        progressDialog.setCopyDir(false);
        progressDialog.show();
        progressDialog.setNameText(oldPathName);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {//点击返回取消时，关闭线程和流
            @Override
            public void onCancel(DialogInterface arg0) {
                showNotifiticaon =true;
            }
        });
        run = new Runnable() {
            @Override
            public void run() {
                try {
                    File fromFile = new File(oldPathName);
                    File targetFile = new File(newPathName);
                    fileInputStream = new FileInputStream(fromFile);
                    fileOutputStream = new FileOutputStream(targetFile);
                    fileChannelOutput = fileOutputStream.getChannel();
                    fileChannelInput = fileInputStream.getChannel();
                    ByteBuffer buffer = ByteBuffer.allocate(4096);
                    long transferSize = 0;
                    long size = new File(oldPathName).length();
                    int fileVolume = (int) (size / 1024 /1024);
                    int tempP = 0;
                    int progress = 0;
                    while (fileChannelInput.read(buffer) != -1) {
                        buffer.flip();
                        transferSize += fileChannelOutput.write(buffer);
                        progress = (int) (transferSize * 100 / size);
                        if(progress>tempP){
                            tempP = progress;
                            Message message = handler.obtainMessage(0);
                            Bundle b = new Bundle();
                            b.putInt("progress", progress);
                            b.putLong("fileVolume", fileVolume);
                            message.setData(b);
                            handler.sendMessage(message);
                            if(progressDialog.isShow() && progress==100){
                                progressDialog.dismiss();
                            }
                            if(showNotifiticaon &&progress<100){
                                createNotification(context,"复制任务-"+progress+"%",progress,false);
                            }else if(progress==100){
                                builder.setContentIntent(null);
                                createNotification(context,"复制任务已完成",progress,false);
                                showNotifiticaon=false;
                            }

                        }
                        buffer.clear();
                    }
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    fileInputStream.close();
                    fileChannelOutput.close();
                    fileChannelInput.close();
                    if(progressDialog.isShow()){
                        progressDialog.dismiss();
                    }
                } catch (Exception e) {
                    Log.e("CopyPasteUtil", "CopyPasteUtil copyFile error:" + e.getMessage());
                }
            }
        };
        copyFileThread = new Thread(run);
        copyFileThread.start();
        return true;
    }

    /**
     * 复制文件夹
     * @param sourceDir G:\壁纸\写真
     * @param targetDir E:\Nox\Nox\写真
     * @param context
     */
    public static void copyDirectiory(final String sourceDir, final String targetDir, final Context context) {
        if (context != null) {
            initValueAndGetDirSize(new File(sourceDir));
            builder = new NotificationCompat.Builder(context, CHANNEL_ID);
            notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            createNotification(context,"复制任务进行中...",0,true);
            progressDialog = new CopyProgressDialog(context);
            progressDialog.show();
            progressDialog.setCopyDir(true);
            progressDialog.setNameText(sourceDir);
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {//点击返回取消时，关闭线程和流
                @Override
                public void onCancel(DialogInterface arg0) {
                    showNotifiticaon =true;
                }
            });
        }
        run = new Runnable() {
            @Override
            public void run() {
                (new File(targetDir)).mkdirs();
                File[] file = (new File(sourceDir)).listFiles();// 获取源文件夹当下的文件或目录
                for (int i = 0; i < file.length; i++) {
                    if (file[i].isFile()) {
                        File sourceFile = file[i];
                        File targetFile = new File(new File(targetDir).getAbsolutePath() + File.separator + file[i].getName());// 目标文件
                        Message message =handler.obtainMessage(1);
                        Bundle b = new Bundle();
                        b.putString("file_name",sourceFile.getName());
                        message.setData(b);
                        handler.sendMessage(message);

                        copyFile(sourceFile, targetFile,context);
                    }
                    if (file[i].isDirectory()) {
                        String dir1 = sourceDir + "/" + file[i].getName();
                        String dir2 = targetDir + "/" + file[i].getName();
                        copyDirectiory(dir1, dir2, null);
                    }
                }

            }
        };
        copyDirThread = new Thread(run);
        copyDirThread.start();
    }

/**
     * 复制单个文件，用于上面的复制文件夹方法
     *
     * @param sourcefile
     *            源文件路径
     * @param targetFile
     *            目标路径
     */
    public static synchronized void copyFile(final File sourcefile, final File targetFile,Context context) {
        try {
            fileInputStream = new FileInputStream(sourcefile);
            inbuff = new BufferedInputStream(fileInputStream);
            fileOutputStream = new FileOutputStream(targetFile);// 新建文件输出流并对它进行缓冲
            outbuff = new BufferedOutputStream(fileOutputStream);
            int fileVolume = (int) (dirSize / (1024 * 1024));
            fileChannelOutput = fileOutputStream.getChannel();
            fileChannelInput = fileInputStream.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(4096);
            long transferSize = 0;
            int tempP = 0;
            int progress = 0;
            while (fileChannelInput.read(buffer) != -1) {
                buffer.flip();
                transferSize += fileChannelOutput.write(buffer);
                if(dirSize!=0)
                progress = (int) (((transferSize + hasReadSize) * 100) / dirSize);
                if(progress>tempP){
                    tempP = progress;
                    Message message = handler.obtainMessage(0);
                    Bundle b = new Bundle();
                    b.putInt("progress", progress);
                    b.putLong("fileVolume", fileVolume);
                    message.setData(b);
                    handler.sendMessage(message);
                    if(progressDialog.isShow() && progress==100){
                        progressDialog.dismiss();
                    }
                    if(showNotifiticaon &&progress<100){
                        createNotification(context,"复制任务-"+progress+"%",progress,false);
                    }else if(progress==100){
                        builder.setContentIntent(null);
                        createNotification(context,"复制任务已完成",progress,false);
                        showNotifiticaon=false;
                    }
                }
                buffer.clear();
            }
            hasReadSize += sourcefile.length();
            outbuff.flush();
            inbuff.close();
            outbuff.close();
            fileOutputStream.close();
            fileInputStream.close();
            fileChannelOutput.close();
            fileChannelInput.close();
        } catch (FileNotFoundException e) {
            Log.e("CopyPasteUtil", "CopyPasteUtil copyFile error:" + e.getMessage());
        } catch (IOException e) {
            Log.e("CopyPasteUtil", "CopyPasteUtil copyFile error:" + e.getMessage());
        }
    }

    /**
     * 获取文件夹大小
     * @param file
     */
    public static void getDirSize(File file) {
        if (file.isFile()) {
            // 如果是文件，获取文件大小累加
            dirSize += file.length();
        } else if (file.isDirectory()) {
            File[] f1 = file.listFiles();
            for (int i = 0; i < f1.length; i++) {
                // 调用递归遍历f1数组中的每一个对象
                getDirSize(f1[i]);
            }
        }
    }

    /**
     * 初始化全局变量
     */
    public static void initDirSize() {
        dirSize = 0;
        hasReadSize = 0;
    }

    /**
     * 复制文件夹前，初始化两个变量
     */
    public static void initValueAndGetDirSize(File file) {
        initDirSize();
        getDirSize(file);
    }

}
