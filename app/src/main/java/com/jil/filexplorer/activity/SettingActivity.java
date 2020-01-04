package com.jil.filexplorer.activity;


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
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jil.filexplorer.api.ActivityManager;
import com.jil.filexplorer.api.SettingItem;
import com.jil.filexplorer.api.SettingParam;
import com.jil.filexplorer.R;
import com.jil.filexplorer.adapter.SupperAdapter;
import com.jil.filexplorer.ui.MyItemDecoration;
import com.jil.filexplorer.ui.SimpleDialog;
import com.jil.filexplorer.utils.EmailHelper;
import com.jil.filexplorer.utils.FileUtils;
import com.jil.filexplorer.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;

import static com.jil.filexplorer.api.SettingParam.readSharedPreferences;
import static com.jil.filexplorer.api.SettingParam.saveSharedPreferences;
import static com.jil.filexplorer.api.SettingParam.setImageCacheSwitch;
import static com.jil.filexplorer.api.SettingParam.setRecycleBin;
import static com.jil.filexplorer.api.SettingParam.setSmallViewSwitch;
import static com.jil.filexplorer.utils.ConstantUtils.BULE_COLOR;
import static com.jil.filexplorer.utils.ConstantUtils.DARK_COLOR;
import static com.jil.filexplorer.utils.ConstantUtils.MB;
import static com.jil.filexplorer.utils.DialogUtils.showAlertDialog;
import static com.jil.filexplorer.utils.FileUtils.deleteDirectory;
import static com.jil.filexplorer.utils.FileUtils.installApk;
import static com.jil.filexplorer.utils.FileUtils.stayFrieNumber;
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
    private ActivityManager activityManager;
    SettingItem updateItem;
    javax.mail.Message updateMessage;
    String versionName;
    EmailHelper up;
    Thread downLoadFile;


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

    Runnable checkUpdate = new Runnable() {//检查更新
        @Override
        public void run() {

            if (up == null)
                up = new EmailHelper();
            String s=up.getSubject();
            if (s != null) {
                if (s.contains("=")) {
                    String ver = s.substring(s.lastIndexOf("=") + 1);
                    if (versionName.equals(ver)) {
                        updateItem.setDescription("已经是最新版本" + ver);
                    } else {
                        updateItem.setDescription("最新版本" + ver + "，点击查看");
                        updateItem.setSwitchOpen(true);
                    }
                }
            }
            updateUi();
        }
    };

    Runnable downloadUpdate = new Runnable() {
        @Override
        public void run() {
            File updateApk = null;
            try {
                updateApk = up.saveAttachMent(SettingActivity.this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (updateApk != null && updateApk.exists())
                installApk(updateApk.getPath(), SettingActivity.this);
            else
                ToastUtils.showToast(SettingActivity.this, "升级下载失败！", 1000);
        }
    };


    Runnable countCacheSize = new Runnable() {//计算缓存大小
        @Override
        public void run() {
            cacheSize = (float) FileUtils.getLength(SettingActivity.this.getCacheDir()) / MB;
            if (clearCache == null) {
                clearCache = new SettingItem("清除缓存", "当前缓存大小为" + stayFrieNumber(cacheSize) + "MB", 5151) {
                    @Override
                    public void click(View v) {
                        new Thread(deleteCacheThread).start();
                    }
                };
            } else {
                clearCache.setDescription("当前缓存大小为" + stayFrieNumber(cacheSize) + "MB");
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
        activityManager = ActivityManager.getInstance();
        itemArrayList = new ArrayList<>();
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
                    //holder.getView(R.id.textView).setVisibility(View.VISIBLE);
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

        SettingItem smallViewSwitch = new SettingItem("显示缩略图", "开启后可以显示视频或图片的预览图标", (SettingParam.SmallViewSwitch > 0), 4485) {
            @Override
            public void click(View v) {
                CompoundButton s = (CompoundButton) v;
                boolean check = s.isChecked();
                setSwitchOpen(s.isChecked());
                int i = check ? 1 : -1;
                saveSharedPreferences(SettingActivity.this, "SmallViewSwitch", i);
                setSmallViewSwitch(check ? 1 : -1);
            }
        };

        SettingItem email = new SettingItem("发送日志", "发送日志给开发者帮助开发者发现bug", 851) {
            @Override
            public void click(View v) {
                EmailHelper.sendEmail();
            }
        };

        SettingItem donations =new SettingItem("支持开发者","开发者正在为生计奔波劳累◑﹏◐",1552) {
            @Override
            public void click(View v) {
                SimpleDialog donationsAlipayPic =new SimpleDialog(SettingActivity.this,R.layout.dialog_detail_pic_layout,"打钱给开发者") {
                    @Override
                    public void queryButtonClick(View v) {
                              ToastUtils.showToast(SettingActivity.this,"开发者支付宝13809761134",5000); 
                    }

                    @Override
                    public void customView() {
                        super.customView();
                        ImageView pic =findViewById(R.id.imageView10);
                        Glide.with(getContext()).load(R.mipmap.alipay).into(pic);
                    }
                };
                donationsAlipayPic.showAndSet(R.drawable.ic_payment_black_24dp);

            }
        };

        updateItem = new SettingItem("检查更新", "当前版本" + versionName + "，点击检查更新", 4501) {
            @Override
            public void click(View v) {
                if (isSwitchOpen()) {
                    SimpleDialog updateDialog =new SimpleDialog(SettingActivity.this,R.layout.dialog_detail_info_layout,"升级信息") {
                        TextView info;
                        @Override
                        public void queryButtonClick(View v) {
                            new Thread(downloadUpdate).start();
                        }

                        @Override
                        public void customView() {
                            super.customView();
                            info=findViewById(R.id.textView13);
                            info.setText(up.getUpdateInfo());
                            query.setText("下载");
                        }
                    };
                    updateDialog.showAndSet(R.drawable.ic_arrow_upward_black_24dp);

                } else {
                    setDescription("正在检查更新，请稍侯...");
                    updateUi();
                    new Thread(checkUpdate).start();
                }
            }

            @Override
            public String getDescription() {

                return super.getDescription();
            }
        };

        itemArrayList.add(theme);
        itemArrayList.add(recycleBin);
        itemArrayList.add(imageCache);
        itemArrayList.add(smallViewSwitch);
        itemArrayList.add(email);
        itemArrayList.add(updateItem);
        itemArrayList.add(donations);
        new Thread(checkUpdate).start();
        new Thread(countCacheSize).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!activityManager.isLive(MainActivity.class)) {
            Intent i = new Intent(SettingActivity.this, MainActivity.class);
            startActivity(i);
        }
    }

    private void updateUi(){
        Message me = new Message();
        me.what = 28;
        handler.sendMessage(me);
    }
}
