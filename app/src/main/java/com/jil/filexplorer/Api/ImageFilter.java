package com.jil.filexplorer.Api;

import com.jil.filexplorer.utils.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

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
