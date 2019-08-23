package com.jil.filexplorer.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.jil.filexplorer.R;
import com.jil.filexplorer.utils.ConstantUtils;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class BoxAdapter<T> extends ArrayAdapter<T> {
    ArrayList<T> datas;
    int itemLayoutRes;
    private Context mContent;

    public BoxAdapter(Context context, int resource, ArrayList<T> datas) {
        super(context, resource);
        this.mContent=context;
        this.itemLayoutRes=resource;
        this.datas = datas;
    }

    public BoxAdapter(Context context, int resource, T[] datas) {
        super(context, resource);
        this.mContent=context;
        this.itemLayoutRes=resource;
        this.datas =new ArrayList<>(Arrays.asList(datas));
    }

    @Override
    public abstract long getItemId(int position);

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BoxAdapter.ViewHolder holder=null;
        if(convertView == null){
            convertView = LayoutInflater.from(mContent).inflate(R.layout.menu_simple_list_item,parent,false);
            holder = new BoxAdapter.ViewHolder();
            holder.txt_content = convertView.findViewById(android.R.id.text1);
            if(position==datas.size()-1){
                View view =convertView.findViewById(R.id.view9);
                view.setBackgroundColor(ConstantUtils.IMAGE_SELECTED_COLOR);
            }
            convertView.setTag(holder);
        }else{
            holder = (BoxAdapter.ViewHolder) convertView.getTag();
        }
        setData(holder,datas.get(position));
        return convertView;
    }

    public abstract void setData(BoxAdapter.ViewHolder holder,T data);

    public static class ViewHolder{
        public ImageView img_icon;
        public TextView txt_content;
    }
}
