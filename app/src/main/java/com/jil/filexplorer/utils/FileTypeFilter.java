package com.jil.filexplorer.utils;

import com.jil.filexplorer.R;
import com.jil.filexplorer.interfaces.Filter;

import java.io.File;
import java.io.FilenameFilter;

public class FileTypeFilter{
    private final static long MB =1024*1024;
    private final static String[] VIDEO = {"mp4", "avi", "flv"};
    private final static String[] MUSIC = {"mp3", "wav", "fla"};
    private final static String[] IMAGE = {"jpg", "png", "gif"};
    private final static String[] TEXT = {"txt", "doc", "lrc"};

    public static int getIconRes(String name, long fileSize) {
        if (fileSize < 2*MB) {//小于2M
            if (imageIf(name)) return R.mipmap.list_ico_image;
            if (textIf(name)) return R.mipmap.list_ico_text;
            if (musicIf(name)) return R.mipmap.list_ico_music;
            if (videoIf(name)) return R.mipmap.list_ico_video;
        } else if (fileSize < 12*MB) {//小于12M
            if (musicIf(name)) return R.mipmap.list_ico_music;
            if (videoIf(name)) return R.mipmap.list_ico_video;
            if (imageIf(name)) return R.mipmap.list_ico_image;
            if (textIf(name)) return R.mipmap.list_ico_text;
        } else if (fileSize < 50*MB) {//小于50M
            if (videoIf(name)) return R.mipmap.list_ico_video;
            if (musicIf(name)) return R.mipmap.list_ico_music;
            if (imageIf(name)) return R.mipmap.list_ico_image;
            if (textIf(name)) return R.mipmap.list_ico_text;
        }else {
            if (videoIf(name)) return R.mipmap.list_ico_video;
            if (musicIf(name)) return R.mipmap.list_ico_music;
            if (imageIf(name)) return R.mipmap.list_ico_image;
            if (textIf(name)) return R.mipmap.list_ico_text;
        }
        return R.mipmap.list_ico_unknow;
    }

    private static boolean textIf(String name) {
        boolean isType = false;
        int i = 0;
        while (!isType && i < TEXT.length) {
            isType = name.equals(TEXT[i]);
            i++;
        }
        return isType;
    }

    public static boolean imageIf(String name) {
        boolean isType = false;
        int i = 0;
        while (!isType && i < IMAGE.length) {
            isType = name.equals(IMAGE[i]);
            i++;
        }
        return isType;
    }

    private static boolean musicIf(String name) {
        boolean isType = false;
        int i = 0;
        while (!isType && i < MUSIC.length) {
            isType = name.equals(MUSIC[i]);
            i++;
        }
        return isType;
    }

    private static boolean videoIf(String name) {
        boolean isType = false;
        int i = 0;
        while (!isType && i < VIDEO.length) {
            isType = name.equals(VIDEO[i]);
            i++;
        }
        return isType;
    }


   /* public FileTypeFilter(String name, long fileSize) {
        this.name = name;
        this.fileSize = fileSize;
    }

    @Override
    public boolean accept(File file, String s) {


        return false;
    }*/

   /* class CodeFilter implements Filter {


        @Override
        public boolean accept(String name) {
            return false;
        }
    }

    class ImageFilter implements Filter {

        @Override
        public boolean accept(String name) {
            return false;
        }
    }

    class VideoFilter implements Filter {

        @Override
        public boolean accept(String name) {
            return false;
        }
    }

    class MusicFilter implements Filter {

        @Override
        public boolean accept(String name) {
            return false;
        }
    }*/
}
