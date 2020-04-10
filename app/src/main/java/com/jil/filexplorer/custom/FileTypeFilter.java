package com.jil.filexplorer.custom;

import com.jil.filexplorer.R;

import static com.jil.filexplorer.utils.ConstantUtils.MB;

public class FileTypeFilter{

    private final static String[] VIDEO = {"mp4", "avi", "flv"};
    private final static String[] MUSIC = {"mp3", "wav", "fla"};
    private final static String[] IMAGE = {"jpg", "png", "gif"};
    private final static String[] TEXT = {"txt", "doc", "lrc"};
    private final static String[] PAGKE ={"zip","rar","7z","jar","iso"};
    /**
     *
     * @param mineType image/jpeg
     * @param fileSize
     * @return
     */
    public static int getIconRes(String mineType, long fileSize) {
        if (imageIf(mineType)) return R.mipmap.list_ico_image;
        if (textIf(mineType)) return R.mipmap.list_ico_text;
        if (musicIf(mineType)) return R.mipmap.list_ico_music;
        if (videoIf(mineType)) return R.mipmap.list_ico_video;
        if(pagkeIf(mineType)) return R.mipmap.ico_pagke;
        return R.mipmap.list_ico_unknow;
    }

    private static boolean textIf(String name) {
        if(name.startsWith("text"))return true;
        boolean isType = false;
        int i = 0;
        while (!isType && i < TEXT.length) {
            isType = name.equals(TEXT[i]);
            i++;
        }
        return isType;
    }

    public static boolean imageIf(String name) {
        if(name.startsWith("image"))return true;
        boolean isType = false;
        int i = 0;
        while (!isType && i < IMAGE.length) {
            isType = name.equals(IMAGE[i]);
            i++;
        }
        return isType;
    }

    private static boolean musicIf(String name) {
        if(name.startsWith("audio"))return true;
        boolean isType = false;
        int i = 0;
        while (!isType && i < MUSIC.length) {
            isType = name.equals(MUSIC[i]);
            i++;
        }
        return isType;
    }

    public static boolean videoIf(String name) {
        if(name.startsWith("video"))return true;
        boolean isType = false;
        int i = 0;
        while (!isType && i < VIDEO.length) {
            isType = name.equals(VIDEO[i]);
            i++;
        }

        return isType;
    }

    public static boolean pagkeIf(String name) {
        boolean isType = false;
        int i = 0;
        while (!isType && i < PAGKE.length) {
            isType = name.contains(PAGKE[i]);
            i++;
        }
        return isType;
    }

}
