package com.jil.filexplorer.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jil.filexplorer.Api.ProgressMessage;
import com.jil.filexplorer.R;


public class CopyProgressDialog extends AlertDialog implements View.OnClickListener{
    private View mView;
    private LayoutInflater mLayoutInflater;
    private ProgressBar progressBar;
    private TextView mTitleText,mMessageText,speedText,chliaTitleText;
    private TextView mFileNameText,mTimeText,reMainingText;
    private ImageView dismissButton,closeButton;


    protected CopyProgressDialog(Context context) {
        super(context);
    }

    public CopyProgressDialog(Context context,int layoutRes) {
        super(context);
        this.mLayoutInflater =LayoutInflater.from(context);
        mView =mLayoutInflater.inflate(layoutRes, null);
        mTitleText =mView.findViewById(R.id.textView8);
        mMessageText=mView.findViewById(R.id.textView10);
        speedText =mView.findViewById(R.id.textView17);
        chliaTitleText =mView.findViewById(R.id.textView14);
        mFileNameText=mView.findViewById(R.id.textView9);
        mTimeText=mView.findViewById(R.id.textView11);
        reMainingText=mView.findViewById(R.id.textView12);
        dismissButton=mView.findViewById(R.id.imageView11);
        closeButton=mView.findViewById(R.id.imageView8);
        progressBar=mView.findViewById(R.id.progressBar);
        closeButton.setOnClickListener(this);
        dismissButton.setOnClickListener(this);
        setView(mView);
    }

    protected CopyProgressDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }


    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        mTitleText.setText(title);
    }

   public void setParame(ProgressMessage message){
        setTitle(message.getTitle());
        mMessageText.setText(message.getMessage());
        speedText.setText(message.getSpeed());
        reMainingText.setText(message.getReMainCount());
        mTimeText.setText(message.getReMainTime());
        mFileNameText.setText(message.getCopyOverCount());
        setProgress(message.getProggress());

   }



    @Override
    public void setIcon(int resId) {
        super.setIcon(resId);
    }


    @Override
    public void show() {
        super.show();
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imageView8:
                dismiss();
                break;
            case R.id.imageView11:
                dismiss();
                break;
        }
    }

    public void setProgress(int value){
        progressBar.setProgress(value);
    }
}
