package com.jil.filexplorer.Api;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.jil.filexplorer.Activity.MainActivity;
import com.jil.filexplorer.R;
import com.jil.filexplorer.adapter.SettingItemAdapter;
import com.jil.filexplorer.ui.CustomFragment;
import com.jil.filexplorer.ui.SimpleDialog;
import com.jil.filexplorer.utils.EmailHelper;
import com.jil.filexplorer.utils.FileUtils;
import com.jil.filexplorer.utils.LogUtils;
import com.jil.filexplorer.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;

import static com.jil.filexplorer.Api.SettingParam.readSharedPreferences;
import static com.jil.filexplorer.Api.SettingParam.saveSharedPreferences;
import static com.jil.filexplorer.Api.SettingParam.setImageCacheSwitch;
import static com.jil.filexplorer.Api.SettingParam.setRecycleBin;
import static com.jil.filexplorer.Api.SettingParam.setSmallViewSwitch;
import static com.jil.filexplorer.utils.ConstantUtils.BULE_COLOR;
import static com.jil.filexplorer.utils.ConstantUtils.DARK_COLOR;
import static com.jil.filexplorer.utils.ConstantUtils.MB;
import static com.jil.filexplorer.utils.DialogUtils.showAlertDialog;
import static com.jil.filexplorer.utils.FileUtils.deleteDirectory;
import static com.jil.filexplorer.utils.FileUtils.installApk;
import static com.jil.filexplorer.utils.FileUtils.stayFrieNumber;
import static com.jil.filexplorer.utils.PackageInfoManager.getVersionName;

@SuppressLint("ValidFragment")
public class SettingFragment extends CustomFragment<SettingItem> {
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
                tListAdapter.notifyDataSetChanged();
            }
        }
    };

    @SuppressLint("ValidFragment")
    public SettingFragment(MainActivity mainActivity, String path) {
        super(mainActivity,path);
    }

    @Override
    protected void initAction() {
        versionName=getVersionName(getContext());
        enlargeIcon();
        load(path,false);
    }

    @Override
    protected void deleteItem(int adapterPosition) {

    }

    @Override
    protected boolean initDates(String filePath, boolean isBack) {
        return false;
    }

    @Override
    public void load(String filePath, boolean isBack) {
        ts =new ArrayList<>();
        createSettingItem();
        makeLinerLayout();

    }

    @Override
    public void sortReFresh(int sortType) {

    }

    @Override
    public int getSortType() {
        return 0;
    }

    @Override
    public boolean isAllSelect() {
        return false;
    }

    @Override
    public int refreshUnderBar() {
        return 0;
    }

    @Override
    public int getFileInfosSize() {
        return ts.size();
    }

    @Override
    public void setUnderBarMsg(String underMsg) {
        this.underBarMsg =underMsg;
    }

    @Override
    public void makeGridLayout(int spanCount, int layoutRes) {
//        if(linearLayoutManager!=null){
//            saveSharedPreferences(mMainActivity,"Column",spanCount);
//        }
//        linearLayoutManager = new GridLayoutManager(mMainActivity, spanCount);
//        tListAdapter = new SettingItemAdapter(ts, this, mMainActivity, layoutRes);
//        tList.setAdapter(tListAdapter);
//        tList.setLayoutManager(linearLayoutManager);
    }

    @Override
    public void makeLinerLayout() {
        if(linearLayoutManager!=null){
            saveSharedPreferences(mMainActivity,"Column",1);
        }
        linearLayoutManager = new LinearLayoutManager(mMainActivity);
        tListAdapter = new SettingItemAdapter(ts, this, mMainActivity, R.layout.file_list_item_layout);
        try{
            tList.setAdapter(tListAdapter);
            tList.setLayoutManager(linearLayoutManager);
        }catch (Exception e){
            LogUtils.e(e.getMessage(),e.getMessage()+getString(R.string.eat_err));
        }
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
                AlertDialog.Builder builder = showAlertDialog(mMainActivity, "主题选择");
                builder.setSingleChoiceItems(item, i, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            saveSharedPreferences(mMainActivity, "theme", R.style.AppTheme);
                            saveSharedPreferences(mMainActivity, "main_color", BULE_COLOR);
                        } else if (which == 1) {
                            saveSharedPreferences(mMainActivity, "theme", R.style.MyThemeGray);
                            saveSharedPreferences(mMainActivity, "main_color", DARK_COLOR);
                        }
                        readSharedPreferences(mMainActivity);
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
                saveSharedPreferences(mMainActivity, "RecycleBin", i);
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
                saveSharedPreferences(mMainActivity, "ImageCacheSwitch", i);
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
                saveSharedPreferences(mMainActivity, "SmallViewSwitch", i);
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
                SimpleDialog donationsAlipayPic =new SimpleDialog(mMainActivity,R.layout.dialog_detail_pic_layout,"打钱给开发者") {
                    @Override
                    public void queryButtonClick(View v) {
                        mMainActivity.showToast("开发者支付宝13809761134");
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

        updateItem = new SettingItem("检查更新", "当前版本" + getVersionName(mMainActivity)+ "，点击检查更新", 4501) {
            @Override
            public void click(View v) {
                if (isSwitchOpen()) {
                    SimpleDialog updateDialog =new SimpleDialog(mMainActivity,R.layout.dialog_detail_info_layout,"升级信息") {
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
                    tListAdapter.notifyDataSetChanged();
                    new Thread(checkUpdate).start();
                }
            }

            @Override
            public String getDescription() {

                return super.getDescription();
            }
        };

        ts.add(theme);
        ts.add(recycleBin);
        ts.add(imageCache);
        ts.add(smallViewSwitch);
        ts.add(email);
        ts.add(updateItem);
        ts.add(donations);
        new Thread(checkUpdate).start();
        new Thread(countCacheSize).start();
    }


    Runnable deleteCacheThread = new Runnable() {
        @Override
        public void run() {
            deleteDirectory(mMainActivity.getCacheDir().getPath());
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
                updateApk = up.saveAttachMent(mMainActivity);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (updateApk != null && updateApk.exists())
                installApk(updateApk.getPath(), mMainActivity);
            else
                ToastUtils.showToast(mMainActivity, "升级下载失败！", 1000);
        }
    };


    Runnable countCacheSize = new Runnable() {//计算缓存大小
        @Override
        public void run() {
            cacheSize = (float) FileUtils.getLength(mMainActivity.getCacheDir()) / MB;
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
            if (!ts.contains(clearCache))
                ts.add(clearCache);

            updateUi();
        }
    };

    private void updateUi(){
        Message me = new Message();
        me.what = 28;
        handler.sendMessage(me);
    }


}
