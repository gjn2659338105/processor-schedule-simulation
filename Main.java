import scheduler.job_scheduler.FCFS;
import scheduler.job_scheduler.HRN;
import scheduler.job_scheduler.SJF;
import scheduler.process_scheduler.MultipleFeedbackRR;
import scheduler.process_scheduler.PriorityRR;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class Main {
    private static JFrame frame = new JFrame("主控制窗口");
    private static JPanel panel = new JPanel();
    private static JLabel jLabel1 = new JLabel("进程调度算法");
    private static JLabel jLabel2 = new JLabel("作业调度算法");
    private static JButton jButton1 = new JButton("基于优先级的时间片轮转调度算法");
    private static JButton jButton2 = new JButton("多级反馈队列轮转调度算法");
    private static JButton jButton3 = new JButton("先来先服务调度算法");
    private static JButton jButton4 = new JButton("短作业优先调度算法");
    private static JButton jButton5 = new JButton("最高响应比调度算法");

    private static JFrame jtest = new JFrame("测试窗口");

    private static void init() {
        frame.setSize(600,500);
        frame.setLocation(200,200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(panel);
        // 采用网格布局
        panel = new JPanel( new GridLayout(7,1,20,20) );
//        panel.setSize(600,360);
        jLabel1.setFont(new Font("", Font.BOLD,30));
        jLabel2.setFont(new Font("", Font.BOLD,30));
        jButton1.setFont(new Font("", Font.BOLD,20));
        jButton2.setFont(new Font("", Font.BOLD,20));
        jButton3.setFont(new Font("", Font.BOLD,20));
        jButton4.setFont(new Font("", Font.BOLD,20));
        jButton5.setFont(new Font("", Font.BOLD,20));

        panel.add(jLabel1);
        panel.add(jButton1);
        panel.add(jButton2);
        panel.add(jLabel2);
        panel.add(jButton3);
        panel.add(jButton4);
        panel.add(jButton5);
        frame.add(panel);
        frame.setVisible(true);

    }

    private static void setClickListener() {
        jButton1.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PriorityRR.run();
            }
        });
        jButton2.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MultipleFeedbackRR.run();
            }
        });
        jButton3.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FCFS.run();
            }
        });
        jButton4.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SJF.run();
            }
        });
        jButton5.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HRN.run();
            }
        });
    }

    public static void main(String[] args) {
        EventQueue.invokeLater( new Runnable() {
            @Override
            public void run() {
                init();
                setClickListener();
            }
        });

    }
}