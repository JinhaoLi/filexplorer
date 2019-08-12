package com.jil.filexplorer.Api;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Message;

import androidx.core.app.NotificationCompat;

import com.jil.filexplorer.Activity.AfterIntentService;
import com.jil.filexplorer.utils.LogUtils;
import com.jil.filexplorer.utils.NotificationUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.jil.filexplorer.utils.ConstantUtils.CHANNEL_ID;
import static com.jil.filexplorer.utils.ConstantUtils.MB;
import static com.jil.filexplorer.utils.FileUtils.closeAnyThing;
import static com.jil.filexplorer.utils.FileUtils.deleteDirectory;
import static com.jil.filexplorer.utils.FileUtils.deleteSingleFile;

/**
 * 文件操作类
 * 必须在非UI线程调用
 */
public class FileOperation {
    public final static int MODE_COPY   = -4;
    public final static int MODE_MOVE   = -5;
    public final static int MODE_DELETE = -6;
    public final static int MODE_RENAME = -7;
    private Context context;
    private static FileOperation fileOperation;
    /**
     * 任务模式
     */
    private int mode;
    /**
     * 任务队列
     */
    private ArrayList<FileInfo> inFiles;
    /**
     * 任务总大小
     */
    private long actionSize;
    /**
     * 已完成大小
     */
    private long overSize;
    /**
     * 运行状态
     */
    private static boolean running;
    /**
     * 完成项目数
     */
    private int overCount;
    /**
     * 进度信息
     */
    private ProgressMessage progressMessage;
    /**
     * 目标路径
     */
    private FileInfo toDir;
    /**
     * 任务总数
     */
    private int projectCount;

    private static AfterIntentService.UpdateUI updateUI;
    private static NotificationCompat.Builder builder;
    private static NotificationManager notificationManager;

    public static void setUpdateUI(AfterIntentService.UpdateUI updateui){
        updateUI=updateui;
    }

    private FileOperation(Context context) {
        this.context =context;
    }

    public void addProgressChangeListener(ProgressChangeListener progressChangeListener) {
        //this.progressChangeListener = progressChangeListener;
    }


    public static FileOperation with(Context context) {
        fileOperation = new FileOperation(context);
        builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        NotificationUtils.setNotification(context, builder, notificationManager,"复制任务进行中...");
        return fileOperation;
    }


    public FileOperation move(ArrayList<FileInfo> inFiles) {
        this.inFiles = inFiles;
        this.mode = MODE_MOVE;
        initialization();
        return this;
    }

    public FileOperation delete(ArrayList<FileInfo> inFiles) {
        this.inFiles = inFiles;
        this.mode = MODE_DELETE;
        initialization();
        return this;
    }

    public FileOperation copy(ArrayList<FileInfo> inFiles) {
        this.inFiles = inFiles;
        this.mode = MODE_COPY;
        initialization();
        return this;
    }

    public FileOperation to(FileInfo toDir) {
        if (!toDir.isDir()) {
            LogUtils.e("FileOperation", "目标不是文件夹");
            return null;
        } else {
            this.toDir = toDir;
            progressMessage = new ProgressMessage(System.currentTimeMillis(), actionSize
                    , projectCount, mode, toDir.getFilePath());
            return this;
        }
    }


    private void initialization() {
        overSize = 0;
        actionSize = 0;
        projectCount = 0;
        overCount = 0;
        if (inFiles.size() > 0) {
            long size = 0;
            for (FileInfo temp : inFiles) {
                size += getLength(new File(temp.getFilePath()));
            }
            actionSize = size;
        }
    }

    /**
     * 历遍目录获取总大小
     *
     * @param f
     */
    public long getLength(File f) {
        long size = 0;
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null)
                for (int z = 0; z < files.length; z++) {
                    File file = files[z];
                    size += getLength(file);
                }
        }
        if (f.isFile()) {
            projectCount++;
            return f.length();
        }
        return size;
    }


    private void filesCopy() {
        for (FileInfo temp : inFiles) {
            progressMessage.setIn(temp.getFilePath());
            File$DirCopy(new File(temp.getFilePath()), new File(toDir.getFilePath()));
        }
//        progressMessage.setCopyOverCount(projectCount);
//        progressChangeListener.progressChang(progressMessage);
    }

    private void File$DirCopy(File inFile, File toDir) {
        if (toDir.getPath().startsWith(inFile.getPath())) {
            System.err.println("目标文件夹是源文件的子目录");
            return;
        }
        if (!inFile.isDirectory()) {
            nioBufferCopy(inFile, new File(toDir, inFile.getName()));
        } else {
            copyDirWithFile(inFile, toDir);
        }

    }

    public void start() {
        running = true;
        //progressChangeListener.progressChang(progressMessage);
        switch (mode) {
            case MODE_COPY:
                filesCopy();
                break;
            case MODE_MOVE:
                filesMove();
                break;
            case MODE_DELETE:
                filesDelete();
                break;
        }
        misstionFinish();
    }

    private void misstionFinish() {
        Message finishMesssage = new Message();
        finishMesssage.what = -1;
        if (updateUI != null) updateUI.updateUI(finishMesssage);
        notificationManager.cancel(1410);
    }

    private void filesDelete() {
        progressMessage = new ProgressMessage(System.currentTimeMillis(), actionSize
                , projectCount, mode);
        for(FileInfo temp:inFiles){
            if(!running)
                break;
            String loaction =temp.getFilePath();
            progressMessage.setNowProjectName(temp.getFileName());
            if(temp.isDir()){
                deleteDirectory(loaction);
            }else {
                deleteSingleFile(loaction);
            }
        }
        progressMessage.setNowLoacation(actionSize);
        //progressChangeListener.progressChang(progressMessage);
    }

    private void filesMove() {
        for(FileInfo temp:inFiles){
            if(!running)
                break;
            if (toDir.getFileName().startsWith(temp.getFileName())) {
                System.err.println("目标文件夹是源文件的子目录");
                continue;
            }
            File f =new File(temp.getFilePath());
            if(moveFile(f)) {
                overCount++;
                progressMessage.setCopyOverCount(overCount);
            }
            refreshProgresss(f.length());
        }
        progressMessage.setNowLoacation(actionSize);
        //progressChangeListener.progressChang(progressMessage);

    }

    /**
     * 移动文件
     * @param file
     * @return
     */
    private boolean moveFile(File file){
        File result =new File(toDir.getFilePath(),file.getName());
        boolean b=file.renameTo(result);
        return b;
    }

    public static void stopAction() {
        running = false;
    }

    /**
     * 复制文件，可监测进度
     *
     * @param source G:\作业\安卓\ayun.apk
     * @param target G:\作业\安卓\安卓项目开发\解包打包G:\作业\安卓\安卓项目开发\解包打包\ayun.apk
     */
    private void nioBufferCopy(File source, File target) {
        FileChannel in = null;
        FileChannel out = null;
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        long size = source.length();
        try {
            inStream = new FileInputStream(source);
            outStream = new FileOutputStream(target);
            in = inStream.getChannel();
            out = outStream.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024 * 20);
            progressMessage.setNowProjectName(source.getName());
            while (in.read(buffer) != -1) {
                buffer.flip();
                out.write(buffer);
                buffer.clear();
                size -= 1024 * 1024 * 20;
                refreshProgresss(size > 0 ? 1024 * 1024 * 20 : (1024 * 1024 * 20) + size);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            overCount++;
            progressMessage.setCopyOverCount(overCount);
            closeAnyThing(inStream, in, outStream, out);
        }
    }

    /**
     *
     * @param add
     */
    private void sendProgress(long add) {
        overSize += add;
        progressMessage.setNowLoacation(overSize);
        //if (actionSize > 0 && progressChangeListener != null)
           // progressChangeListener.progressChang(progressMessage);

    }

    /**
     * 复制文件时发送进度
     */
    private void refreshProgresss(long size) {
        Message message = new Message();
        LogUtils.i("已复制：", progressMessage.getProgress() + "%--" + overSize / MB);
        progressMessage.setCopyOverCount(overCount);
        overSize += size;
        progressMessage.setNowLoacation(overSize);
        overSize=progressMessage.getNowLoacation();
        message.obj = progressMessage;
        message.what = progressMessage.getProgress();
        if (updateUI != null) updateUI.updateUI(message);
        builder.setProgress(100, progressMessage.getProgress(), false);
        notificationManager.notify(1410, builder.build());
    }

    /**
     * 复制文件夹及其路径下的所有文件
     *
     * @param from G:\作业\安卓
     * @param to   F:\Oracle VM VirtualBox\
     */
    private void copyDirWithFile(File from, File to) {
        File targt = new File(to.getPath(), from.getName()); //F:\Oracle VM VirtualBox\安卓
        if (!targt.exists()) targt.mkdir();
        File[] files = from.listFiles();
        if (files != null) {
            for (File temp : files) {
                if (!running)
                    break;
                if (temp.isFile()) {
                    nioBufferCopy(temp, new File(targt, temp.getName()));
                } else {
                    copyDirWithFile(temp, targt);
                }
            }
        }
    }

    /** 删除单个文件
     * @param filePath$Name 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    private boolean deleteSingleFile(String filePath$Name) {
        File file = new File(filePath$Name);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            overCount++;
            progressMessage.setCopyOverCount(overCount);
            refreshProgresss(file.length());
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

    /** 删除目录及目录下的文件
     * @param filePath 要删除的目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    private boolean deleteDirectory(String filePath) {
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



}
