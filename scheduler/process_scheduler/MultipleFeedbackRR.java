package scheduler.process_scheduler;

import bean.PCB;
import bean.PCBsQueue;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

// round robin with multiple feedback 多级反馈队列轮转法
public class MultipleFeedbackRR {
    private static final int QUEUE_NUM = 3; // 队列数
    private static int PCB_NUM = 0; // 进程数
    private static PCBsQueue preparePCBsQueue = new PCBsQueue(); // 未到达的进程队列
    private static PCBsQueue[] PCBsQueues = new PCBsQueue[QUEUE_NUM]; // 多级反馈队列(就绪+执行)
    private static PCBsQueue overPCBsQueue = new PCBsQueue(); // 已经完成的进程队列(按完成时间先后排序)

    // JFrame初始化
    private static JFrame frame = new JFrame("进程调度模拟 -- 多级反馈队列轮转法");

    // 菜单组件初始化
    private static JMenuBar menuBar = new JMenuBar();
    private static JMenu processSettingsMenu = new JMenu("进程设置");
    private static JMenuItem createProcessItem = new JMenuItem("创建一个新进程");
    private static JMenuItem setTimeSliceItem = new JMenuItem("设置各级队列的时间片");
    private static JMenuItem clearProcessItem = new JMenuItem("清空所有进程");
    private static JMenuItem startScheduleItem = new JMenuItem("开始调度");
    private static JMenu helpMenu = new JMenu("帮助");
    private static JMenuItem aboutItem = new JMenuItem("关于");

    // 表格组件和数据
    private static JTable table = new JTable();
    private static String[] header = {"进程号","到达时间","总需执行时间","已执行时间","剩余执行时间","状态","当前所在队列"};
    private static Object[][] tableData = new Object[13][7];

    // 信息面板组件和数据
    private static JPanel infoPanel = new JPanel();
    private static JPanel panelItem1 = new JPanel();
    private static JPanel panelItem2 = new JPanel();
    private static JPanel panelItem3 = new  JPanel();
    private static JPanel panelItem4 = new JPanel();
    private static JLabel jl_currentTime = new JLabel("当前时间:");
    private static JLabel jl_timeSlice = new JLabel("各级时间片大小:");
    private static JLabel jl_totalPCBsNum = new JLabel("总进程数:");
    private static JLabel jl_overPCBsNum = new JLabel("已完成进程数:");
    private static int CURRENT_TIME = 0;
    private static int TOTAL_PCBS_NUM = 0;
    private static int OVER_PCBS_NUM = 0;
    private static int[] PCBsQueuesTimeSlice = new int[QUEUE_NUM]; // 每个队列的时间片大小
    private static int[] tempTimeSlice = new int[QUEUE_NUM]; // 临时时间片 用于记录在执行过程中当前进程还剩多少时间片
    private static int RUNNING_QUEUE = -1; // 记录当前是哪个队列的进程在运行

    // 展示队列的窗口
    private static JFrame queuesFrame = new JFrame("三级队列运行情况");
    private static JPanel queuePanel = new JPanel();

    //设置Swing的控件显示风格为Windows风格
    private static void setWindowsStyle() {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e)
        {
            e.printStackTrace();
        }
    }

    // 更新信息面板
    private static void updateInfoPanel() {
        jl_currentTime.setText("当前时间:" + CURRENT_TIME);
        jl_timeSlice.setText("各级时间片大小:" + PCBsQueuesTimeSlice[0] + "," + PCBsQueuesTimeSlice[1] + "," + PCBsQueuesTimeSlice[2]);
        jl_totalPCBsNum.setText("总进程数:" + TOTAL_PCBS_NUM);
        jl_overPCBsNum.setText("已完成进程数:" + OVER_PCBS_NUM);
    }

    // 更新表格中信息
    private static void updateTable() {
        // 新建一个列表 获取所有进程信息
        List<PCB> PCBs = new ArrayList<PCB>(preparePCBsQueue.getQueue());
        PCBs.addAll(PCBsQueues[0].getQueue());
        PCBs.addAll(PCBsQueues[1].getQueue());
        PCBs.addAll(PCBsQueues[2].getQueue());
        PCBs.addAll(overPCBsQueue.getQueue());
        Object[] obj = PCBs.toArray();

        // 用二维数组接收
        Object[][] tempData = new Object[13][7];
        for(int i=0; i< obj.length; i++) {
            PCB temp = (PCB) obj[i];
            tempData[i] = temp.getPCBInfo2();
            // 这里添加一个过滤,已完成和未到达的都没有队列
            if(tempData[i][5].equals("完成")||tempData[i][5].equals("未到达"))
                tempData[i][6] = "";
        }

        tableData = tempData;
        // 通过更新模型来更新表格
        table.setModel(new DefaultTableModel(tableData,header));
    }

    // 图形化显示内存中的多级反馈队列
    public static void showPCBQueues(PCBsQueue[] PCBsQueues) {
        int queueLocationX = 0;
        JPanel queuesPanel = new JPanel(new GridLayout(1,3,30,0));
        queuesPanel.setSize(1000,800);

        for(int i = 0; i < PCBsQueues.length; i++)
        {
            LinkedList<PCB> queue = PCBsQueues[i].getQueue();

            //创建一个PCB队列
            JPanel PCBsQueue = new JPanel(new GridLayout(10,1));
            PCBsQueue.setSize(250,700);
            // PCBsQueue.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            PCBsQueue.setBounds(queueLocationX, 0, 250, 600);
            queueLocationX += 250;

            //创建队列前面的优先级提示块
            JLabel PCBsQueuePriorityLabel = new JLabel(String.valueOf(i) + "级队列");
            PCBsQueuePriorityLabel.setFont(new Font("",Font.PLAIN,20));
            PCBsQueuePriorityLabel.setOpaque(true);
            PCBsQueuePriorityLabel.setBackground(Color.CYAN);
            PCBsQueuePriorityLabel.setForeground(Color.BLACK);

            JPanel PCBsQueuePriorityBlock = new JPanel();
            PCBsQueuePriorityBlock.add(PCBsQueuePriorityLabel);

            PCBsQueue.add(PCBsQueuePriorityBlock);

            for (PCB pcb : queue)
            {
                //JLabel默认情况下是透明的所以直接设置背景颜色是无法显示的，必须将其设置为不透明才能显示背景

                //设置pid标签
                JLabel pidLabel = new JLabel("Pid: " + String.valueOf(pcb.getPid()));
                pidLabel.setFont(new Font("",Font.PLAIN,15));
                pidLabel.setOpaque(true);
                pidLabel.setBackground(Color.PINK);
                pidLabel.setForeground(Color.BLACK);
                pidLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                //设置status标签
                JLabel statusLabel = new JLabel("状态: " + pcb.getState());
                statusLabel.setFont(new Font("",Font.PLAIN,15));
                statusLabel.setOpaque(true);
                statusLabel.setBackground(Color.PINK);
                statusLabel.setForeground(Color.BLACK);
                statusLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                //设置已运行时间标签
                JLabel runLabel = new JLabel("已运行: " + String.valueOf(pcb.getRunTime()));
                runLabel.setFont(new Font("",Font.PLAIN,15));
                runLabel.setOpaque(true);
                runLabel.setBackground(Color.PINK);
                runLabel.setForeground(Color.BLACK);
                runLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));


                //设置life标签
                JLabel lifeLabel = new JLabel("剩余: " + String.valueOf(pcb.getTotalTime()-pcb.getRunTime()));
                lifeLabel.setFont(new Font("",Font.PLAIN,15));
                lifeLabel.setOpaque(true);
                lifeLabel.setBackground(Color.PINK);
                lifeLabel.setForeground(Color.BLACK);
                lifeLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                //绘制一个PCB
                JPanel PCBPanel = new JPanel();
                PCBPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                PCBPanel.setBackground(Color.LIGHT_GRAY);
                PCBPanel.add(pidLabel);
                PCBPanel.add(statusLabel);
                PCBPanel.add(runLabel);
                PCBPanel.add(lifeLabel);

                //将PCB加入队列
                PCBsQueue.add(new DrawLinePanel());
                PCBsQueue.add(PCBPanel);


            }
            queuesPanel.add(PCBsQueue);
        }


        //设置queuesPanel中的所有PCB队列（PCBsQueue组件）按垂直方向排列
//        BoxLayout boxLayout = new BoxLayout(queuesPanel, BoxLayout.X_AXIS);

        queuePanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        queuePanel.removeAll();
        queuePanel.add(queuesPanel);
        queuePanel.updateUI();
        queuePanel.repaint();

        // 将窗口置为true
        queuesFrame.setVisible(true);
    }

    // 创建新进程
    private static void createProcess() {
        // 创建一个模态对话框
        JDialog dialog = new JDialog(frame, "创建新进程", true);
        // 设置对话框的宽高
        dialog.setSize(250, 200);
        // 设置对话框大小不可改变
        dialog.setResizable(false);
        // 设置对话框相对显示的位置
        dialog.setLocationRelativeTo(frame);

        // 创建对话框的内容面板
        JLabel jLabel1 = new JLabel("到达时间: ");
        JLabel jLabel2 = new JLabel("预估运行时间: ");
        jLabel1.setFont(new Font("", Font.PLAIN,15));
        jLabel2.setFont(new Font("", Font.PLAIN,15));
        JTextField jTextField1 = new JTextField("0");
        JTextField jTextField2 = new JTextField("1");
        JPanel panel = new JPanel(new GridLayout(3,2,0,15));
        panel.add(jLabel1);
        jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(jTextField1);
        panel.add(jLabel2);
        jLabel2.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(jTextField2);

        // 创建一个按钮用于保存设置
        JButton okBtn = new JButton("确定");
        okBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int t1 = Integer.parseInt(jTextField1.getText());
                    int t2 = Integer.parseInt(jTextField2.getText());
                    if(t1<0) {
                        JOptionPane.showMessageDialog(dialog, "到达时间需要为大于等于0的整数!");
                    }
                    else if(t2<=0){
                        JOptionPane.showMessageDialog(dialog, "预估运行时间需要为大于0的整数!");
                    }
                    else {
                        // 创建进程
                        PCB p = new PCB(TOTAL_PCBS_NUM,t1,t2);
                        TOTAL_PCBS_NUM++;
                        // 添加进程至预备队列
                        if(t1>CURRENT_TIME)
                            preparePCBsQueue.getQueue().addLast(p);
                        else
                            PCBsQueues[0].getQueue().addLast(p);
                        // 更新信息面板和表格
                        updateInfoPanel();
                        updateTable();
                        // 关闭对话框
                        dialog.dispose();
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "请输入整数!");
                    throw new RuntimeException(ex);
                } catch (HeadlessException ex) {
                    throw new RuntimeException(ex);
                }

            }
        });

        // 创建一个取消操作的按钮
        JButton cancelBtn = new JButton("取消");
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 关闭对话框
                dialog.dispose();
            }
        });

        panel.add(okBtn);
        panel.add(cancelBtn);

        // 设置对话框的内容面板
        dialog.setContentPane(panel);
        // 显示对话框
        dialog.setVisible(true);
    }

    // 设置各队列的时间片
    private static void setTimeSlice() {

        // 创建一个模态对话框
        JDialog dialog = new JDialog(frame, "选择各级队列的时间片大小", true);
        // 设置对话框的宽高
        dialog.setSize(250, 200);
        // 设置对话框大小不可改变
        dialog.setResizable(false);
        // 设置对话框相对显示的位置
        dialog.setLocationRelativeTo(frame);

        // 创建对话框的内容面板
        JLabel jLabel1 = new JLabel("一级队列: ");
        JLabel jLabel2 = new JLabel("二级队列: ");
        JLabel jLabel3 = new JLabel("三级队列: ");
        jLabel1.setFont(new Font("", Font.PLAIN,15));
        jLabel2.setFont(new Font("", Font.PLAIN,15));
        jLabel3.setFont(new Font("", Font.PLAIN,15));
        JTextField jTextField1 = new JTextField();
        JTextField jTextField2 = new JTextField();
        JTextField jTextField3 = new JTextField();
        JPanel panel = new JPanel(new GridLayout(4,2,0,15));
        panel.add(jLabel1);
        jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(jTextField1);
        panel.add(jLabel2);
        jLabel2.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(jTextField2);
        panel.add(jLabel3);
        jLabel3.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(jTextField3);

        // 创建一个按钮用于保存设置
        JButton okBtn = new JButton("确定");
        okBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int t1 = Integer.parseInt(jTextField1.getText());
                    int t2 = Integer.parseInt(jTextField2.getText());
                    int t3 = Integer.parseInt(jTextField3.getText());
                    if(t1>0 && t1<t2 && t2<t3) {
                        // 当时间片大小随着优先级降低而增大时,就保存设置
                        PCBsQueuesTimeSlice[0]=t1;
                        PCBsQueuesTimeSlice[1]=t2;
                        PCBsQueuesTimeSlice[2]=t3;
                        updateInfoPanel();
                        JOptionPane.showMessageDialog(dialog, "时间片设置成功!");
                        // 关闭对话框
                        dialog.dispose();
                    }
                    else {
                        JOptionPane.showMessageDialog(dialog, "时间片要为正整数,并且随着队列的优先级降低而增大,请重新设置!");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "请输入正整数!");
                    throw new RuntimeException(ex);
                } catch (HeadlessException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        // 创建一个取消操作的按钮
        JButton cancelBtn = new JButton("取消");
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 关闭对话框
                dialog.dispose();
            }
        });

        panel.add(okBtn);
        panel.add(cancelBtn);

        // 设置对话框的内容面板
        dialog.setContentPane(panel);
        // 显示对话框
        dialog.setVisible(true);

    }

    // 清空所有进程
    private static void clearProcess() {
        // 清空队列
        preparePCBsQueue.clearPCBs();
        PCBsQueues[0].clearPCBs();
        PCBsQueues[1].clearPCBs();
        PCBsQueues[2].clearPCBs();
        overPCBsQueue.clearPCBs();
        // 更新表格
        updateTable();
        // 重置信息
        CURRENT_TIME=0;
        TOTAL_PCBS_NUM=0;
        OVER_PCBS_NUM=0;
        // 更新信息面板
        updateInfoPanel();
    }

    // 判断当前时间有无新的进程到达,如果有就从prepare队列转到最高优先级队列的末尾(依照FCFS原则)
    private static void judgeArrive() {
        // 如果预备(未到达)进程队列不为空,就执行以下程序
        if(preparePCBsQueue.getQueue().size()!=0) {
            for(int i=0;i<preparePCBsQueue.getQueue().size();i++) {
                PCB temp = preparePCBsQueue.getQueue().get(i);
                if(temp.getArriveTime()==CURRENT_TIME){
                    // 把该进程从预备队列删除,投入到最高优先级队列的末尾,并把优先级置为0
                    temp.setPriority(0);
                    temp.setState("就绪");
                    preparePCBsQueue.getQueue().remove(i);
                    PCBsQueues[0].getQueue().addLast(temp);
                    i--;
                    // 下一秒运行的必定是最高优先级队列里的进程
//                    RUNNING_QUEUE=0;
                }
            }
        }
    }

    // 选择当前时间应该开始运行的进程,并把其他运行的进程
    private static void choosePCB() {
        int rq = RUNNING_QUEUE; // 用rq接收当前running_queue的值
        // 优先选择高优先级队列的首个进程
        if(PCBsQueues[0].getQueue().size()!=0) {
            PCBsQueues[0].getQueue().getFirst().setState("执行");
            RUNNING_QUEUE = 0;

        }
        else if(PCBsQueues[1].getQueue().size()!=0) {
            PCBsQueues[1].getQueue().getFirst().setState("执行");
            RUNNING_QUEUE = 1;
        }
        else if(PCBsQueues[2].getQueue().size()!=0){
            PCBsQueues[2].getQueue().getFirst().setState("执行");
            RUNNING_QUEUE = 2;
        }

        // 如果running_queue发生了变化(变小了),就是剥夺CPU,就要把之前在运行的进程置为就绪,并且重置时间片
        if(rq>RUNNING_QUEUE) {
            if(PCBsQueues[rq].getQueue().size()!=0) {
                // 被剥夺的进程需要中断,并且投入原队列的末尾
                PCB p = PCBsQueues[rq].getQueue().pollFirst();
                if(p!=null)
                    p.setState("就绪");
                PCBsQueues[rq].getQueue().addLast(p);
            }
            // 重置时间片
            System.arraycopy(PCBsQueuesTimeSlice, 0, tempTimeSlice, 0, 3);
        }

        if(RUNNING_QUEUE>=0 && RUNNING_QUEUE<=2 && PCBsQueues[RUNNING_QUEUE].getQueue().getFirst().getRunTime()==0) {
            // 如果是首次执行,要设置一下它的开始时间
            PCBsQueues[RUNNING_QUEUE].getQueue().getFirst().setStartTime(CURRENT_TIME);
        }
    }

    // 将在执行状态的进程运行1秒
    private static void runPCB() {
        if(RUNNING_QUEUE>=0 && RUNNING_QUEUE<=2 &&PCBsQueues[RUNNING_QUEUE].getQueue().size()!=0) {
            PCBsQueues[RUNNING_QUEUE].getQueue().getFirst().addRunTime();
            // 对应时间片-1
            tempTimeSlice[RUNNING_QUEUE]--;
            // 判断当前的进程是否执行完了
            if(PCBsQueues[RUNNING_QUEUE].getQueue().getFirst().getRunTime()==PCBsQueues[RUNNING_QUEUE].getQueue().getFirst().getTotalTime()) {
                // 设置结束时间
                PCBsQueues[RUNNING_QUEUE].getQueue().getFirst().setOverTime(CURRENT_TIME);
                PCBsQueues[RUNNING_QUEUE].getQueue().getFirst().setState("完成");
                PCBsQueues[RUNNING_QUEUE].getQueue().getFirst().setPriority(-1);
                // 从队列中弹出该进程,加入到完成队列的队尾
                PCB p = PCBsQueues[RUNNING_QUEUE].getQueue().pollFirst();
                overPCBsQueue.getQueue().addLast(p);
                // 重置时间片
                System.arraycopy(PCBsQueuesTimeSlice, 0, tempTimeSlice, 0, 3);
                // 将RUNNING_QUEUE置为空闲
                RUNNING_QUEUE = -1;
                // 已完成进程数+1
                OVER_PCBS_NUM++;
            }
            else if(tempTimeSlice[RUNNING_QUEUE]==0){
                // 如果没有执行完并且当前时间片用完了,就要降低到低一级的队列(如果已经在第三级,就放到队尾)
                PCB p = PCBsQueues[RUNNING_QUEUE].getQueue().pollFirst();
                if(p!=null) {
                    p.setState("就绪");
                    if(RUNNING_QUEUE!=2) {
                        p.setPriority(RUNNING_QUEUE+1);
                        PCBsQueues[RUNNING_QUEUE+1].getQueue().addLast(p);
                    }

                    else {
                        p.setPriority(RUNNING_QUEUE);
                        PCBsQueues[RUNNING_QUEUE].getQueue().addLast(p);
                    }

                }
                // 并且要重置时间片大小,便于后续进程的正常调度
                System.arraycopy(PCBsQueuesTimeSlice, 0, tempTimeSlice, 0, 3);
                // 将RUNNING_QUEUE置为空闲
                RUNNING_QUEUE = -1;
            }
        }
    }

    // 完成调度
    private static void finishSchedule() {
        // 调度完成后,按照完成的时间展示所有的进程
        String message = "按进程的结束时间排序:";
        for (PCB p:overPCBsQueue.getQueue()) {
            message += "\n";
            message += ("进程号: " + p.getPid() + "  ");
            message += ("开始时间: " + p.getStartTime() + "  ");
            message += ("结束时间: " + p.getOverTime() + "  ");
        }
        JOptionPane.showMessageDialog(frame, message,"调度完成！",JOptionPane.PLAIN_MESSAGE);

        // 清空数据
        clearProcess();

        // 关闭队列展示窗口
        queuesFrame.setVisible(false);
    }

    // 开始调度
    private static void startSchedule() {
        System.arraycopy(PCBsQueuesTimeSlice, 0, tempTimeSlice, 0, 3);
        // 首先清空一下已经完成的进程队列
        overPCBsQueue.clearPCBs();
        // 判断第0秒的时候有无进程到达
        judgeArrive();
        // 选择首先要执行的进程
        choosePCB();
        // 更新一下表格和信息面板
        updateTable();
        updateInfoPanel();
        showPCBQueues(PCBsQueues);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(OVER_PCBS_NUM != TOTAL_PCBS_NUM) {
                    // 每隔一秒更新一次
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    CURRENT_TIME++;
                    // 如果当前有执行状态的进程,就将它的运行时间+1,对应的时间片-1
                    // 然后判断是否要调度其他进程
                    /*
                    调度其他进程的情况有三种：
                    1. 当前进程的时间片用完,则需要把当前运行进程降级,并为更高级别的进程分配CPU.
                    2. 当前进程未用完时间片,但是这时有更高优先级的进程到达了,就要剥夺CPU,将该进程置于原队列的末尾,转而运行更高优先级的进程.
                    3. 当前进程在规定时间片内运行结束,进入完成队列.
                    */
                    runPCB();
                    // 判断当前有无新进程的到达,有则加入优先级最高的队列
                    judgeArrive();
                    // 再次遍观三个队列,选取下一个需要执行的进程(如果有剥夺CPU,就要把原来在执行的进程投入到原队列末尾)
                    choosePCB();
                    // 更新UI
                    updateTable();
                    updateInfoPanel();
                    showPCBQueues(PCBsQueues);
                }
                finishSchedule();
            }
        }).start();
    }

    // 设置监听
    private static void setComponentsListeners() {
        aboutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(frame, "Process/Job Schedule application (1.0 version)\n\n" +
                        "Copyright © 2023, 顾珺楠, All Rights Reserved.");
            }
        });
        setTimeSliceItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setTimeSlice();
            }
        });
        createProcessItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createProcess();
            }
        });
        clearProcessItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearProcess();
            }
        });
        startScheduleItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startSchedule();
            }
        });
    }

    private static void init() {
        // 设置窗口风格为Windows风格
        setWindowsStyle();

        // 设置窗口大小和位置
        frame.setSize(1000,600);
        frame.setLocation(200,200);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 添加菜单选项
        processSettingsMenu.add(createProcessItem);
        processSettingsMenu.addSeparator();
        processSettingsMenu.add(setTimeSliceItem);
        processSettingsMenu.addSeparator();
        processSettingsMenu.add(startScheduleItem);
        processSettingsMenu.addSeparator();
        processSettingsMenu.add(clearProcessItem);
        helpMenu.add(aboutItem);
        menuBar.add(processSettingsMenu);
        menuBar.add(helpMenu);
        frame.setJMenuBar(menuBar);

        // 设置表格(表格用于填入关于进程的信息)
        DefaultTableModel tableModel=new DefaultTableModel(tableData,header);
        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(30);
        JTableHeader head = table.getTableHeader(); // 创建表格标题对象
        head.setFont(new Font("", Font.PLAIN, 20));// 设置表格字体
        table.setFont(new Font("", Font.PLAIN, 18));
        JScrollPane scrollPane = new JScrollPane(table); //不添加，则表头不会显示
        frame.add(scrollPane, BorderLayout.NORTH);

        // 初始化信息面板
        infoPanel = new JPanel(new GridLayout(1,4));
        infoPanel.setSize(1000,200);
        infoPanel.add(jl_currentTime);
        infoPanel.add(jl_timeSlice);
        infoPanel.add(jl_totalPCBsNum);
        infoPanel.add(jl_overPCBsNum);
        frame.add(infoPanel);
        PCBsQueuesTimeSlice = new int[]{1, 2, 4}; // 初始化三级队列的时间片大小分别为1 2 4
        jl_currentTime.setFont(new Font("", Font.BOLD,20));
        jl_timeSlice.setFont(new Font("", Font.BOLD,20));
        jl_overPCBsNum.setFont(new Font("", Font.BOLD,20));
        jl_totalPCBsNum.setFont(new Font("", Font.BOLD,20));
        updateInfoPanel();
        frame.setVisible(true);

        // 初始化队列(优先级分别为0 1 2, 越小优先级越高)
        for(int i=0;i<PCBsQueues.length;i++) {
            PCBsQueues[i] = new PCBsQueue(i);
        }

        // 设置展示队列的界面
        queuesFrame.add(queuePanel);
        // 设置窗口大小和位置
        queuesFrame.setSize(900,500);
        queuesFrame.setLocation(200,200);
        queuesFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 设置监听
        setComponentsListeners();
    }

    public static void main(String[] args) {
        init();
    }

    public static void run() {
        init();
    }
}

//绘制直线类
class DrawLinePanel extends JPanel
{
    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        g.drawLine(this.getSize().width/2, 10, this.getSize().width/2, this.getSize().height-10);
    }
}


