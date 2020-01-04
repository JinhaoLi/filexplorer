package com.jil.filexplorer.api;

import com.jil.filexplorer.utils.FileUtils;

import java.io.File;
import java.io.FileFilter;

public class ImageFilter implements FileFilter {

    @Override
    public boolean accept(File file) {
        if(file.isDirectory()){
            return false;
        }
        String type = FileUtils.getMimeType(file.getPath());
        return type.startsWith("image");
    }
}
