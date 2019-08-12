package com.jil.filexplorer.Api;

import static com.jil.filexplorer.Api.FileOperation.MODE_COPY;
import static com.jil.filexplorer.Api.FileOperation.MODE_DELETE;
import static com.jil.filexplorer.Api.FileOperation.MODE_MOVE;
import static com.jil.filexplorer.Api.FileOperation.MODE_RENAME;
import static com.jil.filexplorer.utils.ConstantUtils.MB;

/**
 * 进度消息
 */
public class ProgressMessage {
    //开始时间
    private long startTime;
    //结束位置
    private long endLoacation=1;
    //项目总数量
    public int projectCount;
    //进度类型
    private int mType;
    //原路径
    private String in;
    //目标路径
    private String to;

    //现在复制到的位置
    private long nowLoacation=1;
    //已进行项目
    public int copyOverCount;
    //进行中的项目名称
    private String nowProjectName;

    //百分比
    private int mProgress;
    //进度标题
    private String title;
    //剩余时间
    private long reMainTime;
    //速度
    private float speed;

    public long getEndLoacation() {
        return endLoacation;
    }

    public int getProjectCount() {
        return projectCount;
    }

    public ProgressMessage(long startTime, long endLoacation, int projectCount, int mType) {
        this.startTime = startTime;
        this.endLoacation = endLoacation;
        this.projectCount = projectCount;
        this.mType = mType;
    }

    public ProgressMessage(long startTime, long endLoacation, int projectCount, int mType, String to) {
        this.startTime = startTime;
        this.endLoacation = endLoacation;
        this.projectCount = projectCount;
        this.mType = mType;
        this.to = to;
    }

    public void setIn(String in) {
        this.in = in;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public ProgressMessage(long startTime) {
        this.startTime = startTime;
    }

    public ProgressMessage(long startTime, int projectCount, int mType, String in, String to) {
        this.startTime = startTime;
        this.projectCount = projectCount;
        this.mType = mType;
        this.in = in;
        this.to = to;
    }

    public ProgressMessage() {

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
        this.mProgress =getProgress();
        return "已完成"+ mProgress +"%";
    }

    /**
     * 返回百分比
     * @return 百分比
     */
    public int getProgress(){
        if(endLoacation==0){
            return 100;
        }
        if(mType== MODE_COPY){
            return (int) (nowLoacation*100/endLoacation);
        }else {
            return (int) (nowLoacation*100/endLoacation);
        }
    }

    public String getMessage(){
        String start,type;
        if(this.in!=null&&this.in.length()>50){
            String s =this.in.substring(this.in.length()-45);
            this.in="..."+s;
        }

        if(this.to!=null&&this.to.length()>50){
            String s =this.to.substring(this.to.length()-45);
            this.to="..."+s;
        }
        String out ="<font color=\"#1586C6\">"+this.in +"</font>";
        String to ="<font color=\"#1586C6\">"+this.to+"</font>";
        if(projectCount!=0){
            start="正在将"+projectCount+"个项目从";
        }else {
            start="正在将";
        }
        if(mType== MODE_COPY){
            type ="复制到";
        }else if(mType==MODE_MOVE){
            type ="移动到";
        }else if(mType==MODE_RENAME){
            type ="重命名为";
        }else if(mType==MODE_DELETE){
            type="删除";
            to ="";
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
        //nowTime= nowTime==0? 1:nowTime;
        if(nowTime!=0){
            if(mType== MODE_COPY){
                return "速度："+ (nowLoacation/(nowTime))/MB+"Mb/秒";
            }else if(mType==MODE_MOVE) {
                return "速度："+ copyOverCount /(nowTime)+"个项目/秒";
            }else {
                return "";
            }
        }
        return "";
    }

    public String getReMainCount(){
        if(mType== MODE_COPY){
            return "剩余项目："+(projectCount-copyOverCount)+"("+(endLoacation-nowLoacation)/MB+"MB)";
        }else {
            return "剩余项目："+(projectCount- copyOverCount);
        }

    }

    public String getReMainTime(){
        long nowTime =System.currentTimeMillis()-startTime;
        nowTime= nowTime==0 ? 1:nowTime;
        float s= (endLoacation-nowLoacation) / (nowLoacation/(nowTime/1000f));
        if(mType== MODE_COPY){
            return "剩余时间：大约"+s+"秒";
        }else {
            float speed_1 =(copyOverCount/(nowTime/1000f));
            speed_1= speed_1==0 ? 1:speed_1;
            return "剩余时间：大约"+(projectCount- copyOverCount)  /  speed_1 +"秒";
        }
    }


    public String getfileName(){
        return "名称："+ nowProjectName;
    }

    public int getOverCount(){
        return copyOverCount;
    }

}
