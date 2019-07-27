package com.jil.filexplorer.Api;

import static com.jil.filexplorer.utils.ConstantUtils.MB;
import static com.jil.filexplorer.utils.ConstantUtils.PROGRESS_MODE_COPY;
import static com.jil.filexplorer.utils.ConstantUtils.PROGRESS_MODE_MOVE;
import static com.jil.filexplorer.utils.ConstantUtils.PROGRESS_MODE_RECYCLE;
import static com.jil.filexplorer.utils.ConstantUtils.PROGRESS_MODE_RENAME;

public class ProgressMessage {
    //开始时间
    private long startTime;
    //结束位置
    private long endLoacation=1;
    //项目总数量
    private int projectCount;
    //进度类型
    private int mType;
    //原路径
    String out;
    //目标路径
    String to;

    //现在复制到的位置
    private long nowLoacation=1;
    //已进行项目
    private int copyOverCount;
    //进行中的项目名称
    private String nowProjectName;

    //百分百
    private int mProgress;
    //进度标题
    private String title;
    //剩余时间
    private long reMainTime;
    //速度
    private float speed;

    public ProgressMessage(long startTime, long endLoacation, int projectCount, int mType, String out, String to) {
        this.startTime = startTime;
        this.endLoacation = endLoacation;
        this.projectCount = projectCount;
        this.mType = mType;
        this.out = out;
        this.to = to;
    }

    public ProgressMessage(long startTime,int projectCount, int mType, String out, String to) {
        this.startTime = startTime;
        this.projectCount = projectCount;
        this.mType = mType;
        this.out = out;
        this.to = to;
    }

    public void setNowLoacation(long nowLoacation) {
        this.nowLoacation = nowLoacation>endLoacation ? endLoacation:nowLoacation;
    }

    public long getNowLoacation() {
        return nowLoacation;
    }

    public void setCopyOverCount(int copyOverCount) {
        this.copyOverCount = copyOverCount;
    }

    public void setNowProjectName(String nowProjectName) {
        this.nowProjectName = nowProjectName;
    }

    public String getNowProjectName() {
        return "项目名称："+nowProjectName;
    }

    public void setEndLoacation(long endLoacation) {
        this.endLoacation = endLoacation;
    }

    public String getTitle(){
        this.mProgress =getProggress();
        return "已完成"+ mProgress +"%";
    }

    public int getProggress(){
        if(mType==PROGRESS_MODE_COPY){
            return (int) (nowLoacation*100/endLoacation);
        }else {
            return (int) (nowLoacation*100/endLoacation);
            //return (copyOverCount *100/projectCount);
        }
    }

    public String getMessage(){
        String start,type;
        String out ="<font color=\"#1586C6\">"+this.out+"</font>";
        String to ="<font color=\"#1586C6\">"+this.to+"</font>";
        if(projectCount!=1){
            start="正在将"+projectCount+"个项目从";
        }else {
            start="正在将";
        }
        if(mType==PROGRESS_MODE_COPY){
            type ="复制到";
        }else if(mType==PROGRESS_MODE_MOVE){
            type ="移动到";
        }else if(mType==PROGRESS_MODE_RECYCLE){
            type ="移动到";
        }else if(mType==PROGRESS_MODE_RENAME){
            type ="重命名为";
        }else {
            type="";
        }
        return start+out+type+to;
    }

    public void setProjectCount(int projectCount) {
        this.projectCount = projectCount;
    }

    public String getSpeed(){
        long nowTime =(System.currentTimeMillis()-startTime)/1000;
        if(mType==PROGRESS_MODE_COPY&&nowTime!=0){
            return "速度："+ (nowLoacation/(nowTime))/MB+"Mb/秒";
        }else if(nowTime!=0) {
            return "速度："+ copyOverCount /(nowTime)+"个项目/秒";
        }else {
            return "";
        }
    }

    public String getReMainCount(){
        if(mType==PROGRESS_MODE_COPY){
            return "剩余项目："+(projectCount- copyOverCount)+"("+(endLoacation-nowLoacation)/MB+"MB)";
        }else {
            return "剩余项目："+(projectCount- copyOverCount);
        }

    }

    public String getReMainTime(){
        long nowTime =System.currentTimeMillis()-startTime;
        if(mType==PROGRESS_MODE_COPY){
            return "剩余时间：大约"+(endLoacation-nowLoacation) / (nowLoacation/nowTime) /1000+"秒";
        }else {
            return "剩余时间：大约"+(projectCount- copyOverCount)  / (copyOverCount /(nowTime/1000))  +"秒";
        }
    }

    public String getCopyOverCount(){
        return "名称："+ nowProjectName;
    }

}
