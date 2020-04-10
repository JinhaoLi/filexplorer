package com.jil.filexplorer.bean;

import com.jil.filexplorer.utils.FileUtils;
import com.jil.filexplorer.utils.LogUtils;

import static com.jil.filexplorer.api.FileOperation.*;
import static com.jil.filexplorer.utils.ConstantUtils.MB;

/**
 * 进度消息
 */
public class ProgressMessage {
    //开始时间
    private long startTime;
    //总大小
    private long totalSize;
    //项目总数量
    private int projectCount;
    //进度类型
    private int mode;
    //原路径
    private String in;
    //目标路径
    private String to;

    //现在的位置
    private long nowLoacation = 1;
    //已进行项目
    public int projectOverCount;
    //进行中的项目名称
    private String nowProjectName;

    //进度标题
    private String title;

    private float speed;


    public ProgressMessage(long startTime, long totalSize, int projectCount, int mode) {
        this.startTime = startTime;
        this.totalSize = totalSize;
        this.projectCount = projectCount;
        this.mode = mode;
    }

    public ProgressMessage(long startTime, long totalSize, int projectCount, int mode, String to) {
        this.startTime = startTime;
        this.totalSize = totalSize;
        this.projectCount = projectCount;
        this.mode = mode;
        this.to = to;
    }

    public void setIn(String in) {
        this.in = in;
    }

    public void setNowLoacation(long nowLoacation) {
        this.nowLoacation = Math.min(nowLoacation, totalSize);
    }

    public long getNowLoacation() {
        return nowLoacation;
    }

    public void setProjectOverCount(int projectOverCount) {
        this.projectOverCount = projectOverCount;
    }

    public void setNowProjectName(String nowProjectName) {
        this.nowProjectName = nowProjectName;
    }

    public String getNowProjectName() {
        return "项目名称：" + nowProjectName;
    }

    public String getTitle(int progress) {
        if (title == null || title.equals("")) {
            return "已完成" + progress + "%";
        } else {
            return title;
        }

    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 返回百分比
     *
     * @return 百分比
     */
    public int getProgress() {
        LogUtils.i(getClass().getName(), "{nowLoacation:" + nowLoacation + "}" +
                "{projectCount:" + projectCount + "}");
        if (totalSize == 0 || projectCount == 0) {
            return 0;
        }

        if (mode == MODE_COPY) {
            return (int) (nowLoacation * 100 / totalSize);
        } else if (mode == MODE_COMPRESS) {
            return (int) (nowLoacation * 100 / totalSize);
        } else {
            return (projectOverCount * 100) / projectCount;
        }
    }

    public String getMessage() {
        String start, type;
        if (this.in != null && this.in.length() > 50) {
            String s = this.in.substring(this.in.length() - 45);
            this.in = "..." + s;
        }

        if (this.to != null && this.to.length() > 50) {
            String s = this.to.substring(this.to.length() - 45);
            this.to = "..." + s;
        }
        String out = "<font color=\"#1586C6\">" + this.in + "</font>";
        String to = "<font color=\"#1586C6\">" + this.to + "</font>";
        if (projectCount != 0) {
            start = "正在将" + projectCount + "个项目从";
        } else {
            start = "正在将";
        }
        if (mode == MODE_COPY) {
            type = "复制到";
        } else if (mode == MODE_MOVE) {
            type = "移动到";
        } else if (mode == MODE_RENAME) {
            type = "重命名为";
        } else if (mode == MODE_DELETE) {
            type = "删除";
            to = "";
        } else if (mode == MODE_COMPRESS) {
            type = "压缩到";
        } else if (mode == MODE_RECYCLE) {
            type = "移动到";
            to = "回收站";
        } else {
            type = "";
        }
        return start + out + type + to;
    }

    public void setProjectCount(int projectCount) {
        this.projectCount = projectCount;
    }

    public String getSpeedMessage() {
        if (mode == MODE_COPY || mode == MODE_COMPRESS) {
            return "速度：" + FileUtils.stayFireNumber(speedGet() / MB) + "Mb/秒";
        } else if (mode == MODE_MOVE||mode==MODE_DELETE) {
            return "速度：" + FileUtils.stayFireNumber(speedGet()) + "个项目/秒";
        } else {
            return "";
        }
    }

    private float speedGet(){
        float useTime = (System.currentTimeMillis() - startTime) / 1000f;
        if (useTime != 0f) {
            if (mode == MODE_COPY || mode == MODE_COMPRESS) {
                speed= nowLoacation / useTime;
            } else if (mode == MODE_MOVE||mode==MODE_DELETE) {
                speed = projectOverCount / useTime;
            } else {
                speed= nowLoacation / useTime;
            }
            return speed;
        }else {
            speed= (float) (totalSize);
            return speed;
        }
    }

    public String getReMainCount() {
        if (mode == MODE_COPY || mode == MODE_COMPRESS) {
            return "剩余项目：" + (projectCount - projectOverCount) + "\t(\t" + (totalSize - nowLoacation) / MB + "MB\t)";
        } else {
            return "剩余项目：" + (projectCount - projectOverCount);
        }

    }

    public String getReMainTime() {
        float s =(totalSize - nowLoacation) / speed;

        if (mode == MODE_COPY || mode == MODE_COMPRESS) {
            return "剩余时间：大约" + FileUtils.keepADecimalPlaces(s) + "秒";
        } else {
            float speed_1 = (projectCount - projectOverCount) / speed;
            return "剩余时间：大约" + (int)speed_1 + "秒";
        }
    }

}
