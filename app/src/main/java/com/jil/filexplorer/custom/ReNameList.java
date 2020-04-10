package com.jil.filexplorer.custom;

import android.annotation.SuppressLint;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ReNameList {
    private static ReNameList reNameList;
    static String DIO =":/";
    @SuppressLint("UseSparseArrays")
    Map<Integer,String> map=new HashMap<>();

    private ReNameList(){

    }

    public  static ReNameList getInstance(String reg) {
        if(reNameList==null){
            reNameList=new ReNameList();
        }
        reNameList.makeMap(reg);
        return reNameList;
    }


    public void makeMap(String str){
        String exmple = str.replaceAll(":",DIO);
        String[] s =exmple.split(":");
        ArrayList<String> as =new ArrayList<>(Arrays.asList(s));
        map.clear();
        System.out.println(exmple);
        int count =1;
        System.out.println("===================表达式");
        for(String temp:as){
            if(temp.trim().equals("")){
                continue;
            }
            System.out.println(temp);
            if(temp.startsWith("/s")){
                map.put(count,temp.trim().substring(temp.indexOf("=")+1));
            }else if(temp.startsWith("/i")){
                map.put(count,temp.trim());
            }else if(temp.startsWith("/d")){
                map.put(count,formatDate(System.currentTimeMillis()));
            }
            count++;
        }
        System.out.println("===================表达式");
    }

    public String reName(int twice){
        StringBuilder name =new StringBuilder();
        for(int i =1;i<=map.size();i++){
            String t =map.get(i);
            int it;
            if(t.startsWith("/i+")){
                it= Integer.parseInt(t.substring(t.indexOf("=")+1));
                name.append(it+twice);
            }else if(t.startsWith("/i-")){
                it = Integer.parseInt(t.substring(t.indexOf("=")+1));
                name.append(it-twice);
            }else {
                name.append(t);
            }
        }
        return name.toString();

    }

    public ArrayList<String> getNameList(int size){
        ArrayList<String> temp =new ArrayList<>();
        for(int j =0;j<size;j++){
            temp.add(reName(j));
        }
        return temp;
    }

    private static String formatDate(long date){
        SimpleDateFormat dt =new SimpleDateFormat("yyyy-MM-dd");
        return dt.format(date);
    }

}
