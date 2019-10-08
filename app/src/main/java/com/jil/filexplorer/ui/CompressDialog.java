package com.jil.filexplorer.ui;

import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.jil.filexplorer.Activity.MainActivity;
import com.jil.filexplorer.R;

import net.lingala.zip4j.util.Zip4jConstants;

/**
 * @author JIL
 * 压缩窗口
 */
public class CompressDialog extends InputDialog {
    private EditText passInput;
    private Spinner typeChoose;
    private CheckBox seePass;
    private RadioGroup compressGrade;
    private MainActivity mainActivity;

    public CompressDialog(final MainActivity mainActivity,int layoutRes, String title) {
        super(mainActivity,layoutRes,title);
        this.mainActivity=mainActivity;
        nameInput =findViewById(R.id.name_input);
        passInput =findViewById(R.id.pass_input);
        typeChoose =findViewById(R.id.spinner2);
        seePass=findViewById(R.id.checkbox3);
        compressGrade=findViewById(R.id.raf);
        icon.setImageResource(R.drawable.ic_pages_black_24dp);
        seePass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    passInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }else {
                    passInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
        compressGrade.check(R.id.radioButton3);
    }

    @Override
    public void queryButtonClick(View v,String zipName) {
        String zipPass=passInput.getText().toString();
        String zipType=typeChoose.getSelectedItem().toString();
        int zipFast =compressGrade.getCheckedRadioButtonId();
        switch (zipFast){
            case R.id.radioButton:
                zipFast= Zip4jConstants.DEFLATE_LEVEL_ULTRA;
                break;
            case R.id.radioButton2:
                zipFast= Zip4jConstants.DEFLATE_LEVEL_FAST;
                break;
            case R.id.radioButton3:
                zipFast= Zip4jConstants.DEFLATE_LEVEL_NORMAL;
                break;
            case R.id.radioButton4:
                zipFast= Zip4jConstants.DEFLATE_LEVEL_FASTEST;
                break;
        }
        mainActivity.compressFileAction(zipName+zipType,zipPass,zipFast);
    }

    @Override
    public void queryButtonClick(View v) {

    }
}
