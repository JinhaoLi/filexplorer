package com.jil.filexplorer.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.jil.filexplorer.R;

public abstract class SimpleDialog extends Dialog implements View.OnClickListener{

    protected ImageView icon;
    protected TextView titleText;//标题
    protected Button cancle,query;//取消，确认按钮

    /**
     *
     * @param context
     * @param layoutRes 布局中必须包含
     *                  id==button 取消
     *                  id==button3 确认
     *                  id==imageView7 ico
     *                  id==textView8 标题
     */
    public SimpleDialog(Context context, int layoutRes, String title) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(layoutRes);
        icon =findViewById(R.id.imageView7);
        titleText = findViewById(R.id.textView8);
        cancle=findViewById(R.id.button);
        query=findViewById(R.id.button3);
        setTitle(title);
        customView();
    }

    @Override
    public void setTitle(@Nullable CharSequence title) {
        titleText.setText(title);
    }


    public void customView(){

    }

    /**
     * 输入框中输入默认值and show
     * @param icoRes
     */
    public void showAndSet(int icoRes){
        cancle.setOnClickListener(this);
        query.setOnClickListener(this);
        if(icoRes!=-1)
        Glide.with(getContext()).load(icoRes).into(icon);
        show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button3:
                queryButtonClick(v);
                break;
        }
        dismiss();
    }

    public abstract void queryButtonClick(View v);
}
