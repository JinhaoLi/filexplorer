package com.jil.filexplorer.Api;

import java.io.File;
import java.io.FileFilter;

public class ApkFileFilter implements FileFilter {

    @Override
    public boolean accept(File pathname) {
        return pathname.getName().endsWith(".apk");
    }
}
