package scheduler.process_scheduler;

import bean.PCB;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

// round robin based on priority 基于优先级的时间片轮转法
public class PriorityRR{
    private static int MIN_PRIORITY = 1; // 进程优先级最小初值
    private static int MAX_PRIORITY = 100; // 进程优先级最大初值
    private static int PRIORITY_DECLINE_NUM = 2; // 每执行完一个时间片后的优先级下降数
    private static int tempSlice; // 执行的时候 当前时间片的剩余值
    private static JFrame frame = new JFrame("进程调度模拟 -- 基于优先级的时间片轮转法");
    private static JPanel infoPanel = new JPanel();
    private static JMenuBar menuBar = new JMenuBar();
    private static JMenu processMenu = new JMenu("进程设置");
    private static JMenuItem createProcess = new JMenuItem("创建一个新进程");
    private static JMenuItem sliceSetting = new JMenuItem("时间片设置");
    private static JMenuItem clearPCBs = new JMenuItem("清空所有进程");
    private static JMenuItem startSchedule = new JMenuItem("开始调度");
    private static JMenu helpMenu = new JMenu("帮助");
    private static JMenuItem about = new JMenuItem("关于");
    private static String[] header = {"进程名","优先级","到达时间","总需执行时间","已执行时间","剩余执行时间","状态"}; // 表头
    private static int TABLE_ROWS = 15;
    private static int TABLE_COLS = 7;
    private static JTable table;
    private static List<PCB> inarrive_PCBS = new ArrayList<PCB>(); // 未到达的进程数组
    private static List<PCB> PCBs = new ArrayList<PCB>(); // 正在执行/就绪的进程数组
    private static List<PCB> overPCBs = new ArrayList<PCB>(); // 已经执行完的进程数组
    private static Object[][] tableData = new Object[TABLE_ROWS][TABLE_COLS]; // 用于填装进表格的二维数组

    private static int CURRENT_TIME = 0; // 起始时间为0s
    private static int TOTAL_PCBS_NUM = 0; // 总进程数
    private static int OVER_PCBS_NUM = 0; // 已经完成的进程数
    private static int timeSlice = 3; // 时间片大小
    private static JLabel slice = new JLabel("时间片: ");
    private static JLabel time = new JLabel("时间: ");
    private static JLabel totalNum = new JLabel("总进程数: ");
    private static JLabel overNum = new JLabel("已完成进程数: ");
    private static JLabel jl_timeSlice;
    private static JLabel jl_currentTime;
    private static JLabel jl_totalNum;
    private static JLabel jl_overNum;
    private static JPanel panel1 = new JPanel();
    private static JPanel panel2 = new JPanel();
    private static JPanel panel3 = new JPanel();
    private static JPanel panel4 = new JPanel();

    //设置Swing的控件显示风格为Windows风格
    public static void setWindowsStyle() {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e)
        {
            e.printStackTrace();
        }
    }

    public static void setComponentsListeners() {
        createProcess.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createProcess();
            }
        });

        sliceSetting.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setTimeSlice();
            }
        });

        startSchedule.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                 startSchedule();
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

        clearPCBs.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                clearAllPCBs();
            }
        });
    }

    // 根据优先级对List<PCB>排序 * 排序前要先把所有进程的状态都置为就绪状态
    /*
        需要排序的情况有两种：
        1. 当前进程在一个时间片内已经完成，这时需要把该进程置为完成状态，并从队列PCBs中去除，挑选新的优先级最高的进程来执行；
        2. 当前进程用完一个时间片尚未结束，剥夺cpu，降低该进程的优先级，并把其他优先级更好的进程提上来。
    */
    private static void sortPCBs() {
        if(PCBs.size()!=0) {
            PCBs.sort(new Comparator<PCB>() {
                @Override
                public int compare(PCB o1, PCB o2) {
                    return o1.getPriority() - o2.getPriority();
                }
            });
            // 排序后将第一个进程状态置为"执行"
            PCBs.get(0).setState("执行");
        }
//        updataTable();
    }

    // 更改正在执行的进程的信息(逐秒更改)
    private static void runPCB() {
        if(PCBs.size()!=0) {
            if(PCBs.get(0).getRunTime()==0)
                PCBs.get(0).setStartTime(CURRENT_TIME-1);
            PCBs.get(0).addRunTime();
        }
//        updataTable();
    }

    // 一个时间片用完后，进程还未执行完，就要剥夺CPU，降低它的优先级并且重新排序
    private static void depriveCPU() {
        PCBs.get(0).setState("就绪");
        PCBs.get(0).deClinePriority(PRIORITY_DECLINE_NUM);
        sortPCBs();
    }

    // 将PCBs中执行完的进程放到overPCBs中
    private static void removePCB() {
        PCBs.get(0).setState("完成");
        PCBs.get(0).setOverTime(CURRENT_TIME);
        overPCBs.add(PCBs.get(0));
        PCBs.remove(0);
        // 移除成功后，再进行排序并更新界面
        sortPCBs();
        // 增加一个已经完成的进程数
        OVER_PCBS_NUM += 1;
        jl_overNum.setText(String.valueOf(OVER_PCBS_NUM));
    }

    // 判断当前时间有没有进程到达,如果有就将对应进程的状态置为”就绪“,并移入PCBs当中
    private static void judgeArrive() {
        for (int i=0; i < inarrive_PCBS.size(); i++) {
            if (inarrive_PCBS.get(i).getArriveTime() == CURRENT_TIME) {
                inarrive_PCBS.get(i).setState("就绪");
                PCB temp = inarrive_PCBS.get(i);
                PCBs.add(temp);
                inarrive_PCBS.remove(i);
                i-=1;
            }
        }
    }

    // 更新显示的表格信息
    private static void updataTable() {
        // 把List<PCB>转为Object[]类型的一维数组
        Object[] obj1 = PCBs.toArray();
        Object[] obj2 = inarrive_PCBS.toArray();
        Object[] obj3 = overPCBs.toArray();
        Object[] obj = new Object[obj1.length + obj2.length + obj3.length];
        System.arraycopy(obj1, 0, obj, 0, obj1.length);
        System.arraycopy(obj2, 0, obj, obj1.length, obj2.length);
        System.arraycopy(obj3, 0, obj, obj1.length+obj2.length, obj3.length);
        PCB temp;
        Object[][] tempData = new Object[TABLE_ROWS][TABLE_COLS];
        // 把PCB读出的信息装入table中
        for(int i=0; i< obj.length; i++) {
            temp = (PCB) obj[i];
            tempData[i] = temp.getPCBInfo();
        }
        tableData = tempData;
        // 通过更新模型来更新表格
        table.setModel(new DefaultTableModel(tableData,header));
//        table.validate();
//        table.updateUI();
    }

    // 创建新进程
    public static void createProcess() {
        String name=JOptionPane.showInputDialog("请输入进程名","P");
        int priority=Integer.parseInt(JOptionPane.showInputDialog("请输入优先级","1"));
        while(priority < MIN_PRIORITY || priority > MAX_PRIORITY)
        {
            JOptionPane.showMessageDialog(frame, "优先级应为1到100间的正整数！");
            priority = Integer.parseInt(JOptionPane.showInputDialog(frame, "请重新输入优先级", "设置优先级", JOptionPane.PLAIN_MESSAGE));
        }
        int arriveTime =Integer.parseInt(JOptionPane.showInputDialog("请输入到达时间","0"));
        while(arriveTime < 0)
        {
            JOptionPane.showMessageDialog(frame, "到达时间需要大于等于0！");
            arriveTime = Integer.parseInt(JOptionPane.showInputDialog(frame, "请重新输入到达时间", "设置到达时间", JOptionPane.PLAIN_MESSAGE));
        }
        int runTime=Integer.parseInt(JOptionPane.showInputDialog("请输入运行时间","1"));
        while(runTime <= 0)
        {
            JOptionPane.showMessageDialog(frame, "运行时间需要大于0！");
            runTime = Integer.parseInt(JOptionPane.showInputDialog(frame, "请重新输入运行时间", "设置运行时间", JOptionPane.PLAIN_MESSAGE));
        }
        if(arriveTime == CURRENT_TIME)
            PCBs.add(new PCB(name,arriveTime,runTime,priority));
        else
            inarrive_PCBS.add(new PCB(name,arriveTime,runTime,priority));
        updataTable();
        TOTAL_PCBS_NUM += 1;
        jl_totalNum.setText(String.valueOf(TOTAL_PCBS_NUM));
        JOptionPane.showMessageDialog(null, "新进程创建成功！","创建成功",JOptionPane.PLAIN_MESSAGE);
    }

    // 清空已有进程
    private static void clearAllPCBs() {
        TOTAL_PCBS_NUM = 0;
        jl_totalNum.setText(String.valueOf(TOTAL_PCBS_NUM));
        OVER_PCBS_NUM = 0;
        jl_overNum.setText(String.valueOf(OVER_PCBS_NUM));
        PCBs.clear();
        inarrive_PCBS.clear();
        overPCBs.clear();
        updataTable();
    }

    //设置时间片大小
    private static void setTimeSlice() {
        String inputMsg = JOptionPane.showInputDialog(frame, "请输入时间片大小(s)", 3);

        int timeSliceInput = Integer.parseInt(inputMsg);

        while(timeSliceInput <= 0)
        {
            JOptionPane.showMessageDialog(frame, "时间片要设置为正整数，请重新设置!");
            inputMsg = JOptionPane.showInputDialog(frame, "请输入时间片大小(s)", "设置时间片", JOptionPane.PLAIN_MESSAGE);
            timeSliceInput = Integer.parseInt(inputMsg);
        }

        timeSlice = timeSliceInput;
        jl_timeSlice.setText(String.valueOf(timeSlice));
    }

    // 调度结束后的收尾工作
    private static void finishSchedule() {
        overPCBs.sort(new Comparator<PCB>() {
            @Override
            public int compare(PCB o1, PCB o2) {
                return o1.getStartTime()-o2.getStartTime();
            }
        });
        String message = "";
        for (PCB p:overPCBs) {
            message += "\n";
            message += ("进程名: " + p.getName() + "  ");
            message += ("开始时间: " + p.getStartTime() + "  ");
            message += ("结束时间: " + p.getOverTime() + "  ");
        }
        JOptionPane.showMessageDialog(frame, message,"调度完成！",JOptionPane.PLAIN_MESSAGE);

        // 清空数据
        CURRENT_TIME = 0;
        jl_currentTime.setText(String.valueOf(CURRENT_TIME));
        timeSlice = 3;
        jl_timeSlice.setText(String.valueOf(timeSlice));
        TOTAL_PCBS_NUM = 0;
        jl_totalNum.setText(String.valueOf(TOTAL_PCBS_NUM));
        OVER_PCBS_NUM = 0;
        jl_overNum.setText(String.valueOf(OVER_PCBS_NUM));
        PCBs.clear();
        inarrive_PCBS.clear();
        overPCBs.clear();
        updataTable();
    }

    // 开始调度
    private static void startSchedule() {
        tempSlice = timeSlice;
        // 首先清空已完成的进程数组
        overPCBs.clear();
        sortPCBs();
        updataTable();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(OVER_PCBS_NUM!=TOTAL_PCBS_NUM) {
                    //每隔一秒更新一次
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }

                    // 每过一秒 时间就+1 当前时间片-1
                    CURRENT_TIME+=1;
                    jl_currentTime.setText(String.valueOf(CURRENT_TIME));
                    tempSlice -= 1;
                    // 当前执行的进程 已运行时间+1
                    runPCB();
                    // 判断当前时间有没有进程到达
                    judgeArrive();

                    if(PCBs.size()!=0) {
                        // 如果进程执行完了
                        if(PCBs.get(0).getTotalTime() == PCBs.get(0).getRunTime()) {
                            removePCB();
                            tempSlice = timeSlice;
                        }
                        else {
                            // 如果当前时间片用完了
                            if(tempSlice == 0) {
                                depriveCPU();
                                tempSlice = timeSlice;
                            }
                        }
                    }

                    // 过了一秒后，等前面的判断和操作全部完成，再调用updataTable
                    updataTable();
                }
                finishSchedule();
            }
        }).start();
    }

    // 展示时间

    private static void init() {
        // 设置窗口风格为Windows风格
        setWindowsStyle();

        // 设置窗口大小和位置
        frame.setSize(1000,600);
        frame.setLocation(200,200);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 添加菜单选项
        processMenu.add(createProcess);
        processMenu.add(sliceSetting);
        processMenu.add(startSchedule);
        processMenu.add(clearPCBs);
        helpMenu.add(about);
        menuBar.add(processMenu);
        menuBar.add(helpMenu);
        frame.setJMenuBar(menuBar);

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

        // 设置运行信息
        jl_currentTime = new JLabel(String.valueOf(CURRENT_TIME));
        jl_timeSlice = new JLabel(String.valueOf(timeSlice));
        jl_totalNum = new JLabel(String.valueOf(TOTAL_PCBS_NUM));
        jl_overNum = new JLabel(String.valueOf(OVER_PCBS_NUM));
        infoPanel = new JPanel(new GridLayout(1,4));
        infoPanel.setSize(1000,200);
//        infoPanel.setBackground(Color.YELLOW);
        frame.add(infoPanel);
        slice.setFont(new Font("", Font.BOLD,20));
        time.setFont(new Font("", Font.BOLD,20));
        totalNum.setFont(new Font("", Font.BOLD,20));
        overNum.setFont(new Font("", Font.BOLD,20));
        jl_overNum.setFont(new Font("", Font.BOLD,20));
        jl_totalNum.setFont(new Font("", Font.BOLD,20));
        jl_timeSlice.setFont(new Font("", Font.BOLD,20));
        jl_currentTime.setFont(new Font("", Font.BOLD,20));
        panel1.add(time);
        panel1.add(jl_currentTime);
        panel2.add(slice);
        panel2.add(jl_timeSlice);
        panel3.add(totalNum);
        panel3.add(jl_totalNum);
        panel4.add(overNum);
        panel4.add(jl_overNum);
        infoPanel.add(panel1);
        infoPanel.add(panel2);
        infoPanel.add(panel3);
        infoPanel.add(panel4);

        // 设置监听
        setComponentsListeners();

        // frame设置为可见
        frame.setVisible(true);
    }

    public static void run() {
        init();
    }

    public static void main(String[] args) {
        init();
    }
}
