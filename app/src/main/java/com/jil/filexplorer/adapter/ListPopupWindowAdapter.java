package com.jil.filexplorer.adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jil.filexplorer.Api.Item;
import com.jil.filexplorer.R;
import com.jil.filexplorer.utils.ConstantUtils;
import com.jil.filexplorer.utils.LogUtils;

import java.util.ArrayList;

public class ListPopupWindowAdapter extends ArrayAdapter<Item> {
    Item[] itemArrayList;

    public ListPopupWindowAdapter(Context context,int layoutRes,Item[] itemArrayList) {
        super(context,layoutRes);
        this.itemArrayList = itemArrayList;
    }



    @Override
    public long getItemId(int position) {
        return itemArrayList[position].getId();
    }


    @Override
    public int getCount() {
        return itemArrayList.length;
    }

    @Override
    public View getView(int position, View convertView,  ViewGroup parent) {
        ViewHolder holder=null;
        if(convertView == null){

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.menu_simple_list_item,parent,false);
            holder = new ViewHolder();
            //holder.img_icon =convertView.findViewById(R.id.img_icon);
            holder.txt_content = convertView.findViewById(android.R.id.text1);
            if(position==itemArrayList.length-1){
                View view =convertView.findViewById(R.id.view9);
                view.setBackgroundColor(ConstantUtils.IMAGE_SELECTED_COLOR);
            }
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        holder.txt_content.setText(itemArrayList[position].getName());
        return convertView;
    }

    private static class ViewHolder{
        ImageView img_icon;
        TextView txt_content;
    }
}
