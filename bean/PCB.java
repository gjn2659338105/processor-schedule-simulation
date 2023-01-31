package bean;

// 进程类
public class PCB {
    // 进程号
    private int pid;

    // 进程名
    private String name;
    // 进程到达时间
    private int arriveTime;
    // 进程开始时间
    private int startTime;
    // 进程结束时间
    private int overTime;
    // 进程运行时间
    private int totalTime;
    // 进程已执行时间
    private int runTime;

    // 进程状态(未到达、就绪、执行和完成)
    private String state;
    // 进程优先级
    private int priority;

    // 多级队列 -- 构造函数
    public PCB(int pid, int arriveTime, int totalTime) {
        this.pid = pid;
        this.arriveTime = arriveTime;
        this.totalTime = totalTime;
        this.runTime = 0;

        if(arriveTime!=0)
            this.state = "未到达";
        else
            this.state = "就绪";

        // 如果创建的时候已经是就绪状态，就直接投入到最高优先级队列中，即优先级设置为0
        if(this.state.equals("就绪"))
            priority = 0;
    }

    // 优先级时间片 -- 构造函数
    public PCB(String name, int arriveTime, int totalTime, int priority) {
        this.name = name;
        this.arriveTime = arriveTime;
        this.runTime = 0;
        this.priority = priority;
        this.totalTime = totalTime;

        if(arriveTime!=0)
            this.state = "未到达";
        else
            this.state = "就绪";
    }


    // getter and setter


    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getArriveTime() {
        return arriveTime;
    }

    public void setArriveTime(int arriveTime) {
        this.arriveTime = arriveTime;
    }

    public int getRunTime() {
        return runTime;
    }

    public void setRunTime(int runTime) {
        this.runTime = runTime;
    }

    public int getOverTime() {
        return overTime;
    }

    public void setOverTime(int overTime) {
        this.overTime = overTime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    // 降低优先级
    public void deClinePriority(int num) {
        setPriority(this.getPriority() + num);
    }

    public int getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    // 已执行时间 +1
    public void addRunTime() {
        this.setRunTime(this.getRunTime() + 1);
    }

    // 获取进程信息(用于填充入表格中)
    public Object[] getPCBInfo() {
        Object[] info = {this.getName(), this.getPriority(), this.getArriveTime(),
        this.getTotalTime() , this.getRunTime() , (this.getTotalTime()-this.getRunTime()) ,  this.getState()};
        return info;
    }

    public Object[] getPCBInfo2() {
        Object[] info = {this.getPid(),  this.getArriveTime(), this.getTotalTime() ,
                this.getRunTime() , (this.getTotalTime()-this.getRunTime()) ,  this.getState(), this.getPriority()};
        return info;
    }
}
