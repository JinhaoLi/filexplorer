package com.jil.filexplorer.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.jil.filexplorer.activity.MainActivity;
import com.jil.filexplorer.api.*;
import com.jil.filexplorer.R;
import com.jil.filexplorer.adapter.SupperAdapter;
import com.jil.filexplorer.utils.*;

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
import static com.jil.filexplorer.utils.FileUtils.stayFireNumber;
import static com.jil.filexplorer.utils.PackageInfoManager.getVersionName;

@SuppressLint("ValidFragment")
public class SettingFragment extends CustomFragment {
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
    private RecyclerView.Adapter tListAdapter;
    private String fragmentTitle;
    private ArrayList<SettingItem> ts;
    private String path;

    @SuppressLint("ValidFragment")
    public SettingFragment(MainActivity mainActivity, String path) {
        //super(mainActivity);
        this.path=path;

    }

    @Override
    protected void initAction() {
        fragmentTitle="设置";
        versionName=getVersionName(getContext());
        enlargeIcon();
        load(path,false);
        tList.setBackgroundColor(DARK_COLOR);
    }


    @Override
    public void load(String filePath, boolean isBack) {
        ts =new ArrayList<>();
        createSettingItem();
        makeLinerLayout();

    }

    @Override
    public String getUnderBarMsg() {
        return null;
    }


    @Override
    public void selectSomePosition(int startPosition, int endPosition) {

    }

    @Override
    public String getFragmentTitle() {
        return null;
    }

    @Override
    public void sortReFresh(int sortType) {

    }

    @Override
    public int getSortType() {
        return 0;
    }

    @Override
    public void addData(FileInfo fileInfoFromPath) {

    }


    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void refresh() {

    }

    @Override
    public boolean isAllSelect() {
        return false;
    }

    @Override
    public void selectAllPositionOrNot(boolean selectAll) {

    }

    @Override
    public void changeView(int spanCount) {

    }

    @Override
    public void refreshUnderBar() {

    }

    @Override
    public void makeGridLayout(int spanCount) {

    }


    @Override
    public void makeLinerLayout() {
//        if(linearLayoutManager!=null){
//            saveSharedPreferences(getContext(),"Column",1);
//        }
        linearLayoutManager = new LinearLayoutManager(getContext());
        //tListAdapter = new SettingItemAdapter(ts, this, getContext(), R.layout.file_list_item_layout);
        tListAdapter = new SupperAdapter<SettingItem>(ts, this.getContext()) {
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
                AlertDialog.Builder builder = showAlertDialog(getContext(), "主题选择");
                builder.setSingleChoiceItems(item, i, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            saveSharedPreferences(getContext(), "theme", R.style.AppTheme);
                            saveSharedPreferences(getContext(), "main_color", BULE_COLOR);
                        } else if (which == 1) {
                            saveSharedPreferences(getContext(), "theme", R.style.MyThemeGray);
                            saveSharedPreferences(getContext(), "main_color", DARK_COLOR);
                        }
                        readSharedPreferences(getContext());
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
                saveSharedPreferences(getContext(), "RecycleBin", i);
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
                saveSharedPreferences(getContext(), "ImageCacheSwitch", i);
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
                saveSharedPreferences(getContext(), "SmallViewSwitch", i);
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
                SimpleDialog donationsAlipayPic =new SimpleDialog(getContext(),R.layout.dialog_detail_pic_layout,"打钱给开发者") {
                    @Override
                    public void queryButtonClick(View v) {
                        //getContext().showToast("开发者支付宝13809761134");
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

        updateItem = new SettingItem("检查更新", "当前版本" + getVersionName(getContext())+ "，点击检查更新", 4501) {
            @Override
            public void click(View v) {
                if (isSwitchOpen()) {
                    SimpleDialog updateDialog =new SimpleDialog(getContext(),R.layout.dialog_detail_info_layout,"升级信息") {
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
            deleteDirectory(getContext().getCacheDir().getPath());
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
                updateApk = up.saveAttachMent(getContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (updateApk != null && updateApk.exists())
                installApk(updateApk.getPath(), getContext());
            else
                ToastUtils.showToast(getContext(), "升级下载失败！", 1000);
        }
    };


    Runnable countCacheSize = new Runnable() {//计算缓存大小
        @Override
        public void run() {
            cacheSize = (float) FileUtils.getLength(getContext().getCacheDir()) / MB;
            if (clearCache == null) {
                clearCache = new SettingItem("清除缓存", "当前缓存大小为" + stayFireNumber(cacheSize) + "MB", 5151) {
                    @Override
                    public void click(View v) {
                        new Thread(deleteCacheThread).start();
                    }
                };
            } else {
                clearCache.setDescription("当前缓存大小为" + stayFireNumber(cacheSize) + "MB");
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
