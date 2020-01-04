package com.jil.filexplorer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.jil.filexplorer.activity.MainActivity;
import com.jil.filexplorer.api.OnDoubleClickListener;
import com.jil.filexplorer.ui.SettingFragment;
import com.jil.filexplorer.api.SettingItem;
import com.jil.filexplorer.api.SettingParam;
import com.jil.filexplorer.R;

import java.util.ArrayList;

import static com.jil.filexplorer.utils.FileUtils.getOptions;
import static com.jil.filexplorer.utils.FileUtils.viewFile;

public class SettingItemAdapter extends RecyclerView.Adapter<SettingItemAdapter.DefaultViewHolder>{
    private ArrayList<SettingItem> mData;
    private SettingFragment settingFragment;
    private MainActivity mMainActivity;
    private int itemLayoutRes;
    public SettingItemAdapter(ArrayList<SettingItem> ts, SettingFragment settingFragment, MainActivity mMainActivity, int layoutRes) {
        this.mData=ts;
        this.settingFragment=settingFragment;
        this.mMainActivity=mMainActivity;
        this.itemLayoutRes=layoutRes;
    }

    @NonNull
    @Override
    public DefaultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayoutRes, parent, false);
        return new DefaultViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DefaultViewHolder holder, int position) {
        final SettingItem settingItem =mData.get(position);
        holder.fileName.setText(settingItem.getName());
        RequestOptions options=getOptions(SettingParam.ImageCacheSwitch,260,283);
        Glide.with(mMainActivity).load(R.drawable.ic_settings).apply(options).into(holder.icon);
        holder.type.setText("");
        holder.itemView.setOnTouchListener(new OnDoubleClickListener(new OnDoubleClickListener.DoubleClickCallback() {
            @Override
            public void onDoubleClick(View view) {
                settingItem.click(view);
            }

            @Override
            public boolean onLongClick() {
                return false;
            }
        }));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class DefaultViewHolder extends RecyclerView.ViewHolder {
        private TextView fileName;
        private TextView date;
        private TextView type;
        private TextView size;
        private ImageView icon;

        public DefaultViewHolder(@NonNull View itemView) {
            super(itemView);
            if(itemLayoutRes== R.layout.file_list_item_layout){
                this.fileName = itemView.findViewById(R.id.textView2);
                this.date = itemView.findViewById(R.id.textView3);
                this.type = itemView.findViewById(R.id.textView4);
                this.size = itemView.findViewById(R.id.textView5);
                this.icon = itemView.findViewById(R.id.imageView2);
            }else {
                this.type = itemView.findViewById(R.id.textView4);
                this.fileName = itemView.findViewById(R.id.textView7);
                this.icon = itemView.findViewById(R.id.imageView6);
            }

        }
    }
}
