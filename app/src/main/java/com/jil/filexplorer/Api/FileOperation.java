package com.jil.filexplorer.Api;

import com.jil.filexplorer.utils.LogUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import static com.jil.filexplorer.utils.FileUtils.closeAnyThing;

/**
 * 文件操作类
 * 必须在非UI线程调用
 */
public class FileOperation {
    public final static int MODE_COPY =1521;
    public final static int MODE_COPYS =1541;
    public final static int MODE_MOVE =1410;
    public final static int MODE_RENAME=1415;
    private int ID;
    private File inFile;
    private int mode;
    private ArrayList<File> inFiles;
    private long actionSize;
    private long overSize;
    private boolean running;
    private int overCount;
    private ProgressMessage progressMessage;
    private ProgressChangeListener progressChangeListener;

    private File toDir;
    private static FileOperation fileOperation;
    private int projectCount;

    public int getProjectCount() {
        return projectCount;
    }

    private FileOperation(int ID) {
        this.ID = ID;
    }

    public void addProgressChangeListener(ProgressChangeListener progressChangeListener){
        this.progressChangeListener=progressChangeListener;
    }

    public ProgressMessage getProgressMessage() {
        return progressMessage;
    }

    public static FileOperation with(int id){
        fileOperation =new FileOperation(id);
        return fileOperation;
    }

    public FileOperation copy(File inFile){
        this.inFile =inFile;
        this.mode=MODE_COPY;
        return this;
    }

    public FileOperation move(ArrayList<FileInfo> inFiles){

    }

    public FileOperation copy(ArrayList<File> inFiles){
        this.inFiles =inFiles;
        this.mode=MODE_COPYS;
        initialization();
        return this;
    }

    public FileOperation to(File toDir){
        if(!toDir.isDirectory()) {
            LogUtils.e("FileOperation", "目标不是文件夹");
            return null;
        }else {
            this.toDir=toDir;
            progressMessage=new ProgressMessage(System.currentTimeMillis(),actionSize
                    ,projectCount,mode,toDir.getPath());
            return this;
        }
    }

    private void initialization() {
        actionSize=0;
        overSize=0;
        projectCount=0;
        overCount=0;
        if(mode==MODE_COPYS&&inFiles.size()>0){
            long size=0;
            for(File temp:inFiles){
                size+=getLength(temp);
            }
            actionSize=size;
        }else {
            actionSize=getLength(inFile);
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


    private void FilesCopy() {
        for (File temp:inFiles){
            progressMessage.setIn(temp.getPath());
            File$DirCopy(temp,toDir);
        }
    }

    private void File$DirCopy(File inFile , File toDir) {
        if(toDir.getPath().startsWith(inFile.getPath())){
            System.err.println("目标文件夹是源文件的子目录");
            return ;
        }
        if(!inFile.isDirectory()){
            nioBufferCopy(inFile,new File(toDir,inFile.getName()));
        }else {
            copyDirWithFile(inFile,toDir);
        }

    }

    public void start(){
        running=true;
        progressChangeListener.progressChang(progressMessage);
        switch (mode){
            case MODE_COPY:
                File$DirCopy(inFile,toDir);
                break;
            case MODE_COPYS:
                FilesCopy();
                break;
        }
    }

    public void stopAction(){
        running=false;
    }

    /**
     * 复制文件，可监测进度
     * @param source G:\作业\安卓\ayun.apk
     * @param target G:\作业\安卓\安卓项目开发\解包打包G:\作业\安卓\安卓项目开发\解包打包\ayun.apk
     */
    private void nioBufferCopy(File source, File target) {
        FileChannel in = null;
        FileChannel out = null;
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        long size =source.length();
        try {
            inStream = new FileInputStream(source);
            outStream = new FileOutputStream(target);
            in = inStream.getChannel();
            out = outStream.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(1024*1024*20);
            progressMessage.setNowProjectName(source.getName());
            while (in.read(buffer) != -1) {
                buffer.flip();
                out.write(buffer);
                buffer.clear();
                size-=1024*1024*20;
                sendProgress(size>0 ? 1024*1024*20:(1024*1024*20)+size);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            overCount++;
            progressMessage.setCopyOverCount(overCount);
            closeAnyThing(inStream,in,outStream,out);
        }
    }

    private void sendProgress(long add) {
        overSize+=add;
        progressMessage.setNowLoacation(overSize);
        if(actionSize>0&&progressChangeListener!=null)
            progressChangeListener.progressChang(progressMessage);
        //progressChangeListener.progressChang((int)((overSize*100)/actionSize),overSize);

    }

    /**
     * 复制文件夹及其路径下的所有文件
     * @param from  G:\作业\安卓
     * @param to    F:\Oracle VM VirtualBox\
     */
    private void copyDirWithFile(File from,File to) {
        File targt =new File(to.getPath(),from.getName()); //F:\Oracle VM VirtualBox\安卓
        if(!targt.exists()) targt.mkdir();
        File[] files =from.listFiles();
        if(files!=null){
            for(File temp:files){
                if(!running)
                    break;
                if(temp.isFile()){
                    nioBufferCopy(temp,new File(targt,temp.getName()));
                }else {
                    copyDirWithFile(temp,targt);
                }
            }
        }
    }

}
