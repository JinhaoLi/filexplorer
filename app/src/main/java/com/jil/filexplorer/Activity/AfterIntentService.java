package com.jil.filexplorer.Activity;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.jil.filexplorer.Api.FileInfo;
import com.jil.filexplorer.Api.ProgressMessage;
import com.jil.filexplorer.R;
import com.jil.filexplorer.ui.CopyProgressDialog;
import com.jil.filexplorer.utils.LogUtils;
import com.jil.filexplorer.utils.NotificationUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.jil.filexplorer.utils.ConstantUtils.CHANNEL_ID;
import static com.jil.filexplorer.utils.ConstantUtils.MB;
import static com.jil.filexplorer.utils.ConstantUtils.PROGRESS_MODE_COPY;
import static com.jil.filexplorer.utils.DialogUtils.getDensity;
import static com.jil.filexplorer.utils.FileUtils.closeAnyThing;

public class AfterIntentService extends IntentService{
    private static final String ACTION_COPY = "com.jil.filexplorer.Activity.action.COPY";
    private static final String ACTION_MOVE = "com.jil.filexplorer.Activity.action.MOVE";
    private static final String EXTRA_ComeFile = "com.jil.filexplorer.Activity.extra.PARAM1";
    private static final String EXTRA_ToFile = "com.jil.filexplorer.Activity.extra.PARAM2";
    //进度对象
    private ProgressMessage mProgresss;
    //文件总大小
    private long allSize;
    //文件总数量
    private int projectCount;
    //已复制项目数
    private int copyOverCount;
    //已复制
    private long copySize;
    //handler接收对象
    boolean over;

    //多项复制数组
    private static ArrayList<FileInfo> fileInfoArrayList;

    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;

    private static UpdateUI updateUI;

    public static void setUpdateUI(UpdateUI updateUIInterface) {
        updateUI = updateUIInterface;
    }


    public AfterIntentService() {
        super("com.jil.filexplorer.Activity.AfterIntentService");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static void startActionsCopy(Context context, String comeFilePath, String toFilePath,ArrayList<FileInfo> infos) {
        fileInfoArrayList=infos;
        Intent intent = new Intent(context, AfterIntentService.class);
        intent.setAction(ACTION_COPY);
        intent.putExtra(EXTRA_ComeFile, comeFilePath);
        intent.putExtra(EXTRA_ToFile, toFilePath);
        context.startService(intent);
    }


    public static void startActionCopy(Context context, String comeFilePath, String toFilePath) {
        Intent intent = new Intent(context, AfterIntentService.class);
        intent.setAction(ACTION_COPY);
        intent.putExtra(EXTRA_ComeFile, comeFilePath);
        intent.putExtra(EXTRA_ToFile, toFilePath);
        context.startService(intent);
    }

    public static void startActionMove(Context context, String param1, String param2) {
        Intent intent = new Intent(context, AfterIntentService.class);
        intent.setAction(ACTION_MOVE);
        intent.putExtra(EXTRA_ComeFile, param1);
        intent.putExtra(EXTRA_ToFile, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_COPY.equals(action)) {
                builder = new NotificationCompat.Builder(this, CHANNEL_ID);
                notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
                String in = intent.getStringExtra(EXTRA_ComeFile);
                String to = intent.getStringExtra(EXTRA_ToFile);
                mProgresss = new ProgressMessage(System.currentTimeMillis(), projectCount, PROGRESS_MODE_COPY, in, to);
                copyFile$Dir(new File(in), new File(to));
            } else if (ACTION_MOVE.equals(action)) {

            }
        }
    }

    private void copyFile$Dir(File in, File to) {
        allSize=getLength(in);
        mProgresss.setProjectCount(projectCount);
        mProgresss.setEndLoacation(allSize);
        File file=new File(to.getPath(),in.getName());
        Intent i =new Intent(this,ProgressActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (in.isFile()) {//文件对拷
            if(!file.exists()){
                startActivity(i);
                NotificationUtils.setNotification(this, builder, notificationManager,"复制任务进行中...");
                canSeeBufferCopy(in, file);
                misstionFinish();
            }else NotificationUtils.setNotification(this, builder, notificationManager,"路径包含重名文件");

        } else if(to.exists()) {
            if(!file.exists()){
                startActivity(i);
                NotificationUtils.setNotification(this, builder, notificationManager,"复制任务进行中...");
                copyDirWithFile(in, to);
                misstionFinish();
            }else NotificationUtils.setNotification(this, builder, notificationManager,"路径包含重名文件夹");


        }

    }

    /**
     * 复制文件
     *
     * @param source
     * @param target
     */
    private void nioBufferCopy(File source, File target) {
        LogUtils.i(source.getPath(), target.getPath());
        if (!source.isDirectory() && !target.isDirectory() && source.exists() && !target.exists()) {
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
                closeAnyThing(inStream, in, outStream, out);

            }
        }
    }

    /**
     * 复制文件，可监测进度
     *
     * @param source
     * @param target
     */
    private void canSeeBufferCopy(File source, File target) {
        mProgresss.setNowProjectName(source.getName());
        if (!target.isDirectory() && source.exists()) {
            FileChannel in = null;
            FileChannel out = null;
            FileInputStream inStream = null;
            FileOutputStream outStream = null;
            try {
                inStream = new FileInputStream(source);
                outStream = new FileOutputStream(target);
                in = inStream.getChannel();
                out = outStream.getChannel();
                ByteBuffer buffer = ByteBuffer.allocate(4096 * 20);
                while (in.read(buffer) != -1) {
                    buffer.flip();
                    out.write(buffer);
                    buffer.clear();
                    refreshProgresss(4096 * 20);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeAnyThing(inStream, in, outStream, out);
            }
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


    /**
     * 复制文件夹及其路径下的所有文件
     *
     * @param from
     * @param to
     * @throws IOException
     */
    public void copyDirWithFile(File from, File to) {
        File targt = new File(to.getPath(), from.getName());
        if (!targt.exists()) {
            targt.mkdir();
        }
        File[] files = from.listFiles();
        if (files != null) {
            for (File temp : files) {
                if (temp.isFile()) {
                    //nioBufferCopy(temp, new File(targt, temp.getName()));
                    canSeeBufferCopy(temp, new File(targt, temp.getName()));
                    copyOverCount++;
                    //refreshProgresss(temp.length());
                } else {
                    copyDirWithFile(temp, targt);
                }
            }
        }
    }

    private void misstionFinish() {
        Message finishMesssage = new Message();
        finishMesssage.what = -1;
        if (updateUI != null) updateUI.updateUI(finishMesssage);
        notificationManager.cancel(1410);
    }


    /**
     * 复制文件时发送进度
     */
    private void refreshProgresss(long size) {
        Message message = new Message();
        LogUtils.i("已复制：", mProgresss.getProggress() + "%--" + copySize / MB);
        mProgresss.setCopyOverCount(copyOverCount);
        copySize += size;
        mProgresss.setNowLoacation(copySize);
        copySize=mProgresss.getNowLoacation();
        message.obj = mProgresss;
        message.what = mProgresss.getProggress();
        if (updateUI != null) updateUI.updateUI(message);
        builder.setProgress(100, mProgresss.getProggress(), false);
        notificationManager.notify(1410, builder.build());
    }

    public interface UpdateUI {
        void updateUI(Message message);
    }


    /***
     * 一个悬浮窗口
     */
    private int time = 1;
    private CopyProgressDialog c1;
    float startX;
    float startY;
    //要引用的布局文件.
    LinearLayout toucherLayout;
    //布局参数.
    WindowManager.LayoutParams params;
    //实例化的WindowManager.
    WindowManager windowManager;
    ImageView imageButton1;
    TextView title, message2;
    //状态栏高度.（接下来会用到）
    int statusBarHeight = -1;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("ClickableViewAccessibility")
    private void createToucher() {
        //赋值WindowManager&LayoutParam.
        params = new WindowManager.LayoutParams();
        windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        //设置type.系统提示型窗口，一般都在应用程序窗口之上.
        if (Build.VERSION.SDK_INT <= 23) {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }

        //设置效果为背景透明.
        params.format = PixelFormat.RGBA_8888;
        //设置flags.不可聚焦及不可使用按钮对悬浮窗进行操控.
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        //设置窗口初始停靠位置.
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = 0;
        params.y = 0;

        //设置悬浮窗口长宽数据.
        //注意，这里的width和height均使用px而非dp.这里我偷了个懒
        //如果你想完全对应布局设置，需要先获取到机器的dpi
        //px与dp的换算为px = dp * (dpi / 160).
        float density = getDensity(this);
        LogUtils.i("DPI:", density * 160 + "");
        params.width = (int) (400 * density);
        params.height = (int) (180 * density);

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局.
        toucherLayout = (LinearLayout) inflater.inflate(R.layout.progress_dialog_layout, null);
        //添加toucherlayout
        windowManager.addView(toucherLayout, params);

        Log.i(TAG, "toucherlayout-->left:" + toucherLayout.getLeft());
        Log.i(TAG, "toucherlayout-->right:" + toucherLayout.getRight());
        Log.i(TAG, "toucherlayout-->top:" + toucherLayout.getTop());
        Log.i(TAG, "toucherlayout-->bottom:" + toucherLayout.getBottom());

        //主动计算出当前View的宽高信息.
        toucherLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        //用于检测状态栏高度.
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        imageButton1 = toucherLayout.findViewById(R.id.imageView8);
        Glide.with(this).load(R.drawable.ic_close_black_24dp).into(imageButton1);
        imageButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                windowManager.removeViewImmediate(toucherLayout);
            }
        });
        title = toucherLayout.findViewById(R.id.textView8);
        message2 = toucherLayout.findViewById(R.id.textView10);

        String s = "正在将172个项目从\t<font color=\"#1586C6\">G:\\程序\\cpu-z_1.89-cn</font>\t移动到\nE:\\Program Files\\Windows Portable Devices";
        message2.setText(Html.fromHtml(s));

        title.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getRawX();
                        startY = event.getRawY();
                    case MotionEvent.ACTION_MOVE:

                        params.x += (event.getRawX() - startX);
                        params.y += (event.getRawY() - startY);

                        windowManager.updateViewLayout(toucherLayout, params);

                        startX = event.getRawX();
                        startY = event.getRawY();
                        LogUtils.i("触摸信息：", startX + "--" + startY);
                        break;
                }

                return true;
            }
        });
    }

}
