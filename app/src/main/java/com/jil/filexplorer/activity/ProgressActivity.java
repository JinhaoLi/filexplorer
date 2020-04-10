package com.jil.filexplorer.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.*;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.jil.filexplorer.api.*;
import com.jil.filexplorer.R;
import com.jil.filexplorer.bean.ProgressMessage;
import com.jil.filexplorer.utils.LogUtils;

/**
 * 进度并显示
 */
public class ProgressActivity extends AppCompatActivity implements ProgressChangeListener {
    @SuppressLint("HandlerLeak")
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ProgressMessage progressMessage = (ProgressMessage) msg.obj;
            if(progressMessage !=null){

                progressBar.setProgress(progressMessage.getProgress());
                mTitle.setText(progressMessage.getTitle(progressBar.getProgress()));
                mMessage.setText(Html.fromHtml(progressMessage.getMessage()));
                smallTitle.setText(progressMessage.getTitle(progressBar.getProgress()));
                speedText.setText(progressMessage.getSpeedMessage());
                projectName.setText(progressMessage.getNowProjectName());
                remainTime.setText(progressMessage.getReMainTime());
                remainCount.setText(progressMessage.getReMainCount());
                if(progressBar.getProgress()==100){
                    finish.setText("关闭");
                    finish.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });
                }
            }
        }
    };

    private ProgressBar progressBar;
    private Button finish;
    private TextView mTitle,mMessage,smallTitle,speedText,projectName,remainTime,remainCount;
    private FileOperation fileOperation;

    public static void start(Context context, long id) {
        Intent intent =new Intent(context,ProgressActivity.class);
        intent.putExtra("FileOperation.id",id);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature( Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.progress_dialog_layout);

        Intent intent=getIntent();
        final long id =intent.getLongExtra("FileOperation.id",888888888888L);
        LogUtils.d(getClass().getName(),"fileOperation.id=="+id);
        if(id!=888888888888L){
            fileOperation=FileOperation.getInstance(id);
            if(fileOperation!=null){
                fileOperation.setProgressChangeListener(this);
                fileOperation.pushProgressMsg();
            } else {
                LogUtils.d(getClass().getName(),"fileOperation==null");
                finish();
            }
        }else {
            finish();
        }

        ImageView ico = findViewById(R.id.imageView7);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ico.setImageIcon(Icon.createWithResource(this,R.mipmap.copy_move_progress_ico));
        }else {
            Glide.with(this).load(R.mipmap.copy_move_progress_ico).into(ico);
        }
        finish =findViewById(R.id.button3);
        finish.setText("取消任务");
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileOperation.stopAction();
                FileOperation.removeMission(fileOperation.getId());
                finish();
            }
        });
        Button hide = findViewById(R.id.button);
        hide.setText("隐藏");
        progressBar= findViewById(R.id.progressBar);
        mTitle=findViewById(R.id.textView8);
        mMessage =findViewById(R.id.textView10);
        smallTitle=findViewById(R.id.textView14);
        speedText =findViewById(R.id.textView17);
        projectName=findViewById(R.id.textView9);
        remainTime= findViewById(R.id.textView11);
        remainCount =findViewById(R.id.textView12);
        hide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //fileOperation.stopAction();
                finish();
            }
        });
    }

    @Override
    public void progressChang(ProgressMessage ProgressMessage) {
        Message msg =new Message();
        msg.obj=ProgressMessage;
        handler.sendMessage(msg);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        final long id =intent.getLongExtra("FileOperation.id",888888888888L);
        LogUtils.d(getClass().getName(),"fileOperation.id=="+id);
        fileOperation.setProgressChangeListener(null);
        if(id!=888888888888L){
            fileOperation=FileOperation.getInstance(id);
            if(fileOperation!=null){
                fileOperation.setProgressChangeListener(this);
                fileOperation.pushProgressMsg();
            } else {
                LogUtils.d(getClass().getName(),"fileOperation==null");
            }
        }
    }
}
