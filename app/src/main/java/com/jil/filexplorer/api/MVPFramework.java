package com.jil.filexplorer.api;

import java.io.FileFilter;
import java.util.List;

public class MVPFramework {
    public interface IView {
        /**
         * 初始化
         */
        void init();

        /**
         * 更新视图
         */
        void update();

        /**
         * 展示错误信息
         * @param msg 错误信息
         */
        void setErr(String msg);
    }

    public interface IPresenter {

    }

    public interface IModel {
        /**
         * 数据加载完成后的监听回调
         */
        interface ResultListener<T> {
            /**
             * 任务成功
             * @param list
             * @param jsonMsg
             * @param name
             */
            void onComplete(List<T> list, String jsonMsg, String name);
            void onError(String msg);
        }

    }


}
