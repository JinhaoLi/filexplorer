package com.jil.filexplorer.Api;

import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.jil.filexplorer.utils.LogUtils;
import com.jil.filexplorer.utils.NotificationUtils;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.jil.filexplorer.utils.ConstantUtils.CHANNEL_ID;
import static com.jil.filexplorer.utils.ConstantUtils.MB;
import static com.jil.filexplorer.utils.FileUtils.closeAnyThing;
import static com.jil.filexplorer.utils.FileUtils.getFileInfoFromFile;
import static com.jil.filexplorer.utils.FileUtils.getFileInfoFromPath;

/**
 * 文件操作类
 * 必须在非UI线程调用
 * 结合ProgressMessage，ProgressChangeListener
 */
public class FileOperation implements Runnable{

    public final static int MODE_COPY = -4;
    public final static int MODE_MOVE = -5;
    public final static int MODE_DELETE = -6;
    public final static int MODE_RENAME = -7;
    public final static int MODE_COMPRESS =-8;
    public final static int MODE_DOWNLOAD =-9;
    private Context context;
    private static FileOperation fileOperation;
    /**
     * 任务模式
     */
    private int mode;
    /**
     * 任务队列
     * 下载模式MODE_DOWNLOAD下，此为下载文件名数组
     */
    private ArrayList<FileInfo> inFiles;
    /**
     * 模式MODE_DOWNLOAD输入
     */
    private ArrayList<InputStream> inputStreams;
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
    /**
     * 任务监听器
     */
    private ProgressChangeListener progressChangeListener;

    /**
     * 是否准备完成
     */
    private boolean isReady;
    /***
     * 压缩文件参数
     */
    private ZipParameters zipParameters;

    private static String notifitionMsg;

    private static NotificationCompat.Builder builder;
    private static NotificationManager notificationManager;

    public void setProgressChangeListener(ProgressChangeListener progressChangeListener) {
        this.progressChangeListener = progressChangeListener;
    }

    public ProgressMessage getProgressMessage() {
        if (progressMessage == null) {
            //progressMessage = new ProgressMessage(System.currentTimeMillis(), actionSize, projectCount, mode);
        }
        return progressMessage;
    }

    private FileOperation(Context context) {
        this.context = context;
    }

    public static FileOperation with(Context context) {
        fileOperation = new FileOperation(context);
        builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notifitionMsg = "任务准备中...";
        return fileOperation;
    }

    public static FileOperation getInstance() {
        if (fileOperation != null) {
            return fileOperation;
        } else {
            return null;
        }


    }

    public FileOperation compress(ArrayList<FileInfo> inFiles){
        this.inFiles =inFiles;
        this.mode =MODE_COMPRESS;
        initialization();
        return this;
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

    public FileOperation download(ArrayList<InputStream> inputStreams,ArrayList<FileInfo> downloadFileNameList,long actionSize){
        this.inputStreams =inputStreams;
        this.inFiles =downloadFileNameList;
        this.mode =MODE_DOWNLOAD;
        downloadInit(actionSize);
        return this;
    }

    public FileOperation to(FileInfo to$Path) {
        notifitionMsg = "源文件不存在";
        if(mode==MODE_MOVE||mode==MODE_COPY){
            if (!to$Path.isDir()) {
                LogUtils.i("FileOperation", "目标不是文件夹");
                notifitionMsg = "目标不是文件夹";
                isReady = false;
                return null;
            }
        }else if(mode==MODE_COMPRESS){
            File f =new File(to$Path.getFilePath());
            if(f.exists()){
                LogUtils.i("FileOperation.to():mode==MODE_COMPRESS", "存在同名文件");
                notifitionMsg="存在同名文件";
                isReady = false;
                return null;
            }
        }else {
            notifitionMsg = "源文件不存在";
        }
        this.toDir = to$Path;
        progressMessage = new ProgressMessage(System.currentTimeMillis(), actionSize, projectCount, mode, to$Path.getFilePath());
        return this;
    }

    public FileOperation to(String to$Path) {
        File to =new File(to$Path);
        if(mode==MODE_MOVE||mode==MODE_COPY){
            if (!to.isDirectory()) {
                LogUtils.i("FileOperation", "目标不是文件夹");
                notifitionMsg = "目标不是文件夹";
                isReady = false;
                return null;
            }
        }else if(mode==MODE_COMPRESS){
            if(to.exists()){
                LogUtils.i("FileOperation.to():mode==MODE_COMPRESS", "存在同名文件");
                notifitionMsg="存在同名文件";
                isReady = false;
                return null;
            }
        }
        this.toDir = getFileInfoFromPath(to$Path);
        progressMessage = new ProgressMessage(System.currentTimeMillis(), actionSize, projectCount, mode, to$Path);
        return this;
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
        isReady = projectCount > 0;
    }

    private void downloadInit(long actionSize){
        overCount = 0;
        overSize = 0;
        this.actionSize =actionSize;
        projectCount = inputStreams.size();
        isReady = inputStreams.size()>0;
    }

    public void pushProgressMsg() {
        if (progressMessage != null && progressChangeListener != null) {
            progressChangeListener.progressChang(progressMessage);
        }
    }


    /**
     * 历遍目录获取总大小
     * @param f
     */
    public long getLength(File f) {
        long size = 0;
        if (f.isDirectory() && f.exists()) {
            projectCount++;
            File[] files = f.listFiles();
            if (files != null)
                for (File file : files) {
                    size += getLength(file);
                }
        }
        if (f.isFile() && f.exists()) {
            projectCount++;
            return f.length();
        }
        return size;
    }

    private long onlyGetLength(File f) {
        long size = 0;
        if (f.isDirectory() && f.exists()) {
            File[] files = f.listFiles();
            if (files != null)
                for (File file : files) {
                    size += onlyGetLength(file);
                }
        }
        if (f.isFile() && f.exists()) {
            overCount++;
            return f.length();
        }
        return size;
    }


    private void filesCopy() {
        for (FileInfo temp : inFiles) {
            if (!running) {
                break;
            }
            if (toDir.getFilePath().startsWith(temp.getFilePath())) {
                System.err.println("目标文件夹是源文件的子目录");
                notifitionMsg = "目标文件夹是源文件的子目录";
                missionNotReady();
                break;
            }
            progressMessage.setIn(temp.getFilePath());
            File$DirCopy(new File(temp.getFilePath()), new File(toDir.getFilePath()));
        }
    }

    private void File$DirCopy(File inFile, File toDir) {
        if (!inFile.isDirectory()) {
            nioBufferCopy(inFile, new File(toDir, inFile.getName()));
        } else {
            copyDirWithFile(inFile, toDir);
        }
    }


    private void missionFinish() {
        progressMessage.setNowLoacation(actionSize);
        progressMessage.setProjectCount(overCount);
        if (progressChangeListener != null)
            progressChangeListener.progressChang(progressMessage);
        notificationManager.cancel(1410);

    }

    private void missionNotReady() {
        if (progressMessage == null) {
            progressMessage = new ProgressMessage(System.currentTimeMillis(), overSize, projectCount, mode);
        }
        progressMessage.setTitle(notifitionMsg);
        if (progressChangeListener != null)
            progressChangeListener.progressChang(progressMessage);
        notificationManager.cancel(1410);
        builder.setContentTitle(notifitionMsg);
        builder.setProgress(100, 100, false);
        notificationManager.notify(39, builder.build());
    }


    private void filesDelete() {
        for (FileInfo temp : inFiles) {
            if (!running)
                break;
            String loaction = temp.getFilePath();
            progressMessage.setNowProjectName(temp.getFileName());
            if (temp.isDir()) {
                deleteDirectory(loaction);
            } else {
                deleteSingleFile(loaction);
            }
        }
    }

    private void filesMove() {
        for (FileInfo temp : inFiles) {
            if (!running)
                break;
            if (toDir.getFilePath().startsWith(temp.getFilePath())) {
                System.err.println("目标文件夹是源文件的子目录");
                notifitionMsg = "目标文件夹是源文件的子目录";
                missionNotReady();
                break;
            }
            File f = new File(temp.getFilePath());
            if (moveFile(f)) {
                overCount++;
                progressMessage.setProjectOverCount(overCount);
            }
            refreshProgresss(f.length());
        }
    }

    public FileOperation applyZipParameters(ZipParameters zipParameters){
        if(mode==MODE_COMPRESS){
            this.zipParameters=zipParameters;
        }else {
            this.zipParameters=null;
        }
        return this;
    }

    private boolean compressFiles(){
        List<File> fs =new ArrayList<>();
        for(int i = 0;i<inFiles.size();i++){
            File f =new File(inFiles.get(i).getFilePath());
            fs.add(f);
        }
        try {
            zipEncryptMulti(fs,new File(toDir.getFilePath()));
        } catch (ZipException e) {
            LogUtils.e("压缩文件出错:",e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void zipEncryptMulti(List<File> files, File outFile) throws ZipException {
        zipEncryptMulti(files,outFile,null);
    }

    /**
     * 压缩一组文件
     * @param files
     * @param outFile
     * @param pass
     * @throws ZipException
     */
    public void zipEncryptMulti(List<File> files, File outFile, String pass) throws ZipException {
        if(files==null||files.size()==0)return;
        if(zipParameters==null){
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // 压缩方式
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL); // 压缩级别
            if(pass!=null){
                parameters.setEncryptFiles(true);
                parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD); // 加密方式
                parameters.setPassword(pass);//密码
            }
        }
        //parameters.setIncludeRootFolder(true);
        //压缩文件,并生成压缩文件
        ZipFile zipFile  =new ZipFile(outFile);
        for (File temp : files) {
            if (temp.isFile()) {
                zipFile.addFile(temp, zipParameters);
            } else {
                zipFile.addFolder(temp, zipParameters);
            }
            refreshProgresss(onlyGetLength(temp));
        }
        missionFinish();
    }

    /**
     * 移动文件
     *
     * @param file
     *
     * @return
     */
    private boolean moveFile(File file) {
        File result = new File(toDir.getFilePath(), file.getName());
        boolean b = file.renameTo(result);
        return b;
    }

    public void stopAction() {
        running = false;
        notifitionMsg = "正在取消-";
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
            progressMessage.setProjectOverCount(overCount);
            closeAnyThing(inStream, in, outStream, out);
        }
    }

    /**
     * 复制文件时发送进度
     */
    private void refreshProgresss(long size) {
        progressMessage.setProjectOverCount(overCount);
        overSize += size;
        progressMessage.setNowLoacation(overSize);
        overSize = progressMessage.getNowLoacation();
        if (progressChangeListener != null) progressChangeListener.progressChang(progressMessage);
        builder.setProgress(100, progressMessage.getProgress(), false);
        builder.setContentTitle(notifitionMsg + progressMessage.getTitle());
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
        }else{
            missionFinish();
        }
    }

    /**
     * 删除单个文件
     *
     * @param filePath$Name 要删除的文件的文件名
     *
     * @return 单个文件删除成功返回true，否则返回false
     */
    private boolean deleteSingleFile(String filePath$Name) {
        File file = new File(filePath$Name);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            overCount++;
            progressMessage.setProjectOverCount(overCount);
            refreshProgresss(file.length());
            if (file.delete()) {
                LogUtils.i("删除进程：", "Copy_Delete.deleteSingleFile: 删除单个文件" + filePath$Name + "成功！");
                return true;
            } else {
                LogUtils.i("删除进程：", "删除单个文件-" + filePath$Name + "-失败！");
                return false;
            }
        } else {
            LogUtils.i("删除进程：", "删除单个文件失败-" + filePath$Name + "-不存在！");
            return false;
        }
    }

    /**
     * 删除目录及目录下的文件
     *
     * @param filePath 要删除的目录的文件路径
     *
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
            LogUtils.i("删除进程", "Copy_Delete.deleteDirectory: 删除目录" + filePath + "成功！");
            return true;
        } else {
            LogUtils.i("删除进程：", "删除目录：" + filePath + "失败！");
            return false;
        }
    }

    public ArrayList<FileInfo> getInFiles() {
        return inFiles;
    }

    public FileInfo getToDir() {
        return toDir;
    }

    @Override
    public void run() {
        if (!isReady) {
            missionNotReady();
            NotificationUtils.setNotification(context, builder, notificationManager, notifitionMsg);
            return;
        } else {
            NotificationUtils.setNotification(context, builder, notificationManager, notifitionMsg);
            notifitionMsg = "任务进行中-";
            running = true;
        }
        switch (mode) {
            case MODE_COPY:
                if (progressChangeListener != null)
                    progressChangeListener.progressChang(progressMessage);
                filesCopy();
                break;
            case MODE_MOVE:
                if (progressChangeListener != null)
                    progressChangeListener.progressChang(progressMessage);
                filesMove();
                break;
            case MODE_DELETE:
                progressMessage = new ProgressMessage(System.currentTimeMillis(), actionSize, projectCount, mode);
                filesDelete();
                break;
            case MODE_COMPRESS:
                if (progressChangeListener != null)
                    progressChangeListener.progressChang(progressMessage);
                compressFiles();
                break;
            case MODE_DOWNLOAD:
                if (progressChangeListener != null)
                    progressChangeListener.progressChang(progressMessage);
                downloadFiles();
                break;
        }
        missionFinish();
    }

    private void downloadFiles() {
        for(InputStream in:inputStreams){
            downloadFile(in,inFiles.get(inputStreams.indexOf(in)).getFileName());
        }
        missionFinish();
    }

    private void downloadFile(InputStream inputStream, String name) {
        File storeFile = new File(toDir.getFilePath(),name);
        FileOutputStream fis = null;
        int sum=0;
        try {
            fis = new FileOutputStream(storeFile);
            byte[] b =new byte[1024];
            int len;
            while ((len = inputStream.read(b)) != -1) {
                fis.write(b,0,len);
                fis.flush();
                sum+=len;
                refreshProgresss(len);
            }
            System.out.println("数据大小：---------------------------------------------"+sum);
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            closeAnyThing(fis, inputStream);
        }
    }
}
