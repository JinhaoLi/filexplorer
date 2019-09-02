package com.jil.filexplorer.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.jil.filexplorer.Activity.ProgressActivity;
import com.jil.filexplorer.Activity.MainActivity;
import com.jil.filexplorer.R;
import com.jil.filexplorer.ui.CopyProgressDialog;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.jil.filexplorer.utils.ConstantUtils.CHANNEL_ID;

public class NotificationUtils {
    private static final int NOTIFICATION_ID = 1410;
    /**
     * 注册通知步骤1填写相关信息
     */
    public static void registerNotifty(Context context) {
        if(Build.VERSION.SDK_INT>=26){
            String channelName = "复制进度通知";
            NotificationChannel channel =new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("用于显示复制·移动·重命名·删除等操作进度的通知！");
            createNotificationChannel(context,channel);
        }
    }

    /**
     * 注册通知步骤2提交
     * @param context
     * @param channel
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void createNotificationChannel(Context context, NotificationChannel channel) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(
                NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }


    public static void setNotification(Context context, NotificationCompat.Builder builder,NotificationManager notificationManager,String content) {
        Intent intent = new Intent(context, ProgressActivity.class);// ========= 重点2============
        PendingIntent pendingIntent = null;
        //if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(context, 0, intent,PendingIntent.FLAG_IMMUTABLE);
        //}
        //通知的构建过程基本与默认相同
        Notification notification = builder
                .setSmallIcon(R.mipmap.ic_launcher)//通知的构建过程基本与默认相同
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle(content)//标题
                .setProgress(100,0,false)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.copy_move_progress_ico))
                .setLocalOnly(true)
                .setPriority(Notification.DEFAULT_ALL)
                //.setVibrate(new long[]{100, 200, 200, 200})//震动
                .setContentIntent(pendingIntent)
                .build();
        //notification.flags=Notification.FLAG_ONGOING_EVENT; //如果此通知涉及正在进行的事情，请设置,用户无法移除
        //notification.flags |= Notification.FLAG_NO_CLEAR;//点击才可以移除
        //启动activity
        //Intent intentOne = new Intent(context, MainActivity.class);
        //PendingIntent pendingIntentOne = PendingIntent.getActivity(context, 0, intentOne, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }


}
