package bean;

import java.sql.Time;
import java.text.DecimalFormat;


// 作业类
public class JCB {
    // 作业标识号(在创建过程中设置为自增)
    private int id;
    // 作业提交时间
    private Time submitTime;
    // 作业需要的总时间
    private float totaltime;
    // 作业已运行时间
    private float runTime;
    // 作业开始时间
    private Time startTime;
    // 作业完成时间
    private Time overTime;
    // 作业周转时间
    private Float roundTime;
    // 作业状态 四种:未提交、等待 、执行、完成
    private String state;

    // 构造函数
    public JCB(int id, Time submitTime, float totalTime, String state) {
        this.id = id;
        this.submitTime = submitTime;
        this.totaltime = totalTime;
        this.runTime = 0;
        this.state = state;
    }

    // getter and setter

    public float getTotaltime() {
        return totaltime;
    }

    public void setTotaltime(float totaltime) {
        this.totaltime = totaltime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Time getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Time submitTime) {
        this.submitTime = submitTime;
    }

    public float getRunTime() {
        return runTime;
    }

    public void setRunTime(float runTime) {
        this.runTime = runTime;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getOverTime() {
        return overTime;
    }

    public void setOverTime(Time overTime) {
        this.overTime = overTime;
    }

    // 计算周转时间 overTime-startTime
    public String getRoundTime() {
        if(submitTime!=null && overTime!=null) {
            String[] t1 = submitTime.toString().split(":");
            String[] t2 = overTime.toString().split(":");
            int h1 = Integer.parseInt(t1[0]);
            int m1 = Integer.parseInt(t1[1]);
            int s1 = Integer.parseInt(t1[2]);
            int h2 = Integer.parseInt(t2[0]);
            int m2 = Integer.parseInt(t2[1]);
            int s2 = Integer.parseInt(t2[2]);
            int ss1 = h1*3600 + m1*60 + s1;
            int ss2 = h2*3600 + m2*60 + s2;
            return new DecimalFormat("0.000000").format((float)(ss2-ss1)/3600) ;
        }
        return "";
    }

    public void setRoundTime(float roundTime) {
        this.roundTime = roundTime;
    }

    // runTime +1s
    public void addRunTime() {
        this.setRunTime(this.getRunTime()+1);
    }

    public Object[] getJCBInfo_FCFS() {
        Object[] info = {this.getId(), this.getSubmitTime(), this.getTotaltime(), this.getStartTime(),
        this.getState(), this.getOverTime(), this.getRoundTime()};
        return info;
    }


}
