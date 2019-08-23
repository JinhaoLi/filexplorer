package com.jil.filexplorer.Activity;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jil.filexplorer.Api.ActivityManager;
import com.jil.filexplorer.Api.SettingItem;
import com.jil.filexplorer.Api.SettingParam;
import com.jil.filexplorer.R;
import com.jil.filexplorer.adapter.SurperAdapter;
import com.jil.filexplorer.ui.MyItemDecoration;
import com.jil.filexplorer.utils.FileUtils;

import java.util.ArrayList;

import static com.jil.filexplorer.Api.SettingParam.readSharedPreferences;
import static com.jil.filexplorer.Api.SettingParam.saveSharedPreferences;
import static com.jil.filexplorer.Api.SettingParam.setImageCacheSwitch;
import static com.jil.filexplorer.Api.SettingParam.setRecycleBin;
import static com.jil.filexplorer.utils.ConstantUtils.BULE_COLOR;
import static com.jil.filexplorer.utils.ConstantUtils.DARK_COLOR;
import static com.jil.filexplorer.utils.ConstantUtils.MB;
import static com.jil.filexplorer.utils.DialogUtils.showAlertDialog;
import static com.jil.filexplorer.utils.FileUtils.deleteDirectory;
import static com.jil.filexplorer.utils.FileUtils.stayFrieNumber;

public class SettingActivity extends AppCompatActivity {

    private SurperAdapter<SettingItem> surperAdapter;
    private ArrayList<SettingItem> itemArrayList;
    private RecyclerView settingList;
    float cacheSize;
    SettingItem clearCache;
    private ActivityManager activityManager;
    @SuppressLint("HandlerLeak")
    Handler handler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==28){
                surperAdapter.notifyDataSetChanged();
            }
        }
    };
    Runnable deleteCacheThread =new Runnable() {
        @Override
        public void run() {
            deleteDirectory(SettingActivity.this.getCacheDir().getPath());
            countCacheSize.run();
        }
    };
    Runnable countCacheSize =new Runnable() {
        @Override
        public void run() {
            cacheSize =(float)FileUtils.getLength(SettingActivity.this.getCacheDir())/MB;
            if(clearCache==null){
                clearCache = new SettingItem("清除缓存", "当前缓存大小为"+ stayFrieNumber(cacheSize)+"MB",5151) {
                    @Override
                    public void click(View v) {
                        deleteCacheThread.run();
                    }
                };
            }else {
                clearCache.setDescription("当前缓存大小为"+ stayFrieNumber(cacheSize)+"MB");
            }
            if(!itemArrayList.contains(clearCache))
                itemArrayList.add(clearCache);

            Message message =new Message();
            message.what= 28;
            handler.sendMessage(message);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settting_layout);
        activityManager = ActivityManager.getInstance();
        itemArrayList = new ArrayList<>();
        createSettingItem();
        settingList = findViewById(R.id.set_list);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        surperAdapter = new SurperAdapter<SettingItem>(itemArrayList, this) {
            @Override
            public int getLayoutId(int viewType, SettingItem data) {
                if (data.getItemLayout() == 0) {
                    return R.layout.setting_item_normal_layout;
                } else {
                    return data.getItemLayout();
                }
            }

            @Override
            public void convert(final VH holder, final SettingItem data, int position, Context mContext) {
                holder.setText(R.id.textView2, data.getName());
                if (data.getDescription() != null && !data.getDescription().equals("")) {
                    holder.setText(R.id.textView, data.getDescription());
                    //holder.getView(R.id.textView).setVisibility(View.VISIBLE);
                } else {
                    holder.getView(R.id.textView).setVisibility(View.GONE);
                }
                final boolean isSwitch =data.getItemLayout() == R.layout.setting_item_switch_layout;
                final View cuView = holder.getView(R.id.textView2);
                if(isSwitch){
                    Switch sw =(Switch)cuView;
                    sw.setChecked(data.isSwitchOpen());
                }
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(isSwitch){
                            Switch sw =(Switch)cuView;
                            sw.setChecked(!sw.isChecked());
                            data.click(sw);
                        }else {
                            data.click(cuView);
                        }

                    }
                });

            }

            @Override
            public VH onCreateViewHolder(ViewGroup viewGroup, int i) {
                return super.onCreateViewHolder(viewGroup, i);
            }
        };
        //设置分隔线
        settingList.addItemDecoration(new MyItemDecoration(this, 1));
        settingList.setAdapter(surperAdapter);
        settingList.setLayoutManager(llm);
    }

    private void createSettingItem() {

        SettingItem theme = new SettingItem("主题", 5151) {
            @Override
            public void click(View v) {
                int i = 0;
                if (SettingParam.Theme != R.style.AppTheme) {
                    i = 1;
                }
                String[] item = new String[]{"亮色", "暗色"};
                AlertDialog.Builder builder = showAlertDialog(SettingActivity.this, "主题选择");
                builder.setSingleChoiceItems(item, i, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            saveSharedPreferences(SettingActivity.this, "theme", R.style.AppTheme);
                            saveSharedPreferences(SettingActivity.this, "main_color", BULE_COLOR);
                        } else if (which == 1) {
                            saveSharedPreferences(SettingActivity.this, "theme", R.style.MyThemeGray);
                            saveSharedPreferences(SettingActivity.this, "main_color", DARK_COLOR);
                        }
                        readSharedPreferences(SettingActivity.this);
                        activityManager.removeActivity(MainActivity.class);
                        //activityManager.reCreatActivity(MainActivity.class);
                    }
                }).create().show();
            }
        };
        SettingItem recycleBin = new SettingItem("回收站", "开启回收站之后文件不会被真正地删除", (SettingParam.RecycleBin > 0), 5051) {
            @Override
            public void click(View v) {
                CompoundButton s = (CompoundButton) v;
                boolean check = s.isChecked();
                setSwitchOpen(s.isChecked());
                int i = check ? 1 : -1;
                saveSharedPreferences(SettingActivity.this, "RecycleBin", i);
                setRecycleBin(check ? 1 : -1);
            }
        };
        SettingItem imageCache = new SettingItem("图标缓存", "开启图标缓存之后可以加图标载入速度", (SettingParam.ImageCacheSwitch > 0), 4615) {
            @Override
            public void click(View v) {
                CompoundButton s = (CompoundButton) v;
                boolean check = s.isChecked();
                setSwitchOpen(s.isChecked());
                int i = check ? 1 : -1;
                saveSharedPreferences(SettingActivity.this, "ImageCacheSwitch", i);
                setImageCacheSwitch(check ? 1 : -1);
            }
        };
        itemArrayList.add(theme);
        itemArrayList.add(recycleBin);
        itemArrayList.add(imageCache);
        countCacheSize.run();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!activityManager.isLive(MainActivity.class)) {
            Intent i = new Intent(SettingActivity.this, MainActivity.class);
            startActivity(i);
        }
    }
}
