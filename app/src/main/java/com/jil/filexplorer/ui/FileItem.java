package com.jil.filexplorer.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.CompoundButton;

import androidx.annotation.Nullable;


public class FileItem extends CompoundButton implements Checkable {


    public FileItem(Context context) {
        super(context);
    }

    public FileItem(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FileItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setChecked(boolean checked) {

    }

    @Override
    public boolean isChecked() {
        return false;
    }

    @Override
    public void toggle() {
        super.toggle();
    }
}
