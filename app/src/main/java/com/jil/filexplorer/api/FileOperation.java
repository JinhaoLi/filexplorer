package com.jil.filexplorer.api;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;

import android.content.Intent;
import androidx.core.app.NotificationCompat;

import com.jil.filexplorer.R;
import com.jil.filexplorer.activity.ProgressActivity;
import com.jil.filexplorer.bean.FileInfo;
import com.jil.filexplorer.bean.ProgressMessage;
import com.jil.filexplorer.utils.LogUtils;

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
import java.util.HashMap;
import java.util.List;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.jil.filexplorer.utils.ConstantUtils.PROGRESS_ID;
import static com.jil.filexplorer.utils.FileUtils.closeAnyThing;
import static com.jil.filexplorer.utils.FileUtils.getFileInfoFromPath;

/**
 * 文件操作类
 * 必须在非UI线程调用
 * 结合ProgressMessage，ProgressChangeListener
 */
public class FileOperation implements Runnable {

    public final static int MODE_COPY = -4;
    public final static int MODE_MOVE = -5;
    public final static int MODE_DELETE = -6;
    public final static int MODE_RENAME = -7;
    public final static int MODE_COMPRESS = -8;
    public final static int MODE_DOWNLOAD = -9;
    public final static int MODE_CREATE_FILE = -10;
    public static final int MODE_RECYCLE = -11;
    /**
     * 任务队列
     */
    private static HashMap<Long, FileOperation> MissionList;
    private long id;
    /**
     * 任务模式
     */
    private int mode;
    /**
     * 剪切板
     * 下载模式MODE_DOWNLOAD下，此为下载文件名数组
     */
    public static ArrayList<FileInfo> inFiles;
    /**
     * 待删除文件
     */
    private ArrayList<FileInfo> deleteList;
    /**
     * 模式MODE_DOWNLOAD输入
     */
    private ArrayList<InputStream> inputStreams;
    /**
     * 任务总大小
     */
    private long totalTaskSize;
    /**
     * 已完成大小
     */
    private long completedSize;
    /**
     * 运行状态
     */
    private boolean running;
    /**
     * 完成项目数
     */
    private int completedCount;
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
    private FileChangeListener fileChangeListener;
    /**
     * 是否准备完成
     */
    private boolean isReady;
    /***
     * 压缩文件参数
     */
    private ZipParameters zipParameters;
    /**
     * 点击通知显示进度activity
     */
    private PendingIntent pendingIntent;

    /**
     * 通知消息
     */
    private String notificationMessage;

    /**
     * 通知构建
     */
    private NotificationCompat.Builder builder;

    private static NotificationManager notificationManager;

    public void setProgressChangeListener(ProgressChangeListener progressChangeListener) {
        this.progressChangeListener = progressChangeListener;
    }

    public void setFileChangeListener(FileChangeListener fileChangeListener) {
        this.fileChangeListener = fileChangeListener;
    }

    public long getId() {
        return id;
    }

    private FileOperation(Context context, long id) {
        this.id = id;
        this.pendingIntent = getIntent(context, id);
        this.builder = new NotificationCompat.Builder(context, PROGRESS_ID);
        this.notificationMessage = "任务准备中...";
    }

    @SuppressLint("UseSparseArrays")
    public static FileOperation with(Context context) {
        FileOperation fileOperation = new FileOperation(context, System.currentTimeMillis());

        if (MissionList == null)
            MissionList = new HashMap<>();
        MissionList.put(fileOperation.id, fileOperation);

        if (notificationManager == null)
            notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        return fileOperation;
    }

    public static FileOperation getInstance(long id) {
        FileOperation fileOperation = MissionList.get(id);
        if (fileOperation != null) {
            return fileOperation;
        } else {
            return null;
        }
    }

    public FileOperation compress() {
        this.mode = MODE_COMPRESS;
        initialization(inFiles);
        return this;
    }

    public FileOperation move() {
        this.mode = MODE_MOVE;
        initialization(inFiles);
        return this;
    }

    public FileOperation moveToRecycleBin(ArrayList<FileInfo> deleteList) {
        this.deleteList = deleteList;
        this.mode = MODE_RECYCLE;
        initialization(deleteList);
        return this;
    }

    public FileOperation delete(ArrayList<FileInfo> deleteList) {
        this.deleteList = deleteList;
        this.mode = MODE_DELETE;
        initialization(deleteList);
        return this;
    }

    public FileOperation copy() {
        this.mode = MODE_COPY;
        initialization(inFiles);
        return this;
    }

    public FileOperation download(ArrayList<InputStream> inputStreams, ArrayList<FileInfo> downloadFileNameList, long actionSize) {
        this.inputStreams = inputStreams;
        this.inFiles = downloadFileNameList;
        this.mode = MODE_DOWNLOAD;
        downloadInit(actionSize);
        return this;
    }

    public FileOperation to(FileInfo to$Path) {
        notificationMessage = "源文件不存在";
        if (mode == MODE_MOVE || mode == MODE_COPY) {
            if (!to$Path.isDir()) {
                LogUtils.i("FileOperation", "目标不是文件夹");
                notificationMessage = "目标不是文件夹";
                isReady = false;
                return null;
            }
        } else if (mode == MODE_COMPRESS) {
            File f = new File(to$Path.getFilePath());
            if (f.exists()) {
                LogUtils.i("FileOperation.to():mode==MODE_COMPRESS", "存在同名文件");
                notificationMessage = "存在同名文件";
                isReady = false;
                return null;
            }
        } else {
            notificationMessage = "源文件不存在";
        }
        this.toDir = to$Path;
        progressMessage = new ProgressMessage(System.currentTimeMillis(), totalTaskSize, projectCount, mode, to$Path.getFilePath());
        return this;
    }

    public FileOperation to(String to$Path) {
        File to = new File(to$Path);
        if (mode == MODE_MOVE || mode == MODE_COPY) {
            if (!to.isDirectory()) {
                LogUtils.i("FileOperation", "目标不是文件夹");
                notificationMessage = "目标不是文件夹";
                isReady = false;
                return null;
            }
        } else if (mode == MODE_COMPRESS) {
            if (to.exists()) {
                LogUtils.i("FileOperation.to():mode==MODE_COMPRESS", "存在同名文件");
                notificationMessage = "存在同名文件";
                isReady = false;
                return null;
            }
        }
        this.toDir = getFileInfoFromPath(to$Path);
        progressMessage = new ProgressMessage(System.currentTimeMillis(), totalTaskSize, projectCount, mode, to$Path);
        return this;
    }


    private void initialization(ArrayList<FileInfo> inFiles) {
        completedSize = 0;
        totalTaskSize = 0;
        projectCount = 0;
        completedCount = 0;
        if (inFiles.size() > 0) {
            long size = 0;
            for (FileInfo temp : inFiles) {
                size += getLength(new File(temp.getFilePath()));
            }
            totalTaskSize = size;
        }
        isReady = projectCount > 0;
        LogUtils.d(getClass().getName(),"totalTaskSize="+totalTaskSize+"\t"+"projectCount="+projectCount);
    }

    private void downloadInit(long actionSize) {
        completedCount = 0;
        completedSize = 0;
        this.totalTaskSize = actionSize;
        projectCount = inputStreams.size();
        isReady = inputStreams.size() > 0;
    }

    public void pushProgressMsg() {
        if (progressMessage != null && progressChangeListener != null) {
            progressChangeListener.progressChang(progressMessage);
        }
    }


    /**
     * 历遍目录获取总大小
     *
     * @param f
     */
    public long getLength(File f) {
        long size = 0;
        if (!f.exists()) {
            notificationMessage = f + "不存在";
        }
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
            completedCount++;
            return f.length();
        }
        return size;
    }


    private boolean filesCopy() {
        for (FileInfo temp : inFiles) {
            if (!running) {
                return false;
            }
            if (toDir.getFilePath().startsWith(temp.getFilePath())) {
                System.err.println("目标文件夹是源文件的子目录");
                notificationMessage = "目标文件夹是源文件的子目录";
                missionNotReady();
                return false;
            }
            progressMessage.setIn(temp.getFilePath());
            File$DirCopy(new File(temp.getFilePath()), new File(toDir.getFilePath()));
        }
        return true;
    }

    private void File$DirCopy(File inFile, File toDir) {
        if (!inFile.isDirectory()) {
            nioBufferCopy(inFile, new File(toDir, inFile.getName()));
        } else {
            copyDirWithFile(inFile, toDir);
        }
    }


    private void missionFinish() {
        progressMessage.setNowLoacation(totalTaskSize);
        progressMessage.setProjectOverCount(completedCount);
        if (progressChangeListener != null)
            progressChangeListener.progressChang(progressMessage);
        if (fileChangeListener != null)
            fileChangeListener.change();
        notificationManager.cancel((int) id);
        removeMission(id);

    }

    public static void removeMission(long id) {
        MissionList.remove(id);
    }

    private void missionNotReady() {
        if (progressMessage == null) {
            progressMessage = new ProgressMessage(System.currentTimeMillis(), totalTaskSize, projectCount, mode);
        }
        progressMessage.setTitle(notificationMessage);
        if (progressChangeListener != null)
            progressChangeListener.progressChang(progressMessage);
        notificationManager.cancel((int) id);
        setNotification(0);
    }


    private void filesDelete() {
        for (FileInfo temp : deleteList) {
            if (!running)
                break;
            String location = temp.getFilePath();
            progressMessage.setIn(location);
            progressMessage.setNowProjectName(temp.getFileName());
            if (temp.isDir()) {
                deleteDirectory(location);
            } else {
                deleteSingleFile(location);
            }
        }
    }


    private boolean filesMove() {
        ArrayList<FileInfo> list;
        if (deleteList != null && deleteList.size() != 0) {
            list = deleteList;
        } else {
            list = inFiles;
        }
        for (FileInfo temp : list) {
            if (!running)
                return false;
            if (toDir.getFilePath().startsWith(temp.getFilePath())) {
                System.err.println("目标文件夹是源文件的子目录");
                notificationMessage = "目标文件夹是源文件的子目录";
                missionNotReady();
                return false;
            }
            File f = new File(temp.getFilePath());
            if (moveFile(f)) {
                completedCount++;
                progressMessage.setIn(f.getPath());
                progressMessage.setProjectOverCount(completedCount);
            }
            updateProgress(f.length());
        }
        return true;
    }

    public FileOperation applyZipParameters(ZipParameters zipParameters) {
        if (mode == MODE_COMPRESS) {
            this.zipParameters = zipParameters;
        } else {
            this.zipParameters = null;
        }
        return this;
    }

    private boolean compressFiles() {
        List<File> fs = new ArrayList<>();
        for (int i = 0; i < inFiles.size(); i++) {
            File f = new File(inFiles.get(i).getFilePath());
            fs.add(f);
        }
        try {
            zipEncryptMulti(fs, new File(toDir.getFilePath()));
        } catch (ZipException e) {
            LogUtils.e("压缩文件出错:", e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void zipEncryptMulti(List<File> files, File outFile) throws ZipException {
        zipEncryptMulti(files, outFile, null);
    }

    /**
     * 压缩一组文件
     *
     * @param files
     * @param outFile
     * @param pass
     * @throws ZipException
     */
    public void zipEncryptMulti(List<File> files, File outFile, String pass) throws ZipException {
        if (files == null || files.size() == 0) return;
        if (zipParameters == null) {
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // 压缩方式
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL); // 压缩级别
            if (pass != null) {
                parameters.setEncryptFiles(true);
                parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD); // 加密方式
                parameters.setPassword(pass);//密码
            }
        }
        //parameters.setIncludeRootFolder(true);
        //压缩文件,并生成压缩文件
        ZipFile zipFile = new ZipFile(outFile);
        for (File temp : files) {
            if (temp.isFile()) {
                zipFile.addFile(temp, zipParameters);
            } else {
                zipFile.addFolder(temp, zipParameters);
            }
            updateProgress(onlyGetLength(temp));
        }
        missionFinish();
    }

    /**
     * 移动文件
     *
     * @param file
     * @return
     */
    private boolean moveFile(File file) {
        File result = new File(toDir.getFilePath(), file.getName());

        boolean b = file.renameTo(result);
        return b;
    }

    public void stopAction() {
        running = false;
        notificationMessage = "正在取消-";
    }

    /**
     * 复制文件，可监测进度
     *
     * @param source G:\作业\安卓\ayun.apk
     * @param target G:\作业\安卓\安卓项目开发\解包打包G:\作业\安卓\安卓项目开发\解包打包\ayun.apk
     */
    private void nioBufferCopy(File source, File target) {
        if (!source.exists()) {
            notificationMessage = source.getPath() + "不存在！";
            updateProgress(0);
            return;
        }
        if(target.exists()){
            notificationMessage=target.getPath()+"已存在!";
            updateProgress(0);
            return;
        }
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
                updateProgress(size > 0 ? 1024 * 1024 * 20 : (1024 * 1024 * 20) + size);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            completedCount++;
            progressMessage.setProjectOverCount(completedCount);
            closeAnyThing(inStream, in, outStream, out);
        }
    }

    /**
     * 复制文件时发送进度
     */
    private void updateProgress(long size) {
        progressMessage.setProjectOverCount(completedCount);
        completedSize += size;
        progressMessage.setNowLoacation(completedSize);
        completedSize = progressMessage.getNowLoacation();
        if (progressChangeListener != null)
            progressChangeListener.progressChang(progressMessage);
        setNotification(progressMessage.getProgress());
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
        } else {
            missionFinish();
        }
    }

    /**
     * 删除单个文件
     *
     * @param filePath$Name 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    private boolean deleteSingleFile(String filePath$Name) {
        File file = new File(filePath$Name);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                completedCount++;
                progressMessage.setProjectOverCount(completedCount);
                updateProgress(file.length());
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

            completedCount++;
            progressMessage.setProjectOverCount(completedCount);
            updateProgress(1);

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
        if (SettingParam.TestModeSwitch > 0) {
            test();
            return;
        }
        if (!isReady) {
            missionNotReady();
            return;
        } else {
            notificationMessage = "任务进行中-";
            setNotification(0);
            running = true;
        }
        switch (mode) {
            case MODE_COPY:
                if (progressChangeListener != null)
                    progressChangeListener.progressChang(progressMessage);
                if (!filesCopy()) {
                    return;
                }
                break;
            case MODE_RECYCLE:
            case MODE_MOVE:
                if (progressChangeListener != null)
                    progressChangeListener.progressChang(progressMessage);
                if (!filesMove()) {
                    return;
                }
                break;
            case MODE_DELETE:
                progressMessage = new ProgressMessage(System.currentTimeMillis(), totalTaskSize, projectCount, mode);
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

    private void test() {
        ArrayList<FileInfo> missionList;
        if(mode==MODE_DELETE||mode==MODE_RECYCLE){
            missionList=deleteList;
        }else {
            missionList=inFiles;
        }
        if (!isReady) {
            return;
        }
        notificationMessage = "任务进行中...";
        running = true;
        if (progressMessage == null)
            progressMessage = new ProgressMessage(System.currentTimeMillis(), totalTaskSize, projectCount, mode);
        setNotification(0);

        if(mode!=MODE_COPY&&mode!=MODE_COMPRESS&&mode!=MODE_DOWNLOAD&&running){
            for(int i=0;i<projectCount;i++){
                if(!running){
                    break;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                completedCount++;
                progressMessage.setIn(missionList.get(0).getFilePath());
                progressMessage.setNowProjectName(missionList.get(0).getFileName());
                updateProgress(totalTaskSize/projectCount);
            }
        }else {
            while (running) {

                if (progressMessage.getProgress() == 100) {
                    break;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                progressMessage.setIn(missionList.get(0).getFilePath());
                progressMessage.setNowProjectName(missionList.get(0).getFileName());
                updateProgress(totalTaskSize / 10);
            }
        }

        completedCount = projectCount;
        missionFinish();
    }

    private void downloadFiles() {
        for (InputStream in : inputStreams) {
            downloadFile(in, inFiles.get(inputStreams.indexOf(in)).getFileName());
        }
        missionFinish();
    }

    private void downloadFile(InputStream inputStream, String name) {
        File storeFile = new File(toDir.getFilePath(), name);
        FileOutputStream fis = null;
        int sum = 0;
        try {
            fis = new FileOutputStream(storeFile);
            byte[] b = new byte[1024];
            int len;
            while ((len = inputStream.read(b)) != -1) {
                fis.write(b, 0, len);
                fis.flush();
                sum += len;

                updateProgress(len);
            }
            System.out.println("数据大小：---------------------------------------------" + sum);
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            closeAnyThing(fis, inputStream);
        }
    }

    private static PendingIntent getIntent(Context context, long id) {
        Intent intent = new Intent(context, ProgressActivity.class);
        intent.putExtra("FileOperation.id", id);
        return PendingIntent.getActivity(context, (int) (Math.random() * 100000), intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public void setNotification(int progress) {
        String message;
        if (progressMessage != null)
            message = progressMessage.getTitle(progress);
        else
            message = "";

        Notification notification = builder.setSmallIcon(R.mipmap.copy_move_progress_ico)
                .setWhen(id)
                .setContentTitle(notificationMessage + message)
                .setProgress(100, progress, false)
                .setContentIntent(pendingIntent)
                .build();
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        if (notificationManager != null) {
            notificationManager.notify((int) getId(), notification);
        }
    }


}
