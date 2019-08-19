package com.jil.filexplorer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.jil.filexplorer.Api.Item;
import com.jil.filexplorer.adapter.ImageAdapter;
import com.jil.filexplorer.adapter.SurperAdapter;
import com.jil.filexplorer.ui.MyViewPager;
import com.jil.filexplorer.utils.ConstantUtils;
import com.jil.filexplorer.utils.FileUtils;
import com.jil.filexplorer.utils.LogUtils;

import java.io.File;
import java.util.ArrayList;

import static com.jil.filexplorer.utils.DialogUtils.showListPopupWindow;

public class ImageDisplayActivity extends AppCompatActivity {
    private MyViewPager viewPager;
    private RecyclerView small_image_list;
    private ImageAdapter imageAdapter;
    private TextView imageTitle;
    private LinearLayout linearLayout;
    private SurperAdapter<File> fileSurperAdapter;
    private Button menuButton;
    private int selectPosition=-1;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*set it to be no title*/
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        /*set it to be full screen*/
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_image_display);

        linearLayout=findViewById(R.id.image_top_bar);
        menuButton =findViewById(R.id.button2);
        final View view =findViewById(R.id.view9);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Item[] list = {new Item("删除",0),new Item("分享",1),new Item("属性",2),new Item("打开为",3)};//要填充的数据
                final ListPopupWindow listPopupWindow=showListPopupWindow(ImageDisplayActivity.this,view,R.layout.menu_simple_list_item,list);
                listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        File file =imageAdapter.getImages().get(selectPosition);
                        switch (position){
                            case 0:
                                file.delete();
                                if(selectPosition==0){
                                    viewPager.setCurrentItem(selectPosition+1);
                                }else {
                                    viewPager.setCurrentItem(selectPosition-1);
                                }
                                imageAdapter.remove(selectPosition);
                                fileSurperAdapter.notifyDataSetChanged();
                                break;
                            case 1:
                                FileUtils.shareImage(ImageDisplayActivity.this,file.getPath());
                                break;
                            case 2:
                            case 3:
                                FileUtils.chooseViewFile(ImageDisplayActivity.this,file.getPath());
                                break;
                        }
                        listPopupWindow.dismiss();

                    }
                });
                listPopupWindow.show();

            }
        });
        Intent intent =getIntent();
        getRes(intent);
        final String imageName =intent.getStringExtra("image_path");
        if(imageName==null||imageName.equals("")){
            return;
        }
        viewPager=findViewById(R.id.image_show);
        imageTitle=findViewById(R.id.textView15);
        small_image_list=findViewById(R.id.image_small_view_list);
        final LinearLayoutManager linearLayoutManager =new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        imageAdapter=new ImageAdapter(imageName,this);



        viewPager.setAdapter(imageAdapter);
        selectPosition=imageAdapter.findPositionByName(imageName);
        viewPager.setCurrentItem(selectPosition);
        imageTitle.setText(imageAdapter.getImages().get(selectPosition).getName());

        fileSurperAdapter =new SurperAdapter<File>(imageAdapter.getImages(), this, new SurperAdapter.OnItemClickListener<File>() {
            @Override
            public void onItemClick(SurperAdapter.VH holder, File data) {
                viewPager.setCurrentItem(holder.getLayoutPosition());
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
        small_image_list.setLayoutManager(linearLayoutManager);
        linearLayoutManager.scrollToPosition(selectPosition);
        small_image_list.setAdapter(fileSurperAdapter);
        //viewPager.setPageTransformer(true);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                if(selectPosition!=-1){
                    if(linearLayoutManager.findViewByPosition(selectPosition)!=null)
                        linearLayoutManager.findViewByPosition(selectPosition).setBackgroundColor(ConstantUtils.HALF_DARK_COLOR);
                }
                selectPosition=position;
                if(linearLayoutManager.findViewByPosition(position)!=null)
                    linearLayoutManager.findViewByPosition(position).setBackgroundColor(ConstantUtils.IMAGE_SELECTED_COLOR);

            }

            @Override
            public void onPageSelected(int position) {
                linearLayoutManager.scrollToPosition(position);
                imageTitle.setText(imageAdapter.getImages().get(position).getName());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void getRes(Intent intent) {
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            } else if (type.startsWith("image/")) {
                handleSendImage(intent); // Handle single image being sent
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendMultipleImages(intent); // Handle multiple images being sent
            }
        } else {
            // Handle other intents, such as being started from the home screen
        }

    }

    public void hideView(){
        if(linearLayout.getVisibility()==View.GONE){
            linearLayout.setVisibility(View.VISIBLE);
            small_image_list.setVisibility(View.VISIBLE);
        }else {
            linearLayout.setVisibility(View.GONE);
            small_image_list.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            // Update UI to reflect text being shared
        }
    }

    void handleSendImage(Intent intent) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            // Update UI to reflect image being shared

        }
    }

    void handleSendMultipleImages(Intent intent) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            // Update UI to reflect multiple images being shared
        }
    }
}
