package com.jil.filexplorer.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.jil.filexplorer.R;

import java.util.ArrayList;

public abstract class NewNameDialog extends Dialog implements View.OnClickListener {
    protected TextView titleText;
    protected EditText nameInput;
    protected Button cancle,query;

    /**
     *
     * @param context
     * @param layoutRes 布局中必须包含id==button 取消&&id==button3 确认的Button
     */
    public NewNameDialog(Context context,int layoutRes, String title) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(layoutRes);
        nameInput =findViewById(R.id.name_input);
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
     * @param name
     */
    public void showAndSetName(String name){
        nameInput.setText(name);
        cancle.setOnClickListener(this);
        query.setOnClickListener(this);
        show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button3:
                String nameInputStr=nameInput.getText().toString();
                queryButtonClick(v,nameInputStr);
                break;
        }
        dismiss();
    }

    public abstract void queryButtonClick(View v,String nameInputStr);

}
