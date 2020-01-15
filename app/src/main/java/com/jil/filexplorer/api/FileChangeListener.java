package com.jil.filexplorer.api;

/**
 * 在其他activity删除了文件，主activity监听改变
 */
public interface FileChangeListener {
    void change();
}
