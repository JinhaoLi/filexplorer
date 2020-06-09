package com.jil.filexplorer.activity;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jil.filexplorer.bean.SettingItem;
import com.jil.filexplorer.api.SettingParam;
import com.jil.filexplorer.R;
import com.jil.filexplorer.adapter.SupperAdapter;
import com.jil.filexplorer.ui.MyItemDecoration;
import com.jil.filexplorer.ui.SimpleDialog;
//import com.jil.filexplorer.utils.EmailHelper;
import com.jil.filexplorer.utils.FileUtils;
import com.jil.filexplorer.utils.ToastUtils;

import java.util.ArrayList;

import static com.jil.filexplorer.api.SettingParam.*;
import static com.jil.filexplorer.utils.ConstantUtils.MB;
import static com.jil.filexplorer.utils.FileUtils.*;
import static com.jil.filexplorer.utils.PackageInfoManager.getVersionName;

/**
 * 设置
 */
public class SettingActivity extends AppCompatActivity {

    private SupperAdapter<SettingItem> supperAdapter;
    private ArrayList<SettingItem> itemArrayList;
    private RecyclerView settingList;
    float cacheSize;
    SettingItem clearCache;
    String versionName;


    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 28) {
                supperAdapter.notifyDataSetChanged();
            }
        }
    };

    Runnable deleteCacheThread = new Runnable() {
        @Override
        public void run() {
            deleteDirectory(SettingActivity.this.getCacheDir().getPath());
            countCacheSize.run();
        }
    };

    Runnable countCacheSize = new Runnable() {//计算缓存大小
        @Override
        public void run() {
            cacheSize = (float) FileUtils.getLength(SettingActivity.this.getCacheDir()) / MB;
            if (clearCache == null) {
                clearCache = new SettingItem("清除缓存", "当前缓存大小为" + stay4Number(cacheSize) + "MB", 5151) {
                    @Override
                    public void click(View v) {
                        new Thread(deleteCacheThread).start();
                    }
                };
            } else {
                clearCache.setDescription("当前缓存大小为" + stay4Number(cacheSize) + "MB");
            }
            if (!itemArrayList.contains(clearCache))
                itemArrayList.add(clearCache);

            updateUi();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settting_layout);
        if (NavUtils.getParentActivityName(SettingActivity.this) != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        versionName = getVersionName(this);
        itemArrayList=new ArrayList<>();
        createSettingItem();
        settingList = findViewById(R.id.set_list);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        supperAdapter = new SupperAdapter<SettingItem>(itemArrayList, this) {
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
                } else {
                    holder.getView(R.id.textView).setVisibility(View.GONE);
                }
                final boolean isSwitch = data.getItemLayout() == R.layout.setting_item_switch_layout;
                final View cuView = holder.getView(R.id.textView2);
                if (isSwitch) {
                    Switch sw = (Switch) cuView;
                    sw.setChecked(data.isSwitchOpen());
                }
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isSwitch) {
                            Switch sw = (Switch) cuView;
                            sw.setChecked(!sw.isChecked());
                            data.click(sw);
                        } else {
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
        settingList.setAdapter(supperAdapter);
        settingList.setLayoutManager(llm);
    }

    private void createSettingItem() {
        SettingItem item =new SettingItem("测试项目","",false,515){
            @Override
            public void click(View v) {
                CompoundButton s = (CompoundButton) v;
                setSwitchOpen(s.isChecked());
            }
        };
        SettingItem recycleBin = new SettingItem("回收站", "开启回收站之后文件不会被真正地删除", (SettingParam.RecycleBin > 0), 5051) {
            @Override
            public void click(View v) {
                CompoundButton s = (CompoundButton) v;
                boolean check = s.isChecked();
                setSwitchOpen(s.isChecked());
                int i = check ? 1 : -1;
                saveInt(SettingActivity.this, "RecycleBin", i);
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
                saveInt(SettingActivity.this, "ImageCacheSwitch", i);
                setImageCacheSwitch(check ? 1 : -1);
            }
        };
        SettingItem smallViewSwitch = new SettingItem("显示缩略图", "开启后可以显示视频或图片的预览图标", (SettingParam.SmallViewSwitch > 0), 4485) {
            @Override
            public void click(View v) {
                CompoundButton s = (CompoundButton) v;
                boolean check = s.isChecked();
                setSwitchOpen(s.isChecked());
                int i = check ? 1 : -1;
                saveInt(SettingActivity.this, "SmallViewSwitch", i);
                setSmallViewSwitch(check ? 1 : -1);
            }
        };
        SettingItem showHide =new SettingItem("显示隐藏文件","显示以.开头的文件", (ShowHide>0),1468) {
            @Override
            public void click(View v) {
                CompoundButton s = (CompoundButton) v;
                boolean check = s.isChecked();
                setSwitchOpen(s.isChecked());
                int i = check ? 1 : -1;
                saveInt(SettingActivity.this, "ShowHide", i);
                setShowHide(check ? 1 : -1);
            }
        };
        SettingItem donations =new SettingItem("支持开发者","开发者正在为生计奔波劳累◑﹏◐",1552) {
            @Override
            public void click(View v) {
                SimpleDialog donationsAlipayPic =new SimpleDialog(SettingActivity.this,R.layout.dialog_detail_pic_layout,"打钱给开发者") {
                    @Override
                    public void queryButtonClick(View v) {
                        ToastUtils.showToast(v.getContext(),"开发者支付宝13809761134",1000);
                    }

                    @Override
                    public void customView() {
                        ImageView pic =findViewById(R.id.imageView10);
                        Glide.with(SettingActivity.this).load(R.mipmap.alipay).into(pic);
                    }
                };
                donationsAlipayPic.showAndSet(R.drawable.ic_payment_black_24dp);

            }
        };
        SettingItem testMode =new SettingItem("测试模式","测试模式下不会进行真正的文件操作",(SettingParam.TestModeSwitch>0),9595) {
            @Override
            public void click(View v) {
                CompoundButton s = (CompoundButton) v;
                boolean check = s.isChecked();
                setSwitchOpen(s.isChecked());
                int i = check ? 1 : -1;
                saveInt(SettingActivity.this, "TestModeSwitch", i);
                setTestModeSwitch(check ? 1 : -1);
            }
        };

        itemArrayList.add(item);
        itemArrayList.add(recycleBin);
        itemArrayList.add(imageCache);
        itemArrayList.add(smallViewSwitch);
        itemArrayList.add(showHide);
        itemArrayList.add(donations);
        itemArrayList.add(testMode);

        new Thread(countCacheSize).start();
    }

    private void updateUi(){
        Message me = new Message();
        me.what = 28;
        handler.sendMessage(me);
    }
}
