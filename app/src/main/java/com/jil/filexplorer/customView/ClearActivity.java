package com.jil.filexplorer.customView;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public abstract class ClearActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initAction();
    }

    protected abstract void initView();
    protected abstract void initAction();
}
