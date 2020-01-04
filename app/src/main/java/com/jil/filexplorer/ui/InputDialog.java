package com.jil.filexplorer.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.jil.filexplorer.R;

import java.util.ArrayList;

public abstract class InputDialog extends SimpleDialog implements View.OnClickListener {
    protected EditText nameInput;//输入框

    public InputDialog(Context context, int layoutRes, String title) {
        super(context,layoutRes,title);
        nameInput =findViewById(R.id.name_input);
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
        showAndSet(-1);
        nameInput.setText(name);

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
