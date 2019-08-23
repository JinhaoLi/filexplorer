package com.jil.filexplorer.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.jil.filexplorer.Api.FileOperation;
import com.jil.filexplorer.Api.ProgressChangeListener;
import com.jil.filexplorer.Api.ProgressMessage;
import com.jil.filexplorer.R;

/**
 * 接收AfterIntentService进度并显示
 */
public class ProgressActivity extends AppCompatActivity implements ProgressChangeListener {
    @SuppressLint("HandlerLeak")
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg!=null)
                progressMessage= (ProgressMessage) msg.obj;
            if(progressMessage!=null){
                if(progressMessage.getProgress()==100){
                    if(onActionFinish!=null)onActionFinish.OnRefresh();
                    finish();
                }else {
                    progressBar.setProgress(progressMessage.getProgress());
                    mTitle.setText(progressMessage.getTitle());
                    mMessage.setText(Html.fromHtml(progressMessage.getMessage()));
                    smallTitle.setText(progressMessage.getTitle());
                    speedText.setText(progressMessage.getSpeed());
                    projectName.setText(progressMessage.getNowProjectName());
                    remainTime.setText(progressMessage.getReMainTime());
                    remainCount.setText(progressMessage.getReMainCount());
                }
            }
        }
    };

    private ProgressMessage progressMessage;
    private ProgressBar progressBar;
    private ImageView close,smaller;
    private static OnActionFinish onActionFinish;
    private TextView mTitle,mMessage,smallTitle,speedText,projectName,remainTime,remainCount;
    FileOperation fileOperation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        fileOperation=FileOperation.getInstance();
        if(fileOperation!=null){
            fileOperation.setProgressChangeListener(this);
            fileOperation.pushProgressMsg();
        }else {
            finish();
        }
        super.onCreate(savedInstanceState);
        requestWindowFeature( Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.progress_dialog_layout);
        smaller=findViewById(R.id.imageView11);
        smaller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        close=findViewById(R.id.imageView8);
        progressBar= findViewById(R.id.progressBar);
        mTitle=findViewById(R.id.textView8);
        mMessage =findViewById(R.id.textView10);
        smallTitle=findViewById(R.id.textView14);
        speedText =findViewById(R.id.textView17);
        projectName=findViewById(R.id.textView9);
        remainTime= findViewById(R.id.textView11);
        remainCount =findViewById(R.id.textView12);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileOperation.stopAction();
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


    public interface OnActionFinish{
        void OnRefresh();
    }

    public static void setOnActionFinish(OnActionFinish onAction){
        onActionFinish=onAction;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onActionFinish=null;
    }
}
