package bean;

import java.util.LinkedList;

// 进程队列类
public class PCBsQueue {
    // 优先级
    private int priority;
    // 队列
    private LinkedList<PCB> queue = new LinkedList<PCB>();

    // 构造函数
    public PCBsQueue() {}
    public PCBsQueue(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public LinkedList<PCB> getQueue() {
        return queue;
    }

    public void setQueue(LinkedList<PCB> queue) {
        this.queue = queue;
    }


    // 清空队列
    public void clearPCBs() {
        this.queue.clear();
    }
}
