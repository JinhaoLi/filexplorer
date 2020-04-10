package com.jil.filexplorer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.jil.filexplorer.bean.Item;
import com.jil.filexplorer.R;
import com.jil.filexplorer.utils.ConstantUtils;

/***
 * 用于ListPopupWindow的适配器
 */
public class ListPopupWindowAdapter extends BaseAdapter {
    private Item[] itemArrayList;
    private Context mContext;
    private int layoutRes;

    public ListPopupWindowAdapter(Context context,int layoutRes,Item[] itemArrayList) {
        this.layoutRes=layoutRes;
        this.mContext=context;
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
    public Object getItem(int position) {
        return null;
    }

    @Override
    public View getView(int position, View convertView,  ViewGroup parent) {
        ViewHolder holder=null;
        if(convertView == null){

            convertView = LayoutInflater.from(mContext).inflate(R.layout.menu_simple_list_item,parent,false);
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
