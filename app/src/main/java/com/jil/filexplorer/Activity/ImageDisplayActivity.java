package com.jil.filexplorer.Activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.jil.filexplorer.Api.FileChangeListenter;
import com.jil.filexplorer.Api.ImageFilter;
import com.jil.filexplorer.Api.Item;
import com.jil.filexplorer.R;
import com.jil.filexplorer.adapter.ImageAdapter;
import com.jil.filexplorer.adapter.SupperAdapter;
import com.jil.filexplorer.adapter.UriAdapter;
import com.jil.filexplorer.ui.ImagesPager;
import com.jil.filexplorer.utils.ConstantUtils;
import com.jil.filexplorer.utils.FileUtils;
import com.jil.filexplorer.utils.LogUtils;
import com.jil.filexplorer.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static com.jil.filexplorer.utils.DialogUtils.showFileInfoMsg;
import static com.jil.filexplorer.utils.DialogUtils.showListPopupWindow;
import static com.jil.filexplorer.utils.FileUtils.getPicWidthAndHeight;
import static com.jil.filexplorer.utils.UiUtils.NavigationBarStatusBar;
import static com.jil.filexplorer.utils.UiUtils.setNavigationBar;

public class ImageDisplayActivity extends AppCompatActivity {
    private ImagesPager viewPager;
    private ArrayList<File> images;
    private ArrayList<Uri> uris;
    private RecyclerView small_image_list;
    private ImageAdapter imageAdapter;
    private UriAdapter uriAdapter;
    private LinearLayoutManager linearLayoutManager;
    private TextView imageTitle;
    private LinearLayout titleTopBar;
    private ListPopupWindow menu;
    private SupperAdapter<File> imageFileSupperAdapter;
    private SupperAdapter<Uri> uriSupperAdapter;
    private Button menuButton;
    private String imageDirPath;
    private boolean isThisAppRes;
    private boolean isSamePath;
    private static FileChangeListenter fileChangeListenter;

    public static void setFileChangeListenter(FileChangeListenter fileChange) {
        fileChangeListenter = fileChange;
    }
   @SuppressLint("HandlerLeak")
   Handler updateUi =new Handler(){
       @Override
       public void handleMessage(Message msg) {
           super.handleMessage(msg);
           if(msg.what==1521){
               initAction();
           }
       }
   };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //NavigationBarStatusBar(ImageDisplayActivity.this,true);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_image_display);
        Intent intent =getIntent();
        tryFindPath(intent);

        initView();

    }




    private void tryFindPath(Intent intent) {
        isThisAppRes=checkUriEqualsDisk(intent);
        if (isThisAppRes) {
            loadImagesInDisk();
        } else {
            getRes(intent);
            uriAdapter=new UriAdapter(uris,this);
        }
    }

    private boolean checkUriEqualsDisk(Intent intent) {
        isSamePath=false;
        Uri uri;
        File f;
        String path;
        uri = intent.getData();
        if(uri==null){
            return false;
        }else {
            path=uri.getPath();
        }
        if(path!=null&&!new File(path).exists()){
            f=new File(Environment.getExternalStorageDirectory()+File.separator+path.substring(path.indexOf("/",2)));
        }else {
            f =new File(uri.getPath());
        }
        if(f.exists()){
            if(imageDirPath!=null){
                File old =new File(imageDirPath);
                if(old.getParent().equals(f.getParent()))
                isSamePath=true;
            }
            imageDirPath=f.getPath();
            return true;
        }
        return false;
    }

    private void initView() {
        titleTopBar =findViewById(R.id.image_top_bar);
        menuButton =findViewById(R.id.button2);
        viewPager=findViewById(R.id.image_show);
        imageTitle=findViewById(R.id.textView15);
        small_image_list=findViewById(R.id.image_small_view_list);
    }

    private void initAction() {
        imageAdapter=new ImageAdapter(images,ImageDisplayActivity.this);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageMenu(v);
            }
        });
        linearLayoutManager =new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        //imageAdapter=new ImageAdapter(imageDirPath,this);
        viewPager.setAdapter(isThisAppRes?imageAdapter:uriAdapter);
        selectPosition=findNowPosition();
        viewPager.setCurrentItem(selectPosition);
        setTopBarTitle(selectPosition);
        makeAdapter();
        small_image_list.setLayoutManager(linearLayoutManager);
        linearLayoutManager.scrollToPosition(selectPosition);
        setSmallListAdapter();

        //viewPager.setPageTransformer(true);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if(selectPosition!=-1){
                    if(linearLayoutManager.findViewByPosition(selectPosition)!=null)
                        linearLayoutManager.findViewByPosition(selectPosition).setBackgroundColor(ConstantUtils.HALF_DARK_COLOR);
                }
                selectPosition=position;
                //nowPosition =position;
                linearLayoutManager.scrollToPosition(position);
                if(linearLayoutManager.findViewByPosition(position)!=null){
                    linearLayoutManager.findViewByPosition(position).setBackgroundColor(ConstantUtils.IMAGE_SELECTED_COLOR);
                }else {
                    stateStopSelect=true;
                }
                setTopBarTitle(selectPosition);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if(stateStopSelect&&state==0){
                    try{
                        linearLayoutManager.findViewByPosition(selectPosition).setBackgroundColor(ConstantUtils.IMAGE_SELECTED_COLOR);
                    }catch (Exception e ){
                        LogUtils.e("设置选中颜色时发生的错误",e.getMessage()+"由于被设置的view不可见，view==null");
                    }
                    stateStopSelect=false;
                }

            }
        });
    }

    private void setSmallListAdapter() {
        if(isThisAppRes){
            small_image_list.setAdapter(imageFileSupperAdapter);
        }else {
            small_image_list.setAdapter(uriSupperAdapter);
        }

    }

    private int findNowPosition() {
        if(isThisAppRes)
            return imageAdapter.findPositionByName(imageDirPath);
        else
            return uriAdapter.findPositionByName(uris.get(0));
    }

    private void makeAdapter() {
        if(isThisAppRes){
            imageFileSupperAdapter =new SupperAdapter<File>(images, this, new SupperAdapter.OnItemClickListener<File>() {
                @Override
                public void onItemClick(SupperAdapter.VH holder, File data, int position) {
                    viewPager.setCurrentItem(holder.getLayoutPosition(),true);
                }
            }) {
                @Override
                public int getLayoutId(int viewType, File data) {
                    return R.layout.small_image_layout;
                }

                @Override
                public void convert(VH holder, File data, int position, Context mContext) {
                    holder.setPic(R.id.imageView,data,mContext);
                    if(selectPosition!=position)
                        holder.itemView.setBackgroundColor(ConstantUtils.HALF_DARK_COLOR);
                    else
                        holder.itemView.setBackgroundColor(ConstantUtils.IMAGE_SELECTED_COLOR);
                }
            };
        }else {
            uriSupperAdapter =new SupperAdapter<Uri>(uris, this, new SupperAdapter.OnItemClickListener<Uri>() {
                @Override
                public void onItemClick(SupperAdapter.VH holder, Uri data, int position) {
                    viewPager.setCurrentItem(holder.getLayoutPosition(),true);
                }
            }) {
                @Override
                public int getLayoutId(int viewType, Uri data) {
                    return R.layout.small_image_layout;
                }

                @Override
                public void convert(VH holder, Uri data, int position, Context mContext) {
                    holder.setPic(R.id.imageView,data,mContext);
                    if(selectPosition!=position)
                        holder.itemView.setBackgroundColor(ConstantUtils.HALF_DARK_COLOR);
                    else
                        holder.itemView.setBackgroundColor(ConstantUtils.IMAGE_SELECTED_COLOR);
                }
            };
        }

    }

    private void setTopBarTitle(int selectPosition) {
        if(isThisAppRes){
            imageTitle.setText(images.get(selectPosition).getName());
        }else {
            imageTitle.setText(uris.get(selectPosition).getPath());
        }

    }

    private void loadImagesInDisk() {
        Runnable loadImage =new Runnable() {
            @Override
            public void run() {
                File file =new File(imageDirPath);
                ImageFilter imageFilter =new ImageFilter();
                File[] image = file.getParentFile().listFiles(imageFilter);
                images=new ArrayList<>(Arrays.asList(image));
                if(!FileUtils.getMimeType(imageDirPath).startsWith("image")){
                    images.add(file);
                }
                Message msg =new Message();
                msg.what=1521;
                updateUi.sendMessage(msg);
            }
        };
        new Thread(loadImage).start();
    }

    /**
     * 展示菜单
     * @param v
     */
    private void showImageMenu(final View v) {
        if(menu ==null){
            Item[] menuList;
            if(isThisAppRes){
               menuList = new Item[]{new Item("删除", 0), new Item("分享", 1), new Item("属性", 2), new Item("打开为", 3),new Item("加载原图",4)};//要填充的数据
            }else {
                menuList = new Item[]{new Item("属性", 2),new Item("加载大图",4)};//要填充的数据
            }
            menu =showListPopupWindow(ImageDisplayActivity.this,v,R.layout.menu_simple_list_item, menuList);
            menu.setWidth(300);
        }
        menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int i =(int)id;
                int clickId=-1;
                switch (i){
                    case 0:
                        File file=images.get(selectPosition);
                        boolean deleteSuccess=file.delete();
                        if(deleteSuccess){
                            imageAdapter.remove(selectPosition);
                            imageFileSupperAdapter.notifyDataSetChanged();
                            if(selectPosition!=images.size()){
                                setTopBarTitle(selectPosition);
                            }
                            if(fileChangeListenter!=null)
                            fileChangeListenter.change();
                        }else {
                            ToastUtils.showToast(ImageDisplayActivity.this,"删除失败",1000);
                        }
                        break;
                    case 1:
                        File file1=images.get(selectPosition);
                        FileUtils.shareFile(ImageDisplayActivity.this,file1.getPath(),"image/*");
                        break;
                    case 2:
                        showFileInfoMsg(ImageDisplayActivity.this,images.get(selectPosition).getPath());
                        break;
                    case 3:
                        clickId=3;
                        File file2=images.get(selectPosition);
                        FileUtils.chooseViewFile(ImageDisplayActivity.this,view,file2.getPath(),menu);
                        break;
                    case 4:
                        int[] wh =getPicWidthAndHeight(images.get(selectPosition).getAbsolutePath());
                        imageAdapter.width=wh[0];
                        imageAdapter.height=wh[1];
                        imageAdapter.notifyDataSetChanged();
                        break;
                }
                if(clickId!=3)
                menu.dismiss();
            }
        });
        menu.show();
//        menu.setOnDismissListener(new PopupWindow.OnDismissListener() {
//            @Override
//            public void onDismiss() {
//                NavigationBarStatusBar(ImageDisplayActivity.this,true);
//            }
//        });
    }


    private boolean stateStopSelect;
    private int selectPosition=-1;

    private void getRes(Intent intent) {
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_VIEW.equals(action) && type != null) {
            if(type.startsWith("image/")){
                handleSendImage(intent);
            }
        }
    }

    public void hideView(){
        if(titleTopBar.getVisibility()==View.INVISIBLE){
            titleTopBar.setVisibility(View.VISIBLE);
            small_image_list.setVisibility(View.VISIBLE);
            setNavigationBar(ImageDisplayActivity.this,true);
        }else {
            titleTopBar.setVisibility(View.INVISIBLE);
            small_image_list.setVisibility(View.INVISIBLE);
            NavigationBarStatusBar(ImageDisplayActivity.this,true);
        }

    }


    void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            // Update UI to reflect text being shared
        }
    }

    void handleSendImage(Intent intent) {
        Uri imageUri = intent.getData();
        if (imageUri != null) {
            uris=new ArrayList<>();
            uris.add(imageUri);
        }else {
            finish();
        }
    }

    void handleSendMultipleImages(Intent intent) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            // Update UI to reflect multiple images being shared
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkUriEqualsDisk(intent);
        if(isSamePath){
            selectPosition=findNowPosition();
            viewPager.setCurrentItem(selectPosition);
        }else {
            finish();
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fileChangeListenter =null;
    }
}
