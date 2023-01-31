package scheduler.job_scheduler;

import bean.JCB;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Time;
import java.text.DecimalFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// SJF - shortest job first 最短作业优先法
public class SJF {
    private static List<JCB> JCBs = new ArrayList<JCB>(); // 作业队列
    private static JTable table; // 显示列表
    private static String[] header = {
            "作业号","提交时间","运行时间(h)","开始执行时间","状态","结束时间","周转时间"
    }; // 表头
    private static JMenuBar jMenuBar = new JMenuBar(); // 菜单栏
    private static JMenu jMenu1 = new JMenu("作业设置"); // 菜单
    private static JMenu jMenu2 = new JMenu("帮助");
    private static JMenuItem createJob = new JMenuItem("创建一个新作业");
    private static JMenuItem clearJCBs = new JMenuItem("清空所有作业");
    private static JMenuItem startSchedule = new JMenuItem("开始调度");
    private static JMenuItem about = new JMenuItem("关于");
    private static JFrame frame = new JFrame("作业调度模拟 -- 最短作业优先算法");
    private static int TABLE_ROWS = 15; // 宽
    private static int TABLE_COLS = 7; // 列

    private static Object[][] tableData = new Object[TABLE_ROWS][TABLE_COLS]; // 用于填装进表格的二维数组
    private static Integer[] hour;
    private static Integer[] minute;
    private static Integer[] second;
    private static JComboBox cmbHour; // 时间选择下拉框
    private static JComboBox cmbMinute; // 时间选择下拉框
    private static JComboBox cmbSecond; // 时间选择下拉框
    private static Time submitT;

    // 信息面板需要的组件
    private static JPanel infoPanel = new JPanel();
    private static JLabel current_time = new JLabel("当前时间:");
    private static JLabel total_JCBs_num = new JLabel("总作业数:");
    private static JLabel over_JCBs_num = new JLabel("已完成作业数:");
    private static JLabel currentTime = new JLabel();
    private static JLabel totalJCBsNum = new JLabel();
    private static JLabel overJCBsNum = new JLabel();

    // 填充面板需要的数值
    private static String CURRENT_TIME = "未开始调度";
    private static int TOTAL_JCBS_NUMS = 0;  // 当前已经创建的作业数量
    private static int OVER_JCBS_NUMS = 0; // 已经完成的作业数量
    private static int RUN_JCB_ID = -1; // 记录正在执行的作业的id


    // 设置Swing的控件显示风格为Windows风格
    private static void setWindowsStyle()
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e)
        {
            e.printStackTrace();
        }
    }

    // 显示实时时间和作业的完成情况
    private static void setInfoPanel() {
        infoPanel = new JPanel(new GridLayout(1,3));
        infoPanel.setSize(1000,200);
        JPanel panel1 = new JPanel();
        JPanel panel2 = new JPanel();
        JPanel panel3 = new JPanel();
        current_time.setFont(new Font("", Font.BOLD,20));
        currentTime.setFont(new Font("", Font.BOLD,20));
        total_JCBs_num.setFont(new Font("", Font.BOLD,20));
        totalJCBsNum.setFont(new Font("", Font.BOLD,20));
        over_JCBs_num.setFont(new Font("", Font.BOLD,20));
        overJCBsNum.setFont(new Font("", Font.BOLD,20));
        totalJCBsNum.setText(String.valueOf(TOTAL_JCBS_NUMS));
        overJCBsNum.setText(String.valueOf(OVER_JCBS_NUMS));
        panel1.add(current_time);
        panel1.add(currentTime);
        panel2.add(total_JCBs_num);
        panel2.add(totalJCBsNum);
        panel3.add(over_JCBs_num);
        panel3.add(overJCBsNum);
        infoPanel.add(panel1);
        infoPanel.add(panel2);
        infoPanel.add(panel3);
        frame.add(infoPanel);
    }

    private static void createJob() {
        newSubmitTime(frame,frame);
        float runTime = Float.parseFloat(JOptionPane.showInputDialog(frame,"设置运行时间(h)","1"));
        while(runTime <= 0)
        {
            JOptionPane.showMessageDialog(frame, "运行时间需要大于0！");
            runTime = Float.parseFloat(JOptionPane.showInputDialog(frame, "请重新输入运行时间(h)", "设置运行时间", JOptionPane.PLAIN_MESSAGE));
        }
        LocalTime lt = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        CURRENT_TIME = lt.format(formatter);
        String state = timeEqual(submitT)?"等待":"未到达";
        JCBs.add(new JCB(TOTAL_JCBS_NUMS,submitT,runTime,state));
        TOTAL_JCBS_NUMS++;
        updateTable();
        totalJCBsNum.setText(String.valueOf(TOTAL_JCBS_NUMS));
    }

    // 获取当前时间并显示
    private static void getCurrentTime() {
        LocalTime lt = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        CURRENT_TIME = lt.format(formatter);
        currentTime.setText(CURRENT_TIME);
    }

    // 判断当前时间与某一输入的时间是否相同
    private static boolean timeEqual(Time time) {
        return time.toString().equals(CURRENT_TIME);
    }

    // 从JCBs列表中找出当前时间提交的作业,设置为等待状态
    private static void judgeSubmit() {
        for(JCB j: JCBs) {
            if(timeEqual(j.getSubmitTime()))
                j.setState("等待");
        }
    }

    private static void updateTable() {
        Object[][] tempData = new Object[TABLE_ROWS][TABLE_COLS];
        Object[] obj = JCBs.toArray();
        for(int i=0; i<obj.length; i++) {
            JCB temp = (JCB) obj[i];
            tempData[i] = temp.getJCBInfo_FCFS();
        }
        tableData = tempData;
        // 通过更新模型来更新表格
        table.setModel(new DefaultTableModel(tableData,header));
    }

    private static void newSubmitTime(Frame owner, Component parentComponent) {

        // 创建一个模态对话框
        JDialog dialog = new JDialog(owner, "选择提交时间", true);

        // 设置对话框的宽高
        dialog.setSize(250, 100);
        // 设置对话框大小不可改变
        dialog.setResizable(false);
        // 设置对话框相对显示的位置
        dialog.setLocationRelativeTo(parentComponent);

        // 创建一个按钮用于关闭对话框
        JButton okBtn = new JButton("确定");
        okBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitT = saveSubmitTime();
                // 关闭对话框
                dialog.dispose();
            }
        });

        // 创建一个取消操作的按钮
        JButton cancelBtn = new JButton("取消");
        okBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 关闭对话框
                dialog.dispose();
            }
        });
        // 创建对话框的内容面板, 在面板内可以根据自己的需要添加任何组件并做任意是布局
        JPanel panel = new JPanel();

        // 添加组件到面板
        panel.add(new JLabel("时"));
        panel.add(cmbHour);
        panel.add(new JLabel("分"));
        panel.add(cmbMinute);
        panel.add(new JLabel("秒"));
        panel.add(cmbSecond);
        panel.add(okBtn);

        // 设置对话框的内容面板
        dialog.setContentPane(panel);
        // 显示对话框
        dialog.setVisible(true);

    }

    private static Time saveSubmitTime() {
        Integer h = (Integer) cmbHour.getSelectedItem();
        Integer m = (Integer) cmbMinute.getSelectedItem();
        Integer s = (Integer) cmbSecond.getSelectedItem();
        String hh = String.valueOf(h);
        String mm = String.valueOf(m);
        String ss = String.valueOf(s);

        String t = hh + ':' + mm + ':' + ss;
        return Time.valueOf(t);
    }

    private static void runJCB() {
        if(RUN_JCB_ID!=-1) {
            JCBs.get(RUN_JCB_ID).addRunTime();
        }

    }

    // 比较两个时刻的先后(目前仅限于一天24小时内,不涉及第二天)
//    private static boolean compareTwoTime(String time1,String time2) {
//        String[] t1 = time1.split(":");
//        String[] t2 = time2.split(":");
//        int h1 = Integer.parseInt(t1[0]);
//        int m1 = Integer.parseInt(t1[1]);
//        int s1 = Integer.parseInt(t1[2]);
//        int h2 = Integer.parseInt(t2[0]);
//        int m2 = Integer.parseInt(t2[1]);
//        int s2 = Integer.parseInt(t2[2]);
//
//        if(h1<h2)
//            return true;
//        if(h1==h2) {
//            if(m1<m2)
//                return true;
//            if(m1==m2)
//                return s1<s2;
//        }
//        return false;
//    }

    // 寻找新的被调度的作业(优先选择最短的作业)
    private static void findNextJCB() {
        int flag = 0;
        float t = Float.MAX_VALUE;
        for(JCB j:JCBs)
            if(j.getState().equals("等待") && j.getTotaltime()<=t) {
                flag = 1;
                RUN_JCB_ID = j.getId();
                t = j.getTotaltime();
            }
        if(flag == 1) {
            // 设置下一个作业的开始时间
            JCBs.get(RUN_JCB_ID).setStartTime(Time.valueOf(CURRENT_TIME));
            // 设置下一个作业的状态为执行
            JCBs.get(RUN_JCB_ID).setState("执行");
        }
        else
            RUN_JCB_ID = -1;

    }

    private static void scheduleOtherJCB() {
        JCBs.get(RUN_JCB_ID).setState("完成");
        JCBs.get(RUN_JCB_ID).setOverTime(Time.valueOf(CURRENT_TIME));
        OVER_JCBS_NUMS += 1;
        overJCBsNum.setText(String.valueOf(OVER_JCBS_NUMS));
        // 处理完已结束作业的信息后 开始寻找下一个被调度的作业
        findNextJCB();
    }

    // 清空JCBs队列
    private static void clearJCBs() {
        // 清空数据
        CURRENT_TIME = "未开始调度";
        TOTAL_JCBS_NUMS = 0;
        OVER_JCBS_NUMS = 0;
        RUN_JCB_ID = -1;
        currentTime.setText(CURRENT_TIME);
        totalJCBsNum.setText(String.valueOf(TOTAL_JCBS_NUMS));
        overJCBsNum.setText(String.valueOf(OVER_JCBS_NUMS));
        JCBs.clear();
        updateTable();
    }

    // 调度结束后的收尾工作
    private static void finishSchedule() {
        String message = "";
        float totalRoundTime = 0;
        for(JCB j:JCBs) {
            message += "\n";
            message += ("作业号:" + j.getId() + "  ");
            message += ("开始时间:" + j.getStartTime().toString() + "  ");
            message += ("结束时间:" + j.getOverTime().toString() + "  ");
            message += ("周转时间:" + j.getRoundTime() + "(h)");
            totalRoundTime += Float.parseFloat(j.getRoundTime());
        }
        String trt = new DecimalFormat("0.000000").format(totalRoundTime/JCBs.size());
        message += ("\n");
        message += ("平均周转时间:" + trt + "(h)");
        JOptionPane.showMessageDialog(frame, message,"调度完成！",JOptionPane.PLAIN_MESSAGE);

        // 清空数据
        clearJCBs();
    }

    private static void setComponentsListners() {
        createJob.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createJob();
            }
        });
        startSchedule.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startSchedule();
            }
        });
        clearJCBs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearJCBs();
            }
        });
        about.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JOptionPane.showMessageDialog(frame, "Process/Job Schedule application (1.0 version)\n\nCopyright © 2023, 顾珺楠, All Rights Reserved.");
            }
        });
    }

    private static void startSchedule() {
        // 开始实时读取时间 并且从当前时间开始就判断是否有作业提交
        getCurrentTime();
        judgeSubmit();
        findNextJCB();
        updateTable();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(OVER_JCBS_NUMS!=TOTAL_JCBS_NUMS) {
                    //每隔一秒更新一次
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    // 更新时间并显示
                    getCurrentTime();
                    // 判断当前时刻有无作业提交
                    judgeSubmit();
                    if(RUN_JCB_ID==-1) {
                        findNextJCB();
                    }
                    else {
                        // 运行作业1秒
                        runJCB();
                        // 如果在当前时间 某一作业执行完了，就调度其他的作业
                        if(JCBs.get(RUN_JCB_ID).getRunTime() /3600 >= JCBs.get(RUN_JCB_ID).getTotaltime()) {
                            scheduleOtherJCB();
                        }
                    }
                    updateTable();
                }
                finishSchedule();
            }
        }).start();

    }

    private static void init() {
        // 初始化时分秒选项
        hour = new Integer[24];
        minute = new Integer[60];
        second = new Integer[60];
        for (int i=0;i<24;i++) hour[i]=i;
        for (int i=0;i<60;i++) minute[i]=i;
        for (int i=0;i<60;i++) second[i]=i;
        cmbHour = new JComboBox(hour);
        cmbMinute = new JComboBox(minute);
        cmbSecond = new JComboBox(second);
        cmbHour.setSelectedIndex(0);
        cmbMinute.setSelectedIndex(0);
        cmbSecond.setSelectedIndex(0);

        // 初始化窗口风格
        setWindowsStyle();
        // 设置窗口大小和位置
        frame.setSize(1000,600);
        frame.setLocation(200,200);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 初始化菜单组件
        jMenu1.add(createJob);
        jMenu1.add(clearJCBs);
        jMenu1.add(startSchedule);
        jMenu2.add(about);
        jMenuBar.add(jMenu1);
        jMenuBar.add(jMenu2);
        frame.setJMenuBar(jMenuBar);

        // 设置表格
        DefaultTableModel tableModel=new DefaultTableModel(tableData,header);
        table = new JTable(tableModel);
//        table = new AbstractTableModel(tableData,header);
//        table.setPreferredScrollableViewportSize(new Dimension(1000,400));
        table.setFillsViewportHeight(true);
        table.setRowHeight(30);
        JTableHeader head = table.getTableHeader(); // 创建表格标题对象
        head.setFont(new Font("", Font.PLAIN, 20));// 设置表格字体
        table.setFont(new Font("", Font.PLAIN, 18));
        JScrollPane scrollPane = new JScrollPane(table); //不添加，则表头不会显示
        frame.add(scrollPane, BorderLayout.NORTH);

        // 设置信息面板
        setInfoPanel();

        frame.setVisible(true);

        // 为组件添加监听事件
        setComponentsListners();
    }

    public static void run() {
        init();
    }

    public static void main(String[] args) {
        init();
    }
}
