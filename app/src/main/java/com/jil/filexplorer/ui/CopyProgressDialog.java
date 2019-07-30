package com.jil.filexplorer.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jil.filexplorer.Api.DialogCloseClickListener;
import com.jil.filexplorer.Api.ProgressMessage;
import com.jil.filexplorer.R;

import static com.jil.filexplorer.utils.CopyFileUtils.stopCopyDir;
import static com.jil.filexplorer.utils.CopyFileUtils.stopCopyFile;

/**
 * 一个监测进度的dialog
 */
public class CopyProgressDialog extends AlertDialog implements View.OnClickListener{
    private View mView;
    private LayoutInflater mLayoutInflater;
    private ProgressBar progressBar;
    private TextView mTitleText,mMessageText,speedText,chliaTitleText;
    private TextView mFileNameText,mTimeText,reMainingText;
    private ImageView dismissButton,closeButton;
    private boolean copyDir;
    private DialogCloseClickListener dialogCloseClickListener;

    public void setDialogCloseClickListener(DialogCloseClickListener dialogCloseClickListener){
        this.dialogCloseClickListener=dialogCloseClickListener;
    }

    public CopyProgressDialog(Context context) {
        super(context);
        this.mLayoutInflater =LayoutInflater.from(context);
        mView =mLayoutInflater.inflate(R.layout.progress_dialog_layout, null);
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
        mTitleText.setText(title);
    }

   public void setParame(ProgressMessage message){
        setTitle(message.getTitle());
        mMessageText.setText(Html.fromHtml(message.getMessage()));
        speedText.setText(message.getSpeed());
        reMainingText.setText(message.getReMainCount());
        mTimeText.setText(message.getReMainTime());
        mFileNameText.setText(message.getCopyOverCount());
        setProgress(message.getProgress());
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
                dialogCloseClickListener.onCloseClick();
//                if(copyDir){
//                    stopCopyDir();
//                }else{
//                    stopCopyFile();
//                }
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

    /**
     * 剩余项目
     * @param s
     */
    public void setFileVolumeText(String s) {
        reMainingText.setText("剩余项目:"+s);
    }



    /**
     * 项目名称
     * @param oldPathName
     */
    public void setNameText(String oldPathName) {
        mFileNameText.setText("项目名称:"+oldPathName);
    }

    public boolean isShow() {
        return isShowing();
    }

    @Override
    public boolean isShowing() {
        return super.isShowing();
    }

    public void setCopyDir(boolean copyDir) {
        this.copyDir = copyDir;
    }
}
