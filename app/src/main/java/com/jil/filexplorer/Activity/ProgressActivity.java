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

import com.jil.filexplorer.Api.ProgressMessage;
import com.jil.filexplorer.R;

public class ProgressActivity extends AppCompatActivity implements AfterIntentService.UpdateUI{
    @SuppressLint("HandlerLeak")
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what>0) progressMessage= (ProgressMessage) msg.obj;
            if(msg.what==-1){
                onActionFinish.OnRefresh();
                finish();
            }
            if(progressMessage!=null){
                progressBar.setProgress(progressMessage.getProggress());
                mTitle.setText(progressMessage.getTitle());
                mMessage.setText(Html.fromHtml(progressMessage.getMessage()));
                smallTitle.setText(progressMessage.getTitle());
                speedText.setText(progressMessage.getSpeed());
                projectName.setText(progressMessage.getNowProjectName());
                remainTime.setText(progressMessage.getReMainTime());
                remainCount.setText(progressMessage.getReMainCount());
            }

        }
    };


    private ProgressMessage progressMessage;
    private ProgressBar progressBar;
    private ImageView close,smaller;
    AfterIntentService.UpdateUI updateUI;
    private static OnActionFinish onActionFinish;
    private TextView mTitle,mMessage,smallTitle,speedText,projectName,remainTime,remainCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AfterIntentService.setUpdateUI(this);
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
                Intent i =new Intent(ProgressActivity.this,AfterIntentService.class);
                stopService(i);
                finish();
            }
        });
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
    }

    @Override
    public void updateUI(Message message) {
        handler.sendMessage(message);
    }
}
